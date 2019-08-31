/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vbs2gui.SimpleUAVSim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.border.LineBorder;
import vbs2gui.FrameManager;
import vbs2gui.sagat.panels.DialPanel;
import vbs2gui.video.ImageReplay;
import vbs2gui.video.ImageServerLink;
import vbs2gui.map.MapPanel;
import vbs2gui.Vbs2Handler;
import vbs2gui.Vbs2Scripts;
import vbs2gui.server.Vbs2Link;
import vbs2gui.video.VideoBank;
import vbs2gui.video.ImageAndTelemetry;
import vbs2gui.sagat.SagatMission;

/**
 *
 * @author pscerri
 */
public class Main {
    // Number of UAVs to be spawned on the server

    public static int NUM_UAVS = 11;
    public static int NUM_UGVS = 8;
    public static int NUM_ANIMALS_PER_HERD = 0;
    public static int NUM_HERDS = 0;
    public static int NUM_ANIMALS = NUM_HERDS * NUM_ANIMALS_PER_HERD;
    final static int COMMAND_SLEEP = 1000;
    // Respawn all UAVs, groups and cameras? Otherwise use what was spawned last
    //  time
    final boolean RESPAWN = true;
    // In the case of multiple VBS2 machines, vbs2Hosts[0] must be the game
    //  server
    static String[] vbs2Hosts;
    static int[] vbs2Ports;
    // Give fuel to the UAV?
    public static boolean[] uavFuel = {true};
    public final static int UAV_FLY_IN_HEIGHT = 200; // in m
    public final static int UGV_SPEED = 30; // in m/s
    public static double[] vbs2Origin;
    Vbs2Link[] links = null;
    UAV[] uavs = null;
    DialPanel[] dialPanels = null;
    FrameManager frameManager;
    JFrame mapFrame, suaveFrame;
    JFrame[] dialFrames = null;
    String response;
    // Actual UAV colors that the VBS2 script is using - these are not what
    //  vbs2gui tries to match to though, we round down each RGB channel value
    //  to the nearest multiple of 64 to compensate for JPG compression. So the
    //  RGB values used in VBS2 should be 64 * x + 32, where x = {0, 1, 2, 3} to
    //  avoid edge cases
    public static Color[] actualUavColors = {
        new Color(32, 32, 32),
        new Color(32, 32, 96),
        new Color(32, 96, 32),
        new Color(32, 96, 96),
        new Color(96, 32, 32),
        new Color(96, 32, 96),
        new Color(96, 96, 32),
        new Color(96, 96, 96),
        new Color(160, 160, 160),
        new Color(160, 160, 224),
        new Color(160, 224, 160),
        new Color(160, 224, 224),
        new Color(224, 160, 160),
        new Color(224, 160, 224),
        new Color(224, 224, 160),
        new Color(224, 224, 224),
        new Color(32, 32, 160),
        new Color(32, 160, 32),
        new Color(32, 160, 160),
        new Color(160, 32, 32),
        new Color(96, 96, 224),
        new Color(96, 224, 96),
        new Color(96, 224, 224),
        new Color(224, 96, 96)};
    // UAV names used in the below hashtables for consistency
    public static String[] uavNames = {
        "PLANE0", "PLANE1", "PLANE2", "PLANE3", "PLANE4", "PLANE5", "PLANE6", "PLANE7", "PLANE8", "PLANE9", "PLANE10", "PLANE11", "PLANE12", "PLANE13", "PLANE14", "PLANE15", "PLANE16", "PLANE17", "PLANE18", "PLANE19", "PLANE20", "PLANE21", "PLANE22", "PLANE23"};
    // This is ugly but is the quicket way to go from the rounded UAV indicator
    //  pixel RGB value to UAV index in our arrays (VideoPanels, DialPanels, etc)
    public static final Hashtable<Color, Integer> roundedColorToUavIndex = new Hashtable<Color, Integer>();

    static {
        roundedColorToUavIndex.put(new Color(0, 0, 0), new Integer(0));
        roundedColorToUavIndex.put(new Color(0, 0, 64), new Integer(1));
        roundedColorToUavIndex.put(new Color(0, 64, 0), new Integer(2));
        roundedColorToUavIndex.put(new Color(0, 64, 64), new Integer(3));
        roundedColorToUavIndex.put(new Color(64, 0, 0), new Integer(4));
        roundedColorToUavIndex.put(new Color(64, 0, 64), new Integer(5));
        roundedColorToUavIndex.put(new Color(64, 64, 0), new Integer(6));
        roundedColorToUavIndex.put(new Color(64, 64, 64), new Integer(7));
        roundedColorToUavIndex.put(new Color(128, 128, 128), new Integer(8));
        roundedColorToUavIndex.put(new Color(128, 128, 192), new Integer(9));
        roundedColorToUavIndex.put(new Color(128, 192, 128), new Integer(10));
        roundedColorToUavIndex.put(new Color(128, 192, 192), new Integer(11));
        roundedColorToUavIndex.put(new Color(192, 128, 128), new Integer(12));
        roundedColorToUavIndex.put(new Color(192, 128, 192), new Integer(13));
        roundedColorToUavIndex.put(new Color(192, 192, 128), new Integer(14));
        roundedColorToUavIndex.put(new Color(192, 192, 192), new Integer(15));
        roundedColorToUavIndex.put(new Color(0, 0, 128), new Integer(16));
        roundedColorToUavIndex.put(new Color(0, 128, 0), new Integer(17));
        roundedColorToUavIndex.put(new Color(0, 128, 128), new Integer(18));
        roundedColorToUavIndex.put(new Color(128, 0, 0), new Integer(19));
        roundedColorToUavIndex.put(new Color(64, 64, 192), new Integer(20));
        roundedColorToUavIndex.put(new Color(64, 192, 64), new Integer(21));
        roundedColorToUavIndex.put(new Color(64, 192, 192), new Integer(22));
        roundedColorToUavIndex.put(new Color(192, 64, 64), new Integer(23));
    }
    // This is ugly but is the quicket way to go from the rounded UAV indicator
    //  pixel RGB value to UAV name, which is needed for some last minute code
    //  additions
    public static final Hashtable<Color, String> roundedColorToUavName = new Hashtable<Color, String>();

    static {
        roundedColorToUavName.put(new Color(0, 0, 0), "PLANE0");
        roundedColorToUavName.put(new Color(0, 0, 64), "PLANE1");
        roundedColorToUavName.put(new Color(0, 64, 0), "PLANE2");
        roundedColorToUavName.put(new Color(0, 64, 64), "PLANE3");
        roundedColorToUavName.put(new Color(64, 0, 0), "PLANE4");
        roundedColorToUavName.put(new Color(64, 0, 64), "PLANE5");
        roundedColorToUavName.put(new Color(64, 64, 0), "PLANE6");
        roundedColorToUavName.put(new Color(64, 64, 64), "PLANE7");
        roundedColorToUavName.put(new Color(128, 128, 128), "PLANE8");
        roundedColorToUavName.put(new Color(128, 128, 192), "PLANE9");
        roundedColorToUavName.put(new Color(128, 192, 128), "PLANE10");
        roundedColorToUavName.put(new Color(128, 192, 192), "PLANE11");
        roundedColorToUavName.put(new Color(192, 128, 128), "PLANE12");
        roundedColorToUavName.put(new Color(192, 128, 192), "PLANE13");
        roundedColorToUavName.put(new Color(192, 192, 128), "PLANE14");
        roundedColorToUavName.put(new Color(192, 192, 192), "PLANE15");
        roundedColorToUavName.put(new Color(0, 0, 128), "PLANE16");
        roundedColorToUavName.put(new Color(0, 128, 0), "PLANE17");
        roundedColorToUavName.put(new Color(0, 128, 128), "PLANE18");
        roundedColorToUavName.put(new Color(128, 0, 0), "PLANE19");
        roundedColorToUavName.put(new Color(64, 64, 192), "PLANE20");
        roundedColorToUavName.put(new Color(64, 192, 64), "PLANE21");
        roundedColorToUavName.put(new Color(64, 192, 192), "PLANE22");
        roundedColorToUavName.put(new Color(192, 64, 64), "PLANE23");
    }
    static int[][][] herd_waypoints;
    static boolean[][] danger_zones;
    // UAV loiter waypoint: x center (m), y center (m), radius (min is ~300m)
    static int[][] uav_loiter = {
        {1755, 2513, 1100},
        {2155, 2563, 1100},
        {2155, 2113, 1100},
        {1755, 2513, 1100},
        {2155, 2563, 1100},
        {2155, 2113, 1100},
        {1755, 2513, 1100},
        {2155, 2563, 1100},
        {2155, 2113, 1100},
        {1755, 2513, 1100},
        {2155, 2563, 1100},
        {2155, 2113, 1100},
        {1755, 2513, 1100},
        {2155, 2563, 1100},
        {2155, 2113, 1100},
        {1755, 2513, 1100},
        {2155, 2563, 1100},
        {2155, 2113, 1100},
        {1755, 2513, 1100},
        {2155, 2563, 1100},
        {2155, 2113, 1100},
        {1755, 2513, 1100},
        {2155, 2563, 1100},
        {2155, 2113, 1100},};
    // UAV start position: x (m), y (m), heading (deg clockwise from North - 
    //  VBS2 convention)
    // Start heading is not used because VBS2 has no reliable method that I know
    //  of for changing the heading of a moving vehicle. So instead I played
    //  around with the offset from the center location so that once the UAVs
    //  get turned around they will be roughly evenly spaced around the loiter
    //  waypoint that is assigned
    static int[][] uav_start = {
        {1755 - 1100, 2513 - 1100, 0},
        {2155 - 1100, 2563 - 1100, 0},
        {2155 - 1100, 2113 - 1100, 0},
        {1755 - 1100 + 550, 2513 + 1100, 90},
        {2155 - 1100 + 550, 2563 + 1100, 90},
        {2155 - 1100 + 550, 2113 + 1100, 90},
        {1755 + 1100, 2513 + 1100 - 250, 180},
        {2155 + 1100, 2563 + 1100 - 250, 180},
        {2155 + 1100, 2113 + 1100 - 250, 180},
        {1755 + 1100, 2513 - 1100, 270},
        {2155 + 1100, 2563 - 1100, 270},
        {2155 + 1100, 2113 - 1100, 270},
        {1755 - 1100, 2513, 45},
        {2155 - 1100, 2563, 45},
        {2155 - 1100, 2113, 45},
        {1755, 2513 + 1100, 135},
        {2155, 2563 + 1100, 135},
        {2155, 2113 + 1100, 135},
        {1755 + 1100, 2513, 225},
        {2155 + 1100, 2563, 225},
        {2155 + 1100, 2113, 225},
        {1755, 2513 - 1100, 315},
        {2155, 2563 - 1100, 315},
        {2155, 2113 - 1100, 315},};
    static int waypointSet = 1;
    // Herd move waypoint: x, y, forceSpeed (in m/s)
    static int[][][] SUAVE_herd_waypoints1 = {
        {{650, 1000, UGV_SPEED}, {583, 2159, UGV_SPEED}, {1086, 2579, UGV_SPEED}, {595, 3562, UGV_SPEED}},
        {{590, 863, UGV_SPEED}, {1299, 533, UGV_SPEED}, {2454, 483, UGV_SPEED}, {3146, 1086, UGV_SPEED}},
        {{1284, 3094, UGV_SPEED}, {1863, 3561, UGV_SPEED}, {2422, 3027, UGV_SPEED}, {1863, 3561, UGV_SPEED}},
        {{3284, 1033, UGV_SPEED}, {3279, 1691, UGV_SPEED}, {2726, 2143, UGV_SPEED}, {2726, 2897, UGV_SPEED}},
        {{3072, 2245, UGV_SPEED}, {3212, 1597, UGV_SPEED}, {3206, 1000, UGV_SPEED}, {2576, 1126, UGV_SPEED}},
        {{3454, 3810, UGV_SPEED}, {3365, 3291, UGV_SPEED}, {2855, 3434, UGV_SPEED}, {3365, 3291, UGV_SPEED}},
        {{2372, 3026, UGV_SPEED}, {2409, 2505, UGV_SPEED}, {1855, 1328, UGV_SPEED}, {2138, 400, UGV_SPEED}},
        {{544, 4039, UGV_SPEED}, {907, 3313, UGV_SPEED}, {1605, 2839, UGV_SPEED}, {1175, 1678, UGV_SPEED}}};
    static int[][][] SUAVE_herd_waypoints2 = {
        {{3258, 1375, UGV_SPEED}, {3280, 1962, UGV_SPEED}, {2906, 2393, UGV_SPEED}, {3353, 2744, UGV_SPEED}},
        {{1187, 3528, UGV_SPEED}, {1154, 3068, UGV_SPEED}, {1289, 2548, UGV_SPEED}, {1754, 2912, UGV_SPEED}},
        {{1711, 2645, UGV_SPEED}, {1307, 2342, UGV_SPEED}, {782, 2571, UGV_SPEED}, {583, 2158, UGV_SPEED}},
        {{3197, 1896, UGV_SPEED}, {2826, 1575, UGV_SPEED}, {2869, 1216, UGV_SPEED}, {2575, 1126, UGV_SPEED}},
        {{714, 3022, UGV_SPEED}, {584, 2598, UGV_SPEED}, {583, 2158, UGV_SPEED}, {969, 1629, UGV_SPEED}},
        {{2266, 3317, UGV_SPEED}, {2520, 3611, UGV_SPEED}, {2855, 3434, UGV_SPEED}, {3425, 3308, UGV_SPEED}},
        {{1468, 1624, UGV_SPEED}, {1882, 1399, UGV_SPEED}, {1095, 1555, UGV_SPEED}, {1380, 2221, UGV_SPEED}},
        {{1793, 3955, UGV_SPEED}, {1863, 3561, UGV_SPEED}, {2468, 3002, UGV_SPEED}, {2686, 2461, UGV_SPEED}}};
    static int[][][] SUAVE_herd_waypoints_warmup = {
        {{595, 3562, UGV_SPEED}, {1086, 2579, UGV_SPEED}, {583, 2159, UGV_SPEED}, {650, 1000, UGV_SPEED}},
        {{3146, 1086, UGV_SPEED}, {2454, 483, UGV_SPEED}, {1299, 533, UGV_SPEED}, {590, 863, UGV_SPEED}},
        {{1863, 3561, UGV_SPEED}, {2422, 3027, UGV_SPEED}, {1863, 3561, UGV_SPEED}, {1284, 3094, UGV_SPEED}},
        {{2726, 2897, UGV_SPEED}, {2726, 2143, UGV_SPEED}, {3279, 1691, UGV_SPEED}, {3284, 1033, UGV_SPEED}},
        {{969, 1629, UGV_SPEED}, {583, 2158, UGV_SPEED}, {584, 2598, UGV_SPEED}, {714, 3022, UGV_SPEED}},
        {{3425, 3308, UGV_SPEED}, {2855, 3434, UGV_SPEED}, {2520, 3611, UGV_SPEED}, {2266, 3317, UGV_SPEED}},
        {{1380, 2221, UGV_SPEED}, {1095, 1555, UGV_SPEED}, {1882, 1399, UGV_SPEED}, {1468, 1624, UGV_SPEED}},
        {{2686, 2461, UGV_SPEED}, {2468, 3002, UGV_SPEED}, {1863, 3561, UGV_SPEED}, {1793, 3955, UGV_SPEED}}};
    // Danger zones: PARKING, FOREST1, FOREST2, HOUSING, AIRPORT
    static boolean[][] SUAVE_danger_zones1 = {
        {true, true, false, false, false},
        {false, false, true, true, false},
        {false, true, false, false, true}};
    static boolean[][] SUAVE_danger_zones2 = {
        {false, true, false, true, false},
        {true, false, true, false, false},
        {true, false, false, false, true}};
    // This will never happen, so warmup logs will never have a SAGAT pause
    //  trigger
    static boolean[][] SUAVE_danger_zones_warmup = {
        {true, true, true, true, true},
        {true, true, true, true, true},
        {true, true, true, true, true}};
    static int[][][] SUAVE_herd_waypoints3 = SUAVE_herd_waypoints1;
    static boolean[][] SUAVE_danger_zones3 = SUAVE_danger_zones1;
    public static int timePerRound = 5 * 60000; // ms
    public static int numRoundsPerMission = 3;
    SagatMission sagatMission;
    // GUI
    // Will image telemetry be in GPS lat/lon or local XY?
    public static final boolean USE_GPS = true;
    static boolean showVideo = true;
    double trackUpdateInterval = 0.25; // How often to update positions of assets on the 2D map
    MapPanel mapPanel;
    VideoBank videoBank;
    // Warminster settings
    public static final double[] MAP_BOUNDS = {0, 5000.0, 5000.0, 0};
    public static final String AERIAL_MAP = "src/vbs2gui/icons/warminster-danger-small.jpg";
    static Properties properties = null;
    static long uavPositionUpdateDelay = 50;
    long[] pauseTimes;
    public static String baseFolder;
    public static String replayLogFile = "run/logs/";
    BlockingQueue<ImageAndTelemetry> inQueue;
    ExperimentType experimentType;
    public static GUIType guiType;

    public enum ExperimentType {

        FEED_3_WARMUP, FEED_11_WARMUP, FEED_22_WARMUP, SUAVE_3_WARMUP, SUAVE_11_WARMUP, SUAVE_22_WARMUP, FEED_3_1, FEED_3_2, FEED_11_1, FEED_11_2, FEED_22_1, FEED_22_2, SUAVE_3_1, SUAVE_3_2, SUAVE_11_1, SUAVE_11_2, SUAVE_22_1, SUAVE_22_2;
    };

    public enum GUIType {

        FEED, QUEUE, SUAVE, VIDEO_TEST, RECORD;
    };

    public enum WPType {

        MOVE, LOITER
    };

    public static void main(String[] args) {
        if (args.length == 0) {
            baseFolder = "";
            // This is for recording from a single VBS2 machine using the first
            //  set of waypoints
//            new Main(1, null, null, GUIType.RECORD, new String[] {"192.168.1.100"}, new int[] {5003}, null, null);
            // This is for recording from a VBS2 host machine (128.2.177.187) 
            //  and a VBS2 client machine (128.2.177.183) using the first set of 
            //  waypoints
//            new Main(1, null, null, GUIType.RECORD, new String[] {"128.2.177.187", "128.2.177.183"}, new int[] {5003, 5003}, null, null);

//            baseFolder = "/home/usar/Programming/vbs2gui/";
//            new Main(new LinkedBlockingDeque<ImageAndTelemetry>(), null, ExperimentType.SUAVE_11_1, baseFolder);
        } else {
            ExperimentType experimentType = null;
            for (int loopi = 0; loopi < args.length; loopi++) {
                if (args[loopi].equalsIgnoreCase("--exp") && (loopi + 1) < args.length) {
                    experimentType = ExperimentType.valueOf(args[++loopi]);
                    System.out.println("experimentType is: " + experimentType);
                } else if (args[loopi].equalsIgnoreCase("--folder") && (loopi + 1) < args.length) {
                    baseFolder = args[++loopi];
                    System.out.println("baseFolder is: " + baseFolder);
                }
            }
            if (experimentType != null) {
                new Main(new LinkedBlockingDeque<ImageAndTelemetry>(), null, experimentType, baseFolder);
            }
        }
    }

    public Main(BlockingQueue<ImageAndTelemetry> imgQueue, JFrame suaveFrame, ExperimentType experimentType, String baseFolder) {
        // For running experiment from log file
        this.inQueue = imgQueue;
        this.suaveFrame = suaveFrame;
        this.experimentType = experimentType;
        this.baseFolder = baseFolder;
        replayLogFile = baseFolder + "run/logs/";
        guiType = null;
        pauseTimes = null;
        String[] questions;
        if (experimentType == ExperimentType.FEED_3_WARMUP) {
            guiType = GUIType.FEED;
            pauseTimes = null;
            replayLogFile += "vbs2_suave_canned_feed_WARMUP.binlog";
            NUM_UAVS = 3;
            waypointSet = 3;
        } else if (experimentType == ExperimentType.FEED_11_WARMUP) {
            guiType = GUIType.FEED;
            pauseTimes = null;
            replayLogFile += "vbs2_11-3.binlog";
            NUM_UAVS = 11;
            waypointSet = 3;
        } else if (experimentType == ExperimentType.FEED_22_WARMUP) {
            guiType = GUIType.FEED;
            pauseTimes = null;
            replayLogFile += "vbs2_22-3.binlog";
            NUM_UAVS = 22;
            waypointSet = 3;
        } else if (experimentType == ExperimentType.SUAVE_3_WARMUP) {
            guiType = GUIType.SUAVE;
            pauseTimes = null;
            replayLogFile += "vbs2_suave_canned_feed_WARMUP.binlog";
            NUM_UAVS = 3;
            waypointSet = 3;
        } else if (experimentType == ExperimentType.SUAVE_11_WARMUP) {
            guiType = GUIType.SUAVE;
            pauseTimes = null;
            replayLogFile += "vbs2_11-3.binlog";
            NUM_UAVS = 11;
            waypointSet = 3;
        } else if (experimentType == ExperimentType.SUAVE_22_WARMUP) {
            guiType = GUIType.SUAVE;
            pauseTimes = null;
            replayLogFile += "vbs2_22-3.binlog";
            NUM_UAVS = 2;
            waypointSet = 3;
        } else if (experimentType == ExperimentType.FEED_3_1) {
            guiType = GUIType.FEED;
//            pauseTimes = new long[]{1297784723200L, 1297785257201L, 1297785557202L}; // Starts SAGAT 6 seconds into run for debugging purposes
            pauseTimes = new long[]{1297784957201L, 1297785257201L, 1297785557202L};
            replayLogFile += "vbs2_suave_canned_feed_1.binlog";
            NUM_UAVS = 3;
            waypointSet = 1;
        } else if (experimentType == ExperimentType.FEED_3_2) {
            guiType = GUIType.FEED;
            pauseTimes = new long[]{1297799209355L, 1297799449356L, 1297799689357L};
            replayLogFile += "vbs2_suave_canned_feed_2.binlog";
            NUM_UAVS = 3;
            waypointSet = 2;
        } else if (experimentType == ExperimentType.FEED_11_1) {
            guiType = GUIType.FEED;
//            pauseTimes = new long[]{1300912395053L, 1300912695053L, 1300912995054L};
            pauseTimes = new long[]{1301105717727L, 1301106017728L, 1301106317728L};
            replayLogFile += "vbs2_11-1.binlog";
            NUM_UAVS = 11;
            waypointSet = 1;
        } else if (experimentType == ExperimentType.FEED_11_2) {
            guiType = GUIType.FEED;
//            pauseTimes = new long[]{1300908798503L, 1300909038504L, 1300909278505L};
            pauseTimes = new long[]{1301105877231L, 1301106117231L, 1301106357232L};
            replayLogFile += "vbs2_11-2.binlog";
            NUM_UAVS = 11;
            waypointSet = 2;
        } else if (experimentType == ExperimentType.FEED_22_1) {
            guiType = GUIType.FEED;
//            pauseTimes = new long[]{1300926211780L, 1300926511781L, 1300926811782L};
//            pauseTimes = new long[]{1301116602821L, 1301116902822L, 1301117202822L};
            pauseTimes = null;
            replayLogFile += "vbs2_22-1.binlog";
            NUM_UAVS = 22;
            waypointSet = 1;
        } else if (experimentType == ExperimentType.FEED_22_2) {
            guiType = GUIType.FEED;
//            pauseTimes = new long[]{1300927890101L, 1300928130102L, 1300927890101L};
//            pauseTimes = new long[]{1301119069994L, 1301119309995L, 1301119549998L};
            pauseTimes = null;
            replayLogFile += "vbs2_22-2.binlog";
            NUM_UAVS = 22;
            waypointSet = 2;
        } else if (experimentType == ExperimentType.SUAVE_3_1) {
            guiType = GUIType.SUAVE;
//            pauseTimes = new long[]{1297784723200L, 1297785257201L, 1297785557202L}; // Starts SAGAT 6 seconds into run for debugging purposes
            pauseTimes = new long[]{1297784957201L, 1297785257201L, 1297785557202L};
            replayLogFile += "vbs2_suave_canned_feed_1.binlog";
            NUM_UAVS = 3;
            waypointSet = 1;
        } else if (experimentType == ExperimentType.SUAVE_3_2) {
            guiType = GUIType.SUAVE;
            pauseTimes = new long[]{1297799209355L, 1297799449356L, 1297799689357L};
//            replayLogFile += "vbs2_suave_canned_feed_2.binlog";
            replayLogFile += "vbs2_11-2.binlog";
            NUM_UAVS = 3;
            waypointSet = 2;
        } else if (experimentType == ExperimentType.SUAVE_11_1) {
            guiType = GUIType.SUAVE;
//            pauseTimes = new long[]{1300912395053L, 1300912695053L, 1300912995054L};
//            pauseTimes = new long[]{1301105717727L, 1301106017728L, 1301106317728L};
            pauseTimes = null;
            replayLogFile += "vbs2_11-1.binlog";
            NUM_UAVS = 11;
            waypointSet = 1;
        } else if (experimentType == ExperimentType.SUAVE_11_2) {
            guiType = GUIType.SUAVE;
//            pauseTimes = new long[]{1300908798503L, 1300909038504L, 1300909278505L};
//            pauseTimes = new long[]{1301105877231L, 1301106117231L, 1301106357232L};
            pauseTimes = null;
            replayLogFile += "vbs2_11-2.binlog";
            NUM_UAVS = 11;
            waypointSet = 2;
        } else if (experimentType == ExperimentType.SUAVE_22_1) {
            guiType = GUIType.SUAVE;
//            pauseTimes = new long[]{1300926211780L, 1300926511781L, 1300926811782L};
//            pauseTimes = new long[]{1301116602821L, 1301116902822L, 1301117202822L};
            pauseTimes = null;
            replayLogFile += "vbs2_22-1.binlog";
            NUM_UAVS = 22;
            waypointSet = 1;
        } else if (experimentType == ExperimentType.SUAVE_22_2) {
            guiType = GUIType.SUAVE;
//            pauseTimes = new long[]{1300927890101L, 1300928130102L, 1300927890101L};
//            pauseTimes = new long[]{1301119069994L, 1301119309995L, 1301119549998L};
            pauseTimes = null;
            replayLogFile += "vbs2_22-2.binlog";
            NUM_UAVS = 22;
            waypointSet = 2;
        }

        missionInit();
        if (null != replayLogFile) {
            if (null == this.inQueue) {
                inQueue = new LinkedBlockingQueue<ImageAndTelemetry>();
            }
        }
        if (guiType != GUIType.SUAVE) {
            Logger logger = Logger.getLogger("");
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
                String logFileName = "VBS2GUI_USER_LOG_" + sdf.format(new Date()) + ".log";
                FileHandler fh = new FileHandler(logFileName);
                fh.setFormatter(new vbs2gui.DataLogFormatter());
                logger.addHandler(fh);
                logger.setLevel(Level.INFO);
            } catch (IOException e) {
            }
            logger.info("experimentType is " + experimentType);
            logger.info("replayLogFile is " + replayLogFile);
            logger.info("guiType is " + guiType);
            logger.info("NUM_UAVS is " + NUM_UAVS);
            logger.info("waypointSet is " + waypointSet);
            logger.info("STARTING VBS2GUI RUN");
        }
        guiInit();
        videoInit();
    }

    public Main(int waypointSet, BlockingQueue<ImageAndTelemetry> imgQueue, JFrame suaveFrame, GUIType guiType, String[] vbs2Host, int[] vbs2Port, String replayLogFile, long[] pauseTimes) {
        // For running from VBS2 server or log file
        this.waypointSet = waypointSet;
        this.inQueue = imgQueue;
        this.suaveFrame = suaveFrame;
        this.guiType = guiType;
        this.vbs2Hosts = vbs2Host;
        this.vbs2Ports = vbs2Port;
        this.replayLogFile = replayLogFile;
        this.pauseTimes = pauseTimes;
        missionInit();
        if (null != replayLogFile) {
            if (null == this.inQueue) {
                inQueue = new LinkedBlockingQueue<ImageAndTelemetry>();
            }
        }
        if (guiType != GUIType.SUAVE) {
            Logger logger = Logger.getLogger("");
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
                String logFileName = "VBS2GUI_USER_LOG_" + sdf.format(new Date()) + ".log";
                FileHandler fh = new FileHandler(logFileName);
                fh.setFormatter(new vbs2gui.DataLogFormatter());
                logger.addHandler(fh);
                logger.setLevel(Level.INFO);
            } catch (IOException e) {
            }
            logger.info("experimentType is " + experimentType);
            logger.info("guiType is " + guiType);
            logger.info("replayLogFile is " + replayLogFile);
            logger.info("waypointSet is " + waypointSet);
            logger.info("STARTING VBS2GUI RUN");
        }
        if (replayLogFile == null) {
            serverInit();
        }
        guiInit();
        videoInit();
    }

    public void missionInit() {
        // Choose which set of mission waypoints to use
        switch (waypointSet) {
            case 1:
                herd_waypoints = SUAVE_herd_waypoints1;
                danger_zones = SUAVE_danger_zones1;
                break;
            case 2:
                herd_waypoints = SUAVE_herd_waypoints2;
                danger_zones = SUAVE_danger_zones2;
                break;
            case 3:
                herd_waypoints = SUAVE_herd_waypoints_warmup;
                danger_zones = SUAVE_danger_zones_warmup;
                break;
            default:
                System.out.println("Invalid waypoint set!");
                System.exit(0);
                break;
        }
    }

    private void serverInit() {
        int num;
        Vbs2Link serverLink;

        if (vbs2Hosts == null || vbs2Hosts.length == 0) {
            System.err.println("Error: No VBS2 host specified.");
            System.exit(0);
        }
        links = new Vbs2Link[vbs2Hosts.length];
        for (int i = 0; i < links.length; i++) {
            System.out.println("*************\n Linking to: " + vbs2Hosts[i] + "\n*****************");
            links[i] = new Vbs2Link();
            links[i].connect(vbs2Hosts[i]);
        }
        serverLink = links[0];

        // uavs[] setup
        uavs = new UAV[NUM_UAVS + NUM_ANIMALS + NUM_UGVS];
        // UAVs
        for (int i = 0; i < NUM_UAVS; i++) {
            uavs[i] = new UAV(serverLink, i, "uav" + i, "group" + i, UAV.Type.UAV, vbs2Hosts[0], "vbs2_us_scaneagle2");
        }
        // UGVs
        num = NUM_UAVS;
        for (int i = 0; i < NUM_UGVS; i++) {
            uavs[num] = new UAV(serverLink, num, "uav" + num, "group" + num, UAV.Type.UGV, vbs2Hosts[0], "VBS2_IQ_Civil_2624_Dumptruck");
//            System.out.println("uavs["+num+"]: " + uavs[num].getUnitName() + ", " + uavs[num].getUavName());
            num++;
        }
        // Animals
        num = NUM_UAVS + NUM_UGVS;
        for (int i = 0; i < NUM_HERDS; i++) {
            for (int j = 0; j < NUM_ANIMALS_PER_HERD; j++) {
                uavs[num] = new UAV(serverLink, num, "uav" + num, "group" + (NUM_UAVS + NUM_UGVS + i), UAV.Type.ANIMAL, vbs2Hosts[0], "vbs2_animal_camel_lightbrown_none");
                num++;
            }
        }

        // Start position and waypoint setup
        for (int i = 0; i < NUM_UAVS; i++) {
            uavs[i].setStartPosition(uav_start[i][0], uav_start[i][1], UAV_FLY_IN_HEIGHT, uav_start[i][2]);
        }
        num = NUM_UAVS;
        for (int i = 0; i < NUM_UGVS; i++) {
            uavs[num].setStartPosition(
                    herd_waypoints[i][0][0],
                    herd_waypoints[i][0][1],
                    herd_waypoints[i][0][2],
                    0);
            num++;
        }
        num = NUM_UAVS + NUM_UGVS;
        for (int i = 0; i < NUM_HERDS; i++) {
            for (int j = 0; j < NUM_ANIMALS_PER_HERD; j++) {
                uavs[num].setStartPosition(
                        herd_waypoints[i][0][0],
                        herd_waypoints[i][0][1],
                        herd_waypoints[i][0][2],
                        0);
                num++;
            }
        }

        // VBS2 asset creation
        vbsConnect();
        // Add in position update scripts for the UAVS
        System.out.println("createTrackingCmds");
        createTracking();
    }

    public void guiInit() {
        // Start waypoints and SAGAT interrupts
        Vbs2Link serverLink = null;
        if (links != null && links.length > 0) {
            serverLink = links[0];
        }
        sagatMission = new SagatMission(serverLink, waypointSet, danger_zones, uav_loiter, herd_waypoints, uavs, experimentType);
        if (guiType == GUIType.FEED) {
            createFrameManager();
            // UAV panels
            createUAVPanels();
            for (JFrame frame : dialFrames) {
                sagatMission.addSagatListener(frame);
            }
            for (DialPanel dialPanel : dialPanels) {
                sagatMission.addSagatDialListener(dialPanel);
            }
            // 2d map
            create2dMap(MAP_BOUNDS, AERIAL_MAP, uavs);
            sagatMission.addSagatListener(mapFrame);
            // Video bank
            videoBank = new VideoBank(NUM_UAVS, sagatMission, mapPanel, inQueue);
            // Connect map and SAGAT to VBS2
            Vbs2Handler vbs2Handler = new Vbs2Handler(mapPanel, sagatMission);
            if (serverLink != null) {
                serverLink.addMessageListener(vbs2Handler);
            }

            if (replayLogFile != null) {
                new Thread() {

                    public void run() {
                        runFromQueue(inQueue);
                    }
                }.start();
            }
        } else if (guiType == GUIType.SUAVE) {
            createFrameManager();
            // UAV panels
            createUAVPanels();
            for (JFrame frame : dialFrames) {
                sagatMission.addSagatListener(frame);
            }
            sagatMission.addSagatListener(suaveFrame);
            for (DialPanel dialPanel : dialPanels) {
                sagatMission.addSagatDialListener(dialPanel);
            }
            System.out.println("guiInit: suaveFrame is " + suaveFrame);
            // Video bank
//            videoBank = new VideoBank(NUM_UAVS, sagatMission, mapPanel, inQueue);
            // Connect SAGAT to VBS2
            Vbs2Handler vbs2Handler = new Vbs2Handler(sagatMission);
            if (serverLink != null) {
                serverLink.addMessageListener(vbs2Handler);
            }
        } else if (guiType == GUIType.VIDEO_TEST) {
            createFrameManager();
            // Video bank
            videoBank = new VideoBank(NUM_UAVS, sagatMission, mapPanel, inQueue);
            // Connect SAGAT to VBS2
            Vbs2Handler vbs2Handler = new Vbs2Handler(sagatMission);
            if (serverLink != null) {
                serverLink.addMessageListener(vbs2Handler);
            }
        } else if (guiType == GUIType.RECORD) {
            createFrameManager();
            // Connect SAGAT to VBS2
            Vbs2Handler vbs2Handler = new Vbs2Handler(sagatMission);
            if (serverLink != null) {
                serverLink.addMessageListener(vbs2Handler);
            }
        }
    }

    public void videoInit() {
        if (replayLogFile == null) {
            // Set up image server
            System.out.println("imageServerLink");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_hh_mm");
            String logFileName = ImageServerLink.LOG_TO_CANNED_DATA ? ImageServerLink.CANNED_DATA_FILENAME_PREFIX + sdf.format(new Date()) + ".binlog" : null;
            DataOutputStream logStream = null;
            try {
                logStream = new DataOutputStream(new FileOutputStream(logFileName));
                System.err.println("ImageServerLink constructor: open canned data logfile filename=" + logFileName);
            } catch (java.io.FileNotFoundException e) {
                System.err.println("ImageServerLink constructor: Exception trying to open canned data logfile filename= " + logFileName);
                System.err.println("ImageServerLink constructor: Exception = " + e);
                e.printStackTrace();
                System.err.println("ImageServerLink constructor: Ignoring exception and continuing.");
            }

            ImageServerLink[] imageServerLinks = new ImageServerLink[vbs2Hosts.length];
            for (int i = 0; i < vbs2Hosts.length; i++) {
                imageServerLinks[i] = new ImageServerLink(vbs2Hosts[i], vbs2Ports[i], videoBank, inQueue, logStream);
                imageServerLinks[i].connect();
            }
            // Run mission (handles setting new waypoints to assets)
            sagatMission.run();
        } else {
            System.out.println("imageReplay");
            ImageReplay imageReplay = new ImageReplay(replayLogFile, inQueue, pauseTimes, sagatMission);
            imageReplay.start();
        }
    }

    private void create2dMap(double[] bounds, String aerialMap, UAV[] uavs) {
        // Bounds consisis of VBS2 coordiantes for
        //  [ x-left, x-right, y-top, y-bottom ]
        // aerialMap is file name plus extension of file in the src/icons folder
        mapPanel = new MapPanel();
        mapPanel.setPreferredSize(new Dimension(600, 600));
        mapPanel.setBorder(new LineBorder(Color.BLACK));
        mapFrame = new JFrame();
        mapFrame.getContentPane().add(mapPanel, BorderLayout.CENTER);
        mapFrame.pack();
        mapFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mapFrame.setVisible(true);

        if (bounds.length != 4) {
            System.err.print("Argument bounds is of length " + bounds.length
                    + "; should be length 4.");
            return;
        }
        try {
            File imgFile = new File(baseFolder + AERIAL_MAP);
            BufferedImage img = ImageIO.read(imgFile);
            mapPanel.setMapImage(img);
            mapPanel.setMapRect(bounds[0], bounds[1], bounds[2], bounds[3]); // Warminster bounds
            mapPanel.repaint();
        } catch (IOException e) {
            System.err.println("Failed to load aerial map at: " + (baseFolder + AERIAL_MAP));
        }
        while (mapPanel.isPaintingTile()) {
            try {
                Thread.sleep(COMMAND_SLEEP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createFrameManager() {
        frameManager = new FrameManager();
//        FrameManager.restoreLayout();
        frameManager.setVisible(true);
    }

    private void createTracking() {
        String[] cmds;
        cmds = Vbs2Scripts.createTrackingCmds(uavs, NUM_UAVS, NUM_UGVS, NUM_ANIMALS, trackUpdateInterval);
        for (String s : cmds) {
            response = links[0].evaluate(s);
        }
    }

    private void createUAVPanels() {
        dialPanels = new DialPanel[NUM_UAVS];
        dialFrames = new JFrame[NUM_UAVS];
        for (int i = 0; i < NUM_UAVS; i++) {
            dialPanels[i] = new DialPanel();
            dialPanels[i].setPreferredSize(new java.awt.Dimension(650, 250));
            dialPanels[i].setSize(new java.awt.Dimension(650, 250));
            dialFrames[i] = new JFrame(uavNames[i] + " Panel");
            dialFrames[i].getContentPane().setLayout(new BorderLayout());
            dialFrames[i].pack();
            dialFrames[i].getContentPane().add(dialPanels[i]);
            dialFrames[i].setPreferredSize(new java.awt.Dimension(670, 270));
            dialFrames[i].setSize(new java.awt.Dimension(670, 270));
            dialFrames[i].setVisible(true);
            dialPanels[i].setOwner(uavNames[i]);
            dialPanels[i].setAndRecordBackground(actualUavColors[i]);
        }
        for (JFrame f : dialFrames) {
            f.setVisible(true);
        }
    }

    private void vbsConnect() {
        Vbs2Link serverLink = links[0];
        String[] cmds;
        System.out.println("Starting");

        // Terminate 2D map's tracking scripts (server only)
        System.out.println("Delete scripts and dialogue");
        Vbs2Scripts.deleteScripts(serverLink);
        // Terminate mux scripts (all VBS2 machines)
        for (Vbs2Link link : links) {
            Vbs2Scripts.deleteLoops(link);
        }
        // Improve weather, camera settings
        System.out.println("Increase visibility");
        for (Vbs2Link link : links) {
            Vbs2Scripts.createVisibility(link);
        }

        if (RESPAWN) {
            System.out.println("Doing respawn");
            // Delete UAVs, goats, groups, cameras
            // @todo: The cameras are created on ALL VBS2 machines since they
            //  are strictly local, so we should delete them from each machine
            //  as well. But this means making the VBS2 numUAVs variable global,
            //  etc
            Vbs2Scripts.deleteAssets(serverLink);
            // Update number of UAVs and goats
            // @todo: Make this global
            serverLink.evaluate("numUAVs = " + NUM_UAVS + "; ");
            serverLink.evaluate("numUGVs = " + NUM_UGVS + "; ");
            serverLink.evaluate("numAnimals = " + NUM_ANIMALS + "; ");
            serverLink.evaluate("numHerds = " + NUM_HERDS + "; ");
            // Eject player from camera
            serverLink.evaluate("player action [\"eject\", vehicle player]; ");
            // Create groups
            System.out.println("createGroupCmds");
            cmds = Vbs2Scripts.createGroupCmds(uavs, NUM_ANIMALS_PER_HERD);
            for (String s : cmds) {
                serverLink.evaluate(s);
            }
            // Wait for groups to be created before assigning vehicles to them
            try {
                Thread.sleep(COMMAND_SLEEP);
            } catch (InterruptedException ie) {
            }
            // Create units
            System.out.println("createUAVCmds");
            for (int i = 0; i < uavs.length; i++) {
                double[] startPosition = uavs[i].getStartPosition();
                cmds = Vbs2Scripts.createUAVCmds(uavs, i, uavs[i].getUnitName(), startPosition[0], startPosition[1], startPosition[2], startPosition[3]);
                for (String s : cmds) {
                    serverLink.evaluate(s);
                }
            }
            // Wait for groups to be created before joining
            try {
                Thread.sleep(COMMAND_SLEEP);
            } catch (InterruptedException ie) {
            }
            // Make global references to the UAV vehicle objects. createUnit
            //  objects have global references, but createVehicle objects do
            //  not, so we must do this
            System.out.println("createGlobalRefCmds");
            cmds = Vbs2Scripts.createGlobalRefCmds(uavs, NUM_UAVS);
            for (String s : cmds) {
                serverLink.evaluate(s);
            }
            // Join groups
            cmds = Vbs2Scripts.createJoinCmds(uavs, NUM_UAVS);
            for (String s : cmds) {
                serverLink.evaluate(s);
            }
            // Create cameras we will record from
            // camCreate calls are strictly local and must be performed on the
            //  machine that will use the camera
            System.out.println("createCamCmds");
            for (int i = 0; i < NUM_UAVS; i++) {
                double fov = (guiType == GUIType.SUAVE ? 0.7 : 0.7);
                double offset = (guiType == GUIType.SUAVE ? -5 : -5);
                cmds = Vbs2Scripts.createCamCmds(uavs, i, offset, fov);
                // I am sick of dealing with this cryptic local/global stuff,
                //  I'm just creating all the cameras in every VBS2 instance
                for (Vbs2Link link : links) {
                    for (String s : cmds) {
                        link.evaluate(s);
                    }
                }
            }
        }
        // Add a new waypoint so the UAVs will listen to our waypoint edit commands
//        System.out.println("Add waypoint");
//        for (int i = 0; i < uavs.length;) {
//            link.evaluate(uavs[i].getGroupName() + " addWaypoint [getPos " + uavs[i].getUavName() + ", 5]; ");
//            link.evaluate(uavs[i].getGroupName() + " setCurrentWaypoint [" + uavs[i].getGroupName() + ", (nWaypoints " + uavs[i].getGroupName() + ") - 1]; ");
//            if(uavs[i].getType() != UAV.Type.ANIMAL) {
//                i++;
//            } else {
//                i+=NUM_ANIMALS_PER_HERD;
//            }
//        }
        serverLink.evaluate("Revive player; ");
        // Create MUX dialog boxes, compile scripts, run switching/telemetry
        // If we have multiple machines, we can split the list of units we want
        //  to mux between the machines to increase the framerate. (That was the
        //  motivation for originally introducing multiple machine support into
        //  this project)
        int uavsSoFar = 0;
        for (int i = 0; i < links.length; i++) {
            System.out.println("createLoopCmds for " + vbs2Hosts[i]);
            if (i < links.length - 1) {
                cmds = Vbs2Scripts.createLoopCmds(uavs, (int) (NUM_UAVS / links.length), uavsSoFar, USE_GPS, false);
            } else {
                cmds = Vbs2Scripts.createLoopCmds(uavs, NUM_UAVS - uavsSoFar, uavsSoFar, USE_GPS, false);
            }
            for (String s : cmds) {
                links[i].evaluate(s);
            }
            uavsSoFar += (int) (NUM_UAVS / links.length);
        }
    }

    public void runFromQueue(BlockingQueue<ImageAndTelemetry> inQueue) {
        while (true) {
            ImageAndTelemetry imgAndTelem = null;
            try {
//                Debug.debug(1, "VBS2Sim.run: read thread calling inQueue.take()");
                imgAndTelem = inQueue.take();
//                Debug.debug(1, "VBS2Sim.run: read thread returned from calling inQueue.take()");
            } catch (InterruptedException e) {
            }
            if (null == imgAndTelem) {
//                Debug.debug(1, "VBS2Sim.run: read thread msg is null, skipping");
                continue;
            }
//            Debug.debug(1, "VBS2Sim.run: inQueue size= " + inQueue.size() + ", image is " + imgAndTelem.img.getWidth() + " by " + imgAndTelem.img.getHeight());
            // We have 9 doubles;
            //
            // Longitude
            // Latitude
            // ASL position (above sea level)
            // x direction
            // y direction
            // z direction
            // x up
            // y up
            // z up
//            Debug.debug(1, "Received uav telem for uav " + imgAndTelem.uavid + ", lon/lat/alt = " + imgAndTelem.telem[VBS2GUI_LON] + " " + imgAndTelem.telem[VBS2GUI_LAT] + " " + imgAndTelem.telem[VBS2GUI_ALT]);
            videoBank.handleImage(imgAndTelem.img);
        }
    }
}
