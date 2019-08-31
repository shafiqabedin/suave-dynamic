package suave;

// THis class is a georeferenced texture - i.e. it has a Texture instance and some information
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.imageio.ImageIO;

// regarding where it is in the real world, along with some methods to use that information...
public class GeoTexture implements GeoTransformsConstants {

//    private final static boolean USE_DEM_AS_TEXTURE = false;
    // These are duplicated here and in DemAndTexture.  Really they should maybe be defined in GeoTransforms.
    private String imageryFilename = null;
    private Origin origin;
    private double northLat;
    private double southLat;
    private double westLon;
    private double eastLon;
    private WorldFile worldFile;
    transient private TextureReader.Texture imagery = null;
    private static double imageryWidth;
    private static double imageryHeight;
    private static double localWestX = -1;
    private static double localEastX = -1;
    private static double localNorthZ = -1;
    private static double localSouthZ = -1;
    private static double localWidth = -1;
    private static double localHeight = -1;
    private static double localXLower = -1;
    private static double localZLower = -1;

    // @TODO: replacing this with the code that uses the WorldFile and Origin - delete this later.
    // These methods are only called from Mesh and I'll be changing that right now to call the
    // methods that use WorldFile.
    //
    // @TODO: THE OGL to texture functions below look.... suspicious to me.  Review carefully!
    // 
    // GeoTexture.loadTexture: Loaded imagery f:\laptop\owens\generictextures\earth_texture_3131.jpg size 512,512 with
    // left/right/top/bottom -584.0160154473102, -584.0160154473102, 1057.0952633437637, -570.5889351524386
    // width 1454.4573307519886 height 1627.6841984962023 lowerx -584.0160154473102 lowery -570.5889351524386
    // @TODO: I'm interpolating between left and right, or top and
    // bottom, in one dimension.  Really I should interpolate in 2D as
    // we're apparently not exactly perfectly axis aligned.  Which really I
    // guess means I need to have a separate lat/lon pair for each
    // corner.  Or something.
    //
    // @deprecated
    public float oglVertexXToTextureU(float vertexX) {
        return (float) ((vertexX - localXLower) / localWidth);
    }

    // @deprecated
    public float oglVertexZToTextureV(float vertexZ) {
        return (float) (1.0 - ((vertexZ - localZLower) / localHeight));
    }
    // nathan's new values 2011/01/17
    double texNorthLat = 51.250;
    double texSouthLat = 51.207;
    double texEastLon = -2.111;
    double texWestLon = -2.180;
    private final static DecimalFormat fmt = new DecimalFormat("0.000000");

    // NEW vertex to texture using world file affine transform
    public void oglVertexToTextureUV(float[] ogl, float[] uv) {
        double[] oglD = {ogl[OGL_X], ogl[OGL_Y], ogl[OGL_Z]};
        double[] lvcs = new double[3];
        double[] lla = new double[3];
        double[] pixel = new double[2];
        origin.openGLToLvcs(oglD, lvcs);
        origin.lvcsToGpsDegrees(lvcs, lla);
        worldFile.toPixel(lla, pixel);
        uv[TEXT_U] = (float) (pixel[PIXEL_X] / imageryWidth);
        uv[TEXT_V] = (float) (pixel[PIXEL_Y] / imageryHeight);
//        Debug.debug(1, "GeoTexture.oglVertexToTextureUV: "
//                //+ " ogl=" + fmt.format(ogl[0]) + ", " + fmt.format(ogl[1]) + ", " + fmt.format(ogl[2])
//                + " lvcs=" + fmt.format(lvcs[0]) + ", " + fmt.format(lvcs[1]) + ", " + fmt.format(lvcs[2])
//                + " latlon=" + fmt.format(lla[0]) + ", " + fmt.format(lla[1])
//                + " diff=" + fmt.format(lla[0]- texSouthLat) + ", " + fmt.format(lla[1]-texWestLon)
//                + " pixel=" + fmt.format(pixel[0]) + ", " + fmt.format(pixel[1])
//                + " uv=" + fmt.format(uv[0]) + ", " + fmt.format(uv[1]));
    }

    public GeoTexture(String imageryFilename, TextureReader.Texture imagery, Origin origin, double northLat, double southLat, double eastLon, double westLon, WorldFile worldFile) {
        this.imageryFilename = imageryFilename;
        this.imagery = imagery;
        this.origin = origin;
        this.northLat = northLat;
        this.southLat = southLat;
        this.westLon = westLon;
        this.eastLon = eastLon;
        this.worldFile = worldFile;
        calculateBoundaries();
        imageryWidth = imagery.getWidth();
        imageryHeight = imagery.getHeight();
        Debug.debug(1, "GeoTexture: Loaded imagery " + imageryFilename + " size " + imagery.getWidth() + "," + imagery.getHeight() + " with left " + localWestX + " right " + localEastX + " top " + localNorthZ + " bottom " + localSouthZ + " width " + localWidth + " height " + localHeight + " lowerx " + localXLower + " lowerz " + localZLower);
    }

    public GeoTexture(TextureReader.Texture imagery, Origin origin, double northLat, double southLat, double eastLon, double westLon) {
        this.imagery = imagery;
        this.origin = origin;
        this.northLat = northLat;
        this.southLat = southLat;
        this.westLon = westLon;
        this.eastLon = eastLon;
        calculateBoundaries();
        imageryWidth = imagery.getWidth();
        imageryHeight = imagery.getHeight();
        Debug.debug(1, "GeoTexture: Using line texture size " + imagery.getWidth() + "," + imagery.getHeight() + " with left " + localWestX + " right " + localEastX + " top " + localNorthZ + " bottom " + localSouthZ + " width " + localWidth + " height " + localHeight + " lowerx " + localXLower + " lowerz " + localZLower);
    }

    private void calculateBoundaries() {
        // @TODO: We compute the corners/sides of the texture imagery
        // using hardcoded TEXTURE_ constants declared above - these
        // really should come from a config file or something since
        // obviously these will ONLY work with a specific file for
        // gascola.
        double xyz[] = new double[3];
        origin.gpsDegreesToLvcs(northLat, westLon, origin.getAlt(), xyz);
        Origin.lvcsToOpenGL(xyz, xyz);
        localWestX = xyz[OGL_X];
        localNorthZ = xyz[OGL_Z];
        origin.gpsDegreesToLvcs(southLat, eastLon, origin.getAlt(), xyz);
        Origin.lvcsToOpenGL(xyz, xyz);
        localEastX = xyz[OGL_X];
        localSouthZ = xyz[OGL_Z];

//        origin.gpsDegreesToLvcs(51.252342224121094, -2.109255790710449, origin.getAlt(), xyz);
//        Origin.lvcsToOpenGL(xyz, xyz);
//        localWestX = xyz[OGL_X];
//        localNorthZ = xyz[OGL_Z];
//        origin.gpsDegreesToLvcs(51.207466125488281, -2.181341171264648, origin.getAlt(), xyz);
//        Origin.lvcsToOpenGL(xyz, xyz);
//        localEastX = xyz[OGL_X];
//        localSouthZ = xyz[OGL_Z];

        if (localWestX < localEastX) {
            localXLower = localWestX;
            localWidth = localEastX - localWestX;
        } else {
            localXLower = localEastX;
            localWidth = localWestX - localEastX;
        }
        if (localNorthZ < localSouthZ) {
            localZLower = localNorthZ;
            localHeight = localSouthZ - localNorthZ;
        } else {
            localZLower = localSouthZ;
            localHeight = localNorthZ - localSouthZ;
        }
    }

    public int getImageryPixelWidth() {
        return imagery.getWidth();
    }

    public int getImageryPixelHeight() {
        return imagery.getHeight();
    }

    public ByteBuffer getImageryPixels() {
        return imagery.getPixels();
    }

    public TextureReader.Texture getImagery() {
        return imagery;
    }

    public void setImagery(TextureReader.Texture texture) {
        Debug.debug(1, "GeoTexture.setImagery: Setting imagery to new value, texture=" + texture);
        if ((texture.getPixels().limit() - texture.getPixels().position()) < (1024 * 1024 * 4)) {
            throw new RuntimeException("new texture has bad position/limit, position should be 0, limit should be (1024*1024*4 == 4194304)");
        }
        imagery = texture;
    }

    public void snapshot() {
        long timeStart = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
        String filename = "terrain_texture_snapshot_"+sdf.format(new Date()) +".jpg";
        try {
            File f = new File(filename);
            BufferedImage img = imagery.getImageFromTexture();
            ImageIO.write(img, "jpg", f);
            Debug.debug(1,"GeoTexture.snapshot: Snapshot of terrain texture saved to "+filename);
        } catch (IOException e) {
            Debug.debug(3,"GeoTexture.snapshot: Exception trying to save terrain texture as jpeg snapshot to file name "+filename);
            Debug.debug(3,"GeoTexture.snapshot: Exception = "+e);
            e.printStackTrace();
            Debug.debug(3,"GeoTexture.snapshot: Ignoring exception and continuing.");
        }
        long timeEnd = System.currentTimeMillis();
        Debug.debug(1, "GeoTexture.snapshot: elapsed time to save snapshot="+(timeEnd - timeStart));
    }
}
