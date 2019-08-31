package suave;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

// http://www.java-tips.org/other-api-tips/jogl/use-of-some-of-the-gluquadric-routines.html
public class Axis implements Renderable {

    private final static float AXIS_BOX_START = 10;
    private final static float AXIS_BOX_END = 1000;
    private final static float AXIS_BOX_RADIUS = 1.0f;
    private final static float AXIS_BOX_SPACE = 1.0f;

    // @TODO: Redo these using VBOs (not immediate mode) so that we can use them without totally messing up the colors
    public Axis() {
    }

    public void destroy(GL gl) {
       // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void render(GL gl) {
      drawBoxes(gl);
    }

    public static void drawBoxes(GL gl) {
        // @TODO: immediate mode is slowwwwwwwww.  Probably won't
        // matter for these, especially since they're only for
        // debugging.
        GLU glu = new GLU();
        GLUquadric quadric = null;

        // White Sphere at 0,0,0
        quadric = glu.gluNewQuadric();
        gl.glPushMatrix();                        //save first position
        gl.glTranslatef(0f, 0.0f, 0.0f);
        gl.glColor3f(1.0f, 1.0f, 1.0f);
        glu.gluQuadricDrawStyle(quadric, GLU.GLU_LINE);
        glu.gluSphere(quadric, 10, 20, 20);
        gl.glPopMatrix();                        //load first position

        float x1, y1, z1;
        float x2, y2, z2;

        // Red - X axis box
        x1 = AXIS_BOX_START;
        y1 = -AXIS_BOX_RADIUS;
        z1 = -AXIS_BOX_RADIUS;
        x2 = AXIS_BOX_END;
        y2 = AXIS_BOX_RADIUS;
        z2 = AXIS_BOX_RADIUS;
        Box.drawBoxShaded(gl, x1, y1, z1, x2, y2, z2, 1.0f, 0.0f, 0.0f);

        // Green - Y axis box
        x1 = -AXIS_BOX_RADIUS;
        y1 = AXIS_BOX_START;
        z1 = -AXIS_BOX_RADIUS;
        x2 = AXIS_BOX_RADIUS;
        y2 = AXIS_BOX_END;
        z2 = AXIS_BOX_RADIUS;
        Box.drawBoxShaded(gl, x1, y1, z1, x2, y2, z2, 0.0f, 1.0f, 0.0f);

        // Blue - Z axis box
        x1 = -AXIS_BOX_RADIUS;
        y1 = -AXIS_BOX_RADIUS;
        z1 = AXIS_BOX_START;
        x2 = AXIS_BOX_RADIUS;
        y2 = AXIS_BOX_RADIUS;
        z2 = AXIS_BOX_END;
        Box.drawBoxShaded(gl, x1, y1, z1, x2, y2, z2, 0.0f, 0.0f, 1.0f);

        //   for(float loopd = -80 * AXIS_BOX_SPACE; loopd <= (80 * AXIS_BOX_SPACE); loopd += AXIS_BOX_SPACE) {
        //      //       Red - X axis box
        //       x1 = -AXIS_BOX_END;
        //       y1 = -AXIS_BOX_RADIUS;
        //       z1 = -AXIS_BOX_RADIUS + loopd;
        //       x2 = AXIS_BOX_END;
        //       y2 = AXIS_BOX_RADIUS;
        //       z2 = AXIS_BOX_RADIUS + loopd;
        //       Box.drawBoxShaded(gl,x1,y1,z1, x2,y2,z2, 1.0f, 0.0f, 0.0f);
        //   }

        //   for(float loopd = -80 * AXIS_BOX_SPACE; loopd <= (80 * AXIS_BOX_SPACE); loopd += AXIS_BOX_SPACE) {
        //      //       Green - Y axis box
        //       x1 = -AXIS_BOX_RADIUS + loopd;
        //       y1 = -AXIS_BOX_END;
        //       z1 = -AXIS_BOX_RADIUS;
        //       x2 = AXIS_BOX_RADIUS + loopd;
        //       y2 = AXIS_BOX_END;
        //       z2 = AXIS_BOX_RADIUS;
        //       Box.drawBoxShaded(gl,x1,y1,z1, x2,y2,z2, 0.0f, 1.0f, 0.0f);
        //   }

        //   for(float loopd = -80 * AXIS_BOX_SPACE; loopd <= (80 * AXIS_BOX_SPACE); loopd += AXIS_BOX_SPACE) {
        //      //       Blue - Z axis box
        //       x1 = -AXIS_BOX_RADIUS + loopd;
        //       y1 = -AXIS_BOX_RADIUS;
        //       z1 = -AXIS_BOX_END;
        //       x2 = AXIS_BOX_RADIUS + loopd;
        //       y2 = AXIS_BOX_RADIUS;
        //       z2 = AXIS_BOX_END;
        //       Box.drawBoxShaded(gl,x1,y1,z1, x2,y2,z2, 0.0f, 0.0f, 1.0f);
        //   }
    }
}
