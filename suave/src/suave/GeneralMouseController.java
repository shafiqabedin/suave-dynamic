/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.opengl.GL;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * I'm refactoring the code out of Select, that handles MouseEvents. For
 * now I'm just throwing all of that code into this class.  Later on maybe will
 * sort out something more rational.  @TODO: Eventually split all this stuff out
 * to other Controllers.  I guess the plan would be to have each controller have
 * an 'active' flag and based on mode changes by user, set one active and the
 * others inactive.
 *
 * @author owens
 */
public class GeneralMouseController implements Observer, GeoTransformsConstants {

    private static final Logger logger = Logger.getLogger(GeneralMouseController.class.getName());
    private static final boolean PLACE_TARGET_MARKER = true;
    private final static boolean DEBUG_DRAW_CLICK_LINE = false;
    private final static boolean DEBUG_MARK_TRIANGLE_VERTICES = false;
    private final static boolean DEBUG_PRINT_AND_DRAW_MARKERS = false;
    private final static boolean DEBUG_DO_WHITEOUT_TO_TEST_RASTERIZER = false;
    private final static boolean DEBUG_DUMP_TRIANGLE_FRAMELIST = true;
    private final static int DEFAULT_FRAME_INDEX = 17060;
    private final static int FRAMES_BEFORE = 60;
    private final static int FRAMES_AFTER = 60;
    private Origin origin;
    private GLCamera camera;
    private StateDB stateDB;
    private TextureDB textureDB;
    private String imagedir;
    private Model model;
    private JPopupMenu jpm;
    int pictureindex;
    ArrayList filelist;
    String reffilename;
    JLabel affinePicture = new JLabel("Affine", JLabel.CENTER);

//    private boolean pressed = false;
//    private int button = 0;
//    private ArrayList<float[]> draggedPoints = new ArrayList<float[]>();
    public GeneralMouseController(Origin origin, GLCamera camera, StateDB stateDB, TextureDB textureDB, Model model, String imagedir) {
        this.origin = origin;
        this.camera = camera;
        this.stateDB = stateDB;
        this.textureDB = textureDB;
        this.model = model;
        this.imagedir = imagedir;
    }

    private void drawClickLine(GL gl, float x1, float y1, float z1, float x2, float y2, float z2) {
        Renderable vbo;
        float[][] linePoints;
        int lineIndex;
        int size = 2;

        linePoints = new float[size][];
        for (int loopi = 0; loopi < size; loopi++) {
            linePoints[loopi] = new float[3];
        }

        lineIndex = 0;

        linePoints[lineIndex][0] = x1;
        linePoints[lineIndex][1] = y1;
        linePoints[lineIndex][2] = z1;
        lineIndex++;
        linePoints[lineIndex][0] = x2;
        linePoints[lineIndex][1] = y2;
        linePoints[lineIndex][2] = z2;
        Color lineColor = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 128);
        TextureInfo ti = textureDB.getColorTexture(gl, 32, lineColor);
        vbo = RenderableVBOFactory.buildInterleavedVBOLine(gl, ti.textID, linePoints);
        model.addRenderable("SELECTION", vbo, 0.0f, 0.0f, 0.0f);
    }

    private void drawBoundary(GL gl, float x1, float y1, float z1, float x2, float y2, float z2) {
        Renderable vbo;
        float[][] linePoints;
        int lineIndex;
        int size = 2;

        linePoints = new float[size][];
        for (int loopi = 0; loopi < size; loopi++) {
            linePoints[loopi] = new float[3];
        }

        lineIndex = 0;

        linePoints[lineIndex][0] = x1;
        linePoints[lineIndex][1] = y1;
        linePoints[lineIndex][2] = z1;
        lineIndex++;
        linePoints[lineIndex][0] = x2;
        linePoints[lineIndex][1] = y2;
        linePoints[lineIndex][2] = z2;
        Color lineColor = new Color(Color.green.getRed(), Color.green.getGreen(), Color.green.getBlue(), 128);
        TextureInfo ti = textureDB.getColorTexture(gl, 32, lineColor);
        vbo = RenderableVBOFactory.buildInterleavedVBOLine(gl, ti.textID, linePoints);
        model.addRenderable("SELECTION", vbo, 0.0f, 0.0f, 0.0f);
    }
    Renderable clickMarker = null;
    Renderable triangleMarker = null;

    private void debugDrawMarkers(GL gl, Triangle t, float[] hitPos) {
        if (null == clickMarker) {
            Color clickColor = new Color(Color.yellow.getRed(), Color.yellow.getGreen(), Color.yellow.getBlue(), 128);
            TextureInfo ti = textureDB.getColorTexture(gl, 32, clickColor);
            clickMarker = RenderableVBOFactory.buildSphere(gl, ti.textID, 10);

            Color triangleColor = new Color(Color.white.getRed(), Color.white.getGreen(), Color.white.getBlue(), 128);
            TextureInfo ti2 = textureDB.getColorTexture(gl, 32, triangleColor);
            triangleMarker = RenderableVBOFactory.buildSphere(gl, ti.textID, 10);
        }

        model.addRenderable("SELECTION_MARKER_HIT",
                clickMarker,
                hitPos[0],
                hitPos[1],
                hitPos[2]);

        if (DEBUG_MARK_TRIANGLE_VERTICES) {
            model.addRenderable("SELECTION_MARKER1", triangleMarker, t.v1.x, t.v1.y, t.v1.z);
            model.addRenderable("SELECTION_MARKER2", triangleMarker, t.v2.x, t.v2.y, t.v2.z);
            model.addRenderable("SELECTION_MARKER3", triangleMarker, t.v3.x, t.v3.y, t.v3.z);
        }
    }

    private void updateOld(Observable o, Object arg) {
        boolean pressed = false;
        int button = 0;

        SelectEvent se = (SelectEvent) arg;
        if (se.e.getID() == se.e.MOUSE_PRESSED) {
            // @TODO: THis button code needs to be re-done properly.
            if ((se.e.getModifiers() & se.e.BUTTON1_MASK) != 0) {
                pressed = true;
                button = 1;
            } else if ((se.e.getModifiers() & se.e.BUTTON2_MASK) != 0) {
                button = 2;
            } else if ((se.e.getModifiers() & se.e.BUTTON3_MASK) != 0) {
                button = 3;
            }
        } else if (se.e.getID() == se.e.MOUSE_RELEASED) {
            pressed = false;
            button = 0;
        }

        model.setSelectionPressed(pressed, se.e);

        if (DEBUG_DRAW_CLICK_LINE) {
            // Draw A Line From 5 Points Below Our Viewpoint To The
            // Click Point On The Clip Plane, For Debugging.
            drawClickLine(se.gl, se.eyePos[0], se.eyePos[1], se.eyePos[2] - 5, se.worldPos[0], se.worldPos[1], se.worldPos[2]);
        }

        model.setSelectionPoint(se.hitPos[0], se.hitPos[1], se.hitPos[2]);

        if (DEBUG_PRINT_AND_DRAW_MARKERS) {
            debugDrawMarkers(se.gl, se.t, se.hitPos);
        }

        if (button == 3) {
            if (DEBUG_DO_WHITEOUT_TO_TEST_RASTERIZER) {
                model.debugWhiteOutTriangle(se.t);
            } else {
                // @TODO: Need to move this 'add video' code into
                // model/state or something... into a controller.
                // Really this whole class should be refactored.
                ArrayList<Integer> frameList = null;
                frameList = se.t.getFrameList();
                if (DEBUG_DUMP_TRIANGLE_FRAMELIST) {
                    if (null == frameList) {
                        Debug.debug(1, "Select.selectTriangle: NO FRAMES ASSOCIATED WITH TRIANGLE =" + se.t);
                    } else {
                        Debug.debug(1, "Select.selectTriangle: Printing frames");
                        for (int loopi = 0; loopi < frameList.size(); loopi++) {
                            Debug.debug(1, "Select.selectTriangle:         " + frameList.get(loopi));
                        }
                        Debug.debug(1, "Select.selectTriangle: Done printing frames");
                    }
                }
                int frameIndex = 0;
                if (null == frameList) {
                    Debug.debug(1, "Select.selectTriangle: for video, no framelist associated with this triangle, using default frame index=" + DEFAULT_FRAME_INDEX);
                    frameIndex = DEFAULT_FRAME_INDEX;
                } else {
                    frameIndex = frameList.get(frameList.size() - 1);
                    Debug.debug(1, "Select.selectTriangle: for video, found framelist with last frame index==" + frameIndex);
                }
                RenderableVideo rv = RenderableVideoFactory.build(se.gl, model, camera, imagedir, frameIndex - FRAMES_BEFORE, frameIndex + FRAMES_AFTER, se.hitPos[0], se.hitPos[1], se.hitPos[2]);
                model.addRenderable(RenderableVideo.VIDEO_POPUP_KEY, rv, se.hitPos[0], se.hitPos[1], se.hitPos[2]);
                button = 0;
            }
        } else if (button == 1 || button == 2) {
            model.removeRenderable(RenderableVideo.VIDEO_POPUP_KEY);
        }

    }
    int markerCounter = 0;
    String currentMarkerKey = null;
    State currentMarker = null;
    // NOTE: Make sure SELECT_RANGE is large enough to take into account that the marker position
    // will be MARKER_HEIGHT_ABOVE_HIT_POSITION above the original hit position.
    private double SELECT_RANGE = 80;
    private float MARKER_HEIGHT_ABOVE_HIT_POSITION = 40;

    private State findClosest(SelectEvent se, double range, boolean highlighted, boolean selected) {
        State[] stateAry = stateDB.getStates();
        if (stateAry == null) {
            return null;
        }
        if (stateAry.length == 0) {
            return null;
        }
        double rangeSqd = range * range;
        double closestRangeSqd = 0;
        State closest = null;
        for (int loopi = 0; loopi < stateAry.length; loopi++) {
            State state = stateAry[loopi];
            if (state == null) {
                continue;
            }
            if (state.getType() != StateEnums.StateType.TARGET_MARKER) {
                continue;
            }
            if (highlighted) {
                state.setHighlighted(false);
            }
            if (selected) {
                state.setSelected(false);
            }
            double xdiff = se.hitPos[0] - state.getXPos();
            double ydiff = se.hitPos[1] - state.getYPos();
            double zdiff = se.hitPos[2] - state.getZPos();
            double distSqd = xdiff * xdiff + ydiff * ydiff + zdiff * zdiff;
            if (distSqd > rangeSqd) {
                continue;
            }
            if (closest == null || distSqd < closestRangeSqd) {
                closest = state;
                closestRangeSqd = distSqd;
            }
        }
        return closest;
    }

    public void updateSuaveExperiment(Observable o, Object arg) {
        SelectEvent se = (SelectEvent) arg;
        if ((se.e.getID() == se.e.MOUSE_MOVED) && (currentMarker == null)) {
            State state = findClosest(se, SELECT_RANGE, true, false);
            if (null != state) {
                state.setHighlighted(true);
            }
            return;
        }
        double[] ogl = new double[]{se.hitPos[0], se.hitPos[1], se.hitPos[2]};
        double[] lvcs = new double[3];
        double[] gps = new double[3];
        origin.openGLToLvcs(ogl, lvcs);
        origin.lvcsToGpsDegrees(lvcs, gps);

        if (se.e.getButton() == se.e.BUTTON3 && se.e.getID() == se.e.MOUSE_PRESSED) {
            currentMarker = findClosest(se, SELECT_RANGE, false, true);
            if (null != currentMarker) {
                execPopUp(lvcs, gps, ogl, se);
//                currentMarker.setDeleted(true);
//                currentMarker.setDirty(true);
//                logger.info("\tTARGETMARKER"
//                        + "\t" + System.currentTimeMillis()
//                        + "\tPRESSEDDELETED"
//                        + "\t" + currentMarker.getKey()
//                        + "\t" + lvcs[0]
//                        + "\t" + lvcs[1]
//                        + "\t" + lvcs[2]
//                        + "\t" + gps[0]
//                        + "\t" + gps[1]
//                        + "\t" + gps[2]);
//                currentMarker = null;
                //Create the popup menu.

            }
            return;
        }

        if (se.e.getButton() != se.e.BUTTON1 && (se.e.getID() != se.e.MOUSE_DRAGGED)) {
            return;
        }

        if ((se.e.getID() == se.e.MOUSE_PRESSED)) {

            if (currentMarker == null) {
                currentMarker = findClosest(se, SELECT_RANGE, false, true);
                if (null == currentMarker) {

                    currentMarkerKey = StateEnums.StateType.TARGET_MARKER + "_" + markerCounter++;
                    currentMarker = new State(currentMarkerKey, StateEnums.StateType.TARGET_MARKER);
                    currentMarker.setHasLine(false);
                    stateDB.put(currentMarker);
                    logger.info("\tTARGETMARKER"
                            + "\t" + System.currentTimeMillis()
                            + "\tPRESSEDCREATED"
                            + "\t" + currentMarker.getKey()
                            + "\t" + lvcs[0]
                            + "\t" + lvcs[1]
                            + "\t" + lvcs[2]
                            + "\t" + gps[0]
                            + "\t" + gps[1]
                            + "\t" + gps[2]);

                } else {
                    logger.info("\tTARGETMARKER"
                            + "\t" + System.currentTimeMillis()
                            + "\tPRESSEDSELECTED"
                            + "\t" + currentMarker.getKey()
                            + "\t" + lvcs[0]
                            + "\t" + lvcs[1]
                            + "\t" + lvcs[2]
                            + "\t" + gps[0]
                            + "\t" + gps[1]
                            + "\t" + gps[2]);
                }
                currentMarker.setSelected(true);
            }
            currentMarker.setPos(se.hitPos[0], se.hitPos[1] + MARKER_HEIGHT_ABOVE_HIT_POSITION, se.hitPos[2]);
        } else if (((se.e.getID() == se.e.MOUSE_RELEASED)) && (currentMarker != null)) {
            logger.info("\tTARGETMARKER"
                    + "\t" + System.currentTimeMillis()
                    + "\tRELEASED"
                    + "\t" + currentMarker.getKey()
                    + "\t" + lvcs[0]
                    + "\t" + lvcs[1]
                    + "\t" + lvcs[2]
                    + "\t" + gps[0]
                    + "\t" + gps[1]
                    + "\t" + gps[2]);
            currentMarker.setPos(se.hitPos[0], se.hitPos[1] + MARKER_HEIGHT_ABOVE_HIT_POSITION, se.hitPos[2]);
            currentMarker.setSelected(false);
            currentMarker = null;
        } else if ((se.e.getID() == se.e.MOUSE_DRAGGED) && (null != currentMarker)) {
            logger.info("\tTARGETMARKER"
                    + "\t" + System.currentTimeMillis()
                    + "\tDRAGGED"
                    + "\t" + currentMarker.getKey()
                    + "\t" + lvcs[0]
                    + "\t" + lvcs[1]
                    + "\t" + lvcs[2]
                    + "\t" + gps[0]
                    + "\t" + gps[1]
                    + "\t" + gps[2]);

            currentMarker.setPos(se.hitPos[0], se.hitPos[1] + MARKER_HEIGHT_ABOVE_HIT_POSITION, se.hitPos[2]);
        }
    }

    public void update(Observable o, Object arg) {
        if (PLACE_TARGET_MARKER) {
            updateSuaveExperiment(o, arg);
        } else {
            updateOld(o, arg);
        }
    }

    public void execPopUp(final double[] lv, final double[] gp, double[] ogl, final SelectEvent s) {

        jpm = new JPopupMenu();
        JMenuItem menuItemDel;
        JMenuItem menuItemBillboard;
        JMenuItem menuItemClose;
        final double[] oglPosition = ogl;

        //Delete marker option
        menuItemDel = new JMenuItem("Delete Marker");
        jpm.add(menuItemDel);
        //adding action listener to menu items
        menuItemDel.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        currentMarker = findClosest(s, SELECT_RANGE, false, true);
                        if (null != currentMarker) {
                            currentMarker.setDeleted(true);
                            currentMarker.setDirty(true);
                            logger.info("\tTARGETMARKER"
                                    + "\t" + System.currentTimeMillis()
                                    + "\tPRESSEDDELETED"
                                    + "\t" + currentMarker.getKey()
                                    + "\t" + lv[0]
                                    + "\t" + lv[1]
                                    + "\t" + lv[2]
                                    + "\t" + gp[0]
                                    + "\t" + gp[1]
                                    + "\t" + gp[2]);
                            currentMarker = null;
                            //Create the popup menu.

                        }
//                        int[][][] waypoints1 = {{{650, 1000}, {648, 1027}, {644, 1092}, {640, 1162}, {636, 1228}, {632, 1290}, {629, 1354}, {625, 1424}, {621, 1495}, {616, 1568}, {612, 1644}, {609, 1709}, {605, 1776}, {600, 1847}, {597, 1919}, {592, 1988}, {589, 2048}, {586, 2108}, {583, 2154}, {582, 2154}, {582, 2154}, {582, 2154}, {582, 2154}, {582, 2154}, {582, 2154}, {582, 2154}, {582, 2154}, {582, 2154}, {582, 2154}, {582, 2154}, {582, 2154}, {622, 2187}, {671, 2229}, {722, 2272}, {773, 2315}, {821, 2355}, {870, 2397}, {925, 2443}, {977, 2487}, {1033, 2535}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1082, 2576}, {1060, 2619}, {1030, 2680}, {999, 2744}, {968, 2806}, {936, 2870}, {904, 2935}, {873, 2997}, {844, 3056}, {815, 3116}, {784, 3178}, {749, 3249}, {719, 3310}, {687, 3374}, {654, 3441}, {621, 3508}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}, {597, 3557}},
//                            {{601, 853}, {598, 846}, {641, 831}, {705, 803}, {767, 776}, {827, 749}, {886, 722}, {951, 692}, {1015, 663}, {1082, 632}, {1152, 600}, {1212, 572}, {1273, 544}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1293, 535}, {1353, 532}, {1418, 529}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1452, 527}, {1496, 542}, {1563, 564}, {1630, 586}, {1697, 608}, {1765, 630}, {1832, 653}, {1894, 673}, {1957, 694}, {2023, 716}, {2070, 731}, {2115, 746}, {2181, 767}, {2249, 790}, {2321, 814}, {2386, 835}, {2450, 856}, {2519, 879}, {2588, 901}, {2652, 923}, {2720, 945}, {2784, 967}, {2851, 989}, {2917, 1010}, {2976, 1030}, {3036, 1049}, {3094, 1068}, {3142, 1084}, {3142, 1084}, {3142, 1084}, {3142, 1084}, {3142, 1084}, {3142, 1084}, {3142, 1084}},
//                            {{1284, 3094}, {1291, 3104}, {1345, 3143}, {1400, 3187}, {1452, 3230}, {1502, 3270}, {1552, 3310}, {1605, 3352}, {1659, 3396}, {1713, 3440}, {1773, 3488}, {1824, 3529}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1860, 3558}, {1873, 3554}, {1917, 3504}, {1964, 3459}, {2013, 3413}, {2058, 3370}, {2105, 3326}, {2155, 3279}, {2205, 3232}, {2258, 3181}, {2310, 3132}, {2359, 3085}, {2409, 3039}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2420, 3028}, {2409, 3050}, {2389, 3066}, {2336, 3112}, {2284, 3159}, {2232, 3208}, {2180, 3257}, {2130, 3306}, {2083, 3351}, {2034, 3397}, {1984, 3444}, {1926, 3499}, {1877, 3546}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}, {1866, 3558}},
//                            {{3284, 1033}, {3284, 1053}, {3283, 1118}, {3282, 1189}, {3282, 1256}, {3281, 1321}, {3281, 1386}, {3280, 1457}, {3279, 1527}, {3279, 1600}, {3279, 1677}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3279, 1685}, {3241, 1716}, {3191, 1758}, {3140, 1800}, {3088, 1842}, {3074, 1855}, {3067, 1860}, {3059, 1867}, {3051, 1873}, {3017, 1901}, {2963, 1947}, {2912, 1988}, {2859, 2032}, {2805, 2077}, {2751, 2121}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2728, 2140}, {2727, 2188}, {2727, 2255}, {2727, 2325}, {2726, 2395}, {2726, 2466}, {2727, 2538}, {2726, 2557}, {2726, 2586}, {2726, 2651}, {2726, 2719}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}, {2726, 2727}},
//                            {{3072, 2245}, {3069, 2236}, {3073, 2225}, {3087, 2157}, {3104, 2092}, {3118, 2030}, {3131, 1967}, {3147, 1898}, {3161, 1830}, {3177, 1760}, {3193, 1686}, {3206, 1620}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3211, 1601}, {3210, 1551}, {3210, 1485}, {3209, 1420}, {3209, 1353}, {3208, 1292}, {3207, 1232}, {3207, 1160}, {3207, 1092}, {3206, 1019}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3206, 1001}, {3194, 1004}, {3128, 1017}, {3058, 1031}, {2989, 1044}, {2920, 1058}, {2850, 1072}, {2782, 1085}, {2718, 1098}, {2652, 1110}, {2586, 1124}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}, {2581, 1125}},
//                            {{3454, 3810}, {3455, 3803}, {3457, 3789}, {3441, 3720}, {3428, 3654}, {3417, 3590}, {3405, 3526}, {3393, 3456}, {3381, 3386}, {3369, 3315}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3365, 3295}, {3353, 3299}, {3289, 3316}, {3225, 3333}, {3160, 3351}, {3101, 3367}, {3038, 3384}, {2969, 3403}, {2902, 3421}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2859, 3432}, {2880, 3427}, {2892, 3422}, {2906, 3410}, {2976, 3397}, {3046, 3379}, {3113, 3361}, {3177, 3343}, {3241, 3325}, {3308, 3307}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}, {3362, 3291}},
//                            {{2372, 3026}, {2371, 3018}, {2366, 3007}, {2375, 2939}, {2382, 2872}, {2387, 2807}, {2392, 2745}, {2393, 2718}, {2399, 2647}, {2404, 2575}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2408, 2509}, {2389, 2468}, {2360, 2409}, {2334, 2351}, {2305, 2289}, {2279, 2233}, {2251, 2174}, {2221, 2108}, {2192, 2047}, {2161, 1982}, {2131, 1917}, {2102, 1856}, {2073, 1794}, {2044, 1731}, {2014, 1667}, {1985, 1605}, {1955, 1543}, {1926, 1479}, {1898, 1419}, {1869, 1358}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1857, 1333}, {1871, 1289}, {1890, 1224}, {1910, 1156}, {1930, 1089}, {1950, 1025}, {1970, 956}, {1986, 904}, {1992, 885}, {1994, 883}, {1994, 883}, {1993, 882}, {1995, 882}, {1994, 882}, {1994, 883}, {1994, 883}, {1993, 882}, {1995, 882}, {1993, 883}, {1994, 883}, {1993, 882}, {1993, 882}, {1994, 882}, {1993, 883}, {1994, 882}, {1994, 883}, {1994, 882}, {1993, 882}, {1995, 883}, {1994, 882}, {1994, 882}, {1993, 883}, {1995, 882}, {1993, 882}, {1994, 883}},
//                            {{544, 4039}, {542, 4033}, {547, 4020}, {581, 3957}, {613, 3898}, {642, 3841}, {672, 3782}, {703, 3720}, {734, 3657}, {767, 3592}, {801, 3524}, {831, 3465}, {860, 3405}, {892, 3342}, {905, 3316}, {905, 3316}, {905, 3316}, {905, 3316}, {905, 3316}, {905, 3316}, {905, 3316}, {905, 3316}, {905, 3316}, {905, 3316}, {905, 3316}, {905, 3316}, {905, 3316}, {905, 3316}, {905, 3316}, {905, 3316}, {905, 3316}, {941, 3291}, {995, 3254}, {1050, 3216}, {1106, 3178}, {1157, 3143}, {1211, 3107}, {1271, 3066}, {1327, 3027}, {1389, 2986}, {1449, 2945}, {1504, 2907}, {1561, 2869}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1600, 2842}, {1602, 2829}, {1573, 2770}, {1549, 2704}, {1526, 2639}, {1501, 2573}, {1476, 2504}, {1452, 2438}, {1430, 2377}, {1408, 2317}, {1384, 2251}, {1357, 2177}, {1334, 2112}, {1309, 2045}, {1283, 1974}, {1257, 1904}, {1234, 1839}, {1210, 1775}, {1185, 1707}, {1177, 1682}, {1177, 1682}, {1177, 1682}, {1177, 1682}, {1177, 1682}, {1177, 1682}, {1177, 1682}, {1177, 1682}, {1177, 1682}, {1177, 1682}, {1177, 1682}, {1177, 1682}, {1177, 1682}, {1177, 1682}, {1177, 1682}, {1177, 1682}}};
//
//                        for (int idx = 0; idx < waypoints1.length; idx++) {
//                            for (int id = 0; id < waypoints1[idx].length; id++) {
//                                float xp = (float) (waypoints1[idx][id][0] - 2500);
//                                float zp = (float) (2500 - waypoints1[idx][id][1]);
//                                State currentMarkerP = new State("Path_" + waypoints1[idx][id][0] + "_" + waypoints1[idx][id][1] + "_" + System.currentTimeMillis(), StateEnums.StateType.TARGET_MARKER);
//                                currentMarkerP.setPos(xp, (float) 50.0, zp);
//                                currentMarkerP.setSelected(false);
//                                stateDB.put(currentMarkerP);
//                            }
//                        }
//

//                        Debug.debug(4, "------------------------------------------------------------------------------- -------------------------------------------------------------------------------");
                    }
                });

//        //Show billboard option
//        menuItemBillboard = new JMenuItem("Show Billboard");
//        jpm.add(menuItemBillboard);
//        //adding action listener to menu items
//        menuItemBillboard.addActionListener(
//                new ActionListener() {
//
//                    public void actionPerformed(ActionEvent e) {
//                        BillboardViewPanel bvp = new BillboardViewPanel();
//                        bvp.createAndShowBillboard(location);
//                    }
//                });

        //Close popup option
        menuItemClose = new JMenuItem("Close");
        jpm.add(menuItemClose);
        //adding action listener to menu items
        menuItemClose.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        jpm.removeAll();
                        currentMarker.setSelected(false);
                    }
                });

        FloatingBillboardViewPanel bvp;
        try {
            bvp = new FloatingBillboardViewPanel(jpm, oglPosition);
            jpm.add(bvp);
        } catch (IOException ex) {
            Logger.getLogger(GeneralMouseController.class.getName()).log(Level.SEVERE, null, ex);
        }

        jpm.show(s.e.getComponent(), s.e.getX(), s.e.getY());
    }
}
