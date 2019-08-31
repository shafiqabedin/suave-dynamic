/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author sha33
 */
public class FloatingBillboardViewPanel extends JPanel {

    private JLabel picture = new JLabel();
    private JPanel picturePanel = new JPanel();
    private JPopupMenu popupmenu;
    private int gplToImgIdx_x = 0;
    private int gplToImgIdx_y = 0;
    private double[] oglPosition;
    private LinkedHashMap<Integer, BufferedImage> imageList = new LinkedHashMap<Integer, BufferedImage>();

    public FloatingBillboardViewPanel(final JPopupMenu popupmenu, double[] oglPosition) throws IOException {

        this.setPreferredSize(new Dimension(600, 600));
        this.popupmenu = popupmenu;
        this.oglPosition = oglPosition;
        //update the location
        translateGpltoTexmap();
        getImageList();

//use global mouse event capture to disable left click on anything when popup is visible
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

            @Override
            public void eventDispatched(AWTEvent event) {
                MouseWheelEvent me = (MouseWheelEvent) event;
                if (me.getID() == MouseEvent.MOUSE_WHEEL) {
                    if (popupmenu.isVisible()) {
                        me.consume();
                        int notches = me.getWheelRotation();
                        JSlider source = (JSlider) picturePanel.getComponent(0);
                        source.setValue(source.getValue() + notches);
                    }
                }
            }
        }, AWTEvent.MOUSE_WHEEL_EVENT_MASK);

        setLayout(new BorderLayout());
        picturePanel.setLayout(new BorderLayout());
        //updatePicturePanel
        updatePicturePanel();
        updatePicture(0);
        //Add conponents to the Main Panel
        add(picturePanel, BorderLayout.CENTER);
    }

    private JSlider createSlider(final int slidervalue) {
        int changableSliderVal = slidervalue;
        if (slidervalue < 1) {
            changableSliderVal = 1;
        }
        final int changedSliderVal = slidervalue;
        JSlider frameSlider;
        frameSlider = new JSlider(JSlider.HORIZONTAL, 0, changableSliderVal, 0);
        //Turn on labels at major tick marks.
        frameSlider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent ce) {
                JSlider source = (JSlider) ce.getSource();
                updatePicture(source.getValue());
            }
        });
        frameSlider.setPreferredSize(new Dimension(this.getWidth(), 50));
        frameSlider.setMajorTickSpacing(10);
        frameSlider.setMinorTickSpacing(1);
        frameSlider.setPaintTicks(true);
        frameSlider.setPaintLabels(true);
        frameSlider.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return frameSlider;

    }

    private void updatePicturePanel() {
        picturePanel.removeAll();
        picturePanel.add(createSlider(imageList.size()), BorderLayout.PAGE_START);
        picturePanel.add(picture, BorderLayout.PAGE_END);
        picturePanel.repaint();
    }

    private void updatePicture(int imageidx) {
        if (imageList.size() > 0) {
            AffineTransform at = new AffineTransform();
            BufferedImage scaled = null;
            at.scale(1.5, 1.5);
            AffineTransformOp scaleOp =
                    new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
            scaled = scaleOp.filter(imageList.get(imageidx), scaled);
            picture.setIcon(new ImageIcon(scaled));
        }
    }

    public void getImageList() throws IOException {
        String[] temp;
        String delimiter = "_";
        imageList.clear();
        //Get the image name
        int[] xcoordinates = {0, 400, 800, 1200, 1600, 2000, 2400, 2800, 3200, 3600, 4000};
        int[] zcoordinates = {0, 400, 800, 1200, 1600, 2000, 2400, 2800, 3200, 3600, 4000};
        String texIndex = "0x0";
        int ctr;
        //For all 4 coordinates we want to find out which segments we want o save.
        int tmpX = 0, tmpZ = 0;

        for (ctr = 0; ctr < xcoordinates.length; ctr++) {
            if ((gplToImgIdx_x) > xcoordinates[ctr]) {
                tmpX = xcoordinates[ctr];
            }
        }
        for (ctr = 0; ctr < zcoordinates.length; ctr++) {
            if ((gplToImgIdx_y) > zcoordinates[ctr]) {
                tmpZ = zcoordinates[ctr];
            }
        }
        texIndex = tmpX + "x" + tmpZ;
        String tmp = "";

        File file = new File("/Users/sha33/NetBeansProjects/vbs2gui/saved");
        File[] files = file.listFiles();
        List<Integer> hashIdx = new ArrayList();
        LinkedHashMap<Integer, BufferedImage> tmpList = new LinkedHashMap<Integer, BufferedImage>();
        int imageIdx = 0;
        for (int fileInList = 0; fileInList < files.length; fileInList++) {
            temp = files[fileInList].getName().toString().split(delimiter);
            if (temp[1].equals(texIndex + ".png") && Integer.parseInt(temp[0]) <= Baker.imageIdx) {
                tmp += files[fileInList].getName().toString() + "\n";
                tmpList.put(Integer.parseInt(temp[0]), ImageIO.read(files[fileInList]));
                hashIdx.add(Integer.parseInt(temp[0]));
                imageIdx++;

            }
        }
        Collections.sort(hashIdx);
        for (ctr = 0; ctr < hashIdx.size(); ctr++) {
            imageList.put(ctr, tmpList.get(hashIdx.get(ctr)));
        }
    }

    private void translateGpltoTexmap() {
        int xPos = (int) ((2500 + oglPosition[0]) * 0.82);
        int YPos = (int) ((2500 + oglPosition[2]) * 0.82);
        gplToImgIdx_x = xPos;
        gplToImgIdx_y = YPos;
    }
}
