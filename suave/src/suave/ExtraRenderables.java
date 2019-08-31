package suave;

import java.util.*;
import java.awt.Color;
import java.awt.geom.Point2D;

import javax.media.opengl.*;

// This class is kindof a hack and should go away realllll soon.  It's
// just meant for testing the telemetry/line rendering stuff
public class ExtraRenderables implements GeoTransformsConstants {

    public static Model model;
    public static TextureDB textureDB = null;
    public static Origin origin;
    private static HashMap<String, Renderable> extraRenderables = new HashMap<String, Renderable>();
    private static boolean displayExtraRenderables = true;

    public static void toggleDisplayExtraRenderables() {
        displayExtraRenderables = !displayExtraRenderables;
        Debug.debug(1, "ExtraRenderables.toggleDisplayExtraRenderables:  toggled drawing of ExtraRenderables to " + displayExtraRenderables);
        for (String key : extraRenderables.keySet()) {
            if (displayExtraRenderables) {
                Renderable r = extraRenderables.get(key);
                model.addRenderable(key, r, 0.0f, 0.0f, 0.0f);
            } else {
                model.removeRenderable(key);
            }
        }
    }

    private static void addRenderable(String key, Renderable r) {
        extraRenderables.put(key, r);
        model.addRenderable(key, r, 0.0f, 0.0f, 0.0f);
    }

    public static void init(Model model, TextureDB textureDB, Origin origin) {
        ExtraRenderables.model = model;
        ExtraRenderables.textureDB = textureDB;
        ExtraRenderables.origin = origin;
    }
    static ArrayList<VirtualCockpitLogLine> vclogList = null;
    static int startCounter = 0;
    static int endCounter = 0;
    static int numPointsToDraw = 200;
    static boolean TEST_LIGHTING = true;

    private static void addBoundaryLine(GL gl) {
        float boundary[][] = {
            {-79.79507271018659f, 40.45147329823364f, 400},
            {-79.79490307634671f, 40.45134741269505f, 400},
            {-79.79443928563066f, 40.45132616246155f, 400},
            {-79.79372343435465f, 40.45139442475032f, 400},
            {-79.79331210935493f, 40.45145682052742f, 400},
            {-79.79301314779059f, 40.45159930354284f, 400},
            {-79.7926388152768f, 40.45182401061143f, 400},
            {-79.79222845316957f, 40.45191256512908f, 400},
            {-79.79189226162107f, 40.45205507427018f, 400},
            {-79.79151939517436f, 40.4521987624144f, 400},
            {-79.79110928027744f, 40.45231362603987f, 400},
            {-79.79073866736917f, 40.45231733302556f, 400},
            {-79.79032966821059f, 40.45237564114098f, 400},
            {-79.78995435976489f, 40.45251514968953f, 400},
            {-79.78957741236563f, 40.45268230320298f, 400},
            {-79.78924332246604f, 40.45271231619384f, 400},
            {-79.78880195262953f, 40.45271694092449f, 400},
            {-79.78846571662324f, 40.45288670672399f, 400},
            {-79.78809449661276f, 40.45305783278533f, 400},
            {-79.78775984449682f, 40.45328512849694f, 400},
            {-79.78746553810848f, 40.45342857533362f, 400},
            {-79.78709540152725f, 40.4537128261788f, 400},
            {-79.78687084857896f, 40.45391077809556f, 400},
            {-79.78649494042669f, 40.45416487004226f, 400},
            {-79.78616218750415f, 40.45430722051951f, 400},
            {-79.78593100006722f, 40.45458916144407f, 400},
            {-79.78566883998438f, 40.45478755390998f, 400},
            {-79.78523194447998f, 40.45493081865733f, 400},
            {-79.7846771555876f, 40.45534352484297f, 400},
            {-79.7844925404751f, 40.45549069407286f, 400},
            {-79.78420552093115f, 40.45573185840019f, 400},
            {-79.78369480745997f, 40.45595896872612f, 400},
            {-79.78337983422543f, 40.45608826866803f, 400},
            {-79.78291539710985f, 40.45637620503778f, 400},
            {-79.78253087918615f, 40.45668942675888f, 400},
            {-79.78232382972821f, 40.45693284826435f, 400},
            {-79.78227329171932f, 40.45722773638785f, 400},
            {-79.78219598140657f, 40.4575459769524f, 400},
            {-79.78197848491038f, 40.4578727486013f, 400},
            {-79.78171493805699f, 40.45821394813272f, 400},
            {-79.78156859706152f, 40.45841147909092f, 400},
            {-79.78130869405509f, 40.45872249007185f, 400},
            {-79.78108072463643f, 40.45907604619425f, 400},
            {-79.78077298413915f, 40.45943871392483f, 400},
            {-79.78050696427101f, 40.45969197462082f, 400},
            {-79.78013512914231f, 40.45977471554645f, 400},
            {-79.77981895985086f, 40.45992277282842f, 400},
            {-79.77960794569079f, 40.45999816902973f, 400},
            {-79.77948560611117f, 40.46033208512127f, 400},
            {-79.77944649451395f, 40.46070060644927f, 400},
            {-79.77946271792824f, 40.4609338756488f, 400},
            {-79.78001159700523f, 40.46087502755713f, 400},
            {-79.7803440917163f, 40.46084227949569f, 400},
            {-79.78078982092519f, 40.46094317063314f, 400},
            {-79.7811438036412f, 40.46116120857087f, 400},
            {-79.78146066935233f, 40.46137930574701f, 400},
            {-79.78173700282818f, 40.46154139047373f, 400},
            {-79.78208594855781f, 40.46173159931114f, 400},
            {-79.78261269038535f, 40.46203804711966f, 400},
            {-79.78277416632385f, 40.46221387587313f, 400},
            {-79.78294253725755f, 40.46256768331879f, 400},
            {-79.78294276090512f, 40.46277293652315f, 400},
            {-79.78293927185182f, 40.46281089142249f, 400},
            {-79.78291540350409f, 40.4630197653192f, 400},
            {-79.78283284156173f, 40.4632064089443f, 400},
            {-79.78275841688875f, 40.46341320557601f, 400},
            {-79.78270686678158f, 40.46354083953099f, 400},
            {-79.78273887725425f, 40.46353916484814f, 400},
            {-79.78262964201312f, 40.46383677883301f, 400},
            {-79.78259463392278f, 40.46399117292425f, 400},
            {-79.78260095259745f, 40.46415762225778f, 400},
            {-79.78278563123514f, 40.46435476156479f, 400},
            {-79.78294977898875f, 40.4644013032553f, 400},
            {-79.78304431765444f, 40.46442163394514f, 400},
            {-79.78308665423776f, 40.46442493250961f, 400},
            {-79.78318705212517f, 40.46442267443388f, 400},
            {-79.78323985617323f, 40.46443230927822f, 400},
            {-79.78354041765687f, 40.46448402586287f, 400},
            {-79.78392940870474f, 40.46452763532727f, 400},
            {-79.78436742261229f, 40.46458072439022f, 400},
            {-79.78482479256432f, 40.46470832369431f, 400},
            {-79.78515268489039f, 40.46483395622591f, 400},
            {-79.78550028369716f, 40.46502708415341f, 400},
            {-79.78579882091064f, 40.46518881107279f, 400},
            {-79.78602441419031f, 40.46535077419658f, 400},
            {-79.78625712744014f, 40.46545003781814f, 400},
            {-79.78677150884025f, 40.46566406969073f, 400},
            {-79.7871263453178f, 40.46497172126468f, 400},
            {-79.78791483357867f, 40.46471092989705f, 400},
            {-79.78855295502962f, 40.46463508850812f, 400},
            {-79.78921135060799f, 40.46492457335086f, 400},
            {-79.78938256060086f, 40.46579842764411f, 400},
            {-79.78922730480927f, 40.46641880528367f, 400},
            {-79.78939076241852f, 40.4668035246112f, 400},
            {-79.78966808469167f, 40.46701575702141f, 400},
            {-79.79017127201539f, 40.4671315117011f, 400},
            {-79.79021675699003f, 40.46666104938554f, 400},
            {-79.7906166312965f, 40.46660017829957f, 400},
            {-79.79089153164492f, 40.46663443829008f, 400},
            {-79.79102097600728f, 40.46634400967993f, 400},
            {-79.7910582579806f, 40.4659751739185f, 400},
            {-79.79102274722138f, 40.465650505311f, 400},
            {-79.79047579752489f, 40.4654006674369f, 400},
            {-79.79040004813828f, 40.46516235785362f, 400},
            {-79.79032268417925f, 40.46455012432212f, 400},
            {-79.79027190018816f, 40.46425136326558f, 400},
            {-79.79024959469425f, 40.46402001265009f, 400},
            {-79.79027408806462f, 40.46365583406232f, 400},
            {-79.79039395975782f, 40.46324449855622f, 400},
            {-79.79107351430385f, 40.46316618616461f, 400},
            {-79.79115986765412f, 40.46291523875328f, 400},
            {-79.79126421247099f, 40.46263749218841f, 400},
            {-79.79130625056168f, 40.46242558688974f, 400},
            {-79.79135567616061f, 40.46204815218059f, 400},
            {-79.7913905966449f, 40.46169066622075f, 400},
            {-79.79150200791052f, 40.46129387542639f, 400},
            {-79.79161281211874f, 40.46088250108794f, 400},
            {-79.79172586983755f, 40.46060855641396f, 400},
            {-79.79180417730248f, 40.46027905817694f, 400},
            {-79.79188098614317f, 40.46000374381893f, 400},
            {-79.79123856825761f, 40.4595337711879f, 400},
            {-79.79141285357221f, 40.45906800661936f, 400},
            {-79.79206499571498f, 40.45906828091318f, 400},
            {-79.79214814201126f, 40.45876740692689f, 400},
            {-79.79239891925164f, 40.45843886447788f, 400},
            {-79.79254538660683f, 40.45818824717896f, 400},
            {-79.79273173123882f, 40.45780708557246f, 400},
            {-79.79286262048882f, 40.45749537979836f, 400},
            {-79.79235448806887f, 40.45703909554597f, 400},
            {-79.79241385746094f, 40.45672095193918f, 400},
            {-79.79261309784269f, 40.45645923423042f, 400},
            {-79.79285576956114f, 40.45637393170848f, 400},
            {-79.79315672495952f, 40.45625832245691f, 400},
            {-79.79352358069231f, 40.45595692715145f, 400},
            {-79.79361079583241f, 40.45564265609728f, 400},
            {-79.79372202561524f, 40.45528453668f, 400},
            {-79.79380185068459f, 40.4550060842075f, 400},
            {-79.79390547182034f, 40.45473426649645f, 400},
            {-79.79399157130513f, 40.45441010752132f, 400},
            {-79.7940769472707f, 40.4540961531454f, 400},
            {-79.79415934651104f, 40.45376511377415f, 400},
            {-79.79430929716948f, 40.45339754412271f, 400},
            {-79.79459475320576f, 40.45317991911404f, 400},
            {-79.7948189836622f, 40.45307868472642f, 400},
            {-79.79498609576555f, 40.45281570789445f, 400},
            {-79.79526737179842f, 40.45248728042014f, 400},
            {-79.79567031039268f, 40.45238933519176f, 400},
            {-79.79587451535255f, 40.45232464090482f, 400},
            {-79.7956346751205f, 40.45187283255127f, 400},
            {-79.79507271018659f, 40.45147329823364f, 400}
        };

        float[][] linePoints;
        linePoints = new float[boundary.length][];
        for (int loopi = 0; loopi < boundary.length; loopi++) {
            linePoints[loopi] = new float[3];
            double xyz[] = new double[3];
            origin.gpsDegreesToLvcs(boundary[loopi][1], boundary[loopi][0], boundary[loopi][2], xyz);

            // Note that lvcs gives us x/y/z with "positive x axis
            // points east, y points north, and positive z points
            // up (parallel to gravity)".  Since OpenGL uses (as
            // you look at the screen) positive x right, positive
            // y up and positive z towards the viewer, we need to
            // swap things around a little.
            //
            // x stays the same
            // swap y and z, and then negate z
            linePoints[loopi][0] = (float) xyz[0];
            linePoints[loopi][1] = (float) xyz[2];
            linePoints[loopi][2] = -(float) xyz[1];
        }

        Color lineColor = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 128);
        TextureInfo ti = textureDB.getColorTexture(gl, 32, lineColor);

        RenderableInterleavedVBO rivbo = RenderableVBOFactory.buildInterleavedVBOLine(gl, ti.textID, linePoints);
        addRenderable("Gascola Boundary", rivbo);


    }
    private static boolean addBoundary = true;

    public static void addTelemetryFile(GL gl, String filename, Color color) {
        if (TEST_LIGHTING) {
            TEST_LIGHTING = false;
            // Some junk just to test lighting
            RenderableInterleavedVBO rivbo;

            TextureInfo ti;
            ti = textureDB.getLinedColorTexture(gl, 32, Color.red, Color.white, 4);

            TextureReader.Texture flagTexture;
            flagTexture = RenderableVBOFactory.buildFlagColorTexture(64, 64, Color.blue, null, 8);
            int flagTextID = textureDB.addNamedTexture(gl, "testblueHumveeFlag", flagTexture, false);
//
//            rivbo = RenderableVBOFactory.buildSphere(gl, ti.textID, 5);
//            model.addRenderable("HMMWV1", rivbo, 300, 200, 300);
//            rivbo = RenderableVBOFactory.buildSphere(gl, ti.textID, 10);
//            model.addRenderable("HMMWV2", rivbo, 400, 200, 300);
//            rivbo = RenderableVBOFactory.buildFlag(gl, flagTextID);
//            model.addRenderable("HMMWV3", rivbo, 500, 200, 300);
//            rivbo = RenderableVBOFactory.buildFlag(gl, flagTextID);
//            model.addRenderable("HMMWV4", rivbo, 600, 200, 300);
        }

        if (addBoundary) {
            addBoundary = false;
            addBoundaryLine(gl);
        }

        RenderableDisplayList dlr;
        float[][] linePoints;
        int size;
        int lineIndex;
        int logIndex = 0;
        VirtualCockpitLogLine logline;

        if (null == vclogList) {
            vclogList = VirtualCockpitLogLine.parseFile(filename);
        }

        endCounter = startCounter + numPointsToDraw;
        if (endCounter > (vclogList.size() - 1)) {
            endCounter = vclogList.size() - 1;
        }
        size = endCounter - startCounter;
        linePoints = new float[size][];

        logIndex = 0;
        for (int loopi = startCounter; loopi < endCounter; loopi++) {
            logline = vclogList.get(loopi);
            linePoints[logIndex] = new float[3];
            double xyz[] = new double[3];
            origin.gpsDegreesToLvcs(logline.gpsLat, logline.gpsLong, logline.gpsAlt, xyz);

            // Note that lvcs gives us x/y/z with "positive x axis
            // points east, y points north, and positive z points
            // up (parallel to gravity)".  Since OpenGL uses (as
            // you look at the screen) positive x right, positive
            // y up and positive z towards the viewer, we need to
            // swap things around a little.
            //
            // x stays the same
            // swap y and z, and then negate z
            linePoints[logIndex][0] = (float) xyz[0];
            linePoints[logIndex][1] = (float) xyz[2];
            linePoints[logIndex][2] = -(float) xyz[1];
            logIndex++;
        }

        Color lineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 128);
        TextureInfo ti = textureDB.getColorTexture(gl, 32, lineColor);

        RenderableInterleavedVBO rivbo = RenderableVBOFactory.buildInterleavedVBOLine(gl, ti.textID, linePoints);
        addRenderable(filename, rivbo);

        startCounter += 20;
        if (startCounter >= vclogList.size()) {
            startCounter = 0;
        }
    }
    private static float[][] PARKING_LOCAL = {
        {2260.31f, 566.022f, -0.00143433f},
        {2192.28f, 687.999f, -0.00141907f},
        {2073.82f, 603.553f, -0.00143433f},
        {2148.88f, 490.959f, -0.00143433f}
    };
    private static float[][] HOUSING_LOCAL = {
        {1489.92f, 2146.28f, -0.00143433f},
        {1386.31f, 2139.05f, -0.00143433f},
        {1069.98f, 1953.51f, -0.00143433f},
        {1004.93f, 1710.15f, -0.00143433f},
        {1094.08f, 1493.29f, -0.00143433f},
        {1214.56f, 1423.41f, -0.00143433f},
        {1306.12f, 1577.62f, -0.00143433f},
        {1575.99f, 1635.45f, -0.00143433f},
        {1595.27f, 1775.2f, -0.00143433f}
    };
    private static float[][] AIRPORT_LOCAL = {
        {1565.79f, 2596.68f, -0.00143433f},
        {1498.44f, 2660.34f, -0.00143433f},
        {1257.42f, 2611.44f, -0.00138855f},
        {1135.63f, 2791.35f, -0.00143433f},
        {850.842f, 2571.07f, -0.00143433f},
        {952.329f, 2410.54f, -0.00143433f},
        {1067.69f, 2448.37f, -0.00143433f},
        {1115.67f, 2421.61f, -0.00143433f},
        {1262.07f, 2457.59f, -0.00143433f},
        {1273.14f, 2441.91f, -0.00143433f},
        {1428.65f, 2506.49f, -0.00143433f}
    };
    private static float[][] FOREST2_LOCAL = {
        {3789.76f, 2889.81f, -0.00143433f},
        {3417.42f, 3085.04f, -0.00143433f},
        {3277.16f, 2997.85f, -0.00143433f},
        {3127.43f, 2645.3f, -0.00143433f},
        {3208.93f, 2578.96f, -0.00143433f},
        {3390.89f, 2588.44f, -0.00141907f},
        {3508.41f, 2696.48f, -0.00143433f},
        {3599.39f, 2700.27f, -0.00143433f}
    };
    private static float[][] FOREST1_LOCAL = {
        {3732.9f, 1496.98f, -0.00143433f},
        {3710.15f, 1902.6f, -0.00141907f},
        {3617.28f, 2319.59f, -0.00143433f},
        {3541.46f, 2315.8f, -0.00143433f},
        {3448.59f, 2179.33f, -0.00143433f},
        {3422.05f, 1927.24f, -0.00140381f},
        {3435.32f, 1529.2f, -0.00138855f},
        {3499.76f, 1394.62f, -0.00143433f},
        {3630.55f, 1404.1f, -0.00143433f}
    };
    private static float[][] PARKING_LLA = {
        {-2.1856257915f, 51.2258911133f, -0.00143433f},
        {-2.1838896275f, 51.2252693176f, -0.00141907f},
        {-2.1851172447f, 51.2242164612f, -0.00143433f},
        {-2.1867179871f, 51.2248992920f, -0.00143433f}
    };
    private static float[][] HOUSING_LLA = {
        {-2.1631231308f, 51.2188034058f, -0.00143433f},
        {-2.1632430553f, 51.2178726196f, -0.00143433f},
        {-2.1659510136f, 51.2150497437f, -0.00143433f},
        {-2.1694455147f, 51.2144889832f, -0.00143433f},
        {-2.1725351810f, 51.2153129578f, -0.00143433f},
        {-2.1735162735f, 51.2164001465f, -0.00143433f},
        {-2.1712939739f, 51.2172050476f, -0.00143433f},
        {-2.1704227924f, 51.2196273804f, -0.00143433f},
        {-2.1684186459f, 51.2197914124f, -0.00143433f}
    };
    private static float[][] AIRPORT_LLA = {
        {-2.1566615105f, 51.2194404602f, -0.00143433f},
        {-2.1557617188f, 51.2188262939f, -0.00143433f},
        {-2.1565015316f, 51.2166633606f, -0.00138855f},
        {-2.1539454460f, 51.2155494690f, -0.00143433f},
        {-2.1571459770f, 51.2130165100f, -0.00143433f},
        {-2.1594269276f, 51.2139472961f, -0.00143433f},
        {-2.1588668823f, 51.2149772644f, -0.00143433f},
        {-2.1592419147f, 51.2154121399f, -0.00143433f},
        {-2.1587035656f, 51.2167167664f, -0.00143433f},
        {-2.1589252949f, 51.2168197632f, -0.00143433f},
        {-2.1579756737f, 51.2182159424f, -0.00143433f}
    };
    private static float[][] FOREST2_LLA = {
        {-2.1520974636f, 51.2394065857f, -0.00143433f},
        {-2.1493628025f, 51.2360420227f, -0.00143433f},
        {-2.1506345272f, 51.2347869873f, -0.00143433f},
        {-2.1557087898f, 51.2334747314f, -0.00143433f},
        {-2.1566457748f, 51.2342185974f, -0.00143433f},
        {-2.1564798355f, 51.2358551025f, -0.00141907f},
        {-2.1549129486f, 51.2369003296f, -0.00143433f},
        {-2.1548442841f, 51.2377128601f, -0.00143433f}
    };
    private static float[][] FOREST1_LLA = {
        {-2.1720569134f, 51.2390403748f, -0.00143433f},
        {-2.1662504673f, 51.2387886047f, -0.00141907f},
        {-2.1602938175f, 51.2379150391f, -0.00143433f},
        {-2.1603598595f, 51.2372283936f, -0.00143433f},
        {-2.1623301506f, 51.2364082336f, -0.00143433f},
        {-2.1659448147f, 51.2362022400f, -0.00140381f},
        {-2.1716439724f, 51.2363624573f, -0.00138855f},
        {-2.1735606194f, 51.2369575500f, -0.00143433f},
        {-2.1734037399f, 51.2381248474f, -0.00143433f}
    };

    private static void addDangerAreaLocal(GL gl, String name, float[][] vbs2points) {
        float[][] linePoints;
        linePoints = new float[vbs2points.length + 1][];
        for (int loopi = 0; loopi < vbs2points.length; loopi++) {
            linePoints[loopi] = new float[3];
            // from vsb2sim
//            float xPos = (float) imgAndTelem.telem[VBS2GUI_POS_Y] - 2500;
//            float yPos = (float) (-1 * imgAndTelem.telem[VBS2GUI_POS_Z]);
//            float zPos = (float) (-1 * imgAndTelem.telem[VBS2GUI_POS_X] + 2500);

            linePoints[loopi][0] = vbs2points[loopi][1] - 2500;
            linePoints[loopi][1] = (float) (-1 * vbs2points[loopi][2]);
            linePoints[loopi][2] = (float) (-1 * vbs2points[loopi][0] + 2500);

            linePoints[loopi][1] += 300; // lift the boundary line up a tad.
        }
        linePoints[linePoints.length - 1] = new float[3];
        linePoints[linePoints.length - 1][0] = linePoints[0][0];
        linePoints[linePoints.length - 1][1] = linePoints[0][1];
        linePoints[linePoints.length - 1][2] = linePoints[0][2];

        // sadly, OpenGl doesn't really "do" transparent colors... i.e.  you have to get into writing shaders to do that
        Color lineColor = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 128);
        TextureInfo ti = textureDB.getColorTexture(gl, 32, lineColor);

        RenderableInterleavedVBO rivbo = RenderableVBOFactory.buildInterleavedVBOLine(gl, ti.textID, linePoints, 10.0f);
        addRenderable(name, rivbo);
    }

    private static void addDangerAreaLLA(GL gl, String name, float[][] LLApoints, Origin origin) {
        float[][] linePoints;
        linePoints = new float[LLApoints.length + 1][];
        for (int loopi = 0; loopi < LLApoints.length; loopi++) {
            linePoints[loopi] = new float[3];
            double lat = LLApoints[loopi][LAT_INDEX];
            double lon = LLApoints[loopi][LON_INDEX];
            double alt = LLApoints[loopi][ALT_INDEX];
            double[] lvcs = new double[3];
            origin.gpsDegreesToLvcs(lat, lon, alt, lvcs);
            double[] ogl = new double[3];
            origin.lvcsToOpenGL(lvcs, ogl);
            linePoints[loopi][OGL_X] = (float) ogl[OGL_X];
            linePoints[loopi][OGL_Y] = (float) ogl[OGL_Y];
            linePoints[loopi][OGL_Z] = (float) ogl[OGL_Z];
            linePoints[loopi][1] += 250; // lift the boundary line up a tad.
        }
        linePoints[linePoints.length - 1] = new float[3];
        linePoints[linePoints.length - 1][0] = linePoints[0][0];
        linePoints[linePoints.length - 1][1] = linePoints[0][1];
        linePoints[linePoints.length - 1][2] = linePoints[0][2];

        // sadly, OpenGl doesn't really "do" transparent colors... i.e.  you have to get into writing shaders to do that
        Color lineColor = new Color(Color.red.getRed(), Color.red.getGreen(), Color.red.getBlue(), 128);
        TextureInfo ti = textureDB.getColorTexture(gl, 32, lineColor);

        RenderableInterleavedVBO rivbo = RenderableVBOFactory.buildInterleavedVBOLine(gl, ti.textID, linePoints, 100.0f);
        addRenderable(name, rivbo);
    }

    public static void addDangerAreas(GL gl, Origin origin) {
        if (addDangerAreas) {
            boolean local = false;
            if (local) {
                addDangerAreaLocal(gl, "PARKING", PARKING_LOCAL);
                addDangerAreaLocal(gl, "HOUSING", HOUSING_LOCAL);
                addDangerAreaLocal(gl, "AIRPORT", AIRPORT_LOCAL);
                addDangerAreaLocal(gl, "FOREST2", FOREST2_LOCAL);
                addDangerAreaLocal(gl, "FOREST1", FOREST1_LOCAL);
            }
            if (!local) {
                addDangerAreaLLA(gl, "PARKING", PARKING_LLA, origin);
                addDangerAreaLLA(gl, "HOUSING", HOUSING_LLA, origin);
                addDangerAreaLLA(gl, "AIRPORT", AIRPORT_LLA, origin);
                addDangerAreaLLA(gl, "FOREST2", FOREST2_LLA, origin);
                addDangerAreaLLA(gl, "FOREST1", FOREST1_LLA, origin);
            }
        }
    }
    private static boolean addDangerAreas = true;
}

