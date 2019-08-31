/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JFrame;
import suave.StateEnums.ForceID;
import suave.StateEnums.KillStatus;
import suave.StateEnums.StateType;
import vbs2gui.video.ImageAndTelemetry;

/**
 *
 * @author owens
 */
public class VBS2Sim implements Runnable, GeoTransformsConstants {

    private final DecimalFormat fmt = new DecimalFormat("0.00");
    private final DecimalFormat fmt2 = new DecimalFormat("0.00000");
    private final static int VBS2GUI_POS_X = 0;
    private final static int VBS2GUI_POS_Y = 1;
    private final static int VBS2GUI_POS_Z = 2;
    private final static int VBS2GUI_LON = 0;
    private final static int VBS2GUI_LAT = 1;
    private final static int VBS2GUI_ALT = 2;
    private final static int VBS2GUI_DIR_X = 3;
    private final static int VBS2GUI_DIR_Y = 4;
    private final static int VBS2GUI_DIR_Z = 5;
    private final static int VBS2GUI_UP_X = 6;
    private final static int VBS2GUI_UP_Y = 7;
    private final static int VBS2GUI_UP_Z = 8;
    private String replayLogFile;
    private Baker baker;
    private Model model;
    private StateDB stateDB;
    private GLCamera userViewCamera = null;
    private Origin origin;
    private String vbs2host;
    private int vbs2port;
    private DEM dem;
    private vbs2gui.SimpleUAVSim.Main vbs2link;
    private BlockingQueue<ImageAndTelemetry> inQueue;
    private Thread myThread;
    private Axis axis;
    private PosRot axisPosRot;
    private Renderable uavMarker;
    private Renderable uavDir;
    private Renderable uavUp;
    // NOTE: baking is slow - on slower machines it can really bog
    // things down and make it impossible to even kill the program, or
    // navigate around the terrain model.  Set SIMULATE_SLEEP_TIME
    // higher (200?  depends on how fast the computer is) when baking
    // on slow machines and lower when not baking, or when on a fast
    // machine.
    private final static int DEBUG_SLEEP_TIME_BETWEEN_FRAMES_TO_AVOID_SWAMPING = 0;
    private final static boolean BAKE_FRAMES = true;
    private final static boolean DEBUG_USER_VIEW_FOLLOWS_TELEMETRY = false;
    private final static boolean DEBUG_DRAW_AXIS_AT_UAV_POS = false;
    private final static boolean DEBUG_DRAW_UAV_POS_DIR_UP_MARKERS = false;
    private final static int EVERY_N_FRAMES = 0;
    private HashMap<Integer, Integer> skipCount = new HashMap<Integer, Integer>();
    JFrame glDisplayFrame;
    vbs2gui.SimpleUAVSim.Main.ExperimentType experimentType;
    String vbs2guiFolder;

    public VBS2Sim(String replayLogFile, Baker baker, Model model, StateDB stateDB, GLCamera userViewCamera, TextureDB textureDB, Origin origin, String vbs2host, int vbs2port, DEM dem) {
        this.replayLogFile = replayLogFile;
        this.baker = baker;
        this.model = model;
        this.stateDB = stateDB;
        this.userViewCamera = userViewCamera;
        this.origin = origin;
        this.vbs2host = vbs2host;
        this.vbs2port = vbs2port;
        this.dem = dem;
        inQueue = new LinkedBlockingQueue<ImageAndTelemetry>();

        myThread = new Thread(this);
        axis = new Axis();
        uavMarker = new Marker(Color.white);
        uavDir = new Marker(Color.red);
        uavUp = new Marker(Color.blue);

        boolean addMarkersAndCorners = false;
        if (addMarkersAndCorners) {

//            // @TODO: HACK - use public static corners from Main so I can adjust them in one place visually until they match heightmap
//            for (int loopi = 0; loopi < Main.corners.length; loopi++) {
//                double[] mc = Main.corners[loopi];
//                double[] xyz = new double[3];
//                origin.gpsDegreesToLvcs(mc[LAT_INDEX], mc[LON_INDEX], mc[ALT_INDEX], xyz);
//                Origin.lvcsToOpenGL(xyz, xyz);
//                model.addRenderable("CORNER__" + loopi, uavUp, (float) xyz[OGL_X], (float) xyz[OGL_Y] + 200, (float) xyz[OGL_Z]);
//                model.addRenderable("CORNER_AXIS_" + loopi, axis, (float) xyz[OGL_X], (float) xyz[OGL_Y] + 200, (float) xyz[OGL_Z]);
//            }

            double[][] markerCoords = {
                {51.2128791809, -2.1839690208, 158.642},
                {51.2372741699, -2.1765704155, 161.618},
                {51.2143402100, -2.1651692390, 154.609},
                {51.2285003662, -2.1496288776, 200.848},
                {51.2086067200, -2.1452369690, 135.671},
                {51.2244567871, -2.1400635242, 197.516},
                {51.2448577881, -2.1387512684, 181.027}
            };

            for (int loopi = 0; loopi < markerCoords.length; loopi++) {
                double[] mc = markerCoords[loopi];

                double[] xyz = new double[3];
                origin.gpsDegreesToLvcs(mc[0], mc[1], mc[2], xyz);
                Origin.lvcsToOpenGL(xyz, xyz);
                model.addRenderable("NATHAN_" + loopi, axis, (float) xyz[OGL_X], (float) xyz[OGL_Y], (float) xyz[OGL_Z]);
                model.addRenderable("NATHAN_red_" + loopi, uavDir, (float) xyz[OGL_X], (float) xyz[OGL_Y] + 200, (float) xyz[OGL_Z]);
            }
        }
    }

    public VBS2Sim(String replayLogFile, Baker baker, Model model, StateDB stateDB, GLCamera userViewCamera, TextureDB textureDB, Origin origin, String vbs2host, int vbs2port, DEM dem, JFrame glDisplayFrame) {
        this(replayLogFile, baker, model, stateDB, userViewCamera, textureDB, origin, vbs2host, vbs2port, dem);
        this.glDisplayFrame = glDisplayFrame;
        Debug.debug(1, "VBS2Sim: glDisplayFrame is " + glDisplayFrame);
    }

    public VBS2Sim(String replayLogFile, Baker baker, Model model, StateDB stateDB, GLCamera userViewCamera, TextureDB textureDB, Origin origin, String vbs2host, int vbs2port, DEM dem, JFrame glDisplayFrame, vbs2gui.SimpleUAVSim.Main.ExperimentType experimentType, String vbs2guiFolder) {
        this(replayLogFile, baker, model, stateDB, userViewCamera, textureDB, origin, vbs2host, vbs2port, dem);
        this.glDisplayFrame = glDisplayFrame;
        this.experimentType = experimentType;
        this.vbs2guiFolder = vbs2guiFolder;
    }

    public void start() {
        myThread.start();
    }

    // @override
    public void run() {
        if (null != experimentType) {
            Debug.debug(1, "Experiment type is " + experimentType);
            Debug.debug(1, "GUI type is " + vbs2gui.SimpleUAVSim.Main.GUIType.SUAVE);
            Debug.debug(1, "Replay log file is " + replayLogFile);
            vbs2link = new vbs2gui.SimpleUAVSim.Main(inQueue, glDisplayFrame, experimentType, vbs2guiFolder);
        } else if (null != replayLogFile) {
            int waypointSet = 1;
            Debug.debug(1, "Experiment type is " + experimentType);
            Debug.debug(1, "GUI type is " + vbs2gui.SimpleUAVSim.Main.GUIType.SUAVE);
            Debug.debug(1, "Replay log file is " + replayLogFile);
            vbs2link = new vbs2gui.SimpleUAVSim.Main(waypointSet, inQueue, glDisplayFrame, vbs2gui.SimpleUAVSim.Main.GUIType.SUAVE, null, null, replayLogFile, null);
        } else {
            new Thread() {

                public void run() {
                    vbs2link = new vbs2gui.SimpleUAVSim.Main(1, inQueue, glDisplayFrame, vbs2gui.SimpleUAVSim.Main.GUIType.SUAVE, new String[] {vbs2host}, new int[] {vbs2port}, null, null);
                }
            }.start();
        }
        while (true) {
            try {
                Thread.sleep(DEBUG_SLEEP_TIME_BETWEEN_FRAMES_TO_AVOID_SWAMPING);
            } catch (InterruptedException e) {
            }
            ImageAndTelemetry imgAndTelem = null;
            try {
//                Debug.debug(1, "VBS2Sim.run: read thread calling inQueue.take()");
                imgAndTelem = inQueue.take();
//                Debug.debug(1, "VBS2Sim.run: read thread returned from calling inQueue.take()");
            } catch (InterruptedException e) {
            }
            if (null == imgAndTelem) {
                Debug.debug(1, "VBS2Sim.run: read thread msg is null, skipping");
                continue;
            }
//            Debug.debug(1, "VBS2Sim.run: inQueue size= " + inQueue.size() + ", image is " + imgAndTelem.img.getWidth() + " by " + imgAndTelem.img.getHeight());

            Integer skippedCount = skipCount.get(imgAndTelem.uavid);
            if (null != skippedCount) {
                if (skippedCount < EVERY_N_FRAMES) {
                    skipCount.put(imgAndTelem.uavid, new Integer(skippedCount + 1));
                    continue;
                }
            }
            skipCount.put(imgAndTelem.uavid, new Integer(0));

            // We have 9 floats;
            //
            // X position
            // Y position
            // ASL position (above sea level)
            // x direction
            // y direction
            // z direction
            // x up
            // y up
            // z up
            //
            // and they are in USARSim coordinate system, i.e.;
            //
            // +x is north
            // +y is east
            // +z is down
            //
            // And I need to swap things around to match the opengl/my local
            // coordinate system, which is
            //
            // -z is north
            // +x is east
            // -y is down
            //
            // so;
            //
            // newx = oldy
            // newy = -oldz
            // newz = -oldx

            // @TODO: HACK HACK HACK
            // I 'shifted' the local coord version of the warminster heightmap file so
            // that 0,0,0 was in the middel... since it's 5km, that means I have to likewise shift the
            // incoming coords.

//            float xPos = (float) imgAndTelem.telem[VBS2GUI_POS_Y] - 2500;
//            float yPos = (float) (-1 * imgAndTelem.telem[VBS2GUI_POS_Z]);
//            float zPos = (float) (-1 * imgAndTelem.telem[VBS2GUI_POS_X] + 2500);

//            Debug.debug(1, "Received uav telem for uav " + imgAndTelem.uavid + ", lon/lat/alt = " + imgAndTelem.telem[VBS2GUI_LON] + " " + imgAndTelem.telem[VBS2GUI_LAT] + " " + imgAndTelem.telem[VBS2GUI_ALT]);

            Color uavColor = new Color(imgAndTelem.uavid);
            Color roundedColor = new Color((uavColor.getRed() / 64) * 64, (uavColor.getGreen() / 64) * 64, (uavColor.getBlue() / 64) * 64);
            Debug.debug(1, vbs2gui.SimpleUAVSim.Main.roundedColorToUavName.get(roundedColor)
                    + " POS " + fmt.format(imgAndTelem.telem[0]) + " " + fmt.format(imgAndTelem.telem[1]) + " " + fmt.format(imgAndTelem.telem[2])
                    + " DIR " + fmt.format(imgAndTelem.telem[3]) + " " + fmt.format(imgAndTelem.telem[4]) + " " + fmt.format(imgAndTelem.telem[5])
                    + " UP " + fmt.format(imgAndTelem.telem[6]) + " " + fmt.format(imgAndTelem.telem[7]) + " " + fmt.format(imgAndTelem.telem[8]));
            double[] lla = new double[3];
            lla[0] = imgAndTelem.telem[0];
            lla[1] = imgAndTelem.telem[1];
            lla[2] = imgAndTelem.telem[2];
            double[] xyz = new double[3];
            double[] ogl = new double[3];
            origin.gpsDegreesToLvcs(lla[LAT_INDEX], lla[LON_INDEX], lla[ALT_INDEX], xyz);
            origin.lvcsToOpenGL(xyz, ogl);
            Debug.debug(1, "converted telem lon lat alt (NOTE ORDER) " + Arrays.toString(lla) + " to opengl " + Arrays.toString(ogl));

            float xPos = (float) ogl[OGL_X];
            float yPos = (float) ogl[OGL_Y];
            float zPos = (float) ogl[OGL_Z];

            double localAlt = dem.getAltitude(xPos, yPos);
            Debug.debug(1, "TELEM original ASL=" + imgAndTelem.telem[2] + " opengl=" + yPos + " according to DEM ground level=" + localAlt + " diff=" + (yPos - localAlt));

            float xDir = (float) imgAndTelem.telem[VBS2GUI_DIR_Y];
            float yDir = (float) (-1 * imgAndTelem.telem[VBS2GUI_DIR_Z]);
            float zDir = (float) (-1 * imgAndTelem.telem[VBS2GUI_DIR_X]);

            float xUp = (float) imgAndTelem.telem[VBS2GUI_UP_Y];
            float yUp = (float) (-1 * imgAndTelem.telem[VBS2GUI_UP_Z]);
            float zUp = (float) (-1 * imgAndTelem.telem[VBS2GUI_UP_X]);

            double dot = xDir * xUp + yDir * yUp + zDir * zUp;
            double dirUnit = xDir * xDir + yDir * yDir + zDir * zDir;
            double upUnit = xUp * xUp + yUp * yUp + zUp * zUp;
//            Debug.debug(1, "VBS2Sim.run: dot=" + dot + ", dirUnit=" + dirUnit + ", upUnit = " + upUnit);

            String uavID = vbs2gui.SimpleUAVSim.Main.roundedColorToUavName.get(new Color(imgAndTelem.uavid));
            ForceID uavForceID = null;
            if (uavID.equals("PLANE0")) {
                uavForceID = ForceID.COLOR0;
            } else if (uavID.equals("PLANE1")) {
                uavForceID = ForceID.COLOR1;
            } else if (uavID.equals("PLANE2")) {
                uavForceID = ForceID.COLOR2;
            } else if (uavID.equals("PLANE3")) {
                uavForceID = ForceID.COLOR3;
            } else if (uavID.equals("PLANE4")) {
                uavForceID = ForceID.COLOR4;
            } else if (uavID.equals("PLANE5")) {
                uavForceID = ForceID.COLOR5;
            } else if (uavID.equals("PLANE6")) {
                uavForceID = ForceID.COLOR6;
            } else if (uavID.equals("PLANE7")) {
                uavForceID = ForceID.COLOR7;
            } else if (uavID.equals("PLANE8")) {
                uavForceID = ForceID.COLOR8;
            } else if (uavID.equals("PLANE9")) {
                uavForceID = ForceID.COLOR9;
            } else if (uavID.equals("PLANE10")) {
                uavForceID = ForceID.COLOR10;
            } else if (uavID.equals("PLANE11")) {
                uavForceID = ForceID.COLOR11;
            } else if (uavID.equals("PLANE12")) {
                uavForceID = ForceID.COLOR12;
            } else if (uavID.equals("PLANE13")) {
                uavForceID = ForceID.COLOR13;
            } else if (uavID.equals("PLANE14")) {
                uavForceID = ForceID.COLOR14;
            } else if (uavID.equals("PLANE15")) {
                uavForceID = ForceID.COLOR15;
            } else if (uavID.equals("PLANE16")) {
                uavForceID = ForceID.COLOR16;
            } else if (uavID.equals("PLANE17")) {
                uavForceID = ForceID.COLOR17;
            } else if (uavID.equals("PLANE18")) {
                uavForceID = ForceID.COLOR18;
            } else if (uavID.equals("PLANE19")) {
                uavForceID = ForceID.COLOR19;
            } else if (uavID.equals("PLANE20")) {
                uavForceID = ForceID.COLOR20;
            } else if (uavID.equals("PLANE21")) {
                uavForceID = ForceID.COLOR21;
            } else if (uavID.equals("PLANE22")) {
                uavForceID = ForceID.COLOR22;
            } else if (uavID.equals("PLANE23")) {
                uavForceID = ForceID.COLOR23;
            }

//            System.out.println("\n\nForce id is " + uavForceID + "\n\n");
            if (uavForceID != null) {
                updateUavTrack("UAV" + imgAndTelem.uavid,
                        xPos,
                        yPos,
                        zPos,
                        //                    true, ForceID.BLUEFOR);
                        true, uavForceID);
                if (DEBUG_DRAW_UAV_POS_DIR_UP_MARKERS) {
                    float dist = 50;
                    updateUavTrack("UAVDIR" + imgAndTelem.uavid,
                            xPos + dist * xDir,
                            yPos + dist * yDir,
                            zPos + dist * zDir,
                            //                        false, ForceID.OPFOR);
                            false, ForceID.RED);
                    updateUavTrack("UAVUP" + imgAndTelem.uavid,
                            xPos + dist * xUp,
                            yPos + dist * yUp,
                            zPos + dist * zUp,
                            //                        false, ForceID.NEUTRAL);
                            false, ForceID.WHITE);
                }
            }

            if (DEBUG_USER_VIEW_FOLLOWS_TELEMETRY) {
                userViewCamera.xPos = xPos;
                userViewCamera.yPos = yPos;
                userViewCamera.zPos = zPos;
                userViewCamera.setDirectionAndUp(xDir, yDir, zDir, xUp, yUp, zUp);

                Debug.debug(1, "VBS2Sim.run: Set userViewCamera location (VBS) "
                        + fmt.format(imgAndTelem.telem[VBS2GUI_POS_X]) + ", " + fmt.format(imgAndTelem.telem[VBS2GUI_POS_Y]) + ", " + fmt.format(imgAndTelem.telem[VBS2GUI_POS_Z])
                        + " (local) "
                        + fmt.format(userViewCamera.xPos) + ", " + fmt.format(userViewCamera.yPos) + ", " + fmt.format(userViewCamera.zPos));
            }

            VideoFrame videoFrame = new VideoFrame();
            videoFrame.timeMs = -1;
            videoFrame.frameIndex = -1;
            if (BAKE_FRAMES) {
//                Debug.debug(1, "VBS2Sim.run: Set videoframe location (VBS) "
//                        + fmt.format(imgAndTelem.telem[VBS2GUI_POS_X]) + ", " + fmt.format(imgAndTelem.telem[VBS2GUI_POS_Y]) + ", " + fmt.format(imgAndTelem.telem[VBS2GUI_POS_Z])
//                        + " local pos "
//                        + fmt.format(videoFrame.x) + ", " + fmt.format(videoFrame.y) + ", " + fmt.format(videoFrame.z)
//                        + " dir "
//                        + fmt2.format(xDir) + ", " + fmt2.format(yDir) + ", " + fmt2.format(zDir)
//                        + " up "
//                        + fmt2.format(xUp) + ", " + fmt2.format(yUp) + ", " + fmt2.format(zUp));

                // @TODO: originally we got the subimage, slicing off the top 21 pixels to ignore the telemetry.
                // Instead we now ignore those pixels in Baker, mainly because we're tryign to make sure we get
                // the camera calibration correct.
                //
                //                //                BufferedImage img = imgAndTelem.img.getSubimage(0, 21, 640, 459);
                //                // @TODO: Note that at this point we are flipping the image around the x axis (i.e. What all pixels with
                //                // Y = 0 become Y = 479.  I think I did  this here as a quick hack to make it match the projection going
                //                // on in Projection - but now we're using CameraCalibration projection so we may want to stop doing this...
                //                // jury is still out on this one.
                //                AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
                //                tx.translate(0, -img.getHeight(null));
                //                AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                //                imgAndTelem.img = op.filter(img, null);

//                // @TODO: Note that at this point we are flipping the image around the x axis (i.e. What all pixels with
//                // Y = 0 become Y = 479.  I think I did  this here as a quick hack to make it match the projection going
//                // on in Projection - but now we're using CameraCalibration projection so we may want to stop doing this...
//                // jury is still out on this one.
//                AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
//                tx.translate(0, -imgAndTelem.img.getHeight(null));
//                AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
//                imgAndTelem.img = op.filter(imgAndTelem.img, null);

                videoFrame.x = xPos;
                videoFrame.y = yPos;
                videoFrame.z = zPos;
                videoFrame.setDirectionAndUp(xDir, yDir, zDir, xUp, yUp, zUp);
                videoFrame.uavid = imgAndTelem.uavid;
                videoFrame.img = imgAndTelem.img;
                final VideoFrame fVideoFrame = videoFrame;
//                if (fVideoFrame.img.getType() == BufferedImage.TYPE_INT_RGB) {
//                    System.err.println("VBS2Sim: Image is type TYPE_INT_RGB");
//                }
//                if (fVideoFrame.img.getType() == BufferedImage.TYPE_INT_ARGB) {
//                    System.err.println("VBS2Sim: Image is type TYPE_INT_ARGB");
//                }
//                if (fVideoFrame.img.getType() == BufferedImage.TYPE_INT_ARGB_PRE) {
//                    System.err.println("VBS2Sim: Image is type TYPE_INT_ARGB_PRE");
//                }
//                if (fVideoFrame.img.getType() == BufferedImage.TYPE_INT_BGR) {
//                    System.err.println("VBS2Sim: Image is type TYPE_INT_BGR");
//                }
//                if (fVideoFrame.img.getType() == BufferedImage.TYPE_3BYTE_BGR) {
//                    System.err.println("VBS2Sim: Image is type TYPE_3BYTE_BGR");
//                }
//                if (fVideoFrame.img.getType() == BufferedImage.TYPE_4BYTE_ABGR) {
//                    System.err.println("VBS2Sim: Image is type TYPE_4BYTE_ABGR");
//                }
//                if (fVideoFrame.img.getType() == BufferedImage.TYPE_4BYTE_ABGR_PRE) {
//                    System.err.println("VBS2Sim: Image is type TYPE_4BYTE_ABGR_PRE");
//                }
//                if (fVideoFrame.img.getType() == BufferedImage.TYPE_BYTE_GRAY) {
//                    System.err.println("VBS2Sim: Image is type TYPE_BYTE_GRAY");
//                }
//                if (fVideoFrame.img.getType() == BufferedImage.TYPE_BYTE_BINARY) {
//                    System.err.println("VBS2Sim: Image is type TYPE_BYTE_BINARY");
//                }
//                if (fVideoFrame.img.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
//                    System.err.println("VBS2Sim: Image is type TYPE_BYTE_INDEXED");
//                }
//                if (fVideoFrame.img.getType() == BufferedImage.TYPE_USHORT_GRAY) {
//                    System.err.println("VBS2Sim: Image is type TYPE_USHORT_GRAY");
//                }
//                if (fVideoFrame.img.getType() == BufferedImage.TYPE_USHORT_565_RGB) {
//                    System.err.println("VBS2Sim: Image is type TYPE_USHORT_565_RGB");
//                }
//                if (fVideoFrame.img.getType() == BufferedImage.TYPE_USHORT_555_RGB) {
//                    System.err.println("VBS2Sim: Image is type TYPE_USHORT_555_RGB");
//                }
//                if (fVideoFrame.img.getType() == BufferedImage.TYPE_CUSTOM) {
//                    System.err.println("VBS2Sim: Image is type TYPE_CUSTOM");
//                }

                new Thread(new Runnable() {

                    public void run() {
                        int paintWidth = fVideoFrame.img.getWidth();
                        int paintHeight = fVideoFrame.img.getHeight();
                        if (fVideoFrame.img.getType() == BufferedImage.TYPE_3BYTE_BGR) {
                            DataBufferByte buf = (DataBufferByte) fVideoFrame.img.getRaster().getDataBuffer();
                            fVideoFrame.paintBytes = buf.getData();
                        } else {
                            fVideoFrame.paintPixels = fVideoFrame.img.getRGB(0, 0, paintWidth, paintHeight, null, 0, paintWidth);
                        }
                        baker.queueCommand(new BakerCommand(fVideoFrame));
                    }
                }).start();
            }

            if (DEBUG_DRAW_AXIS_AT_UAV_POS) {
                model.addRenderable("AXIS_UAV" + imgAndTelem.uavid, axis,
                        (float) videoFrame.x,
                        (float) videoFrame.z,
                        (float) videoFrame.y,
                        (float) Math.toDegrees(videoFrame.xRot),
                        (float) Math.toDegrees(videoFrame.zRot),
                        (float) Math.toDegrees(videoFrame.yRot));
            }
        }
    }

    public void updateUavTrack(String key, float xPos, float yPos, float zPos, boolean hasLine, ForceID forceId) {
        State state = stateDB.get(key);
        if (null == state) {
            StateType type = StateType.SPHERE;
            state = new State(key, type);
            stateDB.put(state);
            Debug.debug(1, "VBS2Sim.updateUavTrack: created new state=" + state);
        }

        synchronized (state) {
            state.setPos(xPos, yPos, zPos);
//            state.setRot(xRot, yRot, zRot);
//            state.setHeadingDegrees((float) msg.headingDegrees);
            state.setForceID(forceId);
            state.setKillStatus(KillStatus.LIVE);
            state.setHasLine(hasLine);
            state.setDirty(true);
            Debug.debug(1, "VBS2Sim.updateUavTrack: updated state=" + state);
        }
//
//        ArrayList<Point2D.Float> track = uavTracks.get(key);
//        if (null == track) {
//            track = new ArrayList<Point2D.Float>();
//            uavTracks.put(key, track);
//        }
//        if (track.size() < 2) {
//            return;
//        }
//        float[][] linePoints = new float[track.size()][];
//        // sadly, OpenGl doesn't really "do" transparent colors... i.e.  you have to get into writing shaders to do that
//        Color lineColor = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 128);
//        TextureInfo ti = textureDB.getColorTexture(gl, 32, lineColor);
//
//        RenderableInterleavedVBO rivbo = RenderableVBOFactory.buildInterleavedVBOLine(gl, ti.textID, linePoints, 10.0f);
//        addRenderable(name, rivbo);
    }
}
