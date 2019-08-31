package suave;

public class GeoTransformsLVCS {

    /**********************************************************************
     * Local Vertical Coordinate System. The LVCS is a local cartesian     *
     * coordinate system oriented so that the positive x axis points east, *
     * y points north, and positive z points up (parallel to gravity).     *
     * It is defined with respect to some origin lat0, lon0, height0.      *
     **********************************************************************/
    // typedef struct {
    /* origin in lat, lon, (radians) and height (meters) wrt to WGS84   */
    public double lat0rad, lon0rad, hmeters;
    /* origin in geocentric xyz.  Geocentric coords are defined wrt     */
    /* the center of the WGS84 ellipsoid.  X goes through the Greenwich */
    /* meridian, Z through the north pole, and Y is orthogonal to both. */
    public double x0_ecef, y0_ecef, z0_ecef;
    /* rotation matrix to reorient local cartesian coord system so that */
    /* xy plane is tangent to WGS84 ellipsoid at origin lat0,lon0       */
    public double rotmat[] = new double[9];
    public double invrotmat[] = new double[9];
    // } LVCS;
}
