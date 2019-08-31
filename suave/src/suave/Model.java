package suave;

import java.awt.event.MouseEvent;

// for debugWhiteoutTriangle
import javax.media.opengl.GL;
import java.util.*;
import java.awt.image.*;
import java.awt.*;

// http://www.java-tips.org/other-api-tips/jogl/use-of-some-of-the-gluquadric-routines.html
public class Model implements StateEnums {

    private static final boolean DRAW_TRACK_LINES = true;
    private static int HAS_LINE_SKIP_EVERY = 5;
    private static int HAS_LINE_NUM_ELEMENTS = 20;
    private static int HAS_LINE_REBUILD_LINE_EVERY_N_FRAMES = 5;
    private static final boolean DRAW_AXIS = false;
    private boolean initFlag = true;
    private boolean drawSanjayaUAVs = true;
    private StateDB stateDB = null;
    private TextureDB textureDB = null;
    private SkyAndGround skyAndGround = null;
    private DEM dem = null;
    private Mesh mesh = null;
    private UAVCamera uavCamera = null;
    private Skybox skybox = null;
    private final HashMap<String, PosRotRenderable> renderables = new HashMap<String, PosRotRenderable>();
    private Renderable humveeBlueforFlag;
    private Renderable humveeOpforFlag;
    private Renderable humveeBluefor;
    private Renderable humveeOpfor;
    private Renderable sphereBluefor;
    private Renderable sphereOpfor;
    private Renderable sphereNeutral;
    private Renderable sphereBlack;
    private Renderable sphereBlue;
    private Renderable sphereGreen;
    private Renderable sphereRed;
    private Renderable sphereColor0;
    private Renderable sphereColor1;
    private Renderable sphereColor2;
    private Renderable sphereColor3;
    private Renderable sphereColor4;
    private Renderable sphereColor5;
    private Renderable sphereColor6;
    private Renderable sphereColor7;
    private Renderable sphereColor8;
    private Renderable sphereColor9;
    private Renderable sphereColor10;
    private Renderable sphereColor11;
    private Renderable sphereColor12;
    private Renderable sphereColor13;
    private Renderable sphereColor14;
    private Renderable sphereColor15;
    private Renderable sphereColor16;
    private Renderable sphereColor17;
    private Renderable sphereColor18;
    private Renderable sphereColor19;
    private Renderable sphereColor20;
    private Renderable sphereColor21;
    private Renderable sphereColor22;
    private Renderable sphereColor23;
    private Renderable sphereWhite;
    private Renderable uav;
    private Renderable rSelectionPressed;
    private Renderable rSelectionReleased;
    private Renderable targetMarker;
    private Renderable targetMarkerHighlighted;
    private Renderable targetMarkerSelected;
    private Renderable atr;
    private boolean selectionPressed = false;
    private long renderCounter = 0;
    private long rebuildLinesAfter = 250;
    private boolean followingUav0 = false;
    private GLCamera camera;

    public boolean isSelectionPressed() {
        return selectionPressed;
    }

    // @TODO: Move this out into a SelectEvent controller/observer.
    public void setSelectionPressed(boolean value, MouseEvent event) {
        selectionPressed = value;
        PosRotRenderable p;
        synchronized (renderables) {
            p = renderables.get("MODEL.SELECTION");
        }
        if (null == p) {
            return;
        }

        if (selectionPressed) {

            if ((event.getModifiers() & event.BUTTON1_MASK) != 0) {
                p.r = rSelectionPressed;
                if (null == lineBeingDrawn) {
                    //		    Debug.debug(1,"Model.setSelectionPressed: Starting new line!");
                    lineBeingDrawn = new State("LINE_BEING_DRAWN", StateEnums.StateType.LINE);
                }
                stateDB.put(lineBeingDrawn);
            } else if ((event.getModifiers() & event.BUTTON3_MASK) != 0) {
            }
        } else {
            p.r = rSelectionReleased;
            lineBeingDrawn = null;
        }
    }
    private State lineBeingDrawn = null;

    public void setSelectionPoint(float x, float y, float z) {
        synchronized (renderables) {
            PosRotRenderable p = renderables.get("MODEL.SELECTION");
            if (null == p) {
                return;
            }
            p.pr.setPos(x, y, z);
        }
        if (selectionPressed) {
            //	    Debug.debug(1,"Model.setSelectionPoint: Adding point to lineBeingDrawn, p="+x+", "+y+", "+z+" line="+lineBeingDrawn);
            lineBeingDrawn.addLinePoint(x, z, y + 5);
        }
    }
    private boolean meshReady = false;
    private Object meshReadyLock = new Object();

    public boolean isMeshReady() {
        synchronized (meshReadyLock) {
            return meshReady;
        }
    }

    private void setMeshReady(boolean value) {
        synchronized (meshReadyLock) {
            meshReady = value;
        }
    }
    private boolean renderUAV = true;

    public void uavRenderingOnOff(boolean flag) {
        if (flag) {
            renderUAV = !renderUAV;
            Debug.debug(1, "Model.uavRenderingOnOff: set renderUAV to " + renderUAV);
        }
    }
    private boolean drawFlags = true;

    public void flipFlags(boolean pressed) {
        if (pressed) {
            //Debug.debug(1, "Model.flipFlags: Setting drawFlags to " + !drawFlags);
            synchronized (renderables) {
                drawFlags = !drawFlags;
                // @TODO: simply clearing the renderables is probably
                // a _bad_ idea with the new statedb stuff.  Among
                // other things it will throw away any user generated
                // lines.
                renderables.clear();
            }
        }
    }

    public void debugWhiteOutTriangle(Triangle t) {
        if (null == t.rasterizedUvxyz) {
            return;
        }

        BufferedImage img = mesh.getImagery().getImg2();

        int texRGBPixelsWidth;
        int texRGBPixelsHeight;
        int[] texRGBPixels = null;
        BufferedImage texRGBPixelsImg = null;

        texRGBPixelsWidth = img.getWidth();
        texRGBPixelsHeight = img.getHeight();
        int numPixels = texRGBPixelsWidth * texRGBPixelsHeight;
        TextureReader.Texture imagery = mesh.getImagery();

        texRGBPixels = img.getRGB(0, 0, texRGBPixelsWidth, texRGBPixelsHeight, null, 0, texRGBPixelsWidth);

        // Create an integer data buffer to hold the pixel array
        DataBuffer data_buffer = new DataBufferInt(texRGBPixels, numPixels);

        // Need bit masks for the color bands for the ARGB color model
        int[] band_masks = {0xFF0000, 0xFF00, 0xff, 0xff000000};

        // Create a WritableRaster that will modify the image
        // when the pixels are modified.
        WritableRaster write_raster =
                Raster.createPackedRaster(data_buffer, texRGBPixelsWidth, texRGBPixelsHeight, texRGBPixelsWidth,
                band_masks, null);

        // Create a RGB color model
        ColorModel color_model = ColorModel.getRGBdefault();

        // Finally, build the image from the
        texRGBPixelsImg = new BufferedImage(color_model, write_raster, false, null);

        for (int loopj = 0; loopj < t.rasterizedUvxyz.length; loopj += 5) {
            int u = (int) (t.rasterizedUvxyz[loopj] * texRGBPixelsWidth);
            int v = (int) (t.rasterizedUvxyz[loopj + 1] * texRGBPixelsHeight);
            // Another sanity check, floating point roundoff
            // might come into play here.
            if ((u < 0)
                    || (u >= texRGBPixelsWidth)
                    || (v < 0)
                    || (v >= texRGBPixelsHeight)) {
                continue;
            }

            // set it in our texture array
            texRGBPixels[(texRGBPixelsHeight - v) * texRGBPixelsWidth + u] = (255 << 16) | (255 << 8) | 255;
        }
        TextureReader.Texture newTexture = TextureReader.createTexture(texRGBPixelsImg, true);
        mesh.setImagery(newTexture);
    }

    public Model(StateDB stateDB, TextureDB textureDB, SkyAndGround skyAndGround, DEM dem, Mesh mesh, UAVCamera uavCamera, Skybox skybox, boolean drawSanjayaUAVs) {
        this.stateDB = stateDB;
        this.textureDB = textureDB;
        this.skyAndGround = skyAndGround;
        this.dem = dem;
        this.mesh = mesh;
        this.uavCamera = uavCamera;
        this.skybox = skybox;
        this.drawSanjayaUAVs = drawSanjayaUAVs;
    }

    private void init(GL gl) {
        initFlag = false;

        uavCamera.init(gl);


        TextureInfo ti;
        ti = textureDB.getLinedColorTexture(gl, 32, Color.blue, Color.white, 4);
        humveeBluefor = RenderableVBOFactory.buildHumvee(gl, ti.textID);

        ti = textureDB.getLinedColorTexture(gl, 32, Color.red, Color.white, 4);
        humveeOpfor = RenderableVBOFactory.buildHumvee(gl, ti.textID);

        ti = textureDB.getLinedColorTexture(gl, 32, Color.black, Color.white, 4);
        rSelectionPressed = RenderableVBOFactory.buildSphere(gl, ti.textID, 2);

        ti = textureDB.getLinedColorTexture(gl, 32, Color.white, Color.black, 4);
        rSelectionReleased = RenderableVBOFactory.buildSphere(gl, ti.textID, 2);

        ti = textureDB.getLinedColorTexture(gl, 32, Color.white, Color.black, 4);
        sphereBluefor = new Marker(gl, ti, "Model.sphere.bluefor");

        ti = textureDB.getLinedColorTexture(gl, 32, Color.red, Color.black, 4);
        sphereOpfor = new Marker(gl, ti, "Model.sphere.opfor");

        ti = textureDB.getLinedColorTexture(gl, 32, Color.green, Color.black, 4);
        sphereNeutral = new Marker(gl, ti, "Model.sphere.neutral");

        ti = textureDB.getLinedColorTexture(gl, 32, Color.black, Color.black, 4);
        sphereBlack = new Marker(gl, ti, "Model.sphere.black");

        ti = textureDB.getLinedColorTexture(gl, 32, Color.blue, Color.black, 4);
        sphereBlue = new Marker(gl, ti, "Model.sphere.blue");

        ti = textureDB.getLinedColorTexture(gl, 32, Color.green, Color.black, 4);
        sphereGreen = new Marker(gl, ti, "Model.sphere.green");

        ti = textureDB.getLinedColorTexture(gl, 32, Color.red, Color.black, 4);
        sphereRed = new Marker(gl, ti, "Model.sphere.red");

        ti = textureDB.getLinedColorTexture(gl, 32, Color.white, Color.black, 4);
        sphereWhite = new Marker(gl, ti, "Model.sphere.white");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(32, 32, 32), Color.black, 4);
        sphereColor0 = new Marker(gl, ti, "Model.sphere.color0");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(32, 32, 96), Color.black, 4);
        sphereColor1 = new Marker(gl, ti, "Model.sphere.color1");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(32, 96, 32), Color.black, 4);
        sphereColor2 = new Marker(gl, ti, "Model.sphere.color2");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(32, 96, 96), Color.black, 4);
        sphereColor3 = new Marker(gl, ti, "Model.sphere.color3");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(96, 32, 32), Color.black, 4);
        sphereColor4 = new Marker(gl, ti, "Model.sphere.color4");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(96, 32, 96), Color.black, 4);
        sphereColor5 = new Marker(gl, ti, "Model.sphere.color5");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(96, 96, 32), Color.black, 4);
        sphereColor6 = new Marker(gl, ti, "Model.sphere.color6");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(96, 96, 96), Color.black, 4);
        sphereColor7 = new Marker(gl, ti, "Model.sphere.color7");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(160, 160, 160), Color.black, 4);
        sphereColor8 = new Marker(gl, ti, "Model.sphere.color8");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(160, 160, 224), Color.black, 4);
        sphereColor9 = new Marker(gl, ti, "Model.sphere.color9");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(160, 224, 160), Color.black, 4);
        sphereColor10 = new Marker(gl, ti, "Model.sphere.color10");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(160, 224, 224), Color.black, 4);
        sphereColor11 = new Marker(gl, ti, "Model.sphere.color11");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(224, 160, 160), Color.black, 4);
        sphereColor12 = new Marker(gl, ti, "Model.sphere.color12");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(224, 160, 224), Color.black, 4);
        sphereColor13 = new Marker(gl, ti, "Model.sphere.color13");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(224, 224, 160), Color.black, 4);
        sphereColor14 = new Marker(gl, ti, "Model.sphere.color14");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(224, 224, 224), Color.black, 4);
        sphereColor15 = new Marker(gl, ti, "Model.sphere.color15");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(32, 32, 160), Color.black, 4);
        sphereColor16 = new Marker(gl, ti, "Model.sphere.color16");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(32, 160, 32), Color.black, 4);
        sphereColor17 = new Marker(gl, ti, "Model.sphere.color17");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(32, 160, 160), Color.black, 4);
        sphereColor18 = new Marker(gl, ti, "Model.sphere.color18");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(160, 32, 32), Color.black, 4);
        sphereColor19 = new Marker(gl, ti, "Model.sphere.color19");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(96, 96, 224), Color.black, 4);
        sphereColor20 = new Marker(gl, ti, "Model.sphere.color20");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(96, 224, 96), Color.black, 4);
        sphereColor21 = new Marker(gl, ti, "Model.sphere.color21");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(96, 224, 224), Color.black, 4);
        sphereColor22 = new Marker(gl, ti, "Model.sphere.color22");

        ti = textureDB.getLinedColorTexture(gl, 32, new Color(224, 96, 96), Color.black, 4);
        sphereColor23 = new Marker(gl, ti, "Model.sphere.color23");

        ti = textureDB.getColorTexture(gl, 32, Color.red);
        targetMarker = RenderableVBOFactory.buildTarget(gl, ti.textID, 55, 50, 20);

        ti = textureDB.getColorTexture(gl, 32, Color.yellow);
        targetMarkerHighlighted = RenderableVBOFactory.buildTarget(gl, ti.textID, 55, 50, 20);

        ti = textureDB.getColorTexture(gl, 32, Color.white);
        targetMarkerSelected = RenderableVBOFactory.buildTarget(gl, ti.textID, 55, 50, 20);

        ti = textureDB.getColorTexture(gl, 32, Color.ORANGE);
        atr = RenderableVBOFactory.buildRing(gl, ti.textID, 55, 53, 20);

        // TODO: Obviously need to replace this with something else
        // besides a 'humvee' which is really just a ball...  ok, how about a flag?  Still crappy but at least we can tell how it's rotated.
        ti = textureDB.getLinedColorTexture(gl, 32, Color.GREEN, Color.white, 4);
        uav = RenderableVBOFactory.buildFlag(gl, ti.textID);

        TextureReader.Texture flagTexture;
        int textID;

        flagTexture = RenderableVBOFactory.buildFlagColorTexture(64, 64, Color.blue, null, 8);
        textID = textureDB.addNamedTexture(gl, "blueHumveeFlag", flagTexture, false);
        humveeBlueforFlag = RenderableVBOFactory.buildFlag(gl, textID);

        flagTexture = RenderableVBOFactory.buildFlagColorTexture(64, 64, Color.red, null, 8);
        textID = textureDB.addNamedTexture(gl, "redHumveeFlag", flagTexture, false);
        humveeOpforFlag = RenderableVBOFactory.buildFlag(gl, textID);


        addRenderable("MODEL.SELECTION", rSelectionReleased, 0, 0, 0);
    }
    private int meshNotReadyCounter = 0;
    // Render or model;
    //
    // Our model consists of
    // 1) X, Y, Z axes in Red, Green, Blue, with a white ball at the center
    // 2) Sky plane
    // 3) ground plane
    // 4) box to surround everything?  Maybe
    // 5) terrain mesh

    public void render(GL gl) {
        renderCounter++;
        if (initFlag) {
            Debug.debug(1, "Model.render: init(gl);");
            init(gl);
        }

        // @TODO: Technically, to do a skybox right, it is always
        // drawn centered on the viewpoint, i.e. it moves _with_ the
        // viewpoint.  This one is just statically placed.
        skybox.render(gl);

        if (DRAW_AXIS) {
            Axis.drawBoxes(gl);
        }

        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);    // Set The Color To White

        skyAndGround.render(gl);

        if (!isMeshReady()) {
            if (mesh.readyToDraw()) {
                //Debug.debug(1, "Model.render: Initting mesh to openGL");
                //Debug.debug(1, "Model.render: mesh.init(gl);");
                mesh.init(gl);
                //Debug.debug(1, "Model.render: Done initting mesh to openGL");

                float x;
                float y;
                float z;
                TextureInfo ti;
                Renderable tiepointMarker;

                double tiepoint[] = dem.getTiepoint();
                if (null != tiepoint) {
                    x = (float) tiepoint[0];
                    y = (float) tiepoint[1];
                    z = (float) tiepoint[2];
                    ti = textureDB.getLinedColorTexture(gl, 32, Color.BLACK, Color.white, 4);
                    tiepointMarker = RenderableVBOFactory.buildSphere(gl, ti.textID, 10);
                    addRenderable("TIEPOINT MARKER", tiepointMarker, x, y, z);
                    //Debug.debug(1, "Model.render: Placing tiepointMarker at " + x + ", " + y + ", " + z);
                }

                double xAndYMinusHeight[] = dem.getXAndYMinusHeight();
                if (null != xAndYMinusHeight) {
                    x = (float) xAndYMinusHeight[0];
                    y = (float) xAndYMinusHeight[1];
                    z = (float) xAndYMinusHeight[2];
                    ti = textureDB.getLinedColorTexture(gl, 32, Color.BLACK, Color.white, 4);
                    tiepointMarker = RenderableVBOFactory.buildSphere(gl, ti.textID, 10);
                    addRenderable("TIEPOINT MARKER_Y_MINUS_HEIGHT", tiepointMarker, x, y, z);
                    //Debug.debug(1, "Model.render: Placing Y_MINUS_HEIGHT tiepointMarker at " + x + ", " + y + ", " + z);
                }


                setMeshReady(true);
            } else {
                meshNotReadyCounter++;
                if (0 == (meshNotReadyCounter % 20)) {
                    //Debug.debug(1, "Model.render: Mesh is not readyToDraw, waiting.");
                }
            }
        } else {
            mesh.render(gl);
        }

//	boolean skip = true;	if(skip) return;

        if (renderUAV) {
            uavCamera.render(gl);
        }

        checkStateUpdates(gl);

        PosRotRenderable[] renderableAry;
        synchronized (renderables) {
            renderableAry = renderables.values().toArray(new PosRotRenderable[1]);
        }
        if (null == renderableAry) {
            return;
        }
        if (renderableAry.length == 0) {
            return;
        }
        for (int loopi = 0; loopi < renderableAry.length; loopi++) {
            if (null == renderableAry[loopi]) {
                continue;
            }
            renderableAry[loopi].render(gl);
        }
    }

    private class PosRotRenderable implements Renderable {

        String key;
        PosRot pr = new PosRot();
        Renderable r;

        public PosRotRenderable(String key, Renderable renderable) {
            this.key = key;
            this.r = renderable;
        }

        public void render(GL gl) {
            if (null != r) {
                gl.glPushMatrix();
                pr.position(gl);
                r.render(gl);
                GLError.check(gl);
                gl.glPopMatrix();
            }
        }

        public void destroy(GL gl) {
            r.destroy(gl);
        }
    }

    public void addRenderable(String key, Renderable r, float xPos, float yPos, float zPos) {
        addRenderable(key, r, xPos, yPos, zPos, 0, 0, 0);
    }

    public void addRenderable(String key, Renderable r, float xPos, float yPos, float zPos, float xRot, float yRot, float zRot) {
        PosRotRenderable p = new PosRotRenderable(key, r);
        p.pr.setPos(xPos, yPos, zPos);
        p.pr.setRot(xRot, yRot, zRot);
        synchronized (renderables) {
            renderables.put(key, p);
        }
    }

    public void removeRenderable(String key) {
        synchronized (renderables) {
            renderables.remove(key);
        }
    }

    private void checkStateUpdates(GL gl) {
// 	if(!stateDB.isDirty())
// 	    return;
        boolean rebuildLines = false;
        if (renderCounter > rebuildLinesAfter) {
            rebuildLinesAfter += HAS_LINE_REBUILD_LINE_EVERY_N_FRAMES;
            rebuildLines = true;
        }
        State[] stateAry = stateDB.getStates();
        if (null == stateAry) {
            return;
        }

        for (int loopi = 0; loopi < stateAry.length; loopi++) {
            State state = stateAry[loopi];

            if (null == state) {
                continue;
            }

            String key = null;
            ForceID forceID = ForceID.UNKNOWN;
            StateType type = StateType.UNKNOWN;
            boolean hasLine = false;
            float[][] linePoints = null;
            boolean highlighted = false;
            boolean selected = false;
            boolean deleted;
            synchronized (state) {
                if (!state.isDirty()) {
                    continue;
                }
                state.setDirty(false);
                key = state.getKey();
                type = state.getType();
                forceID = state.getForceID();
                hasLine = state.hasLine();
                linePoints = state.getLinePoints();
                selected = state.isSelected();
                highlighted = state.isHighlighted();
                deleted = state.isDeleted();
            }
            if (deleted) {
                Debug.debug(1, "Model.checkStateUpdates: state " + key + " is marked as deleted, deleting from renderables.");
                stateDB.remove(key);
                synchronized (renderables) {
                    renderables.remove(key);
                }
                continue;
            }
            PosRotRenderable p;
            synchronized (renderables) {
                p = renderables.get(key);
            }
            if (null == p) {
                Renderable r = null;
                if (type == StateType.UNIT) {
                    if (forceID == ForceID.BLUEFOR) {
                        if (drawFlags) {
                            r = humveeBlueforFlag;
                        } else {
                            r = humveeBluefor;
                        }
                    } else if (forceID == ForceID.OPFOR) {
                        if (drawFlags) {
                            r = humveeOpforFlag;
                        } else {
                            r = humveeOpfor;
                        }
                    }
                } else if (type == StateType.UAV || type == StateType.SPHERE) {
                    if (!drawSanjayaUAVs) {
                        continue;
                    }
                    if (type == StateType.SPHERE) {
                        if (forceID == ForceID.BLUEFOR) {
                            r = sphereBluefor;
                        } else if (forceID == ForceID.OPFOR) {
                            r = sphereOpfor;
                        } else if (forceID == ForceID.NEUTRAL) {
                            r = sphereNeutral;
                        } else if (forceID == ForceID.BLACK) {
                            r = sphereBlack;
                        } else if (forceID == ForceID.BLUE) {
                            r = sphereBlue;
                        } else if (forceID == ForceID.GREEN) {
                            r = sphereGreen;
                        } else if (forceID == ForceID.WHITE) {
                            r = sphereWhite;
                        } else if (forceID == ForceID.RED) {
                            r = sphereRed;
                        } else if (forceID == ForceID.COLOR0) {
                            r = sphereColor0;
                        } else if (forceID == ForceID.COLOR1) {
                            r = sphereColor1;
                        } else if (forceID == ForceID.COLOR2) {
                            r = sphereColor2;
                        } else if (forceID == ForceID.COLOR3) {
                            r = sphereColor3;
                        } else if (forceID == ForceID.COLOR4) {
                            r = sphereColor4;
                        } else if (forceID == ForceID.COLOR5) {
                            r = sphereColor5;
                        } else if (forceID == ForceID.COLOR6) {
                            r = sphereColor6;
                        } else if (forceID == ForceID.COLOR7) {
                            r = sphereColor7;
                        } else if (forceID == ForceID.COLOR8) {
                            r = sphereColor8;
                        } else if (forceID == ForceID.COLOR9) {
                            r = sphereColor9;
                        } else if (forceID == ForceID.COLOR10) {
                            r = sphereColor10;
                        } else if (forceID == ForceID.COLOR11) {
                            r = sphereColor11;
                        } else if (forceID == ForceID.COLOR12) {
                            r = sphereColor12;
                        } else if (forceID == ForceID.COLOR13) {
                            r = sphereColor13;
                        } else if (forceID == ForceID.COLOR14) {
                            r = sphereColor14;
                        } else if (forceID == ForceID.COLOR15) {
                            r = sphereColor15;
                        } else if (forceID == ForceID.COLOR16) {
                            r = sphereColor16;
                        } else if (forceID == ForceID.COLOR17) {
                            r = sphereColor17;
                        } else if (forceID == ForceID.COLOR18) {
                            r = sphereColor18;
                        } else if (forceID == ForceID.COLOR19) {
                            r = sphereColor19;
                        } else if (forceID == ForceID.COLOR20) {
                            r = sphereColor20;
                        } else if (forceID == ForceID.COLOR21) {
                            r = sphereColor21;
                        } else if (forceID == ForceID.COLOR22) {
                            r = sphereColor22;
                        } else if (forceID == ForceID.COLOR23) {
                            r = sphereColor23;
                        }
                    } else {
                        r = uav;
                    }
                } else if (type == StateType.TARGET_MARKER) {
                    Debug.debug(1, "Model.checkStateUpdates: FOUND A TARGET_MARKER STATE=" + state);
                    if (selected) {
                        r = targetMarkerSelected;
                    } else if (highlighted) {
                        r = targetMarkerHighlighted;
                    } else {
                        r = targetMarker;
                    }
                } else if (type == StateType.ATR) {
                    Debug.debug(1, "Model.checkStateUpdates: FOUND A ATR STATE=" + state);
                        r = atr;
                } else if (type == StateType.LINE) {
                    // NOTE: for line, we fill this in later when we 'rebuild' the line
                    r = null;
                }
                p = new PosRotRenderable(key, r);
                synchronized (renderables) {
                    renderables.put(p.key, p);
                }
            }

            p.pr.setPos(state.getXPos(), state.getYPos(), state.getZPos());
            p.pr.setRot(state.getXRot(), state.getYRot(), state.getZRot());
            if (type == StateType.TARGET_MARKER) {
                Debug.debug(1, "Model.checkStateUpdates: FOUND A CIRCLE STATE=" + state);
                if (selected) {
                    p.r = targetMarkerSelected;
                } else if (highlighted) {
                    p.r = targetMarkerHighlighted;
                } else {
                    p.r = targetMarker;
                }
            }

            if (followingUav0 && camera != null) {
                if (type == StateType.UAV && key.endsWith("UAV0")) {
//                    Debug.debug(1, "Model.checkStateUpdate: updating uav key='" + key + "'");
                    // @TODO: HACK HACK - add 30 to xrot to point viewpoint downwrad, to match UAV camera.
                    camera.xRot = p.pr.xRot + 30;
                    camera.yRot = p.pr.yRot;
                    camera.zRot = p.pr.zRot;
                    camera.xPos = p.pr.xPos;
                    camera.yPos = p.pr.yPos;
                    camera.zPos = p.pr.zPos;
                    // raise the UAV _model_ by 5 so the user viewpoint (for followUAV0) won't be 'inside' the UAV
                    p.pr.setPos(p.pr.xPos, p.pr.yPos + 5, p.pr.zPos);
                }
            }
            if (DRAW_TRACK_LINES) {
                if (rebuildLines && hasLine) {
                    if (null == linePoints) {
                        continue;
                    }
                    // NOTE: Need at least 2 points for a line!
                    if (linePoints.length <= HAS_LINE_SKIP_EVERY + 1) {
                        continue;
                    }
                    int start = linePoints.length - (HAS_LINE_NUM_ELEMENTS * HAS_LINE_SKIP_EVERY + 1);
                    if (start < 0) {
                        start = 0;
                    }
                    ArrayList<float[]> newLine = new ArrayList<float[]>();
                    for (int loopj = start; loopj < linePoints.length; loopj += HAS_LINE_SKIP_EVERY) {
                        float[] lineEl = new float[3];
                        lineEl[0] = linePoints[loopj][0];
                        lineEl[1] = linePoints[loopj][1];
                        lineEl[2] = linePoints[loopj][2];
                        newLine.add(lineEl);
                    }
                    float[][] linePoints2 = newLine.toArray(new float[1][]);

                    Color color = Color.green;
                    if (type == StateType.UNIT) {
                        if (forceID == ForceID.BLUEFOR) {
                            color = Color.blue;
                        } else if (forceID == ForceID.OPFOR) {
                            color = Color.red;
                        }
                    } else {
                        color = Color.white;
                    }
                    Color lineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 128);
                    TextureInfo ti = textureDB.getColorTexture(gl, 32, lineColor);
                    Renderable rivbo = RenderableVBOFactory.buildInterleavedVBOLine(gl, ti.textID, linePoints2);
                    addRenderable(key + ".LINE", rivbo, 0.0f, 0.0f, 0.0f);
                }
            }
        }
    }

    public void followUav0(boolean pressed, GLCamera camera) {
        if (pressed) {
            this.camera = camera;
            followingUav0 = !followingUav0;
            Debug.debug(1, "Model.followUav0: Setting follow to " + followingUav0);
        }
    }

    public void toggleDrawUAVs() {
        drawSanjayaUAVs = !drawSanjayaUAVs;
        Debug.debug(1, "Model.toggleDrawUAVS:  toggled drawing of UAVs to " + drawSanjayaUAVs);
    }
//    private void checkRenderableQueue(GL gl) {
//	while(true) {
//	    RenderableUpdate ru = renderableQueue.poll();
//	    if(null == ru)
//		return;
//	    Renderable r = getRenderable(ru.key);
//	    if(null == r) {
//		Color color;
//		if(ru.forceID.equalsIgnoreCase("bluefor"))
//		    color = Color.blue;
//		else if(ru.forceID.equalsIgnoreCase("opfor"))
//		    color = Color.red;
//		else
//		    color = Color.green;
//
//		RenderableDisplayList dr;
//		if(ru.type.equalsIgnoreCase("HMMMV")) {
//		    Debug.debug(1,"Model.checkRenderableQueue:  Creating new HMMMV renderable.");
//		    if(drawFlags) {
//			r = RenderableDisplayList.buildFlag(gl, color);
//		    }
//		    else {
//			r = RenderableDisplayList.buildHumvee(gl, color);
//		    }
//		}
//		else if(ru.type.equalsIgnoreCase("UAV")) {
//		    Debug.debug(1,"Model.checkRenderableQueue:  Creating new UAV renderable.");
//		    r = new UAVCamera(null);
//		}
//		else {
//		    Debug.debug(1,"Model.checkRenderableQueue:  Unknown type="+ru.type+", ignoring it.");
//		    continue;
//		}
//		addRenderable(ru.key, r);
//	    }
//	    if(r instanceof RenderableDisplayList) {
//		float newAlt = demAndTexture.getAltitude(ru.xPos,ru.yPos);
//		//		Debug.debug(1,"Model.checkRenderableQueue: Updating altitude, original alt="+ru.zPos+" new alt="+newAlt+" difference = "+(newAlt - ru.zPos));
//		ru.zPos = newAlt;
//		RenderableDisplayList dr = (RenderableDisplayList)r;
//		// NOTE the swapping of y and z.  OpenGL expects Y to refer to the up/down axis.
//		dr.setPos(ru.xPos,(float)(ru.zPos+RenderableDisplayList.HMMWV_SPHERE_RADIUS),ru.yPos);
//		dr.setRot(ru.xRot,ru.yRot,ru.zRot);
//	    }
//	    else if(r instanceof UAVCamera) {
//		UAVCamera uc = (UAVCamera)r;
//		// NOTE the swapping of y and z.  OpenGL expects Y to refer to the up/down axis.
//		uc.setPosition(ru.xPos,ru.zPos,ru.yPos,ru.xRot,ru.yRot,ru.zRot);
//		// uc.setPosition(474, 405, 705,ru.xRot,ru.yRot,ru.zRot);
//	    }
//	}
//    }
}
