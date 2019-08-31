package suave;

import java.io.Serializable;
import javax.media.opengl.GL;

public class PosRot implements Serializable {

    float xPos = 0.0f;
    float yPos = 0.0f;
    float zPos = 0.0f;
    float xRot = 0.0f;
    float yRot = 0.0f;
    float zRot = 0.0f;
    boolean rotDirty = true;
    float[] rot = new float[16];

    public PosRot() {
    }

    public PosRot(PosRot old) {
        this.xPos = old.xPos;
        this.yPos = old.yPos;
        this.zPos = old.zPos;
        this.xRot = old.xRot;
        this.yRot = old.yRot;
        this.zRot = old.zRot;
    }

    public void setPos(float x, float y, float z) {
        xPos = x;
        yPos = y;
        zPos = z;
    }

    public void setRot(float x, float y, float z) {
        xRot = x;
        yRot = y;
        zRot = z;
        rotDirty = true;
    }

    public void position(GL gl) {
        gl.glTranslatef(xPos, yPos, zPos);
        if (rotDirty) {
            Rot44.createTotalRot(xRot, yRot, zRot, rot);
            rotDirty = false;
        }
        gl.glMultMatrixf(rot, 0);
    }

    public String toString() {
        return "pos " + xPos + ", " + yPos + ", " + zPos + " rot " + xRot + ", " + yRot + ", " + zRot;
    }
}
