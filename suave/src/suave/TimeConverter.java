package suave;

import java.util.*;
import java.text.*;

public class TimeConverter {

    // telemetry.heading = { 'Abs Time', 'Rel Time (ms)', 'Altitude', 'Airspeed', 'Roll', 'Pitch', 'Heading', 'Turn Rate', 'RSSI', 'RC PPS', 'Battery Cur', 'Battery Volt', 'Sys Status', 'GPS Num Sat Lock', 'Alt AI state', 'Des Alt', 'Des Airspd', 'Des Roll', 'Des Pitch', 'Des Hdg', 'Des Turn Rate', 'Servo Aileron', 'Servo Elev', 'Servo Thro', 'Servo Rud', 'UAV Control Mode', 'Fail Safe', 'Mag Hdg', 'Airborne Timer', 'Avionics Timer', 'System Flags', 'Payload 1', 'Payload 2', 'GPS velocity', 'GPS Alt', 'GPS Hdg', 'GPS Lat', 'GPS Long', 'Home Lat', 'Home Long', 'Cur Cmd', 'Nav State', 'Desired Lat', 'Desired Long', 'Time Over Target', 'Distance to Target', 'Heading to Target', 'FLC', 'Wind Hdg', 'Wind Spd', 'IO State', 'UTC Time', 'Count Timer', 'System Flags 1', 'Temperature R', 'NAV Avx Timer', 'Home Alt MSL', 'Roll Rate', 'Pitch Rate', 'Yaw Rate', 'Alt MSL', 'Gimbal Trg Lat', 'Gimbal Trg Lon' };
    // [ [0 0 0 12 15 22] 0 -126.666641 -2.350000 0.171000 -0.264000 0.092000 0.015000 0 0 1.3 12.4 141 9 0 100.000031 14.000000 0.000000 -0.379000 0.000000 0.000000 -0.050000 0.028571 0.000000 0.000000 0 0 0.092000 0.000000 560.475525 4096 0.000000 0.519000 0.450000 332.166718 0.977000 40.461918 -79.784279 40.460812 -79.783882 0 0 0.000000 0.000000 0 0 0.000000 828 0.000000 0.000000 10 [08 10 22 16 15 24] 16 0 37.857143 560.455383 0.000030 0.000000 0.000000 0.012500 203.166702 0.000000 0.000000];...
    public TimeConverter() {
    }

    public static long convertToMsSinceEpoch(String UTCTime) {
        if (UTCTime.startsWith("[")) {
            UTCTime = UTCTime.substring(1);
        }
        if (UTCTime.endsWith("]")) {
            UTCTime = UTCTime.substring(0, UTCTime.length() - 1);
        }

        TimeZone UTCZone = TimeZone.getTimeZone("UTC");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd hh mm ss");
        sdf.setTimeZone(UTCZone);
        Date parsedDate = null;
        try {
            System.err.println("Parsing UTC string '" + UTCTime + "'");
            parsedDate = sdf.parse(UTCTime);
        } catch (ParseException e) {
            System.err.println("Exception parsing UTC time: e=" + e);
            e.printStackTrace();
        }
        Date dateNow = new Date();
        long diff = dateNow.getTime() - parsedDate.getTime();

        SimpleDateFormat output = new SimpleDateFormat();

        System.err.println("now = " + dateNow.getTime() + " parsed value in UTC = " + parsedDate.getTime() + " diff = " + diff + " in days = " + (((double) diff) / (24 * 60 * 60 * 1000)) + " formatted parsed UTC time = " + output.format(parsedDate));

        return parsedDate.getTime();
    }
}
