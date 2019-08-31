package suave;

import java.awt.geom.Area;
import java.awt.Rectangle;
import java.util.*;

public class Triangle implements Comparable {

    Vertex v1;
    Vertex v2;
    Vertex v3;
    float normalx = 0;
    float normaly = 0;
    float normalz = 1;

    public void calculateTriangleNormal() {
        float[] edge1 = new float[3];
        float[] edge2 = new float[3];
        float[] normal = new float[3];
        float[] p1 = {v1.x, v1.y, v1.z};
        float[] p2 = {v2.x, v2.y, v2.z};
        float[] p3 = {v3.x, v3.y, v3.z};

        Vec3f.sub(edge1, p2, p1);
        Vec3f.sub(edge2, p3, p2);
        Vec3f.cross(normal, edge1, edge2);
        normalx = normal[0];
        normaly = normal[1];
        normalz = normal[2];
    }
    float closestDistSqdToViewpoint = 0;
    int visibleCount = 0;
    Triangle extraTriangles = null;
    boolean areaHasBeenClipped = false;
    Area area = null;
    int leftx = 0;
    int rightx = 0;
    int topy = 0;
    int boty = 0;
    float[] rasterizedUvxyz = null;
    public int spatialSearchCounter = 0;
    private FrameList frameList = null;

    public void addFrame(VideoFrame frame) {
        if (null == frameList) {
            frameList = new FrameList();
        }
        frameList.addFrame(frame);
    }

    public ArrayList<Integer> getFrameList() {
        if (null == frameList) {
            return null;
        }
        return frameList.getFrameList();
    }

    public Triangle(Vertex v1, Vertex v2, Vertex v3) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public Triangle addTriangle() {
        Triangle t2 = new Triangle(new Vertex(v1), new Vertex(v2), new Vertex(v3));
        t2.extraTriangles = extraTriangles;
        extraTriangles = t2;
        return t2;
    }

    public void reset() {
        visibleCount = 0;
        extraTriangles = null;
        areaHasBeenClipped = false;
        area = null;
    }

    public String toString() {
        return "visibleCount=" + visibleCount + " closestDistSqd=" + closestDistSqdToViewpoint + " normal = (" + normalx + ", " + normaly + ", " + normalz + ") vertices = " + v1 + " / " + v2 + " / " + v3;
    }

    public int compareTo(Object triangle) throws ClassCastException {
        if (!(triangle instanceof Triangle)) {
            throw new ClassCastException("A Triangle object expected.");
        }

        if (closestDistSqdToViewpoint < ((Triangle) triangle).closestDistSqdToViewpoint) {
            return -1;
        } else if (closestDistSqdToViewpoint > ((Triangle) triangle).closestDistSqdToViewpoint) {
            return 1;
        } else {
            return 0;
        }
    }

    public void setBounds(Rectangle bounds) {
        leftx = bounds.x;
        rightx = bounds.x + bounds.width;
        topy = bounds.y;
        boty = bounds.y + bounds.height;
    }

    public boolean boundsOverlap(Triangle t) {
        if ((leftx >= t.leftx) && (leftx <= t.rightx)) {
            return true;
        }
        if ((rightx <= t.rightx) && (rightx >= t.leftx)) {
            return true;
        }
        if ((topy >= t.topy) && (topy <= t.boty)) {
            return true;
        }
        if ((boty <= t.boty) && (boty >= t.topy)) {
            return true;
        }
        return false;
    }
    public static double fudge = .002;

    public static int collision(Triangle tri1, Triangle tri2) {
        double axisX, axisY;
        double tmp, minA, maxA, minB, maxB;

        axisX = tri1.v3.screeny - tri1.v1.screeny;
        axisY = tri1.v1.screenx - tri1.v3.screenx;

        minA = maxA = tri1.v1.screenx * axisX + tri1.v1.screeny * axisY;
        tmp = tri1.v2.screenx * axisX + tri1.v2.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }
        tmp = tri1.v3.screenx * axisX + tri1.v3.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }

        minB = maxB = tri2.v1.screenx * axisX + tri2.v1.screeny * axisY;
        tmp = tri2.v2.screenx * axisX + tri2.v2.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }
        tmp = tri2.v3.screenx * axisX + tri2.v3.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }

        if ((maxA - fudge) < minB || (minA + fudge) > maxB) {
            return 0;
        }

        axisX = tri1.v1.screeny - tri1.v2.screeny;
        axisY = tri1.v2.screenx - tri1.v1.screenx;

        minA = maxA = tri1.v1.screenx * axisX + tri1.v1.screeny * axisY;
        tmp = tri1.v2.screenx * axisX + tri1.v2.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }
        tmp = tri1.v3.screenx * axisX + tri1.v3.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }

        minB = maxB = tri2.v1.screenx * axisX + tri2.v1.screeny * axisY;
        tmp = tri2.v2.screenx * axisX + tri2.v2.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }
        tmp = tri2.v3.screenx * axisX + tri2.v3.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }

        if ((maxA - fudge) < minB || (minA + fudge) > maxB) {
            return 0;
        }

        axisX = tri1.v2.screeny - tri1.v3.screeny;
        axisY = tri1.v3.screenx - tri1.v2.screenx;

        minA = maxA = tri1.v1.screenx * axisX + tri1.v1.screeny * axisY;
        tmp = tri1.v2.screenx * axisX + tri1.v2.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }
        tmp = tri1.v3.screenx * axisX + tri1.v3.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }

        minB = maxB = tri2.v1.screenx * axisX + tri2.v1.screeny * axisY;
        tmp = tri2.v2.screenx * axisX + tri2.v2.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }
        tmp = tri2.v3.screenx * axisX + tri2.v3.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }

        if ((maxA - fudge) < minB || (minA + fudge) > maxB) {
            return 0;
        }

        axisX = tri2.v2.screeny - tri2.v1.screeny;
        axisY = tri2.v1.screenx - tri2.v2.screenx;

        minA = maxA = tri1.v1.screenx * axisX + tri1.v1.screeny * axisY;
        tmp = tri1.v2.screenx * axisX + tri1.v2.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }
        tmp = tri1.v3.screenx * axisX + tri1.v3.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }

        minB = maxB = tri2.v1.screenx * axisX + tri2.v1.screeny * axisY;
        tmp = tri2.v2.screenx * axisX + tri2.v2.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }
        tmp = tri2.v3.screenx * axisX + tri2.v3.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }

        if ((maxA - fudge) < minB || (minA + fudge) > maxB) {
            return 0;
        }

        axisX = tri2.v1.screeny - tri2.v2.screeny;
        axisY = tri2.v2.screenx - tri2.v1.screenx;

        minA = maxA = tri1.v1.screenx * axisX + tri1.v1.screeny * axisY;
        tmp = tri1.v2.screenx * axisX + tri1.v2.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }
        tmp = tri1.v3.screenx * axisX + tri1.v3.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }

        minB = maxB = tri2.v1.screenx * axisX + tri2.v1.screeny * axisY;
        tmp = tri2.v2.screenx * axisX + tri2.v2.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }
        tmp = tri2.v3.screenx * axisX + tri2.v3.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }

        if ((maxA - fudge) < minB || (minA + fudge) > maxB) {
            return 0;
        }

        axisX = tri2.v2.screeny - tri2.v3.screeny;
        axisY = tri2.v3.screenx - tri2.v2.screenx;

        minA = maxA = tri1.v1.screenx * axisX + tri1.v1.screeny * axisY;
        tmp = tri1.v2.screenx * axisX + tri1.v2.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }
        tmp = tri1.v3.screenx * axisX + tri1.v3.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }

        minB = maxB = tri2.v1.screenx * axisX + tri2.v1.screeny * axisY;
        tmp = tri2.v2.screenx * axisX + tri2.v2.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }
        tmp = tri2.v3.screenx * axisX + tri2.v3.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }

        if ((maxA - fudge) < minB || (minA + fudge) > maxB) {
            return 0;
        }

        return 1;
    }

    private static void overlap(Triangle tri1, Triangle tri2, double[] axisAndAmount) {
        double axisX, axisY;
        double tmp, minA, maxA, minB, maxB;

        axisX = tri1.v3.screeny - tri1.v1.screeny;
        axisY = tri1.v1.screenx - tri1.v3.screenx;

        double smallestMax;
        double largestMin;
        double overlap;
        double smallestAxisX;
        double smallestAxisY;
        double smallestOverlap = Double.MAX_VALUE;

        minA = maxA = tri1.v1.screenx * axisX + tri1.v1.screeny * axisY;
        tmp = tri1.v2.screenx * axisX + tri1.v2.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }
        tmp = tri1.v3.screenx * axisX + tri1.v3.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }

        minB = maxB = tri2.v1.screenx * axisX + tri2.v1.screeny * axisY;
        tmp = tri2.v2.screenx * axisX + tri2.v2.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }
        tmp = tri2.v3.screenx * axisX + tri2.v3.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }

// 	if (maxA < minB || minA > maxB)
// 	    return 0;

        smallestAxisX = axisX;
        smallestAxisY = axisY;
        smallestMax = (maxA > maxB) ? maxB : maxA;
        largestMin = (minA > minB) ? minA : minB;
        smallestOverlap = smallestMax - largestMin;

        axisX = tri1.v1.screeny - tri1.v2.screeny;
        axisY = tri1.v2.screenx - tri1.v1.screenx;

        minA = maxA = tri1.v1.screenx * axisX + tri1.v1.screeny * axisY;
        tmp = tri1.v2.screenx * axisX + tri1.v2.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }
        tmp = tri1.v3.screenx * axisX + tri1.v3.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }

        minB = maxB = tri2.v1.screenx * axisX + tri2.v1.screeny * axisY;
        tmp = tri2.v2.screenx * axisX + tri2.v2.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }
        tmp = tri2.v3.screenx * axisX + tri2.v3.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }

// 	if (maxA < minB || minA > maxB)
// 	    return 0;

        smallestMax = (maxA > maxB) ? maxB : maxA;
        largestMin = (minA > minB) ? minA : minB;
        overlap = smallestMax - largestMin;
        if (overlap < smallestOverlap) {
            smallestAxisX = axisX;
            smallestAxisY = axisY;
            smallestOverlap = overlap;
        }


        axisX = tri1.v2.screeny - tri1.v3.screeny;
        axisY = tri1.v3.screenx - tri1.v2.screenx;

        minA = maxA = tri1.v1.screenx * axisX + tri1.v1.screeny * axisY;
        tmp = tri1.v2.screenx * axisX + tri1.v2.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }
        tmp = tri1.v3.screenx * axisX + tri1.v3.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }

        minB = maxB = tri2.v1.screenx * axisX + tri2.v1.screeny * axisY;
        tmp = tri2.v2.screenx * axisX + tri2.v2.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }
        tmp = tri2.v3.screenx * axisX + tri2.v3.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }

// 	if (maxA < minB || minA > maxB)
// 	    return 0;
        smallestMax = (maxA > maxB) ? maxB : maxA;
        largestMin = (minA > minB) ? minA : minB;
        overlap = smallestMax - largestMin;
        if (overlap < smallestOverlap) {
            smallestAxisX = axisX;
            smallestAxisY = axisY;
            smallestOverlap = overlap;
        }

        axisX = tri2.v2.screeny - tri2.v1.screeny;
        axisY = tri2.v1.screenx - tri2.v2.screenx;

        minA = maxA = tri1.v1.screenx * axisX + tri1.v1.screeny * axisY;
        tmp = tri1.v2.screenx * axisX + tri1.v2.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }
        tmp = tri1.v3.screenx * axisX + tri1.v3.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }

        minB = maxB = tri2.v1.screenx * axisX + tri2.v1.screeny * axisY;
        tmp = tri2.v2.screenx * axisX + tri2.v2.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }
        tmp = tri2.v3.screenx * axisX + tri2.v3.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }

// 	if (maxA < minB || minA > maxB)
// 	    return 0;
        smallestMax = (maxA > maxB) ? maxB : maxA;
        largestMin = (minA > minB) ? minA : minB;
        overlap = smallestMax - largestMin;
        if (overlap < smallestOverlap) {
            smallestAxisX = axisX;
            smallestAxisY = axisY;
            smallestOverlap = overlap;
        }

        axisX = tri2.v1.screeny - tri2.v2.screeny;
        axisY = tri2.v2.screenx - tri2.v1.screenx;

        minA = maxA = tri1.v1.screenx * axisX + tri1.v1.screeny * axisY;
        tmp = tri1.v2.screenx * axisX + tri1.v2.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }
        tmp = tri1.v3.screenx * axisX + tri1.v3.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }

        minB = maxB = tri2.v1.screenx * axisX + tri2.v1.screeny * axisY;
        tmp = tri2.v2.screenx * axisX + tri2.v2.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }
        tmp = tri2.v3.screenx * axisX + tri2.v3.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }

// 	if (maxA < minB || minA > maxB)
// 	    return 0;
        smallestMax = (maxA > maxB) ? maxB : maxA;
        largestMin = (minA > minB) ? minA : minB;
        overlap = smallestMax - largestMin;
        if (overlap < smallestOverlap) {
            smallestAxisX = axisX;
            smallestAxisY = axisY;
            smallestOverlap = overlap;
        }

        axisX = tri2.v2.screeny - tri2.v3.screeny;
        axisY = tri2.v3.screenx - tri2.v2.screenx;

        minA = maxA = tri1.v1.screenx * axisX + tri1.v1.screeny * axisY;
        tmp = tri1.v2.screenx * axisX + tri1.v2.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }
        tmp = tri1.v3.screenx * axisX + tri1.v3.screeny * axisY;
        if (tmp > maxA) {
            maxA = tmp;
        } else if (tmp < minA) {
            minA = tmp;
        }

        minB = maxB = tri2.v1.screenx * axisX + tri2.v1.screeny * axisY;
        tmp = tri2.v2.screenx * axisX + tri2.v2.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }
        tmp = tri2.v3.screenx * axisX + tri2.v3.screeny * axisY;
        if (tmp > maxB) {
            maxB = tmp;
        } else if (tmp < minB) {
            minB = tmp;
        }

// 	if (maxA < minB || minA > maxB)
// 	    return 0;
        smallestMax = (maxA > maxB) ? maxB : maxA;
        largestMin = (minA > minB) ? minA : minB;
        overlap = smallestMax - largestMin;
        if (overlap < smallestOverlap) {
            smallestAxisX = axisX;
            smallestAxisY = axisY;
            smallestOverlap = overlap;
        }

        axisAndAmount[0] = smallestAxisX;
        axisAndAmount[1] = smallestAxisY;
        axisAndAmount[2] = smallestOverlap;

        return;
    }
}
