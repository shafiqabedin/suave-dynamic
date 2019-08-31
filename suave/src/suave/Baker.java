package suave;

import com.perc.utils.collection.ArrayUtils;
import java.io.IOException;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import java.nio.IntBuffer;
import com.sun.opengl.util.BufferUtil;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;

public class Baker {
    // ONLY ONE OF THESE SHOULD BE SET TO TRUE:
    // get all RGB values from video image at once, ahead of time, via getData().getPixels(),

    private final static boolean RGB_GET_SAMPLES_ALL_AT_ONCE = false;
    // get all the RGB values at once using getDataElements()?
    private final static boolean RGB_GET_DATA_ELEMENTS_ALL_AT_ONCE = false;
    // Get all RGB values from video image at once, ahead of time, via  getRGB() that returns an array
    private final static boolean RGB_GETRGB_ALL_AT_ONCE = true;
    // Get RGBs one at a time from video image, as we need them, using .getRGB(x,y) but cache them
    private final static boolean RGB_GET_ONE_AT_A_TIME_AND_CACHE = false;
    // When we use simulated imagery and telemetry from VBS, the telemetry is encoded
    // into the image as colored blocks in the top 21 lines of pixels, so we only really
    // get 480-21 lines of proper imagery.  So we ignore anything that lands in the top
    // 21 lines.  @TODO: It would be BETTER to get 480+21 lines of imagery and then just slice
    // off the top 21 lines, but I'm worried how that will affect the camera calibration, so for
    // now...
    private final static boolean SKIP_TOP_VBS_TELEMETRY_PIXELS = false;
    private final static int VBS_TELEMETRY_PIXELS_LINE_COUNT = 21;
    // in general it seems we get the most distortion at the edges so we ignore anything close to
    // the edge.  @TODO: This is kind of a hack that's been in here a good long while and we should
    // take another look and see if it's really getting us anything.
    private final static int PIXELS_TO_IGNORE_AT_EDGE = 10;
    private final static int PIXELS_TO_IGNORE_AT_TOP_AND_BOTTOM = 150;
    // It seems like when the FOV is such that the top of the imagery is much farther away than
    // the bottom, we get a lot of distortion.  This is an attempt to leave the top out unless
    // the FOV is such that everything is close by.
    public final static float MAX_BAKE_RANGE = 300;
    public final static float MAX_BAKE_RANGE_SQD = MAX_BAKE_RANGE * MAX_BAKE_RANGE;
    private final static boolean SKIP_TRIANGLES_BEYOND_MAX_BAKE_RANGE = true;
    // for debugging
    private final static boolean DEBUG_CHECK_BOUNDS_OVERLAP = true;
    private final static boolean DEBUG_USE_TEXTURE_BYTEBUFFER = true;
    public final static DecimalFormat fmt = new DecimalFormat(".00000000000000000000");
    // These are somewhat guesses at the proper setting of the
    // projection to match the projection of the physical video
    // camera/lens.
    public final static float VIDEO_NEAR_CLIP_PLANE = 1.0f;
    public final static float VIDEO_FAR_CLIP_PLANE = 1000.0f;
//    // Procerus UAV
//    public final static float VIDEO_FRAME_WIDTH = 720.0f;
//    public final static float VIDEO_FRAME_HEIGHT = 480.0f;
//    public final static float VIDEO_FOV_Y = 46.0f;
//    public final static float VIDEO_ASPECT_RATIO = 1.5f;
    // VBS2
    public final static float VIDEO_FRAME_WIDTH = 640.0f;
    public final static float VIDEO_FRAME_HEIGHT = 459.0f;
    public final static float VIDEO_FOV_Y = 69.9840404f; // VBS2
    public final static float VIDEO_ASPECT_RATIO = 1.0f;
    private BlockingQueue<BakerCommand> commandQueue = new LinkedBlockingQueue<BakerCommand>();
    private boolean lastCommandWasReset = true;
    private StateDB stateDB;
    public static int imageIdx = 0;
    public static int[] xcoordinates = {0, 400, 800, 1200, 1600, 2000, 2400, 2800, 3200, 3600, 4000};
    public static int[] zcoordinates = {0, 400, 800, 1200, 1600, 2000, 2400, 2800, 3200, 3600, 4000};
    public static ArrayList<String> fileIndex = new ArrayList();

    public void queueResetTexture() {
        BakerCommand com2 = new BakerCommand();
        com2.type = BakerCommand.Type.RESET_TEXTURE;
        queueCommand(com2);
    }

    public void queueCommand(BakerCommand bc) {
        Debug.debug(1, "Baker.queueCommand:  commandQueue size = " + commandQueue.size());
        commandQueue.add(bc);
    }
    private GLCamera userViewCamera = null;
    private Model model;
    // @TODO: Should factor out x/y/z/xrot/yrot/zrot as a separate
    // class, use that instead.
    private GLCamera lastFrameViewpoint = new GLCamera();
    private Triangle[] triangleAry = null;
    private Spatial spatial;

    public Baker(GLCamera userViewCamera, Model model, StateDB stateDB) {
        this.userViewCamera = userViewCamera;
        this.model = model;
        this.stateDB = stateDB;
        spatial = new Spatial(10, VIDEO_FRAME_WIDTH, VIDEO_FRAME_HEIGHT);
    }

    // @TODO: remove these? They used to actually do some math but not anymore
    public float fscreenx(double x) {
        return (float) (x);
    }

    public float fscreeny(double y) {
        return (float) (y);
    }

    private void calcDistSqd(Vertex v, GLCamera camera) {
        double xdiff = camera.xPos - v.x;
        double ydiff = camera.yPos - v.y;
        double zdiff = camera.zPos - v.z;
        v.distSqdFromViewpoint = (float) ((xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff));
    }

    private void calcDistanceToViewpointAllTriangles(GLCamera camera) {
        if (null == triangleAry) {
            Debug.debug(1, "Baker.calcDistanceToViewpointAllTriangles: WARNING: triangleAry is null, cannot calc distance to viewpoint for all triangles, returning.");
            return;
        }

        for (int loopi = 0; loopi < triangleAry.length; loopi++) {
            Triangle t = triangleAry[loopi];
            t.v1.distSqdFromViewpoint = Float.MAX_VALUE;
            t.v2.distSqdFromViewpoint = Float.MAX_VALUE;
            t.v3.distSqdFromViewpoint = Float.MAX_VALUE;
        }
        for (int loopi = 0; loopi < triangleAry.length; loopi++) {
            Triangle t = triangleAry[loopi];
            if (t.v1.distSqdFromViewpoint == Float.MAX_VALUE) {
                calcDistSqd(t.v1, camera);
            }
            if (t.v2.distSqdFromViewpoint == Float.MAX_VALUE) {
                calcDistSqd(t.v2, camera);
            }
            if (t.v3.distSqdFromViewpoint == Float.MAX_VALUE) {
                calcDistSqd(t.v3, camera);
            }
            t.closestDistSqdToViewpoint = t.v1.distSqdFromViewpoint;
            if (t.v2.distSqdFromViewpoint < t.closestDistSqdToViewpoint) {
                t.closestDistSqdToViewpoint = t.v2.distSqdFromViewpoint;
            }
            if (t.v3.distSqdFromViewpoint < t.closestDistSqdToViewpoint) {
                t.closestDistSqdToViewpoint = t.v3.distSqdFromViewpoint;
            }
        }
    }

    // @TODO: It may be faster just to maintain a 'foreground' Area object, i.e. for
    // each triangle front to back, subtract foreground from triangle, store triangle
    // if non empty, then merge original triangle into foreground area, repeat.
    //
    // Note, removeOcclusions requires/relies upon the triangle list
    // being sorted before hand, by distance from the viewpoint,
    // closest to furthest.
    private void removeOcclusions() {
        ArrayList<Triangle> tList = new ArrayList<Triangle>();

        float[] x = new float[3];
        float[] y = new float[3];

        // Note, at the moment the triangleAry is sorted from 'closest
        // to viewpoint' to 'farthest from viewpoint' - we want to
        // start closest and go to farthest.

        spatial.clear();
        ArrayList<Triangle> occlusionCandidates = new ArrayList<Triangle>();

        double numCandidateListSearches = 0;
        double numCandidatesFound = 0;

        Debug.debug(1, "Baker.removeOcclusions: About to process " + triangleAry.length + " triangles.");
        int drawCount = 0;
        // for each triangle
        for (int loopi = 0; loopi < triangleAry.length; loopi++) {
            Triangle t1 = triangleAry[loopi];

            // that is at least partial visible from the current
            // viewpoint;
            if (t1.visibleCount < 1) {
                continue;
            }

            if ((SKIP_TRIANGLES_BEYOND_MAX_BAKE_RANGE) && (t1.closestDistSqdToViewpoint > MAX_BAKE_RANGE_SQD)) {
                continue;
            }

            x[0] = fscreenx(t1.v1.screenx);
            y[0] = fscreeny(t1.v1.screeny);
            x[1] = fscreenx(t1.v2.screenx);
            y[1] = fscreeny(t1.v2.screeny);
            x[2] = fscreenx(t1.v3.screenx);
            y[2] = fscreeny(t1.v3.screeny);

            // Create a Path2D and then an Area describing the
            // triangle - lower down in the code, if this is not
            // completely obscured, we store it with the triangle.
            Path2D.Double trianglePath = new Path2D.Double();
            trianglePath.moveTo(x[0], y[0]);
            trianglePath.lineTo(x[1], y[1]);
            trianglePath.lineTo(x[2], y[2]);
            trianglePath.closePath();
            t1.area = new Area(trianglePath);
            t1.setBounds(t1.area.getBounds());

            if (tList.size() > 0) {

                // At this poitn we use our spatial lookup data structure to find
                // any other previously processed triangles that might overlap with the
                // new triangle, and subtract any such overlaps from the new triangle.

                numCandidateListSearches++;
                // From the set of all triangles that we've ALREADY
                // PROCESSED that might be in front of this triangle
                // (since we're going from "closest triangle to
                // viewpoint" to "furthest away") and have added to
                // our spatial lookup table,
                //
                // Get a set of triangles that _might_ overlap from
                // our 'spatial' lookup table.
                spatial.search(t1.area, occlusionCandidates);
                numCandidatesFound += occlusionCandidates.size();
                // For each candidate returned by spatial
                for (int loopj = 0; loopj < occlusionCandidates.size(); loopj++) {
                    Triangle t2 = occlusionCandidates.get(loopj);
                    if (DEBUG_CHECK_BOUNDS_OVERLAP) {
                        if (!t2.boundsOverlap(t1)) {
                            continue;
                        }
                    }
                    // Triangle.collision() is a 2D overlap test that
                    // _should_ be fairly fast.
                    if (Triangle.collision(t1, t2) > 0) {
                        t1.area.subtract(t2.area);
                        t1.setBounds(t1.area.getBounds());
                        t1.areaHasBeenClipped = true;
                        if (t1.area.isEmpty()) {
                            break;
                        }
                    }
                }
            }
            if (t1.area.isEmpty()) {
                t1.area = null;
                continue;
            }
            drawCount++;
            tList.add(t1);
            spatial.add(t1);
        }
        Debug.debug(1, "Baker.removeOcclusions: Done drawing " + drawCount + " triangles! numCandidatesFound = " + numCandidatesFound + " numCandidateListSearches = " + numCandidateListSearches + " avg  (i.e. for each triagnle processed we only compared against avg other triangles) = " + (numCandidatesFound / numCandidateListSearches));
    }
    // @TODO: I think I'm wrong below, I think i already hacked something
    // in that will update the texture VBO on the fly.  (although it doesn't (yet) manage
    // trackign what's changed and only uploading the changed parts of the VBO.)
    //
    // @TODO: Currently we re-create the Texture, every time it is
    // updated with a video frame, using
    // Texture.createTexture(BufferedImage); I'm not sure how much
    // impact that has on CPU, I'd like to clean it up, but for _now_
    // we create a BufferedImage with a writable raster and get the
    // pixels backing it, and then we can simply alter values in that
    // array and then call Texture.createTexture(BufferedImage) to
    // recreate the texture.
    //
    // @TODO: Move all of the pixel arrays and etc out to it's own
    // class - even if they're all just public members, it'll make
    // more sense.
    //
    // @TODO: what we _want_ to do to make things faster, we want to
    // operate on the texture data that we give to OpenGL.  So we need
    // to keep that buffer around, associated with some "change
    // extent" bounds, and then when we bake into the texture we
    // update the 'bounds' as changed.  THen when we're done we use
    // OpenGLs "glSubBufferData" to just upload the part that has
    // changed.  Right now we're re-creating the entire texture from
    // scratch every time we bake and it's taking a fair bit of time.
    // (On the order of 200 ms.)
    private int texRGBPixelsWidth;
    private int texRGBPixelsHeight;
    private int[] texRGBPixels = null;
    private BufferedImage texRGBPixelsImg = null;
    private int[] texRGBPixelsBackup = null;
    private ByteBuffer texPixelsBuffer = null;
    private int smallestIndexModified;
    private int largestIndexModified;
    private byte[] texBackup = null;
    int[] packedPixels = null;

    private void initRaster(TextureReader.Texture imagery, Mesh mesh) {
        BufferedImage img = imagery.getImg2();
        BufferedImage flipped = verticalflip(imagery.getImg2());

        texRGBPixelsWidth = img.getWidth();
        texRGBPixelsHeight = img.getHeight();
        int numPixels = texRGBPixelsWidth * texRGBPixelsHeight;
        packedPixels = new int[texRGBPixelsWidth * texRGBPixelsHeight * 3];

        texRGBPixels = img.getRGB(0, 0, texRGBPixelsWidth, texRGBPixelsHeight, null, 0, texRGBPixelsWidth);

        texRGBPixelsBackup = new int[texRGBPixels.length];
        System.arraycopy(texRGBPixels, 0, texRGBPixelsBackup, 0, texRGBPixels.length);

        int c, r, g, b;
        Debug.debug(4, "Width: " + img.getWidth() + "------------------------------------------------------------------------------- Height: " + img.getHeight() + " -------------------------------------------------------------------------------");

        for (int x = 0; x < flipped.getWidth(); x++) {
            for (int y = 0; y < flipped.getHeight(); y++) {
                c = flipped.getRGB(x, y);
                r = (c & 0xff0000) >> 16;
                g = (c & 0xff00) >> 8;
                b = c & 0xff;
                int texIndex = (y * texRGBPixelsWidth + x) * 3;
                packedPixels[texIndex++] = r;
                packedPixels[texIndex++] = g;
                packedPixels[texIndex] = b;

            }
        }
        if (DEBUG_USE_TEXTURE_BYTEBUFFER) {
            texPixelsBuffer = imagery.getPixels();
            texBackup = new byte[texPixelsBuffer.limit()];	// HACK HACK HACK HACK HACK HACK
            texPixelsBuffer.get(texBackup);
            texPixelsBuffer.flip();
        }

        // Create an integer data buffer to hold the pixel array
        DataBuffer data_buffer = new DataBufferInt(texRGBPixels, numPixels);

        // Need bit masks for the color bands for the ARGB color model
        int[] band_masks = {0xFF0000, 0xFF00, 0xFF, 0xFF000000};

        // Create a WritableRaster that will modify the image
        // when the pixels are modified.
        WritableRaster write_raster =
                Raster.createPackedRaster(data_buffer, texRGBPixelsWidth, texRGBPixelsHeight, texRGBPixelsWidth,
                band_masks, null);

        // Create a RGB color model
        ColorModel color_model = ColorModel.getRGBdefault();

        // Finally, build the image from the
        texRGBPixelsImg = new BufferedImage(color_model, write_raster, false, null);
    }
    private boolean doLateInit = true;

    public BufferedImage verticalflip(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(w, h, img.getType());
        Graphics2D g = dimg.createGraphics();
//        g.drawImage(img, 0, 0, w, h, 0, h, w, 0, null);
        g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null);
        g.drawImage(img, 0, 0, w, h, 0, h, w, 0, null);
        g.dispose();
        return dimg;
    }

    private void lateInit(Mesh mesh) {
        doLateInit = false;
        if (null == texRGBPixels) {
            TextureReader.Texture imagery = mesh.getImagery();
            if (imagery.getImg2() == null) {
                Debug.debug(4, "Baker.lateInit: ERROR The image texture from mesh is null, so BAKING WILL FAIL, since we don't have a texture to bake video frames into.");
                return;
            }
            initRaster(imagery, mesh);
        }

    }

    private void setVideoCameraPerspectiveFrustum(GL gl, double fovy, double aspect, double zNear, double zFar) {
        double xmin, xmax, ymin, ymax;

        ymax = zNear * Math.tan(fovy * Math.PI / 360.0);
        ymin = -ymax;

        xmin = ymin * aspect;
        xmax = ymax * aspect;

        gl.glFrustum(xmin, xmax, ymin, ymax, zNear, zFar);
    }
    private int[] viewport = new int[4];

    private void saveGLViewPortAndCalculateCameraProjection(GL gl) {
        // save current viewport;
        IntBuffer vpBuf = BufferUtil.newIntBuffer(4);
        gl.glGetIntegerv(GL.GL_VIEWPORT, vpBuf);
        vpBuf.get(viewport);

        // @TODO: why am I doing gluPerspective and then doing this
        // again with setVideoCameraPerspectiveFrustum?
        // gluPerspective wraps glFrustum, which is the same thing
        // that setVideoCameraPerspectiveFrustum wraps...
        //
        // EXCEPT that we're calling gluPerspective while in GL_MODELVIEW mode,
        // and then calling setVideoCameraPerspectiveFrustum while we're in GL_PROJECTION mode.
        //
        // which is just totally wierd really.  Gotta re-analyze this code and figure out what it's
        // supposed to be doing.

        gl.glViewport(0, 0, (int) VIDEO_FRAME_WIDTH, (int) VIDEO_FRAME_HEIGHT);
        GLU glu = new GLU();
        glu.gluPerspective(VIDEO_FOV_Y, VIDEO_FRAME_WIDTH / VIDEO_FRAME_HEIGHT, VIDEO_NEAR_CLIP_PLANE, VIDEO_FAR_CLIP_PLANE);

        // Set view frustum etc.

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();      // Reset The Perspective Matrix
        // @TODO:.... buh.... should be near,far not far,near... but this way seems to work
        setVideoCameraPerspectiveFrustum(gl, VIDEO_FOV_Y, VIDEO_ASPECT_RATIO, VIDEO_FAR_CLIP_PLANE, VIDEO_NEAR_CLIP_PLANE);
        gl.glViewport(0, 0, (int) VIDEO_FRAME_WIDTH, (int) VIDEO_FRAME_HEIGHT);
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    private void restoreGLViewPortAndProjection(GL gl) {
        // restore viewport
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    private void getTriangles(Projection projection) {
        ArrayList<Triangle> triangles = projection.trianglesAfterCulling;
        if (triangles == null) {
            Debug.debug(1, "Baker.getTriangles: WARNING: After projection, trianglesAfterCulling is null, no triangles to paint into!  Doing nothing.");
            return;
        }
        if (triangles.size() <= 0) {
            Debug.debug(1, "Baker.getTriangles: WARNING: After projection, trianglesAfterCulling.size() <= 0, no triangles to paint into!  Doing nothing.");
            return;
        }

        triangleAry = triangles.toArray(new Triangle[1]);
    }
    double timeToPaintTexelsTotal = 0;
    double paintTexelsCount = 0;

    // This is the meat of the class, where the magic happens.  THis
    // is where we figure out the correspondence between the video
    // frame and the terrain mesh and texture.
    private void paintVideoFrame2(GL gl, Mesh mesh, Projection projection, VideoFrame videoFrame) {
        boolean textureChanged = false;

        if (null == videoFrame) {
            Debug.debug(1, "Baker.paintVideoFrame: videoFrame is null, cannot paint.");
            return;
        }
        if (doLateInit) {
            lateInit(mesh);
        }

        long startTime;
        long elapsedTime;

//        model.addRenderable("UAV_LOCATION_BAKER" + videoFrame.uavid, uavMarker, (float) videoFrame.x, (float) videoFrame.y, (float) videoFrame.z);
        startTime = System.currentTimeMillis();

        if (DEBUG_USE_TEXTURE_BYTEBUFFER) {
            smallestIndexModified = texPixelsBuffer.limit() + 1;
            largestIndexModified = -1;
        }
        saveGLViewPortAndCalculateCameraProjection(gl);

        GLCamera videoCamera = new GLCamera();
        videoCamera.xPos = (float) videoFrame.x;
        videoCamera.yPos = (float) videoFrame.y;
        videoCamera.zPos = (float) videoFrame.z;
        videoCamera.xRot = (float) videoFrame.xRot;
        videoCamera.yRot = (float) videoFrame.yRot;
        videoCamera.zRot = (float) videoFrame.zRot;
        if (videoFrame.useDirectionVector) {
            videoCamera.setDirectionAndUp(videoFrame.dirx, videoFrame.diry, videoFrame.dirz, videoFrame.upx, videoFrame.upy, videoFrame.upz);
        }

        // Save the current video camera viewpoint, in case user wants
        // to see the world from that viewpoint.
        lastFrameViewpoint.xPos = videoCamera.xPos;
        lastFrameViewpoint.yPos = videoCamera.yPos;
        lastFrameViewpoint.zPos = videoCamera.zPos;
        lastFrameViewpoint.xRot = videoCamera.xRot;
        lastFrameViewpoint.yRot = videoCamera.yRot;
        lastFrameViewpoint.zRot = videoCamera.zRot;

        // Now we project and clip against view frustum - this sets
        // fields in each Triangle, marking them visibile (or not) and
        // calculating the screen coordinates of each triangle, given
        // it's current location and the current projection.
        projection.render(gl, videoCamera);

        // Get the visible triangles from Projection.
        getTriangles(projection);

        if (null == triangleAry) {
            Debug.debug(1, "Baker.paintVideoFrame: WARNING: projection resulted in null visible triangle list.  Nothing to bake frame into, returning.");
            restoreGLViewPortAndProjection(gl);
            return;
        }

        // Now we sort all triangles by their closest point to the viewpoint
        calcDistanceToViewpointAllTriangles(videoCamera);
        Arrays.sort(triangleAry);

        elapsedTime = System.currentTimeMillis() - startTime;
        Debug.debug(1, "Baker.paintVideoFrame: 1) Time to set up projection, distances, and sort triangles = " + elapsedTime);
        startTime = System.currentTimeMillis();

        // After the sort above, since the triangles form a terrain
        // mesh, we know there's no 3D overlap or interpenetration.
        // At this point we're basically doing a Painters algorithm to
        // clip any overlaps in 2D (i.e. terrain triangles behind a
        // hill, etc.) using the Java swing Area class.  We also keep
        // associated with each Triangle an Area object that describes
        // it's 2D outline.  (Which may no longer be a triangle after
        // clipping.)
        removeOcclusions();

        elapsedTime = System.currentTimeMillis() - startTime;
        Debug.debug(1, "Baker.paintVideoFrame: 2) Time to remove obscured parts = " + elapsedTime);
        startTime = System.currentTimeMillis();

        // From here on is where we copy the video frame pixels into the texture texels.

        int paintWidth = videoFrame.img.getWidth();
        int paintHeight = videoFrame.img.getHeight();

        int numPaintPixels = paintWidth * paintHeight;
        int[] paintPixels = null;

        if (RGB_GETRGB_ALL_AT_ONCE) {
            paintPixels = videoFrame.img.getRGB(0, 0, paintWidth, paintHeight, null, 0, paintWidth);
        }

        if (RGB_GET_SAMPLES_ALL_AT_ONCE) {
            paintPixels = new int[numPaintPixels * 3];
            paintPixels = videoFrame.img.getData().getPixels(0, 0, paintWidth, paintHeight, paintPixels);
        }
        byte[] bPaintPixels = null;
        if (RGB_GET_DATA_ELEMENTS_ALL_AT_ONCE) {
            paintPixels = new int[numPaintPixels];
            bPaintPixels = new byte[numPaintPixels * 3];
            WritableRaster srcRaster = videoFrame.img.getRaster();
            srcRaster.getDataElements(0, 0, paintWidth, paintHeight, bPaintPixels);
        }
        if (RGB_GET_ONE_AT_A_TIME_AND_CACHE) {
            paintPixels = new int[numPaintPixels];
            for (int loopi = 0; loopi < numPaintPixels; loopi++) {
                paintPixels[loopi] = Integer.MIN_VALUE;
            }
        }

        float[] projectedPoint = new float[3];
        float screenx;
        float screeny;

        int countPassedTest1 = 0;
        int countPassedTest2 = 0;
        int countPassedTest3 = 0;
        int countPassedTest4 = 0;
        for (int loopi = 0; loopi < triangleAry.length; loopi++) {
            Triangle t1 = triangleAry[loopi];

            // This is set by projection, really triangles with
            // visibleCount < 1 shouldn't even be in the list to start
            // with.
            if (t1.visibleCount < 1) {
                continue;
            }
            // If during clipping we eventually remove all of the Area
            // so that Area.isEmpty() returns true, then we set the
            // triangle's Area to null.
            if (null == t1.area) {
                continue;
            }

            // This is kind of a sanity check.  The entire point of
            // this process is to transfer color values from the video
            // frame into the texture associated with these triangles.
            // The float array rasterizedUvxyz is an array of n groups
            // of 5 coordinates, each one specifying the texel
            // coordinates in UV space and the corresponding position
            // on the triangle in object/world space.  Every triangle
            // SHOULD have some of these.
            if (null == t1.rasterizedUvxyz) {
                continue;
            }

            countPassedTest1++;
            for (int loopj = 0; loopj < t1.rasterizedUvxyz.length; loopj += 5) {
                // Map back to the texel in the texture
                int uPixels = (int) (t1.rasterizedUvxyz[loopj] * texRGBPixelsWidth);
                int vPixels = (int) (t1.rasterizedUvxyz[loopj + 1] * texRGBPixelsHeight);
                // Another sanity check, floating point roundoff
                // might come into play here.
                if ((uPixels < 0)
                        || (uPixels >= texRGBPixelsWidth)
                        || (vPixels < 0)
                        || (vPixels >= texRGBPixelsHeight)) {
                    continue;
                }

                // Since due to simple geometry, the further away
                // from the viewpoint a triangle is, the more the
                // video frame is distorted, this limits the max
                // range.  So basically we get video data from
                // close in, i.e. the 'bottom' of the video frame.
                double xdiff = videoCamera.xPos - t1.rasterizedUvxyz[loopj + 2];
                double ydiff = videoCamera.yPos - t1.rasterizedUvxyz[loopj + 3];
                double zdiff = videoCamera.zPos - t1.rasterizedUvxyz[loopj + 4];
                double distSqdFromViewpoint = (float) ((xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff));

                if (SKIP_TRIANGLES_BEYOND_MAX_BAKE_RANGE && (distSqdFromViewpoint > MAX_BAKE_RANGE_SQD)) {
                    continue;
                }

                // Now we project the x/y/z coords of the point in object/world space.
                projection.project(t1.rasterizedUvxyz[loopj + 2],
                        t1.rasterizedUvxyz[loopj + 3],
                        t1.rasterizedUvxyz[loopj + 4],
                        projectedPoint);
                // Map to the 'screen' i.e. the video frame.
                screenx = fscreenx(projectedPoint[0]);
                screeny = fscreeny(projectedPoint[1]);

                if (t1.areaHasBeenClipped) {
                    // Here is where we skip over texels that have
                    // been obscured by other triangles.
                    if (!(t1.area.contains(screenx, screeny))) {

                        // DEBUG: Paint texels obscured by geometry white for debug purposes
                        //			texRGBPixels[u*texRGBPixelsWidth + v] = Color.white.getRGB();
                        continue;
                    }
                }
                countPassedTest2++;

                // skip over texels that fall outside the video frame.
                if ((projectedPoint[0] < 0)
                        || (projectedPoint[0] >= paintWidth)
                        || (projectedPoint[1] < 0)
                        || (projectedPoint[1] >= paintHeight)) {

                    // DEBUG: Paint texels outside the video frame blue for debug purposes
                    //			texRGBPixels[u*texRGBPixelsWidth + v] = Color.blue.getRGB();

                    continue;
                }

                countPassedTest3++;

                // Ok, now we've got the texel index in the
                // texture, and we know it's not obscured, nor
                // outside the video frame, and we have it's
                // projected position inside the video frame.

                // Note it on the triangle for later dsplay in popup video billboard
                t1.addFrame(videoFrame);

                // Time to sample the color from the video frame
                // and copy it into the texel.

                double fTrueX = projectedPoint[0];
                double fTrueY = projectedPoint[1];

                int iFloorX = (int) (Math.floor(fTrueX));
                int iFloorY = (int) (Math.floor(fTrueY));
                int iCeilingX = (int) (Math.ceil(fTrueX));
                int iCeilingY = (int) (Math.ceil(fTrueY));

                // check bounds
                if (iFloorX < 0 || iCeilingX < 0
                        || iFloorX >= paintWidth
                        || iCeilingX >= paintWidth
                        || iFloorY < 0
                        || iCeilingY < 0
                        || iFloorY >= paintHeight
                        || iCeilingY >= paintHeight) {
                    continue;
                }

                // Avoid using the very edges - since some
                // distortion and other problems seem to
                // happen there.  Basically we throw away
                // anything PIXELS_TO_IGNORE_AT_EDGE pixels from the edge.
                if (iFloorX < PIXELS_TO_IGNORE_AT_EDGE
                        || iCeilingX < PIXELS_TO_IGNORE_AT_EDGE
                        || iFloorX >= (paintWidth - PIXELS_TO_IGNORE_AT_EDGE)
                        || iCeilingX >= (paintWidth - PIXELS_TO_IGNORE_AT_EDGE)
                        || iFloorY < PIXELS_TO_IGNORE_AT_EDGE
                        || iCeilingY < PIXELS_TO_IGNORE_AT_EDGE
                        || iFloorY >= (paintHeight - PIXELS_TO_IGNORE_AT_EDGE)
                        || iCeilingY >= (paintHeight - PIXELS_TO_IGNORE_AT_EDGE)) {
                    continue;
                }

                if (iFloorY < PIXELS_TO_IGNORE_AT_TOP_AND_BOTTOM
                        || iCeilingY < PIXELS_TO_IGNORE_AT_TOP_AND_BOTTOM
                        || iFloorY >= (paintHeight - PIXELS_TO_IGNORE_AT_TOP_AND_BOTTOM)
                        || iCeilingY >= (paintHeight - PIXELS_TO_IGNORE_AT_TOP_AND_BOTTOM)) {
                    continue;
                }
                if (SKIP_TOP_VBS_TELEMETRY_PIXELS) {
                    if (iFloorY >= (paintHeight - VBS_TELEMETRY_PIXELS_LINE_COUNT)
                            || iCeilingY >= (paintHeight - VBS_TELEMETRY_PIXELS_LINE_COUNT)) {
                        continue;
                    }
//                    if (iFloorY < VBS_TELEMETRY_PIXELS_LINE_COUNT
//                            || iCeilingY < VBS_TELEMETRY_PIXELS_LINE_COUNT) {
//                        continue;
//                    }
                }

                double fDeltaX = fTrueX - (double) iFloorX;
                double fDeltaY = fTrueY - (double) iFloorY;

                double fTopRed;
                double fTopGreen;
                double fTopBlue;
                double fBottomRed;
                double fBottomGreen;
                double fBottomBlue;

                if (RGB_GET_DATA_ELEMENTS_ALL_AT_ONCE) {
                    int clrTopLeftInd = (iFloorY * paintWidth + iFloorX) * 3;
                    int clrTopRightInd = (iCeilingY * paintWidth + iFloorX) * 3;
                    int clrBottomLeftInd = (iFloorY * paintWidth + iCeilingX) * 3;
                    int clrBottomRightInd = (iCeilingY * paintWidth + iCeilingX) * 3;
//
//                    // Hmmm, when we get into interpolating below, these will be promoted to double... sign extension problems
//                    // might happen, so we might need to just declare these as double and when we set them below, & them with
//                    // 0x000000FF to avoid sign extension.
//                    byte clrTopLeftR;
//                    byte clrTopLeftG;
//                    byte clrTopLeftB;
//                    byte clrTopRight;
//                    byte clrTopRightR;
//                    byte clrTopRightG;
//                    byte clrTopRightB;
//                    byte clrBottomLeftR;
//                    byte clrBottomLeftG;
//                    byte clrBottomLeftB;
//                    byte clrBottomRightR;
//                    byte clrBottomRightG;
//                    byte clrBottomRightB;
//
//                    clrTopLeftR = bPaintPixels[clrTopLeftInd];
//                    clrTopLeftG = bPaintPixels[clrTopLeftInd + 1];
//                    clrTopLeftB = bPaintPixels[clrTopLeftInd + 2];
//                    clrTopRightR = bPaintPixels[clrTopRightInd];
//                    clrTopRightG = bPaintPixels[clrTopRightInd + 1];
//                    clrTopRightB = bPaintPixels[clrTopRightInd + 2];
//                    clrBottomLeftR = bPaintPixels[clrTopRightInd];
//                    clrBottomLeftG = bPaintPixels[clrTopRightInd + 1];
//                    clrBottomLeftB = bPaintPixels[clrTopRightInd + 2];
//                    clrBottomRightR = bPaintPixels[clrBottomRightInd];
//                    clrBottomRightG = bPaintPixels[clrBottomRightInd + 1];
//                    clrBottomRightB = bPaintPixels[clrBottomRightInd + 2];
//
//                    // linearly interpolate horizontally between top neighbours
//                    fTopRed = (1 - fDeltaX) * (clrTopLeftR & 0xFF) + fDeltaX * (clrTopRightR & 0xFF);
//                    fTopGreen = (1 - fDeltaX) * (clrTopLeftG & 0xFF) + fDeltaX * (clrTopRightG & 0xFF);
//                    fTopBlue = (1 - fDeltaX) * (clrTopLeftB & 0xFF) + fDeltaX * (clrTopRightB & 0xFF);
//
//                    // linearly interpolate horizontally between bottom neighbours
//                    fBottomRed = (1 - fDeltaX) * (clrBottomLeftR & 0xFF) + fDeltaX * (clrBottomRightR & 0xFF);
//                    fBottomGreen = (1 - fDeltaX) * (clrBottomLeftG & 0xFF) + fDeltaX * (clrBottomRightG & 0xFF);
//                    fBottomBlue = (1 - fDeltaX) * (clrBottomLeftB & 0xFF) + fDeltaX * (clrBottomRightB & 0xFF);


                    // linearly interpolate horizontally between top neighbours
                    fTopRed = (1 - fDeltaX) * (bPaintPixels[clrTopLeftInd] & 0xFF) + fDeltaX * (bPaintPixels[clrTopRightInd] & 0xFF);
                    fTopGreen = (1 - fDeltaX) * (bPaintPixels[clrTopLeftInd + 1] & 0xFF) + fDeltaX * (bPaintPixels[clrTopRightInd + 1] & 0xFF);
                    fTopBlue = (1 - fDeltaX) * (bPaintPixels[clrTopLeftInd + 2] & 0xFF) + fDeltaX * (bPaintPixels[clrTopRightInd + 2] & 0xFF);

                    // linearly interpolate horizontally between bottom neighbours
                    fBottomRed = (1 - fDeltaX) * (bPaintPixels[clrBottomLeftInd] & 0xFF) + fDeltaX * (bPaintPixels[clrBottomRightInd] & 0xFF);
                    fBottomGreen = (1 - fDeltaX) * (bPaintPixels[clrBottomLeftInd + 1] & 0xFF) + fDeltaX * (bPaintPixels[clrBottomRightInd + 1] & 0xFF);
                    fBottomBlue = (1 - fDeltaX) * (bPaintPixels[clrBottomLeftInd + 2] & 0xFF) + fDeltaX * (bPaintPixels[clrBottomRightInd + 2] & 0xFF);
                } else {
                    int clrTopLeft;
                    int clrTopRight;
                    int clrBottomLeft;
                    int clrBottomRight;

                    int clrTopLeftInd = iFloorY * paintWidth + iFloorX;
                    int clrTopRightInd = iCeilingY * paintWidth + iFloorX;
                    int clrBottomLeftInd = iFloorY * paintWidth + iCeilingX;
                    int clrBottomRightInd = iCeilingY * paintWidth + iCeilingX;
                    // NOTE: In timing tests, running ten iterations of
                    // paintVideoFrame and averaging times (for same
                    // frame), difference between caching and just
                    // getting the four samples is very little.
                    if (RGB_GET_ONE_AT_A_TIME_AND_CACHE) {
                        if (paintPixels[clrTopLeftInd] == Integer.MIN_VALUE) {
                            paintPixels[clrTopLeftInd] = videoFrame.img.getRGB(iFloorX, iFloorY);
                        }
                        if (paintPixels[clrTopRightInd] == Integer.MIN_VALUE) {
                            paintPixels[clrTopRightInd] = videoFrame.img.getRGB(iFloorX, iCeilingY);
                        }
                        if (paintPixels[clrBottomLeftInd] == Integer.MIN_VALUE) {
                            paintPixels[clrBottomLeftInd] = videoFrame.img.getRGB(iCeilingX, iFloorY);
                        }
                        if (paintPixels[clrBottomRightInd] == Integer.MIN_VALUE) {
                            paintPixels[clrBottomRightInd] = videoFrame.img.getRGB(iCeilingX, iCeilingY);
                        }
                    }


                    clrTopLeft = paintPixels[clrTopLeftInd];
                    clrTopRight = paintPixels[clrTopRightInd];
                    clrBottomLeft = paintPixels[clrTopRightInd];
                    clrBottomRight = paintPixels[clrBottomRightInd];

                    // linearly interpolate horizontally between top neighbours
                    fTopRed = (1 - fDeltaX) * ((clrTopLeft >> 16) & 0xFF) + fDeltaX * ((clrTopRight >> 16) & 0xFF);
                    fTopGreen = (1 - fDeltaX) * ((clrTopLeft >> 8) & 0xFF) + fDeltaX * ((clrTopRight >> 8) & 0xFF);
                    fTopBlue = (1 - fDeltaX) * (clrTopLeft & 0xFF) + fDeltaX * (clrTopRight & 0xFF);

                    // linearly interpolate horizontally between bottom neighbours
                    fBottomRed = (1 - fDeltaX) * ((clrBottomLeft >> 16) & 0xFF) + fDeltaX * ((clrBottomRight >> 16) & 0xFF);
                    fBottomGreen = (1 - fDeltaX) * ((clrBottomLeft >> 8) & 0xFF) + fDeltaX * ((clrBottomRight >> 8) & 0xFF);
                    fBottomBlue = (1 - fDeltaX) * (clrBottomLeft & 0xFF) + fDeltaX * (clrBottomRight & 0xFF);
                }

                // linearly interpolate vertically between top and bottom interpolated results
//                int iRed = (int) (Math.round((1 - fDeltaY) * fTopRed + fDeltaY * fBottomRed));
//                int iGreen = (int) (Math.round((1 - fDeltaY) * fTopGreen + fDeltaY * fBottomGreen));
//                int iBlue = (int) (Math.round((1 - fDeltaY) * fTopBlue + fDeltaY * fBottomBlue));

                int iRed = (int) (((1 - fDeltaY) * fTopRed + fDeltaY * fBottomRed));
                int iGreen = (int) (((1 - fDeltaY) * fTopGreen + fDeltaY * fBottomGreen));
                int iBlue = (int) (((1 - fDeltaY) * fTopBlue + fDeltaY * fBottomBlue));

                // make sure colour values are valid
                if (iRed < 0) {
                    iRed = 0;
                }
                if (iRed > 255) {
                    iRed = 255;
                }
                if (iGreen < 0) {
                    iGreen = 0;
                }
                if (iGreen > 255) {
                    iGreen = 255;
                }
                if (iBlue < 0) {
                    iBlue = 0;
                }
                if (iBlue > 255) {
                    iBlue = 255;
                }
                textureChanged = true;

                // and set it in our texture array!
                if (DEBUG_USE_TEXTURE_BYTEBUFFER) {
                    int texIndex = (vPixels * texRGBPixelsWidth + uPixels) * 4;
                    texPixelsBuffer.put(texIndex++, (byte) iRed);
                    texPixelsBuffer.put(texIndex++, (byte) iGreen);
                    texPixelsBuffer.put(texIndex, (byte) iBlue);
                    if (texIndex > largestIndexModified) {
                        largestIndexModified = texIndex;
                    }
                    if (texIndex < smallestIndexModified) {
                        smallestIndexModified = texIndex;
                    }
                } else {
                    texRGBPixels[(texRGBPixelsHeight - vPixels - 1) * texRGBPixelsWidth + uPixels] = (iRed << 16) | (iGreen << 8) | iBlue;
                }
            }
        }

        elapsedTime = System.currentTimeMillis() - startTime;
        timeToPaintTexelsTotal += elapsedTime;
        paintTexelsCount++;
        Debug.debug(1, "Baker.paintVideoFrame: 3) Time to paint texels = " + elapsedTime + " on avg " + (timeToPaintTexelsTotal / paintTexelsCount));
        startTime = System.currentTimeMillis();

        // Above we did a lot of work in order to update the buffer of
        // RGB values that backs texRGBPixelsImg - now we are going to
        // use texRGBPixelsImg to re-create the Mesh Texture.

        // @TODO: this way of doing it is wasteful, the texture itself
        // is just a ByteBuffer, we should be able to make it a
        // ByteBuffer backed by an array we can access and just set
        // the texel values in that array as we go, instead of copying
        // them afterward (i.e. right here).
        //
        // The texture is 1000 x 1000 (or larger) so we're talking uploading 4
        // megabytes to the graphics card every time we regenerate the
        // texture.  This will be improved when we split up the
        // textures, but also could be greatly improved by only
        // re-uploading the changed part of the texture.
        //
        // On reflection, this process IS slow, but it might all be in
        // on the CPU side, i.e. the actual uploading may not be the
        // bottleneck.
        if (textureChanged) {
            if (DEBUG_USE_TEXTURE_BYTEBUFFER) {
                mesh.setImagery(mesh.getImagery());
            } else {
                TextureReader.Texture newTexture = TextureReader.createTexture(texRGBPixelsImg, true);
                mesh.setImagery(newTexture);
            }
        }
        Debug.debug(1, "Baker.paintVideoFrame: passed test1 (has rasterized uvs)=" + countPassedTest1 + ", passed test2 (screen coords in area)=" + countPassedTest2 + ", passed test3 (projected point in bounds of paint image) = " + countPassedTest3);

        elapsedTime = System.currentTimeMillis() - startTime;
        Debug.debug(1, "Baker.paintVideoFrame: 4) Time to regenerate texture = " + elapsedTime);
        restoreGLViewPortAndProjection(gl);
    }

    // This is the meat of the class, where the magic happens.  THis
    // is where we figure out the correspondence between the video
    // frame and the terrain mesh and texture.
    private void paintVideoFrame(GL gl, Mesh mesh, Projection projection, VideoFrame videoFrame) {
        boolean textureChanged = false;

        if (null == videoFrame) {
            Debug.debug(1, "Baker.paintVideoFrame: videoFrame is null, cannot paint.");
            return;
        }
        if (doLateInit) {
            lateInit(mesh);
        }

        long startTime;
        long elapsedTime;

//        model.addRenderable("UAV_LOCATION_BAKER" + videoFrame.uavid, uavMarker, (float) videoFrame.x, (float) videoFrame.y, (float) videoFrame.z);
        startTime = System.currentTimeMillis();

        smallestIndexModified = texPixelsBuffer.limit() + 1;
        largestIndexModified = -1;

        saveGLViewPortAndCalculateCameraProjection(gl);

        GLCamera videoCamera = new GLCamera();
        videoCamera.xPos = (float) videoFrame.x;
        videoCamera.yPos = (float) videoFrame.y;
        videoCamera.zPos = (float) videoFrame.z;
        videoCamera.xRot = (float) videoFrame.xRot;
        videoCamera.yRot = (float) videoFrame.yRot;
        videoCamera.zRot = (float) videoFrame.zRot;
        if (videoFrame.useDirectionVector) {
            videoCamera.setDirectionAndUp(videoFrame.dirx, videoFrame.diry, videoFrame.dirz, videoFrame.upx, videoFrame.upy, videoFrame.upz);
        }

        // Save the current video camera viewpoint, in case user wants
        // to see the world from that viewpoint.
        lastFrameViewpoint.xPos = videoCamera.xPos;
        lastFrameViewpoint.yPos = videoCamera.yPos;
        lastFrameViewpoint.zPos = videoCamera.zPos;
        lastFrameViewpoint.xRot = videoCamera.xRot;
        lastFrameViewpoint.yRot = videoCamera.yRot;
        lastFrameViewpoint.zRot = videoCamera.zRot;

        // Now we project and clip against view frustum - this sets
        // fields in each Triangle, marking them visibile (or not) and
        // calculating the screen coordinates of each triangle, given
        // it's current location and the current projection.
        projection.render(gl, videoCamera);

        // Get the visible triangles from Projection.
        getTriangles(projection);

        if (null == triangleAry) {
            Debug.debug(1, "Baker.paintVideoFrame: WARNING: projection resulted in null visible triangle list.  Nothing to bake frame into, returning.");
            restoreGLViewPortAndProjection(gl);
            return;
        }

        // Now we sort all triangles by their closest point to the viewpoint
        calcDistanceToViewpointAllTriangles(videoCamera);
        Arrays.sort(triangleAry);

        elapsedTime = System.currentTimeMillis() - startTime;
        Debug.debug(1, "Baker.paintVideoFrame: 1) Time to set up projection, distances, and sort triangles = " + elapsedTime);
        startTime = System.currentTimeMillis();

        // After the sort above, since the triangles form a terrain
        // mesh, we know there's no 3D overlap or interpenetration.
        // At this point we're basically doing a Painters algorithm to
        // clip any overlaps in 2D (i.e. terrain triangles behind a
        // hill, etc.) using the Java swing Area class.  We also keep
        // associated with each Triangle an Area object that describes
        // it's 2D outline.  (Which may no longer be a triangle after
        // clipping.)
        removeOcclusions();

        elapsedTime = System.currentTimeMillis() - startTime;
        Debug.debug(1, "Baker.paintVideoFrame: 2) Time to remove obscured parts = " + elapsedTime);
        startTime = System.currentTimeMillis();

        // From here on is where we copy the video frame pixels into the texture texels.

        int paintWidth = videoFrame.img.getWidth();
        int paintHeight = videoFrame.img.getHeight();
        int[] paintPixels = null;
        byte[] paintBytes = null;
        if (videoFrame.paintPixels != null) {
            paintPixels = videoFrame.paintPixels;
        } else if (videoFrame.paintBytes != null) {
            paintBytes = videoFrame.paintBytes;
        } else if (videoFrame.img.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            DataBufferByte buf = (DataBufferByte) videoFrame.img.getRaster().getDataBuffer();
            paintBytes = buf.getData();
        } else {
            paintPixels = videoFrame.img.getRGB(0, 0, paintWidth, paintHeight, null, 0, paintWidth);
        }

        float[] projectedPoint = new float[3];
        float screenx;
        float screeny;

        int countPassedTest1 = 0;
        int countPassedTest2 = 0;
        int countPassedTest3 = 0;
        int countPassedTest4 = 0;

        int packedPixelsIdx = 0;
        float minX = texRGBPixelsWidth, maxX = 0, minZ = texRGBPixelsHeight, maxZ = 0;

        for (int loopi = 0; loopi < triangleAry.length; loopi++) {
            Triangle t1 = triangleAry[loopi];

            // This is set by projection, really triangles with
            // visibleCount < 1 shouldn't even be in the list to start
            // with.
            if (t1.visibleCount < 1) {
                continue;
            }
            // If during clipping we eventually remove all of the Area
            // so that Area.isEmpty() returns true, then we set the
            // triangle's Area to null.
            if (null == t1.area) {
                continue;
            }

            // This is kind of a sanity check.  The entire point of
            // this process is to transfer color values from the video
            // frame into the texture associated with these triangles.
            // The float array rasterizedUvxyz is an array of n groups
            // of 5 coordinates, each one specifying the texel
            // coordinates in UV space and the corresponding position
            // on the triangle in object/world space.  Every triangle
            // SHOULD have some of these.
            if (null == t1.rasterizedUvxyz) {
                continue;
            }

            countPassedTest1++;
            for (int loopj = 0; loopj < t1.rasterizedUvxyz.length; loopj += 5) {
                // Map back to the texel in the texture
                int uPixels = (int) (t1.rasterizedUvxyz[loopj] * texRGBPixelsWidth);
                int vPixels = (int) (t1.rasterizedUvxyz[loopj + 1] * texRGBPixelsHeight);
                // Another sanity check, floating point roundoff
                // might come into play here.
                if ((uPixels < 0)
                        || (uPixels >= texRGBPixelsWidth)
                        || (vPixels < 0)
                        || (vPixels >= texRGBPixelsHeight)) {
                    continue;
                }

                // Since due to simple geometry, the further away
                // from the viewpoint a triangle is, the more the
                // video frame is distorted, this limits the max
                // range.  So basically we get video data from
                // close in, i.e. the 'bottom' of the video frame.
                double xdiff = videoCamera.xPos - t1.rasterizedUvxyz[loopj + 2];
                double ydiff = videoCamera.yPos - t1.rasterizedUvxyz[loopj + 3];
                double zdiff = videoCamera.zPos - t1.rasterizedUvxyz[loopj + 4];
                double distSqdFromViewpoint = (float) ((xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff));

                if (SKIP_TRIANGLES_BEYOND_MAX_BAKE_RANGE && (distSqdFromViewpoint > MAX_BAKE_RANGE_SQD)) {
                    continue;
                }

                // Now we project the x/y/z coords of the point in object/world space.
                projection.project(t1.rasterizedUvxyz[loopj + 2],
                        t1.rasterizedUvxyz[loopj + 3],
                        t1.rasterizedUvxyz[loopj + 4],
                        projectedPoint);

                if (t1.areaHasBeenClipped) {
                    // Here is where we skip over texels that have
                    // been obscured by other triangles.
                    if (!(t1.area.contains(projectedPoint[0], projectedPoint[1]))) {

                        // DEBUG: Paint texels obscured by geometry white for debug purposes
                        //			texRGBPixels[u*texRGBPixelsWidth + v] = Color.white.getRGB();
                        continue;
                    }
                }
                countPassedTest2++;

                int iFloorX = (int) projectedPoint[0];
                int iFloorY = (int) projectedPoint[1];
                int iCeilingX = iFloorX + 1;
                int iCeilingY = iFloorY + 1;

                // skip over texels that fall outside the video frame.
                if ((projectedPoint[0] < 0)
                        || (projectedPoint[0] >= paintWidth)
                        || (projectedPoint[1] < 0)
                        || (projectedPoint[1] >= paintHeight)) {

                    // DEBUG: Paint texels outside the video frame blue for debug purposes
                    //			texRGBPixels[u*texRGBPixelsWidth + v] = Color.blue.getRGB();

                    continue;
                }


                // Ok, now we've got the texel index in the
                // texture, and we know it's not obscured, nor
                // outside the video frame, and we have it's
                // projected position inside the video frame.

                // Note it on the triangle for later dsplay in popup video billboard
                t1.addFrame(videoFrame);

                // Time to sample the color from the video frame
                // and copy it into the texel.

                // check bounds
                if (iFloorX < 0 || iCeilingX < 0
                        || iFloorX >= paintWidth
                        || iCeilingX >= paintWidth
                        || iFloorY < 0
                        || iCeilingY < 0
                        || iFloorY >= paintHeight
                        || iCeilingY >= paintHeight) {
                    continue;
                }

                countPassedTest3++;

                // Avoid using the very edges - since some
                // distortion and other problems seem to
                // happen there.  Basically we throw away
                // anything PIXELS_TO_IGNORE_AT_EDGE pixels from the edge.
                if (iFloorX < PIXELS_TO_IGNORE_AT_EDGE
                        || iCeilingX < PIXELS_TO_IGNORE_AT_EDGE
                        || iFloorX >= (paintWidth - PIXELS_TO_IGNORE_AT_EDGE)
                        || iCeilingX >= (paintWidth - PIXELS_TO_IGNORE_AT_EDGE)
                        || iFloorY < PIXELS_TO_IGNORE_AT_EDGE
                        || iCeilingY < PIXELS_TO_IGNORE_AT_EDGE
                        || iFloorY >= (paintHeight - PIXELS_TO_IGNORE_AT_EDGE)
                        || iCeilingY >= (paintHeight - PIXELS_TO_IGNORE_AT_EDGE)) {
                    continue;
                }

                if (iFloorY < PIXELS_TO_IGNORE_AT_TOP_AND_BOTTOM
                        || iCeilingY < PIXELS_TO_IGNORE_AT_TOP_AND_BOTTOM
                        || iFloorY >= (paintHeight - PIXELS_TO_IGNORE_AT_TOP_AND_BOTTOM)
                        || iCeilingY >= (paintHeight - PIXELS_TO_IGNORE_AT_TOP_AND_BOTTOM)) {
                    continue;
                }
//                if (SKIP_TOP_VBS_TELEMETRY_PIXELS) {
//                    if (iFloorY >= (paintHeight - VBS_TELEMETRY_PIXELS_LINE_COUNT)
//                            || iCeilingY >= (paintHeight - VBS_TELEMETRY_PIXELS_LINE_COUNT)) {
//                        continue;
//                    }
////                    if (iFloorY < VBS_TELEMETRY_PIXELS_LINE_COUNT
////                            || iCeilingY < VBS_TELEMETRY_PIXELS_LINE_COUNT) {
////                        continue;
////                    }
//                }

                double fDeltaX = projectedPoint[0] - (double) iFloorX;
                double fDeltaY = projectedPoint[1] - (double) iFloorY;

                double fTopRed = 0;
                double fTopGreen = 0;
                double fTopBlue = 0;
                double fBottomRed = 0;
                double fBottomGreen = 0;
                double fBottomBlue = 0;

                int clrTopLeft;
                int clrTopRight;
                int clrBottomLeft;
                int clrBottomRight;



                if (null != paintPixels) {
                    int clrTopLeftInd = iFloorY * paintWidth + iFloorX;
                    int clrTopRightInd = iCeilingY * paintWidth + iFloorX;
                    int clrBottomLeftInd = iFloorY * paintWidth + iCeilingX;
                    int clrBottomRightInd = iCeilingY * paintWidth + iCeilingX;

                    clrTopLeft = paintPixels[clrTopLeftInd];
                    clrTopRight = paintPixels[clrTopRightInd];
                    clrBottomLeft = paintPixels[clrTopRightInd];
                    clrBottomRight = paintPixels[clrBottomRightInd];

                    // linearly interpolate horizontally between top neighbours
                    fTopRed = (1 - fDeltaX) * ((clrTopLeft >> 16) & 0xFF) + fDeltaX * ((clrTopRight >> 16) & 0xFF);
                    fTopGreen = (1 - fDeltaX) * ((clrTopLeft >> 8) & 0xFF) + fDeltaX * ((clrTopRight >> 8) & 0xFF);
                    fTopBlue = (1 - fDeltaX) * (clrTopLeft & 0xFF) + fDeltaX * (clrTopRight & 0xFF);

                    // linearly interpolate horizontally between bottom neighbours
                    fBottomRed = (1 - fDeltaX) * ((clrBottomLeft >> 16) & 0xFF) + fDeltaX * ((clrBottomRight >> 16) & 0xFF);
                    fBottomGreen = (1 - fDeltaX) * ((clrBottomLeft >> 8) & 0xFF) + fDeltaX * ((clrBottomRight >> 8) & 0xFF);
                    fBottomBlue = (1 - fDeltaX) * (clrBottomLeft & 0xFF) + fDeltaX * (clrBottomRight & 0xFF);
                } else if (null != paintBytes) {
                    int clrTopLeftInd = (iFloorY * paintWidth + iFloorX) * 3;
                    int clrTopRightInd = (iCeilingY * paintWidth + iFloorX) * 3;
                    int clrBottomLeftInd = (iFloorY * paintWidth + iCeilingX) * 3;
                    int clrBottomRightInd = (iCeilingY * paintWidth + iCeilingX) * 3;

                    double oneMinusDeltaX = 1 - fDeltaX;
                    // linearly interpolate horizontally between top neighbours
                    fTopRed = oneMinusDeltaX * (paintBytes[clrTopLeftInd + 2] & 0xff) + fDeltaX * (paintBytes[clrTopRightInd + 2] & 0xff);
                    fTopGreen = oneMinusDeltaX * (paintBytes[clrTopLeftInd + 1] & 0xff) + fDeltaX * (paintBytes[clrTopRightInd + 1] & 0xff);
                    fTopBlue = oneMinusDeltaX * (paintBytes[clrTopLeftInd] & 0xff) + fDeltaX * (paintBytes[clrTopRightInd] & 0xff);

                    // linearly interpolate horizontally between bottom neighbours
                    fBottomRed = oneMinusDeltaX * (paintBytes[clrBottomLeftInd + 2] & 0xff) + fDeltaX * (paintBytes[clrBottomRightInd + 2] & 0xff);
                    fBottomGreen = oneMinusDeltaX * (paintBytes[clrBottomLeftInd + 1] & 0xff) + fDeltaX * (paintBytes[clrBottomRightInd + 1] & 0xff);
                    fBottomBlue = oneMinusDeltaX * (paintBytes[clrBottomLeftInd] & 0xff) + fDeltaX * (paintBytes[clrBottomRightInd] & 0xff);
                }

                int iRed = (int) (((1 - fDeltaY) * fTopRed + fDeltaY * fBottomRed));
                int iGreen = (int) (((1 - fDeltaY) * fTopGreen + fDeltaY * fBottomGreen));
                int iBlue = (int) (((1 - fDeltaY) * fTopBlue + fDeltaY * fBottomBlue));
                // make sure colour values are valid
                if (iRed < 0) {
                    iRed = 0;
                }
                if (iRed > 255) {
                    iRed = 255;
                }
                if (iGreen < 0) {
                    iGreen = 0;
                }
                if (iGreen > 255) {
                    iGreen = 255;
                }
                if (iBlue < 0) {
                    iBlue = 0;
                }
                if (iBlue > 255) {
                    iBlue = 255;
                }
                textureChanged = true;

                int texIndex = (vPixels * texRGBPixelsWidth + uPixels) * 4;
                texPixelsBuffer.put(texIndex++, (byte) iRed);
                texPixelsBuffer.put(texIndex++, (byte) iGreen);
                texPixelsBuffer.put(texIndex++, (byte) iBlue);
                texPixelsBuffer.put(texIndex, (byte) 220);

                packedPixelsIdx = (vPixels * texRGBPixelsWidth + uPixels) * 3;
                packedPixels[packedPixelsIdx++] = (byte) iRed;
                packedPixels[packedPixelsIdx++] = (byte) iGreen;
                packedPixels[packedPixelsIdx] = (byte) iBlue;

                if (texIndex > largestIndexModified) {
                    largestIndexModified = texIndex;
                }
                if (texIndex < smallestIndexModified) {
                    smallestIndexModified = texIndex;
                }
//                if ((iRed > 210 && iRed < 250) && (iGreen > 120 && iGreen < 170) && (iBlue > 80 && iBlue < 130)) {
//                    State currentMarker = new State("TargetMarker_" + System.currentTimeMillis(), StateEnums.StateType.ATR);
//                    stateDB.put(currentMarker);
//                    currentMarker.setPos(t1.rasterizedUvxyz[loopj + 2], 100, t1.rasterizedUvxyz[loopj + 4]);
//
////                    Debug.debug(4, "------------------------------------------------------------------------------- " + t1.rasterizedUvxyz[loopj + 2] + " - " + t1.rasterizedUvxyz[loopj + 3] + 40 + " - " + t1.rasterizedUvxyz[loopj + 4]);
//                }


                //Find min - max value

                if (uPixels < minX) {
                    minX = uPixels;
                }
                if (uPixels > maxX) {
                    maxX = uPixels;
                }
                if (vPixels < minZ) {
                    minZ = 4096 - vPixels;
                }
                if (vPixels > maxZ) {
                    maxZ = 4096 - vPixels;
                }

            }
        }

        if (textureChanged) {
            //Lets find out which index the texture belongs to
            imageIdx++;
            /*2487
            if (imageIdx > 5520) {
                int coordX = 0;
                int coordZ = 0;

                int[][] coords = new int[4][2];
                int[][] uniquecoords = new int[4][2];
                for (int i = 0; i < uniquecoords.length; i++) {
                    uniquecoords[i][0] = -1;
                    uniquecoords[i][1] = -1;
                }

                int ctr;
                //For all 4 coordinates we want to find out which segments we want o save.

                for (ctr = 0; ctr < xcoordinates.length; ctr++) {
                    if ((minX) > xcoordinates[ctr]) {
                        coords[0][0] = xcoordinates[ctr];
                    }
                }
                for (ctr = 0; ctr < zcoordinates.length; ctr++) {
                    if ((minZ) > zcoordinates[ctr]) {
                        coords[0][1] = zcoordinates[ctr];
                    }
                }

                //
                for (ctr = 0; ctr < xcoordinates.length; ctr++) {
                    if ((maxX) > xcoordinates[ctr]) {
                        coords[1][0] = xcoordinates[ctr];
                    }
                }
                for (ctr = 0; ctr < zcoordinates.length; ctr++) {
                    if ((minZ) > zcoordinates[ctr]) {
                        coords[1][1] = zcoordinates[ctr];
                    }
                }

                //
                for (ctr = 0; ctr < xcoordinates.length; ctr++) {
                    if ((maxX) > xcoordinates[ctr]) {
                        coords[2][0] = xcoordinates[ctr];
                    }
                }
                for (ctr = 0; ctr < zcoordinates.length; ctr++) {
                    if ((maxZ) > zcoordinates[ctr]) {
                        coords[2][1] = zcoordinates[ctr];
                    }
                }
                //
                for (ctr = 0; ctr < xcoordinates.length; ctr++) {
                    if ((minX) > xcoordinates[ctr]) {
                        coords[3][0] = xcoordinates[ctr];
                    }
                }
                for (ctr = 0; ctr < zcoordinates.length; ctr++) {
                    if ((maxZ) > zcoordinates[ctr]) {
                        coords[3][1] = zcoordinates[ctr];
                    }
                }
                //Now find the unique values in array
                boolean found;
                int idx = 0;
                for (ctr = 0; ctr < coords.length; ctr++) {
                    //                Debug.debug(4, "Baker.boundryrectangle ---------------------------------- coords: ctr " + ctr + " - " + imageIdx + " :: " + minX + ":" + maxX + " - " + minZ + ":" + maxZ + " :: " + coords[ctr][0] + ":" + coords[ctr][1]);
                    found = false;
                    for (int i = 0; i < uniquecoords.length; i++) {
                        if ((uniquecoords[i][0] == coords[ctr][0]) && (uniquecoords[i][1] == coords[ctr][1])) {
                            found = true;
                        }
                    }
                    if (found == false) {
                        uniquecoords[idx][0] = coords[ctr][0];
                        uniquecoords[idx][1] = coords[ctr][1];
                        //                    Debug.debug(4, "Baker.boundryrectangle ---------------------------------- uniquecoords: i " + idx + " - " + imageIdx + " :: " + minX + ":" + maxX + " - " + minZ + ":" + maxZ + " :: " + uniquecoords[idx][0] + "-" + coords[ctr][0] + ":" + uniquecoords[idx][0] + "-" + coords[ctr][1]);
                        idx++;
                    }

                }

                for (int i = 0; i < uniquecoords.length; i++) {
                    if (uniquecoords[i][0] != -1 && uniquecoords[i][1] != -1) {
                        File outputfile = new File("/Users/sha33/NetBeansProjects/vbs2gui/saved/" + imageIdx + "_" + uniquecoords[i][0] + "x" + uniquecoords[i][1] + ".png");
                        //                    Debug.debug(4, "Baker.boundryrectangle ---------------------------------- Raster Boundary Match Found: i " + i + " - " + imageIdx + " :: " + minX + ":" + maxX + " - " + minZ + ":" + maxZ + " :: " + uniquecoords[i][0] + ":" + uniquecoords[i][1]);
                        if (!outputfile.exists()) {
                            BufferedImage savetexRGBPixelsImg = new BufferedImage(texRGBPixelsWidth, texRGBPixelsHeight, BufferedImage.TYPE_INT_RGB);
                            WritableRaster write_raster = savetexRGBPixelsImg.getRaster();
                            write_raster.setPixels(0, 0, texRGBPixelsWidth, texRGBPixelsHeight, packedPixels);

                            Rectangle rect = new Rectangle(400, 400);
                            BufferedImage flippedImage = verticalflip(savetexRGBPixelsImg);
                            BufferedImage cropped = flippedImage.getSubimage(uniquecoords[i][0], uniquecoords[i][1], rect.width, rect.height);

                            try {
                                //                    ImageIO.write(verticalflip(cropped), "png", outputfile);
                                ImageIO.write((cropped), "png", outputfile);
                            } catch (IOException ex) {
                                Debug.debug(4, "Baker.lateInit: ERROR Image Save FAILED!.");
                            }
                            //Getting rid of all huge variables
                            savetexRGBPixelsImg = null;
                            write_raster = null;
                            rect = null;
                            flippedImage = null;
                            cropped = null;

                        }
                        outputfile = null;

                        fileIndex.add(imageIdx + "_" + uniquecoords[i][0] + "x" + uniquecoords[i][1]);
                    }
                }
            }

            */
        }

        elapsedTime = System.currentTimeMillis() - startTime;
        timeToPaintTexelsTotal += elapsedTime;
        paintTexelsCount++;
        Debug.debug(1, "Baker.paintVideoFrame: 3) Time to paint texels = " + elapsedTime + " on avg " + (timeToPaintTexelsTotal / paintTexelsCount));
        startTime = System.currentTimeMillis();

        // Above we did a lot of work in order to update the buffer of
        // RGB values that backs texRGBPixelsImg - now we are going to
        // use texRGBPixelsImg to re-create the Mesh Texture.

        // @TODO: this way of doing it is wasteful, the texture itself
        // is just a ByteBuffer, we should be able to make it a
        // ByteBuffer backed by an array we can access and just set
        // the texel values in that array as we go, instead of copying
        // them afterward (i.e. right here).
        //
        // The texture is 1000 x 1000 (or larger) so we're talking uploading 4
        // megabytes to the graphics card every time we regenerate the
        // texture.  This will be improved when we split up the
        // textures, but also could be greatly improved by only
        // re-uploading the changed part of the texture.
        //
        // On reflection, this process IS slow, but it might all be in
        // on the CPU side, i.e. the actual uploading may not be the
        // bottleneck.
        if (textureChanged) {
            Debug.debug(1, "Baker.paintVideoFrame: smallestChanged=" + smallestIndexModified + " largestChanged=" + largestIndexModified + " diff = " + (largestIndexModified - smallestIndexModified) + " bytes or " + ((largestIndexModified - smallestIndexModified) / 1024) + " kb or " + ((largestIndexModified - smallestIndexModified) / (1024 * 1024)) + " mb whole buffer is " + texPixelsBuffer.limit() + " bytes or " + texPixelsBuffer.limit() / (1024 * 1024) + " mb, percent changed vs whole buffer = " + ((double) (largestIndexModified - smallestIndexModified) / (double) texPixelsBuffer.limit()));
            mesh.setImagery(mesh.getImagery());
        }
        Debug.debug(1, "Baker.paintVideoFrame: passed test1 (has rasterized uvs)=" + countPassedTest1 + ", passed test2 (screen coords in area)=" + countPassedTest2 + ", passed test3 (projected point in bounds of paint image) = " + countPassedTest3);

        elapsedTime = System.currentTimeMillis() - startTime;
        Debug.debug(1, "Baker.paintVideoFrame: 4) Time to regenerate texture = " + elapsedTime);
        restoreGLViewPortAndProjection(gl);

    }
    double timeToPaintTexelsTotalProjection = 0;
    double paintTexelsCountProjection = 0;

    private void resetTerrainTexture(Mesh mesh) {
        if (null == texRGBPixels) {
            TextureReader.Texture imagery = mesh.getImagery();
            if (imagery.getImg2() == null) {
                Debug.debug(1, "Baker.paintVideoFrame: Can't paint into texture because BufferedImage img is null!");
                return;
            }
            initRaster(imagery, mesh);
        }

        if (DEBUG_USE_TEXTURE_BYTEBUFFER) {
            texPixelsBuffer.rewind();
            texPixelsBuffer.put(texBackup);
            texPixelsBuffer.flip();
            mesh.setImagery(mesh.getImagery());
        } else {
            System.arraycopy(texRGBPixelsBackup, 0, texRGBPixels, 0, texRGBPixels.length);
            // and make the new texture this writable image.
            TextureReader.Texture newTexture = TextureReader.createTexture(texRGBPixelsImg, true);
        }
    }

    // Why do we need these params?
    //
    // 1) GL?  To get the current viewport.  Because we're using some
    // GL stuff to do some calculations, we get the current
    // viewport, save it, set our new viewport and such to match the
    // video camera AND THEN DO SOMETHING - I DON'T REMEMBER EXACTLY
    // WHAT so I should look at that and re-figure it out.  And
    // comment it.  We also use it to pass to Projection.render()
    //
    // 2) The Projection class does the grunt work of projecting each
    // triangle onto our video frame and culling those not visibile.
    // Really we should pass Projection in at construction or
    // something and not need to pass it in here.
    //
    // 3) Mesh to do the actual baking into
    public void bake(GL gl, Projection projection, Mesh mesh) {

        BakerCommand com = commandQueue.poll();
        if (null == com) {
            return;
        }

        if (BakerCommand.Type.RESET_TEXTURE == com.type) {
            resetTerrainTexture(mesh);
            lastCommandWasReset = true;
        } else if (BakerCommand.Type.BAKE_FRAME == com.type) {
            BakerCommand com2 = null;
            // Check if this is BAKE_FRAME is going to be immediately
            // reset by the next command, and if so, skip it.
            com2 = commandQueue.peek();
            if (null != com2) {
                if (lastCommandWasReset && (BakerCommand.Type.RESET_TEXTURE == com2.type)) {
                    commandQueue.poll();
                    return;
                }
            }
            paintVideoFrame(gl, mesh, projection, com.videoFrame);
            lastCommandWasReset = false;
        } else if (BakerCommand.Type.SWAP_VIEWPOINT == com.type) {
            float temp;
            temp = userViewCamera.xPos;
            userViewCamera.xPos = lastFrameViewpoint.xPos;
            lastFrameViewpoint.xPos = temp;
            temp = userViewCamera.yPos;
            userViewCamera.yPos = lastFrameViewpoint.yPos;
            lastFrameViewpoint.yPos = temp;
            temp = userViewCamera.zPos;
            userViewCamera.zPos = lastFrameViewpoint.zPos;
            lastFrameViewpoint.zPos = temp;
            temp = userViewCamera.xRot;
            userViewCamera.xRot = lastFrameViewpoint.xRot;
            lastFrameViewpoint.xRot = temp;
            temp = userViewCamera.yRot;
            userViewCamera.yRot = lastFrameViewpoint.yRot;
            lastFrameViewpoint.yRot = temp;
            temp = userViewCamera.zRot;
            userViewCamera.zRot = lastFrameViewpoint.zRot;
            lastFrameViewpoint.zRot = temp;
        }

    }

    public void swapViewpoint(boolean pressed) {
        if (pressed) {
            BakerCommand bc = new BakerCommand();
            bc.type = BakerCommand.Type.SWAP_VIEWPOINT;
            queueCommand(bc);
        }
    }

    public static ArrayList<String> getFileIndex() {
        Debug.debug(4, "Baker.boundryrectangle ---------------------------------- getFileIndex: i " + fileIndex.size());
        return fileIndex;
    }
}
