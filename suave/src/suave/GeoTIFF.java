package suave;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.IOException;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.widget.DisplayJAI;
import javax.swing.JFrame;

import java.awt.image.SampleModel;
import java.awt.image.Raster;

import com.sun.media.jai.codec.TIFFDirectory;
import com.sun.media.jai.codec.TIFFField;

/**
 * This program decodes a GeoTIFF file.
 * It is heavily based on JAI example code.
 * @author pkv
 */
public class GeoTIFF implements java.io.Serializable {

    private final static int MODEL_PIXEL_SCALE_TAG = 33550; // ModelPixelScaleTag
    private final static int MODEL_TRANSFORMATION_TAG = 34264; // ModelTransformationTag
    private final static int MODEL_TIEPOINT_TAG = 33922; // ModelTiepointTag
    private final static int GEO_KEY_DIRECTORY_TAG = 34735; // GeoKeyDirectoryTag
    private final static int GEO_DOUBLE_PARAMS_TAG = 34736; // GeoDoubleParamsTag
    private final static int GEO_ASCII_PARAMS_TAG = 34737; // GeoAsciiParamsTag
    private final static int PROJECTED_CS_TYPE_GEO_KEY = 3072; // ProjectedCSTypeGeoKey
    private final static int GT_MODEL_TYPE_GEO_KEY = 1024;  // GTModelTypeGeoKey
    private final static int GT_MODEL_TYPE_VALUE_PROJECTED = 1;   // ModelTypeProjected - Projection Coordinate System
    private final static int GT_MODEL_TYPE_VALUE_GEOGRAPHIC = 2;   // ModelTypeGeographic - Geographic latitude-longitude System
    private final static int GT_MODEL_TYPE_VALUE_GEOCENTRIC = 3;   // ModelTypeGeocentric - Geocentric (X,Y,Z) Coordinate System
    private final static int GT_RASTER_TYPE_GEO_KEY = 1025;  // GTRasterTypeGeoKey
    private final static int GT_RASTER_TYPE_VALUE_AREA = 1;  // GTRasterTypeGeoKey
    private final static int GT_RASTER_TYPE_VALUE_POINT = 2;  // GTRasterTypeGeoKey
    private final static int GT_CITATION_GEO_KEY = 1026;  // GTCitationGeoKey
    private final static int PCS_WGS84_UTM_zone_1N = 32601;
    private final static int PCS_WGS84_UTM_zone_2N = 32602;
    private final static int PCS_WGS84_UTM_zone_3N = 32603;
    private final static int PCS_WGS84_UTM_zone_4N = 32604;
    private final static int PCS_WGS84_UTM_zone_5N = 32605;
    private final static int PCS_WGS84_UTM_zone_6N = 32606;
    private final static int PCS_WGS84_UTM_zone_7N = 32607;
    private final static int PCS_WGS84_UTM_zone_8N = 32608;
    private final static int PCS_WGS84_UTM_zone_9N = 32609;
    private final static int PCS_WGS84_UTM_zone_10N = 32610;
    private final static int PCS_WGS84_UTM_zone_11N = 32611;
    private final static int PCS_WGS84_UTM_zone_12N = 32612;
    private final static int PCS_WGS84_UTM_zone_13N = 32613;
    private final static int PCS_WGS84_UTM_zone_14N = 32614;
    private final static int PCS_WGS84_UTM_zone_15N = 32615;
    private final static int PCS_WGS84_UTM_zone_16N = 32616;
    private final static int PCS_WGS84_UTM_zone_17N = 32617;
    private final static int PCS_WGS84_UTM_zone_18N = 32618;
    private final static int PCS_WGS84_UTM_zone_19N = 32619;
    private final static int PCS_WGS84_UTM_zone_20N = 32620;
    private final static int PCS_WGS84_UTM_zone_21N = 32621;
    private final static int PCS_WGS84_UTM_zone_22N = 32622;
    private final static int PCS_WGS84_UTM_zone_23N = 32623;
    private final static int PCS_WGS84_UTM_zone_24N = 32624;
    private final static int PCS_WGS84_UTM_zone_25N = 32625;
    private final static int PCS_WGS84_UTM_zone_26N = 32626;
    private final static int PCS_WGS84_UTM_zone_27N = 32627;
    private final static int PCS_WGS84_UTM_zone_28N = 32628;
    private final static int PCS_WGS84_UTM_zone_29N = 32629;
    private final static int PCS_WGS84_UTM_zone_30N = 32630;
    private final static int PCS_WGS84_UTM_zone_31N = 32631;
    private final static int PCS_WGS84_UTM_zone_32N = 32632;
    private final static int PCS_WGS84_UTM_zone_33N = 32633;
    private final static int PCS_WGS84_UTM_zone_34N = 32634;
    private final static int PCS_WGS84_UTM_zone_35N = 32635;
    private final static int PCS_WGS84_UTM_zone_36N = 32636;
    private final static int PCS_WGS84_UTM_zone_37N = 32637;
    private final static int PCS_WGS84_UTM_zone_38N = 32638;
    private final static int PCS_WGS84_UTM_zone_39N = 32639;
    private final static int PCS_WGS84_UTM_zone_40N = 32640;
    private final static int PCS_WGS84_UTM_zone_41N = 32641;
    private final static int PCS_WGS84_UTM_zone_42N = 32642;
    private final static int PCS_WGS84_UTM_zone_43N = 32643;
    private final static int PCS_WGS84_UTM_zone_44N = 32644;
    private final static int PCS_WGS84_UTM_zone_45N = 32645;
    private final static int PCS_WGS84_UTM_zone_46N = 32646;
    private final static int PCS_WGS84_UTM_zone_47N = 32647;
    private final static int PCS_WGS84_UTM_zone_48N = 32648;
    private final static int PCS_WGS84_UTM_zone_49N = 32649;
    private final static int PCS_WGS84_UTM_zone_50N = 32650;
    private final static int PCS_WGS84_UTM_zone_51N = 32651;
    private final static int PCS_WGS84_UTM_zone_52N = 32652;
    private final static int PCS_WGS84_UTM_zone_53N = 32653;
    private final static int PCS_WGS84_UTM_zone_54N = 32654;
    private final static int PCS_WGS84_UTM_zone_55N = 32655;
    private final static int PCS_WGS84_UTM_zone_56N = 32656;
    private final static int PCS_WGS84_UTM_zone_57N = 32657;
    private final static int PCS_WGS84_UTM_zone_58N = 32658;
    private final static int PCS_WGS84_UTM_zone_59N = 32659;
    private final static int PCS_WGS84_UTM_zone_60N = 32660;
    private final static int PCS_WGS84_UTM_zone_1S = 32701;
    private final static int PCS_WGS84_UTM_zone_2S = 32702;
    private final static int PCS_WGS84_UTM_zone_3S = 32703;
    private final static int PCS_WGS84_UTM_zone_4S = 32704;
    private final static int PCS_WGS84_UTM_zone_5S = 32705;
    private final static int PCS_WGS84_UTM_zone_6S = 32706;
    private final static int PCS_WGS84_UTM_zone_7S = 32707;
    private final static int PCS_WGS84_UTM_zone_8S = 32708;
    private final static int PCS_WGS84_UTM_zone_9S = 32709;
    private final static int PCS_WGS84_UTM_zone_10S = 32710;
    private final static int PCS_WGS84_UTM_zone_11S = 32711;
    private final static int PCS_WGS84_UTM_zone_12S = 32712;
    private final static int PCS_WGS84_UTM_zone_13S = 32713;
    private final static int PCS_WGS84_UTM_zone_14S = 32714;
    private final static int PCS_WGS84_UTM_zone_15S = 32715;
    private final static int PCS_WGS84_UTM_zone_16S = 32716;
    private final static int PCS_WGS84_UTM_zone_17S = 32717;
    private final static int PCS_WGS84_UTM_zone_18S = 32718;
    private final static int PCS_WGS84_UTM_zone_19S = 32719;
    private final static int PCS_WGS84_UTM_zone_20S = 32720;
    private final static int PCS_WGS84_UTM_zone_21S = 32721;
    private final static int PCS_WGS84_UTM_zone_22S = 32722;
    private final static int PCS_WGS84_UTM_zone_23S = 32723;
    private final static int PCS_WGS84_UTM_zone_24S = 32724;
    private final static int PCS_WGS84_UTM_zone_25S = 32725;
    private final static int PCS_WGS84_UTM_zone_26S = 32726;
    private final static int PCS_WGS84_UTM_zone_27S = 32727;
    private final static int PCS_WGS84_UTM_zone_28S = 32728;
    private final static int PCS_WGS84_UTM_zone_29S = 32729;
    private final static int PCS_WGS84_UTM_zone_30S = 32730;
    private final static int PCS_WGS84_UTM_zone_31S = 32731;
    private final static int PCS_WGS84_UTM_zone_32S = 32732;
    private final static int PCS_WGS84_UTM_zone_33S = 32733;
    private final static int PCS_WGS84_UTM_zone_34S = 32734;
    private final static int PCS_WGS84_UTM_zone_35S = 32735;
    private final static int PCS_WGS84_UTM_zone_36S = 32736;
    private final static int PCS_WGS84_UTM_zone_37S = 32737;
    private final static int PCS_WGS84_UTM_zone_38S = 32738;
    private final static int PCS_WGS84_UTM_zone_39S = 32739;
    private final static int PCS_WGS84_UTM_zone_40S = 32740;
    private final static int PCS_WGS84_UTM_zone_41S = 32741;
    private final static int PCS_WGS84_UTM_zone_42S = 32742;
    private final static int PCS_WGS84_UTM_zone_43S = 32743;
    private final static int PCS_WGS84_UTM_zone_44S = 32744;
    private final static int PCS_WGS84_UTM_zone_45S = 32745;
    private final static int PCS_WGS84_UTM_zone_46S = 32746;
    private final static int PCS_WGS84_UTM_zone_47S = 32747;
    private final static int PCS_WGS84_UTM_zone_48S = 32748;
    private final static int PCS_WGS84_UTM_zone_49S = 32749;
    private final static int PCS_WGS84_UTM_zone_50S = 32750;
    private final static int PCS_WGS84_UTM_zone_51S = 32751;
    private final static int PCS_WGS84_UTM_zone_52S = 32752;
    private final static int PCS_WGS84_UTM_zone_53S = 32753;
    private final static int PCS_WGS84_UTM_zone_54S = 32754;
    private final static int PCS_WGS84_UTM_zone_55S = 32755;
    private final static int PCS_WGS84_UTM_zone_56S = 32756;
    private final static int PCS_WGS84_UTM_zone_57S = 32757;
    private final static int PCS_WGS84_UTM_zone_58S = 32758;
    private final static int PCS_WGS84_UTM_zone_59S = 32759;
    private final static int PCS_WGS84_UTM_zone_60S = 32760;

    /**
     * Get rid of exception for not using native acceleration
     */
    static {
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
    }
    private String filename = null;
    private RenderedOp image = null;
    private int pixelData[][][] = null;
    private float sampleData[][][] = null;
    private double xscale = 0;
    private double yscale = 0;
    private double tiepointXCoord = 0;
    private double tiepointYCoord = 0;
    private double tiepointZCoord = 0; // in geotiff's the alt is usually left zero
    private double tiepointPixelX = 0;
    private double tiepointPixelY = 0;
    private double tiepointPixelZ = 0;  // in geotiff's the pixel Z is usually left zero

    public double getTiepointXCoord() {
        return tiepointXCoord;
    }

    public double getTiepointYCoord() {
        return tiepointYCoord;
    }

    public double getTiepointZCoord() {
        return tiepointZCoord;
    }
    // @TODO: Adding this for debugging
    public double tieYMinus = 0.0;
    private int projectedCoordinateSpaceID = -1;
    private boolean rasterPixelIsArea = false;

    public boolean getRasterPixelIsArea() {
        return rasterPixelIsArea;
    }
    private boolean rasterPixelIsPoint = false;

    public boolean getRasterPixelIsPoint() {
        return rasterPixelIsPoint;
    }
    private int modelType = -1;

    public int getModelType() {
        return modelType;
    }
    private boolean isGDC = false; // geodetic i.e. lat/lon/alt

    public boolean isGDC() {
        return isGDC;
    }
    private boolean isGCC = false; // geocentric i.e. 3d cartesian, origin in center of earth

    public boolean isGCC() {
        return isGCC;
    }
    private boolean isUtm = false;

    public boolean isUtm() {
        return isUtm;
    }
    private int utmZoneNumber = -1;

    public int getUtmZoneNumber() {
        return utmZoneNumber;
    }
    private boolean utmNorth = false;

    public boolean isUtmNorth() {
        return utmNorth;
    }

    private void setUtmZone(int PCS_WGS84_UTM_zone) {
        isUtm = true;
        if (PCS_WGS84_UTM_zone >= PCS_WGS84_UTM_zone_1S) {
            utmNorth = false;
            utmZoneNumber = PCS_WGS84_UTM_zone - PCS_WGS84_UTM_zone_1S + 1;
        } else {
            utmNorth = true;
            utmZoneNumber = PCS_WGS84_UTM_zone - PCS_WGS84_UTM_zone_1N + 1;
        }
    }
    // @TODO: do something to prevent people from calling these before
    // they're set by generateSampleData().
    private double maxHeight = Double.MIN_VALUE;

    public double getMaxHeight() {
        return maxHeight;
    }
    private double minHeight = Double.MAX_VALUE;

    public double getMinHeight() {
        return minHeight;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public GeoTIFF(String filename) {
        this.filename = filename;
        // Create an input stream from the specified file name
        // to be used with the file decoding operator.
        FileSeekableStream stream = null;
        try {
            stream = new FileSeekableStream(filename);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        // Create an operator to decode the image file.
        image = JAI.create("stream", stream);
        System.err.println("GeoTIFF.GeoTIFF: Loaded GeoTIFF " + filename + " height, width = " + image.getHeight() + ", " + image.getWidth() + " minX (leftmost column) = " + image.getMinX() + " minY (uppermost row) = " + image.getMinY());

        // Load tags;
        loadTags();
    }

    private String tiffTypeToString(int type) {

        if (type == TIFFField.TIFF_ASCII) {
            return "TIFF_ASCII - Flag for null-terminated ASCII strings.";
        }
        if (type == TIFFField.TIFF_BYTE) {
            return "TIFF_BYTE - Flag for 8 bit unsigned integers.";
        }
        if (type == TIFFField.TIFF_DOUBLE) {
            return "TIFF_DOUBLE - Flag for 64 bit IEEE doubles.";
        }
        if (type == TIFFField.TIFF_FLOAT) {
            return "TIFF_FLOAT - Flag for 32 bit IEEE floats.";
        }
        if (type == TIFFField.TIFF_LONG) {
            return "TIFF_LONG - Flag for 32 bit unsigned integers.";
        }
        if (type == TIFFField.TIFF_RATIONAL) {
            return "TIFF_RATIONAL - Flag for pairs of 32 bit unsigned integers.";
        }
        if (type == TIFFField.TIFF_SBYTE) {
            return "TIFF_SBYTE - Flag for 8 bit signed integers.";
        }
        if (type == TIFFField.TIFF_SHORT) {
            return "TIFF_SHORT - Flag for 16 bit unsigned integers.";
        }
        if (type == TIFFField.TIFF_SLONG) {
            return "TIFF_SLONG - Flag for 32 bit signed integers.";
        }
        if (type == TIFFField.TIFF_SRATIONAL) {
            return "TIFF_SRATIONAL - Flag for pairs of 32 bit signed integers.";
        }
        if (type == TIFFField.TIFF_SSHORT) {
            return "TIFF_SSHORT - Flag for 16 bit signed integers.";
        }
        if (type == TIFFField.TIFF_UNDEFINED) {
            return "TIFF_UNDEFINED - Flag for 8 bit uninterpreted bytes.";
        }
        return "Unknown TIFF type";
    }

    private void loadTags() {
        // @TODO: Can't figure out if I should be using mark and reset
        // here or what... so I'll just read the file twice.  Hmm... I
        // was thinking this should be fixed (i.e. done 'right') later
        // but perhaps this IS the right way?
        FileSeekableStream fss = null;
        try {
            fss = new FileSeekableStream(filename);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        TIFFDirectory td = null;
        try {
            td = new TIFFDirectory(fss, 0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

        TIFFField[] fields = td.getFields();
        System.err.println("GeoTIFF.loadTags: TIFFDUMP: Dumping all (" + fields.length + ") tags;");
        for (int loopi = 0; loopi < fields.length; loopi++) {
            System.err.println("GeoTIFF.loadTags: TIFFDUMP: tag # " + fields[loopi].getTag() + " count=" + fields[loopi].getCount() + " type=" + tiffTypeToString(fields[loopi].getType()));
        }

        TIFFField modelPixelScale = td.getField(MODEL_PIXEL_SCALE_TAG); // ModelPixelScaleTag
        TIFFField modelTiepoints = td.getField(MODEL_TIEPOINT_TAG); // ModelTiepointTag

        System.err.println("GeoTIFF.loadTags: Model Pixel Scale count=" + modelPixelScale.getCount() + " type=" + tiffTypeToString(modelPixelScale.getType()));
        System.err.println("GeoTIFF.loadTags: Model Tiepoints count=" + modelTiepoints.getCount() + " type=" + tiffTypeToString(modelTiepoints.getType()));

        try {
            xscale = modelPixelScale.getAsDouble(0);
            yscale = modelPixelScale.getAsDouble(1);

            System.err.println("GeoTIFF.loadTags: xscale " + xscale + " yscale " + yscale
                    + " img height " + image.getHeight()
                    + " img width " + image.getWidth() + " ");

            double[] tiepoints = modelTiepoints.getAsDoubles();

            System.err.print("GeoTIFF.loadTags: Tiepoint count=" + modelTiepoints.getCount() + ", tiepoints=");
            for (int i = 0; i < tiepoints.length; i++) {
                System.err.print(tiepoints[i] + ", ");
            }
            System.err.println();

            // According to the GeoTIFF spec section 2.6.1;
            //
            // http://www.remotesensing.org/geotiff/spec/geotiff2.6.html
            //
            // In order to georeference a GeoTIFF (i.e. map the data
            // to the real world), a GeoTIFF file includes tags that specify;
            //
            // 1) one or more tiepoints (ModelTiepointTag)
            //
            // 2) EITHER
            //   a) xscale and yscale (ModelPixelScaleTag)
            // OR
            //   b) a transformation matrix (ModelTransformationTag)
            //

            // A tiepoint specifies a point in raster space and a
            // corresponding point in 'model' space (i.e. in the real
            // world).
            //
            // If the raster doesn't need to be rotated or sheared in
            // order to map to the real world, (i.e. up/down is
            // north/south, right/left is east/west) then all we need
            // is a modelPixelScaleTag which specifies basically how
            // far apart the pixels are in the x dimension (east/west)
            // and the y dimension (north/south).
            //
            // I'm under the impression from countless hours of
            // digging through forums and what not trying to figure
            // this out, that most of the time a GeoTIFF is
            // georeferenced with a single tie point and
            // xscale/yscale.
            //
            // A tiepoint is specified with 2, 4, or 6 numbers.
            //
            // If there are 2 numbers, we assume it's the lon/lat of
            // the 0,0 (top left) pixel.
            //
            // If there are 4 numbers, the first 2 are the x,y (inside
            // the bitmap) of the pixel (usually 0,0 i.e. top left)
            // and the second 2 are the corresponding lon/lat of the
            // pixel.
            //
            // If there are 6 numbers, the first 3 are the x,y,z
            // (inside the bitmap - z is often just set to zero and
            // from mailing list comments most software that reads
            // geotiff just ignore it) of the pixel and the second 3
            // are the corresponding lon/lat/alt.

            if (modelTiepoints.getCount() == 6) {
                tiepointPixelX = modelTiepoints.getAsDouble(0);
                tiepointPixelY = modelTiepoints.getAsDouble(1);
                tiepointPixelZ = modelTiepoints.getAsDouble(2);
                tiepointXCoord = modelTiepoints.getAsDouble(3);
                tiepointYCoord = modelTiepoints.getAsDouble(4);
                tiepointZCoord = modelTiepoints.getAsDouble(5);

            } else if (modelTiepoints.getCount() == 2) {
                tiepointPixelX = 0;
                tiepointPixelY = 0;
                tiepointPixelZ = 0;
                tiepointXCoord = modelTiepoints.getAsDouble(0);
                tiepointYCoord = modelTiepoints.getAsDouble(1);
                tiepointZCoord = 0;
            } else {
                tiepointPixelX = modelTiepoints.getAsDouble(0);
                tiepointPixelY = modelTiepoints.getAsDouble(1);
                tiepointPixelZ = 0;
                tiepointXCoord = modelTiepoints.getAsDouble(2);
                tiepointYCoord = modelTiepoints.getAsDouble(3);
                tiepointZCoord = 0;
            }

            System.err.println("GeoTIFF.loadTags: NorthWest tiepoint x,y,z = " + tiepointXCoord + ", " + tiepointYCoord + ", " + tiepointZCoord + " tied to pixel x,y,z = " + tiepointPixelX + ", " + tiepointPixelY + ", " + tiepointPixelZ);

            // The GeoKeyDirectoryTag is an array of shorts of length
            // some multiple of 4.  The first 4 shorts are a header
            // specifying KeyDirectoryVersion, KeyRevision,
            // MinorRevision, and NumberofKeys
            TIFFField geoKeyDirectoryTag = td.getField(GEO_KEY_DIRECTORY_TAG);
            int keyDirectoryVersion = geoKeyDirectoryTag.getAsInt(0);
            int keyRevision = geoKeyDirectoryTag.getAsInt(1);
            int keyMinorRevision = geoKeyDirectoryTag.getAsInt(2);
            int numberOfKeys = geoKeyDirectoryTag.getAsInt(3);

            System.err.println("GeoTIFF.loadTags: geoKeyDirectoryTag.getCount() == " + geoKeyDirectoryTag.getCount() + ", version " + keyDirectoryVersion + " revision " + keyRevision + " minor revision " + keyMinorRevision + " number of keys " + numberOfKeys);

            // The KeyEntries are 4 shorts specifying keyId,
            // tagLocation, count and valueOffset.  Some special
            // processing takes place here.  KeyID is like a tiff tag
            // id but in a separate tag space.  tagLocation is is the
            // id of the TiffTag where the value is stored, except
            // that if this is 0 then the value is stored in
            // valueOffset instead.  Count is the number of tags
            // stored in the tiff tag, and valueOffset is the index
            // (based on tifftag type, not number of bytes but number
            // of shorts or what have you) into the tifftag, i.e. if
            // tifftag has 20 ints in it, and tagLocation is 5, then
            // we start with the 5th value in that tifftag.
            //
            // for our purposes we don't need any of the keyEntries
            // that isn't a short so we're just going to ignore
            // anything with a tagLocation != 0.
            for (int loopi = 4; loopi <= (numberOfKeys * 4); loopi += 4) {
                int keyId = geoKeyDirectoryTag.getAsInt(loopi + 0);
                int tagLocation = geoKeyDirectoryTag.getAsInt(loopi + 1);
                int count = geoKeyDirectoryTag.getAsInt(loopi + 2);
                int valueOffset = geoKeyDirectoryTag.getAsInt(loopi + 3);
                int tagValue = -1;
                if (0 == tagLocation) {
                    tagValue = valueOffset;
                    System.err.println("GeoTIFF.loadTags: geoKeyDirectoryTag field " + loopi + " keyId = " + keyId + " valueOffset holds tagValue = " + tagValue);
                } else {
                    System.err.println("GeoTIFF.loadTags: ignoring geoKeyDirectoryTag field " + loopi + " keyId = " + keyId + " valueOffset holds tagLocation =" + tagLocation + " count = " + count + " value offset = " + valueOffset);
                }
                if (PROJECTED_CS_TYPE_GEO_KEY == keyId) {
                    projectedCoordinateSpaceID = tagValue;
                } else if (GT_RASTER_TYPE_GEO_KEY == keyId) {
                    if (GT_RASTER_TYPE_VALUE_AREA == tagValue) {
                        rasterPixelIsArea = true;
                    } else if (GT_RASTER_TYPE_VALUE_POINT == tagValue) {
                        rasterPixelIsPoint = true;
                    }
                } else if (GT_MODEL_TYPE_GEO_KEY == keyId) {
                    modelType = tagValue;
                }
            }
            System.err.print("GeoTIFF.loadTags: after geoKeyDirectoryTag processing, model type is ");
            if (modelType == GT_MODEL_TYPE_VALUE_PROJECTED) {
                setUtmZone(projectedCoordinateSpaceID);
                System.err.print("Projection Coordinate System UTM zone=" + utmZoneNumber + " " + (utmNorth ? "North" : "South"));
            } else if (modelType == GT_MODEL_TYPE_VALUE_GEOGRAPHIC) {
                System.err.print("Geographic latitude-longitude System");
                isGDC = true;
            } else if (modelType == GT_MODEL_TYPE_VALUE_GEOCENTRIC) {
                System.err.print("Geocentric (X,Y,Z) Coordinate System");
                isGCC = true;
            } else {
                System.err.print("ERROR: Unknown model type, not Projected, not Geodetic/Geographic, not Geocentric!");
            }
            System.err.print(", rasterPixelIsArea=" + rasterPixelIsArea + ", rasterPixelIsPoint=" + rasterPixelIsPoint);
            System.err.println(" and projected coordinate space ID = " + projectedCoordinateSpaceID);

            fss.close();
        } catch (Exception e) {
            System.err.println("GeoTIFF.loadTags: Error reading geotiff" + e);
            e.printStackTrace();

            try {
                fss.close();
            } catch (Exception e2) {
                System.err.println("GeoTIFF.loadTags: Exception closing FileSeekableStream for " + filename + ", e=" + e2);
                e2.printStackTrace();
            }

        }

    }

    private void generateSampleData() {
        int width = image.getWidth();
        int height = image.getHeight();
        SampleModel sm = image.getSampleModel();
        int nbands = sm.getNumBands();
        Raster inputRaster = image.getData();
        float[] samplesBand = null;
        samplesBand = inputRaster.getSamples(0, 0, width, height, 0, samplesBand);

        // ----------------------------------------------------------------------
        //
        // NOTE: the tiepoint specifies a real world position and
        // corresponding pixel position.  From what I can tell by
        // rendering a marker at the tiepoint position and comparing
        // against lat/lon data from google earth, pixel 0,0 specifies
        // the SOUTH west corner.  (Unlike in most java graphics 0,0
        // specifies the top left pixel.)
        //
        // ----------------------------------------------------------------------

        System.err.println("GeoTIFF.generateSampleData: width = " + width + " height = " + height + " bands=" + nbands);
        sampleData = new float[width][height][nbands];
        int offset;
        for (int loopY = 0; loopY < height; loopY++) {
            for (int loopX = 0; loopX < width; loopX++) {
                offset = loopY * width * nbands + loopX * nbands;
                for (int band = 0; band < nbands; band++) {
                    sampleData[loopX][loopY][band] = samplesBand[offset + band];
                }
                if (sampleData[loopX][loopY][0] != 0.0) {
                    if (minHeight > sampleData[loopX][loopY][0]) {
                        minHeight = sampleData[loopX][loopY][0];
                    }
                    if (maxHeight < sampleData[loopX][loopY][0]) {
                        maxHeight = sampleData[loopX][loopY][0];
                    }
                }
            }
        }
    }

    public float[][][] getSampleData() {
        if (null == sampleData) {
            generateSampleData();
        }
        return sampleData;
    }

    // This is pretty wasteful - basically we're taking a Texture
    // object that contains our elevations and turning it into a 2d
    // array of 3d points (3 floats).  Then later we can take this
    // array and sample it down or whatever, turn it into a VBO.
    public float[][][] generateHeightMapData() {
        if (null == sampleData) {
            generateSampleData();
        }

        int width = sampleData.length;
        int height = sampleData[0].length;

        System.err.println("GeoTIFF.generateHeightMapData: sampleData width=" + width + " height=" + height);
        tieYMinus = tiepointYCoord - (height * yscale);

        float points[][][] = new float[width][height][3];

        for (int loopX = 0; loopX < width; loopX++) {
            for (int loopY = 0; loopY < height; loopY++) {
                // my original code
                //   points[loopX][loopY][0] = (float)(tiepointXCoord + (loopX * xscale));
                //   points[loopX][loopY][1] = (float)(tiepointYCoord - (loopY * yscale));

                // BufferedImage has 0,0 at top left, so does
                // RenderedOp... at least it does for our geotiff
                // based on RenderedOp methods getMinX() and getMinY()
                // which return the x and y coordinates of the left
                // most column and upper most row.
                //
                // Supposedly the tie point is the upper left
                // geographic point of the data.  It corresponds to a
                // point in the "raster space" and in our case (as is
                // fairly common for geotiffs) that point is 0,0.
                //
                // OK, so, I'm thinking because it's the top left
                // corner, going right (east) in the bitmap we should
                // _add_ x, going down (south) in the bitmap we should
                // _subtract_ y.
                //
                //  		points[loopX][loopY][0] = (float)(tiepointXCoord + (loopX * xscale));
                // 		points[loopX][loopY][1] = (float)(tiepointYCoord - (loopY * yscale));
                //
                // EXCEPT, that when I draw the tie point coordinate
                // as a marker in space... it ends up being drawn at
                // the south west corner (i.e. bottom left) instead of
                // the north west corner.  Yikes.
                //
                // Ok, so let's try going with that, assume it's the
                // south west corner and add Y*yscale and see what we
                // get.
                //
                //  		points[loopX][loopY][0] = (float)(tiepointXCoord + (loopX * xscale));
                // 		points[loopX][loopY][1] = (float)(tiepointYCoord + (loopY * yscale));
                //
                // Ok, that terrain mesh looks right (I think, hard to be sure) but
                // now the entire mesh is shifted to the south.  I.e. the tiepoint is
                // now the north west corner but the rest of the mesh is south of that
                // point.  Either this file is messed up or I'm doing something else
                // wrong.
                //
                // I don't LIKE it but maybe a solution is to add "size in y pixels *
                // yscale" to each y?  Give it a shot.  Ok, tried that, adding just
                // moves it farther south.  Try subtracting.
                //
                //  		points[loopX][loopY][0] = (float)(tiepointXCoord + (loopX * xscale));
                // 		points[loopX][loopY][1] = (float)(tiepointYCoord + (loopY * yscale) - (image.getHeight() * yscale));
                //
                // Annnnnnd no, that moved it north but not quite far enough.
                //
                // OK, so, if I subtract height*yscale from the
                // tiepoint, then I end up in the proper spot for the
                // southwest corner.  But I suspect if I subtract y I
                // end up with a mesh mirrored north-south (around
                // east-west axis)

                points[loopX][loopY][0] = (float) (tiepointXCoord + (loopX * xscale));
                points[loopX][loopY][1] = (float) (tiepointYCoord - (loopY * yscale));


                // Take only band 0 - hopefully we'll never have more
                // than one band of data.  We really shouldn't since
                // this is supposed to be a digital elevation map.
                points[loopX][loopY][2] = sampleData[loopX][loopY][0];
            }
        }
        return points;
    }

    public BufferedImage getBufferedImage() {
        return image.getAsBufferedImage();
    }

    // @TODO: This method probably should be somewhere else, in a
    // utility class or something.
    public BufferedImage getBufferedImage(float width, float height) {
        float xscale = (float) (width / image.getWidth());
        float yscale = (float) (height / image.getHeight());
        // Apply transformations
        //        RenderedOp image2 = scaleImage(image, 0.5F, 0.5F);
        RenderedOp image2 = scaleImage(image, xscale, yscale);
        System.err.println("GeoTIFF.getBufferedImage: image " + filename + " scaled by (" + xscale + ", " + yscale + ") to height, width = " + image2.getHeight() + ", " + image2.getWidth());
        RenderedOp image3 = autoContrastImage(image2);
        System.err.println("GeoTIFF.getBufferedImage: " + filename + " autocontrasted, height, width = " + image3.getHeight() + ", " + image3.getWidth());
        return image3.getAsBufferedImage();
    }

    // @TODO: This method also probably should be somewhere else, in a
    // utility class or something.
    public BufferedImage getBufferedImageWithLines(float width, float height, int lineSpacing) {
        BufferedImage image4 = getBufferedImage(width, height);
        Graphics2D g = image4.createGraphics();
        for (int loopx = 0; loopx <= width; loopx += lineSpacing) {
            g.setColor(Color.black);
            g.drawLine(loopx, 0, loopx, (int) height);
            g.setColor(Color.red);
            g.drawString(loopx + ", 0", loopx, 10);
            g.drawString(loopx + ", " + ((int) height), loopx, height - 10);
        }
        for (int loopy = 0; loopy <= height; loopy += lineSpacing) {
            g.setColor(Color.black);
            g.drawLine(0, loopy, (int) width, loopy);
            g.setColor(Color.red);
            g.drawString("0, " + loopy, 10, loopy);
            g.drawString(((int) width) + ", " + loopy, height - 10, loopy);
        }
        g.dispose();
        return image4;
    }

    public void display() {

        // Apply transformations
        RenderedOp image2 = scaleImage(image, 0.5F, 0.5F);
        System.err.println("GeoTIFF.display: Image " + filename + " scaled to half, height, width = " + image2.getHeight() + ", " + image2.getWidth());
        RenderedOp image3 = autoContrastImage(image2);
        System.err.println("GeoTIFF.display: Image " + filename + " autocontrasted, height, width = " + image3.getHeight() + ", " + image3.getWidth());

        /* Attach image3 to a scrolling panel to be displayed. */
        DisplayJAI panel = new DisplayJAI(image3);

        /* Create a frame to contain the panel. */
        JFrame window = new JFrame("JAI Sample Program");
        window.add(panel);
        window.pack();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    /**
     * Takes the image and scales it in the specified directions using
     * nearest-neighbor interpolation.
     * @param im the image to be scaled.
     * @param xScale the scale factor for the x-axis.
     * @param yScale the scale factor for the y-axis.
     * @return the image after scaling.
     */
    public static RenderedOp scaleImage(RenderedOp im, float xScale, float yScale) {
        // Create a standard bilinear interpolation object to be
        // used with the "scale" operator.
        Interpolation interp = Interpolation.getInstance(Interpolation.INTERP_NEAREST);

        // Stores the required input source and parameters in a
        // ParameterBlock to be sent to the operation registry,
        // and eventually to the "scale" operator.
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(im);
        pb.add(xScale);         // x scale factor
        pb.add(yScale);         // y scale factor
        pb.add(0.0F);           // x translate
        pb.add(0.0F);           // y translate
        pb.add(interp);         // interpolation method

        // Create an operator to scale image.
        return JAI.create("scale", pb);
    }

    /**
     * Takes an image and rescales the magnitude of all the pixels such that
     * the resulting minimum values are scaled to 0.0 and the maximum values
     * are scaled to 1.0 for <i>each band<\i>.
     * @param im the image to be rescaled.
     * @return the image after rescaling.
     */
    public static RenderedOp autoContrastImage(RenderedOp im) {
        // Set up the parameter block for the source image and
        // the constants
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(im);  // The source image
        pb.add(null);          // The region of the image to scan
        pb.add(10);            // The horizontal sampling rate
        pb.add(10);            // The vertical sampling rate

        // Perform the extrema operation on the source image
        RenderedOp op = JAI.create("extrema", pb);

        // Retrieve both the maximum and minimum pixel value
        double[][] extrema = (double[][]) op.getProperty("extrema");
        double[] constants = new double[extrema[0].length];
        double[] offsets = new double[extrema[0].length];

        // Calculate new scaling offsets for each band
        for (int b = 0; b < extrema[0].length; b++) {
            constants[b] = 1.0 / (extrema[1][b] - extrema[0][b]);
            offsets[b] = (1.0 * extrema[0][b]) / (extrema[0][b] - extrema[1][b]);
        }

        // Scale the magnitude of the image
        ParameterBlock pb2 = new ParameterBlock();
        pb2.addSource(im);
        pb2.add(constants);
        pb2.add(offsets);
        return JAI.create("rescale", pb2);
    }
}

