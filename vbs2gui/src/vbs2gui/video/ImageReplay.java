/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vbs2gui.video;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import vbs2gui.Vbs2Utils;
import vbs2gui.sagat.SagatMission;

/**
 *
 * @author owens
 */
public class ImageReplay implements Runnable {

    DecimalFormat fmt1 = new DecimalFormat("0.00000000");
    DecimalFormat fmt2 = new DecimalFormat("0000000");
    private final static int MAX_BUF_SIZE = 256 * 1024;
    private String logFileName;
    private BlockingQueue<ImageAndTelemetry> imgQueue = null;
    private Thread myThread;
    private DataInputStream logStream;
    private long[] pauseTimes;
    private int nextPauseIndex = 0;
    private SagatMission sagatMission;
    private boolean paused = false;

    public synchronized boolean isPaused() {
        return paused;
    }

    public synchronized void setPaused(boolean paused) {
        System.err.println("ImageReplay: Set PAUSED to " + paused);
        this.paused = paused;
    }

    public ImageReplay(String logFileName, BlockingQueue<ImageAndTelemetry> imgQueue, long[] pauseTimes, SagatMission sagatMission) {
        this.logFileName = logFileName;
        this.imgQueue = imgQueue;
        this.pauseTimes = pauseTimes;
        this.sagatMission = sagatMission;

        // Open the log file for playback
        try {
            if (null == logFileName) {
                logStream = new DataInputStream(System.in);
            } else {
                logStream = new DataInputStream(new FileInputStream(logFileName));
            }
        } catch (java.io.FileNotFoundException e) {
            System.err.println("ERROR: Problems opening log file=\"" + logFileName + "\": " + e);
            e.printStackTrace();
        }
        myThread = new Thread(this);
    }

    public void start() {
        myThread.start();
    }

    public void run() {
        BufferedImage img = null;

        try {
            byte[] imgBuffer = new byte[MAX_BUF_SIZE];
            long lastPacketTime = -1;
            while (true) {
                // Note that the log format is very simple - a long
                // representing the time the packet was received, in
                // milliseconds since Jan 1, 1970, then an int with the
                // size of the packet, then the raw data of the packet
                // itself.
                long packetTime = logStream.readLong();
                int imgBufferType = logStream.readInt();
                int bufferLen = (int) logStream.readLong();
                logStream.read(imgBuffer, 0, bufferLen);
                System.err.println("Found packet of len=" + bufferLen + " saved at time=" + packetTime);

                // Note that the 'packetTime' saved when we created the
                // log was saved as an absolute value (i.e. number of
                // seconds since Jan 1, 1970), so we have to keep
                // track of and calculate the time since we started
                // the playback.
                if (-1 == lastPacketTime) {
                    lastPacketTime = packetTime;
                }
                long relativePacketTime = (packetTime - lastPacketTime);
                lastPacketTime = packetTime;
                try {
                    Thread.sleep(relativePacketTime);
                } catch (InterruptedException e) {
                }

                if (null != pauseTimes && null != sagatMission) {
                    if ((nextPauseIndex < pauseTimes.length) && (packetTime > pauseTimes[nextPauseIndex])) {
                        nextPauseIndex++;
                        setPaused(true);
                        final ImageReplay fThis = this;
                        new Thread(new Runnable() {

                            public void run() {
                                sagatMission.pauseSimulation(fThis);
                            }
                        }).start();
                    }
                }
                while (isPaused()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                    }
                }
                if (imgBufferType == 0) {
                    // Read raw image
                    int width = ((int) imgBuffer[0] & 0xFF) << 8 | ((int) imgBuffer[1] & 0xFF);
                    int height = ((int) imgBuffer[2] & 0xFF) << 8 | ((int) imgBuffer[3] & 0xFF);

                    int[] imageArray = new int[(imgBuffer.length - 4) / 3];
                    for (int i = 0; i < imageArray.length; i++) {
                        imageArray[i] = (int) imgBuffer[3 * i + 4] & 0xFF
                                | ((int) imgBuffer[3 * i + 5] & 0xFF) << 8
                                | ((int) imgBuffer[3 * i + 6] & 0xFF) << 16
                                | 0xFF000000;
                    }
                    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    img.setRGB(0, 0, width, height, imageArray, 0, width);
                } else {
                    // Decompress image
                    img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(imgBuffer));
                }
                int[] uavColor = Vbs2Utils.parseImageUAVColor(img);
                double[] telem = Vbs2Utils.parseImageTelemetry(img, true);
                if (null != imgQueue) {
                    int[] roundedColor = new int[]{(uavColor[0] / 64) * 64, (uavColor[1] / 64) * 64, (uavColor[2] / 64) * 64};
                    int sRGB = new Color(roundedColor[0], roundedColor[1], roundedColor[2]).getRGB();
                    imgQueue.add(new ImageAndTelemetry(img, telem, sRGB));
                }
                Thread.yield();
            }
        } catch (EOFException e) {
            // Reached end of file (final SAGAT phase)
            if (null != pauseTimes && null != sagatMission) {
                nextPauseIndex++;
                setPaused(true);
                final ImageReplay fThis = this;
                new Thread(new Runnable() {

                    public void run() {
                        sagatMission.pauseSimulation(fThis);
                    }
                }).start();
            }
            while (isPaused()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ie) {
                }
            }
            System.err.println("Done with EOF Exception");
        } catch (IOException e) {
            System.err.println("ERROR: IO Exception e=" + e);
            e.printStackTrace();
            System.err.println("Done with exception");
        } catch (Exception e) {
            System.err.println("ERROR: Exception e=" + e);
            e.printStackTrace();
            System.err.println("Done with exception");
        }

    }

    public void split(String basename) {

        BufferedImage img = null;

        try {
            byte[] imgBuffer = new byte[MAX_BUF_SIZE];
            long lastPacketTime = -1;
            long firstPacketTime = -1;
            while (true) {
                // Note that the log format is very simple - a long
                // representing the time the packet was received, in
                // milliseconds since Jan 1, 1970, then an int with the
                // size of the packet, then the raw data of the packet
                // itself.
                long packetTime = logStream.readLong();
                int imgBufferType = logStream.readInt();
                int bufferLen = (int) logStream.readLong();
                logStream.read(imgBuffer, 0, bufferLen);
                System.err.println("Found packet of len=" + bufferLen + " saved at time=" + packetTime);

                // Note that the 'packetTime' saved when we created the
                // log was saved as an absolute value (i.e. number of
                // seconds since Jan 1, 1970), so we have to keep
                // track of and calculate the time since we started
                // the playback.
                if (-1 == firstPacketTime) {
                    firstPacketTime = packetTime;
                }
                long relativePacketTime = (packetTime - lastPacketTime);
                lastPacketTime = packetTime;

                if (imgBufferType == 0) {
                    // Read raw image
                    int width = ((int) imgBuffer[0] & 0xFF) << 8 | ((int) imgBuffer[1] & 0xFF);
                    int height = ((int) imgBuffer[2] & 0xFF) << 8 | ((int) imgBuffer[3] & 0xFF);

                    int[] imageArray = new int[(imgBuffer.length - 4) / 3];
                    for (int i = 0; i < imageArray.length; i++) {
                        imageArray[i] = (int) imgBuffer[3 * i + 4] & 0xFF
                                | ((int) imgBuffer[3 * i + 5] & 0xFF) << 8
                                | ((int) imgBuffer[3 * i + 6] & 0xFF) << 16
                                | 0xFF000000;
                    }
                    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    img.setRGB(0, 0, width, height, imageArray, 0, width);
                } else {
                    // Decompress image
                    img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(imgBuffer));
                }
                int[] uavColor = Vbs2Utils.parseImageUAVColor(img);
                double[] telem = Vbs2Utils.parseImageTelemetry(img, true);
                int[] roundedColor = new int[]{(uavColor[0] / 64) * 64, (uavColor[1] / 64) * 64, (uavColor[2] / 64) * 64};
                int sRGB = new Color(roundedColor[0], roundedColor[1], roundedColor[2]).getRGB();

//                ImageAndTelemetry imgAndTelem = new ImageAndTelemetry(img, telem, sRGB);
                double lat, lon, alt;
                lon = telem[0];
                lat = telem[1];
                alt = telem[2];

                String UAVName = "UAV_UNKNOWN";

                String uavID = vbs2gui.SimpleUAVSim.Main.roundedColorToUavName.get(new Color(sRGB));
                if (uavID.contains("PLANE")) {
                    UAVName = uavID.replace("PLANE", "UAV_");
                }
                String imgFileName = basename + "_" + UAVName + "_" + fmt2.format(packetTime - firstPacketTime) + "_" + fmt1.format(lat) + "_" + fmt1.format(lon) + ".jpg";
                if (imgBufferType == 0) {
                    // @TODO: Write out as jpeg
                    throw new RuntimeException("Haven't added code to create jpegs from raw images yet");
                } else {
                    try {
                        DataOutputStream imgStream = new DataOutputStream(new FileOutputStream(imgFileName));
                        imgStream.write(imgBuffer, 0, imgBuffer.length);
                        imgStream.flush();
                        imgStream.close();
                        System.err.println("ImageServerLink constructor: open canned data logfile filename=" + imgFileName);
                    } catch (java.io.FileNotFoundException e) {
                        System.err.println("ImageServerLink constructor: Exception trying to open canned data logfile filename= " + imgFileName);
                        System.err.println("ImageServerLink constructor: Exception = " + e);
                        e.printStackTrace();
                        System.err.println("ImageServerLink constructor: Ignoring exception and continuing.");
                    }
                }
            }
        } catch (EOFException e) {

            System.err.println("Done with EOF Exception");
        } catch (IOException e) {
            System.err.println("ERROR: IO Exception e=" + e);
            e.printStackTrace();
            System.err.println("Done with exception");
        } catch (Exception e) {
            System.err.println("ERROR: Exception e=" + e);
            e.printStackTrace();
            System.err.println("Done with exception");
        }

    }

    public static void main(String[] args) {
        BlockingQueue<ImageAndTelemetry> imgQueue = new LinkedBlockingDeque<ImageAndTelemetry>();
        String[] logfiles = {
            "/home/usar/Desktop/logs/vbs2_suave_canned_feed_1.binlog",
            "/home/usar/Desktop/logs/vbs2_suave_canned_feed_2.binlog",
            "/home/usar/Desktop/logs/vbs2_suave_canned_feed_WARMUP.binlog"
        };
        String[] basenames = {
            "/home/usar/Desktop/splits/feed_1/feed_1",
            "/home/usar/Desktop/splits/feed_2/feed_2",
            "/home/usar/Desktop/splits/feed_WARMUP/feed_WARMUP"
        };

        for (int loopi = 0; loopi < logfiles.length; loopi++) {
            ImageReplay imageReplay = new ImageReplay(logfiles[loopi], imgQueue, null, null);
            imageReplay.split(basenames[loopi]);
        }
    }
}
