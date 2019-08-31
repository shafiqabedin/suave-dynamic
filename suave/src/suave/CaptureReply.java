/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 *
 * @author owens
 */
public class CaptureReply implements Serializable {

    CaptureCommand captureCommand;
    int width;
    int height;
    int[] rgb;

    public CaptureReply(CaptureCommand captureCommand, BufferedImage image) {
        this.captureCommand = captureCommand;
        width = image.getWidth();
        height = image.getHeight();
        rgb = new int[width * height];
        image.getRGB(0, 0, width, height, rgb, 0, width);
    }

    public CaptureReply(CaptureCommand captureCommand, int width, int height, int[] rgb) {
        this.captureCommand = captureCommand;
        this.width = width;
        this.height = height;
        this.rgb = rgb;
    }

    public BufferedImage getImage() {
        Debug.debug(1, "CaptureReply.getImage:  Creating image type 3BYTE_BGR of width, height = " + width + ", " + height + " from rgb array size " + rgb.length);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        image.setRGB(0, 0, width, height, rgb, 0, width);
        return image;
    }

    // @Override
    public String toString() {
        return "CaptureReply=(" + captureCommand + ") width=" + width + " height=" + height;
    }
}
