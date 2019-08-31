package uavmap;

import java.util.*;

public class LogLine {

    public long time;
    public boolean start = false;
    public boolean navData = false;
    public boolean clickAdd = false;
    public String clickType = null;
    public int uavid = -1;
    public int clickId = -1;
    public double lat = -1;
    public double lon = -1;
    public double alt = -1;
    public double localx = -1;
    public double localy = -1;
    public double yaw = 0;
    public double pitch = 0;
    public double roll = 0;
    public double screenx = -1;
    public double screeny = -1;
    public double screenw = -1;
    public double screenh = -1;

    public LogLine() {
    }

    public LogLine(String clicktype, UavMapTarget target) {
        time = System.currentTimeMillis();
        this.clickType = clicktype;
        clickId = target.id;
        lat = target.lat;
        lon = target.lon;
        localx = target.localx;
        localy = target.localy;
        screenx = target.screenx;
        screeny = target.screeny;
        screenw = target.screenw;
        screenh = target.screenh;
    }

    public LogLine(int uavid, double latitude, double longitude, double altitude, double localx, double localy, double yaw, double pitch, double roll) {
        time = System.currentTimeMillis();
        this.uavid = uavid;
        this.lat = latitude;
        this.lon = longitude;
        this.alt = altitude;
        this.localx = localx;
        this.localy = localy;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }

    public LogLine(ArrayList<String> fieldList) {
        String[] fields = fieldList.toArray(new String[1]);
        this.time = Long.parseLong(fields[0]);

        if (fields[1].equalsIgnoreCase("navdata")) {
            this.navData = true;
            this.uavid = Integer.parseInt(fields[2]);
            this.lat = Double.parseDouble(fields[3]);
            this.lon = Double.parseDouble(fields[4]);
            this.alt = Double.parseDouble(fields[5]);
            this.localx = Double.parseDouble(fields[6]);
            this.localy = Double.parseDouble(fields[7]);
            this.yaw = Double.parseDouble(fields[8]);
            this.pitch = Double.parseDouble(fields[9]);
            this.roll = Double.parseDouble(fields[10]);
        } // Original log format, soon to be obsolete
        else if (fields[1].equalsIgnoreCase("UAV")) {
            this.navData = true;
            this.uavid = Integer.parseInt(fields[2]);
            this.lat = Double.parseDouble(fields[3]);
            this.lon = Double.parseDouble(fields[4]);
            this.alt = Double.parseDouble(fields[5]);
            this.localx = Double.parseDouble(fields[6]);
            this.localy = Double.parseDouble(fields[7]);
        } else if (fields[1].equalsIgnoreCase("click")
                || fields[1].equalsIgnoreCase("clickadd")
                || fields[1].startsWith("click")) {
            this.clickAdd = true;
            this.clickType = fields[1];
            this.clickId = Integer.parseInt(fields[2]);
            this.lat = Double.parseDouble(fields[3]);
            this.lon = Double.parseDouble(fields[4]);
            this.localx = Double.parseDouble(fields[5]);
            this.localy = Double.parseDouble(fields[6]);
            this.screenx = Double.parseDouble(fields[7]);
            this.screeny = Double.parseDouble(fields[8]);
            this.screenw = Double.parseDouble(fields[9]);
            this.screenh = Double.parseDouble(fields[10]);
        } else if (fields[1].equalsIgnoreCase("start")) {
            this.start = true;
        }
    }

    public static LogLine parseLine(ArrayList<String> fieldList) {
        return new LogLine(fieldList);
    }
}
