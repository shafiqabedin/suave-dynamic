package suave;

import java.util.*;


import java.nio.FloatBuffer;
import javax.media.opengl.GL;
import com.sun.opengl.util.BufferUtil;

public class Mesh implements Runnable, Renderable, GeoTransformsConstants {

    private final static boolean DEBUG_FLATTEN_TERRAIN = false;
    private final static boolean DEBUG_ACCENTUATE_VERTICAL = false;
    private final static boolean DEBUG_REPEAT_TEXTURES = false;
    boolean initted = false;
    private boolean readyToDraw = false;

    public synchronized boolean readyToDraw() {
        return readyToDraw;
    }

    private synchronized void setReadyToDraw(boolean value) {
        readyToDraw = value;
    }
    private GeoTexture terrainTexture;
    private DEM dem;
    private boolean reloadTexture = false;

    public TextureReader.Texture getImagery() {
        return terrainTexture.getImagery();
    }

    public void setImagery(TextureReader.Texture texture) {
        terrainTexture.setImagery(texture);
        reloadTexture = true;
    }
    private Thread myThread;
    // Mesh Generation Paramaters
    private static final float MESH_HEIGHTSCALE = 10.0f;  // Mesh Height Scale
    private float verticeData[][][] = null;

    public float[][][] getVerticeData() {
        return verticeData;
    }
    // Mesh Data
    private int vertexCount;

    public int getVertexCount() {
        return vertexCount;
    }
    private HashMap<String, Vertex> vertexMap = new HashMap<String, Vertex>();
    private ArrayList<Triangle> triangles = new ArrayList<Triangle>();

    ArrayList<Triangle> getTriangles() {
        return triangles;
    }
    private FloatBuffer vertices;       // Vertex Data
    private FloatBuffer normals;       // Normal Data
    private FloatBuffer texCoords;       // Texture Coordinates
    private int[] meshTextId = new int[1];      // Imagery ID
    // Vertex Buffer Object Names
    private int[] VBOVertices = new int[1];      // Vertex VBO Name
    private int[] VBONormals = new int[1];      // Normals VBO Name
    private int[] VBOTexCoords = new int[1];      // Texture Coordinate VBO Name
    private boolean fUseVBO;

    public Mesh(GeoTexture terrainTexture, DEM dem) {
        this.terrainTexture = terrainTexture;
        this.dem = dem;
        fUseVBO = true;
        myThread = new Thread(this);
    }
    private boolean stopFlag = false;

    public void start() {
        stopFlag = false;
        myThread.start();
    }

    public void stop() {
        stopFlag = true;
    }

    public void run() {
        try {
            verticeData = dem.getVerticeData();
            generateVertexField();
            setReadyToDraw(true);

            try {
                Thread.sleep(50);
            } catch (InterruptedException ie) {
            }

        } catch (Exception e) {
            Main.dealWithExceptions(e);
        }
    }

    private Vertex storeVertex(float[] vertex) {
        float vertexX = vertex[0];
        float vertexY = vertex[1];
        float vertexZ = vertex[2];
        if (vertexY > maxHeight) {
            maxHeight = vertexY;
        }

        if (DEBUG_FLATTEN_TERRAIN) {
            // Hack to make it flat for testing
            vertexY = 330;
        }
        if (DEBUG_ACCENTUATE_VERTICAL) {
            // Hack to accentuate Y (up and down) for testing
            vertexY = (float) ((vertexY - 250) * 5);
            // System.err.println("Mesh.storeVertex: Vertice "+indx+", "+indy+" = "+ vertexX+", "+ vertexY+", "+vertexZ);
        }

        String key = vertexX + "," + vertexY + "," + vertexZ;
        Vertex v = vertexMap.get(key);
//        Debug.debug(1, "storing vertex key="+key+" v="+v);
        if (null == v) {
            v = new Vertex(vertexX, vertexY, vertexZ);
            vertexMap.put(key, v);

//            // NOTE: In OpenGL, y is altitude, x is north/south, z is east west, therefore we get textV from vertexZ
//            float textU = terrainTexture.oglVertexXToTextureU(vertexX);
//            float textV = terrainTexture.oglVertexZToTextureV(vertexZ);
//            v.u = textU;
//            v.v = textV;

            float[] textUV = new float[2];
            terrainTexture.oglVertexToTextureUV(vertex, textUV);
            v.u = textUV[TEXT_U];            
            v.v = textUV[TEXT_V];
        }

        return v;
    }
    private double maxHeight = 0.0;

    public double getMaxHeight() {
        return maxHeight;
    }

    private void generateVertexField() {
//        System.err.println("Mesh.generateVertexField: Entering, verticeData max height = " + demAndTexture.getMaxHeight() + " min height = " + demAndTexture.getMinHeight());
        // Generate Vertex Field
        int width = verticeData.length;
        int height = verticeData[0].length;

        vertexCount = width * height * 6;

        System.err.print("Mesh.generateVertexField: width="+width+" height="+height+" loopX = ");
        int tWidth = 0;
        int tHeight = 0;
        int maxTHeight = 0;
        for (int loopX = 0; loopX < width; loopX++) {
            System.err.print(loopX + ", ");
            tWidth++;
            tHeight = 0;
            for (int loopY = 0; loopY < height; loopY++) {
                // Avoid going outside the array
                if (((loopX + 1) >= width) || ((loopY + 1) >= height)) {
                    continue;
                }
                tHeight++;

                // At this point we store two triangles into the triangle list
                //
                // triloop 1 - x + step , z
                // triloop 2 - x + step , z + step
                // triloop 3 - x        , z

                Vertex v1, v2, v3;
                float[] av1, av2, av3;
                av1 = verticeData[loopX + 1][loopY];
                av2 = verticeData[loopX + 1][loopY + 1];
                av3 = verticeData[loopX][loopY];
                if ((av1[1] > DEM.HOLE_MARKER_ALTITUDE)
                        && (av2[1] > DEM.HOLE_MARKER_ALTITUDE)
                        && (av3[1] > DEM.HOLE_MARKER_ALTITUDE)) {
                    v1 = storeVertex(av1);
                    v2 = storeVertex(av2);
                    v3 = storeVertex(av3);
                    triangles.add(new Triangle(v1, v2, v3));
                }
                // triloop 4 - x        , z + step
                // triloop 5 - x + step , z + step
                // triloop 6 - x        , z
                av1 = verticeData[loopX][loopY + 1];
                av2 = verticeData[loopX + 1][loopY + 1];
                av3 = verticeData[loopX][loopY];
                if ((av1[1] > DEM.HOLE_MARKER_ALTITUDE)
                        && (av2[1] > DEM.HOLE_MARKER_ALTITUDE)
                        && (av3[1] > DEM.HOLE_MARKER_ALTITUDE)) {
                    v1 = storeVertex(av1);
                    v2 = storeVertex(av2);
                    v3 = storeVertex(av3);
                    triangles.add(new Triangle(v1, v2, v3));
                }
            }
            if (tHeight > maxTHeight) {
                maxTHeight = tHeight;
            }
        }
        System.err.println();

        if (vertexMap.values().size() <= 0) {
            Debug.debug(4, "Mesh.generateVertexField: ERROR ERROR ERROR NO VERTICES IN VERTICEMAP!   vertexMap.values().size() <= 0");
        }
        Vertex[] vertexAry = vertexMap.values().toArray(new Vertex[1]);

        for (int loopi = 0; loopi < vertexAry.length; loopi++) {
            float[] zero = {0, 0, 0};
            vertexAry[loopi].setNormal(zero);
        }

        for (int loopi = 0; loopi < triangles.size(); loopi++) {
            Triangle t = triangles.get(loopi);
            t.calculateTriangleNormal();
            t.v1.addToNormal(t.normalx, t.normaly, t.normalz);
        }

        for (int loopi = 0; loopi < vertexAry.length; loopi++) {
            float[] vn = vertexAry[loopi].getNormal();
            Vec3f.normalize(vn);
            vertexAry[loopi].setNormal(vn);
        }

        vertices = BufferUtil.newFloatBuffer(vertexCount * 3); // Allocate Vertex Data
        normals = BufferUtil.newFloatBuffer(vertexCount * 3); // Allocate Normal Data
        texCoords = BufferUtil.newFloatBuffer(vertexCount * 2);    // Allocate Tex Coord Data

        for (int loopi = 0; loopi < triangles.size(); loopi++) {
            Triangle t = triangles.get(loopi);
            vertices.put(t.v1.x);
            vertices.put(t.v1.y);
            vertices.put(t.v1.z);
            normals.put(t.v1.normalx);
            normals.put(t.v1.normaly);
            normals.put(t.v1.normalz);
            texCoords.put(t.v1.u);
            texCoords.put(t.v1.v);

            vertices.put(t.v2.x);
            vertices.put(t.v2.y);
            vertices.put(t.v2.z);
            normals.put(t.v2.normalx);
            normals.put(t.v2.normaly);
            normals.put(t.v2.normalz);
            texCoords.put(t.v2.u);
            texCoords.put(t.v2.v);

            vertices.put(t.v3.x);
            vertices.put(t.v3.y);
            vertices.put(t.v3.z);
            normals.put(t.v3.normalx);
            normals.put(t.v3.normaly);
            normals.put(t.v3.normalz);
            texCoords.put(t.v3.u);
            texCoords.put(t.v3.v);
        }

        vertices.flip();
        normals.flip();
        texCoords.flip();
        Debug.debug(1,"Mesh.generateVertexField: Done!  Triangles generated = " + triangles.size() + " mesh is " + tWidth + " by " + maxTHeight);
    }

    private void buildVBOs(GL gl) {
        // Generate And Bind The Vertex Buffer
        gl.glGenBuffersARB(1, VBOVertices, 0);     // Get A Valid Name
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOVertices[0]);  // Bind The Buffer
        // Load The Data
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, vertexCount * 3 * BufferUtil.SIZEOF_FLOAT, vertices, GL.GL_STATIC_DRAW_ARB);

        // Generate And Bind The Normal Buffer
        gl.glGenBuffersARB(1, VBONormals, 0);     // Get A Valid Name
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBONormals[0]);  // Bind The Buffer
        // Load The Data
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, vertexCount * 3 * BufferUtil.SIZEOF_FLOAT, normals, GL.GL_STATIC_DRAW_ARB);

        // Generate And Bind The Texture Coordinate Buffer
        gl.glGenBuffersARB(1, VBOTexCoords, 0);     // Get A Valid Name
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOTexCoords[0]);  // Bind The Buffer
        // Load The Data
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, vertexCount * 2 * BufferUtil.SIZEOF_FLOAT, texCoords, GL.GL_STATIC_DRAW_ARB);

        // Our Copy Of The Data Is No Longer Necessary, It Is Safe In The Graphics Card
        vertices = null;
        normals = null;
        texCoords = null;
        fUseVBO = true;
    }

    public void init(GL gl) {
        initted = true;
        initTexturesToOpenGL(gl);
    }

    public void initTexturesToOpenGL(GL gl) {
        // Load The Texture Into OpenGL
        gl.glGenTextures(1, meshTextId, 0);      // Get An Open ID
        if (meshTextId[0] == 1) {
            Debug.debug(4,"Mesh.initTextures:  Got ONE for our texture ID!  This is probably bad.");
        } else {
            Debug.debug(1,"Mesh.initTextures:  Got mesh textid = " + meshTextId[0]);
        }

        gl.glBindTexture(GL.GL_TEXTURE_2D, meshTextId[0]);    // Bind The Texture
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        if (DEBUG_REPEAT_TEXTURES) {
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
        } else {
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        }
        //	gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_DECAL);
        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, terrainTexture.getImageryPixelWidth(), terrainTexture.getImageryPixelHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, terrainTexture.getImageryPixels());

        if (fUseVBO) {
            // Load Vertex Data Into The Graphics Card Memory
            buildVBOs(gl);        // Build The VBOs
        }
    }

    public void render(GL gl) {
        if (!initted) {
            init(gl);
        }

        gl.glEnable(GL.GL_TEXTURE_2D);

        // Enable Pointers
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);     // Enable Vertex Arrays
        gl.glEnableClientState(GL.GL_NORMAL_ARRAY);    // Enable Texture Coord Arrays
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);    // Enable Texture Coord Arrays

        gl.glBindTexture(GL.GL_TEXTURE_2D, meshTextId[0]);    // Bind The Texture

        if (reloadTexture) {
            reloadTexture = false;
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, terrainTexture.getImageryPixelWidth(), terrainTexture.getImageryPixelHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, terrainTexture.getImageryPixels());
        }

        // Set Pointers To Our Data
        if (fUseVBO) {
            gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOTexCoords[0]);
            gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);     // Set The TexCoord Pointer To The TexCoord Buffer
            gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBONormals[0]);
            gl.glNormalPointer(GL.GL_FLOAT, 0, 0);     // Set The Normal Pointer To The Normal Buffer
            gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, VBOVertices[0]);
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);     // Set The Vertex Pointer To The Vertex Buffer
        } else {
            gl.glVertexPointer(3, GL.GL_FLOAT, 0, vertices);    // Set The Vertex Pointer To Our Vertex Data
            gl.glNormalPointer(GL.GL_FLOAT, 0, normals);     // Set The Normal Pointer To The Normal Buffer
            gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, texCoords);    // Set The Texcoord Pointer To Our TexCoord Data
        }

        // Render
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, vertexCount);    // Draw All Of The Triangles At Once

        // Disable Pointers
        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);// Disable Vertex Arrays
        gl.glDisableClientState(GL.GL_NORMAL_ARRAY);// Disable Vertex Arrays
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);    // Disable Texture Coord Arrays
        gl.glDisable(GL.GL_TEXTURE_2D);
    }

    public void destroy(GL gl) {
        if (fUseVBO) {
            // @TODO: delete the texture ID
            gl.glDeleteBuffers(1, VBOVertices, 0);
            gl.glDeleteBuffers(1, VBONormals, 0);
            gl.glDeleteBuffers(1, VBOTexCoords, 0);
        }
    }
}
