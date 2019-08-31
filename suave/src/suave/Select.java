package suave;

import com.sun.opengl.util.BufferUtil;
import java.awt.Color;
import java.awt.event.*;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.*;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import java.util.concurrent.*;

// @TODO: Factor this into two classes - one class handles mouse
// input, passed it off to ... somewhere.  Other class handles actual
// intersection with the model/ the triangles.  Ok, maybe three
// classes?
public class Select extends Observable implements MouseListener, MouseMotionListener {

    private GLCamera camera;
    private BlockingQueue<MouseEvent> mouseQueue = new LinkedBlockingQueue<MouseEvent>();

    public Select(GLDisplay glDisplay, GLCamera camera, Observer observer) {
        glDisplay.addMouseListener(this);
        glDisplay.addMouseMotionListener(this);

        this.camera = camera;
        if (null != observer) {
            this.addObserver(observer);
        }
    }

    // NOTE: This method assumes that you've already set the OpenGL
    // matrices to the camera viewpoint.  The camera position is one
    // end of the line, the method returns the other end of the line,
    // where the line goes from the eye point through the click point
    // on the view port, out to the clip plane.
    //
    // @TODO: Convert this to just use matrix math rather than relying on jogl
    private float[] convertClickToLine(GL gl, GLCamera cam, int mouseX, int mouseY, double clipPlaneDepth) {

        double[] projectionMatrix = new double[16];
        double[] modelViewMatrix = new double[16];
        int[] viewport = new int[4];
        int viewportX = 0;
        int viewportY = 0;
        int viewportWidth = 0;
        int viewportHeight = 0;

        /* Get the current PROJECTION matrix from OpenGL */
        DoubleBuffer projectionMatrixBuf = BufferUtil.newDoubleBuffer(16);
        gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projectionMatrixBuf);
        projectionMatrixBuf.get(projectionMatrix);

        /* Get the current MODELVIEW matrix from OpenGL */
        DoubleBuffer modelViewMatrixBuf = BufferUtil.newDoubleBuffer(16);
        gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, modelViewMatrixBuf);
        modelViewMatrixBuf.get(modelViewMatrix);

        /* Get the current VIEWPORT from OpenGL */
        IntBuffer vpBuf = BufferUtil.newIntBuffer(4);
        gl.glGetIntegerv(GL.GL_VIEWPORT, vpBuf);
        vpBuf.get(viewport);

        viewportX = viewport[0];
        viewportY = viewport[1];
        viewportWidth = viewport[2];
        viewportHeight = viewport[3];

        if (mouseX < viewportX || mouseX > viewportWidth
                || mouseY < viewportY && mouseY > viewportHeight) {
            return null;
        }

        // Set the end point of ray in windows coordinates
        double mousePosX = mouseX;
        double mousePosY = viewportHeight - mouseY; // invert mouse Y coordinate

        double mousePosZ = clipPlaneDepth;

        double worldPos[] = new double[3];

        GLU glu = new GLU();

        // NOTE: I think the extra zero params next to the matrix
        // params are a standard JOGL hack, they are offsets INTO THE
        // ARRAY, i.e. this is for when you have a big array of points
        // you want to unproject.  Instead of just passing in an array
        // you pass in an array and an offset into the array.
        glu.gluUnProject(
                mousePosX,
                mousePosY,
                mousePosZ,
                modelViewMatrix, 0,
                projectionMatrix, 0,
                viewport, 0,
                worldPos, 0);

        float[] retval = new float[3];
        retval[0] = (float) worldPos[0];
        retval[1] = (float) worldPos[1];
        retval[2] = (float) worldPos[2];

        return retval;
    }

    public int selectTriangle(GL gl, MouseEvent e, int mouseX, int mouseY, ArrayList<Triangle> triangles) {

        // @TODO: from everything I can tell this _should_ be using
        // the FAR clip plane.  Not the near clip plane.  And yet with
        // NEAR it looks right, with FAR it looks wrong.
        float[] world = convertClickToLine(gl, camera, mouseX, mouseY, Renderer.NEAR_CLIP_PLANE_DEPTH);
        if (null == world) {
            return 0;
        }

        // ok, so now we have at least two points that define the
        // ray/line segment, the camera position and the unprojected
        // point on the far clip plane.
        //
        // @TODO: maybe should maybe factor out the actual intersection code.

        float dir[] = {0.0f, 0.0f, 0.0f};
        float orig[] = {camera.xPos, camera.yPos, camera.zPos};
        Vec3f.sub(dir, world, orig);
        float hitLocation[] = new float[3];

        Triangle closest = null;
        float closestTfactor = Float.MAX_VALUE;
        float[] closestHitPos = null;

        // So, for each triangle in our array
        for (int loopi = 0; loopi < triangles.size(); loopi++) {
            Triangle t = triangles.get(loopi);

            if (MollerTrumbore.intersectTriangle(orig, dir, t, hitLocation) > 0) {
                if (hitLocation[0] < closestTfactor) {
                    float u = hitLocation[1];
                    float v = hitLocation[2];
                    float v1Factor = (1 - u - v);
                    float[] hitPos = new float[3];

                    hitPos[0] = v1Factor * t.v1.x + u * t.v2.x + v * t.v3.x;
                    hitPos[1] = v1Factor * t.v1.y + u * t.v2.y + v * t.v3.y;
                    hitPos[2] = v1Factor * t.v1.z + u * t.v2.z + v * t.v3.z;

                    closestTfactor = hitLocation[0];
                    closest = t;
                    closestHitPos = hitPos;
                }
            }
        }

        // did we hit anything?
        if (null != closest) {
            SelectEvent se = new SelectEvent(gl, e, closest, orig, world, closestHitPos);
        setChanged();
            notifyObservers(se);
        }

        return -1;
    }

    public void update(GL gl, ArrayList<Triangle> triangles) {
        while (true) {
            MouseEvent e = mouseQueue.poll();
            if (null == e) {
                return;
            }
            Debug.debug(1, "Select.update: processing mouse event " + e);

            // On kappa (dell laptop) 100 iterations using ALL (5779)
            // triangles takes 204ms to 219ms - so about 2 ms per
            // iteration.
            //
            // with frustum culling, using 2175 triangles, 100
            // iterations takes 62 to 79 ms, so about .8 ms per
            // iteration.  But then frustum culling ends up costing us
            // back that time we saved.
            selectTriangle(gl, e, e.getX(), e.getY(), triangles);
        }
    }

    public void mousePressed(MouseEvent e) {
        //	Debug.debug(1,"mousePressed: Queueing mouse event "+e);
        mouseQueue.add(e);
    }

    public void mouseDragged(MouseEvent e) {
        //	Debug.debug(1,"mouseDragged: Queueing mouse event "+e);
        mouseQueue.add(e);
    }

    public void mouseReleased(MouseEvent e) {
        //	Debug.debug(1,"mouseReleased: Queueing mouse event "+e);
        mouseQueue.add(e);
    }

    public void mouseMoved(MouseEvent e) {
        //	Debug.debug(1,"mouseMoved: Queueing mouse event "+e);
        mouseQueue.add(e);
    }

    public void mouseExited(MouseEvent e) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseEntered(MouseEvent e) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void mouseClicked(MouseEvent e) {
//        throw new UnsupportedOperationException("Not supported yet.");
    }
}
