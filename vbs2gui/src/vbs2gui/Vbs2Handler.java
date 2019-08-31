/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vbs2gui;

import java.io.File;
import vbs2gui.map.MapPanel;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import vbs2gui.server.Vbs2Link;
import vbs2gui.server.Vbs2Link.MessageEvent;
import vbs2gui.sagat.SagatMission;

/**
 * Contains the translation core of the GUI package, sending out VBS2 commands
 * and interpreting incoming data.
 * @author pkv
 */
public class Vbs2Handler implements Vbs2Link.MessageListener {
    private static final Logger logger = Logger.getLogger(Vbs2Handler.class.getName());
    private MapPanel panel;
    private SagatMission mission;

    static {
        try {
            Icon.playerImage = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/ambulance.png"));
            Icon.ambulanceImage = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/ambulance.png"));
            Icon.hmmwvImage = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/hmmwv.png"));
            Icon.tankImage = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/tank.png"));
            Icon.planeImage = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane.png"));
            Icon.plane0Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane0.png"));
            Icon.plane1Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane1.png"));
            Icon.plane2Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane2.png"));
            Icon.plane3Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane3.png"));
            Icon.plane4Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane4.png"));
            Icon.plane5Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane5.png"));
            Icon.plane6Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane6.png"));
            Icon.plane7Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane7.png"));
            Icon.plane8Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane8.png"));
            Icon.plane9Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane9.png"));
            Icon.plane10Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane10.png"));
            Icon.plane11Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane11.png"));
            Icon.plane12Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane12.png"));
            Icon.plane13Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane13.png"));
            Icon.plane14Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane14.png"));
            Icon.plane15Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane15.png"));
            Icon.plane16Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane16.png"));
            Icon.plane17Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane17.png"));
            Icon.plane18Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane18.png"));
            Icon.plane19Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane19.png"));
            Icon.plane20Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane20.png"));
            Icon.plane21Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane21.png"));
            Icon.plane22Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane22.png"));
            Icon.plane23Image = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/plane23.png"));
            //Icon.goatImage = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/goat-visible.png"));
            Icon.goatImage = ImageIO.read(new File(vbs2gui.SimpleUAVSim.Main.baseFolder + "src/vbs2gui/icons/goat-invisible.png"));
        } catch (IOException e) {
            logger.severe("Unable to load map icons.");
        }
    }

    public Vbs2Handler(MapPanel mp) {
        panel = mp;
    }

    public Vbs2Handler(SagatMission sm) {
        mission = sm;
    }

    public Vbs2Handler(MapPanel mp, SagatMission sm) {
        panel = mp;
        mission = sm;
    }

    public enum Icon {
        PLAYER {
            public BufferedImage image() { return playerImage; }
        },
        AMBUL {
            public BufferedImage image() { return ambulanceImage; }
        },
        HMMWV {
            public BufferedImage image() { return hmmwvImage; }
        },
        TANK {
            public BufferedImage image() { return tankImage; }
        },
        PLANE {
            public BufferedImage image() { return planeImage; }
        },
        PLANE0 {
            public BufferedImage image() { return plane0Image; }
        },
        PLANE1 {
            public BufferedImage image() { return plane1Image; }
        },
        PLANE2 {
            public BufferedImage image() { return plane2Image; }
        },
        PLANE3 {
            public BufferedImage image() { return plane3Image; }
        },
        PLANE4 {
            public BufferedImage image() { return plane4Image; }
        },
        PLANE5 {
            public BufferedImage image() { return plane5Image; }
        },
        PLANE6 {
            public BufferedImage image() { return plane6Image; }
        },
        PLANE7 {
            public BufferedImage image() { return plane7Image; }
        },
        PLANE8 {
            public BufferedImage image() { return plane8Image; }
        },
        PLANE9 {
            public BufferedImage image() { return plane9Image; }
        },
        PLANE10 {
            public BufferedImage image() { return plane10Image; }
        },
        PLANE11 {
            public BufferedImage image() { return plane11Image; }
        },
        PLANE12 {
            public BufferedImage image() { return plane12Image; }
        },
        PLANE13 {
            public BufferedImage image() { return plane13Image; }
        },
        PLANE14 {
            public BufferedImage image() { return plane14Image; }
        },
        PLANE15 {
            public BufferedImage image() { return plane15Image; }
        },
        PLANE16 {
            public BufferedImage image() { return plane16Image; }
        },
        PLANE17 {
            public BufferedImage image() { return plane17Image; }
        },
        PLANE18 {
            public BufferedImage image() { return plane18Image; }
        },
        PLANE19 {
            public BufferedImage image() { return plane19Image; }
        },
        PLANE20 {
            public BufferedImage image() { return plane20Image; }
        },
        PLANE21 {
            public BufferedImage image() { return plane21Image; }
        },
        PLANE22 {
            public BufferedImage image() { return plane22Image; }
        },
        PLANE23 {
            public BufferedImage image() { return plane23Image; }
        },
        UGV {
            public BufferedImage image() { return goatImage; }
        },
        GOAT {
            public BufferedImage image() { return goatImage; }
        };

        public static BufferedImage playerImage = null;
        public static BufferedImage unknownImage = null;
        public static BufferedImage tankImage = null;
        public static BufferedImage ambulanceImage = null;
        public static BufferedImage hmmwvImage = null;
        public static BufferedImage planeImage = null;
        public static BufferedImage plane0Image = null;
        public static BufferedImage plane1Image = null;
        public static BufferedImage plane2Image = null;
        public static BufferedImage plane3Image = null;
        public static BufferedImage plane4Image = null;
        public static BufferedImage plane5Image = null;
        public static BufferedImage plane6Image = null;
        public static BufferedImage plane7Image = null;
        public static BufferedImage plane8Image = null;
        public static BufferedImage plane9Image = null;
        public static BufferedImage plane10Image = null;
        public static BufferedImage plane11Image = null;
        public static BufferedImage plane12Image = null;
        public static BufferedImage plane13Image = null;
        public static BufferedImage plane14Image = null;
        public static BufferedImage plane15Image = null;
        public static BufferedImage plane16Image = null;
        public static BufferedImage plane17Image = null;
        public static BufferedImage plane18Image = null;
        public static BufferedImage plane19Image = null;
        public static BufferedImage plane20Image = null;
        public static BufferedImage plane21Image = null;
        public static BufferedImage plane22Image = null;
        public static BufferedImage plane23Image = null;
        public static BufferedImage goatImage = null;
        public abstract BufferedImage image();
    }

    public void receivedMessage(MessageEvent evt) {
        String message = evt.getMessage();
//            logger.info("received: " + message);
        if (message.startsWith("#ICON")) {
            logger.info("icon message: " + message);
            if(panel == null) {
                return;
            }
            handleIcon(message);
        } else if(message.startsWith("#TGR")) {
            logger.info("sagat message: " + message);
            if(mission == null) {
                return;
            }
            try {
                mission.handleTrigger(message);
            }catch(IOException ioe) {}
        } else if(message.startsWith("#GPS")) {
            logger.info("gps: " + message);
        } else {
//            logger.info("unknown message: " + message);
        }
        if(mission == null) {
//            logger.info("mission is null");
        }
    }

    // #ICON <type> <name> <coords>
    public void handleIcon(String message) {
        String[] args = message.split("\\|");

        if(args[1].contains("PLANE")) {
            // Plane map telemetry is now handled from the telemetry feed
            return;
        }
        Icon icon = Icon.PLAYER;
        try { icon = Icon.valueOf(args[1]); }
        catch(IllegalArgumentException e) {}

        if(args.length >= 5) {
            // Position and heading
            double[] pos = Vbs2Utils.parseArray(args[3]);
            // Positive rotation in VBS2 is clockwise - reverse this
            double heading = -1*Double.parseDouble(args[4])*Math.PI/180.0;
            if (pos[0] > 0.0 && pos[1] > 0.0 && heading <= 0) {
                panel.setIcon(args[2], icon.image(), MapPanel.SET_ICON_SCALE, pos[0], pos[1], heading);
            } else {
                panel.removeIcon(args[2]);
            }
        }
        else {
            // Position
            double[] pos = Vbs2Utils.parseArray(args[3]);
            if (pos[0] > 0.0 && pos[1] > 0.0) {
                panel.setIcon(args[2], icon.image(), 0.125, pos[0], pos[1], 0.0);
            } else {
                panel.removeIcon(args[2]);
            }
        }
    }
}
