package suave;

import com.sun.opengl.util.BufferUtil;

import java.io.Serializable;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Hashtable;

// originally from - not sure, google on Pepijn Van Eeckhoudt
// TextureReader and it shows up all over the place but I can't seem
// to find a canonical source.
/**
 * Image loading class that converts BufferedImages into a data
 * structure that can be easily passed to OpenGL.
 * @author Pepijn Van Eeckhoudt
 */
public class TextureReader implements Serializable {

    public static Texture readTexture(String filename) throws IOException {
        return readTexture(filename, false, false, 0);
    }

    public static Texture readTexture(String filename, boolean storeAlphaChannel) throws IOException {
        return readTexture(filename, storeAlphaChannel, false, 0);
    }

    public static Texture readTexture(String filename, boolean storeAlphaChannel, boolean resize, int newSize) throws IOException {
        BufferedImage bufferedImage;
        if (filename.endsWith(".bmp")) {
            bufferedImage = BitmapLoader.loadBitmap(filename);
        } else {
            bufferedImage = readImage(filename);
        }
        if (resize) {
            //	    System.err.println("Resizing texture, original size is "+bufferedImage.getWidth()+", "+bufferedImage.getHeight()+" resizing to "+newSize+", "+newSize);
            BufferedImage newImage = new BufferedImage(newSize, newSize, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = null;
            g = newImage.createGraphics();
            g.drawImage(bufferedImage, 0, 0, newSize, newSize, null);
            g.dispose();
            bufferedImage = newImage;
        }
        return readPixels(bufferedImage, storeAlphaChannel);
    }

    public static Texture createTexture(BufferedImage image, boolean alphaFlag) {
        return readPixels(image, alphaFlag);
    }

    public static BufferedImage readImage(String resourceName) throws IOException {
        return ImageIO.read(ResourceRetriever.getResourceAsStream(resourceName));
    }

    private static Texture readPixels(BufferedImage img, boolean storeAlphaChannel) {
        int[] packedPixels = new int[img.getWidth() * img.getHeight()];

        PixelGrabber pixelgrabber = new PixelGrabber(img, 0, 0, img.getWidth(), img.getHeight(), packedPixels, 0, img.getWidth());
        try {
            pixelgrabber.grabPixels();
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }

        int bytesPerPixel = storeAlphaChannel ? 4 : 3;
        ByteBuffer unpackedPixels = BufferUtil.newByteBuffer(packedPixels.length * bytesPerPixel);

        for (int row = img.getHeight() - 1; row >= 0; row--) {
            for (int col = 0; col < img.getWidth(); col++) {
                int packedPixel = packedPixels[row * img.getWidth() + col];
                unpackedPixels.put((byte) ((packedPixel >> 16) & 0xFF));
                unpackedPixels.put((byte) ((packedPixel >> 8) & 0xFF));
                unpackedPixels.put((byte) ((packedPixel >> 0) & 0xFF));
                if (storeAlphaChannel) {
                    unpackedPixels.put((byte) ((packedPixel >> 24) & 0xFF));
                }
            }
        }

        unpackedPixels.flip();


        return new Texture(unpackedPixels, img.getWidth(), img.getHeight(), storeAlphaChannel, img);
    }

    private static BufferedImage writePixels(ByteBuffer pixels, int width, int height, boolean storeAlphaChannel) {
//        int[] packedPixels = new int[width * height * (storeAlphaChannel ? 4 : 3)];
        int[] packedPixels = new int[width * height * 3];
        Debug.debug(1, "TextureReader.writePixels: creating an array of pixels for image of width " + width + ", height " + height + " total ints " + packedPixels.length);

        int bufferInd = 0;
        for (int row = height - 1; row >= 0; row--) {
            for (int col = 0; col < width; col++) {
                int A = 0, R, G, B;
                R = pixels.get(bufferInd++);
                G = pixels.get(bufferInd++);
                B = pixels.get(bufferInd++);
                if (storeAlphaChannel) {
                    A = pixels.get(bufferInd++);
                }
                int index = (row * width + col) * 3;
                packedPixels[index++] = R;
                packedPixels[index++] = G;
                packedPixels[index] = B;
            }
        }
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster wr = img.getRaster();
        wr.setPixels(0, 0, width, height, packedPixels);
        return img;
    }

    public static class Texture implements Serializable {

        private ByteBuffer pixels;
        private int width;
        private int height;
        private BufferedImage img = null;
        private boolean storeAlphaChannel;

        public Texture(ByteBuffer pixels, int width, int height) {
            this.height = height;
            this.pixels = pixels;
            this.width = width;
            this.storeAlphaChannel = false;
        }

        public Texture(ByteBuffer pixels, int width, int height, boolean storeAlphaChannel, BufferedImage img) {
            this.height = height;
            this.pixels = pixels;
            this.width = width;
            this.storeAlphaChannel = storeAlphaChannel;
            this.img = img;
        }

        public int getHeight() {
            return height;
        }

        public ByteBuffer getPixels() {
            return pixels;
        }

        public int getWidth() {
            return width;
        }
        // @Override

        public String toString() {
            return "Texture width=" + width + " height=" + height + " pixels.position=" + pixels.position() + " pixels.limit=" + pixels.limit();
        }

        public BufferedImage getImg2() {
            return img;
        }

        public BufferedImage getImageFromTexture() {
            return writePixels(pixels, width, height, storeAlphaChannel);
        }
    }
}
