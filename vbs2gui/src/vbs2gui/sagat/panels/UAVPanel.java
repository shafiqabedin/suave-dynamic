/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package vbs2gui.sagat.panels;

import java.awt.Color;
import javax.swing.JPanel;

/**
 *
 * @author nbb
 */
public class UAVPanel extends JPanel {
        Color myBG = Color.gray;
        DialPanel panel = null;

        public UAVPanel() {
            setLayout(new java.awt.BorderLayout());
            setBackground(Color.WHITE);
            setBorder(new javax.swing.border.LineBorder(new Color(204, 204, 204), 8));

            panel = new DialPanel();

            add(panel, java.awt.BorderLayout.CENTER);
        }

        public void setBackground(Color clr) {
            if (clr != null) {
                myBG = clr;
            }
            super.setBackground(myBG);
        }

        public static void main(String[] args) {
            
        }
}
