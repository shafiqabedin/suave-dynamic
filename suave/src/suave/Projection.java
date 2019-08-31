package suave;

import java.util.*;



import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import javax.media.opengl.GL;
import com.sun.opengl.util.BufferUtil;
import javax.media.opengl.glu.GLU;
import org.apache.commons.math.geometry.Vector3D;

public class Projection {
    // While developing the new camera calibrated projection, let's make sure we're not accidentally
    // ending up in the old projection code.

    private final static boolean EXCEPTION_FOR_OLD_PROJECTION = true;
    private final static boolean USE_CAMERA_CALIBRATION_IF_POSSIBLE = true;

    int viewportX = 0;
    int viewportY = 0;
    int viewportWidth = 0;
    int viewportHeight = 0;
    public ArrayList<Triangle> triangles;
    public ArrayList<Triangle> trianglesAfterCulling;
    private GLU glu = new GLU();
    private CameraCalibration cameraCalibration;
    private boolean useCalibratedCamera = false;

    public Projection(ArrayList<Triangle> triangles, CameraCalibration cameraCalibration) {
        this.triangles = triangles;
        this.cameraCalibration = cameraCalibration;
        trianglesAfterCulling = new ArrayList<Triangle>();
    }
    public int fbTriangleNonVisibleCount = 0;
    public int fbTriangleVisibleCount = 0;
    public int fbTriangleSplitCount = 0;

    public void init(GL gl) {
    }

    private void mult(float[] m1, float[] m2, float[] result) {
        result[ 0] = m1[ 0] * m2[ 0] + m1[ 1] * m2[ 4] + m1[ 2] * m2[ 8] + m1[ 3] * m2[12];
        result[ 1] = m1[ 0] * m2[ 1] + m1[ 1] * m2[ 5] + m1[ 2] * m2[ 9] + m1[ 3] * m2[13];
        result[ 2] = m1[ 0] * m2[ 2] + m1[ 1] * m2[ 6] + m1[ 2] * m2[10] + m1[ 3] * m2[14];
        result[ 3] = m1[ 0] * m2[ 3] + m1[ 1] * m2[ 7] + m1[ 2] * m2[11] + m1[ 3] * m2[15];

        result[ 4] = m1[ 4] * m2[ 0] + m1[ 5] * m2[ 4] + m1[ 6] * m2[ 8] + m1[ 7] * m2[12];
        result[ 5] = m1[ 4] * m2[ 1] + m1[ 5] * m2[ 5] + m1[ 6] * m2[ 9] + m1[ 7] * m2[13];
        result[ 6] = m1[ 4] * m2[ 2] + m1[ 5] * m2[ 6] + m1[ 6] * m2[10] + m1[ 7] * m2[14];
        result[ 7] = m1[ 4] * m2[ 3] + m1[ 5] * m2[ 7] + m1[ 6] * m2[11] + m1[ 7] * m2[15];

        result[ 8] = m1[ 8] * m2[ 0] + m1[ 9] * m2[ 4] + m1[10] * m2[ 8] + m1[11] * m2[12];
        result[ 9] = m1[ 8] * m2[ 1] + m1[ 9] * m2[ 5] + m1[10] * m2[ 9] + m1[11] * m2[13];
        result[10] = m1[ 8] * m2[ 2] + m1[ 9] * m2[ 6] + m1[10] * m2[10] + m1[11] * m2[14];
        result[11] = m1[ 8] * m2[ 3] + m1[ 9] * m2[ 7] + m1[10] * m2[11] + m1[11] * m2[15];

        result[12] = m1[12] * m2[ 0] + m1[13] * m2[ 4] + m1[14] * m2[ 8] + m1[15] * m2[12];
        result[13] = m1[12] * m2[ 1] + m1[13] * m2[ 5] + m1[14] * m2[ 9] + m1[15] * m2[13];
        result[14] = m1[12] * m2[ 2] + m1[13] * m2[ 6] + m1[14] * m2[10] + m1[15] * m2[14];
        result[15] = m1[12] * m2[ 3] + m1[13] * m2[ 7] + m1[14] * m2[11] + m1[15] * m2[15];

    }
    private float[][] frustum = new float[6][4];
    private float[] projTimesModl = new float[16];

    private void extractFrustum(GL gl) {
        float[] proj = new float[16];
        float[] modl = new float[16];
        float[] clip = new float[16];
        float t;

        /* Get the current PROJECTION matrix from OpenGL */
        FloatBuffer projBuf = BufferUtil.newFloatBuffer(16);
        gl.glGetFloatv(GL.GL_PROJECTION_MATRIX, projBuf);
        projBuf.get(proj);

        /* Get the current MODELVIEW matrix from OpenGL */
        FloatBuffer modlBuf = BufferUtil.newFloatBuffer(16);
        gl.glGetFloatv(GL.GL_MODELVIEW_MATRIX, modlBuf);
        modlBuf.get(modl);

        // @TODO: Can I simply replace all this crap below with this;
        // 	mult(modl,proj,clip);
        // Probably. Try it later in isolation to avoid introducing wierd bugs now.

        /* Combine the two matrices (multiply projection by modelview)    */
        clip[ 0] = modl[ 0] * proj[ 0] + modl[ 1] * proj[ 4] + modl[ 2] * proj[ 8] + modl[ 3] * proj[12];
        clip[ 1] = modl[ 0] * proj[ 1] + modl[ 1] * proj[ 5] + modl[ 2] * proj[ 9] + modl[ 3] * proj[13];
        clip[ 2] = modl[ 0] * proj[ 2] + modl[ 1] * proj[ 6] + modl[ 2] * proj[10] + modl[ 3] * proj[14];
        clip[ 3] = modl[ 0] * proj[ 3] + modl[ 1] * proj[ 7] + modl[ 2] * proj[11] + modl[ 3] * proj[15];

        clip[ 4] = modl[ 4] * proj[ 0] + modl[ 5] * proj[ 4] + modl[ 6] * proj[ 8] + modl[ 7] * proj[12];
        clip[ 5] = modl[ 4] * proj[ 1] + modl[ 5] * proj[ 5] + modl[ 6] * proj[ 9] + modl[ 7] * proj[13];
        clip[ 6] = modl[ 4] * proj[ 2] + modl[ 5] * proj[ 6] + modl[ 6] * proj[10] + modl[ 7] * proj[14];
        clip[ 7] = modl[ 4] * proj[ 3] + modl[ 5] * proj[ 7] + modl[ 6] * proj[11] + modl[ 7] * proj[15];

        clip[ 8] = modl[ 8] * proj[ 0] + modl[ 9] * proj[ 4] + modl[10] * proj[ 8] + modl[11] * proj[12];
        clip[ 9] = modl[ 8] * proj[ 1] + modl[ 9] * proj[ 5] + modl[10] * proj[ 9] + modl[11] * proj[13];
        clip[10] = modl[ 8] * proj[ 2] + modl[ 9] * proj[ 6] + modl[10] * proj[10] + modl[11] * proj[14];
        clip[11] = modl[ 8] * proj[ 3] + modl[ 9] * proj[ 7] + modl[10] * proj[11] + modl[11] * proj[15];

        clip[12] = modl[12] * proj[ 0] + modl[13] * proj[ 4] + modl[14] * proj[ 8] + modl[15] * proj[12];
        clip[13] = modl[12] * proj[ 1] + modl[13] * proj[ 5] + modl[14] * proj[ 9] + modl[15] * proj[13];
        clip[14] = modl[12] * proj[ 2] + modl[13] * proj[ 6] + modl[14] * proj[10] + modl[15] * proj[14];
        clip[15] = modl[12] * proj[ 3] + modl[13] * proj[ 7] + modl[14] * proj[11] + modl[15] * proj[15];

        // @TODO: I have no idea what this comment means.
        //
        // Note clip is M * P - mult P * M for projecting later.
        // Ok, that didn't work so well - let's try M * P for projection.
        mult(modl, proj, projTimesModl);

        /* Extract the numbers for the RIGHT plane */
        frustum[0][0] = clip[ 3] - clip[ 0];
        frustum[0][1] = clip[ 7] - clip[ 4];
        frustum[0][2] = clip[11] - clip[ 8];
        frustum[0][3] = clip[15] - clip[12];

        /* Normalize the result */
        t = (float) Math.sqrt(frustum[0][0] * frustum[0][0] + frustum[0][1] * frustum[0][1] + frustum[0][2] * frustum[0][2]);
        frustum[0][0] /= t;
        frustum[0][1] /= t;
        frustum[0][2] /= t;
        frustum[0][3] /= t;

        /* Extract the numbers for the LEFT plane */
        frustum[1][0] = clip[ 3] + clip[ 0];
        frustum[1][1] = clip[ 7] + clip[ 4];
        frustum[1][2] = clip[11] + clip[ 8];
        frustum[1][3] = clip[15] + clip[12];

        /* Normalize the result */
        t = (float) Math.sqrt(frustum[1][0] * frustum[1][0] + frustum[1][1] * frustum[1][1] + frustum[1][2] * frustum[1][2]);
        frustum[1][0] /= t;
        frustum[1][1] /= t;
        frustum[1][2] /= t;
        frustum[1][3] /= t;

        /* Extract the BOTTOM plane */
        frustum[2][0] = clip[ 3] + clip[ 1];
        frustum[2][1] = clip[ 7] + clip[ 5];
        frustum[2][2] = clip[11] + clip[ 9];
        frustum[2][3] = clip[15] + clip[13];

        /* Normalize the result */
        t = (float) Math.sqrt(frustum[2][0] * frustum[2][0] + frustum[2][1] * frustum[2][1] + frustum[2][2] * frustum[2][2]);
        frustum[2][0] /= t;
        frustum[2][1] /= t;
        frustum[2][2] /= t;
        frustum[2][3] /= t;

        /* Extract the TOP plane */
        frustum[3][0] = clip[ 3] - clip[ 1];
        frustum[3][1] = clip[ 7] - clip[ 5];
        frustum[3][2] = clip[11] - clip[ 9];
        frustum[3][3] = clip[15] - clip[13];

        /* Normalize the result */
        t = (float) Math.sqrt(frustum[3][0] * frustum[3][0] + frustum[3][1] * frustum[3][1] + frustum[3][2] * frustum[3][2]);
        frustum[3][0] /= t;
        frustum[3][1] /= t;
        frustum[3][2] /= t;
        frustum[3][3] /= t;

        /* Extract the FAR plane */
        frustum[4][0] = clip[ 3] - clip[ 2];
        frustum[4][1] = clip[ 7] - clip[ 6];
        frustum[4][2] = clip[11] - clip[10];
        frustum[4][3] = clip[15] - clip[14];

        /* Normalize the result */
        t = (float) Math.sqrt(frustum[4][0] * frustum[4][0] + frustum[4][1] * frustum[4][1] + frustum[4][2] * frustum[4][2]);
        frustum[4][0] /= t;
        frustum[4][1] /= t;
        frustum[4][2] /= t;
        frustum[4][3] /= t;

        /* Extract the NEAR plane */
        frustum[5][0] = clip[ 3] + clip[ 2];
        frustum[5][1] = clip[ 7] + clip[ 6];
        frustum[5][2] = clip[11] + clip[10];
        frustum[5][3] = clip[15] + clip[14];

        /* Normalize the result */
        t = (float) Math.sqrt(frustum[5][0] * frustum[5][0] + frustum[5][1] * frustum[5][1] + frustum[5][2] * frustum[5][2]);
        frustum[5][0] /= t;
        frustum[5][1] /= t;
        frustum[5][2] /= t;
        frustum[5][3] /= t;
    }

    private boolean isPointInFrustum(float[] point) {
        // For each side of the Frustum
        for (int f = 0; f < 6; f++) {
            if (!(frustum[f][0] * point[0] + frustum[f][1] * point[1] + frustum[f][2] * point[2] + frustum[f][3] > 0)) {
                return false;
            }
        }
        return true;
    }

    private boolean triangleInFrustum(Triangle t) {
        // For each side of the Frustum
        for (int f = 0; f < 6; f++) {
            if (!(frustum[f][0] * t.v1.x + frustum[f][1] * t.v1.y + frustum[f][2] * t.v1.z + frustum[f][3] > 0)
                    && !(frustum[f][0] * t.v2.x + frustum[f][1] * t.v2.y + frustum[f][2] * t.v2.z + frustum[f][3] > 0)
                    && !(frustum[f][0] * t.v3.x + frustum[f][1] * t.v3.y + frustum[f][2] * t.v3.z + frustum[f][3] > 0)) {
                return false;
            }
        }
        return true;
    }

    private void getViewport(GL gl) {
        int[] viewport = new int[4];
        IntBuffer vpBuf = BufferUtil.newIntBuffer(4);
        gl.glGetIntegerv(GL.GL_VIEWPORT, vpBuf);
        vpBuf.get(viewport);
        viewportX = viewport[0];
        viewportY = viewport[1];
        viewportWidth = viewport[2];
        viewportHeight = viewport[3];
    }

    private float[] multMatrix4x4timesVec4(float[] m, float[] v, float[] results) {
        results[0] = m[0 + 0 * 4] * v[0] + m[0 + 1 * 4] * v[1] + m[0 + 2 * 4] * v[2] + m[0 + 3 * 4] * v[3];
        results[1] = m[1 + 0 * 4] * v[0] + m[1 + 1 * 4] * v[1] + m[1 + 2 * 4] * v[2] + m[1 + 3 * 4] * v[3];
        results[2] = m[2 + 0 * 4] * v[0] + m[2 + 1 * 4] * v[1] + m[2 + 2 * 4] * v[2] + m[2 + 3 * 4] * v[3];
        results[3] = m[3 + 0 * 4] * v[0] + m[3 + 1 * 4] * v[1] + m[3 + 2 * 4] * v[2] + m[3 + 3 * 4] * v[3];

        return results;
    }
    float projtemp[] = new float[4];
    float[] projtemp2 = new float[4];

    // old projection before camera calibration
    public void projectOld(float x, float y, float z, float[] results) {
        if (EXCEPTION_FOR_OLD_PROJECTION) {
            throw new RuntimeException("How did we end up calling project(float,float,float,float[]) in projection?");
        }
        projtemp[0] = x;
        projtemp[1] = y;
        projtemp[2] = z;
        projtemp[3] = 1;
        multMatrix4x4timesVec4(projTimesModl, projtemp, projtemp2);
        if (projtemp2[3] != 1.0) {
            results[0] = projtemp2[0] / projtemp2[3];
            results[1] = projtemp2[1] / projtemp2[3];
            results[2] = projtemp2[2] / projtemp2[3];
        } else {
            results[0] = projtemp2[0];
            results[1] = projtemp2[1];
            results[2] = projtemp2[2];
        }
        results[0] = (float) (.5 * (results[0] + 1) * viewportWidth + viewportX);
        results[1] = (float) (.5 * (results[1] + 1) * viewportHeight + viewportY);
        results[2] = (float) (.5 * (results[2] + 1));
        results[1] = viewportHeight - results[1];
    }

    // new projection using calibration
    public void project(float x, float y, float z, float[] results) {
        if (USE_CAMERA_CALIBRATION_IF_POSSIBLE && useCalibratedCamera) {
            double[] worldPoint = new double[3];
            double[] pixelPoint = new double[3];
            worldPoint[0] = x;
            worldPoint[1] = y;
            worldPoint[2] = z;
            cameraCalibration.transform(worldPoint, pixelPoint);
            pixelPoint[0] = 640.0 - pixelPoint[0];
            pixelPoint[1] = 480 - pixelPoint[1];
            results[0] = (float) pixelPoint[0];
            results[1] = (float) pixelPoint[1];
            results[2] = (float) pixelPoint[2];
        } else {

            projectOld(x, y, z, results);
        }
    }

    public void project(Vertex v) {
        float[] results = new float[3];
        project(v.x, v.y, v.z, results);
        v.screenx = results[0];
        v.screeny = results[1];
        v.screenz = results[2];
    }

    private void project(Triangle t) {
        project(t.v1);
        project(t.v2);
        project(t.v3);
        t.visibleCount = 1;
        //	System.err.println("Projection.project: Triangle = "+t);
    }
//    float[] results = new float[3];

    private void project2(Triangle t) {
        float[] results = new float[3];
        project(t.v1.x, t.v1.y, t.v1.z, results);
        t.v1.screenx = results[0];
        t.v1.screeny = results[1];
        t.v1.screenz = results[2];
        project(t.v2.x, t.v2.y, t.v2.z, results);
        t.v2.screenx = results[0];
        t.v2.screeny = results[1];
        t.v2.screenz = results[2];
        project(t.v3.x, t.v3.y, t.v3.z, results);
        t.v3.screenx = results[0];
        t.v3.screeny = results[1];
        t.v3.screenz = results[2];
        t.visibleCount = 1;
        trianglesAfterCulling.add(t);
        //	System.err.println("Projection.project: Triangle = "+t);
    }

//    private void projectWithCalibration(Triangle t) {
//        projectOld(t.v1.x, t.v1.y, t.v1.z, results);
//        t.v1.screenx = results[0];
//        t.v1.screeny = results[1];
//        t.v1.screenz = results[2];
//        project(t.v2.x, t.v2.y, t.v2.z, results);
//        t.v2.screenx = results[0];
//        t.v2.screeny = results[1];
//        t.v2.screenz = results[2];
//        project(t.v3.x, t.v3.y, t.v3.z, results);
//        t.v3.screenx = results[0];
//        t.v3.screeny = results[1];
//        t.v3.screenz = results[2];
//
////        double[] worldPoint = new double[3];
////        double[] pixelPoint = new double[3];
////
////        worldPoint[0] = t.v1.x;
////        worldPoint[1] = t.v1.y;
////        worldPoint[2] = t.v1.z;
////        cameraCalibration.transform(worldPoint, pixelPoint);
////        pixelPoint[0] = 640.0 - pixelPoint[0];
//////        pixelPoint[0] = 640.0 - pixelPoint[0];
//////        Debug.debug(1, "Projection.projectWithCalibration: old style=" + t.v1.screenx + ", " + t.v1.screeny + ", " + t.v1.screenz
//////                + " new style=" + pixelPoint[0] + ", " + pixelPoint[1] + ", " + pixelPoint[2]
//////                + " diff=" + (t.v1.screenx - pixelPoint[0]) + ", " + (t.v1.screeny - pixelPoint[1]) + ", " + (t.v1.screenz - pixelPoint[2]));
////        t.v1.screenx = (float) pixelPoint[0];
////        t.v1.screeny = (float) pixelPoint[1];
////        t.v1.screenz = (float) pixelPoint[2];
////        worldPoint[0] = t.v2.x;
////        worldPoint[1] = t.v2.y;
////        worldPoint[2] = t.v2.z;
////        cameraCalibration.transform(worldPoint, pixelPoint);
////        pixelPoint[0] = 640.0 - pixelPoint[0];
////        t.v2.screenx = (float) pixelPoint[0];
////        t.v2.screeny = (float) pixelPoint[1];
////        t.v2.screenz = (float) pixelPoint[2];
////        worldPoint[0] = t.v3.x;
////        worldPoint[1] = t.v3.y;
////        worldPoint[2] = t.v3.z;
////        cameraCalibration.transform(worldPoint, pixelPoint);
////        pixelPoint[0] = 640.0 - pixelPoint[0];
////        t.v3.screenx = (float) pixelPoint[0];
////        t.v3.screeny = (float) pixelPoint[1];
////        t.v3.screenz = (float) pixelPoint[2];
//
//
//        t.visibleCount = 1;
//        trianglesAfterCulling.add(t);
//        //	System.err.println("Projection.project: Triangle = "+t);
//    }
    private int projectedCount = 0;

    private void cullAndProject(GL gl) {
        projectedCount = 0;
        for (int loopi = 0; loopi < triangles.size(); loopi++) {
            Triangle t = triangles.get(loopi);
            if (triangleInFrustum(t)) {
                // @TODO: Note even when we use cameraCalibration object, we're still culling using the
                // frustum - currently we're getting the frustum from OpenGL but we really should calculate it
                // ourselves.
                project2(t);
                projectedCount++;
            }
        }
    }
    private int notCulled = 0;

    private void justCull(GL gl) {
        notCulled = 0;
        for (int loopi = 0; loopi < triangles.size(); loopi++) {
            Triangle t = triangles.get(loopi);
            if (triangleInFrustum(t)) {
                notCulled++;
                t.visibleCount = 1;
                trianglesAfterCulling.add(t);
            }
        }
    }

    public void render(GL gl, GLCamera camera) {
        render(gl, camera, null, null);
    }

    public void render(GL gl, GLCamera camera, float[][] pointsInFrustum, boolean[] arePointsInFrustum) {
        //	System.err.println("Projection.render: STARTING PROJECTING");

        trianglesAfterCulling.clear();

        float[] proj = new float[16];

        gl.glMatrixMode(GL.GL_MODELVIEW);
        // save current matrix
        gl.glPushMatrix();
        //  we set up the 'camera', i.e. the UAV viewpoint
        gl.glLoadIdentity();      // Reset The Modelview Matrix

//        // @TODO: Should this bit here be using rotation matrices
//        // instead?  Might this be causing problems?
//        gl.glRotatef(camera.xRot, 1.0f, 0.0f, 0.0f);
//        gl.glRotatef(camera.yRot, 0.0f, 1.0f, 0.0f);
//        gl.glRotatef(camera.zRot, 0.0f, 0.0f, 1.0f);
//        gl.glTranslatef(-camera.xPos, -camera.yPos, -camera.zPos);

        if (!camera.useDirectionVector) {
            // @TODO: Replace this with rotation matrix?
            gl.glRotatef(camera.xRot, 1.0f, 0.0f, 0.0f);
            gl.glRotatef(camera.yRot, 0.0f, 1.0f, 0.0f);
            gl.glRotatef(camera.zRot, 0.0f, 0.0f, 1.0f);
            gl.glTranslatef(-camera.xPos, -camera.yPos, -camera.zPos);
            useCalibratedCamera = false;
        } else {
            double tx = camera.xPos + 100 * camera.dirx;
            double ty = camera.yPos + 100 * camera.diry;
            double tz = camera.zPos + 100 * camera.dirz;
            // @TODO: Note, even if we're using the cameraCalibration object we still need to use gluLookAt because we're 
            // still using the OpenGL matrix for the frustum cull - we need to fix this later.
            glu.gluLookAt(camera.xPos, camera.yPos, camera.zPos, tx, ty, tz, camera.upx, camera.upy, camera.upz);
            Debug.debug(1, "Projection.render: Center = " + tx + ", " + ty + ", " + tz + " dir = " + camera.dirx + ", " + camera.diry + ", " + camera.dirz + " up = " + camera.upx + ", " + camera.upy + ", " + camera.upz);
            if (null != cameraCalibration) {
                Vector3D eye = new Vector3D(camera.xPos, camera.yPos, camera.zPos);
                Vector3D up = new Vector3D(camera.upx, camera.upy, camera.upz);
                Vector3D dir = new Vector3D(camera.dirx, camera.diry, camera.dirz);
                cameraCalibration.setCamera(eye, dir, up);
                useCalibratedCamera = true;
            }
        }
//        Debug.debug(1, "Projection.render: useCalibratedCamera=" + useCalibratedCamera);
//        Exception e = new Exception("Where is Projection.render() being called from?");
//        e.printStackTrace();
        getViewport(gl);

        // Note, extractFrustum grabs PROJECTION_MATRIX P and
        // MODELVIEW_MATRIX M and multiplies clip = M * P; One guy on
        // #opengl seemed to think that to project the vertices onto
        // the view plane, I should do;
        //
        // v' = P * M * v
        //
        // Which is the opposite of clip - but he wasn't sure.  I'm
        // gonna try using clip and see if that looks right and if not
        // try the other way.
        extractFrustum(gl);

// 	// Now retrieve the projection matrix
// 	gl.glGetFloatv( GL.GL_PROJECTION_MATRIX, proj );	

        if (null != pointsInFrustum) {
            for (int loopi = 0; loopi < pointsInFrustum.length; loopi++) {
                arePointsInFrustum[loopi] = isPointInFrustum(pointsInFrustum[loopi]);
            }
        }

        for (int loopi = 0; loopi < triangles.size(); loopi++) {
            triangles.get(loopi).reset();
        }

        cullAndProject(gl);

        // get the original matrix back
        gl.glPopMatrix();

        //	System.err.println("Projection.render: DONE PROJECTING, triangles in Frustum and projected = "+ projectedCount);
    }

    public void frustumCull(GL gl, GLCamera camera) {
        trianglesAfterCulling.clear();

        float[] proj = new float[16];

        gl.glMatrixMode(GL.GL_MODELVIEW);
        // save current matrix
        gl.glPushMatrix();
        //  we set up the 'camera', i.e. the UAV viewpoint
        gl.glLoadIdentity();      // Reset The Modelview Matrix

        // @TODO: Should this bit here be using rotation matrices
        // instead?  Might this be causing problems?
        gl.glRotatef(camera.xRot, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(camera.yRot, 0.0f, 1.0f, 0.0f);
        gl.glRotatef(camera.zRot, 0.0f, 0.0f, 1.0f);
        gl.glTranslatef(-camera.xPos, -camera.yPos, -camera.zPos);

        getViewport(gl);

        // Note, extractFrustum grabs PROJECTION_MATRIX P and
        // MODELVIEW_MATRIX M and multiplies clip = M * P; One guy on
        // #opengl seemed to think that to project the vertices onto
        // the view plane, I should do;
        //
        // v' = P * M * v
        //
        // Which is the opposite of clip - but he wasn't sure.  I'm
        // gonna try using clip and see if that looks right and if not
        // try the other way.
        extractFrustum(gl);

// 	// Now retrieve the projection matrix
// 	gl.glGetFloatv( GL.GL_PROJECTION_MATRIX, proj );	

        for (int loopi = 0; loopi < triangles.size(); loopi++) {
            triangles.get(loopi).reset();
        }

        justCull(gl);

        // get the original matrix back
        gl.glPopMatrix();
    }
}
