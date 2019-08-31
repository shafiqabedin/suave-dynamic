/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author owens
 */
public class DEMLla implements GeoTransformsConstants {

    private DecimalFormat elevFmt = new DecimalFormat(".##");
    private String inputFilename;
    private float[][][] verticeData;

    public float[][][] getVerticeData() {
        return verticeData;
    }

    public DEMLla(String inputFilename) {
        this.inputFilename = inputFilename;
    }

    private void subsample(int step) {
        int width = verticeData.length;
        int height = verticeData[0].length;
        int newWidth = width / step;
        int newHeight = height / step;
        float[][][] verticeData2 = new float[newWidth][newHeight][3];
        Debug.debug(1, "subsample: verticedata size old " + width + ", " + height + " new " + newWidth + ", " + newHeight);

        for (int loopx = 0; loopx < newWidth; loopx++) {
            for (int loopy = 0; loopy < newHeight; loopy++) {
                try {
                    verticeData2[loopx][loopy][0] = verticeData[loopx * step][loopy * step][0];
                    verticeData2[loopx][loopy][1] = verticeData[loopx * step][loopy * step][1];
                    verticeData2[loopx][loopy][2] = verticeData[loopx * step][loopy * step][2];
                } catch (Exception e) {
                    Debug.debug(5, "subsample: Exception subsampling, was copying from " + (loopx * step) + ", " + (loopy * step) + " to " + loopx + ", " + loopy + " e =" + e);
                    e.printStackTrace();
                }
            }
        }
        verticeData = verticeData2;
    }

    public void go(String outputFilename) {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(inputFilename);
            bufferedReader = new BufferedReader(fileReader);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DEMLla.class.getName()).log(Level.SEVERE, "FileNotFound, filename=" + inputFilename, ex);
        }

        FileWriter fileWriter = null;
        PrintWriter writer = null;
        try {
            fileWriter = new FileWriter(outputFilename);
            writer = new PrintWriter(new BufferedWriter(fileWriter));
        } catch (IOException ex) {
            Logger.getLogger(DEMLla.class.getName()).log(Level.SEVERE, "Couldn't open outputfile! filename=" + outputFilename, ex);
        }
        double[] values = new double[3];
        String line;
        try {
            int rowCount = 1;
            double lastx;
            double lasty;
            double firstElev;
            line = bufferedReader.readLine();
            if (null == line) {
                writer.close();
                return;
            }
            Debug.debug(1, "Processing row " + rowCount);
            parseDoubleList(3, values, line);
            writer.print(elevFmt.format(values[0]) + " " + elevFmt.format(values[1]) + " " + elevFmt.format(values[2]));
            lastx = values[0];
            lasty = values[1];
            firstElev = values[2];

            while (true) {
                line = bufferedReader.readLine();
                if (null == line) {
                    break;
                }
                parseDoubleList(3, values, line);
                if (values[0] != lastx || values[1] != (lasty + 1)) {
                    writer.println();
                    rowCount++;
                    Debug.debug(1, "Processing row " + rowCount);
                    writer.print(elevFmt.format(values[0]) + " " + elevFmt.format(values[1]) + " " + elevFmt.format(values[2]));
                    lastx = values[0];
                    lasty = values[1];
                    firstElev = values[2];
                } else {
                    lasty++;
//                    writer.print(" " + elevFmt.format(values[2]));
                    writer.print(" " + elevFmt.format(firstElev - values[2]));
                }
            }
//            }
        } catch (IOException ex) {
            Logger.getLogger(DEMLla.class.getName()).log(Level.SEVERE, null, ex);
        }
        writer.flush();
        writer.close();
    }

    public void loadLla() {
        int width = 0;
        int height = 0;
        ArrayList<double[]> corners = new ArrayList<double[]>();

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(inputFilename);
            bufferedReader = new BufferedReader(fileReader);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DEMLla.class.getName()).log(Level.SEVERE, "FileNotFound, filename=" + inputFilename, ex);
        }


        double[] values = new double[3];
        String line;
        try {
            while (true) {
                line = bufferedReader.readLine();
                if (line.equalsIgnoreCase("DATA")) {
                    Debug.debug(1, "DATA starting");
                    break;
                }
                if (line.startsWith("width=")) {
                    width = Integer.parseInt(line.substring(6));
                    Debug.debug(1, "width=" + width);
                } else if (line.startsWith("height=")) {
                    height = Integer.parseInt(line.substring(7));
                    Debug.debug(1, "height=" + height);
                } else if (line.startsWith("corner=")) {
                    double[] xyzll = new double[5];
                    Debug.debug(1, "Parsing 5 doubles out of |" + line.substring(7) + "|");
                    parseDoubleList(5, xyzll, line.substring(7));
                    Debug.debug(1, "corner= local coords (x is east/west, z is north/south, y is alt) " + xyzll[0] + ", " + xyzll[1] + ", " + xyzll[2] + " lat/lon " + xyzll[3] + ", " + xyzll[4]);
                    corners.add(xyzll);
                }
            }
            verticeData = new float[width][height][3];

            double messageAt = .10;
            double messageAtInc = .10;
            for (int loopx = 0; loopx < width; loopx++) {
                double percentLoaded = (double) loopx / (double) width;
                if (percentLoaded >= messageAt) {
                    Debug.debug(1, "loadLla: percent loaded = " + percentLoaded);
                    messageAt += messageAtInc;
                }

                for (int loopy = 0; loopy < height; loopy++) {
                    line = bufferedReader.readLine();

                    parseDoubleList(3, values, line);
                    float lon = (float) values[0];
                    float lat = (float) values[1];
                    float alt = (float) values[2];
                    if (lat < 51) {
                        Debug.debug(5, "Got lat < 51, lat/lon/alt=" + lat + ", " + lon + ", " + alt + " line=" + line);
                    }
                    verticeData[loopx][loopy][LAT_INDEX] = lat;
                    verticeData[loopx][loopy][LON_INDEX] = lon;
                    verticeData[loopx][loopy][ALT_INDEX] = alt;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DEMLla.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeLla(String outputFilename) {

        FileWriter fileWriter = null;
        PrintWriter writer = null;
        try {
            fileWriter = new FileWriter(outputFilename);
            writer = new PrintWriter(new BufferedWriter(fileWriter));
        } catch (IOException ex) {
            Logger.getLogger(DEMLla.class.getName()).log(Level.SEVERE, "Couldn't open outputfile! filename=" + outputFilename, ex);
        }
        int width = verticeData.length;
        int height = verticeData[0].length;
        writer.println("width=" + width);
        writer.println("height=" + height);
        writer.println("DATA");
        double x;
        double y;
        double z;
        for (int loopx = 0; loopx < width; loopx++) {
            Debug.debug(1, "writing" + loopx);
            for (int loopy = 0; loopy < height; loopy++) {
                x = verticeData[loopx][loopy][0];
                y = verticeData[loopx][loopy][1];
                z = verticeData[loopx][loopy][2];
                writer.println(elevFmt.format(x) + " " + elevFmt.format(y) + " " + elevFmt.format(z));
            }
        }
        writer.flush();
        writer.close();
    }

    public static void printErrMsg(String methodName, String errorMsg, int mCount, char message[]) {
        System.err.println("ERROR: " + methodName + ": " + errorMsg);
        System.err.print("ERROR: " + methodName + ": msg=\\");
        for (int loopi = 0; loopi < message.length; loopi++) {
            System.err.print(message[loopi]);
        }
        System.err.println("\\");
        System.err.print("ERROR: " + methodName + ":      ");
        for (int i = 0; i < mCount; i++) {
            System.err.print(" ");
        }
        System.err.println("^");
    }

    // if errMsg != null, then we test if we've run past end of message
    // and if so, printErrMsg(errMsg), and return -1.  If no error then
    // we return the mCount indexing the next non-whitespace char.
    public static int skipWhiteSpace(int mCount, char messageChars[], String errMsg) {
        //Skip whitespace
        while (mCount < messageChars.length) {
            if (messageChars[mCount] == ' ' || messageChars[mCount] == '\n' || messageChars[mCount] == '\t') {
                mCount++;
            } else {
                break;
            }
        }
        if (errMsg != null) {
            if (mCount >= messageChars.length) {
                printErrMsg("RString.skipWhiteSpace", errMsg, mCount, messageChars);
                return -1;
            }
        }
        return mCount;
    }
    // Parse a string with a known number of space separated doubles
    // into an array of doubles - very quickly.
    //
    // This very gnarly version of parseDoubleList below with hand
    // coded double parsing seems to be about 2.5 times faster.
    // Scary.  I ran a test with this routine against the 'safe'
    // routine above, comparing the numbers resulting, and they're
    // identical.  Note that this fast version only handles numbers
    // formed like; [-]digit*[.digit*] with a maximum of 10 digits
    // after the decimal point.  (But making it handle more digits
    // after the decimal place should be easy, just increase the size
    // of the tenPowers array.)

    public static void parseDoubleListFast(int numDoubles, double returnArray[], String list) {
        if (list == null) {
            return;
        }
        if (list.equals("")) {
            return;
        }

        int returnArrayCount = 0;

        // Copy list into a char array.
        char listChars[];
        listChars = new char[list.length()];
        list.getChars(0, list.length(), listChars, 0);

        int count = 0;
        int itemStart = 0;
        int itemEnd = 0;
        String newItem = "0.0";
        int accum = 0;
        boolean negative = false;
        boolean parsingFraction = false;
        int fractionAccum = 0;
        int fractionDecimalPlaces = 0;
        double result = 0.0;
        double tenPowers[] = {
            1.0,
            10.0,
            100.0,
            1000.0,
            10000.0,
            100000.0,
            1000000.0,
            10000000.0,
            100000000.0,
            1000000000.0,
            10000000000.0,
            100000000000.0,
            1000000000000.0,
            10000000000000.0,
            100000000000000.0,
            1000000000000000.0
        };
        char nextChar;

        while (count < listChars.length) {
            // Skip any leading whitespace

            while (count < listChars.length) {
                if (listChars[count] == ' ' || listChars[count] == '\n' || listChars[count] == '\t') {
                    count++;
                } else {
                    break;
                }
            }
            if (count >= listChars.length) {
                break;
            }

            accum = 0;
            fractionAccum = 0;
            fractionDecimalPlaces = 0;
            negative = false;
            parsingFraction = false;
            while (count < listChars.length) {
                nextChar = listChars[count];
                if (nextChar == ' ' || nextChar == '\n' || nextChar == '\t') {
                    break;
                }

                if (nextChar == '-') {
                    negative = true;
                } else if (nextChar == '.') {
                    parsingFraction = true;
                } else {
                    if (false == parsingFraction) {
                        if (nextChar == '0') {
                            accum = (accum * 10);
                        } else if (nextChar == '1') {
                            accum = (accum * 10) + 1;
                        } else if (nextChar == '2') {
                            accum = (accum * 10) + 2;
                        } else if (nextChar == '3') {
                            accum = (accum * 10) + 3;
                        } else if (nextChar == '4') {
                            accum = (accum * 10) + 4;
                        } else if (nextChar == '5') {
                            accum = (accum * 10) + 5;
                        } else if (nextChar == '6') {
                            accum = (accum * 10) + 6;
                        } else if (nextChar == '7') {
                            accum = (accum * 10) + 7;
                        } else if (nextChar == '8') {
                            accum = (accum * 10) + 8;
                        } else if (nextChar == '9') {
                            accum = (accum * 10) + 9;
                        } else {
                            break;
                        }
                    } else {
                        fractionDecimalPlaces++;
                        if (nextChar == '0') {
                            fractionAccum = (fractionAccum * 10);
                        } else if (nextChar == '1') {
                            fractionAccum = (fractionAccum * 10) + 1;
                        } else if (nextChar == '2') {
                            fractionAccum = (fractionAccum * 10) + 2;
                        } else if (nextChar == '3') {
                            fractionAccum = (fractionAccum * 10) + 3;
                        } else if (nextChar == '4') {
                            fractionAccum = (fractionAccum * 10) + 4;
                        } else if (nextChar == '5') {
                            fractionAccum = (fractionAccum * 10) + 5;
                        } else if (nextChar == '6') {
                            fractionAccum = (fractionAccum * 10) + 6;
                        } else if (nextChar == '7') {
                            fractionAccum = (fractionAccum * 10) + 7;
                        } else if (nextChar == '8') {
                            fractionAccum = (fractionAccum * 10) + 8;
                        } else if (nextChar == '9') {
                            fractionAccum = (fractionAccum * 10) + 9;
                        } else {
                            break;
                        }
                    }
                }
                count++;
            }
            if (fractionDecimalPlaces <= tenPowers.length) {
                result = (double) accum + (((double) fractionAccum) / tenPowers[fractionDecimalPlaces]);
            }

            if (negative) {
                result = -result;
            }
            returnArray[returnArrayCount++] = result;
        }
    }
    // Parse a string with a known number of space separated doubles
    // into an array of doubles.

    public static double[] parseDoubleList(int numDoubles,double[] returnArray,  String list) {
        if (list == null) {
            return null;
        }
        if (list.equals("")) {
            return null;
        }

        int returnArrayCount = 0;

        // Copy list into a char array.
        char listChars[];
        listChars = new char[list.length()];
        list.getChars(0, list.length(), listChars, 0);
        int listLength = listChars.length;

        int count = 0;
        int itemStart = 0;
        int itemEnd = 0;
        int itemLength = 0;
        String newItem = null;

        int lastStart = 0;
        int lastEnd = 0;
        int lastLength = 0;

        while (count < listLength) {
            // Skip any leading whitespace
            itemEnd = skipWhiteSpace(count, listChars, null);
            count = itemEnd;
            if (count >= listLength) {
                break;
            }
            itemStart = count;
            itemEnd = itemStart;
            while (itemEnd < listLength) {
                if ((listChars[itemEnd] != ' ') && (listChars[itemEnd] != '\n') && (listChars[itemEnd] != '\t')) {
                    itemEnd++;
                } else {
                    break;
                }
            }
            itemLength = itemEnd - itemStart;
            returnArray[returnArrayCount++] = Double.parseDouble(new String(listChars, itemStart, itemLength));

            count = itemEnd;
            lastStart = itemStart;
            lastEnd = itemEnd;
            lastLength = itemLength;
        }
        return returnArray;
    }

    public void move(String inputFilename, String outputFilename, double moveX, double moveY, double moveZ) {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(inputFilename);
            bufferedReader = new BufferedReader(fileReader);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DEMLla.class.getName()).log(Level.SEVERE, "FileNotFound, filename=" + inputFilename, ex);
        }

        FileWriter fileWriter = null;
        PrintWriter writer = null;
        try {
            fileWriter = new FileWriter(outputFilename);
            writer = new PrintWriter(new BufferedWriter(fileWriter));
        } catch (IOException ex) {
            Logger.getLogger(DEMLla.class.getName()).log(Level.SEVERE, "Couldn't open outputfile! filename=" + outputFilename, ex);
        }
        double[] values = new double[3];
        String line;


        try {
            while (true) {
                line = bufferedReader.readLine();
                writer.println(line);
                if (line.equalsIgnoreCase("DATA")) {
                    Debug.debug(1, "DATA starting");
                    break;
                }
            }

            while (true) {
                line = bufferedReader.readLine();
                if (null == line) {
                    break;
                }
                parseDoubleList(3, values, line);
                writer.println(elevFmt.format(values[0] - moveX) + " " + elevFmt.format(values[1] - moveY) + " " + elevFmt.format(values[2] - moveZ));
            }
//            }
        } catch (IOException ex) {
            Logger.getLogger(DEMLla.class.getName()).log(Level.SEVERE, null, ex);
        }
        writer.flush();
        writer.close();
    }

    public static void main(String[] argv) {
//        System.err.println("argv[0] = "+argv[0]);
//        System.err.println("argv[1] = "+argv[1]);
//        DEMLla v;
////        v = new VBS2Reader(argv[0], argv[1], 5000, 5000);
//        v = new DEMLla("g:\\suave\\trunk\\warminster\\heightmap_2010_09_08_013723.xyz");
//        v.readVBS2xyz(5000, 5000);
//        v.subsample(5);
//        v.writeXYZ("g:\\suave\\trunk\\warminster\\heightmap_2010_09_08_013723_sub_05.xyz");
//        v.subsample(2);
//        v.writeXYZ("g:\\suave\\trunk\\warminster\\heightmap_2010_09_08_013723_sub_10.xyz");
//        v.subsample(2);
//        v.writeXYZ("g:\\suave\\trunk\\warminster\\heightmap_2010_09_08_013723_sub_20.xyz");
//        v.subsample(2);
//        v.writeXYZ("g:\\suave\\trunk\\warminster\\heightmap_2010_09_08_013723_sub_40.xyz");

//        v = new VBS2Reader("g:\\heightmap_2010_09_08_013723.zzz");
//        v.readZZZ();
//        v.subsample(25);
//        v.writeZZZ("g:\\heightmap_2010_09_08_013723_sub_25.zzz");
//        v = new VBS2Reader("g:\\heightmap_2010_09_08_013723_sub_25.zzz");
//        v.readZZZ();
//        v.subsample(2);
//        v.writeZZZ( "g:\\heightmap_2010_09_08_013723_sub_50.zzz");
        DEMLla v;

        v = new DEMLla(null);
        v.move("g:\\suave\\trunk\\warminster\\heightmap_2010_09_23_012103.xyz",
                "g:\\suave\\trunk\\warminster\\heightmap_2010_09_23_012103_shifted.xyz",
                2500, 2500, 0);


    }
}
