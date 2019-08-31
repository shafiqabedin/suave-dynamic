/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 *
 * @author sha33
 */
public class AffineTransformer {

    String filePath = "/Users/sha33/NetBeansProjects/vbs2gui/saved/";
    double[] affineMatrix = null;
    JFrame affineFrame = new JFrame();
    JPanel affinePanel = new JPanel();
    JLabel affineLabel = new JLabel();
    JTextArea affineLabelLog = new JTextArea("Processing Please Wait...\n");
    boolean isreference = false;
    int pictureindex;

    public void getAffineTransformation(final ArrayList<String> filelist, String reffilename) {
        pictureindex = 0;
        //Initialize frames and add panel
        affineFrame.setSize(new Dimension(700, 500));

        //Buttons
        JButton jbnLeft = new JButton("<");
        //Add action listener to button
        jbnLeft.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (filelist.get(pictureindex - 1) != null) {
                    affineLabel.setIcon(new ImageIcon(filePath + filelist.get(--pictureindex) + "_affine.jpg"));
                }
            }
        });

        JButton jbnRight = new JButton(">");
        jbnRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (filelist.get(pictureindex + 1) != null) {
                    affineLabel.setIcon(new ImageIcon(filePath + filelist.get(++pictureindex) + "_affine.jpg"));
                }
            }
        });

        affinePanel.add(affineLabel, BorderLayout.PAGE_START);
        affinePanel.add(jbnLeft, BorderLayout.LINE_START);
        affinePanel.add(jbnRight, BorderLayout.LINE_END);
        affinePanel.add(affineLabelLog, BorderLayout.PAGE_END);

        affineFrame.add(affinePanel);
        affineFrame.setVisible(true);

        affineLabelLog.append("Reference Image: " + reffilename + "\n");



        //Convert the reference image into nifti format
        try {
            isreference = true;
            writeNifti(reffilename, reffilename);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }


        for (String filename : filelist) {
            affineLabelLog.append("Processing Now: " + filename + "\n");
            //Convert the reference image into nifti format
            try {
                isreference = false;
                writeNifti(filename, reffilename);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                try {
                    //Create script for execution
                    try {
                        BufferedWriter out = new BufferedWriter(new FileWriter(filePath + "shellscript.sh"));
                        out.write("#!/bin/bash\n");
                        out.write("FSLDIR=/usr/local/fsl\n");
                        out.write("PATH=${FSLDIR}/bin:${PATH}\n");
                        out.write(". ${FSLDIR}/etc/fslconf/fsl.sh\n");
                        out.write("export FSLDIR PATH\n");

                        out.write("/usr/local/fsl/bin/flirt -in " + filePath + filename + ".hdr -ref " + filePath + reffilename + "_ref.hdr -omat " + filePath + "omat.mat -2D -nosearch\n");
                        out.close();
                    } catch (IOException e) {
                    }
                    runAfni("sh " + filePath + "shellscript.sh");
                    affineMatrix = getAffineArray("omat.mat");
                    applyAffine(filename);
//                    garbageDisposer(filename);

                } catch (InterruptedException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

    }
    /**
     * @param args the command line arguments
     */
    public boolean littleEndian = false;
    short bitsallocated, datatype;
    int qform_code = 0;
    int sform_code = 0;

    public void writeNifti(String filename, String reffilename) throws FileNotFoundException, IOException {

        File rinputfile = new File(filePath + filename + ".jpg");
        BufferedImage rimg = null;

        // Reference file name
        File refinputfile = new File(filePath + reffilename + ".jpg");
        BufferedImage refimg = null;

        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);


        try {
            rimg = ImageIO.read(rinputfile);
            refimg = ImageIO.read(refinputfile);
        } catch (IOException e) {
        }

        // Copy the ideal header to destination image header.
        File f1 = new File(filePath + "PLANE-IDEAL.hdr");
        File f2;
        if (isreference == true) {
            f2 = new File(filePath + filename + "_ref.hdr");
        } else {
            f2 = new File(filePath + filename + ".hdr");
        }
        InputStream in = new FileInputStream(f1);

        //For Overwrite the file.
        OutputStream out = new FileOutputStream(f2);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        String hdrFile = "";
        if (isreference == true) {
            hdrFile = filePath + filename + "_ref.img";
        } else {
            hdrFile = filePath + filename + ".img";
        }

        FileOutputStream stream = new FileOutputStream(hdrFile);
        DataOutputStream output = new DataOutputStream(stream);

        //Write Image data
        int w = rimg.getWidth();
        int h = rimg.getHeight();
        byte[] g;

        g = new byte[w * h];

//        if (isreference == true) {
//            rimg = imageClustering(rimg);
//        }
//        rimg = imageClustering(rimg);

        rimg = normalizeIntensity(op.filter(refimg, null), op.filter(rimg, null));

        getGray(rimg, g);

        output.write(g, 0, w * h);

        output.close();
        stream.close();

    }

    public void getRGB(BufferedImage img, byte[] R, byte[] G, byte[] B) {
        int c, r, g, b;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                c = img.getRGB(x, y);
                r = (c & 0xff0000) >> 16;
                g = (c & 0xff00) >> 8;
                b = c & 0xff;
                R[y * img.getWidth() + x] = (byte) r;
                G[y * img.getWidth() + x] = (byte) g;
                B[y * img.getWidth() + x] = (byte) r;
            }
        }
    }

    public void getGray(BufferedImage img, byte[] g) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int gray = img.getRaster().getPixel(x, y, (int[]) null)[0];
                g[y * img.getWidth() + x] = (byte) gray;
            }
        }
    }

    public BufferedImage imageClustering(BufferedImage img) {
        int c, r, g, b;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                c = img.getRGB(x, y);
//                Color c = new Color(img.getRGB(x, y));
                r = (c & 0xff0000) >> 16;
                g = (c & 0xff00) >> 8;
                b = c & 0xff;
                if ((r < 170 && g < 170 && b < 170) || y < 55) {
                    img.setRGB(x, y, 0);
                }
            }
        }
        return img;
    }

    public BufferedImage normalizeIntensity(BufferedImage refImage, BufferedImage image) {
        BufferedImage im = image;
        int[] refMinMax = findMaxMin(refImage);
        int[] oldMinMax = findMaxMin(image);

        System.out.println("Ref" + refMinMax[0] + ":" + refMinMax[1] + " old " + oldMinMax[0] + ":" + oldMinMax[1] + " -:- w:" + im.getWidth() + " h: " + im.getHeight());

        int c, r, g, b, nornmalizedGray;
        for (int x = 0; x < im.getWidth(); x++) {
            for (int y = 0; y < im.getHeight(); y++) {

                int gray = image.getRaster().getPixel(x, y, (int[]) null)[0];
                nornmalizedGray = (((gray - oldMinMax[0]) * (refMinMax[1] - refMinMax[0])) / (oldMinMax[1] - oldMinMax[0])) + refMinMax[0];
                byte normalizedRGB[] = new byte[1];
                normalizedRGB[0] = (byte) nornmalizedGray;
//                System.out.println("gray" + gray + ":" + nornmalizedGray);
                im.getRaster().setDataElements(x, y, normalizedRGB);

            }
        }
        return im;
    }

    public int[] findMaxMin(BufferedImage img) {
        int c, r, g, b;
        int min = 255;
        int max = 0;

        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                if (y > 55) {
                    int gray = img.getRaster().getPixel(x, y, (int[]) null)[0];
                    if (gray > max) {
                        max = gray;
                    }
                    if (gray < min) {
                        min = gray;
                    }
                }
            }
        }
        int[] minMax = new int[2];
        minMax[0] = min;
        minMax[1] = max;
        return (minMax);
    }

    public double[] getAffineArray(String affinefilename) {
        double rotation1 = 0;
        double rotation2 = 0;
        double rotation3 = 0;
        double rotation4 = 0;

        double translation1 = 0;
        double translation2 = 0;
        try {
            FileInputStream fstream = new FileInputStream(filePath + affinefilename);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            // omat file must be of specific length (4x4)
            // Although we are only using the first two lines
            int lineCount = 1;

            while ((strLine = br.readLine()) != null) {
                System.out.println("getAffineArray: " + strLine);

                String[] tokens = strLine.split("\\s+");
                if (lineCount == 1) {
                    rotation1 = Double.valueOf(tokens[0]);
                    rotation3 = Double.valueOf(tokens[1]);
                    translation1 = Double.valueOf(tokens[3]);
                } else if (lineCount == 2) {
                    rotation2 = Double.valueOf(tokens[0]);
                    rotation4 = Double.valueOf(tokens[1]);
                    translation2 = Double.valueOf(tokens[3]);
                }
                lineCount++;
            }
            in.close();

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
        System.out.println(rotation1 + " - " + rotation2 + " - " + rotation3 + " - " + rotation4 + " - " + translation1 + " - " + translation2);
        return new double[]{rotation1, rotation2, rotation3, rotation4, translation1, translation2};
    }

    public void applyAffine(String filename) {

        //////////////////
        // Lets do this //
        //////////////////

        File inputfile = new File(filePath + filename + ".jpg");
        BufferedImage img = null;
        BufferedImage rimg = null;

        try {
            rimg = ImageIO.read(inputfile);
        } catch (IOException e) {
        }
        img = new BufferedImage(rimg.getWidth(), rimg.getHeight(), BufferedImage.TYPE_INT_RGB);

        AffineTransform tx = new AffineTransform();
        tx.translate(affineMatrix[4], affineMatrix[5]);
        tx.rotate(affineMatrix[0], affineMatrix[1], affineMatrix[2], affineMatrix[3]);

//        tx.setTransform(affineMatrix[0], affineMatrix[1], affineMatrix[2], affineMatrix[3], affineMatrix[4], affineMatrix[5]);
//        AffineTransformOp op = new AffineTransformOp(tx, null);
//        rimg = op.filter(rimg, null);

        Graphics2D g2 = img.createGraphics();
        g2.setBackground(Color.black);
        g2.drawImage(rimg, tx, null);

        affineLabel.setIcon(new ImageIcon(img));

        try {
            File outputfile = new File(filePath + filename + "_affine.jpg");
            ImageIO.write(img, "jpg", outputfile);

        } catch (IOException e) {
        }

    }

    public void runAfni(String command) throws IOException, InterruptedException {
//        System.out.println(command);
//        java.lang.Runtime rt = java.lang.Runtime.getRuntime();
//        Process afni_proc = rt.exec(command);
//        afni_proc.waitFor();
//        BufferedReader b = new BufferedReader(new InputStreamReader(afni_proc.getInputStream()));
//        String line = "";
//
//        while ((line = b.readLine()) != null) {
//            System.out.println(line);
//        }
//        System.out.println("Process exited with code");



        Runtime rt = Runtime.getRuntime();
        System.out.println(command);
        Process proc = rt.exec(command);
        // any error message?
        streamGobbler errorGobbler = new streamGobbler(proc.getErrorStream(), "ERROR");

        // any output?
        streamGobbler outputGobbler = new streamGobbler(proc.getInputStream(), "OUTPUT");

        // kick them off
        errorGobbler.start();
        outputGobbler.start();

        // any error???
        int exitVal = proc.waitFor();
        System.out.println("ExitValue: " + exitVal);


    }

    class streamGobbler extends Thread {

        InputStream is;
        String type;

        streamGobbler(InputStream is, String type) {
            this.is = is;
            this.type = type;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println(type + ">" + line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void garbageDisposer(String filename) {
        File niiHdrFile = new File(filePath + filename + ".hdr");
        File niiImgFile = new File(filePath + filename + ".img");
        if (niiHdrFile.exists()) {
            niiHdrFile.delete();
        }
        if (niiImgFile.exists()) {
            niiImgFile.delete();
        }
    }
}
