/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 *
 * @author owens
 */
public class GeoTextureFactory {

    private final static boolean TEXTURE_IS_GRAY_FOR_SCREENSHOT = false;
    private final static boolean DRAW_LINES_ON_TEXTURE = true;
    private  static boolean useLineTexture = true;
    private final static Color lineColor = Color.black;
    private final static boolean labelLines = false;
    private final static Color lineLabelColor = Color.red;
    private final static float lineSpacing = 32;

    public static GeoTexture loadFile(String imageryFilename, Origin origin, double northLat, double southLat, double eastLon, double westLon, WorldFile worldFile) {
        TextureReader.Texture imagery = null;
        try {
            imagery = loadFileImagery(imageryFilename);
        } catch (IOException e) {
            Debug.debug(1, "GeoTexture: Exception loading imagery data, e=" + e);
            e.printStackTrace();
        }
        return new GeoTexture(imageryFilename, imagery, origin, northLat, southLat, eastLon, westLon, worldFile);
    }
// public static GeoTexture loadFile(String imageryFilename, Origin origin, double[] topLeftLla,double[] botLeftLla,double[] botRightLla) {
//        TextureReader.Texture imagery = null;
//        try {
//            imagery = loadFileImagery(imageryFilename);
//        } catch (IOException e) {
//            Debug.debug(1, "GeoTexture: Exception loading imagery data, e=" + e);
//            e.printStackTrace();
//        }
//        return new GeoTexture(imageryFilename, imagery, origin, topLeftLla[Geo], botLeftLla, botRightLla);
//    }
    public static GeoTexture createLineGridTexture(Origin origin, double northLat, double southLat, double eastLon, double westLon) {
        TextureReader.Texture imagery = null;
        imagery = TextureReader.createTexture(createLineTextureImage(), true);
        return new GeoTexture(imagery, origin, northLat, southLat, eastLon, westLon);
    }

    private static void makeImageryGrayForScreenshot(BufferedImage tempImage) {
        Graphics2D g = tempImage.createGraphics();
        g.setColor(Color.lightGray);
        g.fillRect(0, 0, 1024, 1024);
        for (int loopx = 0; loopx <= tempImage.getWidth(); loopx += lineSpacing) {
            g.setColor(Color.black);
            g.drawLine(loopx, 0, loopx, (int) tempImage.getHeight());
            g.setColor(Color.red);
            g.drawString(loopx + ", 0", loopx, 10);
            g.drawString(loopx + ", " + ((int) tempImage.getHeight()), loopx, tempImage.getHeight() - 10);
        }
        for (int loopy = 0; loopy <= tempImage.getHeight(); loopy += lineSpacing) {
            g.setColor(Color.black);
            g.drawLine(0, loopy, (int) tempImage.getWidth(), loopy);
            g.setColor(Color.red);
            g.drawString("0, " + loopy, 10, loopy);
            g.drawString(((int) tempImage.getWidth()) + ", " + loopy, tempImage.getHeight() - 10, loopy);
        }
        g.dispose();
    }

    private static TextureReader.Texture loadFileImagery(String imageryFilename) throws IOException {
        TextureReader.Texture imagery = null;
        // @TODO: Note that some graphics cards don't like textures
        // that are not a power of 2 - I've resized the gascola
        // imagery file to be a power of 2 but we may want to add auto
        // resizing here.  Also there is usually a limit to the size
        // of the texture but it varies by card.
        BufferedImage tempImage = TextureReader.readImage(imageryFilename);
        if (TEXTURE_IS_GRAY_FOR_SCREENSHOT) {
            makeImageryGrayForScreenshot(tempImage);
        }

        if (DRAW_LINES_ON_TEXTURE) {
            Graphics2D g = tempImage.createGraphics();
            int lineSpacing = 128;
            for (int loopx = 0; loopx <= tempImage.getWidth(); loopx += lineSpacing) {
                g.setColor(lineColor);
                g.drawLine(loopx, 0, loopx, (int) tempImage.getHeight());
                if (labelLines) {
                    g.setColor(lineLabelColor);
                    g.drawString(loopx + ", 0", loopx, 10);
                    g.drawString(loopx + ", " + ((int) tempImage.getHeight()), loopx, tempImage.getHeight() - 10);
                }
            }
            for (int loopy = 0; loopy <= tempImage.getHeight(); loopy += lineSpacing) {
                g.setColor(lineColor);
                g.drawLine(0, loopy, (int) tempImage.getWidth(), loopy);
                if (labelLines) {
                    g.setColor(lineLabelColor);
                    g.drawString("0, " + loopy, 10, loopy);
                    g.drawString(((int) tempImage.getWidth()) + ", " + loopy, tempImage.getHeight() - 10, loopy);
                }
            }
            g.dispose();
        }
        imagery = TextureReader.createTexture(tempImage, true);
        return imagery;
    }

    private static BufferedImage createLineTextureImage() {
        int size = 4096;
//        int lineStep = size/64;
        int lineStep = (int)(((double)size/(double)50)+.5); // 100 meters - if the map is 5km.
        BufferedImage image5 = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image5.createGraphics();
        g.setColor(Color.gray);
        g.fillRect(0, 0, size, size);
        g.setColor(Color.black);
        for (int loopx = 0; loopx < size; loopx += lineStep) {
            g.drawLine(loopx, 0, loopx, size-1);
        }
        g.setColor(Color.black);
        for (int loopy = 0; loopy < size; loopy += lineStep) {
            g.drawLine(0, loopy, size-1, loopy);
        }
        g.dispose();
        return image5;
    }
}
