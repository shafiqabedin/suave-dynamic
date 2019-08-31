package suave;

// import demos.common.GLDisplay;
// import demos.common.TextureReader;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import com.sun.opengl.util.GLUT;

import javax.swing.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

class Renderer implements GLEventListener {

    public final static boolean USE_LIGHTS = false;
    public final static boolean USE_PAINTER = false;
    public final static boolean ADD_UAV_PATH_LINE = false;
    // NOTE: this defines what we see of the world in particular the
    // last two values (which should always be positive) specify near
    // and far clipping planes, i.e. anything closer than the second
    // to last value will not be rendered, anything further than the
    // last value will not be rendered.
    public final static double NEAR_CLIP_PLANE_DEPTH = 1;
    public final static double FAR_CLIP_PLANE_DEPTH = 10000;
//    public final static double FOVY = 45;
    public final static double FOVY = 69.9840404; // VBS2
    private int windowHeight = 1;
    private int windowWidth = 1;
    //    public final static double ASPECT_RATIO = 45;
    private final boolean UPDATE_BAKER = true;
    public final static DecimalFormat fmt = new DecimalFormat("   0.00");
    private GLDisplay glDisplay;
    private Lights lights = null;
    private Model model = null;
    private Mesh mesh = null;
    private GLCamera camera;
    private Baker baker = null;
    private Select selector = null;
    private boolean videoMode = false;
    private CameraCalibration cameraCalibration;
    private Origin origin;
    private boolean displayFrameRate = true;
    private Projection projection = null;
    private Painter painter = null;
    private Rasterize rasterize = null;
    private BlockingQueue<Message> incomingMsgQ = new LinkedBlockingQueue<Message>();
    private GLU glu = new GLU();

    public BlockingQueue<Message> getIncomingMsgQ() {
        return incomingMsgQ;
    }
    // @TODO: Factor ALL of the camera handling code out to it's own class
    private boolean meshReady = false;

    public Renderer(GLDisplay glDisplay, Lights lights, Model model, Mesh mesh, GLCamera camera, Baker baker, Select selector, boolean videoMode, CameraCalibration cameraCalibration, Origin origin) {
        this.glDisplay = glDisplay;
        this.lights = lights;
        this.model = model;
        this.mesh = mesh;
        this.camera = camera;
        this.baker = baker;
        this.selector = selector;
        this.videoMode = videoMode;
        this.displayFrameRate = !videoMode;
        this.cameraCalibration = cameraCalibration;
        this.origin = origin;
    }

    private void checkExtension(GL gl, String extensionName) {
        if (!gl.isExtensionAvailable(extensionName)) {
            String message = "Unable to initialize " + extensionName + " OpenGL extension";
            unavailableExtension(message);
        }
    }

    private void unavailableExtension(String message) {
        JOptionPane.showMessageDialog(null, message, "Unavailable extension", JOptionPane.ERROR_MESSAGE);
        throw new GLException(message);
    }

    // http://www.felixgers.de/teaching/jogl/vertexBufferObject.html
    private void checkInfo(GL gl) {
        boolean useVBO = true;

        // Check version.
        String versionStr = gl.glGetString(GL.GL_VERSION);
        Debug.debug(1,"Renderer.checkInfo: GL version:" + versionStr);
        // versionStr = versionStr.substring( 0, 4);
        //  float version = new Float( versionStr ).floatValue();
        //  boolean versionOK = ( version >= 1.59f ) ? true : false;
        Debug.debug(1,"Renderer.checkInfo: GL version:" + versionStr + "  needs to be > 1.59 to be ok");

        // @TODO: can probably get rid of this, it's for the
        // projective texturing that I ended up not doing.
        //
        // Check for projective texturing stuff
        try {
            checkExtension(gl, "GL_VERSION_1_3"); // For multitexture
            //	    checkExtension(gl, "GL_ARB_depth_texture");
            //	    checkExtension(gl, "GL_ARB_shadow");
            checkExtension(gl, "GL_ARB_pbuffer");
            checkExtension(gl, "GL_ARB_pixel_format");
        } catch (GLException e) {
            e.printStackTrace();
            throw (e);
        }

        // Check if extension is available.
        boolean extensionOK = gl.isExtensionAvailable("GL_ARB_vertex_buffer_object");
        Debug.debug(1,"Renderer.checkInfo: VBO extension: " + extensionOK);

        // Check for VBO functions.
        boolean functionsOK =
                gl.isFunctionAvailable("glGenBuffersARB")
                && gl.isFunctionAvailable("glBindBufferARB")
                && gl.isFunctionAvailable("glBufferDataARB")
                && gl.isFunctionAvailable("glDeleteBuffersARB");
        Debug.debug(1,"Renderer.checkInfo: Functions: " + functionsOK);

        if (!extensionOK || !functionsOK) {
            // VBO not supported.
            Debug.debug(1,"Renderer.checkInfo: VBOs not supported.");
            useVBO = false;
            return;
        } else {
            Debug.debug(1,"Renderer.checkInfo: VBOs ARE supported.");
        }
    }

    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        checkInfo(gl);
        // Check For VBO support
        final boolean VBOsupported = gl.isFunctionAvailable("glGenBuffersARB")
                && gl.isFunctionAvailable("glBindBufferARB")
                && gl.isFunctionAvailable("glBufferDataARB")
                && gl.isFunctionAvailable("glDeleteBuffersARB");


        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                String title = glDisplay.getTitle();
                if (VBOsupported) {
                    title += ", VBO supported";
                } else {
                    title += ", VBO not supported";
                }
                glDisplay.setTitle(title);
            }
        });


        // Setup GL States
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);   // Black Background
        gl.glClearDepth(1.0f);      // Depth Buffer Setup
        gl.glDepthFunc(GL.GL_LEQUAL);     // The Type Of Depth Testing (Less Or Equal)
        gl.glEnable(GL.GL_DEPTH_TEST);     // Enable Depth Testing
        gl.glShadeModel(GL.GL_SMOOTH);     // Select Smooth Shading
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST); // Set Perspective Calculations To Most Accurate
        gl.glEnable(GL.GL_TEXTURE_2D);     // Enable Textures

        if (USE_LIGHTS) {
            lights.addLights(gl);
        }

        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);    // Set The Color To White
        ExtraRenderables.addDangerAreas(gl, origin);
    }

    public void display(GLAutoDrawable drawable) {

        GL gl = drawable.getGL();

        // GLCamera instances store a location/rotation and our
        // 'camera' can also be updated via keypresses.  This
        // 'update()' call processes those key presses.
        camera.update();

        long frameRateStartTimeMs = System.currentTimeMillis();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear Screen And Depth Buffer
        gl.glMatrixMode(GL.GL_MODELVIEW);

        gl.glLoadIdentity();             // Reset The Modelview Matrix

        // JOGL axes;
        //
        // positive X is towards the right
        // positive Y is towards the ceiling
        // positive Z is towards your eyes

        // We're adding transforms to the overall transform matrix
        // that is done to render everything.  Because order matters when
        // it comes to matrix math, these are added in reverse.  We're
        // 'rotating the world around us'.
        long screenshotStart = System.currentTimeMillis();
        Message msg = incomingMsgQ.poll();
        CaptureCommand cc = null;
        if (msg != null) {
            if (msg.message instanceof CaptureCommand) {
                cc = (CaptureCommand) msg.message;
                gl.glDrawBuffer(GL.GL_BACK);
                camera.xRot = cc.posRot.xRot;
                camera.yRot = cc.posRot.yRot;
                camera.zRot = cc.posRot.zRot;
                camera.xPos = cc.posRot.xPos;
                camera.yPos = cc.posRot.yPos;
                camera.zPos = cc.posRot.zPos;
                Debug.debug(1, "Taking screenshot at " + camera);
            }
        }

        if (!camera.useDirectionVector) {
            // @TODO: Replace this with rotation matrix?
            gl.glRotatef(camera.xRot, 1.0f, 0.0f, 0.0f);
            gl.glRotatef(camera.yRot, 0.0f, 1.0f, 0.0f);
            gl.glRotatef(camera.zRot, 0.0f, 0.0f, 1.0f);
            gl.glTranslatef(-camera.xPos, -camera.yPos, -camera.zPos);
        } else {
            double tx = camera.xPos + 100 * camera.dirx;
            double ty = camera.yPos + 100 * camera.diry;
            double tz = camera.zPos + 100 * camera.dirz;
            glu.gluLookAt(camera.xPos, camera.yPos, camera.zPos, tx, ty, tz, camera.upx, camera.upy, camera.upz);

//            // JUST LOOK NORTH
//            double tx = camera.xPos + 100 * 0;
//            double ty = camera.yPos + 100 * 0;
//            double tz = camera.zPos + 100 * -1;
//            glu.gluLookAt(camera.xPos, camera.yPos, camera.zPos, tx, ty, tz, 0, 1, 0);

//            double tx = camera.xPos + 100 * camera.dirx;
//            double ty = camera.yPos + 100 * camera.diry;
//            double tz = camera.zPos + 100 * camera.dirz;
//            glu.gluLookAt(camera.xPos, camera.yPos, camera.zPos, tx, ty, tz, 0, 1, 0);


//            double tx = camera.xPos + 100 * 0;
//            double ty = camera.yPos + 100 * 0;
//            double tz = camera.zPos + 100 * -1;
//            glu.gluLookAt(camera.xPos, camera.yPos, camera.zPos, tx, ty, tz, camera.upx, camera.upy, camera.upz);


//            double tx = camera.xPos + 100 * camera.dirx;
//            double ty = camera.yPos ;
//            double tz = camera.zPos + 100 * camera.dirz;
//            glu.gluLookAt(camera.xPos, camera.yPos, camera.zPos, tx, ty, tz, 0, 1, 0);

//            Debug.debug(1,"Center = "+tx+", "+ty+", "+tz+" dir = "+camera.dirx+", "+ camera.diry+", "+camera.dirz+" up = "+camera.upx+", "+ camera.upy+", "+camera.upz);

        }

        if (USE_LIGHTS) {
            lights.render(gl);
        }

        // Render the mesh - check if it's ready, and if it is then
        // reset our camera position to be at the max altitude of the
        // mesh.
        if (!meshReady) {
            if (mesh.readyToDraw()) {
                meshReady = true;
                Debug.debug(1,"Renderer.display: creating Projection");
                projection = new Projection(mesh.getTriangles(), cameraCalibration);
                if (USE_PAINTER) {
                    Debug.debug(1,"Renderer.display: creating Painter");
                    painter = new Painter();
                    Debug.debug(1,"Renderer.display: creating Rasterize");
                }
                rasterize = new Rasterize(mesh.getImagery());
                Debug.debug(1,"Renderer.display: calling rasterize.update()");
                rasterize.update(projection.triangles);
                Debug.debug(1,"Renderer.display: done creating things, mesh is ready to draw.");
            }
        }

        if (meshReady) {
            // Note!  selector has to be called done RIGHT after we
            // set up our viewpoint, BEFORE we've done anything else
            // to mess with the viewpoint... i.e. rotations or
            // translations.
            projection.frustumCull(gl, camera);
            selector.update(gl, projection.trianglesAfterCulling);
        }

// 	// Draw the model normally
// 	gl.glColor3f(.5f,.5f,.5f);
// 	gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);

        if (ADD_UAV_PATH_LINE) {

            //	    ADD_UAV_PATH_LINE = false;
            //	    Debug.debug(1,"Adding testing of line display list");

            // "f:\\laptop\\owens\\2009_11_02\\telemetry_5_2009_11_02_00.m"
            // "f:\\laptop\\owens\\suave\\telemetry_UAV5_2008_10_22_00.m"
            ExtraRenderables.addTelemetryFile(gl, "C:\\laptop\\owens\\2009_11_02_uav_logs\\telemetry_5_2009_11_02_00.m", Color.blue);
        }

        model.render(gl);

        if (null != cc) {
            gl.glReadBuffer(GL.GL_BACK);
            ByteBuffer buf = ByteBuffer.allocateDirect(windowWidth * windowHeight * 4);
            gl.glReadPixels(0, 0, windowWidth, windowHeight, GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, buf);
            BufferedImage bufferedImage = saveScreenshot(buf, windowWidth, windowHeight);
            Debug.debug(1, "SCREENSHOT ran in " + (System.currentTimeMillis() - screenshotStart) + " ms");
            if (cc.saveToFile) {
                saveAsFile1(bufferedImage, cc.filename);
            } else {
                if (msg.client != null) {
                    Debug.debug(1, "Sending CaptureReply to client");
                    CaptureReply reply = new CaptureReply(cc, bufferedImage);
                    Message replyMsg = new Message(null, reply);
                    msg.client.addMessage(replyMsg);
                } else {
                    Debug.debug(1, "msg.client == null, can't send image to client");
                }
            }
            // @TODO: I think the problems I was seeing with OutOfMemory were from this buffer
            // not being freed.  What I should do is keep the buffer around and only re-alloc it if the window
            // size changes.
            buf = null;
        }

        if (meshReady) {
            long startTime;
            long elapsedTime;

            if (UPDATE_BAKER) {
                // @TODO: And the big question is, why am I calling projection.render() here?  Hmm.
                // Maybe in the past I didn't pass it into Baker?  Maybe this is a relic from before Baker had
                // it's own command queue?  Let's take it out and see what happens.
//                startTime = System.currentTimeMillis();
//                //		Debug.debug(1,"Renderer.display: Calling projection.render()");
//                projection.render(gl, camera);
//                elapsedTime = System.currentTimeMillis() - startTime;
//                //		Debug.debug(1,"Renderer.display: Done calling projection.render(), elapsed="+elapsedTime);

                startTime = System.currentTimeMillis();
                if (USE_PAINTER) {
                    Debug.debug(1,"Renderer.display: DOING PAINTER UPDATE");
                    painter.update(projection.trianglesAfterCulling, camera);
                    elapsedTime = System.currentTimeMillis() - startTime;
                    Debug.debug(1,"Renderer.display: DONE DOING PAINTER UPDATE, elapsedTime=" + elapsedTime);
                }
            }

            baker.bake(gl, projection, mesh);
        }

        long frameRateEndTimeMs = System.currentTimeMillis();
        if (displayFrameRate) {
            updateFrameRate(gl, frameRateStartTimeMs, frameRateEndTimeMs);
        }

        gl.glLoadIdentity();      // Reset The Modelview Matrix

        // @TODO: don't really need this...  at some point when I'm
        // sure everything is working, try commenting this out,
        // everything should still work.
        gl.glRotatef(camera.xRot, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(camera.yRot, 0.0f, 1.0f, 0.0f);
        gl.glRotatef(camera.zRot, 0.0f, 0.0f, 1.0f);
        gl.glTranslatef(-camera.xPos, -camera.yPos, -camera.zPos);
    }

    private BufferedImage saveScreenshot(ByteBuffer buf, int width, int height) {
        // Convert RGB bytes to ARGB ints with no transparency. Flip image vertically by reading the
        // rows of pixels in the byte buffer in reverse - (0,0) is at bottom left in OpenGL.

        int p = width * height * 4; // Points to first byte (red) in each row.
        int q;                  // Index into ByteBuffer
        int i = 0;                  // Index into target int[]
        int rowWidth = width * 4;        // Number of bytes in each row

        int pixels[] = new int[width * height];
        for (int row = 0; row < height; row++) {
            p -= rowWidth;
            q = p;
            for (int col = 0; col < width; col++) {
                int iB = buf.get(q++);
                int iG = buf.get(q++);
                int iR = buf.get(q++);
                q++; // skip A
                pixels[i++] = 0xFF000000
                        | ((iR & 0x000000FF) << 16)
                        | ((iG & 0x000000FF) << 8)
                        | (iB & 0x000000FF);
            }
        }
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);
        return bufferedImage;
    }

    private void saveAsFile1(BufferedImage bufferedImage, String filename) {
        Debug.debug(1, "Attempting to save screenshot to file = '" + filename + "'");
        try {
            File file = new File(filename);
            javax.imageio.ImageIO.write(bufferedImage, "PNG", file);
        } catch (Exception e) {
            Debug.debug(1, "Couldn't save screenshot, e=" + e);
        }
    }

    private void saveAsFile2(BufferedImage bufferedImage, String filename) {
        Debug.debug(1, "Attempting to save screenshot to file = '" + filename + "'");
        try {
            RenderedImage rendImage = bufferedImage;
            File file = new File(filename);
            ImageWriter writer = null;
            Iterator iter = ImageIO.getImageWritersByFormatName("jpg");
            if (iter.hasNext()) {
                writer = (ImageWriter) iter.next();
            }
            ImageOutputStream ios = ImageIO.createImageOutputStream(file);
            writer.setOutput(ios);
            writer.write(null, new IIOImage(rendImage, null, null), null);
            ios.flush();
            writer.dispose();
            ios.close();

        } catch (IOException e) {
            Debug.debug(1, "Couldn't save screenshot, e=" + e);
        }

    }
    private double times[] = new double[100];
    private int timeIndex = 0;

    // adapted from http://intranet.cs.man.ac.uk/software/OpenGL/frames.txt
    private void updateFrameRate(GL gl, long frameRateStartTimeMs, long frameRateEndTimeMs) {
        float r = 1;
        float g = 1;
        float b = 1;
        float x = 0.01f;
        float y = 0.01f;
        double elapsed = frameRateEndTimeMs - frameRateStartTimeMs;

        times[timeIndex++] = elapsed;
        if (timeIndex >= times.length) {
            timeIndex = 0;
        }
        double total = 0;
        for (int loopi = 0; loopi < times.length; loopi++) {
            total += times[loopi];
        }
        total = total / times.length;

        if (total <= 0) {
            total = 1;
        }
        String fpsLabelStr = "FPS = " + fmt.format((1.0 / (total / 1000)));

        boolean lightingOn = gl.glIsEnabled(GL.GL_LIGHTING);        /* lighting on? */
        if (lightingOn) {
            gl.glDisable(GL.GL_LIGHTING);
        }

        int matrixMode[] = new int[1];
        gl.glGetIntegerv(GL.GL_MATRIX_MODE, matrixMode, 0);  /* matrix mode? */

        GLU glu = new GLU();
        GLUT glut = new GLUT();

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(0.0, 1.0, 0.0, 1.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT);       /* save current colour */
        gl.glColor3f(r, g, b);
        gl.glRasterPos3f(x, y, 0.0f);

        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_10, fpsLabelStr);

        gl.glPopAttrib();
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(matrixMode[0]);
        if (lightingOn) {
            gl.glEnable(GL.GL_LIGHTING);
        }

    }

    public void reshape(GLAutoDrawable drawable,
            int xstart,
            int ystart,
            int width,
            int height) {
        GL gl = drawable.getGL();

        if (videoMode) {
            width = 720;
            height = 480;
        }
        height = (height == 0) ? 1 : height;
        this.windowHeight = height;
        this.windowWidth = width;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

        // NOTE: this defines what we see of the world
        GLU glu = new GLU();
        // @TODO: Hmmmmm should we calculate aspect ratio like this??  Or should it be set to some constant?
        glu.gluPerspective(FOVY, (double) width / (double) height, NEAR_CLIP_PLANE_DEPTH, FAR_CLIP_PLANE_DEPTH);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void displayChanged(GLAutoDrawable drawable,
            boolean modeChanged,
            boolean deviceChanged) {
    }

    public void addMessage(Message msg) {
        incomingMsgQ.add(msg);
    }
}
