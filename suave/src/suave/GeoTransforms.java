package suave;

/* This is a translation to java of some geotransform code written by
 * Bob Collins in 1997. */

/* geotransforms.h -  geographic transformations
 * Bob Collins Sat Oct 25 11:05:16 EDT 1997                      
 * Copyright Carnegie Mellon University, all rights reserved     
 *
 * Modified Fri Aug 14 11:13:45 EDT 1998  - Bob Collins
 *   transforming between geodetic gps coords and ctdb gcs coords
 *
 */
public class GeoTransforms {

    public final static double WGS84_a = 6378137.0;
    public final static double WGS84_ee = 0.00669437999013;

    /**********************************************************************
     * Little routines for manipulating degree,min,second notation         *
     ***********************************************************************/
    public static double dms_to_deg(int d, int m, float s) {
        double absd, deg;
        absd = Math.abs((double) d);
        deg = absd + ((double) m / 60.0) + ((double) s / 3600.0);
        if (d < 0) {
            deg = -deg;
        }
        return (deg);
    }

    public static double dms_to_rad(int d, int m, float s) {
        return (Math.toRadians(dms_to_deg(d, m, s)));
    }

    public static void deg_to_dms(double deg, double degreesMinutesSeconds[]) {
        double tmp, tmp2;
        int dd, mm;
        float ss;

        tmp = Math.abs(deg);
        dd = (int) tmp;
        tmp2 = (tmp - (double) dd) * 60.0;
        mm = (int) tmp2;
        tmp = (tmp2 - (double) mm) * 60.0;
        ss = (float) tmp;
        if (deg < 0) {
            dd = (-dd);
        }
        degreesMinutesSeconds[0] = dd;
        degreesMinutesSeconds[1] = mm;
        degreesMinutesSeconds[2] = ss;
        return;
    }

    //     public static void deg_to_dms_string (double deg, char *string)
    //     {
    //  int d, m;
    //  float s;
    //  deg_to_dms(deg,&d,&m,&s);
    //  sprintf(string,"%d  %d  %.3f",d,m,s);
    //  return;
    //     }
    /**********************************************************************
     * Conversion between geodetic lat-lon-height (GPS coords) and Local   *
     * Vertical Coordinate System x-y-z.  The LVCS is a local cartesian    *
     * coordinate system oriented so that the positive x axis points east, *
     * y points north, and positive z points up (parallel to gravity).     *
     * It is defined with respect to some origin lat0, lon0, height0.      *
     **********************************************************************/
    public static void comp_lvcs_rotmat(double m[], double lat0rad, double lon0rad) {
        m[0] = -Math.sin(lon0rad);
        m[1] = Math.cos(lon0rad);
        m[2] = 0.0;
        m[3] = -Math.sin(lat0rad) * Math.cos(lon0rad);
        m[4] = -Math.sin(lat0rad) * Math.sin(lon0rad);
        m[5] = Math.cos(lat0rad);
        m[6] = Math.cos(lat0rad) * Math.cos(lon0rad);
        m[7] = Math.cos(lat0rad) * Math.sin(lon0rad);
        m[8] = Math.sin(lat0rad);
        return;
    }

    public static void mat_times_vec(double mat[], double vec[], double res[]) {
        res[0] = mat[0] * vec[0] + mat[1] * vec[1] + mat[2] * vec[2];
        res[1] = mat[3] * vec[0] + mat[4] * vec[1] + mat[5] * vec[2];
        res[2] = mat[6] * vec[0] + mat[7] * vec[1] + mat[8] * vec[2];
        return;
    }

    public static void llh_to_ecef(double latrad, double lonrad, double hmeters, double xyz[]) {
        double tmp, N, h;

        h = hmeters;
        tmp = Math.sin(latrad);
        N = WGS84_a / Math.sqrt(1.0 - WGS84_ee * tmp * tmp);
        xyz[0] = (N + h) * Math.cos(latrad) * Math.cos(lonrad);
        xyz[1] = (N + h) * Math.cos(latrad) * Math.sin(lonrad);
        xyz[2] = (N * (1.0 - WGS84_ee) + h) * Math.sin(latrad);
        return;
    }
    public final static double DLATTHRESH = 1.0e-7;  /* gives under .1 meter accuracy */


    public static void ecef_to_llh(double x, double y, double z, double[] llh) {
        double lon, lat, d, newlat, dlat, N = 0, tmp;
        int debugi = 0;
        lon = Math.atan2(y, x);
        d = Math.sqrt(x * x + y * y);
        lat = Math.atan2(z, d);
        dlat = 1.0;
        while (dlat > DLATTHRESH) {
            tmp = Math.sin(lat);
            N = WGS84_a / Math.sqrt(1.0 - WGS84_ee * tmp * tmp);
            newlat = Math.atan2((z + N * WGS84_ee * tmp), d);
            dlat = Math.abs(newlat - lat);
            lat = newlat;
            /* printf("iteration %d, dlat = %f\n",++debugi,dlat); */
        }
        llh[0] = lat;
        llh[1] = lon;
        llh[2] = d / Math.cos(lat) - N;
        return;
    }

    public static void init_lvcs(GeoTransformsLVCS lvcs, double lat0rad, double lon0rad, double hmeters) {
        double xyz[] = new double[3];
        lvcs.lat0rad = lat0rad;
        lvcs.lon0rad = lon0rad;
        lvcs.hmeters = hmeters;
        llh_to_ecef(lat0rad, lon0rad, hmeters, xyz);
        Debug.debug(1, "LVCS origin in ECEF = " + xyz[0] + ", " + xyz[1] + ", " + xyz[2]);
        lvcs.x0_ecef = xyz[0];
        lvcs.y0_ecef = xyz[1];
        lvcs.z0_ecef = xyz[2];
        comp_lvcs_rotmat(lvcs.rotmat, lat0rad, lon0rad);
        lvcs.invrotmat[0] = lvcs.rotmat[0];
        lvcs.invrotmat[1] = lvcs.rotmat[3];
        lvcs.invrotmat[2] = lvcs.rotmat[6];
        lvcs.invrotmat[3] = lvcs.rotmat[1];
        lvcs.invrotmat[4] = lvcs.rotmat[4];
        lvcs.invrotmat[5] = lvcs.rotmat[7];
        lvcs.invrotmat[6] = lvcs.rotmat[2];
        lvcs.invrotmat[7] = lvcs.rotmat[5];
        lvcs.invrotmat[8] = lvcs.rotmat[8];
        return;
    }

    //     void print_lvcs(GeoTransformsLVCS lvcs)
    //     {
    //  char tmpstr[64];
    //  printf("LVCS origin:\n");
    //  deg_to_dms_string(RAD2DEG(lvcs.lat0rad),tmpstr);
    //  printf("    lat0: %f (%s)\n",lvcs.lat0rad,tmpstr);
    //  deg_to_dms_string(RAD2DEG(lvcs.lon0rad),tmpstr);
    //  printf("    lon0: %f (%s)\n",lvcs.lon0rad,tmpstr);
    //  printf("   elev0: %lf\n",lvcs.hmeters);
    //  printf("   ecefX: %f\n",lvcs.x0_ecef);
    //  printf("   ecefY: %f\n",lvcs.y0_ecef);
    //  printf("   ecefZ: %f\n",lvcs.z0_ecef);
    //  printf("  rotmat: %10g %10g %10g\n",
    //         lvcs.rotmat[0],lvcs.rotmat[1],lvcs.rotmat[2]);
    //  printf("          %10g %10g %10g\n",
    //         lvcs.rotmat[3],lvcs.rotmat[4],lvcs.rotmat[5]);
    //  printf("          %10g %10g %10g\n",
    //         lvcs.rotmat[6],lvcs.rotmat[7],lvcs.rotmat[8]);
    //  return;
    //     }
    /* convert between GPS coords and GeoTransformsLVCS */
    public static void gps_to_lvcs(GeoTransformsLVCS lvcs, double latrad, double lonrad, double hmeters, double[] xyz) {
        double tmpxyz[] = new double[3];
        double lxyz[] = new double[3];
        llh_to_ecef(latrad, lonrad, hmeters, tmpxyz);
        tmpxyz[0] -= lvcs.x0_ecef;
        tmpxyz[1] -= lvcs.y0_ecef;
        tmpxyz[2] -= lvcs.z0_ecef;
        mat_times_vec(lvcs.rotmat, tmpxyz, lxyz);
        xyz[0] = lxyz[0];
        xyz[1] = lxyz[1];
        xyz[2] = lxyz[2];
        return;
    }

    /* convert between LVCS and GPS coords */
    public static void lvcs_to_gps(GeoTransformsLVCS lvcs, double xlvcs, double ylvcs, double zlvcs, double llh[]) {
        double tmpxyz[] = new double[3];
        double lxyz[] = new double[3];
        lxyz[0] = xlvcs;
        lxyz[1] = ylvcs;
        lxyz[2] = zlvcs;
        mat_times_vec(lvcs.invrotmat, lxyz, tmpxyz);
        tmpxyz[0] += lvcs.x0_ecef;
        tmpxyz[1] += lvcs.y0_ecef;
        tmpxyz[2] += lvcs.z0_ecef;
        ecef_to_llh(tmpxyz[0], tmpxyz[1], tmpxyz[2], llh);
        return;
    }


    /* convert between two LVCS coord systems */
    public static void lvcs1_to_lvcs2(GeoTransformsLVCS lvcs1, double x1, double y1, double z1,
            GeoTransformsLVCS lvcs2, double[] xyz2) {
        double tmpxyz[] = new double[3];
        double lxyz[] = new double[3];
        /* convert from lvcs1 to ecef */
        lxyz[0] = x1;
        lxyz[1] = y1;
        lxyz[2] = z1;
        mat_times_vec(lvcs1.invrotmat, lxyz, tmpxyz);
        tmpxyz[0] += lvcs1.x0_ecef;
        tmpxyz[1] += lvcs1.y0_ecef;
        tmpxyz[2] += lvcs1.z0_ecef;
        /* convert from ecef to lvcs2 */
        tmpxyz[0] -= lvcs2.x0_ecef;
        tmpxyz[1] -= lvcs2.y0_ecef;
        tmpxyz[2] -= lvcs2.z0_ecef;
        mat_times_vec(lvcs2.rotmat, tmpxyz, lxyz);
        xyz2[0] = lxyz[0];
        xyz2[1] = lxyz[1];
        xyz2[2] = lxyz[2];
        return;
    }
}
