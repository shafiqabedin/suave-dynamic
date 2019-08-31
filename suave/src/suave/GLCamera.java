package suave;

import java.text.DecimalFormat;

public class GLCamera {

    private DecimalFormat fmt = new DecimalFormat("0.000");
    // @TODO: Change these to constants?
    private float moveSpeedDefault = 200f;
    private float moveSpeed = moveSpeedDefault;
    private float moveSpeedDelta = 60f;
    private float shiftMoveMult = 10f;
    private float shiftRotMult = 4f;
    private float rotSpeedDefault = 60f;
    private float rotSpeed = rotSpeedDefault;
    private float rotSpeedDelta = 60f;
    private float yrotrad;
    private float xrotrad;
    private boolean toggleState = true;
    // Need to track;
    //
    // position - x, y, z
    // orientation - rotation in x, rotation in y, rotation in z
    //
    // need controls to rotate plus/minus in x, y, z axes
    // need controls to move plus/minus in all three local axes
    private long previousTime = System.currentTimeMillis();
    // Set default position and rotation to be approximately where the
    // viewer is.
    // Renderer.update: loc=0.000,1277.000,-1410.641 rot 331.920,357.180,0.000 move speed 120.000 rot speed 60.000
    //     private float xPosDefault = 0.0f;
    //     private float yPosDefault = 1300.0f;
    //     private float zPosDefault = -1400.0f;
    //    private float xRotDefault = 330.0f;
    //     private float yRotDefault = 0.0f;
    //     private float zRotDefault = 0.0f;
    // in 'front' of the gate facing in at fairly low altitude
    //     private float xPosDefault = 0.0f;
    //     private float yPosDefault = 370.0f;
    //     private float zPosDefault = -580.0f;
    //     private float xRotDefault = 0.0f;
    //     private float yRotDefault = 0.0f;
    //     private float zRotDefault = 0.0f;
    //     // over where the base station usually is, facing down hill, a little up in the air.
    //     private float xPosDefault = 474.0f;
    //     private float yPosDefault = 405.0f;
    //     private float zPosDefault = 805.0f;
    //     private float xRotDefault = 334.0f;
    //     private float yRotDefault = 179.0f;
    //     private float zRotDefault = 0.0f;
    // over where the base station usually is, facing down hill, a
    // little up in the air - further up and further back so we can
    // see everything.
    //
    // 1 : GLCamera.update: loc=(474.000, 620.520, 1077.040) rot=(334.000, 179.000, 0.000) move speed 120.000 rot speed 60.000
    //     private float xPosDefault = 474.0f;
    //     private float yPosDefault = 620.0f;
    //     private float zPosDefault = 1077.0f;
    //     private float xRotDefault = 334.0f;
    //     private float yRotDefault = 179.0f;
    //     private float zRotDefault = 0.0f;
    // Nearer the gate, for the sanjaya integration video.
    //
    // 1 : GLCamera.update: loc=(237.440, 527.040, -95.000) rot=(339.000, 355.000, 0.000) move speed 120.000 rot speed 60.000
    private float xPosDefault = 237.0f;
    private float yPosDefault = 527.0f;
    private float zPosDefault = -95.0f;
    private float xRotDefault = -339.0f;
    private float yRotDefault = 355.0f;
    private float zRotDefault = 0.0f;
    public float xPos = xPosDefault;
    public float yPos = yPosDefault;
    public float zPos = zPosDefault;
    public float xRot = xRotDefault;
    public float yRot = yRotDefault;
    public float zRot = zRotDefault;
    private float lastMoveSpeed = 0.0f;
    private float lastRotSpeed = 0.0f;
    private float lastXRot = 0.0f;
    private float lastYRot = 0.0f;
    private float lastZRot = 0.0f;
    private float lastXPos = 0.0f;
    private float lastYPos = 0.0f;
    private float lastZPos = 0.0f;
    private boolean shift;
    private boolean xPosPlus;
    private boolean xPosMinus;
    private boolean xPosReset;
    private boolean yPosPlus;
    private boolean yPosMinus;
    private boolean yPosReset;
    private boolean zPosPlus;
    private boolean zPosMinus;
    private boolean forwardLevel;
    private boolean backwardLevel;
    private boolean zPosReset;
    private boolean xRotPlus;
    private boolean xRotMinus;
    private boolean xRotReset;
    private boolean yRotPlus;
    private boolean yRotMinus;
    private boolean yRotReset;
    private boolean zRotPlus;
    private boolean zRotMinus;
    private boolean zRotReset;
    private boolean moveSpeedPlus;
    private boolean moveSpeedMinus;
    private boolean moveSpeedReset;
    private boolean rotSpeedPlus;
    private boolean rotSpeedMinus;
    private boolean rotSpeedReset;
    public boolean useDirectionVector = false;
    public float dirx;
    public float diry;
    public float dirz;
    public float upx;
    public float upy;
    public float upz;

    public void setDirectionAndUp(double dirx,
            double diry,
            double dirz,
            double upx,
            double upy,
            double upz) {
        this.dirx = (float) dirx;
        this.diry = (float) diry;
        this.dirz = (float) dirz;
        this.upx = (float) upx;
        this.upy = (float) upy;
        this.upz = (float) upz;
        useDirectionVector = true;
    }

    public GLCamera() {
    }

    public void xRotPlus(boolean flag) {
        xRotPlus = flag;
    }

    public void xRotMinus(boolean flag) {
        xRotMinus = flag;
    }

    public void xRotReset(boolean flag) {
        xRotReset = flag;
    }

    public void yRotPlus(boolean flag) {
        yRotPlus = flag;
    }

    public void yRotMinus(boolean flag) {
        yRotMinus = flag;
    }

    public void yRotReset(boolean flag) {
        yRotReset = flag;
    }

    public void zRotPlus(boolean flag) {
        zRotPlus = flag;
    }

    public void zRotMinus(boolean flag) {
        zRotMinus = flag;
    }

    public void zRotReset(boolean flag) {
        zRotReset = flag;
    }

    public void shift(boolean flag) {
        shift = flag;
    }

    public void xPosPlus(boolean flag) {
        xPosPlus = flag;
    }

    public void xPosMinus(boolean flag) {
        xPosMinus = flag;
    }

    public void xPosReset(boolean flag) {
        xPosReset = flag;
    }

    public void yPosPlus(boolean flag) {
        yPosPlus = flag;
    }

    public void yPosMinus(boolean flag) {
        yPosMinus = flag;
    }

    public void yPosReset(boolean flag) {
        yPosReset = flag;
    }

    public void zPosPlus(boolean flag) {
        zPosPlus = flag;
    }

    public void zPosMinus(boolean flag) {
        zPosMinus = flag;
    }
    public void forwardLevel(boolean flag) {
        forwardLevel = flag;
    }

    public void backwardLevel(boolean flag) {
        backwardLevel = flag;
    }

    public void zPosReset(boolean flag) {
        zPosReset = flag;
    }

    public void moveSpeedPlus(boolean flag) {
        moveSpeedPlus = flag;
    }

    public void moveSpeedMinus(boolean flag) {
        moveSpeedMinus = flag;
    }

    public void moveSpeedReset(boolean flag) {
        moveSpeedReset = flag;
    }

    public void rotSpeedPlus(boolean flag) {
        rotSpeedPlus = flag;
    }

    public void rotSpeedMinus(boolean flag) {
        rotSpeedMinus = flag;
    }

    public void rotSpeedReset(boolean flag) {
        rotSpeedReset = flag;
    }

    public void setToSouthEastViewPoint(boolean flag) {
        //  loc=(661.440, 382.560, -228.600) rot=(351.820, 28.160, 0.000)
        xPos = 661.440f;
        yPos = 382.560f;
        zPos = -228.600f;
        //xRot = 351.820f;
        //yRot = 28.160f;
        xRot = 0.000f;
        yRot = 0.000f;
        zRot = 0.000f;
    }

    public void update() {    // Perform Motion Updates Here
        long time = System.currentTimeMillis();
        long milliseconds = time - previousTime;
        previousTime = time;

        float ratio = (float) (milliseconds) / 1000.0f;
        float rotSpeedRatio = rotSpeed * ratio;
        float moveSpeedRatio = moveSpeed * ratio;
        float rotSpeedDeltaRatio = rotSpeedDelta * ratio;
        float moveSpeedDeltaRatio = moveSpeedDelta * ratio;

        if (shift) {
            moveSpeedRatio *= shiftMoveMult;
            rotSpeedRatio *= shiftRotMult;
        }

        if (xRotPlus) {
            xRot += rotSpeedRatio;

            if (xRot > 360) {
                xRot -= 360;
            }
        }
        if (xRotMinus) {
            xRot -= rotSpeedRatio;

            if (xRot < 0) {
                xRot += 360;
            }
        }
        if (xRotReset) {
            xRot = xRotDefault;
        }

        if (yRotPlus) {
            yRot += rotSpeedRatio;
            Debug.debug(1, "GLCamera.update: YRot+=" + yRot);
            if (yRot > 360) {
                yRot -= 360;
            }
        }
        if (yRotMinus) {
            yRot -= rotSpeedRatio;
            Debug.debug(1, "GLCamera.update: YRot-=" + yRot);
            if (yRot < 0) {
                yRot += 360;
            }
        }
        if (yRotReset) {
            yRot = yRotDefault;
        }

        if (zRotPlus) {
            zRot += rotSpeedRatio;
            if (zRot > 360) {
                zRot -= 360;
            }
        }
        if (zRotMinus) {
            zRot -= rotSpeedRatio;
            if (zRot < 0) {
                zRot += 360;
            }
        }
        if (zRotReset) {
            zRot = zRotDefault;
        }

        if (xPosPlus) {
            //xPos += moveSpeedRatio;
            yrotrad = (float) Math.toRadians(yRot);
            xrotrad = (float) Math.toRadians(xRot);
            zPos += (Math.sin(yrotrad) * moveSpeedRatio);
            xPos += (Math.cos(yrotrad) * moveSpeedRatio);
            //yPos += (Math.sin(xrotrad) * moveSpeedRatio);
        }
        if (xPosMinus) {
            //xPos -= moveSpeedRatio;
            yrotrad = (float) Math.toRadians(yRot);
            xrotrad = (float) Math.toRadians(xRot);
            zPos -= (Math.sin(yrotrad) * moveSpeedRatio);
            xPos -= (Math.cos(yrotrad) * moveSpeedRatio);
            //yPos -= (Math.sin(xrotrad) * moveSpeedRatio);
        }
        if (xPosReset) {
            xPos = xPosDefault;
        }

        if (yPosPlus) {
            yPos += moveSpeedRatio;
        }
        if (yPosMinus) {
            yPos -= moveSpeedRatio;
        }
        if (yPosReset) {
            yPos = yPosDefault;
        }

        if (zPosPlus) {
            yrotrad = (float) Math.toRadians(yRot);
            xrotrad = (float) Math.toRadians(xRot);
            //zPos += moveSpeedRatio;
            zPos += (Math.cos(yrotrad) * moveSpeedRatio);
            xPos -= (Math.sin(yrotrad) * moveSpeedRatio);
            if (toggleState) {
                yPos += (Math.sin(xrotrad) * moveSpeedRatio);
            }

        }
        if (zPosMinus) {
            yrotrad = (float) Math.toRadians(yRot);
            xrotrad = (float) Math.toRadians(xRot);
            //zPos -= moveSpeedRatio;
            zPos -= (Math.cos(yrotrad) * moveSpeedRatio);
            xPos += (Math.sin(yrotrad) * moveSpeedRatio);
            if (toggleState) {
                yPos -= (Math.sin(xrotrad) * moveSpeedRatio);
            }

        }
        if (zPosReset) {
            zPos = zPosDefault;
        }

        if (forwardLevel) {
            // leave Y alone, only move along x and y
            yrotrad = (float) Math.toRadians(yRot);
            xrotrad = (float) Math.toRadians(xRot);
            zPos -= (Math.cos(yrotrad) * moveSpeedRatio);
            xPos += (Math.sin(yrotrad) * moveSpeedRatio);
        }
        if (backwardLevel) {
            // leave Y alone, only move along x and y
            yrotrad = (float) Math.toRadians(yRot);
            xrotrad = (float) Math.toRadians(xRot);
            zPos += (Math.cos(yrotrad) * moveSpeedRatio);
            xPos -= (Math.sin(yrotrad) * moveSpeedRatio);
        }

        if (moveSpeedPlus) {
            moveSpeed += moveSpeedDeltaRatio;
        }
        if (moveSpeedMinus) {
            moveSpeed -= moveSpeedDeltaRatio;
        }
        if (moveSpeedReset) {
            moveSpeed = moveSpeedDefault;
        }

        if (rotSpeedPlus) {
            rotSpeed += rotSpeedDeltaRatio;
        }
        if (rotSpeedMinus) {
            rotSpeed -= rotSpeedDeltaRatio;
        }
        if (rotSpeedReset) {
            rotSpeed = rotSpeedDefault;
        }

        if ((xPos != lastXPos)
                || (yPos != lastYPos)
                || (zPos != lastZPos)
                || (moveSpeed != lastMoveSpeed)
                || (rotSpeed != lastRotSpeed)
                || (xRot != lastXRot)
                || (yRot != lastYRot)
                || (zRot != lastZRot)) {
            Debug.debug(1, "GLCamera.update: loc=(" + fmt.format(xPos) + ", " + fmt.format(yPos) + ", " + fmt.format(zPos) + ") rot=(" + fmt.format(xRot) + ", " + fmt.format(yRot) + ", " + fmt.format(zRot) + ") move speed " + fmt.format(moveSpeed) + " rot speed " + fmt.format(rotSpeed));
            lastXPos = xPos;
            lastYPos = yPos;
            lastZPos = zPos;
            lastXRot = xRot;
            lastYRot = yRot;
            lastZRot = zRot;
            lastMoveSpeed = moveSpeed;
            lastRotSpeed = rotSpeed;
        }
    }

    public void changeView(boolean flag) {
        Debug.debug(1, "GLCamera.update: Toggle: " + toggleState);
        if (toggleState == true) {
            xRot = 90;
            toggleState = false;
        } else {
            toggleState = true;
            xRot = 0;
        }
    }

    public String toString() {
        return "GLCamera: pos " + xPos + ", " + yPos + ", " + zPos + " rot " + xRot + ", " + yRot + ", " + zRot;
    }
}
