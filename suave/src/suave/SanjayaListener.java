package suave;

import java.util.*;
import java.io.*;
import java.net.*;

public class SanjayaListener implements Runnable, StateEnums {

    private final static int BUF_SIZE = 1024;
    private boolean printPackets = false;
    private String multicastAddr;
    private int port = 0;
    private StateDB stateDB;
    private Origin origin;
    private DEM dem;
    private InetAddress group = null;
    MulticastSocket ms = null;
    private Thread myThread;

    public SanjayaListener(String multicastAddr, int port, StateDB stateDB, Origin origin, DEM dem) {
        this.multicastAddr = multicastAddr;
        this.port = port;
        this.stateDB = stateDB;
        this.origin = origin;
        this.dem = dem;
        myThread = new Thread(this);
    }

    public void start() {
        myThread.start();
        Debug.debug(1, "SanjayaListener.start:  Starting!");
    }

    public void run() {
        Debug.debug(1, "SanjayaListener.run:  Entering!");

        try {
            group = InetAddress.getByName(multicastAddr);
            ms = new MulticastSocket(port);
            ms.joinGroup(group);

            byte[] buffer = new byte[BUF_SIZE];

            Debug.debug(1, "SanjayaListener.run: About to begin listening for multicast packets on group " + multicastAddr + " port " + port);

            while (true) {
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                ms.receive(dp);

                //		String s = new String(dp.getData());
                //		Debug.debug(1, "SanjayaListener.run: Received packet, address="+dp.getAddress()+", port="+dp.getPort()+", length="+dp.getLength()+", offset="+dp.getOffset());
                //		if(printPackets)
                //		    Debug.debug(1, "SanjayaListener.run: packet="+s);

                handlePacket(dp);
            }
        } catch (IOException e) {
            Debug.debug(5, "SanjayaListener.run: exception = " + e);
        } finally {
            if (ms != null) {
                try {
                    ms.leaveGroup(group);
                    ms.close();
                } catch (IOException e) {
                }
            }
        }
    }

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

    private void handlePacket(DatagramPacket dp) {
        byte[] data = dp.getData();
        int length = dp.getLength();

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

        // @TODO: Change to use lvcs2ogl method on origin object
        //
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

        // @TODO: Consider adding something here as a prefix, like
        // "SANJAYA", to avoid collisions with state ids generated
        // elsewhere?
        String key = msg.type + "." + msg.assetId;
        State state = stateDB.get(key);
        if (null == state) {
            StateType type = StateType.UNIT;
            if (msg.type.equals("UAV")) {
                type = StateType.UAV;
            }
            state = new State(key, type);
            stateDB.put(state);
            Debug.debug(1, "SanjayaListener: created new state=" + state);
        }
        if (msg.type.equals("UAV")) {
            // @TODO: HACK HACK HACK - Clamp UAV height to 80 meters...
            yPos = 340+80;
        }
        synchronized (state) {
            state.setPos(xPos, yPos, zPos);
            state.setRot(xRot, yRot, zRot);
            state.setHeadingDegrees((float) msg.headingDegrees);

            ForceID fid = ForceID.UNKNOWN;
            if (msg.forceId.equalsIgnoreCase("bluefor")) {
                fid = ForceID.BLUEFOR;
            } else if (msg.forceId.equalsIgnoreCase("opfor")) {
                fid = ForceID.OPFOR;
            } else if (msg.forceId.equalsIgnoreCase("Neutral")) {
                fid = ForceID.NEUTRAL;
            }
            state.setForceID(fid);

            KillStatus ks = KillStatus.UNKNOWN;
            if (msg.state.equalsIgnoreCase("Live")) {
                ks = KillStatus.LIVE;
            } else if (msg.state.equalsIgnoreCase("Destroyed")) {
                ks = KillStatus.DEAD;
            }
            state.setKillStatus(ks);
            state.setDirty(true);
//            Debug.debug(1, "SanjayaListener: updated, state=" + state);
        }
    }
}
