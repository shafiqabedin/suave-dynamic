package suave;

import javax.media.opengl.*;

public class Lights {

    // move it up high
    //    float lightPos[] = {1.0f, 700.0f, 1.0f, 0.0f};
    float lightPos[] = {1.0f, 1.0f, 1.0f, 0.0f};

    public Lights() {
    }

    // based on http://www.sjbaker.org/steve/omniv/opengl_lighting.html
    // > Good Settings.
    // > With this huge range of options, it can be hard to pick sensible default values for these things.
    // >
    // > My advice for a starting point is to:
    // >
    // >     * Set GL_LIGHT_0's position to something like 45 degrees to the 'vertical'. Coordinate (1,1,0) should work nicely in most cases.
    // >     * Set GL_LIGHT_0's Ambient color to 0,0,0,1
    // >     * Set GL_LIGHT_0's Diffuse color to 1,1,1,1
    // >     * Set GL_LIGHT_0's Specular color to 1,1,1,1
    // >     * Set the glLightModel's global ambient to 0.2,0.2,0.2,1 (this is the default).
    // >     * Don't set any other glLight or glLightModel options - just let them default.
    // >     * Enable GL_LIGHTING and GL_LIGHT_0.
    // >     * Enable GL_COLOR_MATERIAL and set glColorMaterial to GL_AMBIENT_AND_DIFFUSE. This means that glMaterial will control the polygon's specular and emission colours and the ambient and diffuse will both be set using glColor.
    // >     * Set the glMaterial's Specular colour to 1,1,1,1
    // >     * Set the glMaterial's Emission colour to 0,0,0,1
    // >     * Set the glColor to whatever colour you want each polygon to basically appear to be. That sets the Ambient and Diffuse to the same value which is what you generally want. 
    public void addLights(GL gl) {
        float[] blackLight = {0.0f, 0.0f, 0.0f, 1.0f}; // black ambient?
        float[] grayLight = {0.1f, 0.1f, 0.1f, 1.0f}; // weak gray ambient
        float[] whiteLight = {1.0f, 1.0f, 1.0f, 1.0f};

        gl.glEnable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_LIGHT0);

        gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, grayLight, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, whiteLight, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, whiteLight, 0);

        // top right front direction
        // gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, lightPos, 0);
        // 0 means directional, 1 means directionless...
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, lightPos, 0);

        gl.glLightf(GL.GL_LIGHT0, GL.GL_CONSTANT_ATTENUATION, 0.0f);
        gl.glLightf(GL.GL_LIGHT0, GL.GL_LINEAR_ATTENUATION, 0.02f);
        gl.glLightf(GL.GL_LIGHT0, GL.GL_QUADRATIC_ATTENUATION, 0.008f);

        //  float[] lightModelDefault = {0.2f, 0.2f, 0.2f, 1.0f};
        //  gl.glLightModelfv(GL.GL_LIGHT_MODEL_AMBIENT,lightModelDefault);

        gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);
        gl.glEnable(GL.GL_COLOR_MATERIAL);
        gl.glColor3f(1.0f, 1.0f, 1.0f);

        float mat_emission[] = {0.0f, 0.0f, 0.0f, 1.0f};
        float mat_specular[] = {1.0f, 1.0f, 1.0f, 1.0f};

        gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, mat_specular, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, mat_emission, 0);

    }

    public void render(GL gl) {
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, lightPos, 1);
    }
}
