package suave;

public class Vec3f {

    public static void set(float[] vec, float x, float y, float z) {
        vec[0] = x;
        vec[1] = y;
        vec[2] = z;
    }

    public static void cpy(float[] dest, float[] src) {
        dest[0] = src[0];
        dest[1] = src[1];
        dest[2] = src[2];
    }

    public static void sub(float[] result, float[] amount) {
        result[0] -= amount[0];
        result[1] -= amount[1];
        result[2] -= amount[2];
    }

    public static void sub(float[] result, float[] from, float[] amount) {
        result[0] = from[0] - amount[0];
        result[1] = from[1] - amount[1];
        result[2] = from[2] - amount[2];
    }

    public static void add(float[] result, float[] amount) {
        result[0] += amount[0];
        result[1] += amount[1];
        result[2] += amount[2];
    }

    public static void add(float[] result, float[] from, float[] amount) {
        result[0] = from[0] + amount[0];
        result[1] = from[1] + amount[1];
        result[2] = from[2] + amount[2];
    }

    public static float dot(float[] v1, float[] v2) {
        return (v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2]);
    }

    public static void cross(float[] result, float[] vec1, float[] vec2) {
        result[0] = vec1[1] * vec2[2] - vec1[2] * vec2[1];
        result[1] = vec1[2] * vec2[0] - vec1[0] * vec2[2];
        result[2] = vec1[0] * vec2[1] - vec1[1] * vec2[0];
    }

    public static void scale(float[] vec1, float scale) {
        vec1[0] *= scale;
        vec1[1] *= scale;
        vec1[2] *= scale;
    }

    public static void normalize(float[] vec) {
        float len = (float) Math.sqrt(vec[0] * vec[0] + vec[1] * vec[1] + vec[2] * vec[2]);
        if (len != 0.0) {
            len = 1.0f / len;
            vec[0] *= len;
            vec[1] *= len;
            vec[2] *= len;
        }
    }

    public static float distSqd(float[] v1, float[] v2) {
        float xdiff = v1[0] - v2[0];
        float ydiff = v1[1] - v2[1];
        float zdiff = v1[2] - v2[2];
        return (float) ((xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff));
    }

    public static float dist(float[] v1, float[] v2) {
        float xdiff = v1[0] - v2[0];
        float ydiff = v1[1] - v2[1];
        float zdiff = v1[2] - v2[2];
        return (float) Math.sqrt((xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff));
    }
}
