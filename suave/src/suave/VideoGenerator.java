/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DecimalFormat;
import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author owens
 */
public class VideoGenerator implements Runnable {

    private final static int BUF_SIZE = 129 * 1024;
    private final static DecimalFormat fmt = new DecimalFormat("00000000.png");
    private Renderer renderer;
    private DEM dem;
    private Mesh mesh;
    private String logFileName;
    private String outputDir;
    private int startFileNumber;
    private Origin origin;
    private Thread myThread;

    public VideoGenerator(Renderer renderer, DEM dem, Mesh mesh, String logFileName, String outputDir, int startFileNumber, Origin origin) {
        this.renderer = renderer;
        this.dem = dem;
        this.mesh = mesh;
        this.logFileName = logFileName;
        this.outputDir = outputDir;
        this.startFileNumber = startFileNumber;
        this.origin = origin;
        myThread = new Thread(this);
    }

    public void start() {

        myThread.start();
    }

    public void run() {

        DataInputStream logStream = null;

        // Open the log file for playback
        try {
            logStream = new DataInputStream(new FileInputStream(logFileName));
        } catch (java.io.FileNotFoundException e) {
            System.err.println("ERROR: Problems opening log file=\"" + logFileName + "\": " + e);
            e.printStackTrace();
        }
        while (true) {
            if (mesh.readyToDraw()) {
                break;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
            }
        }
        // Now, start sending the packets.
        try {
            Debug.debug(1, "Sending packets...");
            byte[] buffer = new byte[BUF_SIZE];

            while (true) {
                // Note that the log format is very simple - a long
                // representing the time the packet was received, in
                // seconds since Jan 1, 1970, then an int with the
                // size of the packet, then the raw data of the packet
                // itself.
                long packetTime = logStream.readLong();
                int bufferLen = (int) logStream.readLong();
                logStream.read(buffer, 0, bufferLen);
// 		System.err.println("Found packet of len="+bufferLen+" saved at time="+packetTime);

                handlePacket(buffer, bufferLen);
            }
        } catch (IOException e) {
            System.err.println("ERROR:" + e);
        }


        System.err.println("Done sending packets.");
    }

    private void handlePacket(byte[] data, int length) {

        ByteArrayInputStream bIn = new ByteArrayInputStream(data, 0, length);
        ObjectInputStream oIn = null;

        try {
            oIn = new ObjectInputStream(bIn);
        } catch (IOException e) {
            Debug.debug(5, "Failed to create object output stream: " + e);
        }

        Msg msg = new Msg();
        try {
            msg.simTimeMs = oIn.readLong();
            msg.latitude = oIn.readDouble();
            msg.longitude = oIn.readDouble();
            msg.altitude = oIn.readDouble();
            msg.headingDegrees = oIn.readDouble();
            msg.pitchDegrees = oIn.readDouble();
            msg.rollDegrees = oIn.readDouble();
            msg.groundSpeed = oIn.readDouble();
            msg.isMounted = oIn.readInt();
            msg.isLive = oIn.readInt();
            msg.isOnGround = oIn.readInt();
            msg.assetId = oIn.readUTF();
            msg.type = oIn.readUTF();
            msg.state = oIn.readUTF();
            msg.forceId = oIn.readUTF();
            oIn.close();
            bIn.close();
        } catch (IOException e) {
            Debug.debug(5, "Failed to read msg to byte stream: " + e);
            e.printStackTrace();
        } catch (ConcurrentModificationException e2) {
            Debug.debug(5, e2 + " : " + e2.getCause());
            e2.printStackTrace();
        }

        if (!(msg.assetId.endsWith("UAV0"))) {
            return;
        }
        // Doing coordinate conversion here, mostly because if we do
        // it in Model when we're reading from State, we'll have to
        // keep re-converting the unit tracks (i.e. line showing it's
        // past path) it every time the State gets updated...  I'm
        // probably going to regret doing it this way.

        double xyz[] = new double[3];
        float xPos = 0;
        float yPos = 0;
        float zPos = 0;
        origin.gpsDegreesToLvcs(msg.latitude, msg.longitude, msg.altitude, xyz);

        if (1 == msg.isOnGround) {
            xyz[2] = dem.getAltitude((float) xyz[0], (float) xyz[1]);
        }
        //	Debug.debug(1,"SanjayaListener: "+msg.assetId+" pos="+xPos+", "+yPos+", "+zPos+" orig z="+xyz[2]);

        // NOTE: We are swapping z and y here.  UAV logs assume z
        // is up/down (altitude), whereas JOGL assumes Z is
        // into/out of the screen.  We also negate the z value
        // because OGL uses a RHS coordinate system.
        xPos = (float) xyz[0];
        yPos = (float) xyz[2];
        zPos = -(float) xyz[1];

        float yRot = -1.0f * (float) msg.headingDegrees;
        float zRot = (float) msg.rollDegrees;
        float xRot = (float) msg.pitchDegrees;

        // @TODO: HACK HACK HACK - Clamp UAV height to 80 meters...
        yPos = 340 + 80;
        PosRot pr = new PosRot();
        pr.xPos = xPos;
        pr.yPos = yPos;
        pr.zPos = zPos;
        pr.xRot = xRot;
        pr.yRot = yRot;
        pr.zRot = zRot;
        String filename = outputDir + File.separator + fmt.format(startFileNumber++);
        CaptureCommand cc = new CaptureCommand(1, pr, filename);
        Message rendererMsg = new Message(null, cc);
        renderer.addMessage(rendererMsg);



    }

    // @TODO: This is cut'n'pasted from SanjayaListener, so if we update it there we have to update it here
    // I think this class (VideoGenerator)  may go away real soon, but if it sticks around we
    // should probably factor Msg out of SanjayaListener and use it both places.
    // This is identical to the msg being sent by sanjaya over UDP in
    // AirSim.Environment.AssetInfoMulticast.  We don't really need it
    // here as a private class but this is to help us keep things
    // straight, while at the same time avoiding a dependency on the
    // Sanjaya source tree.  NOTE: Obviously we gotta make sure we
    // keep this in synch with packets sent by Sanjaya, or vice versa.
    public class Msg {

        public long simTimeMs = -1;
        public double latitude = 0;
        public double longitude = 0;
        public double altitude = 0;
        public double headingDegrees = 0;
        public double pitchDegrees = 0;
        public double rollDegrees = 0;
        public double groundSpeed = 0;
        public int isMounted = 0; //mounted:1, unmounted:0;
        public int isLive = 1; //live:1, dead:0
        public int isOnGround = 1; //on ground:1, in air :0
        public String assetId;
        public String type;
        public String state;
        public String forceId;
    }
}
