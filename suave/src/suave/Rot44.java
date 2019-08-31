package suave;

public class Rot44 {

    // OpenGL matrices are layed out linearly, i.e. an array of 16 floats
    // 
    //          | 0  4  8  12 |
    //          |             |
    //          | 1  5  9  13 |
    //      M = |             |
    //          | 2  6  10 14 |
    //          |             |
    //          | 3  7  11 15 |
    public static void createXRot(double angle, float[] rot44) {
        if (rot44.length != 16) {
            throw new java.lang.IllegalArgumentException("rot44 array must be size 16 (i.e. 4 x 4)");
        }

        float c = (float) Math.cos(Math.toRadians(angle));
        float s = (float) Math.sin(Math.toRadians(angle));

        rot44[0 + 0 * 4] = 1;
        rot44[0 + 1 * 4] = 0;
        rot44[0 + 2 * 4] = 0;
        rot44[0 + 3 * 4] = 0;

        rot44[1 + 0 * 4] = 0;
        rot44[1 + 1 * 4] = c;
        rot44[1 + 2 * 4] = -s;
        rot44[1 + 3 * 4] = 0;

        rot44[2 + 0 * 4] = 0;
        rot44[2 + 1 * 4] = s;
        rot44[2 + 2 * 4] = c;
        rot44[2 + 3 * 4] = 0;

        rot44[3 + 0 * 4] = 0;
        rot44[3 + 1 * 4] = 0;
        rot44[3 + 2 * 4] = 0;
        rot44[3 + 3 * 4] = 1;
    }

    public static void createYRot(double angle, float[] rot44) {
        if (rot44.length != 16) {
            throw new java.lang.IllegalArgumentException("rot44 array must be size 16 (i.e. 4 x 4)");
        }

        float c = (float) Math.cos(Math.toRadians(angle));
        float s = (float) Math.sin(Math.toRadians(angle));

        rot44[0 + 0 * 4] = c;
        rot44[0 + 1 * 4] = 0;
        rot44[0 + 2 * 4] = s;
        rot44[0 + 3 * 4] = 0;

        rot44[1 + 0 * 4] = 0;
        rot44[1 + 1 * 4] = 1;
        rot44[1 + 2 * 4] = 0;
        rot44[1 + 3 * 4] = 0;

        rot44[2 + 0 * 4] = -s;
        rot44[2 + 1 * 4] = 0;
        rot44[2 + 2 * 4] = c;
        rot44[2 + 3 * 4] = 0;

        rot44[3 + 0 * 4] = 0;
        rot44[3 + 1 * 4] = 0;
        rot44[3 + 2 * 4] = 0;
        rot44[3 + 3 * 4] = 1;
    }

    public static void createZRot(double angle, float[] rot44) {
        if (rot44.length != 16) {
            throw new java.lang.IllegalArgumentException("rot44 array must be size 16 (i.e. 4 x 4)");
        }

        float c = (float) Math.cos(Math.toRadians(angle));
        float s = (float) Math.sin(Math.toRadians(angle));

        rot44[0 + 0 * 4] = c;
        rot44[0 + 1 * 4] = -s;
        rot44[0 + 2 * 4] = 0;
        rot44[0 + 3 * 4] = 0;

        rot44[1 + 0 * 4] = s;
        rot44[1 + 1 * 4] = c;
        rot44[1 + 2 * 4] = 0;
        rot44[1 + 3 * 4] = 0;

        rot44[2 + 0 * 4] = 0;
        rot44[2 + 1 * 4] = 0;
        rot44[2 + 2 * 4] = 1;
        rot44[2 + 3 * 4] = 0;

        rot44[3 + 0 * 4] = 0;
        rot44[3 + 1 * 4] = 0;
        rot44[3 + 2 * 4] = 0;
        rot44[3 + 3 * 4] = 1;
    }

    public static void mult(float[] m1, float[] m2, float[] result) {
        result[ 0] = m1[ 0] * m2[ 0] + m1[ 1] * m2[ 4] + m1[ 2] * m2[ 8] + m1[ 3] * m2[12];
        result[ 1] = m1[ 0] * m2[ 1] + m1[ 1] * m2[ 5] + m1[ 2] * m2[ 9] + m1[ 3] * m2[13];
        result[ 2] = m1[ 0] * m2[ 2] + m1[ 1] * m2[ 6] + m1[ 2] * m2[10] + m1[ 3] * m2[14];
        result[ 3] = m1[ 0] * m2[ 3] + m1[ 1] * m2[ 7] + m1[ 2] * m2[11] + m1[ 3] * m2[15];

        result[ 4] = m1[ 4] * m2[ 0] + m1[ 5] * m2[ 4] + m1[ 6] * m2[ 8] + m1[ 7] * m2[12];
        result[ 5] = m1[ 4] * m2[ 1] + m1[ 5] * m2[ 5] + m1[ 6] * m2[ 9] + m1[ 7] * m2[13];
        result[ 6] = m1[ 4] * m2[ 2] + m1[ 5] * m2[ 6] + m1[ 6] * m2[10] + m1[ 7] * m2[14];
        result[ 7] = m1[ 4] * m2[ 3] + m1[ 5] * m2[ 7] + m1[ 6] * m2[11] + m1[ 7] * m2[15];

        result[ 8] = m1[ 8] * m2[ 0] + m1[ 9] * m2[ 4] + m1[10] * m2[ 8] + m1[11] * m2[12];
        result[ 9] = m1[ 8] * m2[ 1] + m1[ 9] * m2[ 5] + m1[10] * m2[ 9] + m1[11] * m2[13];
        result[10] = m1[ 8] * m2[ 2] + m1[ 9] * m2[ 6] + m1[10] * m2[10] + m1[11] * m2[14];
        result[11] = m1[ 8] * m2[ 3] + m1[ 9] * m2[ 7] + m1[10] * m2[11] + m1[11] * m2[15];

        result[12] = m1[12] * m2[ 0] + m1[13] * m2[ 4] + m1[14] * m2[ 8] + m1[15] * m2[12];
        result[13] = m1[12] * m2[ 1] + m1[13] * m2[ 5] + m1[14] * m2[ 9] + m1[15] * m2[13];
        result[14] = m1[12] * m2[ 2] + m1[13] * m2[ 6] + m1[14] * m2[10] + m1[15] * m2[14];
        result[15] = m1[12] * m2[ 3] + m1[13] * m2[ 7] + m1[14] * m2[11] + m1[15] * m2[15];

    }

    public static void createTotalRot(double angleX, double angleY, double angleZ, float[] rot44) {
        float[] rotX = new float[16];
        float[] rotY = new float[16];
        float[] rotZ = new float[16];
        float[] temp = new float[16];

        createXRot(angleX, rotX);
        createYRot(angleY, rotY);
        createZRot(angleZ, rotZ);

        mult(rotX, rotY, temp);
        mult(rotZ, temp, rot44);
    }
}
