/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vbs2gui.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;
import vbs2gui.video.VideoBank;

/**
 * A display widget that can display a map background and icon overlays.  It
 * support mouse-based panning and zooming.
 * @author pkv
 */
public class MapPanel extends JPanel {

    private static final Logger logger = Logger.getLogger(MapPanel.class.getName());
    private static final int TARGET_RADIUS = 250;
    private static final double TARGET_SELECT_RANGE_SQD = TARGET_RADIUS * TARGET_RADIUS;
    private static final Color TARGET_COLOR = Color.RED;
    private static final Color TARGET_SELECTED_COLOR = Color.WHITE;
    private static final Color TARGET_HIGHLIGHTED_COLOR = Color.YELLOW;
    public static final double SCALE_FACTOR = 1.2;
    private final LinkedHashMap<String, MapIcon> icons = new LinkedHashMap<String, MapIcon>();
    private Image background;
    private final Point2D mapOffset = new Point2D.Double(0.0, 0.0);
    private final Point2D mapScale = new Point2D.Double(1.0, 1.0);
    private final AffineTransform mapImageTransform = new AffineTransform();
    private Point currPoint;
    private Point downPoint;
    private final Point2D offset = new Point2D.Double(0.0, 0.0);
    private final Point2D scale = new Point2D.Double(1.0, 1.0);
    private final MapMouseListener mt = new MapMouseListener();
    private final MapComponentListener cl = new MapComponentListener();
    public static final double SET_ICON_SCALE = 2.0;

    {
        this.addMouseListener(mt);
        this.addMouseMotionListener(mt);
        this.addMouseWheelListener(mt);
        this.addComponentListener(cl);
    }

    private class MapIcon {

        Image icon;
        final AffineTransform xform = new AffineTransform();
    }

    public void setMapRect(double left, double right, double top, double bottom) {
        mapOffset.setLocation(-(left + right) / 2, -(top + bottom) / 2);
        mapScale.setLocation(1 / (right - left), 1 / (bottom - top));
    }

    public void setMapImage(Image img) {
        background = img;

        int width = background.getWidth(this);
        int height = background.getHeight(this);
        if (width > 0 && height > 0) {
            setMapTransform(width, height);
        } else {
            mapImageTransform.setToIdentity();
            System.err.println("Need to use ImageObserver to set transform.");
        }
    }

    private void setMapTransform(int width, int height) {
        mapImageTransform.setToIdentity();
        mapImageTransform.scale(1 / (double) width, 1 / (double) height);
        mapImageTransform.translate(-width / 2, -height / 2);
    }

    public void setIcon(String name, Image img, double s, double x, double y, double th) {
        MapIcon mi = getIcon(name);
        mi.icon = img;
        setIconTransform(mi, x, y, th, s);
        this.repaint();
    }

    public void setIcon(String name, Image img, double x, double y) {
        setIcon(name, img, 1.0, x, y, 0.0);
    }

    public void setIcon(String name, double x, double y, double t) {
        MapIcon mi = getIcon(name);
        setIconTransform(mi, x, y, t, 1.0);
        this.repaint();
    }

    private MapIcon getIcon(String name) {
        synchronized (icons) {
            if (icons.containsKey(name)) {
                return icons.get(name);
            } else {
                MapIcon mi = new MapIcon();
                icons.put(name, mi);
                return mi;
            }
        }
    }

    private void setIconTransform(MapIcon mi, double x, double y, double theta, double scale) {
        int width = mi.icon.getWidth(null);
        int height = mi.icon.getHeight(null);
        
        mi.xform.setToIdentity();
        mi.xform.translate(x, y);
        mi.xform.rotate(theta);
        mi.xform.translate(-scale*width/2, -scale*height/2);
        mi.xform.scale(scale, scale);
    }

    public void removeIcon(String name) {
        synchronized (icons) {
            icons.remove(name);
            this.repaint();
        }
    }

    private void buildTransform1(Graphics2D g2) {
        // Apply transform from frame coords to image coords
        g2.translate(this.getWidth() / 2, this.getHeight() / 2);
        if (downPoint != null && currPoint != null) {
            g2.translate(currPoint.getX() - downPoint.getX(),
                    currPoint.getY() - downPoint.getY());
        }
        g2.scale(scale.getX(), scale.getY());
        g2.translate(offset.getX(), offset.getY());
    }

    private void buildTransform2(Graphics2D g2) {
        // Apply transform from image coords to map coords
        g2.scale(mapScale.getX(), mapScale.getY());
        g2.translate(mapOffset.getX(), mapOffset.getY());
    }

    private AffineTransform mapToScreen() {
        AffineTransform at = new AffineTransform();
        // Apply transform from frame coords to image coords
        at.translate(this.getWidth() / 2, this.getHeight() / 2);
        if (downPoint != null && currPoint != null) {
            at.translate(currPoint.getX() - downPoint.getX(),
                    currPoint.getY() - downPoint.getY());
        }
        at.scale(scale.getX(), scale.getY());
        at.translate(offset.getX(), offset.getY());

        // Apply transform from image coords to map coords
        at.scale(mapScale.getX(), mapScale.getY());
        at.translate(mapOffset.getX(), mapOffset.getY());
        return at;
    }

    private AffineTransform screenToMap() {
        AffineTransform at = mapToScreen();
        try {
            at = at.createInverse();
        } catch (NoninvertibleTransformException e) {
            System.err.println("MapPanel.screenToMap:  NON INVERTIBLE mapToScreen transform! e=" + e);
            e.printStackTrace();
        }
        return at;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        buildTransform1(g2);

        // Draw the map in the window
        drawMap(g2);

        buildTransform2(g2);

        // Draw overlays in the window
        synchronized (icons) {
            for (MapIcon mi : icons.values()) {
                drawIcon(mi, g2);
            }
        }

        drawTargets(g2);

        g2.dispose();
    }

    private void drawMap(Graphics2D g) {
        if (background == null) {
            return;
        }
        if (!mapImageTransform.isIdentity()) {
            g.drawImage(background, mapImageTransform, this);
        }
    }

    private void drawIcon(MapIcon mi, Graphics2D g) {
        if (mi.icon == null) {
            return;
        }
        if (!mi.xform.isIdentity()) {
            g.drawImage(mi.icon, mi.xform, this);
        }
    }

    private void drawTarget(Graphics2D g, int x, int y, int radius, Color color) {
        g.setColor(color);
        int radiusInc = radius / 5;
        for (int loopw = radiusInc; loopw <= radius; loopw += radiusInc) {
            g.drawOval(x - (loopw / 2), y - (loopw / 2), loopw, loopw);
        }
    }

    public void start() {
        new Thread(new Updater()).run();
    }

    private class Target {

        String key;
        boolean highlighted = false;
        boolean selected = false;
        double x;
        double y;
        double lat;
        double lon;

        public void setPos(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    int markerCounter = 0;
    String currentMarkerKey = null;
    Target currentMarker = null;
    private ArrayList<Target> targetList = new ArrayList<Target>();

    // @TODO: Make this actually work.
    private void toMap(double x, double y, double[] xyz) {
        // bleahhhhhh convert from screen/frame coordinates to VBS2 coordinates.  When the icons are drawn they
        // are drawn starting with VBS2 coordinates.   A lot of crap happens in setIconTransform() but that mostly
        // resolves down to nothing since scale is always passed as 1.0.  All it's really doing is
        // translating. (and adjusting for the icon width which we don't need to do.)
        //
        // ok, so drawIcon is basically just drawing using whatever transform is already put in place by paintComponent().
        AffineTransform at = screenToMap();
        double[] screenxyz = new double[]{x, y, 0};
        at.transform(screenxyz, 0, xyz, 0, 1);
    }

    // @TODO: Make this actually work.
    private void toFrame(double x, double y, double[] xy) {
        xy[0] = x;
        xy[1] = y;
    }

    private void drawTargets(Graphics2D g) {
        float strokeSize = 10.0f;
        float strokeScale = (float) (600 / scale.getY());
        if (strokeScale <0.0) {
            strokeScale = 1.0f;
        }
//        g.setStroke(new BasicStroke((float) (1.0 * -1*mapScale.getY()/this.getHeight())));
//        g.setStroke(new BasicStroke(10.0f));
        g.setStroke(new BasicStroke(strokeScale*strokeSize));

        for (Target t : targetList) {
            if (t.selected) {
                drawTarget(g, (int) t.x, (int) t.y, TARGET_RADIUS, TARGET_SELECTED_COLOR);
            } else if (t.highlighted) {
                drawTarget(g, (int) t.x, (int) t.y, TARGET_RADIUS, TARGET_HIGHLIGHTED_COLOR);
            } else {
                drawTarget(g, (int) t.x, (int) t.y, TARGET_RADIUS, TARGET_COLOR);
            }
        }
    }

    private Target findTarget(double x, double y, double rangeSqd, boolean highlighted, boolean selected) {
        Target found = null;
        double bestDistSqd = Double.MAX_VALUE;
        Target[] targetAry = targetList.toArray(new Target[1]);
        if (null == targetAry) {
            return null;
        }
        if (targetAry.length <= 0) {
            return null;
        }
        for (int loopi = 0; loopi < targetAry.length; loopi++) {
            Target target = targetAry[loopi];
            if (null == target) {
                continue;
            }
            if (highlighted) {
                target.highlighted = false;
            }
            if (selected) {
                target.selected = false;
            }
            double xdist = target.x - x;
            double ydist = target.y - y;
            double curDistSqd = xdist * xdist + ydist * ydist;
            if (curDistSqd < rangeSqd) {
                if (curDistSqd < bestDistSqd) {
                    bestDistSqd = curDistSqd;
                    found = target;
                }
            }
        }
        return found;
    }

    public void updateTargetMarkers(MouseEvent e) {
        double xyz[] = new double[3];
        toMap(e.getX(), e.getY(), xyz);
        double[] gps = new double[3];
        VideoBank.vbsToGps2(xyz, gps);
        if ((e.getID() == e.MOUSE_MOVED) && (currentMarker == null)) {
            Target target = findTarget(xyz[0], xyz[1], TARGET_SELECT_RANGE_SQD, true, false);
            if (null != target) {
                target.highlighted = true;
            }
            return;
        }
//        double[] ogl = new double[]{se.hitPos[0], se.hitPos[1], se.hitPos[2]};
        double[] ogl = new double[3];
//        double[] xyz = new double[3];
//        origin.openGLToLvcs(ogl, lvcs);
//        origin.lvcsToGpsDegrees(lvcs, gps);

        if (e.getButton() == e.BUTTON3 && e.getID() == e.MOUSE_PRESSED) {
            currentMarker = findTarget(xyz[0], xyz[1], TARGET_SELECT_RANGE_SQD, false, true);
            if (null != currentMarker) {
                targetList.remove(currentMarker);
                logger.info("\tTARGETMARKER"
                        + "\t" + System.currentTimeMillis()
                        + "\tPRESSEDDELETED"
                        + "\t" + currentMarker.key
                        + "\t" + xyz[0]
                        + "\t" + xyz[1]
                        + "\t" + xyz[2]
                        + "\t" + gps[0]
                        + "\t" + gps[1]
                        + "\t" + gps[2]);
                currentMarker = null;
            }
            return;
        }
        if (e.getButton() != e.BUTTON1 && (e.getID() != e.MOUSE_DRAGGED)) {
            return;
        }
        if ((e.getID() == e.MOUSE_PRESSED)) {
            if (currentMarker == null) {
                currentMarker = findTarget(xyz[0], xyz[1], TARGET_SELECT_RANGE_SQD, false, true);
                if (null == currentMarker) {

                    currentMarkerKey = "TARGET_MARKER" + "_" + markerCounter++;
                    currentMarker = new Target();
                    currentMarker.key = currentMarkerKey;
                    targetList.add(currentMarker);
                    logger.info("\tTARGETMARKER"
                            + "\t" + System.currentTimeMillis()
                            + "\tPRESSEDCREATED"
                            + "\t" + currentMarker.key
                            + "\t" + xyz[0]
                            + "\t" + xyz[1]
                            + "\t" + xyz[2]
                            + "\t" + gps[0]
                            + "\t" + gps[1]
                            + "\t" + gps[2]);
                } else {
                    logger.info("\tTARGETMARKER"
                            + "\t" + System.currentTimeMillis()
                            + "\tPRESSEDSELECTED"
                            + "\t" + currentMarker.key
                            + "\t" + xyz[0]
                            + "\t" + xyz[1]
                            + "\t" + xyz[2]
                            + "\t" + gps[0]
                            + "\t" + gps[1]
                            + "\t" + gps[2]);
                }
                currentMarker.selected = true;
            }
            currentMarker.setPos(xyz[0], xyz[1]);
        } else if (((e.getID() == e.MOUSE_RELEASED)) && (currentMarker != null)) {
            logger.info("\tTARGETMARKER"
                    + "\t" + System.currentTimeMillis()
                    + "\tRELEASED"
                    + "\t" + currentMarker.key
                    + "\t" + xyz[0]
                    + "\t" + xyz[1]
                    + "\t" + xyz[2]
                    + "\t" + gps[0]
                    + "\t" + gps[1]
                    + "\t" + gps[2]);
            currentMarker.setPos(xyz[0], xyz[1]);
            currentMarker.selected = false;
            currentMarker = null;
        } else if ((e.getID() == e.MOUSE_DRAGGED) && (null != currentMarker)) {
            logger.info("\tTARGETMARKER"
                    + "\t" + System.currentTimeMillis()
                    + "\tDRAGGED"
                    + "\t" + currentMarker.key
                    + "\t" + xyz[0]
                    + "\t" + xyz[1]
                    + "\t" + xyz[2]
                    + "\t" + gps[0]
                    + "\t" + gps[1]
                    + "\t" + gps[2]);

            currentMarker.setPos(xyz[0], xyz[1]);
        }

        // Added this in because target interaction was not triggering a Map
        //  panel repaint for somre reason
        MapPanel.this.repaint();
    }

    private class MapComponentListener implements ComponentListener {

        public void componentResized(ComponentEvent e) {
            scale.setLocation(MapPanel.this.getWidth(), MapPanel.this.getHeight());
        }

        public void componentMoved(ComponentEvent e) {
        }

        public void componentShown(ComponentEvent e) {
        }

        public void componentHidden(ComponentEvent e) {
        }
    }

    private class MapMouseListener implements MouseInputListener, MouseWheelListener, MouseMotionListener {

        public void mouseDragged(MouseEvent e) {
            if (!e.isShiftDown()) {
                updateTargetMarkers(e);
                return;
            }
            currPoint = e.getPoint();
            MapPanel.this.repaint();
        }

        public void mouseMoved(MouseEvent e) {
            if (!e.isShiftDown()) {
                updateTargetMarkers(e);
                return;
            }
            currPoint = e.getPoint();
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            if (!e.isShiftDown()) {
                updateTargetMarkers(e);
                return;
            }
            double exp = Math.pow(SCALE_FACTOR, e.getWheelRotation());
            scale.setLocation(scale.getX() * exp, scale.getY() * exp);
            MapPanel.this.repaint();
        }

        public void mouseClicked(MouseEvent e) {
            if (!e.isShiftDown()) {
                updateTargetMarkers(e);
                return;
            }
            Point2D mousePos = new Point2D.Double(e.getX(), e.getY());

            AffineTransform at = new AffineTransform();
            at.translate(MapPanel.this.getWidth() / 2, MapPanel.this.getHeight() / 2);
            if (downPoint != null && currPoint != null) {
                at.translate(currPoint.getX() - downPoint.getX(),
                        currPoint.getY() - downPoint.getY());
            }
            at.scale(scale.getX(), scale.getY());
            at.translate(offset.getX(), offset.getY());

            Point2D map = null;
            try {
                map = at.inverseTransform(mousePos, null);
                map = mapImageTransform.inverseTransform(map, null);
            } catch (NoninvertibleTransformException e2) {
            }

            at.scale(mapScale.getX(), mapScale.getY());
            at.translate(mapOffset.getX(), mapOffset.getY());

            Point2D world = null;
            try {
                world = at.inverseTransform(mousePos, null);
            } catch (NoninvertibleTransformException e2) {
            }

            System.out.println("Map: [" + map + "], "
                    + "World: [" + world + "]");
        }

        public void mousePressed(MouseEvent e) {
            if (!e.isShiftDown()) {
                updateTargetMarkers(e);
                return;
            }
            downPoint = e.getPoint();
        }

        public void mouseReleased(MouseEvent e) {
            if (!e.isShiftDown()) {
                updateTargetMarkers(e);
                return;
            }
            double dx = (e.getPoint().getX() - downPoint.getX()) / scale.getX();
            double dy = (e.getPoint().getY() - downPoint.getY()) / scale.getY();
            offset.setLocation(offset.getX() + dx, offset.getY() + dy);
            downPoint = null;
            MapPanel.this.repaint();
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }
    }

    private class Updater implements Runnable {

        public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
            }
        }
    }
}
