package suave;

import java.util.*;
import javax.media.opengl.GL;
import java.awt.image.BufferedImage;
import java.awt.*;

public class TextureDB {

    private HashMap<String, TextureInfo> tdb = new HashMap<String, TextureInfo>();

    public TextureDB() {
    }

    public synchronized int addNamedTexture(GL gl, String name, TextureReader.Texture texture, boolean hasAlpha) {
        TextureInfo ti = new TextureInfo();
        ti.name = name;
        ti.texture = texture;
        if (hasAlpha) {
            ti.numColorComponents = 4;
        } else {
            ti.numColorComponents = 3;
        }
        //	Debug.debug(1,"TextureDB.addNamedTexture: adding '"+name+" to texture cache");
        initialTextureSetup(gl, ti);
        tdb.put(name, ti);
        return ti.textID;
    }

    public synchronized TextureInfo getNamedTexture(String name) {
        return tdb.get(name);
    }
    private final static int TEXTURE_LEVEL = 0; // mipmapping parameter - we're not using mipmaps (yet).

    private synchronized void initialTextureSetup(GL gl, TextureInfo ti) {
        int textID[] = new int[1];
        gl.glGenTextures(1, textID, 0);      // Get An Open ID
        if (textID[0] == 0) {
            Debug.debug(1, "TextureDB.initialTextureSetup:  Got ZERO for our texture ID!  This is probably bad.");
        }
// 	else {
// 	    Debug.debug(1,"TextureDB.initialTextureSetup:  got text id= "+textID[0]);
// 	}
        ti.textID = textID[0];
        Debug.debug(1, "TextureDB.initialTextureSetup: set texture  named " + ti.name + " to textid=" + ti.textID);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textID[0]);    // Bind The Texture
        // why did I put this here?
        //	gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT, 1);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);

        gl.glTexImage2D(GL.GL_TEXTURE_2D,
                TEXTURE_LEVEL,
                GL.GL_RGBA8,
                ti.texture.getWidth(),
                ti.texture.getHeight(),
                0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,
                ti.texture.getPixels());
    }

    public synchronized TextureInfo getColorTexture(GL gl, int widthHeight, Color color) {
        String name = color.toString() + "." + widthHeight;
        TextureInfo ti = tdb.get(name);
        if (null == ti) {
            ti = new TextureInfo();
            ti.name = name;
            ti.texture = buildColorTexture(widthHeight, widthHeight, color, null, 0);
            if (color.getAlpha() < 255) {
                ti.numColorComponents = 4;
            } else {
                ti.numColorComponents = 3;
            }
            //	    Debug.debug(1,"TextureDB.getColorTexture: adding '"+name+" to texture cache");
            initialTextureSetup(gl, ti);
            tdb.put(name, ti);
        }
        return ti;
    }

    public synchronized TextureInfo getLinedColorTexture(GL gl, int widthHeight, Color mainColor, Color lineColor, int lineStep) {
        String name = mainColor.toString() + "." + widthHeight + "." + lineColor.toString() + "." + lineStep;
        TextureInfo ti = tdb.get(name);
        if (null == ti) {
            ti = new TextureInfo();
            ti.name = name;
            ti.texture = buildColorTexture(widthHeight, widthHeight, mainColor, lineColor, lineStep);
            if ((mainColor.getAlpha() < 255) || (lineColor.getAlpha() < 255)) {
                ti.numColorComponents = 4;
            } else {
                ti.numColorComponents = 3;
            }
            Debug.debug(1, "TextureDB.getLinedColorTexture: adding '" + name + " to texture cache");
            initialTextureSetup(gl, ti);
        }
        return ti;
    }

    // @TODO: Maybe move these out to a factory or util class
    private static TextureReader.Texture buildColorTexture(int width, int height, Color mainColor, Color lineColor, int lineStep) {
        boolean alphaFlag = false;
        if (mainColor.getAlpha() < 255) {
            alphaFlag = true;
        } else if (lineColor != null) {
            if (lineColor.getAlpha() < 255) {
                alphaFlag = true;
            }
        }
        // @TODO: Over-riding, all of our textures have alphas!
        alphaFlag = true;

        BufferedImage textImage;
        if (alphaFlag) {
            textImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        } else {
            textImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D g = textImage.createGraphics();
        g.setColor(mainColor);
        g.fillRect(0, 0, width, height);

        if (null != lineColor) {
            g.setColor(lineColor);
            for (int loopx = 0; loopx < width; loopx += width / lineStep) {
                g.drawLine(loopx, 0, loopx, height);
            }
            g.setColor(lineColor);
            for (int loopy = 0; loopy < height; loopy += height / lineStep) {
                g.drawLine(0, loopy, width, loopy);
            }
        }

        g.dispose();

        return TextureReader.createTexture(textImage, alphaFlag);
    }
    public final static float LINE_STROKE = 4f;

    public static TextureReader.Texture buildFlagColorTexture(int width, int height, Color mainColor, Color lineColor, int lineStep) {
        boolean alphaFlag = false;
        if (mainColor.getAlpha() < 255) {
            alphaFlag = true;
        } else if (lineColor != null) {
            if (lineColor.getAlpha() < 255) {
                alphaFlag = true;
            }
        }
        // @TODO: Over-riding, all of our textures have alphas!
        alphaFlag = true;

        BufferedImage textImage;
        if (alphaFlag) {
            textImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        } else {
            textImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        }

        Graphics2D g = textImage.createGraphics();
        g.setColor(mainColor);
        g.fillRect(0, 0, width, height);

        g.setColor(Color.black);
        g.fillRect(0, 0, 1, 1);

        ((Graphics2D) g).setStroke(new BasicStroke(LINE_STROKE));

        g.setColor(Color.black);
        // Oval for treads... for mech infantry (height/2) fighting vehicles
        g.drawOval((width / 2) - width / 4, (height / 2) - height / 4,
                (int) (width / 2), (int) (height / 2));
        // x for infantry
        g.drawLine(0, 0, width, height);
        g.drawLine(width, 0, 0, height);

        g.drawLine(0, 0, width, 0);
        g.drawLine(0, 0, 0, height);
        g.drawLine(width - 1, height - 1, 0, height - 1);
        g.drawLine(width - 1, height - 1, width - 1, 0);

        if (null != lineColor) {
            g.drawLine(0, 0, 100, 100);
            g.setColor(lineColor);
            for (int loopx = 0; loopx < width; loopx += width / lineStep) {
                g.drawLine(loopx, 0, loopx, height);
            }
            g.setColor(lineColor);
            for (int loopy = 0; loopy < height; loopy += height / lineStep) {
                g.drawLine(0, loopy, width, loopy);
            }
        }
        g.dispose();

        return TextureReader.createTexture(textImage, alphaFlag);
    }
}
