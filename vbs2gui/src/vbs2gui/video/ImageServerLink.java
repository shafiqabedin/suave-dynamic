/*
 * ImageServerLink.java
 *
 * Created on June 9, 2007, 5:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package vbs2gui.video;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import javax.swing.JPanel;
import vbs2gui.Debugger;
import vbs2gui.SimpleUAVSim.Main;
import vbs2gui.Vbs2Utils;

/**
 * Connects to the ImageServer and parses incoming images, then sends them
 * to the appropriate listeners.
 * @author pkv
 */
public class ImageServerLink implements Runnable {

    private static final Logger logger = Logger.getLogger(ImageServerLink.class.getName());
    public final static boolean LOG_TO_CANNED_DATA = false;
    public final static String CANNED_DATA_FILENAME_PREFIX = "vbs2_suave_canned_";
    // Connection to video server
    private String host;
    private int port;
    private BlockingQueue<ImageAndTelemetry> imgQueue = null;
    private Socket vidSock;
    private BufferedWriter out;
    private DataInputStream in;
    // Listener notification
    private Thread handler;
    protected EventListenerList listenerList;
    // Buffer for imaging
    int imgBufferType;
    private byte[] imgBuffer;
    private BufferedImage img = null;
    // Image MUX
    private VideoBank videoBank;
    // Video streams
    final int CAM_WIDTH = VideoBank.CAM_WIDTH, CAM_HEIGHT = VideoBank.CAM_HEIGHT;
    Image currImage = null;
    VideoPanel videoP = new VideoPanel();
    boolean showImageErrors = false;
    private DataOutputStream logStream = null;
    private static final Object LOCK = new Object();

    public ImageServerLink(String videoHost, int videoPort, VideoBank videoBank, BlockingQueue<ImageAndTelemetry> imgQueue, DataOutputStream logStream) {
        host = videoHost;
        port = videoPort;
        this.videoBank = videoBank;
        this.imgQueue = imgQueue;
        this.logStream = logStream;
        vidSock = new Socket();
        imgBuffer = new byte[0];
        handler = new Thread();
        listenerList = new EventListenerList();
    }

    /** Creates a new instance of ImageServerLink */
    public ImageServerLink(String videoHost, int videoPort, VideoBank videoBank, BlockingQueue<ImageAndTelemetry> imgQueue, String logFileName) {
        this(videoHost, videoPort, videoBank, imgQueue, (DataOutputStream)null);
        if (LOG_TO_CANNED_DATA && null != CANNED_DATA_FILENAME_PREFIX) {
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
//            String logFileName = CANNED_DATA_FILENAME_PREFIX + sdf.format(new Date()) + ".binlog";
            try {
                logStream = new DataOutputStream(new FileOutputStream(logFileName));
                System.err.println("ImageServerLink constructor: open canned data logfile filename=" + logFileName);
            } catch (java.io.FileNotFoundException e) {
                System.err.println("ImageServerLink constructor: Exception trying to open canned data logfile filename= " + logFileName);
                System.err.println("ImageServerLink constructor: Exception = " + e);
                e.printStackTrace();
                System.err.println("ImageServerLink constructor: Ignoring exception and continuing.");
            }
        }
    }

    /**
     * Attempt a connection to the ImageServer, or do nothing if there is
     * already a connection open.
     * @return the success of the connection attempt.
     */
    public boolean connect() {
        // Don't connect if a connection is already open
        if (vidSock.isConnected() && !vidSock.isClosed()) {
            return true;
        }

        // Open a new connection to the server
        try {
            vidSock = new Socket();
            vidSock.connect(new InetSocketAddress(host, port), 2000);
            vidSock.setSoTimeout(500);
            out = new BufferedWriter(new OutputStreamWriter(vidSock.getOutputStream()));
            in = new DataInputStream(vidSock.getInputStream());

            handler = new Thread(this);
            handler.start();
            return true;
        } catch (Exception e) {
            Debugger.debug(5, "ImageServerLink: Connection error - " + e);
            return false;
        }
    }

    /**
     * Disconnect the current link, or do nothing if there is no connection.
     */
    public void disconnect() {
        try {
            // Close connection, if it exists
            vidSock.close();
            try {
                handler.join(2000);
            } catch (InterruptedException e) {
            }
        } catch (IOException e) {
            logger.warning("ImageServerLink: Disconnection error - " + e);
        }
    }

    /**
     * Indicates the connection status of the link.
     * @return true if there is a connection, false if not.
     */
    public boolean isConnected() {
        return (vidSock.isConnected() && !vidSock.isClosed());
    }

    /**
     * Reads in a valid image and stores it.
     * @return the success of the read operation.
     * @throws java.io.IOException
     */
    private synchronized boolean receiveImage() throws IOException {
        imgBufferType = in.readByte();
        int size = in.readInt();

        // We expect types above 1, they are JPEG compressed
//        if (imgBufferType <= 1) {
//            return false;
//        }

        // If we get a ridiculously sized image, just drain
        if ((size > 250000) || (size <= 0)) {
            drainImage();
            if (showImageErrors) {
                Debugger.debug(3, "Failure : Invalid ImgServer image size : " + size);
            }
            return false;
        }

        // Reallocate buffer to match image stream
        if (imgBuffer.length != size);
        imgBuffer = new byte[size];

        // Read in the whole image
        int pos = 0;
        while (pos < size) {
            pos += in.read(imgBuffer, pos, size - pos);
            Thread.yield();
        }

        return true;
    }

    /**
     * Reads a valid image and ignores it.
     * @throws java.io.IOException
     */
    private synchronized void skipImage() throws IOException {
        byte type = in.readByte();
        int size = in.readInt();

        // We expect types above 1, they are JPEG compressed
        if (type <= 1) {
            return;
        }

        // If we get a ridiculously sized image, just drain
        if ((size > 250000) || (size <= 0)) {
            drainImage();
            if (showImageErrors) {
                Debugger.debug(3, "Failure : Invalid ImgServer image size : " + size);
            }
            return;
        }

        // Skip the whole image
        int pos = 0;
        while (pos < size) {
            pos += in.skipBytes(size - pos);
            Thread.yield();
        }
    }

    /**
     * Drains remaining bytes on input buffer.
     * @throws java.io.IOException
     */
    private synchronized void drainImage() throws IOException {
        // Just drain the read buffer, we don't want the data
        in.skipBytes(in.available());
    }

    /**
     * Request the next image from the image server.
     * @throws java.io.IOException
     */
    private synchronized void ackImage() throws IOException {
        if (out == null) {
            return;
        }

        // Send out the official 'OK'
        out.write("OK");
        out.flush();
    }

    /**
     * This gets a pointer to the <b>current image buffer</b>.
     * @return the current image.
     */
    public synchronized byte[] getDirectImage() {
        return imgBuffer;
    }

    /**
     * This returns a copy of the current image.
     * @return a copy of the current image.
     */
    public synchronized byte[] getImage() {
        return imgBuffer.clone();
    }

    /**
     * Non-blocking trigger to capture a single picture.
     */
    public void takePicture() {
        try {
            ackImage();
        } catch (IOException e) {
            Debugger.debug(5, "Failed to request image from image server : " + e);
        }
    }

    @Override
    public void run() {
        while (vidSock.isConnected()) {
            try {
                // Wait for new data to come in
                while (in.available() == 0) {
                    Thread.yield();
                }
                // Get the next image
                if (receiveImage()) {
                    // Note the time.
                    long receiveTime = System.currentTimeMillis();
                    if (null != logStream) {
                        synchronized(LOCK) {
                            logStream.writeLong(receiveTime);
                            logStream.writeInt(imgBufferType);
                            logStream.writeLong(imgBuffer.length);
                            logStream.write(imgBuffer, 0, imgBuffer.length);
                            logStream.flush();
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
                    //@todo: Handle image - should turn this into a blocking queue at some point
//                    videoBank.handleImage(img);
                    int[] uavColor = Vbs2Utils.parseImageUAVColor(img);
                    double[] telem = Vbs2Utils.parseImageTelemetry(img, vbs2gui.SimpleUAVSim.Main.USE_GPS);
                    Color roundedColor = new Color((uavColor[0] / 64) * 64, (uavColor[1] / 64) * 64, (uavColor[2] / 64) * 64);
                    if (null != videoBank) {
                        videoBank.handleImage(img);
                    } else {
                        // VideoBank.handleImage would duplicate this message
                        logger.info(Main.roundedColorToUavName.get(roundedColor)
                                + " POS " + VideoBank.fmt.format(telem[0]) + " " + VideoBank.fmt.format(telem[1]) + " " + VideoBank.fmt.format(telem[2])
                                + " DIR " + VideoBank.fmt.format(telem[3]) + " " + VideoBank.fmt.format(telem[4]) + " " + VideoBank.fmt.format(telem[5])
                                + " UP " + VideoBank.fmt.format(telem[6]) + " " + VideoBank.fmt.format(telem[7]) + " " + VideoBank.fmt.format(telem[8]));
                    }
                    if (null != imgQueue) {
                        logger.info("Added to queue");
                        imgQueue.add(new ImageAndTelemetry(img, telem, roundedColor.getRGB()));
                    }

                    Thread.yield();
                    takePicture();
                } else {
                    // Remove bits of this image, then re-request
                    drainImage();
                    takePicture();
                }
            } catch (SocketTimeoutException e) {
            } catch (IOException e) {
                Debugger.debug(5, "Error in image server connection : " + e);
                break;
            }
        }
    }

    private class VideoPanel extends JPanel {

        public Dimension getMinimumSize() {
            return new Dimension(CAM_WIDTH, CAM_HEIGHT);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(CAM_WIDTH, CAM_HEIGHT);//500,500
        }

        public void myRepaint() {
            (new Thread() {

                public void run() {
                    //wv.paintImmediately(new Rectangle(0,0,wv.getWidth(), wv.getHeight()));
                    videoP.repaint();
                }
            }).start();
        }

        public void paint(Graphics g2) {

            double width = (double) getWidth();
            double height = (double) getHeight();

            int rSize = 10;
            int tSize = 10;

            Graphics2D g = (Graphics2D) g2;
            g.clearRect(0, 0, (int) width, (int) height);

            if (currImage != null) {
                g.drawImage(currImage, 0, 0, null);
            }

        }
    }
}
