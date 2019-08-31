package suave;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

public class GLError {

    public static void check(GL gl) {
        int errorCode = gl.glGetError();
        if (0 == errorCode) {
            return;
        }

        GLU glu = new GLU();
        String errorStr = glu.gluErrorString(errorCode);
        Debug.debug(1, "GLError.check: GL ERROR FOUND " + errorCode + " " + errorStr);
        Throwable t = new RuntimeException("GL ERROR FOUND " + errorCode + " " + errorStr);
        t.printStackTrace();
    }
}
