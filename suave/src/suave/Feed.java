package suave;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import java.util.*;
import java.text.DecimalFormat;

// This class is/may be threaded... yeah, it should be threaded.
// It is going to feed video frames to the Baker.  It may simply
// do this over time or it might do it in response to keystroke
// commands.  Initially threaded.
public class Feed implements Runnable {

    private final static boolean DO_PAINTING = true;
    private final static boolean RECALC_FRAME_FUDGE = false;
    private final static long RECALC_FRAME_FUDGE_AMOUNT = -238;
    private final static double NTSC_FRAME_RATE = (30.0 * 1000.0 / 1001.0);
    private final static double NTSC_MS_PER_FRAME = 1000 / NTSC_FRAME_RATE;
    private final static DecimalFormat fmt = new DecimalFormat("00000000.jpg");
    private final static DecimalFormat fmt2 = new DecimalFormat("0.000");
    private final static DecimalFormat fmt3 = new DecimalFormat("0.000000");
    private DEM dem = null;
    private Baker baker = null;
    private ArrayList<VirtualCockpitLogLine> vclogList;
    private String imagedir;
    private int timeCodeFrameNumber;
    private long timeCodeMs;
    private long startAtTimeMs;
    private Origin origin;
    private UAVCamera uavCamera = null;
    private double launchLat = 0;
    private double launchLon = 0;
    private double launchAltitude = 0;
    private boolean launchAltitudeReady = false;
    private boolean manualMode = true;
    private VirtualCockpitLogLine logline = null;
    private int logIndexStart = 0;
    private Object frameLock = new Object();
    private int logIndex = 0;
    private long frameIndex = 0;
    // xRotFactor is the rotation around the x axis, i.e. the axis
    // through the wings (i.e. tip your nose towards the floor or
    // towards the ceiling) - this represents the downward tilt of the
    // camera as mounted on the UAV.  IT's settable for
    // 'debugging'/calibration purposes as we're not quite certain
    // what the exact orientation of the mounted camera is.
    private float xRotFactor = -30;
    private VideoFrame videoFrame = null;
    private Thread myThread;

    // telemetry.heading = { 'Abs Time', 'Rel Time (ms)', 'Altitude', 'Airspeed', 'Roll', 'Pitch', 'Heading', 'Turn Rate', 'RSSI', 'RC PPS', 'Battery Cur', 'Battery Volt', 'Sys Status', 'GPS Num Sat Lock', 'Alt AI state', 'Des Alt', 'Des Airspd', 'Des Roll', 'Des Pitch', 'Des Hdg', 'Des Turn Rate', 'Servo Aileron', 'Servo Elev', 'Servo Thro', 'Servo Rud', 'UAV Control Mode', 'Fail Safe', 'Mag Hdg', 'Airborne Timer', 'Avionics Timer', 'System Flags', 'Payload 1', 'Payload 2', 'GPS velocity', 'GPS Alt', 'GPS Hdg', 'GPS Lat', 'GPS Long', 'Home Lat', 'Home Long', 'Cur Cmd', 'Nav State', 'Desired Lat', 'Desired Long', 'Time Over Target', 'Distance to Target', 'Heading to Target', 'FLC', 'Wind Hdg', 'Wind Spd', 'IO State', 'UTC Time', 'Count Timer', 'System Flags 1', 'Temperature R', 'NAV Avx Timer', 'Home Alt MSL', 'Roll Rate', 'Pitch Rate', 'Yaw Rate', 'Alt MSL', 'Gimbal Trg Lat', 'Gimbal Trg Lon' };
    // [ [0 0 0 12 15 22] 0 -126.666641 -2.350000 0.171000 -0.264000 0.092000 0.015000 0 0 1.3 12.4 141 9 0 100.000031 14.000000 0.000000 -0.379000 0.000000 0.000000 -0.050000 0.028571 0.000000 0.000000 0 0 0.092000 0.000000 560.475525 4096 0.000000 0.519000 0.450000 332.166718 0.977000 40.461918 -79.784279 40.460812 -79.783882 0 0 0.000000 0.000000 0 0 0.000000 828 0.000000 0.000000 10 [08 10 22 16 15 24] 16 0 37.857143 560.455383 0.000030 0.000000 0.000000 0.012500 203.166702 0.000000 0.000000];...
    public Feed(DEM dem, Baker baker, ArrayList<VirtualCockpitLogLine> vclogList, String imagedir, int timeCodeFrameNumber, long timeCodeMs, long startAtTimeMs, Origin origin, UAVCamera uavCamera) {
        this.dem = dem;
        this.baker = baker;
        this.vclogList = vclogList;
        this.imagedir = imagedir;
        this.timeCodeFrameNumber = timeCodeFrameNumber;
        this.timeCodeMs = timeCodeMs;
        this.startAtTimeMs = startAtTimeMs;
        this.origin = origin;
        this.uavCamera = uavCamera;

        logIndex = searchLogList(startAtTimeMs);
        logIndexStart = logIndex;
        logline = vclogList.get(logIndex);

        launchLat = logline.gpsLat;
        launchLon = logline.gpsLong;
        launchAltitude = logline.gpsAlt;

        frameIndex = calcFrameIndex(logline);

        Debug.debug(1, "Feed.constructor: startAtTimeMs " + startAtTimeMs + " results in log index " + logIndex + " frame " + frameIndex);

        myThread = new Thread(this);
    }

    public void start() {
        myThread.start();
    }

    private double getRealAltitude(double barometricAlt) {
        if (!launchAltitudeReady) {
            double xyz[] = new double[3];
            origin.gpsDegreesToLvcs(launchLat, launchLon, launchAltitude, xyz);
            launchAltitude = dem.getAltitude((float) xyz[0], (float) xyz[1]);
            if (-1 == launchAltitude) {
                Debug.debug(1, "Feed: DemAndTexture still not ready to give us launch altitude.");
                return barometricAlt;
            } else {
                launchAltitudeReady = true;
                Debug.debug(1, "Feed: At launch GPS lat/lon/alt=(" + logline.gpsLat + ", " + logline.gpsLong + ", " + logline.gpsAlt + ") according to DEM = " + launchAltitude);
            }
        }
        return launchAltitude + barometricAlt;
    }

    private BufferedImage createLineTextureImage() {
        BufferedImage image5 = new BufferedImage(720, 480, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image5.createGraphics();
        g.setColor(Color.gray);
        g.fillRect(0, 0, 720, 480);
        g.setColor(Color.blue);
        for (int loopx = 0; loopx < 720; loopx += 16) {
            g.drawLine(loopx, 0, loopx, 480);
        }
        g.setColor(Color.red);
        for (int loopy = 0; loopy < 480; loopy += 16) {
            g.drawLine(0, loopy, 720, loopy);
        }
        g.dispose();
        return image5;
    }

    private int searchLogList(long startAtTimeMs) {
        VirtualCockpitLogLine logline = null;
        for (int loopi = 0; loopi < vclogList.size(); loopi++) {
            logline = vclogList.get(loopi);
            if (logline.utcPlusRel > startAtTimeMs) {
                return loopi;
            }
        }
        return -1;
    }

    private void setupFrame(int newLogIndex, long newFrameIndex) {
        float xPos = 0.0f;
        float yPos = 0.0f;
        float zPos = 0.0f;
        float xRot = 0.0f;
        float yRot = 0.0f;
        float zRot = 0.0f;
        logline = null;
        double xyz[] = new double[3];

        synchronized (frameLock) {
            logIndex = newLogIndex;
            frameIndex = newFrameIndex;

            logline = vclogList.get(logIndex);

//            origin.gpsDegreesToLvcs(logline.gpsLat, logline.gpsLong, logline.gpsAlt, xyz);
            origin.gpsDegreesToLvcs(logline.gpsLat, logline.gpsLong, logline.altitude, xyz);

            // NOTE: We are swapping z and y here.  UAV logs assume z
            // is up/down (altitude), whereas JOGL assumes Z is
            // into/out of the screen.  We also negate the z value
            // because OGL uses a RHS coordinate system.
            xPos = (float) xyz[0];
            yPos = (float) xyz[2];
            zPos = -(float) xyz[1];

            // OK, and we're messing with rotations here because
            // otherwise it doesn't do what it should do.  These
            // changes have been figured out by trial and error - not
            // the best way to do it.

            // yaw/heading, pitch, and roll are the angles for an
            // airplane - if you were an airplane, your arms were the
            // wings, your nose was the 'nose' of the airplane and
            // the back of your head was the 'tail';
            //
            // 1) yaw/heading - the axes straight up and down through
            // your spine - i.e. if you were driving around on the
            // ground this would describe what direction you were
            // turning/heading, left or right.
            //
            // 2) pitch - the axis through your arms or 'wings' - this
            // describes if you tilt your nose up or down.
            //
            // 3) roll - the axis from your nose to the back of your
            // head - this describes if you roll your head to the left
            // or right (not turn, but kinda lean).

            // 0, 0, 0 is flat, level, and heading straight south

            // y axis is the up/down axis so it gets heading
            yRot = (float) Math.toDegrees(logline.heading);
            // x axis is the east/west axis
            xRot = (float) Math.toDegrees(logline.pitch);
            // z axis is the north/south axis
            zRot = (float) Math.toDegrees(logline.roll);

            // pre "fix the ogl mirroring and texture crap" good value for yRot
            //	    yRot= 360.0f - yRot;
            // 	    xRot = xRot;
            // 	    zRot = zRot;


            yRot = yRot;	// good
            zRot = zRot;
            xRot = xRot;
            xRot -= xRotFactor;

            uavCamera.setPosition(xPos, yPos, zPos, xRot, yRot, zRot);

            videoFrame = new VideoFrame();

            videoFrame.timeMs = logline.utcPlusRel;
            videoFrame.frameIndex = (int) frameIndex;

            videoFrame.x = xPos;
            videoFrame.y = yPos;
            videoFrame.z = zPos;
            videoFrame.xRot = xRot;
            videoFrame.yRot = yRot;
            videoFrame.zRot = zRot;

            Debug.debug(1, "Feed.setupFrame: for log index " + logIndex + " loading frame index " + frameIndex);
            String imageFileName = imagedir + File.separator + fmt.format(frameIndex);
            try {
                videoFrame.loadImage(imageFileName);
            } catch (Throwable e) {
                Debug.debug(1, "Could not find the video frame file we needed, name='" + imageFileName + "', e=" + e);
            }
        }
    }

    private long calcFrameIndex(VirtualCockpitLogLine logline) {
        // calculate index of appropriate video frame file for a log
        // line;

        // Difference in ms between the logline utcPlusRel time and
        // the timecode
        double diff = logline.utcPlusRel - timeCodeMs;

        // how many frames is that difference, based on NTSC frame
        // rate
        long numFrames = (long) (diff / NTSC_MS_PER_FRAME);

        // frame we want is the frame number that the timecode is
        // from, plus that many frames.
        long newFrameIndex = timeCodeFrameNumber + numFrames;

        if (RECALC_FRAME_FUDGE) {
            newFrameIndex += RECALC_FRAME_FUDGE_AMOUNT;
        }

        Debug.debug(1, "Feed.calcFrameIndex: logline " + logIndex + " utcTime=" + logline.utcTime + ", relTime=" + logline.relTime + ", utcPlusRel = " + logline.utcPlusRel + ", timeCodeMs = " + timeCodeMs + " diff = " + diff + ", NTSC_MS_PER_FRAME=" + NTSC_MS_PER_FRAME + ", numFrames=" + numFrames + " timeCodeFrameNumber = " + timeCodeFrameNumber + " frameIndex = " + newFrameIndex);
        return newFrameIndex;
    }

    // OK, so given a 'timecode frame' index, in other word the number
    // of the frame with the time code on it, and the associated
    // timecode, (time in ms since the epoch), and an index of the
    // frame to _start_ on, 
    //
    // 1) compute start frame time
    // 
    // 2) find the most recent line in vclog after that frame - it may
    // be that we have stale telemetry for that specific frame.
    // 
    // 3) 
    public void run() {

        // @TODO: Replace this sleep with checking if mesh is ready or something.

        // Wait for the mesh to load
        try {
            Thread.sleep(19000);
        } catch (Exception e) {
        }


        boolean oldCodeBroken = true;
        while (oldCodeBroken) {
            if (manualMode) {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                }
                continue;
            }

            // stolen from 'logline forward'
            logIndex++;
            if (logIndex >= vclogList.size()) {
                logIndex = logIndexStart;
            }
            frameIndex = calcFrameIndex(logline);
            changeFrame(0);

            // @TODO: If I decrease the sleep time to 500 ms it
            // doesn't seem to work quite right - looks like maybe
            // it's dropping frames - or perhaps messing up the UAV
            // position?  Or maybe it's all in my mind.  Take a closer
            // look later, verify that it really is a problem, and if
            // so look into fixing it.
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }

        }



        float xPos = 0.0f;
        float yPos = 0.0f;
        float zPos = 0.0f;
        float xRot = 0.0f;
        float yRot = 0.0f;
        float zRot = 0.0f;
        double xyz[] = new double[3];

        BufferedImage lineImage = createLineTextureImage();
        TextureReader.Texture lineFrame = TextureReader.createTexture(lineImage, true);

        while (true) {
            while (logIndex < vclogList.size()) {
                if (manualMode) {
                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                    }
                    continue;
                }

                logline = vclogList.get(logIndex);
                frameIndex = calcFrameIndex(logline);
                String imageFileName = imagedir + "\\" + fmt.format(frameIndex);
                setupFrame(logIndex, frameIndex);

                // ----------------------------------------------------------------------
                //
                // setupFrame should have done all of this already;

                // 		//		GeoTransforms.gps_to_lvcs(lvcs, Math.toRadians(logline.gpsLat),Math.toRadians(logline.gpsLong),logline.gpsAlt, xyz);
                // 		GeoTransforms.gps_to_lvcs(lvcs, Math.toRadians(logline.gpsLat),Math.toRadians(logline.gpsLong),getRealAltitude(logline.altitude), xyz);

                // 		// NOTE: yes we're swapping z and y here.  uav
                // 		// logs assume z is up/down (altitude), whereas
                // 		// jogl assumes Z is into/out of the screen.
                // 		xPos = (float)xyz[0];
                // 		yPos = (float)xyz[2];
                // 		zPos = (float)xyz[1];

                // 		yRot = (float)Math.toDegrees(logline.heading)-180;
                // 		xRot = (float)Math.toDegrees(logline.pitch);
                // 		zRot = (float)Math.toDegrees(logline.roll)-180;

                // 		videoFrame.x = xPos;
                // 		videoFrame.y = yPos;
                // 		videoFrame.z = zPos;
                // 		videoFrame.xRot = xRot;
                // 		videoFrame.yRot = yRot;
                // 		videoFrame.zRot = zRot;

                //		videoFrame.videoFrame = lineFrame;
                //		videoFrame.lineImage = lineImage;

                //		Debug.debug(1, "Feed.run: logline "+logIndex+" About to bake frame "+imageFileName+" at xyz=("+fmt2.format(xPos)+", "+fmt2.format(yPos)+", "+fmt2.format(zPos)+") rot=("+fmt2.format(xRot)+", "+fmt2.format(yRot)+", "+fmt2.format(zRot)+") lat/lon/alt=("+fmt3.format(logline.gpsLat)+", "+fmt3.format(logline.gpsLong)+", "+fmt3.format(logline.gpsAlt)+")");

                //
                // ----------------------------------------------------------------------

                Debug.debug(1, "Feed.run: logline " + logIndex + " About to bake frame " + imageFileName + " at xyz=(" + fmt2.format(xPos) + ", " + fmt2.format(yPos) + ", " + fmt2.format(zPos) + ") rot=(" + fmt2.format(xRot) + ", " + fmt2.format(yRot) + ", " + fmt2.format(zRot) + ") lat/lon/alt=(" + fmt3.format(logline.gpsLat) + ", " + fmt3.format(logline.gpsLong) + ", " + fmt3.format(getRealAltitude(logline.altitude)) + ")");

                BakerCommand com1 = new BakerCommand(videoFrame);
                baker.queueCommand(com1);

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }


                logIndex++;
            }
            if (manualMode) {
                continue;
            }
            Debug.debug(1, "Feed.run: RESETTING!");

            baker.queueResetTexture();
            logIndex = 0;
        }
    }

    private void queueCurrentFrame() {
        // Send it off to the baker.
        if (DO_PAINTING) {
            BakerCommand com1 = new BakerCommand(videoFrame);
            baker.queueCommand(com1);
        }
    }

    private void changeFrame(int move) {
        if (manualMode) {
            baker.queueResetTexture();
        }
        synchronized (frameLock) {
            long newFrameIndex = frameIndex + move;
            setupFrame(logIndex, newFrameIndex);

            //	    Debug.debug(1, "Feed.changeFrame: logline "+logIndex+" About to bake frame "+videoFrame.imageFilename+" at xyz=("+fmt2.format(videoFrame.x)+", "+fmt2.format(videoFrame.y)+", "+fmt2.format(videoFrame.z)+") rot=("+fmt2.format(videoFrame.xRot)+", "+fmt2.format(videoFrame.yRot)+", "+fmt2.format(videoFrame.zRot)+") lat/lon/alt=("+fmt3.format(logline.gpsLat)+", "+fmt3.format(logline.gpsLong)+", "+fmt3.format(logline.gpsAlt)+")");
            Debug.debug(1, "Feed.changeFrame: logline " + logIndex + " About to bake frame " + videoFrame.imageFilename + " at xyz=(" + fmt2.format(videoFrame.x) + ", " + fmt2.format(videoFrame.y) + ", " + fmt2.format(videoFrame.z) + ") rot=(" + fmt2.format(videoFrame.xRot) + ", " + fmt2.format(videoFrame.yRot) + ", " + fmt2.format(videoFrame.zRot) + ") lat/lon/alt=(" + fmt3.format(logline.gpsLat) + ", " + fmt3.format(logline.gpsLong) + ", " + fmt3.format(getRealAltitude(logline.altitude)) + ")");
            queueCurrentFrame();
        }
    }

    public void resetTexture(boolean flag) {
        if (flag) {
            baker.queueResetTexture();
        }
    }

    public void paintVideoFrame(boolean flag) {
        if (flag) {
            queueCurrentFrame();
        }
    }

    public void bakeNextFrame(boolean flag) {
        if (flag) {
            changeFrame(1);
        }
    }

    public void bakePrevFrame(boolean flag) {
        if (flag) {
            changeFrame(-1);
        }
    }

    public void bakeNext20Frame(boolean flag) {
        if (flag) {
            changeFrame(20);
        }
    }

    public void bakePrev20Frame(boolean flag) {
        if (flag) {
            changeFrame(-20);
        }
    }

    public void loglineForward(boolean flag) {
        if (flag) {
            logIndex++;
            if (logIndex >= vclogList.size()) {
                logIndex = logIndexStart;
            }
            changeFrame(0);
        }
    }

    public void loglineBackward(boolean flag) {
        if (flag) {
            logIndex--;
            if (logIndex < logIndexStart) {
                logIndex = vclogList.size() - 1;
            }
            changeFrame(0);
        }
    }

    public void recalculateLoglineFrame(boolean flag) {
        if (flag) {
            Debug.debug(1, "Recalculating which frame to use for current log line=" + logIndex);
            frameIndex = calcFrameIndex(logline);
            changeFrame(0);
        }
    }

    public void minusXRotFactor(boolean flag) {
        if (flag) {
            xRotFactor -= 1.0f;
            Debug.debug(1, "Feed.minusXRotFactor: xRotFactor=" + xRotFactor);
            changeFrame(0);
        }
    }

    public void plusXRotFactor(boolean flag) {
        if (flag) {
            xRotFactor += 1.0f;
            Debug.debug(1, "Feed.minusXRotFactor: xRotFactor=" + xRotFactor);
            changeFrame(0);
        }
    }

    public void flipManualMode(boolean flag) {
        if (flag) {
            manualMode = !manualMode;
            Debug.debug(1, "Feed.flipManualMode: manualMode=" + manualMode);
        }
    }
}
