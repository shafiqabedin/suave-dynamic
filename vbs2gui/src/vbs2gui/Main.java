/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vbs2gui;

import vbs2gui.map.MapPanel;
import vbs2gui.map.ExperimentPanel;
import vbs2gui.server.Vbs2Link;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.border.LineBorder;

/**
 *
 * @author pkv
 */
public class Main {
    static BufferedImage img;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        final MapPanel mp = new MapPanel();
        final ExperimentPanel ep = new ExperimentPanel();

        mp.setPreferredSize(new Dimension(600, 600));
        mp.setBorder(new LineBorder(Color.BLACK));
        ep.setPreferredSize(new Dimension(200, 600));

        JFrame frame = new JFrame();
        frame.getContentPane().add(mp, BorderLayout.CENTER);
        frame.getContentPane().add(ep, BorderLayout.EAST);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        
        try {
            img = ImageIO.read(new File(".\\Intro.png"));
            mp.setMapImage(img);
            //panel.setMapRect(-2570.0, 7690.0, 6965.99, -1853.21); // Rahmadi bounds
            mp.setMapRect(0.0, 5114.0, 5128.0, 0.0); // Rahmadi bounds
            mp.repaint();
        } catch (IOException e) {
            System.err.println("Failed to load image.");
        }

        Vbs2Link link = new Vbs2Link();
        link.addMessageListener(new Vbs2Handler(mp));

        // null = this spawn { while {true} do { sleep 0.250; pluginFunction ["TcpBridge", format [#ICON|TANK|%1|%2", _this, position _this]]; }; };

        link.connect("krasato.cimds.ri.cmu.edu");

        ep.setLink(link);
        
        while(frame.isVisible()) {
            try {Thread.sleep(500);} catch (Exception e) {}
            //link.send("format [\"#ICON PLAYER %1 %2\", player, position player]");
        }
        link.disconnect();
    }

}
