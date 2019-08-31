/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

/**
 *
 * @author owens
 */
public class DEM implements GeoTransformsConstants {

    public final static float HOLE_MARKER_ALTITUDE = Float.NEGATIVE_INFINITY;
    private int width;
    private int height;
    private float verticeData[][][];
    private boolean hasTiepoint = false;
    private double[] tiepoint;
    private double[] xAndYMinusHeight;

    public DEM(int width, int height, float[][][] verticeData) {
        this.width = width;
        this.height = height;
        this.verticeData = verticeData;
    }

    public float[][][] getVerticeData() {
        return verticeData;
    }

    /**
     * @return the hasTiePoint
     */
    public boolean hasTiePoint() {
        return hasTiepoint;
    }

    /**
     * @return the tiepoint
     */
    public double[] getTiepoint() {
        return tiepoint;
    }

    /**
     * @return the xAndYMinusHeight
     */
    public double[] getXAndYMinusHeight() {
        return xAndYMinusHeight;
    }

    /**
     * @param set tiepoint to tiepoint and xAndYMinusHeight to xAndYMinusHeight
     */
    public void setTiepoint(double[] tiepoint, double[] xAndYMinusHeight) {
        if (null != tiepoint) {
            hasTiepoint = true;
        }
        this.tiepoint = tiepoint;
        this.xAndYMinusHeight = xAndYMinusHeight;
    }

    // This is a simultaneous binary search of the verticeData array
    // in 2D.  It's necessary to do this because our vertice array
    // isn't necessarily axis aligned.  It's pretty fast, but if we
    // end up calling this a _lot_ we may want to consider doing some
    // kind of lookup table.
    public float getAltitude(float localX, float localY) {
        if (null == verticeData) {
            return -1;
        }

        float x2, y2;

        int firstx = 0;
        int firsty = 0;
        int lastx = width - 1;
        int lasty = height - 1;
        int midx = 0;
        int midy = 0;

        while ((firstx < lastx) && (firsty < lasty)) {
            midx = (firstx + lastx) / 2;
            midy = (firsty + lasty) / 2;
            // NOTE we're getting y from the 'z' slot.  OpenGL expects
            // Y to refer to the up/down axis.
            x2 = verticeData[midx][midy][OGL_X];
            y2 = verticeData[midx][midy][OGL_Z];
            if (x2 > localX) {
                lastx = midx - 1;
            } else if (x2 < localX) {
                firstx = midx + 1;
            }
            if (y2 > localY) {
                firsty = midy - 1;
            } else if (y2 < localY) {
                lasty = midy + 1;
            }
        }

        return verticeData[midx][midy][OGL_Y];
    }
}
