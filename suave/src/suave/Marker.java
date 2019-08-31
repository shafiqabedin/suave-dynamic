package suave;

import java.awt.Color;
import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

// http://www.java-tips.org/other-api-tips/jogl/use-of-some-of-the-gluquadric-routines.html
public class Marker implements Renderable {

    RenderableInterleavedVBO rivbo;
    Color color;

    public Marker(GL gl, TextureInfo ti , String name) {
        rivbo = RenderableVBOFactory.buildSphere(gl, ti.textID, 10);
//        rivbo.setName(name);
    }

    public Marker(Color color) {
        this.color = color;
    }

    public void destroy(GL gl) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public void render(GL gl) {
        if (null != rivbo) {
            rivbo.render(gl);
        } else {
//            drawBoxes(gl, color);
        }
    }

    public static void drawBoxes(GL gl, Color color) {
        // @TODO: immediate mode is slowwwwwwwww.  Probably won't
        // matter for these, especially since they're only for
        // debugging.
        GLU glu = new GLU();
        GLUquadric quadric = null;

        // White Sphere at 0,0,0
        quadric = glu.gluNewQuadric();
        gl.glPushMatrix();                        //save first position
        gl.glTranslatef(0f, 0.0f, 0.0f);
        float[] colorComps = color.getComponents(null);
        gl.glColor3f(colorComps[0], colorComps[1], colorComps[2]);
        glu.gluQuadricDrawStyle(quadric, GLU.GLU_LINE);
        glu.gluSphere(quadric, 10, 20, 20);
        gl.glPopMatrix();                        //load first position
    }
}
