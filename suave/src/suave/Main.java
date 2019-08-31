package suave;

import java.io.IOException;
import javax.swing.JOptionPane;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main {
    // @TODO: Public here so we can mark them in VBS2Sim
    //
    // from 'shifted' warminster xyz, and then adjusted visually to match the terrain map

    public static double[][] corners = {
        {-2.1805467606 + .00110, 51.2522087097 - .0015, 102.57}, // nw
        {-2.1813411713 + .00175, 51.2074661255 - .0013, 126.13}, // sw
        {-2.1101191044 + .00050, 51.2076110840 - .0015, 193.89} // se
    };
    private final static DecimalFormat fmt = new DecimalFormat("0.000");
    private final static DecimalFormat fmt2 = new DecimalFormat("0.000000");
    private final static double GASCOLA_FLYBOX_DEFAULT_ORIGIN_LAT = 40.4563444444;
    private final static double GASCOLA_FLYBOX_DEFAULT_ORIGIN_LON = -79.789486111;
    private final static double GASCOLA_FLYBOX_DEFAULT_ORIGIN_ALT = 0;
    private final static int SA_DIALS_HEIGHT = 200;
    private static JFrame myFrame;
    static JFrame glDisplayFrame;
    private static GLDisplay glDisplay = null;

    public static void dealWithExceptions(Exception e) {
        Debug.debug(5, "Exception creating program, e=" + e);
        e.printStackTrace();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        pw.close();
        JOptionPane.showMessageDialog(myFrame, "An exception occurred while running the program, the error is printed below\n" + e.toString() + "\n" + sw.toString());
    }

    public static void main(String[] args) {

        String demFilename = null;
        String textureFilename = null;
        String textureWorldFilename = null;
        String skyTextureFilename = null;
        String groundTextureFilename = null;

        String uavCameraLogFilename = null;

        String uavModelFilename = null;

        String skyboxFilenames[] = new String[5];

        // our default origin point for gascola, basically the lower
        // left corner of a 1 km square that covers (fairly well) our
        // typical flying area.
        double originLat = GASCOLA_FLYBOX_DEFAULT_ORIGIN_LAT;
        double originLon = GASCOLA_FLYBOX_DEFAULT_ORIGIN_LON;
        double originAlt = GASCOLA_FLYBOX_DEFAULT_ORIGIN_ALT;

        String loadSerialDemAndTexture = null;
        String saveSerialDemAndTexture = null;

        float meshStep = 25.0f;

        String imagedir = "";

        int timeCodeFrameNumber = 2055;
        long timeCodeMs = 1224695053010L;

//        String vclogfilename = "F:\\laptop\\owens\\suave\\telemetry_UAV5_2008_10_22_00.m";
        String vclogfilename = null;
        long vclogstartat = 6000000;

        // we want to start at logline 4801, which is when it first
        // starts heading straight south after circling up.  In epoch
        // time that logline is 1224695588845, so that minus the
        // timecode;
        //
        // 1224695588845  - 1224695053010 = 535835
        long startAtTimeMs = timeCodeMs + 535835;

        boolean startSanjayaListener = true;
        String multicastAddr = "239.192.0.14";
        int sanjayaListenerPort = 5544;
        boolean clientMode = false;
        boolean serverMode = false;
        String serverName = "localhost";
        int serverPort = 6655;
        boolean videoMode = false;
        boolean drawSanjayaUAVs = true;
        boolean generateVideoStills = false;
        double texNorthLat = 40.465863888888;
        double texSouthLat = 40.451205555555;
        double texWestLon = -79.796372222221;
        double texEastLon = -79.779224999999;

        boolean useVBS2link = true;
        String vbs2ImageReplayFilename = null;
        vbs2gui.SimpleUAVSim.Main.ExperimentType vbs2ExperimentType = null;
        String vbs2guiFolder = null;
        // @TODO: The current version of the vbs2gui code doesn't use this parameter, it's hardcoded.
        String vbs2Hostname = "192.168.1.100";
        int vbs2Port = 5003;

        boolean superUser = false; // if true, enables 'dangerous' key commands - i.e. anything we don't want happening during experiments with users
        boolean addSADials = false;  // if true, add the vbs2gui 'dials' that Shafiq wrote, to help test user situational awareness

        for (int loopi = 0; loopi < args.length; loopi++) {
            if (args[loopi].equalsIgnoreCase("--dem") && (loopi + 1) < args.length) {
                demFilename = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--texture") && (loopi + 1) < args.length) {
                textureFilename = args[++loopi];
                if (textureFilename.equalsIgnoreCase("--linetexture")) {
                    textureFilename = null;
                }
            } else if (args[loopi].equalsIgnoreCase("--worldfile") && (loopi + 1) < args.length) {
                textureWorldFilename = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--sky") && (loopi + 1) < args.length) {
                skyTextureFilename = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--ground") && (loopi + 1) < args.length) {
                groundTextureFilename = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--uavlog") && (loopi + 1) < args.length) {
                uavCameraLogFilename = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--uavmodel") && (loopi + 1) < args.length) {
                uavModelFilename = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--originlat") && (loopi + 1) < args.length) {
                originLat = Double.parseDouble(args[++loopi]);
            } else if (args[loopi].equalsIgnoreCase("--originlon") && (loopi + 1) < args.length) {
                originLat = Double.parseDouble(args[++loopi]);
            } else if (args[loopi].equalsIgnoreCase("--originalt") && (loopi + 1) < args.length) {
                originLat = Double.parseDouble(args[++loopi]);
            } else if (args[loopi].equalsIgnoreCase("--skyfront") && (loopi + 1) < args.length) {
                skyboxFilenames[0] = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--skyback") && (loopi + 1) < args.length) {
                skyboxFilenames[1] = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--skyleft") && (loopi + 1) < args.length) {
                skyboxFilenames[2] = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--skyright") && (loopi + 1) < args.length) {
                skyboxFilenames[3] = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--skytop") && (loopi + 1) < args.length) {
                skyboxFilenames[4] = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--loadserialdemAndTexture") && (loopi + 1) < args.length) {
                loadSerialDemAndTexture = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--saveserialdemAndTexture") && (loopi + 1) < args.length) {
                saveSerialDemAndTexture = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--meshstep") && (loopi + 1) < args.length) {
                meshStep = Float.parseFloat(args[++loopi]);
            } else if (args[loopi].equalsIgnoreCase("--imagedir") && (loopi + 1) < args.length) {
                imagedir = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--timeCodeFrameNumber") && (loopi + 1) < args.length) {
                timeCodeFrameNumber = Integer.parseInt(args[++loopi]);
            } else if (args[loopi].equalsIgnoreCase("--timeCodeMs") && (loopi + 1) < args.length) {
                timeCodeMs = Long.parseLong(args[++loopi]);
            } else if (args[loopi].equalsIgnoreCase("--startAtTimeMs") && (loopi + 1) < args.length) {
                startAtTimeMs = Long.parseLong(args[++loopi]);
            } else if (args[loopi].equalsIgnoreCase("--vclog") && (loopi + 1) < args.length) {
                vclogfilename = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--vclogstartat") && (loopi + 1) < args.length) {
                vclogstartat = Long.parseLong(args[++loopi]);
            } else if (args[loopi].equalsIgnoreCase("--servermode")) {
                serverMode = true;
                videoMode = true;     // servermode implies video generation mode;
            } else if (args[loopi].equalsIgnoreCase("--videomode")) {
                videoMode = true;
            } else if (args[loopi].equalsIgnoreCase("--clientmode")) {
                clientMode = true;
            } else if (args[loopi].equalsIgnoreCase("--dontdrawsanjayauavs")) {
                drawSanjayaUAVs = false;
            } else if (args[loopi].equalsIgnoreCase("--generateVideoStills")) {
                generateVideoStills = true;
            } else if (args[loopi].equalsIgnoreCase("--servername") && (loopi + 1) < args.length) {
                serverName = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--serverport") && (loopi + 1) < args.length) {
                serverPort = Integer.parseInt(args[++loopi]);
            } else if (args[loopi].equalsIgnoreCase("--texNorthLat") && (loopi + 1) < args.length) {
                texNorthLat = Double.parseDouble(args[++loopi]);
            } else if (args[loopi].equalsIgnoreCase("--texSouthLat") && (loopi + 1) < args.length) {
                texSouthLat = Double.parseDouble(args[++loopi]);
            } else if (args[loopi].equalsIgnoreCase("--texWestLon") && (loopi + 1) < args.length) {
                texWestLon = Double.parseDouble(args[++loopi]);
            } else if (args[loopi].equalsIgnoreCase("--texEastLon") && (loopi + 1) < args.length) {
                texEastLon = Double.parseDouble(args[++loopi]);
            } else if (args[loopi].equalsIgnoreCase("--superuser")) {
                superUser = true;
            } else if (args[loopi].equalsIgnoreCase("--addsadials")) {
                addSADials = true;
            } else if (args[loopi].equalsIgnoreCase("--vbs2replaylog") && (loopi + 1) < args.length) {
                vbs2ImageReplayFilename = args[++loopi];
            } else if (args[loopi].equalsIgnoreCase("--exp") && (loopi + 1) < args.length) {
                vbs2ExperimentType = vbs2gui.SimpleUAVSim.Main.ExperimentType.valueOf(args[++loopi]);
            } else if (args[loopi].equalsIgnoreCase("--folder") && (loopi + 1) < args.length) {
                vbs2guiFolder = args[++loopi];
            } else {
                System.err.println("Usage: Main [--dem dem.tif] [--texture texture.jpg] [--sky skytexture.jpg] [--ground groundtexture.jpg] [--originlat nnn.nnn] [--originlon nnn.nnn] [--originalt nnn.nnn]");
            }
        }

        Debug.debug(1, "Main.main: After args processing;");
        Debug.debug(1, "Main.main:     dem       = '" + demFilename + "'");
        Debug.debug(1, "Main.main:     texture   = '" + textureFilename + "'");
        Debug.debug(1, "Main.main:     worldfile   = '" + textureWorldFilename + "'");
        Debug.debug(1, "Main.main:     sky       = '" + skyTextureFilename + "'");
        Debug.debug(1, "Main.main:     ground    = '" + groundTextureFilename + "'");
        Debug.debug(1, "Main.main:     originlat = '" + originLat + "'");
        Debug.debug(1, "Main.main:     originlon = '" + originLon + "'");
        Debug.debug(1, "Main.main:     originalt = '" + originAlt + "'");
        Debug.debug(1, "Main.main:     skyfront = '" + skyboxFilenames[0] + "'");
        Debug.debug(1, "Main.main:     skyback = '" + skyboxFilenames[1] + "'");
        Debug.debug(1, "Main.main:     skyleft = '" + skyboxFilenames[2] + "'");
        Debug.debug(1, "Main.main:     skyright = '" + skyboxFilenames[3] + "'");
        Debug.debug(1, "Main.main:     skytop = '" + skyboxFilenames[4] + "'");
        Debug.debug(1, "Main.main:     uavlog = '" + uavCameraLogFilename + "'");
        Debug.debug(1, "Main.main:     uavmodel = '" + uavModelFilename + "'");
        Debug.debug(1, "Main.main:     meshStep = '" + meshStep + "'");
        Debug.debug(1, "Main.main:     imagedir = '" + imagedir + "'");
        Debug.debug(1, "Main.main:     vclogfilename = '" + vclogfilename + "'");
        Debug.debug(1, "Main.main:     vclogstartat = '" + vclogstartat + "'");
        Debug.debug(1, "Main.main:     serverMode = '" + serverMode + "'");
        Debug.debug(1, "Main.main:     clientMode = '" + clientMode + "'");
        Debug.debug(1, "Main.main:     serverName = '" + serverName + "'");
        Debug.debug(1, "Main.main:     serverPort = '" + serverPort + "'");
        Debug.debug(1, "Main.main:     dontdrawSanjayaUAVs = '" + !drawSanjayaUAVs + "'");
        Debug.debug(1, "Main.main:     generateVideoStills = '" + generateVideoStills + "'");
        Debug.debug(1, "Main.main:     texNorthLat = '" + texNorthLat + "'");
        Debug.debug(1, "Main.main:     texSouthLat = '" + texSouthLat + "'");
        Debug.debug(1, "Main.main:     texWestLon = '" + texWestLon + "'");
        Debug.debug(1, "Main.main:     texEastLon = '" + texEastLon + "'");
        Debug.debug(1, "Main.main:     superuser = '" + superUser + "'");
        Debug.debug(1, "Main.main:     addsadials = '" + addSADials + "'");
        Debug.debug(1, "Main.main:     vbs2replaylog = '" + vbs2ImageReplayFilename + "'");
        Debug.debug(1, "Main.main:     vbs2ExperimentType = '" + vbs2ExperimentType + "'");
        Debug.debug(1, "Main.main:     vbs2guiFolder = '" + vbs2guiFolder + "'");


        Logger logger = Logger.getLogger("");
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
            String logFileName = "SUAVE_USER_LOG_" + sdf.format(new Date()) + ".log";
            FileHandler fh = new FileHandler(logFileName);
            fh.setFormatter(new vbs2gui.DataLogFormatter());
            logger.addHandler(fh);
            logger.setLevel(Level.INFO);
        } catch (IOException e) {
        }
        logger = Logger.getLogger(Main.class.getName());
        logger.info("STARTING SUAVE RUN");
        // [ [0 0 0 12 15 22] 0 -126.666641 -2.350000 0.171000 -0.264000 0.092000 0.015000 0 0 1.3 12.4 141 9 0 100.000031 14.000000 0.000000 -0.379000 0.000000 0.000000 -0.050000 0.028571 0.000000 0.000000 0 0 0.092000 0.000000 560.475525 4096 0.000000 0.519000 0.450000 332.166718 0.977000 40.461918 -79.784279 40.460812 -79.783882 0 0 0.000000 0.000000 0 0 0.000000 828 0.000000 0.000000 10 [08 10 22 16 15 24] 16 0 37.857143 560.455383 0.000030 0.000000 0.000000 0.012500 203.166702 0.000000 0.000000];...
        String displayName = "SUAVE";
        if (serverMode) {
            displayName += "-server";
        } else if (clientMode) {
            displayName += "-client";
        }

        JPanel dialPanel = null;
        // @TODO: Nathan already added other code to instantiate dials... might want to
        // switch to this later, but for now don't use it.
        addSADials = false;
        if (addSADials) {
//            JPanel uav1 = DialPanel.makeUavDialSubpanel("UAV 1", Color.RED);
//            JPanel uav2 = DialPanel.makeUavDialSubpanel("UAV 2", Color.GREEN);
//            JPanel uav3 = DialPanel.makeUavDialSubpanel("UAV 3", Color.BLUE);
//
//            dialPanel = new JPanel(new BorderLayout());
//            dialPanel.setBackground(Color.black);
//            dialPanel.add(uav1, BorderLayout.WEST);
//            dialPanel.add(uav2, BorderLayout.CENTER);
//            dialPanel.add(uav3, BorderLayout.EAST);
        }

        glDisplay = GLDisplay.createGLDisplay(displayName, videoMode, dialPanel);
        while (glDisplayFrame == null) {
            glDisplayFrame = glDisplay.getFrame();
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        originLat = 51.2074661255;
//        originLon = -2.1813411713;
//        originLat = 51.230094909667969;
//        originLon = -2.145133495330811;

//        originLat = 51.2286033630;
//        originLon = -2.1451613903;
//        originAlt = 194.362;

        // 2011/1/29 - after loading Nathans newest LLA file;
//        1 : DEMFactor.loadLla: Average of the four corners in lat,lon,alt = 51.22784423828125, -2.1578927040100098, 154.04800415039062
        originLat = 51.22784423828125;
        originLon = -2.1578927040100098;
        originAlt = 154.04;

        //        texNorthLat = 51.252342224121094;
//        texSouthLat = 51.207466125488281;
//        texEastLon = -2.109255790710449;
//        texWestLon = -2.181341171264648;

        // New tex coords from geotag-locations-small.txt for warminster-aerial-small-geotag.jpg
//        texNorthLat = 51.2505798340;
//        texWestLon = -2.1932995319;
//        texSouthLat = 51.2051124573;
//        texEastLon = -2.122520685;

        // From above, after hand adjusting...
//         public static double[][] corners = {
//        {-2.1805467606  + .00110, 51.2522087097 - .0015, 102.57},         // nw
//        {-2.1813411713  + .00175, 51.2074661255 - .0013, 126.13},         // sw
//        {-2.1101191044  + .00050, 51.2076110840 - .0015, 193.89}          // se
//    };

        // junk!
        //        texNorthLat = 51.2505798340;
//        texWestLon = -2.1932995319;
//        texSouthLat = 51.2051124573;
//        texEastLon = -2.122520685;

        // nathan's new values 2011/01/17
        texNorthLat = 51.250;
        texSouthLat = 51.207;
        texEastLon = -2.111;
        texWestLon = -2.180;

//        // @TODO: Just experimenting/testing world file and affine transforms - these values
//        // for 'world file' were generated for the warminster map, using TransGen.exe and
//        // control points generated by Nathan from VBS2.  They really should be loaded from
//        // a .tfw or.jfw file.
//        //
//        // > g:\suave\trunk\warminster>g:\suave\transgen
//        // > g:\suave\transgen
//        // > Missing option gcps2wld:
//        // > Usage:
//        // >  --input <path> --similar --skew <skew> --phi <rotation> --s --g
//        // > gcps2wld:
//        // >   --input path      File with GCPs
//        // >   --similar         Choose to get similar transformation (skew = 0, scale equal
//        // >                     for both axis). If --similar is choosen --skew and --phi
//        // >                     parameters are ignored
//        // >   --skew skew       Sets exlipcitly the value of skew parameter
//        // >   --phi rotation    Sets exlipcitly the value of rotation parameter (in
//        // >                     radians)
//        // >   --s               Choose to get detail error statistics
//        // >   --g               Choose to get geometric coeficients
//        // >
//        // > g:\suave\trunk\warminster>g:\suave\transgen --input warminster-aerial-small-geotag_2048.gcps
//        // > g:\suave\transgen --input warminster-aerial-small-geotag_2048.gcps
//        // > -0.00000025565
//        // > 0.00003581222
//        // > -0.00002248147
//        // > -0.00000042804
//        // > 51.25166295572
//        // > -2.19322959953
//        // >
//        // >
//        // > g:\suave\trunk\warminster>
//        double worldA = -0.00000025565;
//        double worldD = 0.00003581222;
//        double worldB = -0.00002248147;
//        double worldE = -0.00000042804;
//        double worldC = 51.25166295572;
//        double worldF = -2.19322959953;
//        WorldFile worldFile = new WorldFile(worldA, worldD, worldB, worldE, worldC, worldF);
//
//        double[] pixels = new double[2];
//        double[] coords = new double[2];
//        pixels[0] = 0;
//        pixels[1] = 0;
//        worldFile.toCoords(pixels, coords);
//        Debug.debug(1, "Main.main: pixels=" + fmt2.format(pixels[0]) + ", " + fmt2.format(pixels[1]) + ", coords = " + fmt2.format(coords[0]) + ", " + fmt2.format(coords[1]));
//        pixels[0] = 4096;
//        pixels[1] = 4096;
//        worldFile.toCoords(pixels, coords);
//        Debug.debug(1, "Main.main: pixels=" + fmt2.format(pixels[0]) + ", " + fmt2.format(pixels[1]) + ", coords = " + fmt2.format(coords[0]) + ", " + fmt2.format(coords[1]));
//        pixels[0] = 4096;
//        pixels[1] = 0;
//        worldFile.toCoords(pixels, coords);
//        Debug.debug(1, "Main.main: pixels=" + fmt2.format(pixels[0]) + ", " + fmt2.format(pixels[1]) + ", coords = " + fmt2.format(coords[0]) + ", " + fmt2.format(coords[1]));
//        pixels[0] = 0;
//        pixels[1] = 4096;
//        worldFile.toCoords(pixels, coords);
//        Debug.debug(1, "Main.main: pixels=" + fmt2.format(pixels[0]) + ", " + fmt2.format(pixels[1]) + ", coords = " + fmt2.format(coords[0]) + ", " + fmt2.format(coords[1]));

        // output of camera calibration, for VSB2 camera;
        //  Calibration results after optimization (with uncertainties):
        //
        //  Focal Length:          fc = [ 451.93246   453.89293 ] +/- [ 9.01896   6.63878 ]
        //  Principal point:       cc = [ 322.64415   247.73978 ] +/- [ 6.44784  14.23368 ]
        //  Skew:             alpha_c = [ 0.00000 ] +/- [ 0.00000  ]   =    //  angle of pixel axes = 90.00000 +/- 0.00000 degrees
        //  Distortion:            kc = [ -0.00529   0.15478   0.00005   0.00252  0.00000 ] +/- [ 0.03040   0.17018   0.00347   0.00546  0.00000 ]
        //  Pixel error:          err = [ 0.24842   0.09187 ]
        //
        //  Note: The numerical errors are approximately three times the standard
        //  deviations (for reference).
        //
        //
        //  Recommendation: Some distortion coefficients are found equal to zero (within
        //  their uncertainties).
        //                  To reject them from the optimization set
        //  est_dist=[0;1;0;0;0] and run Calibration
        //
        //      //     //  KK
        //
        //  KK =
        //
        //    451.9325         0  322.6441
        //           0  453.8929  247.7398
        //           0         0    1.0000
//    double[] fc = {451.93246, 453.89293};
//    double[] cc = {322.64415, 247.73978};
//    double alpha_c = 0.0;
//    double[] kc = {-0.00529, 0.15478, 0.00005, 0.00252, 0.00000};
//    double[][] kkparams = {
//        {451.9325, 0, 322.6441},
//        {0, 453.8929, 247.7398},
//        {0, 0, 1.0000}
//    };
        // > From: Nathan Brooks <nbbrooks@gmail.com>
        // > To: owens@cs.cmu.edu
        // > Subject: Re: VBS2 camera calibration
        // > Date: Tue, 25 Jan 2011 01:49:31 -0500
        // >
        // > VBS2 has the default camera FOV set to 0.7, so fc = [640*0.7; 480*0.7] = [448; 336]. So KK is
        // >
        // >
        // > [448, 0, 320
        // >
        // > 0, 336, 240,
        // >
        // > 0, 0, 1]
        // >
        // > kc will be all zeros.
        //
        // This is the "IDEAL" calibration matrix;  from the Matlat camera calibration parameters  page;
        //
        // http://www.vision.caltech.edu/bouguetj/calib_doc/htmls/parameters.html
        //
        //       |  fc(1)    alpha_c*fc(1)  cc(1)  |
        //       |                                 |
        // KK =  |   0          fc(2)       cc(2)  |
        //       |                                 |
        //       |   0            0          1     |
        //
        // so
        //
        // fc = { 448, 336 }
        // cc = { 320, 240 }
        // alpha_c = 0
        // kc = all zeros!
//        double[] fc = {448, 336};
//        double[] cc = {320, 240};
//        double alpha_c = 0.0;
//        double[] kc = {0.0, 0.0, 0.0, 0.0, 0.0};
//        double[][] kkparams = {
//            {448, 0, 320},
//            {0, 336, 240},
//            {0, 0, 1.0000}
//        };

        // 'ideal' matrix from Pras
        double[] fc = {457.8, 457.8};
        double[] cc = {320, 240};
        double alpha_c = 0.0;
        double[] kc = {0.0, 0.0, 0.0, 0.0, 0.0};
        double[][] kkparams = {
            {457.8, 0, 320},
            {0, 457.8, 240},
            {0, 0, 1.0000}
        };

//
//    // Based on using  link.evaluate("cameraOrthography [true, 240]; "); in vbs2Gui.Main to change VBS camera calibration
//    double[] fc = {240, 180};
//    double[] cc = {120, 90};
//    double alpha_c = 0.0;
//    double[] kc = {0.0, 0.0, 0.0, 0.0, 0.0};
//    double[][] kkparams = {
//        {240, 0, 120},
//        {0, 180, 90},
//        {0, 0, 1.0000}
//    };

        // @TODO: See comments in CameraCalibration, using the hardcoded ideal matrix from Pras and the no arg
        // constructor, everything works amazingly well, using the args constructor everything goes wrong.  Fix later.
        //
        //        CameraCalibration cameraCalibration = new CameraCalibration(fc, kc, alpha_c, kc);
        CameraCalibration cameraCalibration = new CameraCalibration();

        ArrayList<VirtualCockpitLogLine> vclogList = null;

        try {
            Origin origin = new Origin(originLat, originLon, originAlt);
            if (null != vclogfilename) {
                vclogList = VirtualCockpitLogLine.parseFile(vclogfilename);
            }
            WorldFile worldFile = new WorldFile(textureWorldFilename);

            GeoTexture terrainTexture = null;
            if (null != textureFilename) {
                terrainTexture = GeoTextureFactory.loadFile(textureFilename, origin, texNorthLat, texSouthLat, texEastLon, texWestLon, worldFile);
//                terrainTexture = GeoTextureFactory.loadFile(textureFilename, origin, corners[0], corners[1], corners[2]);
            } else {
                terrainTexture = GeoTextureFactory.createLineGridTexture(origin, texNorthLat, texSouthLat, texEastLon, texWestLon);
            }
//            demFilename = "warminster/heightmap_2010_09_23_012103_shifted.xyz";
            DEM dem = null;
            if (demFilename.endsWith(".tif") || demFilename.endsWith(".TIF")) {
                dem = DEMFactory.loadTif(demFilename, (int) meshStep, origin, DEMFactory.TIF_HOLE_MARKER_ALTTITUDE, DEM.HOLE_MARKER_ALTITUDE);
            } else if (demFilename.endsWith(".zzz") || demFilename.endsWith(".ZZZ")) {
                dem = DEMFactory.loadZZZ(demFilename, origin);
            } else if (demFilename.endsWith(".xyz") || demFilename.endsWith(".XYZ")) {
                dem = DEMFactory.loadXyz(demFilename, origin, (int) meshStep);
            } else if (demFilename.endsWith(".lla") || demFilename.endsWith(".LLA")) {
                Debug.debug(1, "Main.main: Loading Lat/lon/alt file = " + demFilename);
                dem = DEMFactory.loadLla(demFilename, origin, (int) meshStep);
            }
            Mesh mesh = new Mesh(terrainTexture, dem);
            mesh.start();
            UAVCamera uavCamera = new UAVCamera(uavModelFilename);
            Lights lights = new Lights();
            GLCamera userViewCamera = new GLCamera();
//            userViewCamera.xPos = -1410.975f;
//            userViewCamera.yPos = 594.721f;
//            userViewCamera.zPos = -824.058f;
//            userViewCamera.xRot = -339.000f;
//            userViewCamera.yRot = 0.340f;
//            userViewCamera.zRot = 0.000f;
            userViewCamera.xPos = -965.686f;
            userViewCamera.yPos = 853.882f;
            userViewCamera.zPos = 2300.720f;
            userViewCamera.xRot = -339.000f;
            userViewCamera.yRot = 0.340f;
            userViewCamera.zRot = 0.000f;
            StateDB stateDB = new StateDB();
            TextureDB textureDB = new TextureDB();
            SkyAndGround skyAndGround = new SkyAndGround(skyTextureFilename, groundTextureFilename, textureDB);
            Skybox skybox = new Skybox(skyboxFilenames, textureDB);
            Model model = new Model(stateDB, textureDB, skyAndGround, dem, mesh, uavCamera, skybox, drawSanjayaUAVs);
            Baker baker = new Baker(userViewCamera, model, stateDB);
            GeneralMouseController gmc = new GeneralMouseController(origin, userViewCamera, stateDB, textureDB, model, imagedir);
            Select selector = new Select(glDisplay, userViewCamera, gmc);

            ExtraRenderables.init(model, textureDB, origin);

            Renderer renderer = new Renderer(glDisplay, lights, model, mesh, userViewCamera, baker, selector, videoMode, cameraCalibration, origin);
            Feed feed = null;
            if (null != vclogList) {
                feed = new Feed(dem, baker, vclogList, imagedir, timeCodeFrameNumber, timeCodeMs, startAtTimeMs, origin, uavCamera);
                feed.start();
            }

            if (serverMode) {
                Server server = new Server(serverPort, renderer.getIncomingMsgQ());
                server.start();
            } else {
                if (startSanjayaListener) {
                    SanjayaListener sl = new SanjayaListener(multicastAddr, sanjayaListenerPort, stateDB, origin, dem);
                    sl.start();
                }
                if (clientMode) {
                    Simulate simulate = new Simulate(baker, stateDB, serverName, serverPort);
                    simulate.start();
                }
            }
            VBS2Sim vbs2Sim;
            if (useVBS2link) {
                Debug.debug(1, "Main.main: creating VBS2Sim");
                vbs2Sim = new VBS2Sim(vbs2ImageReplayFilename, baker, model, stateDB, userViewCamera, textureDB, origin, vbs2Hostname, vbs2Port, dem, glDisplayFrame, vbs2ExperimentType, vbs2guiFolder);
                Debug.debug(1, "Main.main: Starting VBS2Sim");
                vbs2Sim.start();
                Debug.debug(1, "Main.main: Done starting VBS2Sim");
            }
            if (generateVideoStills) {
                // these stills can be combined into an mpng file

                //REM NOTE - these commands must be run IN the directory where the images are - specifying full paths doesn't seem to work;
                //cd c:\owens\suave\captures
                //REM MPNG
                //REM c:/owens/MPlayer-p4-svn-31743/mencoder.exe mf://*.png -mf w=720:h=480:fps=30:type=PNG -ovc copy -oac copy -o c:/owens/suave/gen2.avi
                //
                //REM mpeg4?
                //REM c:/owens/MPlayer-p4-svn-31743/mencoder.exe mf://*.png -mf w=720:h=480:fps=30:type=PNG -ovc lavc -lavcopts vcodec=mpeg4:mbd=2:trell -oac copy -o c:/owens/suave/suave_mpeg4_1.avi
                //
                //REM raw - HUGE FILE
                //REM c:/owens/MPlayer-p4-svn-31743/mencoder.exe mf://*.png -mf w=720:h=480:fps=30:type=PNG -ovc raw -oac copy -o c:/owens/suave/gen2.avi

                VideoGenerator vg = new VideoGenerator(renderer, dem, mesh, "c:\\owens\\suave\\SanjayaListener_1uav_6.log", "c:\\owens\\suave\\captures", 0, origin);
                vg.start();
            }
            InputHandler inputHandler = new InputHandler(glDisplay, userViewCamera, feed, baker, model, uavCamera, renderer, terrainTexture, superUser);
            glDisplay.addKeyListener(inputHandler);
            glDisplay.addGLEventListener(renderer);
            glDisplay.start();

        } catch (Exception e) {
            dealWithExceptions(e);
        }
    }
}
