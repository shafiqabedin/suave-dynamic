/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author sha33
 */
public class BillboardViewPanel extends JPanel
        implements
        ChangeListener, ActionListener {

    String filePath = "/Users/sha33/NetBeansProjects/vbs2gui/saved/";
    //Set up animation parameters.
    int frameNumber = 0;
    boolean frozen = false;
    //This label uses ImageIcon to show the doggy pictures.
    JLabel picture;
    JLabel txt;
    JPanel sliderPanel = new JPanel();
    JButton curButton = new JButton();
    public double[] location;
    public static int x;
    public static int z;
    public static int[] xcoordinates = {0, 400, 800, 1200, 1600, 2000, 2400, 2800, 3200, 3600, 4000};
    public static int[] zcoordinates = {0, 400, 800, 1200, 1600, 2000, 2400, 2800, 3200, 3600, 4000};
    public static String texIndex = "0x0";
    public static ArrayList<String[]> billboardFileList = new ArrayList();
    public static int billboardFileIndex = 0;
    public static int billboardFileListSize = 0;

    public BillboardViewPanel() {

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));


        //Create the label.
        JLabel sliderLabel = new JLabel("Frames Per Second", JLabel.CENTER);
        sliderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sliderPanel.add(createSlider(billboardFileListSize - 1));

        //Create the label that displays the animation.
        picture = new JLabel();
        picture.setHorizontalAlignment(JLabel.CENTER);
        picture.setAlignmentX(Component.CENTER_ALIGNMENT);
        picture.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        //Add txt to show location
        txt = new JLabel();

        updatePicture(billboardFileIndex); //display first frame


        JPanel buttonPanel = new JPanel();
        GridLayout buttonPanelLayout = new GridLayout(10, 10);
        buttonPanel.setLayout(buttonPanelLayout);


        File inputfile = new File("/Users/sha33/NetBeansProjects/suave/warminster/warminster-button-image.jpg");
        BufferedImage buttonImage = null;
        try {
            buttonImage = ImageIO.read(inputfile);
        } catch (IOException e) {
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Rectangle rect = new Rectangle(50, 50);
                BufferedImage cropped = buttonImage.getSubimage((50 * j), (50 * i), rect.width, rect.height);
                Icon icon = new ImageIcon(cropped);
                JButton jb = new JButton(icon);
                if (texIndex.matches((400 * j) + "x" + (400 * i))) {
                    curButton = jb;
                    jb.setBorder(BorderFactory.createLineBorder(Color.RED));
                } else {
                    jb.setBorder(BorderFactory.createRaisedBevelBorder());
                }
                jb.setActionCommand((400 * j) + "x" + (400 * i));
                jb.addActionListener(this);

                jb.setBorderPainted(true);
                jb.setSize(new Dimension(50, 50));
                buttonPanel.add(jb);
            }
        }

        //Put everything together.
        add(sliderLabel);
        add(sliderPanel);
        add(picture);
        add(txt);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(buttonPanel);

    }

    public JSlider createSlider(final int slidervalue) {
        int changableSliderVal = slidervalue;
        if (slidervalue < 1) {
            changableSliderVal = 1;
        }
        final int changedSliderVal = slidervalue;


        JSlider framesPerSecond;
        //Create the slider.
        framesPerSecond = new JSlider(JSlider.HORIZONTAL,
                0, changableSliderVal, 0);
        framesPerSecond.addChangeListener(this);

        //Turn on labels at major tick marks.
        framesPerSecond.setPreferredSize(new Dimension(500, 50));
        framesPerSecond.setMajorTickSpacing(10);
        framesPerSecond.setMinorTickSpacing(1);
        framesPerSecond.setPaintTicks(true);
        framesPerSecond.setPaintLabels(true);
        framesPerSecond.setBorder(
                BorderFactory.createEmptyBorder(0, 0, 10, 0));

        framesPerSecond.addMouseWheelListener(new MouseWheelListener() {

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                int notches = e.getWheelRotation();
                JSlider source = (JSlider) e.getSource();
                if (notches < 0 && changedSliderVal <=  slidervalue) {
                    source.setValue(source.getValue() + 1);
                } else if(changedSliderVal >=  slidervalue) {
                    source.setValue(source.getValue() - 1);
                }
            }
        });
        return framesPerSecond;

    }

    public void createAndShowBillboard(double[] loc) {
        location = loc;
        billboardFileIndex = 0;
        billboardFileListSize = 0;
        translateGpltoTexmap();
        billboardFileList.clear();
        getImageIndex();
        //Create and set up the window.
        JFrame frame = new JFrame("Billboard");
        BillboardViewPanel animator = new BillboardViewPanel();

        //Add content to the window.
        frame.add(animator, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    /** Update the label to display the image for the current frame. */
    public void updatePicture(int frameNum) {

        if (frameNum < billboardFileList.size()) {
            String[] filename = billboardFileList.get(frameNum);
            File inputfile = new File("/Users/sha33/NetBeansProjects/vbs2gui/saved/" + filename[1]);
            if (inputfile.exists()) {
                BufferedImage uncropped = null;
                BufferedImage scaled = null;

                try {
                    uncropped = ImageIO.read(inputfile);
                } catch (IOException e) {
                }
                AffineTransform at = new AffineTransform();
                at.scale(1.5, 1.5);
                AffineTransformOp scaleOp =
                        new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
                scaled = scaleOp.filter(uncropped, scaled);
                picture.setIcon(new ImageIcon(scaled));
//                txt.setText("Showing now: " + billboardFileList.get(frameNum));

            } else {
                picture.setText("image #" + billboardFileList.get(frameNum) + " does not exist!");
            }
        }
    }

    public void getImageIndex() {
        String[] temp;
        String delimiter = "_";

        File file = new File("/Users/sha33/NetBeansProjects/vbs2gui/saved");
        File[] files = file.listFiles();
        for (int fileInList = 0; fileInList < files.length; fileInList++) {
            temp = files[fileInList].getName().toString().split(delimiter);
            if (temp[1].equals(texIndex + ".png") && Integer.parseInt(temp[0]) <= Baker.imageIdx) {
                billboardFileList.add(new String[]{temp[0].toString(), files[fileInList].getName().toString()});
//                billboardFileList.add(files[fileInList].getName().toString());
                billboardFileListSize++;
            }
        }
        //sort the list
        Collections.sort(billboardFileList, new Comparator<String[]>() {

            @Override
            public int compare(String[] o1, String[] o2) {
                // TODO tweak the comparator here
                try {
                    Integer integer1 = Integer.valueOf(o1[0]);
                    Integer integer2 = Integer.valueOf(o2[0]);
                    return integer1.compareTo(integer2);
                } catch (java.lang.NumberFormatException e) {
                    return o1[1].compareTo(o2[1]);
                }
            }
        });
    }

    public void translateGpltoTexmap() {


        int half = 2048;
        x = half + (int) location[0] + 100;
        z = half + (int) location[2] + 200;

        x = (int) (x - (Math.log10(x) * 105));
        z = (int) (z - (Math.log10(z) * 105));

        if (location[0] < 0) {
            x = x + 400;
        }
        if (location[2] > 0) {
            z = z - 250;
        }
//        z = 4096 - z;
//
//
//            // Hack Hack Hack Hack - just like Sean
//            if (location[0] < 0 && location[2] > 0) {
//                x = half + (int) location[0] + 100;
//                z = half + (int) location[2] - 500;
//            } else if (location[0] > 0 && location[2] > 0) {
//                x = half + (int) location[0] + 100;
//                z = half + (int) location[2] - 500;
//            } else if (location[0] > 0 && location[2] < 0) {
//                x = half + (int) location[0] + 100;
//                z = half + (int) location[2] - 500;
//            } else if (location[0] < 0 && location[2] < 0) {
//                x = half + (int) location[0] + 100;
//                z = half + (int) location[2] - 500;
//            } else {
//                x = half + (int) location[0] + 100;
//                z = half + (int) location[2] - 500;
//            }
//        if (x < 0) {
//            x = 0;
//        }
//        if (x > 3595) {
//            x = 3595;
//        }
//        if (z < 0) {
//            z = 0;
//        }
//        if (z > 3595) {
//            z = 3595;
//        }


        int ctr;
        //For all 4 coordinates we want to find out which segments we want o save.
        int tmpX = 0, tmpZ = 0;

        for (ctr = 0; ctr < xcoordinates.length; ctr++) {
            if ((x) > xcoordinates[ctr]) {
                tmpX = xcoordinates[ctr];
            }
        }
        for (ctr = 0; ctr < zcoordinates.length; ctr++) {
            if ((z) > zcoordinates[ctr]) {
                tmpZ = zcoordinates[ctr];
            }
        }
        texIndex = tmpX + "x" + tmpZ;
    }

    public void actionPerformed(ActionEvent e) {
        JButton button = (JButton) e.getSource();
        button.setBorder(BorderFactory.createLineBorder(Color.RED));
        curButton.setBorder(BorderFactory.createRaisedBevelBorder());
        curButton = button;
        texIndex = e.getActionCommand();
        billboardFileIndex = 0;
        billboardFileListSize = 0;
        billboardFileList.clear();
        getImageIndex();
//        txt.setText("Size: " + billboardFileListSize);
        sliderPanel.removeAll();
        sliderPanel.revalidate();
        sliderPanel.add(createSlider(billboardFileListSize - 1));
        updatePicture(billboardFileIndex);
        repaint();

    }

    public void stateChanged(ChangeEvent ce) {
        JSlider source = (JSlider) ce.getSource();
        int fps = (int) source.getValue();
        updatePicture(fps);
    }
}
