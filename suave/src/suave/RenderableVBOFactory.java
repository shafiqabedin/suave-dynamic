package suave;

import java.awt.*;
import java.util.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL;
import com.sun.opengl.util.BufferUtil;

public class RenderableVBOFactory {

    // @TODO: order the triangle based on http://home.comcast.net/~tom_forsyth/papers/fast_vert_cache_opt.html
    public static RenderableVBO build(GL gl, int textureID, HashMap<String, Vertex> verticeMap, ArrayList<Triangle> triangles, boolean recalculateVertexNormals) {
        Vertex[] verticeAry = verticeMap.values().toArray(new Vertex[1]);

        // Recalculate triangle normals, if they want us to
        if (recalculateVertexNormals) {
            recalculateVertexNormals(triangles, verticeAry);
        }
        ArrayList<int[]> indexedTriList = calculateIndexedTris(triangles, verticeAry);

        // Now build the buffers for the VBO/IBO
        int verticesCount = verticeAry.length;

        int verticesBufSize = 3 * verticesCount * BufferUtil.SIZEOF_FLOAT;
        int normalsBufSize = 3 * verticesCount * BufferUtil.SIZEOF_FLOAT;
        int texCoordsBufSize = 2 * verticesCount * BufferUtil.SIZEOF_FLOAT;

        int indicesCount = triangles.size() * 3;
        int indicesBufSize = indicesCount * BufferUtil.SIZEOF_INT;

        FloatBuffer verticesBuf;
        FloatBuffer normalsBuf;
        FloatBuffer texCoordsBuf;
        IntBuffer indicesBuf;

        verticesBuf = BufferUtil.newFloatBuffer(verticesCount * 3);
        normalsBuf = BufferUtil.newFloatBuffer(verticesCount * 3);
        texCoordsBuf = BufferUtil.newFloatBuffer(verticesCount * 2);

        for (int loopi = 0; loopi < verticeAry.length; loopi++) {
            verticesBuf.put(verticeAry[loopi].x);
            verticesBuf.put(verticeAry[loopi].y);
            verticesBuf.put(verticeAry[loopi].z);
            normalsBuf.put(verticeAry[loopi].normalx);
            normalsBuf.put(verticeAry[loopi].normaly);
            normalsBuf.put(verticeAry[loopi].normalz);
            texCoordsBuf.put(verticeAry[loopi].u);
            texCoordsBuf.put(verticeAry[loopi].v);
        }

        verticesBuf.flip();
        normalsBuf.flip();
        texCoordsBuf.flip();

        IntBuffer indices;    // indices into the vertices, to specify triangles.
        indices = BufferUtil.newIntBuffer(indicesCount);

        for (int loopi = 0; loopi < indexedTriList.size(); loopi++) {
            int[] vertices = indexedTriList.get(loopi);
            // NOTE: If we want to reduce space usage, we could use
            // unsigned shorts for meshes with < (2^16-1) vertices,
            // and only use four byte ints for meshes with >= (2^16)
            // vertices.  It's probably worth adding later.  (Note,
            // opengl.org says don't use unsigned byte;
            // http://www.opengl.org/wiki/Vertex_Buffer_Object)
            indices.put(vertices[0]);
            indices.put(vertices[1]);
            indices.put(vertices[2]);
        }

        indices.flip();

        // Allrighty!  Now give them to OpenGL!

        int[] verticesBufID = new int[1];
        int[] normalsBufID = new int[1];
        int[] texCoordsBufID = new int[1];
        int[] indicesID = new int[1];

        gl.glGenBuffersARB(1, verticesBufID, 0);
        gl.glGenBuffersARB(1, normalsBufID, 0);
        gl.glGenBuffersARB(1, texCoordsBufID, 0);
        gl.glGenBuffersARB(1, indicesID, 0);

        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, verticesBufID[0]);
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, verticesBufSize, verticesBuf, GL.GL_STATIC_DRAW);

        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, normalsBufID[0]);
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, normalsBufSize, normalsBuf, GL.GL_STATIC_DRAW);

        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, texCoordsBufID[0]);
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, texCoordsBufSize, texCoordsBuf, GL.GL_STATIC_DRAW);

        gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER_ARB, indicesID[0]);
        gl.glBufferDataARB(GL.GL_ELEMENT_ARRAY_BUFFER_ARB, indicesBufSize, indices, GL.GL_STATIC_DRAW);

        // our copy of the data is no longer necessary, it is safe in OpenGl
        verticesBuf = null;
        normalsBuf = null;
        texCoordsBuf = null;
        indices = null;

        return new RenderableVBO(textureID, verticesBufID[0], normalsBufID[0], texCoordsBufID[0], indicesID[0], indicesCount);
    }

    // @TODO: Move this code out to an 'optional' class - i.e. make build 'assume' that normals are done correctly
    private static void recalculateVertexNormals(ArrayList<Triangle> triangles, Vertex[] verticeAry) {
        // Recalculate triangle normals, if they want us to
        float[] zero = {0, 0, 0};
        for (int loopi = 0; loopi < verticeAry.length; loopi++) {
            verticeAry[loopi].setNormal(zero);
        }

        for (int loopi = 0; loopi < triangles.size(); loopi++) {
            Triangle t = triangles.get(loopi);
            t.calculateTriangleNormal();
            t.v1.addToNormal(t.normalx, t.normaly, t.normalz);
        }

        for (int loopi = 0; loopi < verticeAry.length; loopi++) {
            float[] vn = verticeAry[loopi].getNormal();
            Vec3f.normalize(vn);
            verticeAry[loopi].setNormal(vn);
        }
    }

    // @TODO: Move this code out to an 'optional' class - i.e. make
    // build 'assume' that we've already sorted out the entire issue
    // of unique vertice objects etc.
    private static ArrayList<int[]> calculateIndexedTris(ArrayList<Triangle> triangles, Vertex[] verticeAry) {
        // Now sort out the triangle/vertex indices, so we can use a
        // VertexArray in our VBO.
        HashMap<String, Integer> indexMap = new HashMap<String, Integer>();
        for (int loopi = 0; loopi < verticeAry.length; loopi++) {
            indexMap.put(verticeAry[loopi].toString(), loopi);
        }

        ArrayList<int[]> indexedTriList = new ArrayList<int[]>();
        for (int loopi = 0; loopi < triangles.size(); loopi++) {
            Integer v1 = indexMap.get(triangles.get(loopi).v1.toString());
            Integer v2 = indexMap.get(triangles.get(loopi).v2.toString());
            Integer v3 = indexMap.get(triangles.get(loopi).v3.toString());
            if (null == v1 || null == v2 || null == v3) {
                Debug.debug(5, "ERROR: Triangle[" + loopi + "] has vertex not present in vertex map. t=" + triangles.get(loopi) + ", indexes for v1=" + v1 + ", v2=" + v2 + ", v3=" + v3);
            } else {
                int[] t = new int[3];
                t[0] = v1;
                t[1] = v2;
                t[2] = v3;
                indexedTriList.add(t);
            }
        }
        return indexedTriList;
    }

    private static Vertex getVertex(HashMap<String, Vertex> verticeMap, float x, float y, float z, float u, float v) {
        Vertex v1 = new Vertex(x, y, z, u, v);
        if (verticeMap.get(v1.toString()) != null) {
            return verticeMap.get(v1.toString());
        } else {
            verticeMap.put(v1.toString(), v1);
            return v1;
        }
    }

    // @TODO: order the triangle based on http://home.comcast.net/~tom_forsyth/papers/fast_vert_cache_opt.html
    public static RenderableInterleavedVBO buildInterleavedVBO(GL gl, int textureID, HashMap<String, Vertex> verticeMap, ArrayList<Triangle> triangles, boolean recalculateVertexNormals) {
        //	Debug.debug(1,"RenderableVBOFactory.build: building a vbo!");

        if (verticeMap.values().size() <= 0) {
            throw new RuntimeException("Vertice map is size ZERO!  Can't build a VBO!");
        }

        if (triangles.size() <= 0) {
            throw new RuntimeException("Triangle list is size ZERO!  Can't build a VBO!");
        }

        Vertex[] verticeAry = verticeMap.values().toArray(new Vertex[1]);

        // Recalculate triangle normals, if they want us to
        if (recalculateVertexNormals) {
            recalculateVertexNormals(triangles, verticeAry);
        }

        // Now sort out the triangle/vertex indices, so we can use a
        // VertexArray in our VBO.
        HashMap<String, Integer> indexMap = new HashMap<String, Integer>();
        for (int loopi = 0; loopi < verticeAry.length; loopi++) {
            indexMap.put(verticeAry[loopi].toString(), loopi);
        }

        ArrayList<int[]> indexedTriList = calculateIndexedTris(triangles, verticeAry);

        // Now build the buffers for the VBO/IBO
        int verticeAttributesCount = verticeAry.length;
        int indicesCount = triangles.size() * 3;

        FloatBuffer verticeAttributes;
        verticeAttributes = BufferUtil.newFloatBuffer(verticeAttributesCount * RenderableInterleavedVBO.ATTR_SZ_FLOATS);

        for (int loopi = 0; loopi < verticeAry.length; loopi++) {
            verticeAttributes.put(verticeAry[loopi].x);
            verticeAttributes.put(verticeAry[loopi].y);
            verticeAttributes.put(verticeAry[loopi].z);
            verticeAttributes.put(verticeAry[loopi].normalx);
            verticeAttributes.put(verticeAry[loopi].normaly);
            verticeAttributes.put(verticeAry[loopi].normalz);
            verticeAttributes.put(verticeAry[loopi].u);
            verticeAttributes.put(verticeAry[loopi].v);
        }

        verticeAttributes.flip();

        IntBuffer indices;    // indices into the vertices, to specify triangles.
        indices = BufferUtil.newIntBuffer(indicesCount);

        for (int loopi = 0; loopi < indexedTriList.size(); loopi++) {
            int[] vertices = indexedTriList.get(loopi);
            // NOTE: If we want to reduce space usage, we could use
            // unsigned shorts for meshes with < (2^16-1) vertices,
            // and only use four byte ints for meshes with >= (2^16)
            // vertices.  It's probably worth adding later.  (Note,
            // opengl.org says don't use unsigned byte;
            // http://www.opengl.org/wiki/Vertex_Buffer_Object)
            indices.put(vertices[0]);
            indices.put(vertices[1]);
            indices.put(vertices[2]);
        }

        indices.flip();

        // Allrighty!  Now give them to OpenGL!

        int[] verticeAttributesID = new int[1];      // Vertex Attributes VBO ID

        gl.glGenBuffersARB(1, verticeAttributesID, 0);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, verticeAttributesID[0]);
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, verticeAttributesCount * RenderableInterleavedVBO.ATTR_SZ_BYTES, verticeAttributes, GL.GL_STATIC_DRAW);

        int[] indicesID = new int[1];
        gl.glGenBuffersARB(1, indicesID, 0);
        gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER_ARB, indicesID[0]);
        gl.glBufferDataARB(GL.GL_ELEMENT_ARRAY_BUFFER_ARB, indicesCount * RenderableInterleavedVBO.INDICE_SIZE_BYTES, indices, GL.GL_STATIC_DRAW);

        // our copy of the data is no longer necessary, it is safe in OpenGl
        verticeAttributes = null;
        indices = null;

        return new RenderableInterleavedVBO(textureID, verticeAttributesID[0], indicesID[0], indicesCount);
    }

    public static RenderableInterleavedVBO buildInterleavedVBO(GL gl, int textureID, ArrayList<Triangle> triangleList, boolean recalculateVertexNormals) {
        // consolidate redundant vertices and build vertice map.
        HashMap<String, Vertex> verticeMap = new HashMap<String, Vertex>();
        for (int loopi = 0; loopi < triangleList.size(); loopi++) {
            Triangle t = triangleList.get(loopi);
            t.v1 = getVertex(verticeMap, t.v1.x, t.v1.y, t.v1.z, t.v1.u, t.v1.v);
            t.v2 = getVertex(verticeMap, t.v2.x, t.v2.y, t.v2.z, t.v2.u, t.v2.v);
            t.v3 = getVertex(verticeMap, t.v3.x, t.v3.y, t.v3.z, t.v3.u, t.v3.v);
        }
        return buildInterleavedVBO(gl, textureID, verticeMap, triangleList, true);
    }

    public static RenderableInterleavedVBO buildInterleavedVBOLine(GL gl, int textID, float[][] linePoints) {
        return buildInterleavedVBOLine(gl, textID, linePoints, 2);
    }

    public static RenderableInterleavedVBO buildInterleavedVBOLine(GL gl, int textID, float[][] linePoints, float lineHeight) {

        // @TODO: Check for linePoints.length >= 2
        float[] up = new float[3];
        float[] dir = new float[3];
        float[] lat = new float[3];
        float[] q1 = new float[3];
        float[] q2 = new float[3];
        float[] q3 = new float[3];
        float[] q4 = new float[3];

        HashMap<String, Vertex> verticeMap = new HashMap<String, Vertex>();
        ArrayList<Triangle> triangles = new ArrayList<Triangle>();

        Vertex v1, v2, v3;

        Vec3f.set(up, 0, 1, 0);
        for (int loopi = 0; loopi < linePoints.length - 1; loopi++) {

            float[] p1 = linePoints[loopi];
            float[] p2 = linePoints[loopi + 1];
            Vec3f.sub(dir, p2, p1);
            Vec3f.normalize(dir);

            // This bit here tries to generate a vector at right
            // angles to the line of direction and 'up'
            //
            // 	    Vec3f.cross(lat, dir, up);
            // 	    Vec3f.normalize(lat);

            // Or we can just go with an up-and-down line...
            Vec3f.set(lat, 0, lineHeight, 0);

            // q1 through q4 define the corners of the rectangle
            //
            //   q1------q2
            //   |        |
            //   q4------q3
            //
            // so the code below builds them in that order
            Vec3f.cpy(q1, p1);
            Vec3f.add(q1, lat);
            Vec3f.cpy(q2, p1);
            Vec3f.sub(q2, lat);
            Vec3f.cpy(q3, p2);
            Vec3f.sub(q3, lat);
            Vec3f.cpy(q4, p2);
            Vec3f.add(q4, lat);

            // and now we draw the rectangle as two triangles
            //
            // q1,q2,q3
            v1 = getVertex(verticeMap, q1[0], q1[1], q1[2], 0, 0);
            v2 = getVertex(verticeMap, q2[0], q2[1], q2[2], 1, 0);
            v3 = getVertex(verticeMap, q3[0], q3[1], q3[2], 1, 1);
            triangles.add(new Triangle(v1, v2, v3));


            // and q1,q3,q4
            v1 = getVertex(verticeMap, q1[0], q1[1], q1[2], 0, 0);
            v2 = getVertex(verticeMap, q3[0], q3[1], q3[2], 1, 1);
            v3 = getVertex(verticeMap, q4[0], q4[1], q4[2], 0, 1);
            triangles.add(new Triangle(v1, v2, v3));
        }

        return buildInterleavedVBO(gl, textID, verticeMap, triangles, true);
    }

//     public static RenderableVBO buildLineDisplayListJUNK(GL gl, int textID, float[][] linePoints) {
//  	// @TODO: Check for linePoints.length >= 2
// 	float[] up = new float[3];
// 	float[] dir = new float[3];
// 	float[] lat = new float[3];
// 	float[] q1 = new float[3];
// 	float[] q2 = new float[3];
// 	float[] q3 = new float[3];
// 	float[] q4 = new float[3];
// 	HashMap<String,Vertex> verticeMap = new HashMap<String,Vertex>();
// 	ArrayList<Triangle> triangles = new ArrayList<Triangle>();
// 	Vertex v1, v2, v3;
// 	Vec3f.set(up,0,1,0);
//  	for(int loopi = 0; loopi < linePoints.length - 1; loopi++) {
// 	    float[] p1 = linePoints[loopi];
// 	    float[] p2 = linePoints[loopi+1];
// 	    Vec3f.sub(dir,p2,p1);
// 	    Vec3f.normalize(dir);
// 	    // This bit here tries to generate a vector at right
// 	    // angles to the line of direction and 'up'
// 	    // 
// 	    // 	    Vec3f.cross(lat, dir, up);
// 	    // 	    Vec3f.normalize(lat);
// 	    // Or we can just go with an up-and-down line...
// 	    Vec3f.set(lat, 0,2,0);
// 	    // q1 through q4 define the corners of the rectangle
// 	    //
// 	    //   q1------q2
// 	    //   |        |
// 	    //   q4------q3
// 	    // 
// 	    // so the code below builds them in that order
// 	    Vec3f.cpy(q1,p1);
// 	    Vec3f.add(q1,lat);
// 	    Vec3f.cpy(q2,p1);
// 	    Vec3f.sub(q2,lat);
// 	    Vec3f.cpy(q3,p2);
// 	    Vec3f.sub(q3,lat);
// 	    Vec3f.cpy(q4,p2);
// 	    Vec3f.add(q4,lat);
// 	    // and now we draw the rectangle as two triangles
// 	    // 
// 	    // q1,q2,q3
// 	    v1 = getVertex(verticeMap, q1[0],q1[1],q1[2], 0,0);
// 	    v2 = getVertex(verticeMap, q2[0],q2[1],q2[2], 1,0);
// 	    v3 = getVertex(verticeMap, q3[0],q3[1],q3[2], 1,1);
// 	    triangles.add(new Triangle(v1,v2,v3));
// 	    // and q1,q3,q4
// 	    v1 = getVertex(verticeMap, q1[0],q1[1],q1[2], 0,0);
// 	    v2 = getVertex(verticeMap, q3[0],q3[1],q3[2], 1,1);
// 	    v3 = getVertex(verticeMap, q4[0],q4[1],q4[2], 0,1);
// 	    triangles.add(new Triangle(v1,v2,v3));
//  	}
// 	return build(gl, textID, verticeMap, triangles, true);
//     }
    public static RenderableInterleavedVBO buildSphere(GL gl, int textID, double radius) {
        ArrayList<Triangle> triangleList = GeomUtil.sphere(false, 5, (float) radius);

        return buildInterleavedVBO(gl, textID, triangleList, true);
    }
    public final static double HMMWV_SPHERE_RADIUS = 4;
//     private final static int HMMWV_SPHERE_SLICES = 16;
//     private final static int HMMWV_SPHERE_STACKS = 16;

    public static RenderableInterleavedVBO buildHumvee(GL gl, int textID) {
        return buildSphere(gl, textID, HMMWV_SPHERE_RADIUS);
    }

    public static RenderableInterleavedVBO buildRing(GL gl, int textID, double outerRadius, double innerRadius, int numSegments) {
        ArrayList<Triangle> triangleList = GeomUtil.ring(outerRadius, innerRadius, numSegments);
        return buildInterleavedVBO(gl, textID, triangleList, true);
    }

    public static RenderableInterleavedVBO buildTarget(GL gl, int textID, double outerRadius, double innerRadius, int numSegments) {
        ArrayList<Triangle> triangleList = new ArrayList<Triangle>();
        double radDiff = (outerRadius - innerRadius) * 2;
        while (innerRadius > 0) {
            triangleList.addAll(GeomUtil.ring(outerRadius, innerRadius, numSegments));
            outerRadius -= radDiff;
            innerRadius -= radDiff;
        }

        return buildInterleavedVBO(gl, textID, triangleList, true);
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

        return TextureReader.createTexture(textImage, true);
    }
    private final static int FLAG_WIDTH = 16;
    private final static int FLAG_HEIGHT = 12;
    private final static int FLAG_POST_HEIGHT = 20;
    private final static int FLAG_POST_WIDTH = 1;

    public static RenderableInterleavedVBO buildFlag(GL gl, int textID) {

        ArrayList<Triangle> triangleList = new ArrayList<Triangle>();

        // flag part
        Vertex v1 = new Vertex(0, FLAG_POST_HEIGHT + 0, 0, 0, 0);
        Vertex v2 = new Vertex(0, FLAG_POST_HEIGHT + FLAG_HEIGHT, 0, 0, 1);
        Vertex v3 = new Vertex(FLAG_WIDTH, FLAG_POST_HEIGHT + FLAG_HEIGHT, 0, 1, 1);
        Vertex v4 = new Vertex(FLAG_WIDTH, FLAG_POST_HEIGHT, 0, 1, 0);

        triangleList.add(new Triangle(v1, v2, v3));
        triangleList.add(new Triangle(v1, v3, v4));
        triangleList = GeomUtil.subdivide(triangleList, 4);

        // post part
        Vertex v5 = new Vertex(0, 0, 0, 0, 0);
        Vertex v6 = new Vertex(0, FLAG_POST_HEIGHT, 0, 0, 0);
        Vertex v7 = new Vertex(FLAG_POST_WIDTH, FLAG_POST_HEIGHT, 0, 0, 0);
        Vertex v8 = new Vertex(FLAG_POST_WIDTH, 0, 0, 0, 0);
        triangleList.add(new Triangle(v5, v6, v7));
        triangleList.add(new Triangle(v5, v7, v8));

        return buildInterleavedVBO(gl, textID, triangleList, true);
    }

    public static RenderableInterleavedVBO buildFlag(GL gl, int textID, int width, int height, int postWidth, int postHeight) {

        ArrayList<Triangle> triangleList = new ArrayList<Triangle>();

        // flag part
        Vertex v1 = new Vertex(0, postHeight + 0, 0, 0, 0);
        Vertex v2 = new Vertex(0, postHeight + height, 0, 0, 1);
        Vertex v3 = new Vertex(width, postHeight + height, 0, 1, 1);
        Vertex v4 = new Vertex(width, postHeight, 0, 1, 0);

        triangleList.add(new Triangle(v1, v2, v3));
        triangleList.add(new Triangle(v1, v3, v4));
        triangleList = GeomUtil.subdivide(triangleList, 4);

        // post part
        Vertex v5 = new Vertex(0, 0, 0, 0, 0);
        Vertex v6 = new Vertex(0, postHeight, 0, 0, 0);
        Vertex v7 = new Vertex(postWidth, postHeight, 0, 0, 0);
        Vertex v8 = new Vertex(postWidth, 0, 0, 0, 0);
        triangleList.add(new Triangle(v5, v6, v7));
        triangleList.add(new Triangle(v5, v7, v8));

        return buildInterleavedVBO(gl, textID, triangleList, true);
    }

    public static RenderableInterleavedVBO buildFlagCenterPost(GL gl, int textID, int width, int height, int postWidth, int postHeight) {

        ArrayList<Triangle> triangleList = new ArrayList<Triangle>();

        float wHalf = width / 2.0f;

        // flag part

        // @TODO: Note, messign with the u,v coords here, so now they
        // are different than in buildFlag, trying to fix them for
        // video renderable.
        Vertex v1 = new Vertex(-wHalf, postHeight + 0, 0, 0, 0);
        Vertex v2 = new Vertex(-wHalf, postHeight + height, 0, 0, 1);
        Vertex v3 = new Vertex(wHalf, postHeight + height, 0, 1, 1);
        Vertex v4 = new Vertex(wHalf, postHeight, 0, 1, 0);

        triangleList.add(new Triangle(v1, v2, v3));
        triangleList.add(new Triangle(v1, v3, v4));
        triangleList = GeomUtil.subdivide(triangleList, 4);

        // post part
        Vertex v5 = new Vertex(0, 0, 0, 0, 0);
        Vertex v6 = new Vertex(0, postHeight, 0, 0, 0);
        Vertex v7 = new Vertex(postWidth, postHeight, 0, 0, 0);
        Vertex v8 = new Vertex(postWidth, 0, 0, 0, 0);
        triangleList.add(new Triangle(v5, v6, v7));
        triangleList.add(new Triangle(v5, v7, v8));

        return buildInterleavedVBO(gl, textID, triangleList, true);
    }
}
