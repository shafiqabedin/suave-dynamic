package uavmap;

import suave.*;
import java.io.*;
import java.util.*;


public class UavMapLog {

    private FileOutputStream logStream = null;
    private PrintStream logPrint = null;

    // Given a time, get a string representing the current time in
    // format YYYY_MM_DD_HH_MM_SS, i.e. 2005_02_18_23_16_24.  Using
    // this format results in timestamp strings (or filenames) that
    // will sort to date order.  If 'time' is null it will be
    // filled in with the current time.
    private static String getTimeStamp(Calendar time) {
        String timestamp = "";
        String part = null;
        if(null == time)
            time = Calendar.getInstance();
        
        timestamp = Integer.toString(time.get(time.YEAR));
        
        part = Integer.toString((time.get(time.MONTH)+1));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        part = Integer.toString(time.get(time.DAY_OF_MONTH));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        part = Integer.toString(time.get(time.HOUR_OF_DAY));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        part = Integer.toString(time.get(time.MINUTE));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        part = Integer.toString(time.get(time.SECOND));
        if(1 == part.length())
            part = "0"+part;
        timestamp += part;
        
        return timestamp;
    }

    public UavMapLog() {
	String logFileName = "uavmap"+getTimeStamp(null)+".log";
	try {
	    logStream = new FileOutputStream(logFileName);
	}
	catch(FileNotFoundException e) {
	    Debug.debug(3, "IOException opening log file ("+logFileName+") for writing, e="+e);
	}
	logPrint = new PrintStream( logStream );
	synchronized(logPrint) {
	    logPrint.println(System.currentTimeMillis()+" start");
	}
    }

    public void click(String clicktype, UavMapTarget target) {
	LogLine line = new LogLine(clicktype, target);
	synchronized(logPrint) {
	    logPrint.println(line);
	}
    }

    public void navData(int uavid, double latitude, double longitude, double altitude, double localx, double localy, double yaw, double pitch, double roll) {
	LogLine line = new LogLine(uavid, latitude, longitude, altitude, localx, localy, yaw, pitch, roll);
	synchronized(logPrint) {
	    logPrint.println(line);
	}
    }

    public static ArrayList<LogLine> parseFile(String filename) {
	ArrayList<LogLine> logAry = new ArrayList<LogLine>();

	ArrayList<ArrayList<String>> lineList = CSV.parseFile(filename, ' ');
	for(int loopi = 0; loopi < lineList.size(); loopi++) {
	    LogLine logLine = new LogLine(lineList.get(loopi));
	    logAry.add(logLine);
	}
	return logAry;
    }

    public static void playbackNavDataFromFile(UavMapDisplay mapDisplay, String filename, double speedup) {
	// @TODO: This is just to test the GeoTransforms code;
	//
	// guessing at the origin height
	GeoTransformsLVCS lvcs = new GeoTransformsLVCS();
	GeoTransforms.init_lvcs(lvcs, 
				Math.toRadians(UavMapDisplay.ORIGIN_LAT),
				Math.toRadians(UavMapDisplay.ORIGIN_LON),
				400);

	ArrayList<LogLine> logAry = parseFile(filename);

	long playbackStartTime = System.currentTimeMillis();
	long logStartTime = -1;
	for(int loopi = 0; loopi < logAry.size(); loopi++) {
	    LogLine logLine = logAry.get(loopi);

	    // The first logs we recorded don't have a start time.  
	    if(-1 == logStartTime)
		logStartTime = logLine.time;

	    if(logLine.navData) {

		long logLineTimeSinceStart = logLine.time - logStartTime;
		logLineTimeSinceStart = (long)((double)logLineTimeSinceStart/speedup);
		long timeToUpdate = playbackStartTime + logLineTimeSinceStart;
		long timeToSleep = timeToUpdate - System.currentTimeMillis();

		// @TODO: This is just to test the GDS2LVCSUtils code;
		double xyz[] = new double[3];
		GeoTransforms.gps_to_lvcs(lvcs, Math.toRadians(logLine.lat),Math.toRadians(logLine.lon),logLine.alt, xyz);
		Debug.debug(1, "orig_gps "+logLine.lat+", "+logLine.lon+", "+logLine.alt+" orig_conv "+logLine.localx+","+logLine.localy+" lvcs "+xyz[0]+", "+xyz[1]+", "+xyz[2]);

		if(timeToSleep > 0) {
		    Debug.debug(1, "logtime "+logLine.time+" logLineTimeSinceStart "+logLineTimeSinceStart+" sleeping "+timeToSleep+" until "+timeToUpdate);
		    try {Thread.sleep(timeToSleep); } catch (Exception e) {}		    
		}
		mapDisplay.updateUav(logLine.uavid, logLine.lat, logLine.lon, logLine.alt, logLine.yaw, logLine.pitch, logLine.roll);
	    }
	    else if(logLine.start) {
		logStartTime = logLine.time;
	    }
	}

    }

    
}
