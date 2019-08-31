package suave;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.*;

public class VideoFrame {

    public long timeMs = 0;
    public int frameIndex = 0;
    public String filename;
    public double x = 0;
    public double y = 0;
    public double z = 0;
    public double xRot = 0;
    public double yRot = 0;
    public double zRot = 0;
    public String imageFilename;
    public TextureReader.Texture videoFrame;
    public BufferedImage img;
    public int paintPixels[] = null;
    public byte paintBytes[] = null;
    public boolean useDirectionVector = false;
    public float dirx;
    public float diry;
    public float dirz;
    public float upx;
    public float upy;
    public float upz;
    public int uavid;

    public void setDirectionAndUp(double dirx,
            double diry,
            double dirz,
            double upx,
            double upy,
            double upz) {
        this.dirx = (float) dirx;
        this.diry = (float) diry;
        this.dirz = (float) dirz;
        this.upx = (float) upx;
        this.upy = (float) upy;
        this.upz = (float) upz;
        useDirectionVector = true;
    }

    public VideoFrame() {
    }

    public VideoFrame(String filename) {
        load(filename);
    }

    public void loadImage(String imageFilename) {
        if (null != imageFilename) {
            try {
                this.imageFilename = imageFilename;
                img = ImageIO.read(new FileInputStream(imageFilename));
            } catch (Exception e) {
                Debug.debug(3, "VideoFrame.load: file read failed : " + e.toString());
                e.printStackTrace();
            }
        }
    }

    private void printImgType(BufferedImage img) {
        int type = img.getType();
        if (type == img.TYPE_3BYTE_BGR) {
            Debug.debug(1, "VideoFrame.loadImage: image is type " + type + " = TYPE_3BYTE_BGR");
        } else if (type == img.TYPE_4BYTE_ABGR) {
            Debug.debug(1, "VideoFrame.loadImage: image is type " + type + " = TYPE_4BYTE_ABGR");
        } else if (type == img.TYPE_4BYTE_ABGR_PRE) {
            Debug.debug(1, "VideoFrame.loadImage: image is type " + type + " = TYPE_4BYTE_ABGR_PRE");
        } else if (type == img.TYPE_BYTE_BINARY) {
            Debug.debug(1, "VideoFrame.loadImage: image is type " + type + " = TYPE_BYTE_BINARY");
        } else if (type == img.TYPE_BYTE_GRAY) {
            Debug.debug(1, "VideoFrame.loadImage: image is type " + type + " = TYPE_BYTE_GRAY");
        } else if (type == img.TYPE_BYTE_INDEXED) {
            Debug.debug(1, "VideoFrame.loadImage: image is type " + type + " = TYPE_BYTE_INDEXED");
        } else if (type == img.TYPE_CUSTOM) {
            Debug.debug(1, "VideoFrame.loadImage: image is type " + type + " = TYPE_CUSTOM");
        } else if (type == img.TYPE_INT_ARGB) {
            Debug.debug(1, "VideoFrame.loadImage: image is type " + type + " = TYPE_INT_ARGB");
        } else if (type == img.TYPE_INT_ARGB_PRE) {
            Debug.debug(1, "VideoFrame.loadImage: image is type " + type + " = TYPE_INT_ARGB_PRE");
        } else if (type == img.TYPE_INT_BGR) {
            Debug.debug(1, "VideoFrame.loadImage: image is type " + type + " = TYPE_INT_BGR");
        } else if (type == img.TYPE_INT_RGB) {
            Debug.debug(1, "VideoFrame.loadImage: image is type " + type + " = TYPE_INT_RGB");
        } else {
            Debug.debug(1, "VideoFrame.loadImage: image is type " + type + " = unknown type");
        }
    }

    public void load(String filename) {
        File f = new File(filename);
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line = in.readLine();
            while (line != null) {
                Debug.debug(1, "VideoFrame.load: read line='" + line + "'");
                line = line.trim();
                if (line.length() > 0) {
                    int splitPoint = line.indexOf(' ');
                    if (splitPoint > 0) {
                        String field = line.substring(0, splitPoint).trim().intern();
                        String value = line.substring(splitPoint).trim().intern();
                        Debug.debug(1, "VideoFrame.load: loading " + field + " to " + value);
                        if (field.equalsIgnoreCase("imagefilename")) {
                            imageFilename = value;
                        } else if (field.equalsIgnoreCase("x")) {
                            x = Double.parseDouble(value);
                        } else if (field.equalsIgnoreCase("y")) {
                            y = Double.parseDouble(value);
                        } else if (field.equalsIgnoreCase("z")) {
                            z = Double.parseDouble(value);
                        } else if (field.equalsIgnoreCase("xrot")) {
                            xRot = Double.parseDouble(value);
                        } else if (field.equalsIgnoreCase("yrot")) {
                            yRot = Double.parseDouble(value);
                        } else if (field.equalsIgnoreCase("zrot")) {
                            zRot = Double.parseDouble(value);
                        } else {
                            Debug.debug(1, "VideoFrame.load: Unknown field='" + field + "' value='" + value + "'");
                        }
                    }
                }
                line = in.readLine();
            }

            loadImage(imageFilename);
        } catch (Exception e) {
            Debug.debug(3, "VideoFrame.load: file read failed : " + e.toString());
            e.printStackTrace();
        }
    }
}
