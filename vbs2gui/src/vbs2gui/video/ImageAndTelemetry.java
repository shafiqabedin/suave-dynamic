/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vbs2gui.video;

import java.awt.image.BufferedImage;

/**
 *
 * @author owens
 */
public class ImageAndTelemetry {

    public BufferedImage img;
    public double[] telem;
    public int uavid;

    public ImageAndTelemetry(BufferedImage img, double[] telem, int uavid) {
        this.img = img;
        this.telem = telem;
        this.uavid = uavid;
    }
}
