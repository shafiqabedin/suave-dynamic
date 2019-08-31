package uavmap;

// @author      Sean R. Owens
// @version     $Id: LatLonUtil.java,v 1.2 2008/02/08 02:39:49 owens Exp $ 


// NOTE: the functions here that deal with differences in lat/lon,
// i.e. some distance in degrees lat or lon, DO NOT take into account
// 'great circle' distances, and hence WILL NOT WORK right for large
// distances.  Probably it will be ok for distances < 100km.
// (Curvature of the earth gives an altitude drop of approximatley 1
// meter for 25km.)
//
// Straight up conversion of lat/lon to local coords should be ok as
// long as the origin lat/lon are relatively close (100km?) to the
// coordinates being converted.
public class LatLonUtil {
    public final static double LON_ONE_DEGREE_AT_EQUATOR_KM = 111.321;
    public final static double LAT_ONE_DEGREE_AT_EQUATOR_KM = 110.567;
    public final static double LAT_ONE_DEGREE_AT_90DEG_KM = 111.699;
    public final static double MAX_LAT_DEG = 90;
    private final static double LAT_DIFF_KM = LAT_ONE_DEGREE_AT_90DEG_KM - LAT_ONE_DEGREE_AT_EQUATOR_KM;

    public static double toDecimal(double degrees, double minutes, double seconds) {
	return degrees + (minutes/60.0) + (seconds/3600.0);
    }

    public static double degrees(double decimal) {
	return (int)decimal;
    }

    public static double minutes(double decimal) {
	double remain = decimal - (int)decimal;
	return (int)(remain * 60);
    }

    public static double seconds(double decimal) {
	double remain = decimal - (int)decimal;
	double minutes = (remain * 60);
	double remain2 = minutes - (int)minutes;
	return (int)(remain2 * 60);
    }
   
    // @TODO: There are probably better ways to do this...
    // 
    // @NOTE: 
    public static double kmToDegreesLat(double atLat, double distKm) {
	// rough calc of the size of a lat degree, in km, at the
	// spencified lat;
	return distKm/ ((atLat/MAX_LAT_DEG) * LAT_DIFF_KM + LAT_ONE_DEGREE_AT_EQUATOR_KM);
    }

    public static double kmToDegreesLon(double atLat, double distKm) {
	// rough calc of the size of a lat degree, in km, at the
	// specified lat;
	return distKm/(Math.cos(Math.toRadians(atLat)) * LON_ONE_DEGREE_AT_EQUATOR_KM);
    }

    public static double degreesLatToKm(double atLat, double degreesLat) {
        // rough calc of the size of a lat degree, in km, at the
        // spencified lat;
	return degreesLat * ((atLat/MAX_LAT_DEG) * LAT_DIFF_KM + LAT_ONE_DEGREE_AT_EQUATOR_KM);
    }

    public static double degreesLonToKm(double atLat, double degreesLon) {
        // rough calc of the size of a lat degree, in km, at the
        // specified lat;
	return degreesLon * (Math.cos(Math.toRadians(atLat)) * LON_ONE_DEGREE_AT_EQUATOR_KM);
    }

    // NOTE: this does not take into account 'great circle' distances,
    // and hence will not work right for large distances.  Probably it
    // will be ok for distances < 100km.  (Curvature of the earth
    // gives an altitude drop of approximatley 1 meter for 25km.)
    public static double lonToLocalXMeters(double originLat, double originLon, double lon) {
	double lonDiff = lon - originLon;
	double localXMeters = degreesLonToKm(originLat, lonDiff) * 1000;
	//	System.err.println("LatLonUtil.lonToLocalXMeters: origin lat/lon = "+originLat+","+originLon+" lon="+lon+" londiff = "+lonDiff+" localXMeters="+localXMeters);
	return localXMeters;
    }

    // NOTE: this does not take into account 'great circle' distances,
    // and hence will not work right for large distances.  Probably it
    // will be ok for distances < 100km.  (Curvature of the earth
    // gives an altitude drop of approximatley 1 meter for 25km.)
    public static double latToLocalYMeters(double originLat, double originLon, double lat) {
	double latDiff = lat - originLat;
	double localYMeters = degreesLatToKm(originLat, latDiff) * 1000;
	//	System.err.println("LatLonUtil.lonToLocalXMeters: origin lat/lon = "+originLat+","+originLon+" lat="+lat+" latdiff = "+latDiff+" localYMeters="+localYMeters);
	return localYMeters;
    }
}
