/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vbs2gui.sagat.panels;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author sha33
 */
public class PanelRunner extends JFrame {

  private DialPanel left = new DialPanel();

  public PanelRunner() {
    JPanel content = new JPanel();
    content.setLayout(new BorderLayout(5, 5));
    content.add(left, BorderLayout.WEST);

    //... Set window characteristics
    setContentPane(content);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setTitle("Demo Drawing");
    setLocationRelativeTo(null);  // Center window.
    pack();
    validate();
    repaint();
  }

  public static void main(String[] args) {
    JFrame window = new PanelRunner();
    window.setVisible(true);
  }
}
