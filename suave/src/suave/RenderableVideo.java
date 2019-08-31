package suave;

import javax.media.opengl.GL;

public class RenderableVideo implements Renderable {

    public final static String VIDEO_POPUP_KEY = "VIDEO";
    public final static boolean DEBUG_LOOP_VIDEO = true;
    private Model model;
    private GLCamera camera;
    private int textID;
    private TextureReader.Texture[] textures;
    private RenderableInterleavedVBO rivbo;
    private float x;
    private float y;
    private float z;
    int textCounter = 0;
    boolean loading = true;

    public void setTextures(TextureReader.Texture[] textures, boolean stillLoading) {
        synchronized (this) {
            this.textures = textures;
            loading = stillLoading;
        }
    }

    public RenderableVideo(Model model, GLCamera camera, int textID, TextureReader.Texture[] textures, RenderableInterleavedVBO rivbo, float x, float y, float z) {
        this.model = model;
        this.camera = camera;
        this.textID = textID;
        this.textures = textures;
        this.rivbo = rivbo;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    private final static int TEXTURE_LEVEL = 0; // mipmapping parameter - we're not using mipmaps (yet).

    private void incTexture(GL gl) {
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textID);    // Bind The Texture

        TextureReader.Texture texture;
        boolean stillLoading = false;
        synchronized (this) {
            if (null == textures) {
                model.removeRenderable(VIDEO_POPUP_KEY);
            }
            texture = textures[textCounter];
            stillLoading = loading;
        }

        if (null != texture) {
            gl.glTexImage2D(GL.GL_TEXTURE_2D,
                    TEXTURE_LEVEL,
                    GL.GL_RGBA8,
                    texture.getWidth(),
                    texture.getHeight(),
                    0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE,
                    texture.getPixels());
        }

        if (stillLoading) {
            return;
        }

        textCounter++;
        if (textCounter >= textures.length) {
            if (DEBUG_LOOP_VIDEO) {
                textCounter = 0;
            } else {
                model.removeRenderable(VIDEO_POPUP_KEY);
            }
        }
    }

    public void render(GL gl) {
        incTexture(gl);
        billboardSphericalBegin(gl,
                camera.xPos, camera.yPos, camera.zPos,
                x, y, z);
        rivbo.render(gl);
        billboardSphericalEnd(gl);
    }

    public void destroy(GL gl) {
        int[] foo = new int[1];
        foo[0] = textID;
        gl.glDeleteBuffersARB(1, foo, 0);
    }

    // original C version from;
    // 
    // http://www.lighthouse3d.com/opengl/billboarding/index.php?billSphe
    // 
    private void billboardSphericalBegin(GL gl,
            float camX, float camY, float camZ,
            float objPosX, float objPosY, float objPosZ) {

        float lookAt[] = new float[3];
        float objToCamProj[] = new float[3];
        float upAux[] = new float[3];
        float modelview[] = new float[16];
        float angleCosine;
        float objToCam[] = new float[3];

        gl.glPushMatrix();

        // objToCamProj is the vector in world coordinates from the
        // local origin to the camera projected in the XZ plane
        objToCamProj[0] = camX - objPosX;
        objToCamProj[1] = 0;
        objToCamProj[2] = camZ - objPosZ;

        // This is the original lookAt vector for the object
        // in world coordinates
        lookAt[0] = 0;
        lookAt[1] = 0;
        lookAt[2] = 1;

        // normalize both vectors to get the cosine directly afterwards
        Vec3f.normalize(objToCamProj);

        // easy fix to determine wether the angle is negative or positive
        // for positive angles upAux will be a vector pointing in the
        // positive y direction, otherwise upAux will point downwards
        // effectively reversing the rotation.

        Vec3f.cross(upAux, lookAt, objToCamProj);

        // compute the angle
        angleCosine = Vec3f.dot(lookAt, objToCamProj);

        // perform the rotation. The if statement is used for stability reasons
        // if the lookAt and objToCamProj vectors are too close together then
        // |angleCosine| could be bigger than 1 due to lack of precision
        if ((angleCosine < 0.9999999) && (angleCosine > -0.9999999)) {
            float angle = (float) (Math.acos(angleCosine) * (180 / Math.PI));
            gl.glRotatef(angle, upAux[0], upAux[1], upAux[2]);
        }

        // so far it is just like the cylindrical billboard. The code for the
        // second rotation comes now
        // The second part tilts the object so that it faces the camera

        // objToCam is the vector in world coordinates from
        // the local origin to the camera
        objToCam[0] = camX - objPosX;
        objToCam[1] = camY - objPosY;
        objToCam[2] = camZ - objPosZ;

        // Normalize to get the cosine afterwards
        Vec3f.normalize(objToCam);

        // Compute the angle between objToCamProj and objToCam,
        //i.e. compute the required angle for the lookup vector
        angleCosine = Vec3f.dot(objToCamProj, objToCam);

        // Tilt the object. The test is done to prevent instability
        // when objToCam and objToCamProj have a very small
        // angle between them

        if ((angleCosine < 0.99990) && (angleCosine > -0.9999)) {
            float angle = (float) (Math.acos(angleCosine) * (180 / Math.PI));
            if (objToCam[1] < 0) {
                gl.glRotatef(angle, 1, 0, 0);
            } else {
                gl.glRotatef(angle, -1, 0, 0);
            }
        }
    }

    void billboardSphericalEnd(GL gl) {
        gl.glPopMatrix();
    }
}
