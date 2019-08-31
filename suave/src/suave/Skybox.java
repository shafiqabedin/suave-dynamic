package suave;

import java.util.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.io.IOException;
import javax.media.opengl.*;

public class Skybox implements Renderable {

    private final static float SKY_SIZE = 2500;
    private String[] filenames;
    private TextureDB textureDB;
    private boolean initted = false;
    private int[] ids = new int[5];
    private Renderable[] quads = new Renderable[NUM_TEXTURES];
    // @TODO: Consider making these public and make Main use them?
    private final static int NUM_TEXTURES = 5;
    private final static int FRONT = 0;
    private final static int BACK = 1;
    private final static int LEFT = 2;
    private final static int RIGHT = 3;
    private final static int TOP = 4;
    private TextureReader.Texture[] skyTextures = new TextureReader.Texture[NUM_TEXTURES];
    private int textIDs[] = new int[NUM_TEXTURES];
    private float[][][] skyVertices = new float[NUM_TEXTURES][4][3];
    private float[][][] skyTexCoords = new float[NUM_TEXTURES][4][2];

    // front back left right top
    public Skybox(String[] filenames, TextureDB textureDB) {
        this.filenames = new String[NUM_TEXTURES];
        this.filenames[FRONT] = filenames[FRONT];
        this.filenames[BACK] = filenames[BACK];
        this.filenames[LEFT] = filenames[LEFT];
        this.filenames[RIGHT] = filenames[RIGHT];
        this.filenames[TOP] = filenames[TOP];

        this.textureDB = textureDB;

        loadTextures();
    }

    private void loadTextures() {
        try {
            if (null != filenames[FRONT]) {
                skyTextures[FRONT] = TextureReader.readTexture(filenames[FRONT], true, false, 0);
                Debug.debug(1, "Skybox.loadTextures: Using image " + filenames[FRONT] + " as FRONT sky texture");
            }
            if (null != filenames[BACK]) {
                skyTextures[BACK] = TextureReader.readTexture(filenames[BACK], true, false, 0);
                Debug.debug(1, "Skybox.loadTextures: Using Image " + filenames[BACK] + " as BACK sky texture");
            }
            if (null != filenames[LEFT]) {
                skyTextures[LEFT] = TextureReader.readTexture(filenames[LEFT], true, false, 0);
                Debug.debug(1, "Skybox.loadTextures: Using Image " + filenames[LEFT] + " as LEFT sky texture");
            }
            if (null != filenames[RIGHT]) {
                skyTextures[RIGHT] = TextureReader.readTexture(filenames[RIGHT], true, false, 0);
                Debug.debug(1, "Skybox.loadTextures: Using image " + filenames[RIGHT] + " as RIGHT sky texture");
            }
            if (null != filenames[TOP]) {
                skyTextures[TOP] = TextureReader.readTexture(filenames[TOP], true, false, 0);
                Debug.debug(1, "Skybox.loadTextures: Using image " + filenames[TOP] + " as TOP sky texture");
            }
        } catch (IOException e) {
            Debug.debug(1, "Skybox.loadTextures: Exception loading file to use as sky texture, e=" + e);
            e.printStackTrace();
        }
        BufferedImage skyImage = null;
        for (int loopi = 0; loopi < skyTextures.length; loopi++) {
            if (null == skyTextures[loopi]) {
                if (null == skyImage) {
                    // http://www.tayloredmktg.com/rgb/
                    // Deep Sky Blue   0-191-255   00bfff
                    // Sky Blue  135-206-250  87ceeb
                    // Light Sky Blue  135-206-250  87cefa
                    skyImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = skyImage.createGraphics();
                    g.setColor(new Color(135, 206, 250));
                    g.fillRect(0, 0, 64, 64);
                    g.dispose();
                }
                skyTextures[loopi] = TextureReader.createTexture(skyImage, true);
                Debug.debug(1, "Skybox.buildSkybox: Using sky blue as sky texture for face " + loopi);
            }
        }
    }

    public void buildSkyboxRenderables(GL gl) {
        textIDs[FRONT] = textureDB.addNamedTexture(gl, "skybox_front", skyTextures[FRONT], true);
        textIDs[BACK] = textureDB.addNamedTexture(gl, "skybox_back", skyTextures[BACK], true);
        textIDs[LEFT] = textureDB.addNamedTexture(gl, "skybox_left", skyTextures[LEFT], true);
        textIDs[RIGHT] = textureDB.addNamedTexture(gl, "skybox_right", skyTextures[RIGHT], true);
        textIDs[TOP] = textureDB.addNamedTexture(gl, "skybox_top", skyTextures[TOP], true);

        // As you sit watching the screen,
        //
        // positive X is towards the right
        // positive Y is towards the ceiling
        // positive Z is towards your eyes

        // texture coords are specified as if you are facing north
        // (negative z) in the middle of the box, and turn towards the
        // face in question.  I.e. front is north, back is south, left
        // is west, right is east, etc.
        float topy = 1.5f;
        float boty = -.5f;

        Vertex v1;
        Vertex v2;
        Vertex v3;
        Vertex v4;
        ArrayList<Triangle> triangleList;

        v1 = new Vertex(-1, boty, -1, 0, 0);	// bottom left (from inside)
        v2 = new Vertex(-1, topy, -1, 0, 1);	// top left (from inside)
        v3 = new Vertex(1, topy, -1, 1, 1);	// top right (from inside)
        v4 = new Vertex(1, boty, -1, 1, 0);	// bottom right (from inside)
        v1.scale(SKY_SIZE);
        v2.scale(SKY_SIZE);
        v3.scale(SKY_SIZE);
        v4.scale(SKY_SIZE);
        triangleList = GeomUtil.quad(v1, v2, v3, v4);
        triangleList = GeomUtil.subdivide(triangleList, 4);
        quads[FRONT] = RenderableVBOFactory.buildInterleavedVBO(gl, textIDs[FRONT], triangleList, true);

        // Note the texture coords are mirrored from the front

        v1 = new Vertex(1, boty, 1, 0, 0);	// bottom left
        v2 = new Vertex(1, topy, 1, 0, 1);	// top left
        v3 = new Vertex(-1, topy, 1, 1, 1);	// top right
        v4 = new Vertex(-1, boty, 1, 1, 0);	// bottom right
        v1.scale(SKY_SIZE);
        v2.scale(SKY_SIZE);
        v3.scale(SKY_SIZE);
        v4.scale(SKY_SIZE);
        triangleList = GeomUtil.quad(v1, v2, v3, v4);
        triangleList = GeomUtil.subdivide(triangleList, 4);
        quads[BACK] = RenderableVBOFactory.buildInterleavedVBO(gl, textIDs[BACK], triangleList, true);

        v1 = new Vertex(-1, boty, 1, 0, 0);	// bottom left
        v2 = new Vertex(-1, topy, 1, 0, 1);	// top left
        v3 = new Vertex(-1, topy, -1, 1, 1);	// top right
        v4 = new Vertex(-1, boty, -1, 1, 0);	// bottom right
        v1.scale(SKY_SIZE);
        v2.scale(SKY_SIZE);
        v3.scale(SKY_SIZE);
        v4.scale(SKY_SIZE);
        triangleList = GeomUtil.quad(v1, v2, v3, v4);
        triangleList = GeomUtil.subdivide(triangleList, 4);
        quads[LEFT] = RenderableVBOFactory.buildInterleavedVBO(gl, textIDs[LEFT], triangleList, true);

        v1 = new Vertex(1, boty, -1, 0, 0);	// bottom left
        v2 = new Vertex(1, topy, -1, 0, 1);	// top left
        v3 = new Vertex(1, topy, 1, 1, 1);	// top right
        v4 = new Vertex(1, boty, 1, 1, 0);	// bottom right
        v1.scale(SKY_SIZE);
        v2.scale(SKY_SIZE);
        v3.scale(SKY_SIZE);
        v4.scale(SKY_SIZE);
        triangleList = GeomUtil.quad(v1, v2, v3, v4);
        triangleList = GeomUtil.subdivide(triangleList, 4);
        quads[RIGHT] = RenderableVBOFactory.buildInterleavedVBO(gl, textIDs[RIGHT], triangleList, true);

        v1 = new Vertex(-1, topy, -1, 0, 0);
        v2 = new Vertex(-1, topy, 1, 0, 1);
        v3 = new Vertex(1, topy, 1, 1, 1);
        v4 = new Vertex(1, topy, -1, 1, 0);
        v1.scale(SKY_SIZE);
        v2.scale(SKY_SIZE);
        v3.scale(SKY_SIZE);
        v4.scale(SKY_SIZE);
        triangleList = GeomUtil.quad(v1, v2, v3, v4);
        triangleList = GeomUtil.subdivide(triangleList, 4);
        quads[TOP] = RenderableVBOFactory.buildInterleavedVBO(gl, textIDs[TOP], triangleList, true);
    }

    private void init(GL gl) {
        initted = true;
        buildSkyboxRenderables(gl);
    }

    public void render(GL gl) {

        if (!initted) {
            init(gl);
        }

        for (int loopi = 0; loopi < quads.length; loopi++) {
            quads[loopi].render(gl);
        }
    }

    public void destroy(GL gl) {
        for (int loopi = 0; loopi < quads.length; loopi++) {
            quads[loopi].destroy(gl);
        }
    }
}
