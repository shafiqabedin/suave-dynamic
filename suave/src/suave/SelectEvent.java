/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.media.opengl.GL;

/**
 *
 * @author owens
 */
public class SelectEvent {

    // NOTE: GL should only be used if we are inside the GL thread... i.e. make sure we don't pass/store this object elsewhere.
    GL gl;
    MouseEvent e;
    Triangle t;
    float[] eyePos;
    float[] worldPos;
    float[] hitPos;

    public SelectEvent(GL gl, MouseEvent e, Triangle t, float[] eyePos, float[] worldPos, float[] hitPos) {
        this.gl = gl;
        this.e = e;
        this.t = t;
             this.eyePos = new float[3];
        this.eyePos[0] = eyePos[0];
        this.eyePos[1] = eyePos[1];
        this.eyePos[2] = eyePos[2];
        this.worldPos = new float[3];
        this.worldPos[0] = worldPos[0];
        this.worldPos[1] = worldPos[1];
        this.worldPos[2] = worldPos[2];
        this.hitPos = new float[3];
        this.hitPos[0] = hitPos[0];
        this.hitPos[1] = hitPos[1];
        this.hitPos[2] = hitPos[2];
    }

    @Override
    public String toString() {
        return "eyePos=("+Arrays.toString(eyePos)+"), worldPos=("+Arrays.toString(worldPos)+"), hitPos=("+Arrays.toString(hitPos)+ "), triangle="+t +", MouseEvent = "+e;
    }



}
