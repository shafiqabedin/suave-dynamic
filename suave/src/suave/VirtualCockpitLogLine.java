package suave;

import java.util.*;
import java.text.*;
import java.io.*;
import java.io.IOException;

public class VirtualCockpitLogLine {

    private final static DecimalFormat fmt = new DecimalFormat("0000000");
    private final static DecimalFormat fmt2 = new DecimalFormat("0000000000000");
    private final static DecimalFormat fmt3 = new DecimalFormat("0.000000");
    private final static DecimalFormat fmt4 = new DecimalFormat("0.000");
    private static final char DQ = '"';
    private static final char SQ = '\'';
    private static final char ESC = '\\';
    public static ArrayList<String> headers = null;
    public static long firstUTCTime = -1;
    public String absTimeString = null;	// 0
    public long absTime = 0;	// 0
    public long relTime = 0;	// 1
    public double altitude = 0;	// 2
    public double airspeed = 0;	// 3
    public double roll = 0;	// 4 - radians I think
    public double pitch = 0;	// 5 - radians I think
    public double heading = 0;	// 6 - radians I think
    // Mag hdg - raw heading from magnetometer - 27
    // Airborne Timer 28
    // Avionics Timer 29
    // GPS velocity - 33
    public double gpsAlt = 0;	// 34
    public double gpsHdg = 0;	// 35 - radians
    public double gpsLat = 0; 	// 36
    public double gpsLong = 0;	// 37
    public String utcTimeString = null; 	// 51
    public long utcTime = 0; 	// 51
    public double homeAltMsl = 0;    // 56
    public double altMsl = 0; // 60
    public long utcPlusRel = 0;

    public VirtualCockpitLogLine() {
    }

    public VirtualCockpitLogLine(ArrayList<String> fieldList) {
        String[] fields = fieldList.toArray(new String[1]);
// 	for(int loopi = 0; loopi < fields.length; loopi++) {
// 	    System.err.println("fields["+loopi+"] = '"+fields[loopi]+"'");
// 	}

        absTimeString = fields[0];
        absTime = convertToMsSinceEpoch(absTimeString);
        relTime = Long.parseLong(fields[1]);
        altitude = Double.parseDouble(fields[2]);
        airspeed = Double.parseDouble(fields[3]);
        roll = Double.parseDouble(fields[4]);
        pitch = Double.parseDouble(fields[5]);
        heading = Double.parseDouble(fields[6]);
        gpsAlt = Double.parseDouble(fields[34]);
        gpsHdg = Double.parseDouble(fields[35]);
        gpsLat = Double.parseDouble(fields[36]);
        gpsLong = Double.parseDouble(fields[37]);
        utcTimeString = fields[51];
        utcTime = convertToMsSinceEpoch(utcTimeString);
        if (firstUTCTime < 0) {
            firstUTCTime = utcTime;
        }
        utcPlusRel = firstUTCTime + relTime;
        homeAltMsl = Double.parseDouble(fields[56]);
        altMsl = Double.parseDouble(fields[60]);
        //	System.err.println("VCLogLine: "+fmt2.format(utcPlusRel)+" "+fmt.format(relTime)+" "+fmt3.format(airspeed)+" ("+fmt3.format(gpsLat)+", "+fmt3.format(gpsLong)+", "+fmt3.format(gpsAlt)+") "+fmt4.format(altitude)+" "+fmt4.format(altMsl)+" "+fmt4.format(homeAltMsl)+" "+ fmt4.format(roll)+" "+fmt4.format(pitch)+" "+fmt4.format(heading));
        //	System.err.println("VCLogLine: "+fmt2.format(utcPlusRel)+" "+fmt.format(relTime)+", "+fmt3.format(gpsAlt)+") "+fmt4.format(altitude)+" "+fmt4.format(altMsl)+" "+fmt4.format(homeAltMsl));
    }

    public String toString() {
        return //	    absTime
                //	    +"\t"+
                relTime
                + "\t" + altitude
                + "\t" + altMsl
                + "\t" + gpsAlt
                //	    +"\t"+airspeed
                + "\t" + roll
                + "\t" + pitch
                + "\t" + heading
                //	    +"\t"+gpsHdg
                + "\t" + gpsLat
                + "\t" + gpsLong
                + "\t" + utcTime;
    }

    public static VirtualCockpitLogLine parseLine(ArrayList<String> fieldList) {
        return new VirtualCockpitLogLine(fieldList);
    }

    private static String trimChar(String field, char trimChar) {
        boolean noSubstring = true;
        if (field == null) {
            return null;
        }

        int start = 0;
        while (field.charAt(start) == trimChar) {
            start++;
            noSubstring = true;
        }
        int end = field.length() - 1;
        while (field.charAt(end) == trimChar) {
            end--;
            noSubstring = true;
        }
        if (noSubstring) {
            return field;
        }

        return field.substring(start, end);
    }

    public static ArrayList<String> parseLine(String list, char delim) {
        if (list == null) {
            return null;
        }
        if (list.equals("")) {
            return null;
        }

        ArrayList<String> fieldList = new ArrayList<String>();

        // Copy list into a char array.
        char listChars[];
        listChars = new char[list.length()];
        list.getChars(0, list.length(), listChars, 0);

        int count = 0;
        int itemStart = 0;
        int itemEnd = 0;
        String newItem = null;

        while (count < listChars.length) {
            boolean dQuotedFlag = false;
            boolean sQuotedFlag = false;
            boolean bracketFlag = false;
            count = itemEnd;
            itemStart = count;
            itemEnd = itemStart;
            while (itemEnd < listChars.length) {
                // skip over double quoted values
                if (DQ == listChars[itemEnd]) {
                    dQuotedFlag = true;
                    while (itemEnd < listChars.length) {
                        itemEnd++;
                        if (DQ == listChars[itemEnd]) {
                            if (ESC != listChars[itemEnd - 1]) {
                                break;
                            }
                        }
                    }
                }
                if (SQ == listChars[itemEnd]) {
                    sQuotedFlag = true;
                    while (itemEnd < listChars.length) {
                        itemEnd++;
                        if (SQ == listChars[itemEnd]) {
                            if (ESC != listChars[itemEnd - 1]) {
                                break;
                            }
                        }
                    }
                }
                if ('[' == listChars[itemEnd]) {
                    bracketFlag = true;
                    while (itemEnd < (listChars.length - 1)) {
                        itemEnd++;
                        if (']' == listChars[itemEnd]) {
                            if (ESC != listChars[itemEnd - 1]) {
                                break;
                            }
                        }
                    }
                }

                if (delim == listChars[itemEnd]) {
                    break;
                }
                itemEnd++;
            }
            newItem = new String(listChars, itemStart, itemEnd - itemStart);
            itemEnd++;
            count = itemEnd;
            if (dQuotedFlag) {
                newItem = trimChar(newItem.trim(), DQ);
            }
            if (sQuotedFlag) {
                newItem = trimChar(newItem.trim(), SQ);
            }
            if (bracketFlag) {
                // @TODO: this is kinda bad... assumes no [ or ]
                // inside.  But then we don't do nested brackets.
                newItem = trimChar(newItem.trim(), '[');
                newItem = trimChar(newItem.trim(), ']');
            }
            newItem = newItem.trim().intern();
            fieldList.add(newItem);
        }

        return fieldList;
    }

    public static boolean ignoreLogLine(String line) {
        if (null == line) {
            return true;
        } else if (line.startsWith("telemetry.heading = {")) {
            return true;
        } else if (line.startsWith("telemetry.data = [ ...")) {
            return true;
        } else if (line.startsWith("];")) {
            return true;
        }
        return false;
    }

    public static String trimLogLine(String line) {
        line = line.substring(2);
        line = line.substring(0, line.length() - 5);
        return line;
    }

    public static boolean goodLogLine(String line) {
        if (ignoreLogLine(line)) {
            return false;
        }
        return (line.startsWith("[ ") && line.endsWith("];..."));
    }

    public static ArrayList<VirtualCockpitLogLine> parseFile(String filename) {
        int lineCount = 0;
        ArrayList<VirtualCockpitLogLine> logAry = new ArrayList<VirtualCockpitLogLine>();

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(filename);
            bufferedReader = new BufferedReader(fileReader);

            // First line of VC telemetry log is headers (describing
            // what each column is) in a different format.
            String line = null;

            line = bufferedReader.readLine();
            lineCount++;
            VirtualCockpitLogLine.headers = parseLine(line, ',');

            while (true) {
                line = bufferedReader.readLine();
                if (null == line) {
                    break;
                }
                lineCount++;
                if (!goodLogLine(line)) {
                    Debug.debug(3, "VirtualCockpitLogLine.parseFile: WARNING: Found a weird line in telemetry file on line " + lineCount);
                    Debug.debug(3, "VirtualCockpitLogLine.parseFile: WARNING: Will print line delimited by pipe (|) char and then throw line away.");
                    Debug.debug(3, "VirtualCockpitLogLine.parseFile: WARNING: line=|" + line + "|");
                    continue;
                }
                line = trimLogLine(line);
                logAry.add(new VirtualCockpitLogLine(parseLine(line, ' ')));
            }
            bufferedReader.close();
        } catch (IOException e) {
            // Now what?  Do we only get this for real errors, or do we get it for EOF?
            System.err.println("IOException reading file " + filename + ", e=" + e);
            e.printStackTrace();
            return null;
        }

        return logAry;
    }

    public static long convertToMsSinceEpoch(String UTCTime) {
        if (UTCTime.startsWith("[")) {
            UTCTime = UTCTime.substring(1);
        }
        if (UTCTime.endsWith("]")) {
            UTCTime = UTCTime.substring(0, UTCTime.length() - 1);
        }

        TimeZone UTCZone = TimeZone.getTimeZone("UTC");
        SimpleDateFormat sdf = new SimpleDateFormat("yy MM dd hh mm ss");
        sdf.setTimeZone(UTCZone);
        Date parsedDate = null;
        try {
            parsedDate = sdf.parse(UTCTime);
        } catch (ParseException e) {
            System.err.println("Exception parsing UTC time: e=" + e);
            e.printStackTrace();
        }
        return parsedDate.getTime();
    }

    public static void displayFile(String filename) {
        Console console = new Console();
        boolean running = true;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));

            int lineCount = 0;
            while (running) {
                String line;
                line = reader.readLine();
                if (null != line) {
                    lineCount++;
                    if (!goodLogLine(line)) {
                        console.addText("WARNING: weirdness at line " + lineCount + " line=|" + line + "|\n");
                    } else {
                        line = trimLogLine(line);
                        VirtualCockpitLogLine vcl = new VirtualCockpitLogLine(parseLine(line, ' '));
                        console.addText("ROLL= " + fmt4.format(vcl.roll)
                                + " PITCH= " + fmt4.format(vcl.pitch)
                                + " HEADING= " + fmt4.format(vcl.heading)
                                + "\n");
                    }

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        running = false;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("IOException reading file " + filename + ", e=" + e);
            e.printStackTrace();
        }
    }

    // 0  'Abs Time'	
    // 1  'Rel Time (ms)' 
    // 2  'Altitude' 
    // 3  'Airspeed' 
    // 4  'Roll' 
    // 5  'Pitch' 
    // 6  'Heading' 
    // 7  'Turn Rate' 
    // 8  'RSSI' 
    // 9  'RC PPS' 
    // 10  'Battery Cur' 
    // 11  'Battery Volt' 
    // 12  'Sys Status' 
    // 13  'GPS Num Sat Lock' 
    // 14  'Alt AI state' 
    // 15  'Des Alt' 
    // 16  'Des Airspd' 
    // 17  'Des Roll' 
    // 18  'Des Pitch' 
    // 19  'Des Hdg' 
    // 20  'Des Turn Rate' 
    // 21  'Servo Aileron' 
    // 22  'Servo Elev' 
    // 23  'Servo Thro' 
    // 24  'Servo Rud' 
    // 25  'UAV Control Mode' 
    // 26  'Fail Safe' 
    // 27  'Mag Hdg' 
    // 28  'Airborne Timer' 
    // 29  'Avionics Timer' 
    // 30  'System Flags' 
    // 31  'Payload 1' 
    // 32  'Payload 2' 
    // 33  'GPS velocity' 
    // 34  'GPS Alt' 
    // 35  'GPS Hdg' 
    // 36  'GPS Lat' 
    // 37  'GPS Long' 
    // 38  'Home Lat' 
    // 39  'Home Long' 
    // 40  'Cur Cmd' 
    // 41  'Nav State' 
    // 42  'Desired Lat' 
    // 43  'Desired Long' 
    // 44  'Time Over Target' 
    // 45  'Distance to Target' 
    // 46  'Heading to Target' 
    // 47  'FLC' 
    // 48  'Wind Hdg' 
    // 49  'Wind Spd' 
    // 50  'IO State' 
    // 51  'UTC Time' 
    // 52  'Count Timer' 
    // 53  'System Flags 1' 
    // 54  'Temperature R' 
    // 55  'NAV Avx Timer' 
    // 56  'Home Alt MSL' 
    // 57  'Roll Rate' 
    // 58  'Pitch Rate' 
    // 59  'Yaw Rate' 
    // 60  'Alt MSL' 
    // 61  'Gimbal Trg Lat' 
    // 62  'Gimbal Trg Lon' 
    public static void main(String argv[]) {
        displayFile(argv[0]);
    }
}
