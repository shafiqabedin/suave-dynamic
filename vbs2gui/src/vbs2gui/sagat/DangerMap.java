/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vbs2gui.sagat;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.border.LineBorder;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

/**
 * A display widget that can display a map background and icon overlays.  It
 * support mouse-based panning and zooming.
 * @author pkv
 */
public class DangerMap extends JPanel {
    Logger logger = Logger.getLogger(DangerMap.class.getName());

    public static final double SCALE_FACTOR = 1.2;
    public static final float STROKE_WIDTH = 5.0f;
    private final LinkedHashMap<String, Path2D> paths = new LinkedHashMap<String, Path2D>();
    {
        double[][] parkingPoints = new double[][] {
            {137, 121, 98, 113}, {623, 648, 634, 608}};
        Path2D parkingPath = new Path2D.Double();
        parkingPath.moveTo(parkingPoints[0][0], parkingPoints[1][0]);
        for(int i = 1; i < parkingPoints[0].length; i++) {
            parkingPath.lineTo(parkingPoints[0][i], parkingPoints[1][i]);
        }
        parkingPath.closePath();
        paths.put("PARKING", parkingPath);

        double[][] housingPoints = new double[][] {
            {429, 430, 392, 338, 300, 287, 316, 327, 358},
            {774, 788, 861, 872, 859, 832, 812, 752, 750}};
        Path2D housingPath = new Path2D.Double();
        housingPath.moveTo(housingPoints[0][0], housingPoints[1][0]);
        for(int i = 1; i < housingPoints[0].length; i++) {
            housingPath.lineTo(housingPoints[0][i], housingPoints[1][i]);
        }
        housingPath.closePath();
        paths.put("HOUSING", housingPath);

        double[][] airportPoints = new double[][] {
            {520, 532, 524, 561, 515, 481, 489, 485, 492, 488, 502},
            {756, 772, 823, 850, 917, 888, 865, 854, 820, 819, 784}};
        Path2D airportPath = new Path2D.Double();
        airportPath.moveTo(airportPoints[0][0], airportPoints[1][0]);
        for(int i = 1; i < airportPoints[0].length; i++) {
            airportPath.lineTo(airportPoints[0][i], airportPoints[1][i]);
        }
        airportPath.closePath();
        paths.put("AIRPORT", airportPath);

        double[][] forest2Points = new double[][] {
            {576, 615, 604, 532, 521, 522, 540, 540},
            {277, 359, 383, 422, 410, 371, 355, 320}};
        Path2D forest2Path = new Path2D.Double();
        forest2Path.moveTo(forest2Points[0][0], forest2Points[1][0]);
        for(int i = 1; i < forest2Points[0].length; i++) {
            forest2Path.lineTo(forest2Points[0][i], forest2Points[1][i]);
        }
        forest2Path.closePath();
        paths.put("FOREST2", forest2Path);

        double[][] forest1Points = new double[][] {
            {317, 452, 466, 429, 368, 319, 302, 301},
            {298, 303, 335, 354, 359, 356, 338, 312}};
        Path2D forest1Path = new Path2D.Double();
        forest1Path.moveTo(forest1Points[0][0], forest1Points[1][0]);
        for(int i = 1; i < forest1Points[0].length; i++) {
            forest1Path.lineTo(forest1Points[0][i], forest1Points[1][i]);
        }
        forest1Path.closePath();
        paths.put("FOREST1", forest1Path);
    }
    private final LinkedHashMap<String, Boolean> clicked = new LinkedHashMap<String, Boolean>();
    {
        clicked.put("PARKING", false);
        clicked.put("HOUSING", false);
        clicked.put("AIRPORT", false);
        clicked.put("FOREST2", false);
        clicked.put("FOREST1", false);
    }
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
        mapOffset.setLocation(-(left + right)/2, -(top + bottom)/2);
        mapScale.setLocation(1/(right - left), 1/(bottom - top));
    }

    public void setMapImage(Image img) {
        background = img;

        int width = background.getWidth(this);
        int height = background.getHeight(this);
        if (width > 0 && height > 0) {
            setMapTransform(width, height);
            this.repaint();
        } else {
            mapImageTransform.setToIdentity();
            System.err.println("Need to use ImageObserver to set transform.");
        }
    }

    private void setMapTransform(int width, int height) {
        mapImageTransform.setToIdentity();
        mapImageTransform.scale(1/(double)width, 1/(double)height);
        mapImageTransform.translate(-width/2, -height/2);
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
        synchronized(icons) {
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
        int width = mi.icon.getWidth(this);
        int height = mi.icon.getHeight(this);

        mi.xform.setToIdentity();
        mi.xform.translate(x - scale*width/2, y - scale*height/2);
        mi.xform.rotate(theta);
        mi.xform.scale(scale, scale);
    }

    public void removeIcon(String name) {
        synchronized(icons) {
            icons.remove(name);
            this.repaint();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D)g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Apply transform from frame coords to image coords
        g2.translate(this.getWidth()/2, this.getHeight()/2);
        if (downPoint != null && currPoint != null) {
            g2.translate(currPoint.getX() - downPoint.getX(),
                    currPoint.getY() - downPoint.getY());
        }
        g2.scale(scale.getX(), scale.getY());
        g2.translate(offset.getX(), offset.getY());

        // Draw the map in the window
        drawMap(g2);

        // Draw the danger zone outlines/areas
        g2.transform(mapImageTransform);
        g2.setColor(Color.red);
        g2.setStroke(new BasicStroke(STROKE_WIDTH));
        for (Map.Entry<String, Path2D> entry : paths.entrySet()) {
            if (clicked.containsKey(entry.getKey()) && clicked.get(entry.getKey()) == true) {
                g2.fill(entry.getValue());
            } else if (clicked.containsKey(entry.getKey()) && clicked.get(entry.getKey()) == false) {
                g2.draw(entry.getValue());
            }
            if (!clicked.containsKey(entry.getKey())) {
                System.out.println(entry.getKey() + " not found.");
            }
        }

        // Apply transform from image coords to map coords
//        g2.scale(mapScale.getX(), mapScale.getY());
//        g2.translate(mapOffset.getX(), mapOffset.getY());

        g2.dispose();
    }

    private void drawMap(Graphics2D g2) {
        if (background == null) return;
        if (!mapImageTransform.isIdentity()) {
            g2.drawImage(background, mapImageTransform, this);
        }
    }

    private void drawIcon(MapIcon mi, Graphics2D g) {
        if (mi.icon == null) return;
        if (!mi.xform.isIdentity()) {
            g.drawImage(mi.icon, mi.xform, this);
        }
    }

    public void saveForm() {
        for (Map.Entry<String, Boolean> entry : clicked.entrySet()) {
            logger.info(entry.getKey() + "|" + entry.getValue());
        }
    }

    public void start() {
        new Thread(new Updater()).run();
    }

    private class MapComponentListener implements ComponentListener {
        public void componentResized(ComponentEvent e) {
            scale.setLocation(DangerMap.this.getWidth(), DangerMap.this.getHeight());
        }

        public void componentMoved(ComponentEvent e) {}
        public void componentShown(ComponentEvent e) {}
        public void componentHidden(ComponentEvent e) {}
    }

    private class MapMouseListener implements MouseInputListener, MouseWheelListener {

        public void mouseDragged(MouseEvent e) {
            currPoint = e.getPoint();
            DangerMap.this.repaint();
        }

        public void mouseMoved(MouseEvent e) {
            currPoint = e.getPoint();
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            double exp = Math.pow(SCALE_FACTOR, e.getWheelRotation());
            scale.setLocation(scale.getX()*exp, scale.getY()*exp);
            DangerMap.this.repaint();
        }

        public void mouseClicked(MouseEvent evt) {
            Point2D mousePos = new Point2D.Double(evt.getX(), evt.getY());

            AffineTransform at = new AffineTransform();
            at.translate(DangerMap.this.getWidth()/2, DangerMap.this.getHeight()/2);
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

                // Check to see if they clicked on a danger zone
                for (Map.Entry<String, Path2D> entry : paths.entrySet()) {
                    if (entry.getValue().contains(map) && clicked.containsKey(entry.getKey())) {
                        clicked.put(entry.getKey(), !clicked.get(entry.getKey()));
                    }
                }
            } catch (NoninvertibleTransformException e) {}

//            at.scale(mapScale.getX(), mapScale.getY());
//            at.translate(mapOffset.getX(), mapOffset.getY());

//            Point2D world = null;
//            try { world = at.inverseTransform(mousePos, null); }
//            catch (NoninvertibleTransformException e) {}

//            System.out.println("Map: [" + map + "], " +
//                    "World: [" + world + "]");
        }

        public void mousePressed(MouseEvent e) {
            downPoint = e.getPoint();
        }

        public void mouseReleased(MouseEvent e) {
            double dx = (e.getPoint().getX() - downPoint.getX())/scale.getX();
            double dy = (e.getPoint().getY() - downPoint.getY())/scale.getY();
            offset.setLocation(offset.getX() + dx, offset.getY() + dy);
            downPoint = null;
            DangerMap.this.repaint();
        }

        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
    }

    private class Updater implements Runnable {
        public void run() {
            try {Thread.sleep(500);}
            catch (InterruptedException e) {}
        }
    }

    public static void main(String[] args) {
        DangerMap dm = new DangerMap();
        dm.setPreferredSize(new Dimension(600, 600));
        dm.setBorder(new LineBorder(Color.BLACK));
        JFrame frame = new JFrame();
        frame.getContentPane().add(dm, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        try {
            File imgFile = new File((vbs2gui.SimpleUAVSim.Main.baseFolder + vbs2gui.SimpleUAVSim.Main.AERIAL_MAP));
            BufferedImage img = ImageIO.read(imgFile);
            dm.setMapImage(img);
            dm.setMapRect(0, 0, 5000, 5000); // Warminster bounds
            dm.repaint();
        } catch (IOException e) {
            System.err.println("Failed to load aerial map at: " + (vbs2gui.SimpleUAVSim.Main.baseFolder + vbs2gui.SimpleUAVSim.Main.AERIAL_MAP));
        }
        while (dm.isPaintingTile()) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
