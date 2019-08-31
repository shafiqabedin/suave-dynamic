package uavmap;

public class UavMapTarget {

    private static int idCounter = 1;
    public int id = 0;
    public double screenx = 0.0;
    public double screeny = 0.0;
    public double screenw = 0.0;
    public double screenh = 0.0;
    public double localx = 0.0;
    public double localy = 0.0;
    public double lat = 0.0;
    public double lon = 0.0;
    public boolean selected = false;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean value) {
        selected = value;
    }

    public UavMapTarget() {
        id = idCounter++;
    }

    public UavMapTarget(double screenx, double screeny, double screenw, double screenh, double localx, double localy, double lat, double lon) {
        this.screenx = screenx;
        this.screeny = screeny;
        this.screenw = screenw;
        this.screenh = screenh;
        this.localx = localx;
        this.localy = localy;
        this.lat = lat;
        this.lon = lon;
        id = idCounter++;
    }

    public void set(double screenx, double screeny, double screenw, double screenh, double localx, double localy, double lat, double lon) {
        this.screenx = screenx;
        this.screeny = screeny;
        this.screenw = screenw;
        this.screenh = screenh;
        this.localx = localx;
        this.localy = localy;
        this.lat = lat;
        this.lon = lon;
    }

    public String toString() {
        return ("Target = " + id + " " + lat + " " + lon + " " + localx + " " + localy + " " + screenx + " " + screeny + " " + screenw + " " + screenh);

    }
}


