package suave;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;

public class DisplayListFactory {

    private final static double LINE_CYL_RADIUS = 2;
    private final static int LINE_CYL_SLICES = 8;
    private final static int LINE_CYL_STACKS = 8;
    private final static double LINE_SPHERE_RADIUS = LINE_CYL_RADIUS;
    private final static int LINE_SPHERE_SLICES = 8;
    private final static int LINE_SPHERE_STACKS = 8;

    public static RenderableDisplayList buildLineDisplayList(GL gl, int textId, float[][] linePoints) {
        int displayList = gl.glGenLists(1);
        gl.glNewList(displayList, GL.GL_COMPILE);
        gl.glPushMatrix();

        gl.glBegin(gl.GL_TRIANGLES);

        // @TODO: Check for linePoints.length >= 2
        float[] up = new float[3];
        float[] dir = new float[3];
        float[] lat = new float[3];
        float[] q1 = new float[3];
        float[] q2 = new float[3];
        float[] q3 = new float[3];
        float[] q4 = new float[3];

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
            Vec3f.set(lat, 0, 2, 0);

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
            gl.glTexCoord2f(0, 0);
            gl.glVertex3f(q1[0], q1[1], q1[2]);
            gl.glTexCoord2f(1, 0);
            gl.glVertex3f(q2[0], q2[1], q2[2]);
            gl.glTexCoord2f(1, 1);
            gl.glVertex3f(q3[0], q3[1], q3[2]);
            // and q1,q3,q4
            gl.glTexCoord2f(0, 0);
            gl.glVertex3f(q1[0], q1[1], q1[2]);
            gl.glTexCoord2f(1, 1);
            gl.glVertex3f(q3[0], q3[1], q3[2]);
            gl.glTexCoord2f(0, 1);
            gl.glVertex3f(q4[0], q4[1], q4[2]);
        }


        gl.glEnd();
        gl.glPopMatrix();
        gl.glEndList();

        return new RenderableDisplayList(displayList, textId);
    }

    public static RenderableDisplayList buildLineDisplayList(GL gl, int textId, ArrayList<float[]> linePoints) {
        GLU glu = new GLU();
        GLUquadric quadric = glu.gluNewQuadric();
        glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);
        glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
        glu.gluQuadricOrientation(quadric, glu.GLU_OUTSIDE);
        glu.gluQuadricTexture(quadric, true);

        int displayList = gl.glGenLists(1);
        gl.glNewList(displayList, GL.GL_COMPILE);
        gl.glPushMatrix();

        gl.glBegin(gl.GL_TRIANGLES);

        // @TODO: Check for linePoints.length >= 2
        float[] up = new float[3];
        float[] dir = new float[3];
        float[] lat = new float[3];
        float[] q1 = new float[3];
        float[] q2 = new float[3];
        float[] q3 = new float[3];
        float[] q4 = new float[3];

        Vec3f.set(up, 0, 1, 0);
        for (int loopi = 0; loopi < linePoints.size() - 1; loopi++) {

            float[] p1 = linePoints.get(loopi);
            float[] p2 = linePoints.get(loopi + 1);
            Vec3f.sub(dir, p2, p1);
            Vec3f.normalize(dir);

            // This bit here tries to generate a vector at right
            // angles to the line of direction and 'up'
            //
            // 	    Vec3f.cross(lat, dir, up);
            // 	    Vec3f.normalize(lat);

            // Or we can just go with an up-and-down line...
            Vec3f.set(lat, 0, 2, 0);

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
            gl.glTexCoord2f(0, 0);
            gl.glVertex3f(q1[0], q1[1], q1[2]);
            gl.glTexCoord2f(1, 0);
            gl.glVertex3f(q2[0], q2[1], q2[2]);
            gl.glTexCoord2f(1, 1);
            gl.glVertex3f(q3[0], q3[1], q3[2]);
            // and q1,q3,q4
            gl.glTexCoord2f(0, 0);
            gl.glVertex3f(q1[0], q1[1], q1[2]);
            gl.glTexCoord2f(1, 1);
            gl.glVertex3f(q3[0], q3[1], q3[2]);
            gl.glTexCoord2f(0, 1);
            gl.glVertex3f(q4[0], q4[1], q4[2]);
        }

        gl.glEnd();
        gl.glPopMatrix();
        gl.glEndList();

        return new RenderableDisplayList(displayList, textId);
    }
    static float cubeNormals[][] = {{-1.0f, 0.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {1.0f, 0.0f, 0.0f}, {0.0f, -1.0f, 0.0f}, {0.0f, 0.0f, 1.0f}, {0.0f, 0.0f, -1.0f}};
    static int cubeFaces[][] = {{0, 1, 2, 3}, {3, 2, 6, 7}, {7, 6, 5, 4}, {4, 5, 1, 0}, {5, 6, 2, 1}, {7, 4, 0, 3}};
    static float cubeVertices[][] = {
        {-1, -1, 1},
        {-1, -1, -1},
        {-1, 1, -1},
        {-1, 1, 1},
        {1, -1, 1},
        {1, -1, -1},
        {1, 1, -1},
        {1, 1, 1}
    };
    static float cubeVertices2[][] = new float[8][3];
    private final static float HV_WIDTH_METERS = 2.1336f;
    private final static float HV_HEIGHT_METERS = 0.85344f;
    private final static float HV_LENGTH_METERS = 4.57200f;
    private final static float HV_CLEARANCE_METERS = 0.4064f;

    private static void drawBox(GL gl, float scale) {
        for (int loopi = 0; loopi < 8; loopi++) {
            for (int loopj = 0; loopj < 3; loopj++) {
                cubeVertices2[loopi][loopj] = cubeVertices2[loopi][loopj] * scale;
            }
        }

        for (int loopi = 0; loopi < 6; loopi++) {
            gl.glNormal3f(cubeNormals[loopi][0], cubeNormals[loopi][1], cubeNormals[loopi][2]);
            gl.glVertex3f(cubeVertices[cubeFaces[loopi][0]][0],
                    cubeVertices[cubeFaces[loopi][0]][1],
                    cubeVertices[cubeFaces[loopi][0]][2]);

            gl.glVertex3f(cubeVertices[cubeFaces[loopi][1]][0],
                    cubeVertices[cubeFaces[loopi][1]][1],
                    cubeVertices[cubeFaces[loopi][1]][2]);

            gl.glVertex3f(cubeVertices[cubeFaces[loopi][2]][0],
                    cubeVertices[cubeFaces[loopi][2]][1],
                    cubeVertices[cubeFaces[loopi][2]][2]);

            gl.glVertex3f(cubeVertices[cubeFaces[loopi][3]][0],
                    cubeVertices[cubeFaces[loopi][3]][1],
                    cubeVertices[cubeFaces[loopi][3]][2]);
        }
    }

    public static RenderableDisplayList buildSphere(GL gl, int textId, double radius, int slices, int stacks) {
        GLU glu = new GLU();
        GLUquadric quadric = glu.gluNewQuadric();
        glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);
        glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
        glu.gluQuadricOrientation(quadric, glu.GLU_OUTSIDE);
        glu.gluQuadricTexture(quadric, true);

        int displayList = gl.glGenLists(1);
        gl.glNewList(displayList, GL.GL_COMPILE);
        gl.glPushMatrix();
        glu.gluSphere(quadric, radius, slices, stacks);
        gl.glPopMatrix();
        gl.glEndList();

        return new RenderableDisplayList(displayList, textId);
    }
    public final static double HMMWV_SPHERE_RADIUS = 4;
    private final static int HMMWV_SPHERE_SLICES = 16;
    private final static int HMMWV_SPHERE_STACKS = 16;

    public static RenderableDisplayList buildHumvee(GL gl, int textId) {
        return buildSphere(gl, textId, HMMWV_SPHERE_RADIUS, HMMWV_SPHERE_SLICES, HMMWV_SPHERE_STACKS);
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

    public static RenderableDisplayList buildFlag(GL gl, int textId) {
        GLU glu = new GLU();
        GLUquadric quadric = glu.gluNewQuadric();
        glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);
        glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
        glu.gluQuadricOrientation(quadric, glu.GLU_OUTSIDE);
        glu.gluQuadricTexture(quadric, true);

        int displayList = gl.glGenLists(1);
        gl.glNewList(displayList, GL.GL_COMPILE);
        gl.glPushMatrix();

        gl.glBegin(gl.GL_QUADS);

        // post
        gl.glTexCoord2f(0, 0);
        gl.glVertex3f(0, 0, 0);
        gl.glTexCoord2f(0, 0);
        gl.glVertex3f(0, FLAG_POST_HEIGHT, 0);
        gl.glTexCoord2f(0, 0);
        gl.glVertex3f(FLAG_POST_WIDTH, FLAG_POST_HEIGHT, 0);
        gl.glTexCoord2f(0, 0);
        gl.glVertex3f(FLAG_POST_WIDTH, 0, 0);

        // flag
        gl.glTexCoord2f(0, 0);
        gl.glVertex3f(0, FLAG_POST_HEIGHT + 0, 0);
        gl.glTexCoord2f(0, 1);
        gl.glVertex3f(0, FLAG_POST_HEIGHT + FLAG_HEIGHT, 0);
        gl.glTexCoord2f(1, 1);
        gl.glVertex3f(FLAG_WIDTH, FLAG_POST_HEIGHT + FLAG_HEIGHT, 0);
        gl.glTexCoord2f(1, 0);
        gl.glVertex3f(FLAG_WIDTH, FLAG_POST_HEIGHT, 0);

        gl.glEnd();

        gl.glPopMatrix();
        gl.glEndList();

        return new RenderableDisplayList(displayList, textId);
    }
}
