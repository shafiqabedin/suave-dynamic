/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import geotransform.coords.Gdc_Coord_3d;
import geotransform.coords.Utm_Coord_3d;
import geotransform.ellipsoids.IN_Ellipsoid;
import geotransform.transforms.Utm_To_Gdc_Converter;
import java.io.IOException;
import suave.TextureReader.Texture;

/**
 *
 * @author owens
 */
public class DEMFactory implements GeoTransformsConstants {

    public final static float TIF_HOLE_MARKER_ALTTITUDE = 266.67f;
//    private float[][][] loadHeightMapData() throws IOException {
//        if (heightMapFilename.endsWith(".tif") || heightMapFilename.endsWith(".TIF")) {
//            loadTif();
//        } else {
//            loadNonTif();
//        }
//        int width = verticeData.length;
//        int height = verticeData[0].length;
////        Debug.debug(1, "DEMFactory.loadHeightMapData: After coordinate conversion (if required)  size " + width + "," + height + " maxHeight=" + getMaxHeight() + " minHeight=" + getMinHeight());
//        return verticeData;
//    }

    private static float[][][] subsample(float[][][] verticeData, int step) {
        int width = verticeData.length;
        int height = verticeData[0].length;
        int newWidth = width / step;
        int newHeight = height / step;
        float[][][] verticeData2 = new float[newWidth][newHeight][3];
        Debug.debug(1, "DEMFactory.subsample: verticedata size old " + width + ", " + height + " new " + newWidth + ", " + newHeight);

        for (int loopx = 0; loopx < newWidth; loopx++) {
            for (int loopy = 0; loopy < newHeight; loopy++) {
                try {
                    verticeData2[loopx][loopy][0] = verticeData[loopx * step][loopy * step][0];
                    verticeData2[loopx][loopy][1] = verticeData[loopx * step][loopy * step][1];
                    verticeData2[loopx][loopy][2] = verticeData[loopx * step][loopy * step][2];
                } catch (Exception e) {
                    Debug.debug(5, "DEMFactory.subsample: Exception subsampling, was copying from " + (loopx * step) + ", " + (loopy * step) + " to " + loopx + ", " + loopy + " e =" + e);
                    e.printStackTrace();
                }
            }
        }
        return verticeData2;
    }

    public static DEM loadZZZ(String demFilename, Origin origin) {
        DEMxyz v = new DEMxyz(demFilename);
        v.readZZZ();
        float[][][] verticeData = v.getVerticeData();
        int width = verticeData.length;
        int height = verticeData[0].length;
        for (int loopx = 0; loopx < width; loopx++) {
            for (int loopy = 0; loopy < height; loopy++) {
                float x = verticeData[loopx][loopy][0];
                float y = verticeData[loopx][loopy][2];
                float z = -verticeData[loopx][loopy][1];
                verticeData[loopx][loopy][0] = x;
                verticeData[loopx][loopy][1] = y;
                verticeData[loopx][loopy][2] = z;
            }
        }
        return new DEM(verticeData.length, verticeData[0].length, verticeData);
    }

    public static DEM loadXyz(String demFilename, Origin origin, int demGridStepSize) {
        DEMxyz v = new DEMxyz(demFilename);
        v.loadXyz();
        float[][][] verticeData = v.getVerticeData();
        int width = verticeData.length;
        int height = verticeData[0].length;
        Debug.debug(1, "DEMFactor.loadXyz: file " + demFilename + " loaded, contains " + width + " by " + height + " vertices.  Converting to OpenGL coords.");
        for (int loopx = 0; loopx < width; loopx++) {
            for (int loopy = 0; loopy < height; loopy++) {
                // .xyz format is one entry per line, each entry "north-south east-west alt-above-mean-sea-level"
                // So .xyz matches our LVCS.
                // OpenGL does NOT match our LVCS, so;
                Origin.lvcsToOpenGL(verticeData[loopx][loopy], verticeData[loopx][loopy]);
            }
        }
        if (demGridStepSize != 1) {
            verticeData = subsample(verticeData, demGridStepSize);
            Debug.debug(1, "DEMFactory.loadXyz: Subsampled verticeData by step " + demGridStepSize);
        }
        int xmax = verticeData.length - 1;
        int ymax = verticeData[0].length - 1;
        Debug.debug(1, "DEMFactor.loadXyz: corners in OpenGL are;"
                + " | 0, 0 = " + verticeData[0][0][OGL_X] + ", " + verticeData[0][0][OGL_Y] + ", " + verticeData[0][0][OGL_Z]
                + " | " + xmax + ", 0 = " + verticeData[xmax][0][OGL_X] + ", " + verticeData[xmax][0][OGL_Y] + ", " + verticeData[xmax][0][OGL_Z]
                + " | 0, " + ymax + " = " + verticeData[0][ymax][OGL_X] + ", " + verticeData[0][ymax][OGL_Y] + ", " + verticeData[0][0][OGL_Z]
                + " | " + xmax + ", " + ymax + " = " + verticeData[xmax][ymax][OGL_X] + verticeData[xmax][ymax][OGL_Y] + ", " + verticeData[xmax][0][OGL_Z]);
        Debug.debug(1, "DEMFactor.loadXyz: finished converting, creating DEM with " + verticeData.length + " by " + verticeData[0].length + " vertices.");
        return new DEM(verticeData.length, verticeData[0].length, verticeData);
    }

    public static DEM loadLla(String demFilename, Origin origin, int demGridStepSize) {
        DEMLla v = new DEMLla(demFilename);
        v.loadLla();
        float[][][] verticeData = v.getVerticeData();
        int width = verticeData.length;
        int height = verticeData[0].length;
        int xmax = verticeData.length - 1;
        int ymax = verticeData[0].length - 1;
        double avgLat = (verticeData[0][0][LAT_INDEX] + verticeData[xmax][0][LAT_INDEX] + verticeData[0][ymax][LAT_INDEX] + verticeData[xmax][ymax][LAT_INDEX]) / 4;
        double avgLon = (verticeData[0][0][LON_INDEX] + verticeData[xmax][0][LON_INDEX] + verticeData[0][ymax][LON_INDEX] + verticeData[xmax][ymax][LON_INDEX]) / 4;
        double avgAlt = (verticeData[0][0][ALT_INDEX] + verticeData[xmax][0][ALT_INDEX] + verticeData[0][ymax][ALT_INDEX] + verticeData[xmax][ymax][ALT_INDEX]) / 4;
        Debug.debug(1, "DEMFactor.loadLla: corners in lat,lon,alt are "
                + " | 0, 0 = " + verticeData[0][0][LAT_INDEX] + ", " + verticeData[0][0][LON_INDEX] + ", " + verticeData[0][0][ALT_INDEX]
                + " | "+xmax + ", 0 = " + verticeData[xmax][0][LAT_INDEX] + ", " + verticeData[xmax][0][LON_INDEX] + ", " + verticeData[xmax][0][ALT_INDEX]
                + " | 0, " + ymax + " = " + verticeData[0][ymax][LAT_INDEX] + ", " + verticeData[0][ymax][LON_INDEX] + ", " + verticeData[0][0][ALT_INDEX]
                + " | "+xmax + ", " + ymax + " = " + verticeData[xmax][ymax][LAT_INDEX] + verticeData[xmax][ymax][LON_INDEX] + ", " + verticeData[xmax][0][ALT_INDEX]);
        Debug.debug(1, "DEMFactor.loadLla: Average of the four corners in lat,lon,alt = " + avgLat + ", " + avgLon + ", " + avgAlt);
        Debug.debug(1, "DEMFactor.loadLla: file " + demFilename + " loaded, contains " + width + " by " + height + " vertices.  Converting to OpenGL coords.");
        double[] xyz = new double[3];
        double[] ogl = new double[3];
        for (int loopX = 0; loopX < width; loopX++) {
            if (0 == (loopX % 20)) {
                System.err.print("DEMFactory.loadLla: index " + loopX + ":");
            }
            for (int loopY = 0; loopY < height; loopY++) {
                origin.gpsDegreesToLvcs(verticeData[loopX][loopY][LAT_INDEX], verticeData[loopX][loopY][LON_INDEX], verticeData[loopX][loopY][ALT_INDEX], xyz);
                Origin.lvcsToOpenGL(xyz, ogl);
                verticeData[loopX][loopY][OGL_X] = (float) ogl[OGL_X];
                verticeData[loopX][loopY][OGL_Y] = (float) ogl[OGL_Y];
                verticeData[loopX][loopY][OGL_Z] = (float) ogl[OGL_Z];
                if ((0 == (loopX % 20)) && (0 == (loopY % 20))) {
                    System.err.print(verticeData[loopX][loopY][OGL_X] + "," + verticeData[loopX][loopY][OGL_Y] + "," + verticeData[loopX][loopY][OGL_Z] + " ");
                }
            }
            if (0 == (loopX % 20)) {
                System.err.println();
            }
        }
        if (demGridStepSize != 1) {
            verticeData = subsample(verticeData, demGridStepSize);
            Debug.debug(1, "DEMFactory.loadLla: Subsampled verticeData by step " + demGridStepSize);
        }
        xmax = verticeData.length - 1;
        ymax = verticeData[0].length - 1;
        Debug.debug(1, "DEMFactor.loadLla: corners in OpenGL are;"
                + " | 0, 0 = " + verticeData[0][0][OGL_X] + ", " + verticeData[0][0][OGL_Y] + ", " + verticeData[0][0][OGL_Z]
                + " | " + xmax + ", 0 = " + verticeData[xmax][0][OGL_X] + ", " + verticeData[xmax][0][OGL_Y] + ", " + verticeData[xmax][0][OGL_Z]
                + " | 0, " + ymax + " = " + verticeData[0][ymax][OGL_X] + ", " + verticeData[0][ymax][OGL_Y] + ", " + verticeData[0][0][OGL_Z]
                + " | " + xmax + ", " + ymax + " = " + verticeData[xmax][ymax][OGL_X] +", "+ verticeData[xmax][ymax][OGL_Y] + ", " + verticeData[xmax][0][OGL_Z]);
        Debug.debug(1, "DEMFactor.loadLla: finished converting, creating DEM with " + verticeData.length + " by " + verticeData[0].length + " vertices.");
        return new DEM(verticeData.length, verticeData[0].length, verticeData);
    }

    public static DEM loadTif(String heightMapFilename, int demGridStepSize, Origin origin, float tifHoleMarkerAlt, float demHoleMarkerAlt) {
        double[] tiepoint = new double[3];

        // xAndYMinusHeight is to record the position of the tiepoint so we can display it in the 3d interface, mostly for
        // debugging, i.e. make sure everything is in the right place
        double[] xAndYMinusHeight = new double[3];

        GeoTIFF geotiff;
        geotiff = new GeoTIFF(heightMapFilename);
        //	geotiff.display();     // display the geotiff

        if (!geotiff.isUtm() && !geotiff.isGDC()) {
            Debug.debug(1, "DEMFactory.loadTif: ERROR CAN NOT HANDLE GeoTIFF files unless they are in UTM or GDC coordinate systems.");
            return null;
        }
        // Generate the heightmap data we're going to use to generate the mesh
        float[][][] verticeData = geotiff.generateHeightMapData();
        Debug.debug(1, "DEMFactory.loadTif: Loaded TIF " + heightMapFilename + " size " + geotiff.getWidth() + "," + geotiff.getHeight() + " maxHeight=" + geotiff.getMaxHeight() + " minHeight=" + geotiff.getMinHeight());

        if (demGridStepSize != 1) {
            verticeData = subsample(verticeData, demGridStepSize);
            Debug.debug(1, "DEMFactory.loadTif: Subsampled verticeData by step " + demGridStepSize);
        }

        int width = verticeData.length;
        int height = verticeData[0].length;

        if (geotiff.isUtm()) {
            Debug.debug(1, "DEMFactory.loadTif: Converting heightmap data from UTM to GDC (lat/lon)");
            Gdc_Coord_3d gdc = new Gdc_Coord_3d();
            Utm_Coord_3d utm = new Utm_Coord_3d();
            Utm_To_Gdc_Converter.Init(new IN_Ellipsoid());
            utm.zone = (byte) geotiff.getUtmZoneNumber();
            // @TODO: They got the zone wrong in the data file they
            // gave us, the zone number is 13 which is way out west,
            // it should be 17 (near Pittsburgh), not 13.  Buh.
            if (utm.zone == 13) {
                Debug.debug(1, "DEMFactory.loadTif: Resetting zone number from 13 (way out west) to 17 (near pittsburgh) because the geotiff for gascola has the wrong zone number.");
                utm.zone = (byte) 17;
            }
            // Convert from UTM to GDC (Geodetic, i.e. lat/lon/alt)
            utm.hemisphere_north = geotiff.isUtmNorth();

            utm.x = geotiff.getTiepointXCoord();
            utm.y = geotiff.getTiepointYCoord();
            utm.z = geotiff.getTiepointZCoord();
            Utm_To_Gdc_Converter.Convert(utm, gdc);
            Debug.debug(1, "DEMFactory.loadTif: converting tiepoint from utm= ( " + utm.x + ", " + utm.y + ", " + utm.z + " ) to lat/lon/alt= ( " + gdc.latitude + ", " + gdc.longitude + ", " + gdc.elevation + " )");
            tiepoint[LON_INDEX] = gdc.longitude;
            tiepoint[LAT_INDEX] = gdc.latitude;
            tiepoint[ALT_INDEX] = gdc.elevation;

            utm.x = geotiff.getTiepointXCoord();
            utm.y = geotiff.tieYMinus;
            utm.z = geotiff.getTiepointZCoord();
            Utm_To_Gdc_Converter.Convert(utm, gdc);
            xAndYMinusHeight[LON_INDEX] = gdc.longitude;
            xAndYMinusHeight[LAT_INDEX] = gdc.latitude;
            xAndYMinusHeight[ALT_INDEX] = gdc.elevation;

            for (int loopX = 0; loopX < width; loopX++) {
                if (0 == (loopX % (100 / demGridStepSize))) {
                    System.err.print("DEMFactory.loadTif: GDC " + loopX + ":");
                }
                for (int loopY = 0; loopY < height; loopY++) {
                    utm.x = verticeData[loopX][loopY][UTM_X];
                    utm.y = verticeData[loopX][loopY][UTM_Y];
                    utm.z = verticeData[loopX][loopY][UTM_Z];
                    Utm_To_Gdc_Converter.Convert(utm, gdc);
                    verticeData[loopX][loopY][LON_INDEX] = (float) gdc.longitude;
                    verticeData[loopX][loopY][LAT_INDEX] = (float) gdc.latitude;
                    verticeData[loopX][loopY][ALT_INDEX] = (float) gdc.elevation;
                    if ((0 == (loopX % (100 / demGridStepSize))) && (0 == (loopY % (100 / demGridStepSize)))) {
                        System.err.print(verticeData[loopX][loopY][LON_INDEX] + "," + verticeData[loopX][loopY][LAT_INDEX] + "," + verticeData[loopX][loopY][ALT_INDEX] + " ");
                    }
                }
                if (0 == (loopX % (100 / demGridStepSize))) {
                    System.err.println();
                }
            }
        } else if (geotiff.isGDC()) {
            Debug.debug(1, "DEMFactory.loadTif: GeoTIFF format is GDC, no need to convert to GDC, getting tiepoint coords.");
            tiepoint[LON_INDEX] = geotiff.getTiepointXCoord();
            tiepoint[LAT_INDEX] = geotiff.getTiepointYCoord();
            tiepoint[ALT_INDEX] = geotiff.getTiepointZCoord();
        }

        // printOutlineForKML();
        Debug.debug(1, "DEMFactory.loadTif: Converting heightmap data from GDC (lat/lon) to local coordinate system centered at lat/lon " + origin.getLat() + " " + origin.getLon());
        double xyz[] = new double[3];
        origin.gpsDegreesToLvcs(tiepoint[1], tiepoint[0], tiepoint[2], xyz);

        Debug.debug(1, "DEMFactory.loadTif: converting tiepoint from lat/lon/alt= ( "
                + tiepoint[LON_INDEX] + ", "
                + tiepoint[LAT_INDEX] + ", "
                + tiepoint[ALT_INDEX] + " ) "
                + " to lvcs= ( "
                + xyz[0] + ", "
                + xyz[2] + ", "
                + (-xyz[1]) + " )");

        tiepoint[OGL_X] = xyz[LVCS_X];
        tiepoint[OGL_Y] = xyz[LVCS_Z];
        tiepoint[OGL_Z] = -xyz[LVCS_Y];

        origin.gpsDegreesToLvcs(xAndYMinusHeight[LAT_INDEX], xAndYMinusHeight[LON_INDEX], xAndYMinusHeight[ALT_INDEX], xyz);

        xAndYMinusHeight[OGL_X] = xyz[LVCS_X];
        xAndYMinusHeight[OGL_Y] = xyz[LVCS_Z];
        xAndYMinusHeight[OGL_Z] = -xyz[LVCS_Y];

        for (int loopX = 0; loopX < width; loopX++) {
            if (0 == (loopX % (100 / demGridStepSize))) {
                System.err.print("DEMFactory.loadTif: LVCS " + loopX + ":");
            }
            for (int loopY = 0; loopY < height; loopY++) {
                // Notice we store lon/lat/alt i.e. x/y/z -
                // gps_to_lvcs wants lat/lon not lon/lat so we reverse
                // them here.
                origin.gpsDegreesToLvcs(verticeData[loopX][loopY][LAT_INDEX], verticeData[loopX][loopY][LON_INDEX], verticeData[loopX][loopY][ALT_INDEX], xyz);

                // Note that lvcs gives us x/y/z with "positive x axis
                // points east, y points north, and positive z points
                // up (parallel to gravity)".  Since OpenGL uses (as
                // you look at the screen) positive x right, positive
                // y up and positive z towards the viewer, we need to
                // swap things around a little.
                //
                // x stays the same
                // swap y and z, and then negate z
                verticeData[loopX][loopY][OGL_X] = (float) xyz[LVCS_X];
                verticeData[loopX][loopY][OGL_Y] = (float) xyz[LVCS_Z];
                verticeData[loopX][loopY][OGL_Z] = -(float) xyz[LVCS_Y];

                // if original alt is tifHoleMarkerAlt mark it with demHoleMarkerAlt
                if ((verticeData[loopX][loopY][OGL_Y] > (tifHoleMarkerAlt - .001))
                        && (verticeData[loopX][loopY][OGL_Y] < (tifHoleMarkerAlt + .001))) {
                    verticeData[loopX][loopY][OGL_Y] = DEM.HOLE_MARKER_ALTITUDE;
                }

                if ((0 == (loopX % (100 / demGridStepSize))) && (0 == (loopY % (100 / demGridStepSize)))) {
                    System.err.print(verticeData[loopX][loopY][OGL_X] + "," + verticeData[loopX][loopY][OGL_Y] + "," + verticeData[loopX][loopY][OGL_Z] + " ");
                }
            }
            if (0 == (loopX % (100 / demGridStepSize))) {
                System.err.println();
            }
        }

        printCorners(verticeData);
        DEM dem = new DEM(width, height, verticeData);
        dem.setTiepoint(tiepoint, xAndYMinusHeight);
        return dem;
    }

    private void printOutlineForKML(float[][][] verticeData) {
        int width = verticeData.length;
        int height = verticeData[0].length;

        // Print out the outside borders in a format suitable for a kml (google earth) file
        Debug.debug(1, "DEMFactory.printOutlineForKML: Printing outline of DEM in format suitable for kml (google earth");

        for (int loopX = 0; loopX
                < width; loopX++) {
            System.err.print(verticeData[loopX][0][LON_INDEX] + "," + verticeData[loopX][0][LAT_INDEX] + "," + verticeData[loopX][0][ALT_INDEX] + " ");
        }
        for (int loopY = 0; loopY
                < height; loopY++) {
            System.err.print(verticeData[width - 1][loopY][LON_INDEX] + "," + verticeData[width - 1][loopY][LAT_INDEX] + "," + verticeData[width - 1][loopY][ALT_INDEX] + " ");
        }
        for (int loopX = width - 1; loopX
                >= 0; loopX--) {
            System.err.print(verticeData[loopX][height - 1][LON_INDEX] + "," + verticeData[loopX][height - 1][LAT_INDEX] + "," + verticeData[loopX][height - 1][ALT_INDEX] + " ");
        }
        for (int loopY = height - 1; loopY
                >= 0; loopY--) {
            System.err.print(verticeData[0][loopY][LON_INDEX] + "," + verticeData[0][loopY][LAT_INDEX] + "," + verticeData[0][loopY][ALT_INDEX] + " ");
        }
        System.err.println();
        Debug.debug(1, "DEMFactory.printOutlineForKML: Done printing outline of DEM in format suitable for kml (google earth");
    }

    private static void printCorners(float[][][] verticeData) {
        int width = verticeData.length;
        int height = verticeData[0].length;

        Debug.debug(1, "DEMFactory.printCorners: Printing corners.");
        Debug.debug(1, "DEMFactory.printCorners: left top " + verticeData[0][0][OGL_X] + "," + verticeData[0][0][OGL_Y] + "," + verticeData[0][0][OGL_Z] + " ");
        Debug.debug(1, "DEMFactory.printCorners: right top " + verticeData[width - 1][0][OGL_X] + "," + verticeData[width - 1][0][OGL_Y] + "," + verticeData[width - 1][0][OGL_Z] + " ");
        Debug.debug(1, "DEMFactory.printCorners: left bot " + verticeData[0][height - 1][OGL_X] + "," + verticeData[0][height - 1][OGL_Y] + "," + verticeData[0][height - 1][OGL_Z] + " ");
        Debug.debug(1, "DEMFactory.printCorners: right bot " + verticeData[width - 1][height - 1][OGL_X] + "," + verticeData[width - 1][height - 1][OGL_Y] + "," + verticeData[width - 1][height - 1][OGL_Z] + " ");
        Debug.debug(1, "DEMFactory.printCorners: Done printing corners.");
    }

    // ----------------------------------------------------------------------
    //
    // Non tif heightmap generation from this point on
    //
    // ----------------------------------------------------------------------
    public DEM loadNonTif(String heightMapFilename) throws IOException {
        // To load a bmp/jpeg as a texture
        Texture heightMap = TextureReader.readTexture(heightMapFilename, true, false, 0);
        Debug.debug(1, "DEMFactory.loadNonTif: Loaded DEM " + heightMapFilename + " size " + heightMap.getWidth() + "," + heightMap.getHeight());
        return generateHeightMapDataFromTexture(heightMap);
    } // This is pretty wasteful - basically we're taking a Texture
    // object that contains our elevations and turning it into a 2d
    // array array of 3d points (3 floats).  Then later we can take
    // this array and sample it or whatever, turn it into a VBO.

    private static DEM generateHeightMapDataFromTexture(TextureReader.Texture heightMap) {
        int width = heightMap.getWidth();
        int height = heightMap.getHeight();
        float points[][][] = new float[width][height][3];
        for (int loopX = 0; loopX
                < height; loopX++) {
            for (int loopY = 0; loopY
                    < width; loopY++) {
                points[loopX][loopY][0] = loopX;
                points[loopX][loopY][1] = loopY;

                // pointHeight uses luminance algorithm to convert
                // from RGB to luminance, uses luminance as height.
                // NOTE: When we switch to geotiff we won't be doing this.
                points[loopX][loopY][2] = pointHeight(heightMap, loopX, loopY);
            }
        }
        return new DEM(width, height, points);
    }

    private static float pointHeight(TextureReader.Texture texture, int nX, int nY) {
        // Calculate The Position In The Texture, Careful Not To Overflow
        int nPos = ((nX % texture.getWidth()) + ((nY % texture.getHeight()) * texture.getWidth())) * 3;
        float flR = unsignedByteToInt(texture.getPixels().get(nPos));   // Get The Red Component
        float flG = unsignedByteToInt(texture.getPixels().get(nPos + 1));  // Get The Green Component
        float flB = unsignedByteToInt(texture.getPixels().get(nPos + 2));  // Get The Blue Component
        return (0.299f * flR + 0.587f * flG + 0.114f * flB);    // Calculate The Height Using The Luminance Algorithm
    }

    private static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }
}
