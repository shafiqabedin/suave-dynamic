package suave;

import java.awt.image.BufferedImage;

import java.util.*;
import java.awt.*;
import javax.swing.*;

public class Painter extends JPanel {

    Triangle[] triangleAry = null;
    JFrame topFrame = null;
    BufferedImage img = null;
    double panelWidth = 0;
    double panelHeight = 0;

    public Painter() {
        createFrame();
    }

    public int screenx(double x, double xcenter) {
        return (int) (x);
    }

    public int screeny(double y, double ycenter) {
        return (int) (panelHeight - y);
    }

    private void findCenter() {
        double xmin = 100000;
        double xmax = -100000;
        double ymin = 100000;
        double ymax = -100000;
        for (int loopi = 0; loopi < triangleAry.length; loopi++) {
            Triangle t = triangleAry[loopi];
            if (t.visibleCount == 1) {
                if (t.v1.screenx > xmax) {
                    xmax = t.v1.screenx;
                }
                if (t.v1.screenx < xmin) {
                    xmin = t.v1.screenx;
                }
                if (t.v1.screeny > ymax) {
                    ymax = t.v1.screeny;
                }
                if (t.v1.screeny < ymin) {
                    ymin = t.v1.screeny;
                }

                if (t.v2.screenx > xmax) {
                    xmax = t.v2.screenx;
                }
                if (t.v2.screenx < xmin) {
                    xmin = t.v2.screenx;
                }
                if (t.v2.screeny > ymax) {
                    ymax = t.v2.screeny;
                }
                if (t.v2.screeny < ymin) {
                    ymin = t.v2.screeny;
                }

                if (t.v3.screenx > xmax) {
                    xmax = t.v3.screenx;
                }
                if (t.v3.screenx < xmin) {
                    xmin = t.v3.screenx;
                }
                if (t.v3.screeny > ymax) {
                    ymax = t.v3.screeny;
                }
                if (t.v3.screeny < ymin) {
                    ymin = t.v3.screeny;
                }

            }
        }

        double xavg = (xmax - xmin) / 2;
        double yavg = (ymax - ymin) / 2;
        System.out.println("xmax = " + xmax + " xmin = " + xmin + " ymax = " + ymax + " ymin = " + ymin + " xavg = " + xavg + " yavg = " + yavg);
    }

    private void calcDistSqd(Vertex v, GLCamera camera) {
        double xdiff = camera.xPos - v.x;
        double ydiff = camera.yPos - v.y;
        double zdiff = camera.zPos - v.z;
        v.distSqdFromViewpoint = (float) ((xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff));
    }

    private void calculateDistances(GLCamera camera) {
        for (int loopi = 0; loopi < triangleAry.length; loopi++) {
            Triangle t = triangleAry[loopi];
            if (null == t) {
                continue;
            }
            t.v1.distSqdFromViewpoint = Float.MAX_VALUE;
            t.v2.distSqdFromViewpoint = Float.MAX_VALUE;
            t.v3.distSqdFromViewpoint = Float.MAX_VALUE;
        }
        for (int loopi = 0; loopi < triangleAry.length; loopi++) {
            Triangle t = triangleAry[loopi];
            if (null == t) {
                continue;
            }
            if (t.v1.distSqdFromViewpoint == Float.MAX_VALUE) {
                calcDistSqd(t.v1, camera);
            }
            if (t.v2.distSqdFromViewpoint == Float.MAX_VALUE) {
                calcDistSqd(t.v2, camera);
            }
            if (t.v3.distSqdFromViewpoint == Float.MAX_VALUE) {
                calcDistSqd(t.v3, camera);
            }
            t.closestDistSqdToViewpoint = t.v1.distSqdFromViewpoint;
            if (t.v2.distSqdFromViewpoint < t.closestDistSqdToViewpoint) {
                t.closestDistSqdToViewpoint = t.v2.distSqdFromViewpoint;
            }
            if (t.v3.distSqdFromViewpoint < t.closestDistSqdToViewpoint) {
                t.closestDistSqdToViewpoint = t.v3.distSqdFromViewpoint;
            }
        }
    }

    private void drawTriangles() {
        panelWidth = getWidth();
        panelHeight = getHeight();
        double xcenter = panelWidth / 2;
        double ycenter = panelHeight / 2;

        int[] x = new int[3];
        int[] y = new int[3];
        Graphics g = img.getGraphics();
        g.setColor(Color.gray);
        g.fillRect(0, 0, (int) panelWidth, (int) panelHeight);
        g.setColor(Color.black);
        //	System.err.println("Painter.update: Drawing triangles!");

        // Note, at the moment the triangleAry is sorted and we draw
        // from far to near - hence nearer triangles over write
        // further triangles, giving the appearance of hidden line
        // removal.

        int drawCount = 0;
        for (int loopi = triangleAry.length - 1; loopi >= 0; loopi--) {
            Triangle t = triangleAry[loopi];
            if (null == t) {
                continue;
            }
            int visibleCount = t.visibleCount;
            if (t.visibleCount > 0) {
                while (t != null) {
                    drawCount++;
                    x[0] = screenx(t.v1.screenx, xcenter);
                    y[0] = screeny(t.v1.screeny, ycenter);
                    x[1] = screenx(t.v2.screenx, xcenter);
                    y[1] = screeny(t.v2.screeny, ycenter);
                    x[2] = screenx(t.v3.screenx, xcenter);
                    y[2] = screeny(t.v3.screeny, ycenter);
                    // 		System.out.println("Painter.update: Drawing triangle "
                    // 				   +x[0]+", "+y[0] +" ("+t.v1.screenx+", "+t.v1.screeny+") "
                    // 				   +x[1]+", "+y[1] +" ("+t.v2.screenx+", "+t.v2.screeny+") "
                    // 				   +x[2]+", "+y[2] +" ("+t.v3.screenx+", "+t.v3.screeny+")");

                    g.setColor(Color.white);
                    if (visibleCount > 1) {
                        g.setColor(Color.red);
                    }
                    g.fillPolygon(x, y, 3);
                    g.setColor(Color.black);
                    g.drawPolygon(x, y, 3);
                    t = t.extraTriangles;
                }
            }
        }
        g.setFont(new Font("Dialog", Font.BOLD, 18));
        g.drawString("Triangles drawn = " + drawCount, 20, 50);
        g.dispose();
        //	System.err.println("Painter.update: Done drawing "+drawCount+" triangles!");
    }

    public void update(ArrayList<Triangle> triangles, GLCamera camera) {
        if (triangles.size() <= 0) {
            return;
        }
        triangleAry = triangles.toArray(new Triangle[1]);

        calculateDistances(camera);

        Arrays.sort(triangleAry);

        drawTriangles();
        repaint();
    }

    public BufferedImage createCompatible(int width, int height, int transparency) {
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage compatible = gc.createCompatibleImage(width, height, transparency);
        return compatible;
    }

    private void createFrame() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int displayWidth = screenSize.width - 30;
        int displayHeight = screenSize.height - 30;
        img = createCompatible(displayWidth, displayHeight, Transparency.OPAQUE);

        setPreferredSize(new Dimension(displayWidth, displayHeight));
        setSize(new Dimension(displayWidth, displayHeight));
        topFrame = new JFrame("Painter");
        topFrame.getContentPane().setLayout(new BorderLayout());
        topFrame.getContentPane().add(this, BorderLayout.CENTER);
        topFrame.setVisible(true);
        topFrame.setPreferredSize(new Dimension(displayWidth, displayHeight));
        topFrame.setSize(new Dimension(displayWidth, displayHeight));
        setPreferredSize(new Dimension(displayWidth, displayHeight));
        setSize(new Dimension(displayWidth, displayHeight));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        //	System.out.println("Painter.paintComponent: painting");
        g.drawImage(img, 0, 0, null);
        //	System.out.println("Painter.paintComponent: done painting");
    }
}
