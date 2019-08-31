package suave;

import java.util.*;

public class vcdecode {

    private final static double GASCOLA_FLYBOX_DEFAULT_ORIGIN_LAT = 40.4563444444;
    private final static double GASCOLA_FLYBOX_DEFAULT_ORIGIN_LON = -79.789486111;
    private final static double GASCOLA_FLYBOX_DEFAULT_ORIGIN_ALT = 0;

    public static void decode(String filename) {

        double originLat = GASCOLA_FLYBOX_DEFAULT_ORIGIN_LAT;
        double originLon = GASCOLA_FLYBOX_DEFAULT_ORIGIN_LON;
        double originAlt = GASCOLA_FLYBOX_DEFAULT_ORIGIN_ALT;

        double xPos, yPos, zPos;
        ArrayList<VirtualCockpitLogLine> vcLog = VirtualCockpitLogLine.parseFile(filename);

        GeoTransformsLVCS lvcs = new GeoTransformsLVCS();
        GeoTransforms.init_lvcs(lvcs,
                Math.toRadians(originLat),
                Math.toRadians(originLon),
                originAlt);

        VirtualCockpitLogLine vcll = null;
        long lastRelTime = 0;
        for (int loopi = 0; loopi < vcLog.size(); loopi++) {
            vcll = vcLog.get(loopi);

            double xyz[] = new double[3];
            GeoTransforms.gps_to_lvcs(lvcs, Math.toRadians(vcll.gpsLat), Math.toRadians(vcll.gpsLong), vcll.gpsAlt, xyz);

            // NOTE: yes we're swapping z and y here.  uav
            // logs assume z is up/down (altitude), whereas
            // jogl assumes Z is into/out of the screen.
            xPos = (float) xyz[0];
            yPos = (float) xyz[2];
            zPos = (float) xyz[1];
            Debug.debug(1, (vcll.relTime / 1000) + "\t" + (vcll.relTime - lastRelTime) + "\t" + xPos + ", " + yPos + ", " + zPos + "\t" + vcll.heading + ", " + vcll.pitch + ", " + vcll.roll);
            lastRelTime = vcll.relTime;
        }
    }

    public static void main(String[] args) {
        decode("c:\\laptop\\owens\\suave\\telemetry_UAV5_2008_10_24_00.m");
    }
}
