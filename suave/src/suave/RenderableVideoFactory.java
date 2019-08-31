package suave;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.media.opengl.GL;
import java.util.*;
import java.text.DecimalFormat;
import java.io.IOException;

public class RenderableVideoFactory {

    private final static DecimalFormat fmt = new DecimalFormat("00000000.jpg");
    private final static int VIDEO_TEXTURE_SIZE = 256;
    private final static int VIDEO_FRAME_WIDTH = 720;
    private final static int VIDEO_FRAME_HEIGHT = 480;
    private final static int QUAD_WIDTH = 320;
    private final static int QUAD_HEIGHT = 240;
    private final static int POST_WIDTH = 1;
    private final static int POST_HEIGHT = 20;
    private static HashMap<String, TextureReader.Texture> cache = new HashMap<String, TextureReader.Texture>();

    private static TextureReader.Texture buildLoadingTexture(int n, int ofn) {
        BufferedImage temp1 = new BufferedImage(VIDEO_FRAME_WIDTH, VIDEO_FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g;
        g = temp1.createGraphics();
        g.setColor(Color.white);
        g.setFont(new Font("Loading", Font.BOLD, 48));
        g.drawString("Loading " + n + " of " + ofn + "...", VIDEO_FRAME_WIDTH / 4, VIDEO_FRAME_HEIGHT / 2);
        g.dispose();
        return TextureReader.createTexture(temp1, true);
    }

    public static RenderableVideo build(GL gl, Model model, GLCamera camera, String imageDir, int frameIndexStart, int frameIndexEnd,
            float x, float y, float z) {
        int textID[] = new int[1];
        gl.glGenTextures(1, textID, 0);      // Get An Open ID
        if (textID[0] == 0) {
            Debug.debug(1, "RenderableVideoFactory.build:  Got ZERO for our texture ID!  This is probably bad.");
        }

        final int numImages = frameIndexEnd - frameIndexStart;

        TextureReader.Texture[] loadingTextures = new TextureReader.Texture[1];
        loadingTextures[0] = buildLoadingTexture(1, numImages);

        gl.glBindTexture(GL.GL_TEXTURE_2D, textID[0]);    // Bind The Texture
        gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        // @TODO: I think using decal here messes up the other
        // textures - probably need to set all of these before drawing
        // each texture/object Ok, not true... didn't fix it.
//	gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_DECAL);
        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);

        RenderableInterleavedVBO rivbo = RenderableVBOFactory.buildFlagCenterPost(gl, textID[0],
                QUAD_WIDTH,
                QUAD_HEIGHT,
                POST_WIDTH,
                POST_HEIGHT);

        final RenderableVideo rv = new RenderableVideo(model, camera, textID[0], loadingTextures, rivbo, x, y + POST_HEIGHT + QUAD_HEIGHT / 2, z);

        final int fFrameIndexStart = frameIndexStart;
        final String fImageDir = imageDir;
        new Thread() {

            public void run() {
                String[] fileNames = new String[numImages];
                for (int loopi = 0; loopi < numImages; loopi++) {
                    Debug.debug(1, "file " + loopi + " has number " + fFrameIndexStart + " plus " + loopi + " = " + (fFrameIndexStart + loopi));
                    fileNames[loopi] = fImageDir + "\\" + fmt.format(fFrameIndexStart + loopi);
                }

                TextureReader.Texture[] textures = new TextureReader.Texture[numImages];
                for (int loopi = 0; loopi < numImages; loopi++) {
                    textures[loopi] = cache.get(fileNames[loopi]);
                    if (null == textures[loopi]) {
                        try {
                            Debug.debug(1, "RenderableVideoFactory.build: loading video frame " + fileNames[loopi]);
                            // images from video are 720x480
                            textures[loopi] = TextureReader.readTexture(fileNames[loopi], true, true, VIDEO_TEXTURE_SIZE);
                            cache.put(fileNames[loopi], textures[loopi]);
                        } catch (IOException e) {
                            Debug.debug(4, "RenderableVideoFactory.build: exception loading video frame file " + fileNames[loopi] + ", e=" + e);
                            break;
                        }
                    }
                    TextureReader.Texture[] loadingTextures = new TextureReader.Texture[1];
                    loadingTextures[0] = buildLoadingTexture(loopi, numImages);
                    rv.setTextures(loadingTextures, true);
                }
                rv.setTextures(textures, false);
            }
        }.start();

        return rv;
    }
}
