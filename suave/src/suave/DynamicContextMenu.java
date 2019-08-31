/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.util.HashMap;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author sha33
 */
public class DynamicContextMenu extends JPopupMenu {

    JMenuItem menuItem;
    Path2D path = new Path2D.Double();

    public DynamicContextMenu(double[] lvcs, double[] gps, double[] ogl, SelectEvent se) {
        double quadXYZ[][] = new double[4][2];
        quadXYZ[0][0] = -2181.6196;
        quadXYZ[0][1] = 1170.6781;
        //
        quadXYZ[1][0] = -2138.062;
        quadXYZ[1][1] = 1137.1196;
        //
        quadXYZ[2][0] = -2073.533;
        quadXYZ[2][1] = 1202.389;
        //
        quadXYZ[3][0] = -2107.602;
        quadXYZ[3][1] = 1215.0364;



        path.moveTo(quadXYZ[0][0], quadXYZ[0][1]);
        for (int i = 0; i < quadXYZ.length; i++) {
            path.lineTo(quadXYZ[i][0], quadXYZ[i][1]);
        }
        String str = new String();
        if (path.contains(ogl[0], ogl[2])) {
            str = "Found!";
        } else {
            str = "Not Found!";
        }
        menuItem = new JMenuItem("Click Me: " + str);
        add(menuItem);

        //adding action listener to menu items
        menuItem.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        System.out.println("New is pressed");
                    }
                });

    }
}
