package suave;

import java.util.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.io.IOException;
import javax.media.opengl.*;

// http://www.java-tips.org/other-api-tips/jogl/use-of-some-of-the-gluquadric-routines.html
public class SkyAndGround implements Renderable {

    private final static float SKY_SIZE = 1000;
    private final static float GROUND_SIZE = 3000;
    private final static float SKY_HEIGHT = 3900;
    private final static float GROUND_HEIGHT = -200;
    private String skyTextureFilename = null;
    private String groundTextureFilename = null;
    private TextureDB textureDB;
    private boolean initted = false;
    private int[] skyTextureId = new int[1];
    private TextureReader.Texture skyTexture = null;
    private int[] groundTextureId = new int[1];
    private TextureReader.Texture groundTexture = null;
    private Renderable sky;
    private Renderable ground;

    public SkyAndGround(String skyTextureFilename, String groundTextureFilename, TextureDB textureDB) {
        this.skyTextureFilename = skyTextureFilename;
        this.groundTextureFilename = groundTextureFilename;
        this.textureDB = textureDB;
        loadTextures();
    }

    private void loadTextures() {
        Graphics2D g = null;

        if (null != skyTextureFilename) {
            try {
                skyTexture = TextureReader.readTexture(skyTextureFilename, true, false, 0);
                Debug.debug(1, "SkyAndGround.buildSkyAndGround: Using image " + skyTextureFilename + " as sky texture");
            } catch (IOException e) {
                Debug.debug(1, "SkyAndGround.buildSkyAndGround: Exception loading image " + skyTextureFilename + " to use as sky texture, e=" + e);
                e.printStackTrace();
            }
        }
        if (null == skyTexture) {
            // http://www.tayloredmktg.com/rgb/
            // Deep Sky Blue   0-191-255   00bfff
            // Sky Blue  135-206-250  87ceeb
            // Light Sky Blue  135-206-250  87cefa
            BufferedImage skyImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
            g = skyImage.createGraphics();
            g.setColor(new Color(135, 206, 250));
            g.fillRect(0, 0, 64, 64);
            g.dispose();
            skyTexture = TextureReader.createTexture(skyImage, true);
            Debug.debug(1, "SkyAndGround.buildSkyAndGround: Using sky blue as sky texture");
        }

        if (null != groundTextureFilename) {
            try {
                groundTexture = TextureReader.readTexture(groundTextureFilename, true, false, 0);
                Debug.debug(1, "SkyAndGround.buildSkyAndGround: Using image " + groundTextureFilename + " as ground texture");
            } catch (IOException e) {
                Debug.debug(1, "SkyAndGround.buildSkyAndGround: Exception loading image " + groundTextureFilename + " to use as ground texture, e=" + e);
                e.printStackTrace();
            }
        }
        if (null == groundTexture) {
            // Burlywood   222-184-135   deb887
            // Pale Goldenrod   238-232-170   eee8aa
            // Light Goldenrod   238-221-130   eedd82
            // Dark Goldenrod   184-134-11   b8860b
            // #D68C0A 214,140,10  Clay Dirt
            BufferedImage groundImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
            g = groundImage.createGraphics();
            g.setColor(new Color(222, 184, 135));
            g.fillRect(0, 0, 64, 64);
            g.dispose();
            groundTexture = TextureReader.createTexture(groundImage, true);
        }
    }

    public void buildSkyAndGroundRenderables(GL gl) {
        skyTextureId[0] = textureDB.addNamedTexture(gl, "sky", skyTexture, false);
        groundTextureId[0] = textureDB.addNamedTexture(gl, "ground", groundTexture, false);

        Vertex v1;
        Vertex v2;
        Vertex v3;
        Vertex v4;
        ArrayList<Triangle> triangleList;

        v1 = new Vertex(-SKY_SIZE, SKY_HEIGHT, -SKY_SIZE, 0.0f, 0.0f);
        v2 = new Vertex(SKY_SIZE, SKY_HEIGHT, -SKY_SIZE, 0.0f, 1.0f);
        v3 = new Vertex(SKY_SIZE, SKY_HEIGHT, SKY_SIZE, 1.0f, 1.0f);
        v4 = new Vertex(-SKY_SIZE, SKY_HEIGHT, SKY_SIZE, 1.0f, 0.0f);
        triangleList = GeomUtil.quad(v1, v2, v3, v4);
        triangleList = GeomUtil.subdivide(triangleList, 4);
        sky = RenderableVBOFactory.buildInterleavedVBO(gl, skyTextureId[0], triangleList, true);

        v1 = new Vertex(-GROUND_SIZE, GROUND_HEIGHT, -GROUND_SIZE, 0.0f, 0.0f);
        v2 = new Vertex(GROUND_SIZE, GROUND_HEIGHT, -GROUND_SIZE, 0.0f, 1.0f);
        v3 = new Vertex(GROUND_SIZE, GROUND_HEIGHT, GROUND_SIZE, 1.0f, 1.0f);
        v4 = new Vertex(-GROUND_SIZE, GROUND_HEIGHT, GROUND_SIZE, 1.0f, 0.0f);
        triangleList = GeomUtil.quad(v1, v2, v3, v4);
        triangleList = GeomUtil.subdivide(triangleList, 4);
        ground = RenderableVBOFactory.buildInterleavedVBO(gl, groundTextureId[0], triangleList, true);
    }

    private void init(GL gl) {
        initted = true;
        buildSkyAndGroundRenderables(gl);
    }

    // @TODO: I think this is probably wrong, need to check/fix - I
    // suspect I don't need to keep rebinding the textures.  For that
    // matter why isn't this a VBO?
    public void render(GL gl) {
        if (!initted) {
            init(gl);
        }

        sky.render(gl);
        ground.render(gl);

    }

    public void destroy(GL gl) {
        sky.destroy(gl);
        ground.destroy(gl);
    }
}
