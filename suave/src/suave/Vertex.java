package suave;

public class Vertex {

    float x = 0;
    float y = 0;
    float z = 0;
    float u = 0;
    float v = 0;
    float normalx = 0;
    float normaly = 0;
    float normalz = 0;
    float distSqdFromViewpoint = 0;
    float screenx;
    float screeny;
    float screenz;

    public float[] getNormal() {
        float[] n = new float[3];
        n[0] = normalx;
        n[1] = normaly;
        n[2] = normalz;
        return n;
    }

    public void setNormal(float[] n) {
        normalx = n[0];
        normaly = n[1];
        normalz = n[2];
    }

    public void addToNormal(float x, float y, float z) {
        normalx += x;
        normaly += y;
        normalz += z;
    }

    public void scale(float value) {
        x *= value;
        y *= value;
        z *= value;
    }

    public Vertex(float x, float y, float z, float u, float v) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.u = u;
        this.v = v;
    }

    public Vertex(float x, float y, float z, float u, float v, float normalx, float normaly, float normalz) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.u = u;
        this.v = v;
        this.normalx = normalx;
        this.normaly = normaly;
        this.normalz = normalz;
    }

    public Vertex(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vertex(Vertex v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.u = v.u;
        this.v = v.v;
    }

    public void normalize() {
        double mag;
        mag = x * x + y * y + z * z;
        if (mag != 0.0) {
            mag = 1.0 / Math.sqrt(mag);
            x *= mag;
            y *= mag;
            z *= mag;
        }
    }

    public String toString() {
        return ("3D (" + x + ", " + y + ", " + z + ") uv (" + u + ", " + v + ") screen (" + screenx + ", " + screeny + ", " + screenz + ")");
    }
}
