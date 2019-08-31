package suave;

import javax.media.opengl.*;

// http://www.java-tips.org/other-api-tips/jogl/use-of-some-of-the-gluquadric-routines.html
public class Box {

    public static void drawBox(GL gl, float x1, float y1, float z1, float x2, float y2, float z2, float c1, float c2, float c3) {
        gl.glColor3f(c1, c2, c3);
        gl.glBegin(GL.GL_TRIANGLES);
        // side towards viewer
        gl.glVertex3f(x1, y1, z1);
        gl.glVertex3f(x2, y1, z1);
        gl.glVertex3f(x2, y2, z1);
        gl.glVertex3f(x2, y2, z1);
        gl.glVertex3f(x1, y2, z1);
        gl.glVertex3f(x1, y1, z1);
        // side away from viewer
        gl.glVertex3f(x1, y1, z2);
        gl.glVertex3f(x2, y1, z2);
        gl.glVertex3f(x2, y2, z2);
        gl.glVertex3f(x2, y2, z2);
        gl.glVertex3f(x1, y2, z2);
        gl.glVertex3f(x1, y1, z2);
        // top
        gl.glVertex3f(x1, y2, z1);
        gl.glVertex3f(x2, y2, z1);
        gl.glVertex3f(x2, y2, z2);
        gl.glVertex3f(x2, y2, z2);
        gl.glVertex3f(x1, y2, z2);
        gl.glVertex3f(x1, y2, z1);
        // bottom
        gl.glVertex3f(x1, y1, z1);
        gl.glVertex3f(x2, y1, z1);
        gl.glVertex3f(x2, y1, z2);
        gl.glVertex3f(x2, y1, z2);
        gl.glVertex3f(x1, y1, z2);
        gl.glVertex3f(x1, y1, z1);
        // left
        gl.glVertex3f(x1, y1, z1);
        gl.glVertex3f(x1, y2, z1);
        gl.glVertex3f(x1, y2, z2);
        gl.glVertex3f(x1, y2, z2);
        gl.glVertex3f(x1, y1, z2);
        gl.glVertex3f(x1, y1, z1);
        // right
        gl.glVertex3f(x2, y1, z1);
        gl.glVertex3f(x2, y2, z1);
        gl.glVertex3f(x2, y2, z2);
        gl.glVertex3f(x2, y2, z2);
        gl.glVertex3f(x2, y1, z2);
        gl.glVertex3f(x2, y1, z1);
        gl.glEnd();
    }

    public static void drawBoxShaded(GL gl, float x1, float y1, float z1, float x2, float y2, float z2, float red, float green, float blue) {
        float xmax = x1;
        if (xmax < x2) {
            xmax = x2;
        }
        float ymax = y1;
        if (ymax < y2) {
            ymax = y2;
        }
        float zmax = z1;
        if (zmax < z2) {
            zmax = z2;
        }
        float r1 = ((xmax - x1) / xmax) * red;
        float g1 = ((ymax - y1) / ymax) * green;
        float b1 = ((zmax - z1) / zmax) * blue;
        float r2 = ((xmax - x2) / xmax) * red;
        float g2 = ((ymax - y2) / ymax) * green;
        float b2 = ((zmax - z2) / zmax) * blue;

        gl.glBegin(GL.GL_TRIANGLES);
        // side towards viewer
        gl.glColor3f(r1, g1, b1);
        gl.glVertex3f(x1, y1, z1);
        gl.glColor3f(r2, g1, b1);
        gl.glVertex3f(x2, y1, z1);
        gl.glColor3f(r2, g2, b1);
        gl.glVertex3f(x2, y2, z1);
        gl.glColor3f(r2, g2, b1);
        gl.glVertex3f(x2, y2, z1);
        gl.glColor3f(r1, g2, b1);
        gl.glVertex3f(x1, y2, z1);
        gl.glColor3f(r1, g1, b1);
        gl.glVertex3f(x1, y1, z1);
        // side away from viewer
        gl.glColor3f(r1, g1, b2);
        gl.glVertex3f(x1, y1, z2);
        gl.glColor3f(r2, g1, b2);
        gl.glVertex3f(x2, y1, z2);
        gl.glColor3f(r2, g2, b2);
        gl.glVertex3f(x2, y2, z2);
        gl.glColor3f(r2, g2, b2);
        gl.glVertex3f(x2, y2, z2);
        gl.glColor3f(r1, g2, b2);
        gl.glVertex3f(x1, y2, z2);
        gl.glColor3f(r1, g1, b2);
        gl.glVertex3f(x1, y1, z2);
        // top
        gl.glColor3f(r1, g2, b1);
        gl.glVertex3f(x1, y2, z1);
        gl.glColor3f(r2, g2, b1);
        gl.glVertex3f(x2, y2, z1);
        gl.glColor3f(r2, g2, b2);
        gl.glVertex3f(x2, y2, z2);
        gl.glColor3f(r2, g2, b2);
        gl.glVertex3f(x2, y2, z2);
        gl.glColor3f(r1, g2, b2);
        gl.glVertex3f(x1, y2, z2);
        gl.glColor3f(r1, g2, b1);
        gl.glVertex3f(x1, y2, z1);
        // bottom
        gl.glColor3f(r1, g1, b1);
        gl.glVertex3f(x1, y1, z1);
        gl.glColor3f(r2, g1, b1);
        gl.glVertex3f(x2, y1, z1);
        gl.glColor3f(r2, g1, b2);
        gl.glVertex3f(x2, y1, z2);
        gl.glColor3f(r2, g1, b2);
        gl.glVertex3f(x2, y1, z2);
        gl.glColor3f(r1, g1, b2);
        gl.glVertex3f(x1, y1, z2);
        gl.glColor3f(r1, g1, b1);
        gl.glVertex3f(x1, y1, z1);
        // left
        gl.glColor3f(r1, g1, b1);
        gl.glVertex3f(x1, y1, z1);
        gl.glColor3f(r1, g2, b1);
        gl.glVertex3f(x1, y2, z1);
        gl.glColor3f(r1, g2, b2);
        gl.glVertex3f(x1, y2, z2);
        gl.glColor3f(r1, g2, b2);
        gl.glVertex3f(x1, y2, z2);
        gl.glColor3f(r1, g1, b2);
        gl.glVertex3f(x1, y1, z2);
        gl.glColor3f(r1, g1, b1);
        gl.glVertex3f(x1, y1, z1);
        // right
        gl.glColor3f(r2, g1, b1);
        gl.glVertex3f(x2, y1, z1);
        gl.glColor3f(r2, g2, b1);
        gl.glVertex3f(x2, y2, z1);
        gl.glColor3f(r2, g2, b2);
        gl.glVertex3f(x2, y2, z2);
        gl.glColor3f(r2, g2, b2);
        gl.glVertex3f(x2, y2, z2);
        gl.glColor3f(r2, g1, b2);
        gl.glVertex3f(x2, y1, z2);
        gl.glColor3f(r2, g1, b1);
        gl.glVertex3f(x2, y1, z1);
        gl.glEnd();
    }
}
