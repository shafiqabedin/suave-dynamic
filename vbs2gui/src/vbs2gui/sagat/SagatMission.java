/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vbs2gui.sagat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.LineBorder;
import vbs2gui.SimpleUAVSim.Main;
import vbs2gui.SimpleUAVSim.Main.ExperimentType;
import vbs2gui.SimpleUAVSim.Main.WPType;
import vbs2gui.SimpleUAVSim.UAV;
import vbs2gui.sagat.panels.DialPanel;
import vbs2gui.server.Vbs2Link;
import vbs2gui.video.ImageReplay;

/**
 *
 * @author nbb
 */
public class SagatMission implements Runnable {

    Logger logger = Logger.getLogger(SagatMission.class.getName());
    int waypointIndex, roundNum, numUAVs, numUGVs, numAnimals, numAnimalsPerHerd,
            numHerds, numWindowsLeft;
    long roundStartTime;
    final int[][] timePerRound = new int[][] {{4 * 60000, 5 * 60000, 5 * 60000},    // Waypoint set 1 sagat intervals
                                              {5 * 60000, 4 * 60000, 4 * 60000},    // Waypoint set 2 sagat intervals
                                              null};                                // Waypoint set 3 sagat intervals
    boolean[] activeDangerZones;
    boolean[][] roundDangerZones;
    DangerMap dm;
    int[][] uavPatrols;
    int[][][] herdWaypoints;
    JFrame sagatFrame, mapFrame;
    SagatQuestion sq;
    UAV[] uavs;
    Vbs2Link link;
    ArrayList<JFrame> listenerList;
    ArrayList<DialPanel> listenerDialList;
    ExperimentType experimentType;
    DialPanel[] dialPanels;
    private JLabel timerLabel;
    private Timer countdownTimer;
    int timeRemaining = 30;

    public SagatMission(Vbs2Link link, int waypointSet, boolean[][] roundDangerZones,
            int[][] uavPatrols, int[][][] herdWaypoints, UAV[] uavs,
            ExperimentType experimentType) {
        this.link = link;
        this.waypointIndex = waypointSet - 1;
        this.numUAVs = vbs2gui.SimpleUAVSim.Main.NUM_UAVS;
        this.numUGVs = vbs2gui.SimpleUAVSim.Main.NUM_UGVS;
        this.numAnimals = vbs2gui.SimpleUAVSim.Main.NUM_ANIMALS;
        this.numAnimalsPerHerd = vbs2gui.SimpleUAVSim.Main.NUM_ANIMALS_PER_HERD;
        this.numHerds = vbs2gui.SimpleUAVSim.Main.NUM_HERDS;
        this.roundDangerZones = roundDangerZones;
        this.uavPatrols = uavPatrols;
        this.herdWaypoints = herdWaypoints;
        this.uavs = uavs;
        this.experimentType = experimentType;
        roundNum = 1;
        activeDangerZones = new boolean[roundDangerZones[0].length];
        listenerList = new ArrayList<JFrame>();
        listenerDialList = new ArrayList<DialPanel>();

    }

    public void run() {
        if (null == link) {
            return;
        }

        roundStartTime = System.currentTimeMillis();
        logger.info("Started round " + roundNum + " at " + roundStartTime);
        String[] cmds;
        // Set uav loiter waypoints
        // Add new waypoints
        for (int i = 0; i < numUAVs; i++) {
            if (uavPatrols != null) {
                cmds = doNewWPCmds(uavs, i, true, uavPatrols[i][0], uavPatrols[i][1], uavPatrols[i][2], WPType.LOITER);
                for (String s : cmds) {
                    link.evaluate(s);
                }
            }
        }
        // Set ugv waypoints
        for (int i = 0; i < numUGVs; i++) {
            cmds = doNewWPCmds(uavs, (numUAVs + i), true, herdWaypoints[i][roundNum][0], herdWaypoints[i][roundNum][1], 0, WPType.MOVE);
            for (String s : cmds) {
                link.evaluate(s);
            }
        }
        // Set herd waypoints
        for (int i = 0; i < numHerds; i++) {
            cmds = doNewWPCmds(uavs, (numUAVs + numUGVs + i * numAnimalsPerHerd), true, herdWaypoints[i][roundNum][0], herdWaypoints[i][roundNum][1], 0, WPType.MOVE);
            for (String s : cmds) {
                link.evaluate(s);
            }
        }

        // Set up spiral waypoints
//        (new Thread() {
//
//            public void run() {
//                spiralWaypoints(link, uavs, uavPatrols);
//            }
//        }).start();
    }

    public void handleTrigger(String message) throws IOException {
        //TGR|<trigger location>|<trigger is now on/off>|<number of units activating trigger>
        String[] messages = message.split("\\|");
        if (messages.length != 4) {
            return;
        }
        boolean active = messages[2].equals("ON") ? true : false;
        if (messages[1].equals("PARKING")) {
            activeDangerZones[0] = active;
        } else if (messages[1].equals("FOREST1")) {
            activeDangerZones[1] = active;
        } else if (messages[1].equals("FOREST2")) {
            activeDangerZones[2] = active;
        } else if (messages[1].equals("HOUSING")) {
            activeDangerZones[3] = active;
        } else if (messages[1].equals("AIRPORT")) {
            activeDangerZones[4] = active;
        }

        // Check if we are ready to pause
        boolean pause = true;
        for (int i = 0; i < activeDangerZones.length && roundNum <= roundDangerZones.length; i++) {
            if (activeDangerZones[i] != roundDangerZones[roundNum - 1][i]) {
                pause = false;
                break;
            }
        }
        if (pause) {
            new Thread(new SagatPause()).start();
        }
    }

    public void pauseSimulation(ImageReplay imageReplay) {
        logger.info("SAGAT " + roundNum + " triggered at " + System.currentTimeMillis());
        if (Main.guiType != Main.GUIType.RECORD) {
            numWindowsLeft = 0;
            if (null != link) {
                // Pause VBS2
                link.evaluate("pauseSimulation true; ");
            }
            // Hide windows
            for (JFrame frame : listenerList) {
                frame.setVisible(false);
            }

            WindowListener listener = new WindowAdapter() {

                public void windowOpened(WindowEvent evt) {
                    JFrame frame = (JFrame) evt.getSource();
                    numWindowsLeft++;
                }

                public void windowClosed(WindowEvent evt) {
                    JFrame frame = (JFrame) evt.getSource();
                    numWindowsLeft--;
                }
            };
            // Get Timer countdown
            countdownTimer = new Timer(1000, new CountdownTimerListener());
            timerLabel = new JLabel(String.valueOf(timeRemaining), JLabel.CENTER);
            //    countdownTimer.start();
            // Pop up random SAGAT question
            sq = new SagatQuestion(experimentType, roundNum);
            sq.setMinimumSize(new Dimension(600, 500));
            sagatFrame = new JFrame("Question Set 1");
            sagatFrame.setMinimumSize(new Dimension(600, 600));
            sagatFrame.setPreferredSize(new Dimension(600, 600));
            sagatFrame.addWindowListener(listener);
            sagatFrame.setDefaultCloseOperation(JFrame.NORMAL);
            sagatFrame.getContentPane().add(sq, BorderLayout.CENTER);
            JButton sagatDone = new JButton("Done");
            sagatDone.setMinimumSize(new Dimension(150, 150));
            sagatDone.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    logger.info("Done with SAGAT question.");
                    sq.saveForm();
                    sagatFrame.dispose();
                }
            });
            JPanel buttonPanel = new JPanel(new BorderLayout());
            buttonPanel.add(timerLabel, BorderLayout.PAGE_START);
            buttonPanel.add(sagatDone, BorderLayout.PAGE_END);
            //    sagatFrame.add(sagatDone, BorderLayout.SOUTH);
            sagatFrame.add(buttonPanel, BorderLayout.SOUTH);
            sagatFrame.pack();
            sagatFrame.setLocationRelativeTo(null);
            //    sagatFrame.setVisible(true);

            // Danger zone question
            dm = new DangerMap();
            dm.setMinimumSize(new Dimension(500, 500));
            dm.setPreferredSize(new Dimension(500, 500));
            dm.setBorder(new LineBorder(Color.BLACK));
            mapFrame = new JFrame("Question Set 2");
            mapFrame.setMinimumSize(new Dimension(600, 600));
            mapFrame.setPreferredSize(new Dimension(600, 600));
            mapFrame.add(dm, BorderLayout.CENTER);
            mapFrame.addWindowListener(listener);
            mapFrame.setDefaultCloseOperation(JFrame.NORMAL);
            JButton dangerDone = new JButton("Done");
            dangerDone.setMinimumSize(new Dimension(150, 150));
            dangerDone.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    logger.info("Done with danger zone question.");
                    dm.saveForm();
                    mapFrame.dispose();
                    // Start after the operator is done answering the dangermap q's.
                    sagatFrame.setVisible(true);
                    countdownTimer.start();
                    for (DialPanel dp : listenerDialList) {
                        double[] dialPanelValues = dp.getDialPanelValues();
                        logger.info("SAGAT Dial Panel Value of " + dp.getOwner() + " are: Fuel [" + dialPanelValues[0] + "] | Battery [" + dialPanelValues[1] + "] Temperature [" + dialPanelValues[2] + "]");
                    }


                }
            });
            mapFrame.add(dangerDone, BorderLayout.SOUTH);
            mapFrame.pack();
            mapFrame.setLocationRelativeTo(null);
            mapFrame.setVisible(true);
            try {
                //            File imgFile = new File("/Users/sha33/suave/src/vbs2gui/icons/warminster-danger-small.jpg");
                File imgFile = new File(vbs2gui.SimpleUAVSim.Main.baseFolder + vbs2gui.SimpleUAVSim.Main.AERIAL_MAP);
                System.out.println("Path to aerial map: " + imgFile.getAbsolutePath());
                BufferedImage img = ImageIO.read(imgFile);
                dm.setMapImage(img);
                dm.setMapRect(0, 0, 5000, 5000); // Warminster bounds
                dm.repaint();
            } catch (IOException e) {
                System.err.println("Failed to load image.");
            }
            while (dm.isPaintingTile()) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // Sleep for listener to register events
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
            }

            while (numWindowsLeft > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
            }
        }
        roundNum++;
        roundStartTime = System.currentTimeMillis();
        if (roundNum > Main.numRoundsPerMission) {
            System.exit(0);
        }
        logger.info("Started round " + roundNum + " at " + roundStartTime);
        if (null != imageReplay) {
            imageReplay.setPaused(false);
        }
        resumeSimulation();
    }

    public void resumeSimulation() {

        String[] cmds;
        if (Main.guiType != Main.GUIType.RECORD) {
            // Unhide frames
            for (JFrame frame : listenerList) {
                frame.setVisible(true);
            }
            if (null == link) {
                return;
            }
            // Unpause VBS2
            link.evaluate("pauseSimulation false; ");
        }
        // Update herd waypoints

        for (int i = 0; i < numUGVs; i++) {
            cmds = doCompleteWPCmds(uavs, numUAVs + i);
            for (String s : cmds) {
                link.evaluate(s);
            }
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ie) {
        }
        for (int i = 0; i < numUGVs; i++) {
            cmds = doNewWPCmds(uavs, (numUAVs + i), true, herdWaypoints[i][roundNum][0], herdWaypoints[i][roundNum][1], 0, WPType.MOVE);
            for (String s : cmds) {
                link.evaluate(s);
            }
        }
        for (int i = 0; i < numHerds; i++) {
            cmds = doNewWPCmds(uavs, (numUAVs + numUGVs + i * numAnimalsPerHerd), true, herdWaypoints[i][roundNum][0], herdWaypoints[i][roundNum][1], 0, WPType.MOVE);
            for (String s : cmds) {
                link.evaluate(s);
            }
        }
    }

    public void addSagatListener(JFrame frame) {
        listenerList.add(frame);
    }

    public void addSagatDialListener(DialPanel dialpanel) {
        listenerDialList.add(dialpanel);
    }

    public String[] doCompleteWPCmds(UAV[] uavs, int id) {
        String[] cmds = new String[]{
            "[] spawn { "
            + "[" + uavs[id].getGroupName() + ", (waypointCurrent " + uavs[id].getGroupName() + ")] setWPPos (getPos " + uavs[id].getUavName() + "); "
            + "sleep 1; "
            + "}; "};
        return cmds;
    }

    public String[] doNewWPCmds(UAV[] uavs, int id, boolean fuel, double x, double y, double r, WPType type) {
        // Create a new WP and switch to it
        String[] cmds;
        if (uavs[id].getType() == UAV.Type.UAV || uavs[id].getType() == UAV.Type.UGV) {
            if (type == WPType.MOVE) {
                cmds = new String[]{
                            "[] spawn { "
                            + uavs[id].getGroupName() + " addWaypoint [[" + x + "," + y + ", 0], " + r + "]; "
                            + "sleep 1; "
                            + "[" + uavs[id].getGroupName() + ", (nWaypoints " + uavs[id].getGroupName() + ") - 1] setWaypointType \"MOVE\"; "
                            //                            + uavs[id].getGroupName() + " setCurrentWaypoint [" + uavs[id].getGroupName() + ", (nWaypoints " + uavs[id].getGroupName() + ") - 1]; "
                            + uavs[id].getUavName() + " setFuel " + ((fuel) ? 1 : 0) + "; "
                            + "}; "};
            } else if (type == WPType.LOITER) {
                cmds = new String[]{
                            "[] spawn { "
                            + uavs[id].getGroupName() + " addWaypoint [[" + x + "," + y + ", 0], 0]; "
                            + "sleep 1; "
                            + "[" + uavs[id].getGroupName() + ", (nWaypoints " + uavs[id].getGroupName() + ") - 1] setWaypointType \"LOITER\"; "
                            + "[" + uavs[id].getGroupName() + ", (nWaypoints " + uavs[id].getGroupName() + ") - 1] setWaypointLoiterRadius " + r + "; "
                            //                            + uavs[id].getGroupName() + " setCurrentWaypoint [" + uavs[id].getGroupName() + ", (nWaypoints " + uavs[id].getGroupName() + ") - 1]; "
                            + uavs[id].getUavName() + " setFuel " + ((fuel) ? 1 : 0) + "; "
                            + "}; "};
            } else {
                cmds = new String[]{"; "};
            }
        } else if (uavs[id].getType() == UAV.Type.ANIMAL) {
            cmds = new String[]{
                        "[] spawn { "
                        + uavs[id].getGroupName() + " addWaypoint [[" + x + "," + y + ", 0], 0]; "
                        + "sleep 1; "
                        + "[" + uavs[id].getGroupName() + ", (nWaypoints " + uavs[id].getGroupName() + ") - 1] setWaypointType \"MOVE\"; "
                        //                        + uavs[id].getGroupName() + " setCurrentWaypoint [" + uavs[id].getGroupName() + ", (nWaypoints " + uavs[id].getGroupName() + ") - 1]; "
                        + "}; "};
        } else {
            cmds = new String[]{"; "};
        }
        return cmds;
    }

    private void spiralWaypoints(Vbs2Link link, UAV[] uavs, int[][] uavPatrols) {
        String[] cmds;
        int loiterTime = 60 * 1000;
        int xTarget = 2055;
        int yTarget = 2513;
        int minRadius = 500;
        int maxRadius = 1750;
        int stepRadius = 100;
        int uavSpacing = (int) ((maxRadius - minRadius) / numUAVs);
        int currentRadius = minRadius;

        while (true) {
            try {
                Thread.sleep(loiterTime);
            } catch (InterruptedException ie) {
            }

            // Cycles UAVs through loiter waypoint patrol radii
            if (uavPatrols != null) {
                for (int i = 0; i < numUAVs; i++) {
                    int radius = currentRadius + i * uavSpacing;
                    if (radius > maxRadius) {
                        radius -= (maxRadius - minRadius);
                    }
                    cmds = doNewWPCmds(uavs, i, true, xTarget, yTarget, radius, WPType.LOITER);
                    for (String cmd : cmds) {
                        link.evaluate(cmd);
                    }
                }
            }
            currentRadius += stepRadius;
        }
    }

    private class CountdownTimerListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (--timeRemaining > 0) {
                timerLabel.setText("Time Left: " + String.valueOf(timeRemaining));
            } else {
                timerLabel.setText("Time's up!");
                countdownTimer.stop();
            }
        }
    }

    private class SagatPause implements Runnable {

        public void run() {
            long startTime = System.currentTimeMillis();
            logger.info("Start SAGAT sleep for round " + roundNum + " at: " + startTime);
            // Sleep until the round is up
            try {
                Thread.sleep(Math.max(0, timePerRound[waypointIndex][roundNum - 1] - (startTime - roundStartTime)));
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            long endTime = System.currentTimeMillis();
            logger.info("End SAGAT sleep: " + endTime + ": elapsed time:" + (endTime - startTime));
            pauseSimulation(null);
        }
    }
}
