/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

/**
 *
 * @author owens
 */
public class Origin implements GeoTransformsConstants {

    private double lat;
    private double lon;
    private double lt;
    private GeoTransformsLVCS lvcs;

    public Origin(double originLat, double originLon, double originAlt) {
        this.lat = originLat;
        this.lon = originLon;
        this.lt = originAlt;
        lvcs = new GeoTransformsLVCS();
        GeoTransforms.init_lvcs(lvcs, Math.toRadians(originLat), Math.toRadians(originLon), originAlt);
    }

    /**
     * @return the origin latitude
     */
    public double getLat() {
        return lat;
    }

    /**
     * @return the origin longitude
     */
    public double getLon() {
        return lon;
    }

    /**
     * @return the origin altitude
     */
    public double getAlt() {
        return lt;
    }

    public void gpsDegreesToLvcs(double latDegrees, double lonDegrees, double alt, double[] xyzResults) {
        GeoTransforms.gps_to_lvcs(lvcs, Math.toRadians(latDegrees), Math.toRadians(lonDegrees), alt, xyzResults);
    }

    public void lvcsToGpsDegrees(double[] lvcsXyz, double[] llaResults) {
        GeoTransforms.lvcs_to_gps(lvcs, lvcsXyz[LVCS_X],lvcsXyz[LVCS_Y],lvcsXyz[LVCS_Z], llaResults);
        llaResults[0] = Math.toDegrees(llaResults[0]);
        llaResults[1] = Math.toDegrees(llaResults[1]);
        llaResults[2] = Math.toDegrees(llaResults[2]);
    }

    // assume your view point sitting at more or less 0,0,0 facing 'north'
    //
    // in OpenGL; positive Z is south, positive Y is up, positive X is east
    //
    // in the LVCs; positive Y is north, positive Z is up, positive X is east
    //
    // so to convert, we must swap Y and Z and negate the Z value.

    public static void lvcsToOpenGL(double lvcs[], double opengl[]) {
        // this is a little odd - the swap is done this way so that if the caller passes
        // in the same array for both params, it still works.
        opengl[OGL_X] = lvcs[LVCS_X];
        double temp = lvcs[LVCS_Y];
        opengl[OGL_Y] = lvcs[LVCS_Z];
        opengl[OGL_Z] = -temp;
    }

    public static void lvcsToOpenGL(float lvcs[], float opengl[]) {
        // this is a little odd - the swap is done this way so that if the caller passes
        // in the same array for both params, it still works.
        opengl[OGL_X] = lvcs[LVCS_X];
        float temp = lvcs[LVCS_Y];
        opengl[OGL_Y] = lvcs[LVCS_Z];
        opengl[OGL_Z] = -temp;
    }

    public static void openGLToLvcs(double opengl[], double lvcs[]) {
        // this is a little odd - the swap is done this way so that if the caller passes
        // in the same array for both params, it still works.
        lvcs[LVCS_X] = opengl[OGL_X];
        double temp = opengl[OGL_Y];
        lvcs[LVCS_Y] = opengl[OGL_Z];
        lvcs[LVCS_Z] = -temp;
    }

    public static void openGLToLvcs(float opengl[], float lvcs[]) {
        // this is a little odd - the swap is done this way so that if the caller passes
        // in the same array for both params, it still works.
        lvcs[LVCS_X] = opengl[OGL_X];
        float temp = opengl[OGL_Y];
        lvcs[LVCS_Y] = opengl[OGL_Z];
        lvcs[LVCS_Z] = -temp;
    }
}
