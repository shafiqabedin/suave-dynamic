/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vbs2gui;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * A utility class for VBS2 command generation and parsing
 * @author pkv
 */
public class Vbs2Utils {
    // Set in VBS2 scenario/description.ext
    final static int telemetryBoxWidth = 11;
    final static int telemetryBoxHeight = 21;
    
    private static Pattern coordsPattern = Pattern.compile("[\\[\\],]+");
    private static Pattern originPattern = Pattern.compile("[\"\\[\\], ]+");
    
    public enum UnitType {
        HMMWV
    }

    public static double[] parseArray(String array) {
        String[] str = coordsPattern.split(array);
        double[] coords = new double[str.length-1];

        for (int i = 1; i < str.length; i++) {
            try {
                coords[i-1] = Double.parseDouble(str[i]);
            } catch (NumberFormatException e) {
                coords[i-1] = Double.NaN;
            }
        }

        return coords;
    }

    public static double[] parseCoord(String array) {
        // System.out.println("Parsing origin from: " + array);
        double[] ret = new double[2];
        String[] str = originPattern.split(array);
        if(str[2].equalsIgnoreCase("N")) {
            ret[0] = Double.valueOf(str[1])*Math.PI/180.0;
        }
        else {
            ret[0] = -1*Double.valueOf(str[1])*Math.PI/180.0;
        }
        if(str[4].equalsIgnoreCase("E")) {
            ret[1] = Double.valueOf(str[3])*Math.PI/180.0;
        }
        else {
            ret[1] = -1*Double.valueOf(str[3])*Math.PI/180.0;
        }
        return ret;
     }

    /**
     * Takes a series of 6 pixel's RGB values and decodes them to a decimal number.
     * @param img a VBS2 frame
     * @return the object's latitude, longitude, height ASL, direction vector,
     *  and up vector
     * latitude, longitude, direction vector and up vector are encoded with
     *  range ~+-2^10 and height is encoded with range ~+-2^15m
     */
    public static double[] parseImageTelemetry(BufferedImage img, boolean useGPS) {
        // Pixel boxes 2 through 55 encode telemetry data - each telemetry data
        //  value is encoded in 6 boxes, and each box contains 6 bits of data,
        //  so each value is encoded in 4 bytes
        //  pixel boxes).
        int boxesPerNum = 6;
        int sColor;
        int[] raw = new int[3*boxesPerNum];
        Color color;
        // VBS2 uses a right handed coordinate system where +x is East, +y is
        //  North and +z is up. To keep things in the lab standardized, let's
        //  use USARSim's more traditional coordinate system where +x is North,
        //  +y is East and +z is down.
        // Ignore the above, we are now using latitude/longitude instead of x/y
        double temp;
        double[] telemetry = new double[9];
        // X/Y/ASL
        for (int i = 0; i < 3; i++) {
            for (int box = 0; box < boxesPerNum; box++) {
                sColor = img.getRGB((i * boxesPerNum + box + 1) * telemetryBoxWidth + telemetryBoxWidth/2, telemetryBoxHeight/2);
                color = new Color(sColor);
                raw[box * 3] = color.getRed();
                raw[box * 3 + 1] = color.getGreen();
                raw[box * 3 + 2] = color.getBlue();
            }
            if(useGPS && i < 2) {
                // Latitude/Longitude
                telemetry[i] = rgbToGPS(raw);
            } else {
                // Z
                telemetry[i] = rgbToDec15(raw);
            }
        }
        if(useGPS) {
            // Swap order from lat/long to lon/lat
            temp = telemetry[0];
            telemetry[0] = telemetry[1];
            telemetry[1] = temp;
        } else {
            // Transform to USARSim coordinates
            temp = telemetry[0];
            telemetry[0] = telemetry[1];
            telemetry[1] = temp;
            telemetry[2] = -telemetry[2];
        }
        // Look vector
        for (int i = 3; i < 6; i++) {
            for (int box = 0; box < boxesPerNum; box++) {
                sColor = img.getRGB((i * boxesPerNum + box + 1) * telemetryBoxWidth + telemetryBoxWidth/2, telemetryBoxHeight/2);
                color = new Color(sColor);
                raw[box * 3] = color.getRed();
                raw[box * 3 + 1] = color.getGreen();
                raw[box * 3 + 2] = color.getBlue();
            }
            if(useGPS) {
                telemetry[i] = rgbToDec10(raw);
            } else {
                // @todo: Should really be using rgbToDec10 here, but this works too
                telemetry[i] = rgbToDec15(raw);
            }
        }
        // Transform to USARSim coordinates
        temp = telemetry[3];
        telemetry[3] = telemetry[4];
        telemetry[4] = temp;
        telemetry[5] = -telemetry[5];
        // Up vector
        for (int i = 6; i < 9; i++) {
            for (int box = 0; box < boxesPerNum; box++) {
                sColor = img.getRGB((i * boxesPerNum + box + 1) * telemetryBoxWidth + telemetryBoxWidth/2, telemetryBoxHeight/2);
                color = new Color(sColor);
                raw[box * 3] = color.getRed();
                raw[box * 3 + 1] = color.getGreen();
                raw[box * 3 + 2] = color.getBlue();
            }
            if(useGPS) {
                telemetry[i] = rgbToDec10(raw);
            } else {
                // @todo: Should really be using rgbToDec10 here, but this works too
                telemetry[i] = rgbToDec15(raw);
            }
        }
        // Transform to USARSim coordinates
        temp = telemetry[6];
        telemetry[6] = telemetry[7];
        telemetry[7] = temp;
        telemetry[8] = -telemetry[8];
        return telemetry;
    }

    public static int[] parseImageUAVColor(BufferedImage img) {
        int sRGB;
        Color exactColor;
        // JPG encoding degrades image so our "pixels" are actually boxes of
        //  pixels - use the centermost pixel for demuxing.
        // Pixel box 1 encodes the UAV number
        sRGB = img.getRGB(telemetryBoxWidth/2, telemetryBoxHeight/2);
        exactColor = new Color(sRGB);
        int r = exactColor.getRed();
        int g = exactColor.getGreen();
        int b = exactColor.getBlue();
        return new int[] {r, g, b};
    }

    /**
     * Takes a series of 6 pixel's RGB values and decodes them to a decimal number.
     * @param raw int[] of length 18 (3 channels (RGB) times 6 pixels: rgbrgbrgbrgbrgbrgb)
     * @return the decimal value (in the range +-2^16)
     */
    public static double rgbToDec6(int[] raw) {
        int decExp = 7, decMult = -1;
        double decVal = 0;
        for (int i = 0; i < raw.length; i++) {
            for (int rgbExp = 1; rgbExp < 3; rgbExp++) {
                // We store two bits per pixel color channel
                if (raw[i] + 32 >= 255/ Math.pow(2, rgbExp)) {
                    // +32 is to center "bins"
                    // 32 = smallest non-zero pixel channel value (without compression noise)/2 = 64/2
                    // Now a pixel channel with value 64 is binned when a pixel
                    //  channel value between 32 and 96 is encountered
                    raw[i] -= 255 / Math.pow(2, rgbExp);
                    if (decExp == 16) {
                        // First box's first red bit is sign
                        decMult = 1;
                    } else {
                        decVal += Math.pow(2, decExp);
                    }
                }
                decExp--;
            }
        }
        return decVal * decMult;
    }

    /**
     * Takes a series of 6 pixel's RGB values and decodes them to a decimal number.
     * @param raw int[] of length 18 (3 channels (RGB) times 6 pixels: rgbrgbrgbrgbrgbrgb)
     * @return the decimal value (in the range +-2^10)
     */
    public static double rgbToDec10(int[] raw) {
        int decExp = 11, decMult = -1;
        double decVal = 0;
        for (int i = 0; i < raw.length; i++) {
            for (int rgbExp = 1; rgbExp < 3; rgbExp++) {
                // We store two bits per pixel color channel
                if (raw[i] + 32 >= 255/ Math.pow(2, rgbExp)) {
                    // +32 is to center "bins"
                    // 32 = smallest non-zero pixel channel value (without compression noise)/2 = 64/2
                    // Now a pixel channel with value 64 is binned when a pixel
                    //  channel value between 32 and 96 is encountered
                    raw[i] -= 255 / Math.pow(2, rgbExp);
                    if (decExp == 11) {
                        // First box's first red bit is sign
                        decMult = 1;
                    } else {
                        decVal += Math.pow(2, decExp);
                    }
                }
                decExp--;
            }
        }
        return decVal * decMult;
    }

    /**
     * Takes a series of 6 pixel's RGB values and decodes them to a decimal number.
     * @param raw int[] of length 18 (3 channels (RGB) times 6 pixels: rgbrgbrgbrgbrgbrgb)
     * @return the decimal value (in the range +-2^16)
     */
    public static double rgbToDec15(int[] raw) {
        int decExp = 16, decMult = -1;
        double decVal = 0;
        for (int i = 0; i < raw.length; i++) {
            for (int rgbExp = 1; rgbExp < 3; rgbExp++) {
                // We store two bits per pixel color channel
                if (raw[i] + 32 >= 255/ Math.pow(2, rgbExp)) {
                    // +32 is to center "bins"
                    // 32 = smallest non-zero pixel channel value (without compression noise)/2 = 64/2
                    // Now a pixel channel with value 64 is binned when a pixel
                    //  channel value between 32 and 96 is encountered
                    raw[i] -= 255 / Math.pow(2, rgbExp);
                    if (decExp == 16) {
                        // First box's first red bit is sign
                        decMult = 1;
                    } else {
                        decVal += Math.pow(2, decExp);
                    }
                }
                decExp--;
            }
        }
        return decVal * decMult;
    }

    /**
     * Takes a series of 6 pixel's RGB values and decodes them to a decimal number.
     * @param raw int[] of length 18 (3 channels (RGB) times 6 pixels: rgbrgbrgbrgbrgbrgb)
     * @return the decimal value (in the range of +-pi)
     */
    public static double rgbToRad(int[] raw) {
        int radExp = 2, radMult = 1;
        double radVal = 0;
        for (int i = 0; i < raw.length; i++) {
            for (int rgbExp = 1; rgbExp < 3 ; rgbExp++) {
                // We store two bits per pixel color channel
                if (raw[i] +32 >= 255 / Math.pow(2, rgbExp)) {
                    // +32 is to center "bins"
                    // 32 = smallest non-zero pixel channel value (without compression noise)/2 = 64/2
                    // Now a pixel channel with value 64 is binned when a pixel
                    //  channel value between 32 and 96 is encountered
                    raw[i] -= 255 / Math.pow(2, rgbExp);
                    if (radExp == 2) {
                        // First box's first red bit is sign
                        radMult = 1;
                    } else {
                        radVal += Math.pow(2, radExp);
                    }
                }
                radExp--;
            }
        }
        return radVal * radMult;
    }

    /**
     * Takes a series of 6 pixel's RGB values and decodes them to a decimal number.
     * @param raw int[] of length 18 (3 channels (RGB) times 6 pixels: rgbrgbrgbrgbrgbrgb)
     * @return the decimal value (in the range of +-2^10)
     */
    public static double rgbToGPS(int[] raw) {
        int boxIndex = 0, decExp, decMult = -1, rgbExp;
        double num1 = 0, num2 = 0, num3 = 0;

        if (raw[boxIndex] + 32 >= 255/ Math.pow(2, 1)) {
            raw[boxIndex] -= 255/ Math.pow(2, 1);
            decMult = 1;
        }
        decExp = 9;
        rgbExp = 2;
        while(decExp >= 0) {
            if(rgbExp == 3) {
                rgbExp = 1;
                boxIndex++;
            }
            if (raw[boxIndex] + 32 >= 255/ Math.pow(2, rgbExp)) {
                raw[boxIndex] -= 255 / Math.pow(2, rgbExp);
                num1 += Math.pow(2, decExp);
            }
            rgbExp++;
            decExp--;
        }
        decExp = 9;
        rgbExp = 2;
        while(decExp >= 0) {
            if(rgbExp == 3) {
                rgbExp = 1;
                boxIndex++;
            }
            if (raw[boxIndex] + 32 >= 255/ Math.pow(2, rgbExp)) {
                raw[boxIndex] -= 255 / Math.pow(2, rgbExp);
                num2 += Math.pow(2, decExp);
            }
            rgbExp++;
            decExp--;
        }
        decExp = 9;
        rgbExp = 2;
        while(decExp >= 0) {
            if(rgbExp == 3) {
                rgbExp = 1;
                boxIndex++;
            }
            if (raw[boxIndex] + 32 >= 255/ Math.pow(2, rgbExp)) {
                raw[boxIndex] -= 255 / Math.pow(2, rgbExp);
                num3 += Math.pow(2, decExp);
            }
            rgbExp++;
            decExp--;
        }
        return decMult * (num1 + num2*1e-3 + num3*1e-6);
    }

    // For debug purposes - this is performed in VBS2
    public static int[] dec15ToRGB(double _value) {
        int _r5 = 0, _g5 = 0, _b5 = 0;
        int _r4 = 0, _g4 = 0, _b4 = 0;
        int _r3 = 0, _g3 = 0, _b3 = 0;
        int _r2 = 0, _g2 = 0, _b2 = 0;
        int _r1 = 0, _g1 = 0, _b1 = 0;
        int _r0 = 0, _g0 = 0, _b0 = 0;

        if (_value >= 0) {
            _r5 = _r5 + 127;
        } else {
            _value = _value * -1;
        }
        if (_value >= Math.pow(2, 15)) {
            _r5 = _r5 + 63;
            _value = _value - Math.pow(2, 15);
        }
        if (_value >= Math.pow(2, 14)) {
            _g5 = _g5 + 127;
            _value = _value - Math.pow(2, 14);
        }
        if (_value >= Math.pow(2, 13)) {
            _g5 = _g5 + 63;
            _value = _value - Math.pow(2, 13);
        }
        if (_value >= Math.pow(2, 12)) {
            _b5 = _b5 + 127;
            _value = _value - Math.pow(2, 12);
        }
        if (_value >= Math.pow(2, 11)) {
            _b5 = _b5 + 63;
            _value = _value - Math.pow(2, 11);
        }

        if (_value >= Math.pow(2, 10)) {
            _r4 = _r4 + 127;
            _value = _value - Math.pow(2, 10);
        }
        if (_value >= Math.pow(2, 9)) {
            _r4 = _r4 + 63;
            _value = _value - Math.pow(2, 9);
        }
        if (_value >= Math.pow(2, 8)) {
            _g4 = _g4 + 127;
            _value = _value - Math.pow(2, 8);
        }
        if (_value >= Math.pow(2, 7)) {
            _g4 = _g4 + 63;
            _value = _value - Math.pow(2, 7);
        }
        if (_value >= Math.pow(2, 6)) {
            _b4 = _b4 + 127;
            _value = _value - Math.pow(2, 6);
        }
        if (_value >= Math.pow(2, 5)) {
            _b4 = _b4 + 63;
            _value = _value - Math.pow(2, 5);
        }

        if (_value >= Math.pow(2, 4)) {
            _r3 = _r3 + 127;
            _value = _value - Math.pow(2, 4);
        }
        if (_value >= Math.pow(2, 3)) {
            _r3 = _r3 + 63;
            _value = _value - Math.pow(2, 3);
        }
        if (_value >= Math.pow(2, 2)) {
            _g3 = _g3 + 127;
            _value = _value - Math.pow(2, 2);
        }
        if (_value >= Math.pow(2, 1)) {
            _g3 = _g3 + 63;
            _value = _value - Math.pow(2, 1);
        }
        if (_value >= Math.pow(2, 0)) {
            _b3 = _b3 + 127;
            _value = _value - Math.pow(2, 0);
        }
        if (_value >= Math.pow(2, -1)) {
            _b3 = _b3 + 63;
            _value = _value - Math.pow(2, -1);
        }

        if (_value >= Math.pow(2, -2)) {
            _r2 = _r2 + 127;
            _value = _value - Math.pow(2, -2);
        }
        if (_value >= Math.pow(2, -3)) {
            _r2 = _r2 + 63;
            _value = _value - Math.pow(2, -3);
        }
        if (_value >= Math.pow(2, -4)) {
            _g2 = _g2 + 127;
            _value = _value - Math.pow(2, -4);
        }
        if (_value >= Math.pow(2, -5)) {
            _g2 = _g2 + 63;
            _value = _value - Math.pow(2, -5);
        }
        if (_value >= Math.pow(2, -6)) {
            _b2 = _b2 + 127;
            _value = _value - Math.pow(2, -6);
        }
        if (_value >= Math.pow(2, -7)) {
            _b2 = _b2 + 63;
            _value = _value - Math.pow(2, -7);
        }

        if (_value >= Math.pow(2, -8)) {
            _r1 = _r1 + 127;
            _value = _value - Math.pow(2, -8);
        }
        if (_value >= Math.pow(2, -9)) {
            _r1 = _r1 + 63;
            _value = _value - Math.pow(2, -9);
        }
        if (_value >= Math.pow(2, -10)) {
            _g1 = _g1 + 127;
            _value = _value - Math.pow(2, -10);
        }
        if (_value >= Math.pow(2, -11)) {
            _g1 = _g1 + 63;
            _value = _value - Math.pow(2, -11);
        }
        if (_value >= Math.pow(2, -12)) {
            _b1 = _b1 + 127;
            _value = _value - Math.pow(2, -12);
        }
        if (_value >= Math.pow(2, -13)) {
            _b1 = _b1 + 63;
            _value = _value - Math.pow(2, -13);
        }
        
        if (_value >= Math.pow(2, -14)) {
            _r0 = _r0 + 127;
            _value = _value - Math.pow(2, -14);
        }
        if (_value >= Math.pow(2, -15)) {
            _r0 = _r0 + 63;
            _value = _value - Math.pow(2, -15);
        }
        return new int[]{_r5, _g5, _b5,_r4, _g4, _b4, _r3, _g3, _b3, _r2, _g2, _b2, _r1, _g1, _b1, _r0, _g0, _b0};
    }

    // For debug purposes - this is performed in VBS2
    public static int[] gpsToRGB(String _str) {
        boolean _pos;
        int _r5 = 0, _g5 = 0, _b5 = 0;
        int _r4 = 0, _g4 = 0, _b4 = 0;
        int _r3 = 0, _g3 = 0, _b3 = 0;
        int _r2 = 0, _g2 = 0, _b2 = 0;
        int _r1 = 0, _g1 = 0, _b1 = 0;
        int _r0 = 0, _g0 = 0, _b0 = 0;
        int _i;
        double _num1, _num2, _num3;

        //comment "51.0123456789";
        //comment "Need to split this up into +- and three 3-digit segments
        //comment "+ 055 012 345"
        //comment "This requires 1 + 10 + 10 + 10 bits, we have 36 available

        //comment "Need to find decimal place";
        _i = _str.indexOf(".") + 1;

        //comment "Get segments";
        _pos = _str.indexOf("N") != -1 || _str.indexOf("E") != -1;
        _num1 = Integer.parseInt(_str.substring(0, _i - 1));
        _num2 = Integer.parseInt(_str.substring(_i, _i + 3));
        _num3 = Integer.parseInt(_str.substring(_i + 3, _i + 6));

        if(_pos) { _r5 = _r5 + 127; }
        if(_num1 >= Math.pow(2, 9)) { _r5 = _r5 + 63; _num1 = _num1 - Math.pow(2, 9); }
        if(_num1 >= Math.pow(2, 8)) { _g5 = _g5 + 127; _num1 = _num1 - Math.pow(2, 8); }
        if(_num1 >= Math.pow(2, 7)) { _g5 = _g5 + 63; _num1 = _num1 - Math.pow(2, 7); }
        if(_num1 >= Math.pow(2, 6)) { _b5 = _b5 + 127; _num1 = _num1 - Math.pow(2, 6); }
        if(_num1 >= Math.pow(2, 5)) { _b5 = _b5 + 63; _num1 = _num1 - Math.pow(2, 5); }

        if(_num1 >= Math.pow(2, 4)) { _r4 = _r4 + 127; _num1 = _num1 - Math.pow(2, 4); }
        if(_num1 >= Math.pow(2, 3)) { _r4 = _r4 + 63; _num1 = _num1 - Math.pow(2, 3); }
        if(_num1 >= Math.pow(2, 2)) { _g4 = _g4 + 127; _num1 = _num1 - Math.pow(2, 2); }
        if(_num1 >= Math.pow(2, 1)) { _g4 = _g4 + 63; _num1 = _num1 - Math.pow(2, 1); }
        if(_num1 >= Math.pow(2, 0)) { _b4 = _b4 + 127; _num1 = _num1 - Math.pow(2, 0); }
        if(_num2 >= Math.pow(2, 9)) { _b4 = _b4 + 63; _num2 = _num2 - Math.pow(2, 9); }

        if(_num2 >= Math.pow(2, 8)) { _r3 = _r3 + 127; _num2 = _num2 - Math.pow(2, 8); }
        if(_num2 >= Math.pow(2, 7)) { _r3 = _r3 + 63; _num2 = _num2 - Math.pow(2, 7); }
        if(_num2 >= Math.pow(2, 6)) { _g3 = _g3 + 127; _num2 = _num2 - Math.pow(2, 6); }
        if(_num2 >= Math.pow(2, 5)) { _g3 = _g3 + 63; _num2 = _num2 - Math.pow(2, 5); }
        if(_num2 >= Math.pow(2, 4)) { _b3 = _b3 + 127; _num2 = _num2 - Math.pow(2, 4); }
        if(_num2 >= Math.pow(2, 3)) { _b3 = _b3 + 63; _num2 = _num2 - Math.pow(2, 3); }

        if(_num2 >= Math.pow(2, 2)) { _r2 = _r2 + 127; _num2 = _num2 - Math.pow(2, 2); }
        if(_num2 >= Math.pow(2, 1)) { _r2 = _r2 + 63; _num2 = _num2 - Math.pow(2, 1); }
        if(_num2 >= Math.pow(2, 0)) { _g2 = _g2 + 127; _num2 = _num2 - Math.pow(2, 0); }
        if(_num3 >= Math.pow(2, 9)) { _g2 = _g2 + 63; _num3 = _num3 - Math.pow(2, 9); }
        if(_num3 >= Math.pow(2, 8)) { _b2 = _b2 + 127; _num3 = _num3 - Math.pow(2, 8); }
        if(_num3 >= Math.pow(2, 7)) { _b2 = _b2 + 63; _num3 = _num3 - Math.pow(2, 7); }

        if(_num3 >= Math.pow(2, 6)) { _r1 = _r1 + 127; _num3 = _num3 - Math.pow(2, 6); }
        if(_num3 >= Math.pow(2, 5)) { _r1 = _r1 + 63; _num3 = _num3 - Math.pow(2, 5); }
        if(_num3 >= Math.pow(2, 4)) { _g1 = _g1 + 127; _num3 = _num3 - Math.pow(2, 4); }
        if(_num3 >= Math.pow(2, 3)) { _g1 = _g1 + 63; _num3 = _num3 - Math.pow(2, 3); }
        if(_num3 >= Math.pow(2, 2)) { _b1 = _b1 + 127; _num3 = _num3 - Math.pow(2, 2); }
        if(_num3 >= Math.pow(2, 1)) { _b1 = _b1 + 63; _num3 = _num3 - Math.pow(2, 1); }

        if(_num3 >= Math.pow(2, 0)) { _r0 = _r0 + 127; _num3 = _num3 - Math.pow(2, 0); }
        return new int[]{_r5, _g5, _b5,_r4, _g4, _b4, _r3, _g3, _b3, _r2, _g2, _b2, _r1, _g1, _b1, _r0, _g0, _b0};
    }

    // For debug purposes - this is performed in VBS2
    public static int[] radToRGB(double _value) {
        int _r5 = 0, _g5 = 0, _b5 = 0;
        int _r4 = 0, _g4 = 0, _b4 = 0;
        int _r3 = 0, _g3 = 0, _b3 = 0;
        int _r2 = 0, _g2 = 0, _b2 = 0;
        int _r1 = 0, _g1 = 0, _b1 = 0;
        int _r0 = 0, _g0 = 0, _b0 = 0;

        if (_value >= 0) {
            _r5 = _r5 + 127;
        } else {
            _value = _value * -1;
        }
        if (_value > Math.pow(2, 1)) {
            _r5 = _r5 + 63;
            _value = _value - Math.pow(2, 1);
        }
        if (_value > Math.pow(2, 0)) {
            _g5 = _g5 + 127;
            _value = _value - Math.pow(2, 0);
        }
        if (_value > Math.pow(2, -1)) {
            _g5 = _g5 + 63;
            _value = _value - Math.pow(2, -1);
        }
        if (_value > Math.pow(2, -2)) {
            _b5 = _b5 + 127;
            _value = _value - Math.pow(2, -2);
        }
        if (_value > Math.pow(2, -3)) {
            _b5 = _b5 + 63;
            _value = _value - Math.pow(2, -3);
        }

        if (_value > Math.pow(2, -4)) {
            _r4 = _r4 + 127;
            _value = _value - Math.pow(2, -4);
        }
        if (_value > Math.pow(2, -5)) {
            _r4 = _r4 + 63;
            _value = _value - Math.pow(2, -5);
        }
        if (_value > Math.pow(2, -6)) {
            _g4 = _g4 + 127;
            _value = _value - Math.pow(2, -6);
        }
        if (_value > Math.pow(2, -7)) {
            _g4 = _g4 + 63;
            _value = _value - Math.pow(2, -7);
        }
        if (_value > Math.pow(2, -8)) {
            _b4 = _b4 + 127;
            _value = _value - Math.pow(2, -8);
        }
        if (_value > Math.pow(2, -9)) {
            _b4 = _b4 + 63;
            _value = _value - Math.pow(2, -9);
        }

        if (_value > Math.pow(2, -10)) {
            _r3 = _r3 + 127;
            _value = _value - Math.pow(2, -10);
        }
        if (_value > Math.pow(2, -11)) {
            _r3 = _r3 + 63;
            _value = _value - Math.pow(2, -11);
        }
        if (_value > Math.pow(2, -12)) {
            _g3 = _g3 + 127;
            _value = _value - Math.pow(2, -12);
        }
        if (_value > Math.pow(2, -13)) {
            _g3 = _g3 + 63;
            _value = _value - Math.pow(2, -13);
        }
        if (_value > Math.pow(2, -14)) {
            _b3 = _b3 + 127;
            _value = _value - Math.pow(2, -14);
        }
        if (_value > Math.pow(2, -15)) {
            _b3 = _b3 + 63;
            _value = _value - Math.pow(2, -15);
        }

        if (_value > Math.pow(2, -16)) {
            _r2 = _r2 + 127;
            _value = _value - Math.pow(2, -16);
        }
        if (_value > Math.pow(2, -17)) {
            _r2 = _r2 + 63;
            _value = _value - Math.pow(2, -17);
        }
        if (_value > Math.pow(2, -18)) {
            _g2 = _g2 + 127;
            _value = _value - Math.pow(2, -18);
        }
        if (_value > Math.pow(2, -19)) {
            _g2 = _g2 + 63;
            _value = _value - Math.pow(2, -19);
        }
        if (_value > Math.pow(2, -20)) {
            _b2 = _b2 + 127;
            _value = _value - Math.pow(2, -20);
        }
        if (_value > Math.pow(2, -21)) {
            _b2 = _b2 + 63;
            _value = _value - Math.pow(2, -21);
        }

        if (_value > Math.pow(2, -22)) {
            _r1 = _r1 + 127;
            _value = _value - Math.pow(2, -22);
        }
        if (_value > Math.pow(2, -23)) {
            _r1 = _r1 + 63;
            _value = _value - Math.pow(2, -23);
        }
        if (_value > Math.pow(2, -24)) {
            _g1 = _g1 + 127;
            _value = _value - Math.pow(2, -24);
        }
        if (_value > Math.pow(2, -25)) {
            _g1 = _g1 + 63;
            _value = _value - Math.pow(2, -25);
        }
        if (_value > Math.pow(2, -26)) {
            _b1 = _b1 + 127;
            _value = _value - Math.pow(2, -26);
        }
        if (_value > Math.pow(2, -27)) {
            _b1 = _b1 + 63;
            _value = _value - Math.pow(2, -27);
        }
        
        if (_value > Math.pow(2, -28)) {
            _r0 = _r0 + 127;
            _value = _value - Math.pow(2, -28);
        }
        if (_value > Math.pow(2, -29)) {
            _r0 = _r0 + 63;
            _value = _value - Math.pow(2, -29);
        }
        return new int[]{_r5, _g5, _b5, _r4, _g4, _b4, _r3, _g3, _b3, _r2, _g2, _b2, _r1, _g1, _b1, _r0, _g0, _b0};
    }

    public static void main(String[] args) {
        System.out.println("Testing GPS conversion");
        Random random = new Random();
        double numBefore, numAfter;
        String strBefore;
        for(int i = 0; i < 10; i++) {
            numBefore = random.nextDouble() * Math.pow(10, random.nextInt(3));
            strBefore = numBefore + (random.nextBoolean() == true ? " N" : " S");
            numAfter = rgbToGPS(gpsToRGB(strBefore));
            System.out.println("numBefore: " + numBefore);
            System.out.println("strBefore: " + strBefore);
            System.out.println("numAfter: " + numAfter);
        }
    }
}
