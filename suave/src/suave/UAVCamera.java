package suave;


//import OBJLoader.OBJModel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

// http://www.java-tips.org/other-api-tips/jogl/use-of-some-of-the-gluquadric-routines.html
public class UAVCamera implements Renderable {

    private final static boolean DRAW_MULTIPLE_UAVS_FOR_SCREENSHOTS = false;
    private final static boolean DRAW_OBJ_MODEL = false;
    private final static boolean PLACE_UAV_BEHIND_ORIGIN = true;
    private final static boolean ADD_WINGS = true;
    private final static boolean ADD_TRAILING_CONE = false;
    private final static boolean ADD_UP_CONE = true;
    private final static boolean ADD_FORWARD_CYLINDER = true;
    private final static boolean ADD_AXIS_CYLINDERS = false;
    private final static boolean ADD_FOV = false;
    // @TODO: Note, the units are meters, so these are actually quite
    // a bit larger than the real UAVs - 2.2m diameter sphere and 8
    // meter wings.
    private final static float UAV_INITIAL_OFFSET = -4.0f;
    private final static double SCALE_FACTOR = 1;
    private final static double UAV_SPHERE_RADIUS = 1.1 * SCALE_FACTOR;
    private final static int UAV_SPHERE_SLICES = 15;
    private final static int UAV_SPHERE_STACKS = 10;
    private final static double UAV_CYL_BASE = 1 * SCALE_FACTOR;
    private final static double UAV_CYL_TOP = .1 * SCALE_FACTOR;
    private final static double UAV_CYL_HEIGHT = 8 * SCALE_FACTOR;
    private final static int UAV_CYL_SLICES = 15;
    private final static int UAV_CYL_STACKS = 5;
    private final static double UAV_UP_CYL_BASE = 1.1 * SCALE_FACTOR;
    private final static double UAV_UP_CYL_TOP = 0 * SCALE_FACTOR;
    private final static double UAV_UP_CYL_HEIGHT = 2.1 * SCALE_FACTOR;
    private final static int UAV_UP_CYL_SLICES = 15;
    private final static int UAV_UP_CYL_STACKS = 5;
    private final static double UAV_FORWARD_CYL_BASE = .3 * SCALE_FACTOR;
    private final static double UAV_FORWARD_CYL_TOP = .1 * SCALE_FACTOR;
    private final static double UAV_FORWARD_CYL_HEIGHT = 2.1 * SCALE_FACTOR;
    private final static int UAV_FORWARD_CYL_SLICES = 15;
    private final static int UAV_FORWARD_CYL_STACKS = 5;
    private final static double AXIS_CYL_BASE = .3;
    private final static double AXIS_CYL_TOP = .3;
    private final static double AXIS_CYL_HEIGHT = 10;
    private final static int AXIS_CYL_SLICES = 15;
    private final static int AXIS_CYL_STACKS = 5;
    private final static double FOV_CYL_BASE = .1;
    private final static double FOV_CYL_TOP = .1;
    private final static double FOV_CYL_HEIGHT = 400;
    private final static int FOV_CYL_SLICES = 15;
    private final static int FOV_CYL_STACKS = 5;
    private String uavModelFilename = null;
    private boolean initted = false;
    //    private OBJModel uavModel = null;
    private int[] uavTextId = new int[4];      // texture id
    private float xPos = 0.0f;
    private float yPos = -488.5325622558594f;
    private float zPos = -100.0f;
//     private float xPos = 0.0f;
//     private float yPos = 488.5325622558594f;
//     private float zPos = -100.0f;
    private float xRot = 0.0f;
    private float yRot = 0.0f;
    private float zRot = 0.0f;

    public PosRot getPosRot() {
        PosRot pr = new PosRot();
        pr.xPos = xPos;
        pr.yPos = yPos;
        pr.zPos = zPos;
        pr.xRot = xRot;
        pr.yRot = yRot;
        pr.zRot = zRot;
        return pr;
    }
    private float cameraYAngle = -15;
    private boolean rebuildQuadric = false;
    private int uavQuadricList = 0;
    private Thread myThread;
    private TextureReader.Texture uavTexture[] = new TextureReader.Texture[4];

    private void buildUavTexture(int index, Color mainColor, Color lineColor) {
        BufferedImage textImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = textImage.createGraphics();
        g.setColor(mainColor);
        g.fillRect(0, 0, 128, 128);
        g.setColor(lineColor);
        for (int loopx = 0; loopx < 128; loopx += 16) {
            g.drawLine(loopx, 0, loopx, 128);
        }
        g.setColor(lineColor);
        for (int loopy = 0; loopy < 128; loopy += 16) {
            g.drawLine(0, loopy, 128, loopy);
        }
        g.dispose();

        uavTexture[index] = TextureReader.createTexture(textImage, true);
    }
    private static int counter = 0;
    public int myCount = 0;

    public UAVCamera(String uavModelFilename) {
        this.uavModelFilename = uavModelFilename;

        Debug.debug(1, "Constructing UAV #" + myCount);

        myCount = counter++;

        buildUavTexture(0, Color.white, Color.red);
        buildUavTexture(1, Color.white, Color.blue);
        buildUavTexture(2, Color.white, Color.green);
        buildUavTexture(3, Color.white, Color.orange);
    }

    public void setupTexture(GL gl, int index) {
        gl.glBindTexture(GL.GL_TEXTURE_2D, uavTextId[index]);    // Bind The Texture
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, uavTexture[index].getWidth(), uavTexture[index].getHeight(), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, uavTexture[index].getPixels());
    }

    public void setPosition(float newXPos, float newYPos, float newZPos, float newXRot, float newYRot, float newZRot) {
        xPos = newXPos;
        yPos = newYPos;
        zPos = newZPos;
        xRot = newXRot;
        yRot = newYRot;
        zRot = newZRot;
        //	Debug.debug(1, "UAVCamera.setPosition: pos "+xPos+" , "+yPos+" , "+zPos+" rot "+xRot+" , "+yRot+" , "+zRot);
    }

    public String getPosRotString() {
        return "pos " + xPos + ", " + yPos + ", " + zPos + " rot " + xRot + ", " + yRot + ", " + zRot;
    }

    private void buildQuadric(GL gl) {

        GLU glu = new GLU();
        GLUquadric quadric = null;

        if (!rebuildQuadric) {
            gl.glGenTextures(4, uavTextId, 0);      // Get An Open ID
            if (uavTextId[0] == 0) {
                Debug.debug(4,"UAVCamera.buildQuadric:  Got ZERO for our texture ID!  This is probably bad.");
            } else {
                Debug.debug(1,"UAVCamera.buildQuadric:  got uav text id= " + uavTextId[0]);
            }
        }

        gl.glPushMatrix();                        //save first position

        uavQuadricList = gl.glGenLists(1);
        quadric = glu.gluNewQuadric();

        glu.gluQuadricOrientation(quadric, glu.GLU_OUTSIDE);

        glu.gluQuadricTexture(quadric, true);
        setupTexture(gl, 0);
        setupTexture(gl, 1);
        setupTexture(gl, 2);
        setupTexture(gl, 3);

        glu.gluQuadricDrawStyle(quadric, GLU.GLU_FILL);
        glu.gluQuadricNormals(quadric, GLU.GLU_SMOOTH);
        gl.glNewList(uavQuadricList, GL.GL_COMPILE);

        if (PLACE_UAV_BEHIND_ORIGIN) {
            // This is another attempt to make sharing the UAV's
            // viewpoint work better.  The idea here is to draw the
            // UAV model as if the UAV position is at the tip of the
            // nose of the UAV.  (it isn't in reality, the camera is
            // placed on the underside pointing down by some number of
            // degrees - 15?  30? )  So to position the model just
            // 'behind' the uav viewpoint, we need to move the origin
            // point.
            gl.glTranslatef(0.0f, 0.0f, UAV_INITIAL_OFFSET);
        }
        glu.gluSphere(quadric, UAV_SPHERE_RADIUS, UAV_SPHERE_SLICES, UAV_SPHERE_STACKS);

        if (ADD_FORWARD_CYLINDER) {
            // cylinder sticking straight front from the UAV.
            glu.gluCylinder(quadric, UAV_FORWARD_CYL_BASE, UAV_FORWARD_CYL_TOP, UAV_FORWARD_CYL_HEIGHT, UAV_FORWARD_CYL_SLICES, UAV_FORWARD_CYL_STACKS);
        }

        if (ADD_FOV) {
            double fovY = Baker.VIDEO_FOV_Y;
            double fovX = fovY * Baker.VIDEO_ASPECT_RATIO;

            float halfY = (float) (fovY / 2);
            float halfX = (float) (fovX / 2);

            float[] rot = new float[16];

            gl.glPushMatrix();
            Rot44.createTotalRot(halfY - cameraYAngle, halfX, 0, rot);
            gl.glMultMatrixf(rot, 0);
            glu.gluCylinder(quadric, FOV_CYL_BASE, FOV_CYL_TOP, FOV_CYL_HEIGHT, FOV_CYL_SLICES, FOV_CYL_STACKS);
            gl.glPopMatrix();

            gl.glPushMatrix();
            Rot44.createTotalRot(-halfY - cameraYAngle, halfX, 0, rot);
            gl.glMultMatrixf(rot, 0);
            glu.gluCylinder(quadric, FOV_CYL_BASE, FOV_CYL_TOP, FOV_CYL_HEIGHT, FOV_CYL_SLICES, FOV_CYL_STACKS);
            gl.glPopMatrix();

            gl.glPushMatrix();
            Rot44.createTotalRot(-halfY - cameraYAngle, -halfX, 0, rot);
            gl.glMultMatrixf(rot, 0);
            glu.gluCylinder(quadric, FOV_CYL_BASE, FOV_CYL_TOP, FOV_CYL_HEIGHT, FOV_CYL_SLICES, FOV_CYL_STACKS);
            gl.glPopMatrix();

            gl.glPushMatrix();
            Rot44.createTotalRot(halfY - cameraYAngle, -halfX, 0, rot);
            gl.glMultMatrixf(rot, 0);
            glu.gluCylinder(quadric, FOV_CYL_BASE, FOV_CYL_TOP, FOV_CYL_HEIGHT, FOV_CYL_SLICES, FOV_CYL_STACKS);
            gl.glPopMatrix();


// 	    gl.glRotatef(halfY, 1.0f, 0.0f, 0.0f);
// 	    glu.gluCylinder(quadric,FOV_CYL_BASE,FOV_CYL_TOP,FOV_CYL_HEIGHT,FOV_CYL_SLICES,FOV_CYL_STACKS);
// 	    gl.glRotatef(-halfY, 1.0f, 0.0f, 0.0f);

// 	    gl.glRotatef(-halfY, 1.0f, 0.0f, 0.0f);
// 	    glu.gluCylinder(quadric,FOV_CYL_BASE,FOV_CYL_TOP,FOV_CYL_HEIGHT,FOV_CYL_SLICES,FOV_CYL_STACKS);
// 	    gl.glRotatef(halfY, 1.0f, 0.0f, 0.0f);

//  	    gl.glRotatef(halfX, 0.0f, 1.0f, 0.0f);
//  	    glu.gluCylinder(quadric,FOV_CYL_BASE,FOV_CYL_TOP,FOV_CYL_HEIGHT,FOV_CYL_SLICES,FOV_CYL_STACKS);
//  	    gl.glRotatef(-halfX, 0.0f, 1.0f, 0.0f);

//  	    gl.glRotatef(-halfX, 0.0f, 1.0f, 0.0f);
//  	    glu.gluCylinder(quadric,FOV_CYL_BASE,FOV_CYL_TOP,FOV_CYL_HEIGHT,FOV_CYL_SLICES,FOV_CYL_STACKS);
//  	    gl.glRotatef(halfX, 0.0f, 1.0f, 0.0f);

// 	    gl.glRotatef(halfY, 1.0f, 0.0f, 0.0f);
//  	    gl.glRotatef(halfX, 0.0f, 1.0f, 0.0f);
// 	    glu.gluCylinder(quadric,FOV_CYL_BASE,FOV_CYL_TOP,FOV_CYL_HEIGHT,FOV_CYL_SLICES,FOV_CYL_STACKS);
//  	    gl.glRotatef(-halfX, 0.0f, 1.0f, 0.0f);
// 	    gl.glRotatef(-halfY, 1.0f, 0.0f, 0.0f);

// 	    gl.glRotatef(-halfY, 1.0f, 0.0f, 0.0f);
//  	    gl.glRotatef(-halfX, 0.0f, 1.0f, 0.0f);
// 	    glu.gluCylinder(quadric,FOV_CYL_BASE,FOV_CYL_TOP,FOV_CYL_HEIGHT,FOV_CYL_SLICES,FOV_CYL_STACKS);
//  	    gl.glRotatef(halfX, 0.0f, 1.0f, 0.0f);
// 	    gl.glRotatef(halfY, 1.0f, 0.0f, 0.0f);

// 	    gl.glRotatef(halfY, 1.0f, 0.0f, 0.0f);
//  	    gl.glRotatef(-halfX, 0.0f, 1.0f, 0.0f);
// 	    glu.gluCylinder(quadric,FOV_CYL_BASE,FOV_CYL_TOP,FOV_CYL_HEIGHT,FOV_CYL_SLICES,FOV_CYL_STACKS);
//  	    gl.glRotatef(halfX, 0.0f, 1.0f, 0.0f);
// 	    gl.glRotatef(-halfY, 1.0f, 0.0f, 0.0f);

// 	    gl.glRotatef(-halfY, 1.0f, 0.0f, 0.0f);
//  	    gl.glRotatef(halfX, 0.0f, 1.0f, 0.0f);
// 	    glu.gluCylinder(quadric,FOV_CYL_BASE,FOV_CYL_TOP,FOV_CYL_HEIGHT,FOV_CYL_SLICES,FOV_CYL_STACKS);
//  	    gl.glRotatef(-halfX, 0.0f, 1.0f, 0.0f);
// 	    gl.glRotatef(halfY, 1.0f, 0.0f, 0.0f);
        }

        if (ADD_AXIS_CYLINDERS) {
            glu.gluCylinder(quadric, AXIS_CYL_BASE, AXIS_CYL_TOP, AXIS_CYL_HEIGHT, AXIS_CYL_SLICES, AXIS_CYL_STACKS);
            gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
            glu.gluCylinder(quadric, AXIS_CYL_BASE, AXIS_CYL_TOP, AXIS_CYL_HEIGHT, AXIS_CYL_SLICES, AXIS_CYL_STACKS);
            gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
            gl.glRotatef(90.0f, 0.0f, 1.0f, 0.0f);
            glu.gluCylinder(quadric, AXIS_CYL_BASE, AXIS_CYL_TOP, AXIS_CYL_HEIGHT, AXIS_CYL_SLICES, AXIS_CYL_STACKS);
            gl.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
        }

        if (ADD_TRAILING_CONE) {
            // cone sticking straight out behind the UAV.
            glu.gluCylinder(quadric, UAV_CYL_BASE, UAV_CYL_TOP, UAV_CYL_HEIGHT, UAV_CYL_SLICES, UAV_CYL_STACKS);
        }

        if (ADD_UP_CONE) {
            // cone sticking straight up from the UAV.
            gl.glRotatef(-90.0f, 1.0f, 0.0f, 0.0f);
            glu.gluCylinder(quadric, UAV_UP_CYL_BASE, UAV_UP_CYL_TOP, UAV_UP_CYL_HEIGHT, UAV_UP_CYL_SLICES, UAV_UP_CYL_STACKS);
            gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
        }

        if (ADD_WINGS) {
            gl.glRotatef(120.0f, 0.0f, 1.0f, 0.0f);
            glu.gluCylinder(quadric, UAV_CYL_BASE, UAV_CYL_TOP, UAV_CYL_HEIGHT, UAV_CYL_SLICES, UAV_CYL_STACKS);

            gl.glRotatef(-240.0f, 0.0f, 1.0f, 0.0f);
            glu.gluCylinder(quadric, UAV_CYL_BASE, UAV_CYL_TOP, UAV_CYL_HEIGHT, UAV_CYL_SLICES, UAV_CYL_STACKS);
        }

        gl.glEndList();
    }

    public void init(GL gl) {
        if (null != uavModelFilename) {
            System.err.println("UAVCamera.init: Loading OBJModel " + uavModelFilename);
            System.err.flush();
            //	    uavModel = new OBJModel(uavModelFilename, 1.0f, gl,true);
            System.err.println("UAVCamera.init: Done loading OBJModel " + uavModelFilename);
            System.err.flush();
        }
        buildQuadric(gl);

        //	start();
    }

    public void render(GL gl) {
        if (!initted) {
            init(gl);
            initted = true;
        }
        //	Debug.debug(1,"Rendering UAV #"+myCount);

        gl.glPushMatrix();
        gl.glTranslatef(xPos, yPos, zPos);

        float[] rot = new float[16];
        // NOTE: We subtract yRot from 180 because our model is backwards...
        Rot44.createTotalRot(xRot, 180 - yRot, zRot, rot);
        gl.glMultMatrixf(rot, 0);

        if ( // null!= uavModel &&
                DRAW_OBJ_MODEL) {
            //	    uavModel.draw(gl);
        } else {
            if (rebuildQuadric) {
                buildQuadric(gl);
                rebuildQuadric = false;
            }

            if (DRAW_MULTIPLE_UAVS_FOR_SCREENSHOTS) {
                Debug.debug(1, "Drawing multiple UAVs");
                float savexPos = xPos;
                float saveyPos = yPos;
                float savezPos = zPos;
                float savexRot = xRot;
                float saveyRot = yRot;
                float savezRot = zRot;


// 1 : UAVCamera.setPosition: pos 477.89172 , 374.6228 , 575.7602 rot -7.5515485 , 145.77109 , -11.287269
// 1 : UAVCamera.setPosition: pos 507.67126 , 385.12845 , 480.03778 rot -14.598929 , 269.24347 , -26.012283
// 1 : UAVCamera.setPosition: pos 556.2701 , 389.95245 , 545.22815 rot -17.062649 , 25.73642 , -19.881636
//
// 1 : UAVCamera.setPosition: pos 468.9216 , 397.97714 , 269.0404 rot -17.406424 , 203.69711 , -9.110029
// 1 : UAVCamera.setPosition: pos 573.08435 , 386.96793 , 285.59366 rot -10.989296 , 315.3093 , 7.1046767
// 1 : UAVCamera.setPosition: pos 587.9213 , 381.46204 , 372.87997 rot -14.255155 , 147.26077 , 4.182592
// 1 : UAVCamera.setPosition: pos 471.45752 , 379.9708 , 388.0855 rot -8.754761 , 181.75282 , 8.537071
// 1 : UAVCamera.setPosition: pos 480.54175 , 380.80988 , 262.71057 rot -10.588224 , 193.72765 , -1.8907608

                xPos = 477.89172f;
                yPos = 374.6228f;
                zPos = 575.7602f;
                xRot = -7.5515485f;
                yRot = 145.77109f;
                zRot = -11.287269f;
                gl.glBindTexture(GL.GL_TEXTURE_2D, uavTextId[0]);    // Bind The Texture
                gl.glCallList(uavQuadricList);

                xPos = 507.67126f;
                yPos = 385.12845f;
                zPos = 480.03778f;
                xRot = -14.598929f;
                yRot = 269.24347f;
                zRot = -26.012283f;
                gl.glBindTexture(GL.GL_TEXTURE_2D, uavTextId[1]);    // Bind The Texture
                gl.glCallList(uavQuadricList);

                xPos = 556.2701f;
                yPos = 389.95245f;
                zPos = 545.22815f;
                xRot = -17.062649f;
                yRot = 25.73642f;
                zRot = -19.881636f;
                gl.glBindTexture(GL.GL_TEXTURE_2D, uavTextId[2]);    // Bind The Texture
                gl.glCallList(uavQuadricList);

                xPos = 468.9216f;
                yPos = 397.97714f;
                zPos = 269.0404f;
                xRot = -17.406424f;
                yRot = 203.69711f;
                zRot = -9.110029f;
                gl.glBindTexture(GL.GL_TEXTURE_2D, uavTextId[3]);    // Bind The Texture
                gl.glCallList(uavQuadricList);

                xPos = 573.08435f;
                yPos = 386.96793f;
                zPos = 285.59366f;
                xRot = -10.989296f;
                yRot = 315.3093f;
                zRot = 7.1046767f;
                gl.glBindTexture(GL.GL_TEXTURE_2D, uavTextId[0]);    // Bind The Texture
                gl.glCallList(uavQuadricList);

                xPos = 587.9213f;
                yPos = 381.46204f;
                zPos = 372.87997f;
                xRot = -14.255155f;
                yRot = 147.26077f;
                zRot = 4.182592f;
                gl.glBindTexture(GL.GL_TEXTURE_2D, uavTextId[1]);    // Bind The Texture
                gl.glCallList(uavQuadricList);

                xPos = 471.45752f;
                yPos = 379.9708f;
                zPos = 388.0855f;
                xRot = -8.754761f;
                yRot = 181.75282f;
                zRot = 8.537071f;
                gl.glBindTexture(GL.GL_TEXTURE_2D, uavTextId[2]);    // Bind The Texture
                gl.glCallList(uavQuadricList);

                xPos = 480.54175f;
                yPos = 380.80988f;
                zPos = 262.71057f;
                xRot = -10.588224f;
                yRot = 193.72765f;
                zRot = -1.8907608f;
                gl.glBindTexture(GL.GL_TEXTURE_2D, uavTextId[3]);    // Bind The Texture
                gl.glCallList(uavQuadricList);


                xPos = savexPos;
                yPos = saveyPos;
                zPos = savezPos;
                xRot = savexRot;
                yRot = saveyRot;
                zRot = savezRot;
            } else {
                gl.glBindTexture(GL.GL_TEXTURE_2D, uavTextId[0]);    // Bind The Texture
                gl.glCallList(uavQuadricList);
            }
        }

        gl.glPopMatrix();
        gl.glFlush();
    }

    public void destroy(GL gl) {
        // @TODO: actually free the display list - I really wnat to
        // deprecate this object though.
    }

    public void incCameraYAngle() {
        cameraYAngle += 1;
        rebuildQuadric = true;
        System.err.println("UAVCamera.incCameraYAngle: cameraYAngle = " + cameraYAngle);
    }

    public void decCameraYAngle() {
        cameraYAngle -= 1;
        rebuildQuadric = true;
        System.err.println("UAVCamera.decCameraYAngle: cameraYAngle = " + cameraYAngle);
    }

    public void incXPos() {
        xPos += 1;
        System.err.println("UAVCamera.incXPos: xPos = " + xPos);
    }

    public void decXPos() {
        xPos -= 1;
        System.err.println("UAVCamera.decXPos: xPos = " + xPos);
    }

    public void incYPos() {
        yPos += 1;
        System.err.println("UAVCamera.incYPos: yPos = " + yPos);
    }

    public void decYPos() {
        yPos -= 1;
        System.err.println("UAVCamera.decYPos: yPos = " + yPos);
    }
}

