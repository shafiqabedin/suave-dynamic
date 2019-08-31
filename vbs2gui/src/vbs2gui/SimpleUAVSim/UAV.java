package vbs2gui.SimpleUAVSim;

import Machinetta.State.BeliefType.NamedProxyID;
import Machinetta.State.BeliefType.ProxyID;
import java.util.ArrayList;
import vbs2gui.server.Vbs2Link;

public class UAV {
    private boolean newDest = false;
    private final Vbs2Link link;
    private final int id;
    private final ProxyID pid;
    private double currWaypointVBSx = 0.0;
    private double currWaypointVBSy = 0.0;
    private String uavName = null;
    private String groupName = null;
    private String hostName = null;
    private String unitName = null;
    private Type type;
    private final double speed = 1.0;
    // Starting x, y, z (m in VBS2 coordinates), heading (deg clockwise from N 
    //  - VBS2 coordinates)
    // Start heading is not used because VBS2 has no reliable method that I know
    //  of for changing the heading of a moving vehicle
    public double startX, startY, startZ, startHead;
    public boolean laserFunctioning = true;
    private ArrayList<UAVListener> listeners = new ArrayList<UAVListener>();

    public enum Type {

        ANIMAL, UAV, UGV
    };

    public UAV(Vbs2Link link, int id, String uavName, String groupName,
            Type type, String hostName, String unitName) {
        this.link = link;
        this.id = id;
        this.uavName = uavName;
        this.groupName = groupName;
        this.type = type;
        this.hostName = hostName;
        this.unitName = unitName;
        pid = new NamedProxyID("UAV" + id);
    }

    public void resetPosition() {

        String command = "uav" + id + " setPos [" + startX + "," + startY + "," + startZ + "]";
        String response = link.evaluate(command);
        System.out.println("Response from UAV position reset command: " + response);

    }

    public void setCurrentCamera() {
        // @todo This assumes vehicle name is UAV
        String camName = new String(uavName);
        camName = camName.replace("uav", "cam");
        String response = link.evaluate(camName + " cameraEffect [\"External\", \"Front\"]; ");
        Machinetta.Debugger.debug(3, "Response from camera change request: " + response);
    }

    public ProxyID getProxyID() {
        return pid;
    }

    public double getSpeed() {
        return speed;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Type getType() {
        return type;
    }

    public int getUavID() {
        return id;
    }

    public String getUavName() {
        return uavName;
    }

    public void setUavName(String uavName) {
        this.uavName = uavName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public double[] getStartPosition() {
        return new double[] {startX, startY, startZ, startHead};
    }

    public void setStartPosition(double startX, double startY, double startZ, double startHead) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.startHead = startHead;
    }
}

