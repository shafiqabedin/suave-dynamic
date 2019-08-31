package suave;

import java.util.*;

// this class is ONLY supposed to hold state info about things that
// we'll be displaying - not any info about how to display them.  This
// gets a little tricky sometimes.  For instance, we don't have
// anything in here about colors, but we do have force ids.  Or
// somethign trickier... if we're trying to mark things as clickable
// or not, which is more of a UI decision... should it be in here or
// not?  Not.  We're going to need something similar to State to hold
// things like a reference to the OpenGL model, triangles for click
// intersection, and flags regarding things like clickable.
public class State implements StateEnums {

    private String key = null;

    public String getKey() {
        return key;
    }
    //    public void setKey(String key) { this.key = key;}
    private boolean highlighted = false;

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        setDirty(true);
    }
    private boolean selected = false;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        setDirty(true);
    }
    private boolean deleted = false;

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean equals(Object anObject) {
        return anObject.equals(key);
    }

    public int hashCode() {
        return key.hashCode();
    }
    private boolean dirty = false;

    public synchronized boolean isDirty() {
        return dirty;
    }

    public synchronized void setDirty(boolean value) {
        dirty = value;
    }
    private long timeCreated = System.currentTimeMillis();
    private StateType type;

    public StateType getType() {
        return type;
    }

    public void setType(StateType value) {
        type = value;
    }
    private boolean hasLine = true;

    public boolean hasLine() {
        return hasLine;
    }

    public void setHasLine(boolean value) {
        hasLine = value;
    }
    private ArrayList<float[]> linePoints = new ArrayList<float[]>();

    public synchronized void addLinePoint(float x, float y, float z) {
        float[] p = new float[3];
        p[0] = x;
        p[1] = y;
        p[2] = z;
        linePoints.add(p);
        dirty = true;
    }

    public float[][] getLinePoints() {
// 	float[][] p = new float[linePoints.size()][];
// 	for(int loopi = 0; loopi < p.length; loopi++) {
// 	    p[loopi] = linePoints.get(loopi);
// 	}
// 	return p;
        return linePoints.toArray(new float[1][1]);
    }
    // @TODO: bleah, we really should have lat/lon/alt here and have
    // it taken care of inside Model when it accesses stateDB BUT I
    // suspect it's a bad idea to keep reconverting the same values
    // from lat/lon to local coords again and again and also looking
    // up altitudes in the DEM.
    private float xPos = 0.0f;
    private float yPos = 0.0f;
    private float zPos = 0.0f;

    public synchronized float getXPos() {
        return xPos;
    }

    public synchronized float getYPos() {
        return yPos;
    }

    public synchronized float getZPos() {
        return zPos;
    }

    public synchronized void setPos(float x, float y, float z) {
        if (hasLine) {
            addLinePoint(x, y, z);
        }
        xPos = x;
        yPos = y;
        zPos = z;
        setDirty(true);
    }
    private float xRot = 0.0f;
    private float yRot = 0.0f;
    private float zRot = 0.0f;

    public synchronized float getXRot() {
        return xRot;
    }

    public synchronized float getYRot() {
        return yRot;
    }

    public synchronized float getZRot() {
        return zRot;
    }

    public synchronized void setRot(float x, float y, float z) {
        xRot = x;
        yRot = y;
        zRot = z;
    }
    // NOTE: Could/should move some of these fields into a
    // subclass... should do that if this gets much larger, for now
    // keep it simpler.
    private float headingDegrees = 0.0f;

    public synchronized float getHeadingDegrees() {
        return headingDegrees;
    }

    public synchronized void setHeadingDegrees(float value) {
        headingDegrees = value;
    }
    private ForceID forceID = ForceID.UNKNOWN;

    public synchronized ForceID getForceID() {
        return forceID;
    }

    public synchronized void setForceID(ForceID value) {
        forceID = value;
    }
    private KillStatus killStatus = KillStatus.LIVE;

    public KillStatus getKillStatus() {
        return killStatus;
    }

    public void setKillStatus(KillStatus value) {
        killStatus = value;
    }

    public State(String key, StateType type) {
        this.key = key;
        this.type = type;
    }

    public String toString() {
        return "State: key=" + key + " dirty=" + dirty + " timeCreated=" + timeCreated + " type=" + type + " hasLine=" + hasLine + " lineSize=" + linePoints.size() + " pos=" + xPos + "," + yPos + "," + zPos + " heading=" + headingDegrees + " forceid=" + forceID + " killStatus=" + killStatus;
    }
}
