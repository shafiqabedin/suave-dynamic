/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vbs2gui.video;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import vbs2gui.SimpleUAVSim.Main;
import vbs2gui.Vbs2Handler.Icon;
import vbs2gui.Vbs2Utils;
import vbs2gui.map.MapPanel;
import vbs2gui.sagat.SagatMission;

/**
 *
 * @author nbb
 */
public class VideoBank {
    // GPS corners

    private static final double MAP_LAT_START_NORTH = 51.2056236267;
    private static final double MAP_LAT_END_NORTH = 51.2500648499;
    private static final double MAP_LON_START_WEST = -2.1216654778;
    private static final double MAP_LONG_END_WEST = -2.1940853596;
    // @TODO: ARGGGGG
    //  Note that there are two versions of displaImage() - they BOTH are called
    // from handleImage() - and they both use the same values
    // EXCEPT that START_WEST and END_WEST are reversed.  
    private static final double MAP_LAT_START_NORTH2 = 51.2056236267;
    private static final double MAP_LAT_END_NORTH2 = 51.2500648499;
    private static final double MAP_LON_START_WEST2 = -2.1940853596;
    private static final double MAP_LON_END_WEST2 = -2.1216654778;
    Logger logger = Logger.getLogger(VideoBank.class.getName());
    private int numVideos, videoCount;
    private ArrayList<Integer> uavStreamCount;
    public static final DecimalFormat fmt = new DecimalFormat("0.0000000000");
    private Hashtable<Integer, Integer> uavTable;
    JFrame[] videoFrames;
    JPanel[] videoP;
    MapPanel mapPanel;
    MuxFrame[] muxFrames;
    SagatMission sagatMission;
    VideoPanel[] videoPanels;
    BlockingQueue<ImageAndTelemetry> imgQueue;
    final boolean SHOW_TELEMETRY_WINDOW = false;
    final boolean SHOW_VIDEO_WINDOW = true;
    // VBS2 reslution, set in VBS2.cfg
    final static int CAM_WIDTH = 640, CAM_HEIGHT = 480;
    final int vidX = 640 + 50, vidY = 480 + 50;
    // Multiview config information
    private boolean isMultiview = false;
    private int numTilesX, numTilesY, numTiles;

    /**
     * @param numVideos the number of video feeds we will receive (# UAVs)
     */
    public VideoBank(int numVideos, SagatMission sagatMission, MapPanel mapPanel, BlockingQueue<ImageAndTelemetry> imgQueue) {
        this.numVideos = numVideos;
        this.mapPanel = mapPanel;
        this.sagatMission = sagatMission;
        this.imgQueue = imgQueue;
        videoCount = 0;
        uavStreamCount = new ArrayList<Integer>();
        uavTable = new Hashtable<Integer, Integer>();
        videoFrames = new JFrame[numVideos];
        videoP = new JPanel[numVideos];
        muxFrames = new MuxFrame[numVideos];
        videoPanels = new VideoPanel[numVideos];
        for (int i = 0; i < numVideos; i++) {
            if (SHOW_VIDEO_WINDOW) {
                videoPanels[i] = new VideoPanel();
                videoPanels[i].setPreferredSize(new java.awt.Dimension(vidX, vidY));
                videoPanels[i].setSize(new java.awt.Dimension(vidX, vidY));
                videoPanels[i].setBackground(Main.actualUavColors[i]);
                videoFrames[i] = new JFrame("Video" + i);
                videoFrames[i].getContentPane().setLayout(new BorderLayout());
                videoFrames[i].setPreferredSize(new java.awt.Dimension(vidX + 20, vidY + 20));
                videoFrames[i].setSize(new java.awt.Dimension(vidX + 20, vidY + 20));
                videoFrames[i].pack();
                videoFrames[i].getContentPane().add(videoPanels[i]);
                videoFrames[i].setVisible(true);
                if (SHOW_TELEMETRY_WINDOW) {
                    // Display of decoded pixel data
                    muxFrames[i] = new MuxFrame("Mux" + i);
                    muxFrames[i].setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    muxFrames[i].setVisible(true);
                    muxFrames[i].addWindowListener(new WindowListener() {

                        public void windowClosed(WindowEvent arg0) {
                        }

                        public void windowActivated(WindowEvent arg0) {
                        }

                        public void windowClosing(WindowEvent arg0) {
                            // Print out all the colors we saw
                            java.util.Enumeration e = uavTable.keys();
                            int sRGB;
                            Color color;
                            while (e.hasMoreElements()) {
                                sRGB = ((Integer) e.nextElement()).intValue();
                                color = new Color(sRGB);
                                System.out.println(color.getRed() + "," + color.getGreen() + "," + color.getBlue());
                            }
                            System.exit(0);
                        }

                        public void windowDeactivated(WindowEvent arg0) {
                        }

                        public void windowDeiconified(WindowEvent arg0) {
                        }

                        public void windowIconified(WindowEvent arg0) {
                        }

                        public void windowOpened(WindowEvent arg0) {
                        }
                    });
                }
            }
        }
        // Register for frame pausing/hiding
        if (SHOW_VIDEO_WINDOW) {
            for (JFrame vf : videoFrames) {
                sagatMission.addSagatListener(vf);
            }
        }
        if (SHOW_TELEMETRY_WINDOW) {
            for (MuxFrame mf : muxFrames) {
                sagatMission.addSagatListener(mf);
            }
        }
    }

    // @TODO: These methods are exactly the same, except they use different constants.
    // The different constants are ALMOST exactly the same, except the '2' set has END_WEST
    // and START_WEST  the opposite of the non '2' set.  Yay.  For what it's worth, I think the '2'
    // set is what is actually being used.  
    public static void gpsToVBS2(double[] telemetry, double[] eastNorth) {
        // telemetry[0] is position +x (north) or longitude depending on mode
        // telemetry[1] is position +y (east) or latitude depending on mode
        // Assuming we are in longitude/latitude mode,
        eastNorth[0] = (telemetry[0] - MAP_LON_START_WEST2) / (MAP_LON_END_WEST2 - MAP_LON_START_WEST2) * 5000;
        eastNorth[1] = (telemetry[1] - MAP_LAT_START_NORTH2) / (MAP_LAT_END_NORTH2 - MAP_LAT_START_NORTH2) * 5000;
    }

    public static void vbsToGps2(double[] eastNorth, double[] telemetry) {
        telemetry[0] = ((eastNorth[0] / 5000) * (MAP_LON_END_WEST2 - MAP_LON_START_WEST2)) + MAP_LON_START_WEST2;
        telemetry[1] = ((eastNorth[1] / 5000) * (MAP_LAT_END_NORTH2 - MAP_LAT_START_NORTH2)) + MAP_LAT_START_NORTH2;
    }

    private void gpsToVBS(double[] telemetry, double[] eastNorth) {
        // telemetry[0] is position +x (north) or longitude depending on mode
        // telemetry[1] is position +y (east) or latitude depending on mode
        // Assuming we are in longitude/latitude mode,
        eastNorth[0] = (telemetry[0] - MAP_LON_START_WEST) / (MAP_LONG_END_WEST - MAP_LON_START_WEST) * 5000;
        eastNorth[1] = (telemetry[1] - MAP_LAT_START_NORTH) / (MAP_LAT_END_NORTH - MAP_LAT_START_NORTH) * 5000;
    }

    /**
     * Takes a series of 6 pixel's RGB values and decodes them to a decimal number.
     * @param img
     * @param uavColor
     * @param telemetry contains [position.x, position.y, position.z (ASL),
     *  direction.x, direction.y, direction.z, up.x, up.y, up.z] where position
     *  is in (m) and direction and up are the look and up vectors. These all
     *  are in the coordinate system +x North, +y East, +z Down
     */
    public void displayImage(BufferedImage img, int[] uavColor, double[] telemetry) {

        // NOTE: Moved GPS corners to public constants at top.


        // Round our UAV color to account for jpg compression
        int videoNum = -1;
        int[] roundedColor = new int[]{(uavColor[0] / 64) * 64, (uavColor[1] / 64) * 64, (uavColor[2] / 64) * 64};
        int sRGB = new Color(roundedColor[0], roundedColor[1], roundedColor[2]).getRGB();

        // Identify the UAV its associated color
        if (uavTable.containsKey(new Integer(sRGB))) {
            // Retrieve UAV number and save image
            videoNum = (uavTable.get(new Integer(sRGB))).intValue();
            int imageNum = (uavStreamCount.get(videoNum)).intValue();
            uavStreamCount.set(videoNum, imageNum + 1);
        } else {
            // This is a new UAV, assign it a number
            uavTable.put(sRGB, videoCount);
            uavStreamCount.add((Integer) 0);
            videoNum = videoCount;
            if (videoNum != -1 && videoNum < numVideos) {
                Color bg = new Color(sRGB);
                videoPanels[videoNum].setBackground(bg);
            }
            videoCount++;
        }
        // Update UAV video feed frame, if applicable
        if (videoNum != -1 && videoNum < numVideos) {
            // Fetching the UAV number went smoothly
            //videoPanels[videoNum].setCurrImage(img);
            //videoPanels[videoNum].myRepaint();
            videoPanels[videoNum].setImage(img);
            if (SHOW_TELEMETRY_WINDOW) {
                muxFrames[videoNum].update(sRGB, telemetry);
            }
        } else if(numVideos > 0) {
            // We have too many "UAVs" in our hash table - could be because
            //  VideoBank started before the UAV color pixel box was begun to be
            //  drawn, or we are running more UAVs than updateFleet.sqf has
            //  unique colors for.
            System.err.println("Image for uav sRGB " + sRGB + " not displayed in video bank.");
        }
        // Update UAV location on 2D map, if applicable
        if (mapPanel != null) {
            // The camera is facing down so we use the up vector
            // telemetry[6] is direction +x (north)
            // telemetry[7] is direction +y (east)
            // telemetry[8] is direction +z (down)
            // So in the atan2 frame, telemetry[6] is y and telemetry[7] is x
            // Sign flip and +Math.PI/2 are to get into right handed, 0 = north
            //  frame
            double heading = Math.atan2(telemetry[6], telemetry[7]) - Math.PI / 2;
            double[] eastNorth = new double[2];
            gpsToVBS2(telemetry, eastNorth);
            double east = eastNorth[0];
            double north = eastNorth[1];
            String id = Main.roundedColorToUavName.get(new Color(sRGB));
            if (id != null) {
                vbs2gui.Vbs2Handler.Icon icon = vbs2gui.Vbs2Handler.Icon.PLAYER;
                try {
                    icon = vbs2gui.Vbs2Handler.Icon.valueOf(id);
                } catch (IllegalArgumentException e) {
                }
                if (icon.image() == null) {
                    System.err.println("Map icon for unit " + id + " is null.");
                } else {
                    mapPanel.setIcon(id, icon.image(), MapPanel.SET_ICON_SCALE, east, north, heading);
                }
            }
        }
    }

    public void displayImage(BufferedImage img, int uavIndex, double[] telemetry) {
        // NOTE: Moved GPS corners to public constants at top.

        // Update UAV video feed frame, if applicable
        if (uavIndex >= 0 && uavIndex < videoPanels.length) {
            // Fetching the UAV number went smoothly
            //videoPanels[videoNum].setCurrImage(img);
            //videoPanels[videoNum].myRepaint();
            videoPanels[uavIndex].setImage(img);
            if (SHOW_TELEMETRY_WINDOW) {
                muxFrames[uavIndex].update(Main.actualUavColors[uavIndex].getRGB(), telemetry);
            }
        } else if(videoPanels.length > 0) {
            System.err.println("Image for unit index " + uavIndex + " not displayed in video bank.");
        }
        // Update UAV location on 2D map, if applicable
        if (mapPanel != null && uavIndex >= 0 && uavIndex < Main.uavNames.length) {
            // The camera is facing down so we use the up vector
            // telemetry[6] is direction +x (north)
            // telemetry[7] is direction +y (east)
            // telemetry[8] is direction +z (down)
            // So in the atan2 frame, telemetry[6] is y and telemetry[7] is x
            // Sign flip and +Math.PI/2 are to get into right handed, 0 = north
            //  frame
            double heading = Math.atan2(telemetry[6], telemetry[7]) - Math.PI / 2;
            double[] eastNorth = new double[2];
            gpsToVBS2(telemetry, eastNorth);
            double east = eastNorth[0];
            double north = eastNorth[1];
            Icon icon = Icon.PLAYER;
            try {
                icon = Icon.valueOf(Main.uavNames[uavIndex]);
            } catch (IllegalArgumentException e) {
            }
            if (icon.image() == null) {
                System.err.println("Map icon for unit index " + uavIndex + " is null.");
            } else {
                mapPanel.setIcon(Main.uavNames[uavIndex], icon.image(), MapPanel.SET_ICON_SCALE, east, north, heading);
            }
        } else if(mapPanel != null){
            System.err.println("Map icon for unit index " + uavIndex + " not displayed in mapPanel.");
        }
    }

    public Integer getValue(Integer key) {
        return uavTable.get(key);
    }

    public void handleImage(BufferedImage image) {
        if (!isMultiview) {
            int[] uavColor = Vbs2Utils.parseImageUAVColor(image);
            double[] telem = Vbs2Utils.parseImageTelemetry(image, vbs2gui.SimpleUAVSim.Main.USE_GPS);
            Color roundedColor = new Color((uavColor[0] / 64) * 64, (uavColor[1] / 64) * 64, (uavColor[2] / 64) * 64);
            logger.info(Main.roundedColorToUavName.get(roundedColor)
                    + " POS " + fmt.format(telem[0]) + " " + fmt.format(telem[1]) + " " + fmt.format(telem[2])
                    + " DIR " + fmt.format(telem[3]) + " " + fmt.format(telem[4]) + " " + fmt.format(telem[5])
                    + " UP " + fmt.format(telem[6]) + " " + fmt.format(telem[7]) + " " + fmt.format(telem[8]));

            Integer index = Main.roundedColorToUavIndex.get(roundedColor);
            if (index != null) {
                displayImage(image, index.intValue(), telem);
            } else {
                System.err.println("\tFailed to find rounded color [" + roundedColor.getRed() + ", " + roundedColor.getGreen() + ", " + roundedColor.getBlue() + "] in colorToUavIndex");
            }
        } else {
            int offsetX, offsetY, uavIndex;
            double[] telem;
            BufferedImage croppedImg;
            for (int i = 0; i < numTilesX; i++) {
                for (int j = 0; j < numTilesY && i * numTilesX + j < numTiles; j++) {
                    offsetX = i * CAM_WIDTH;
                    offsetY = j * CAM_HEIGHT;
                    uavIndex = i * numTilesX + j;
                    croppedImg = image.getSubimage(offsetX, offsetY, CAM_WIDTH, CAM_HEIGHT);
                    telem = Vbs2Utils.parseImageTelemetry(croppedImg, vbs2gui.SimpleUAVSim.Main.USE_GPS);
                    logger.info(Main.uavNames[uavIndex]
                            + " POS " + fmt.format(telem[0]) + " " + fmt.format(telem[1]) + " " + fmt.format(telem[2])
                            + " DIR " + fmt.format(telem[3]) + " " + fmt.format(telem[4]) + " " + fmt.format(telem[5])
                            + " UP " + fmt.format(telem[6]) + " " + fmt.format(telem[7]) + " " + fmt.format(telem[8]));
                    displayImage(croppedImg, uavIndex, telem);
                }
            }
        }
    }

    private class VideoPanel extends JPanel {

        ImagePanel jImage = null;
        Color myBG = null;

        public VideoPanel() {
            setLayout(new java.awt.BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new javax.swing.border.LineBorder(new Color(204, 204, 204), 8));

            jImage = new ImagePanel(10, true);
            jImage.setBackground(Color.WHITE);

            add(jImage, java.awt.BorderLayout.CENTER);
        }

        public void setImage(BufferedImage img) {
            jImage.setImage(img);
        }

        @Override
        public void setBackground(Color clr) {
            if (clr == null) {
                clr = Color.GRAY;
            }
            myBG = clr;
            super.setBackground(clr);
            if (clr != null && jImage != null) {
                jImage.setBackground(clr);
            }
        }
    }
}
