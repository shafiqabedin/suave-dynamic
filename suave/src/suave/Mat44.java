package suave;

// <sparky> it's slightly expensive but just don't use it to agressively
// <sparky> you can either get the whole package -->
//          http://playstation2-linux.com/projects/sps2
// <sparky> or just this file -->
//          http://playstation2-linux.com/project/download_single_file.php/103_648_14/mat44.cpp
// <sparky> the mat44.cpp has a 4x4 inverse in it
// <sparky> it's the operator ~
// <sparky> but you can make it a function if you want instead
// <sparky> whatever
// <sparky> grab it, twist it, hug it, squeeze it.
// <sparky> do as you please
public class Mat44 {

    public static float[] multVec4(float[] m, float[] v) {
        float[] r = new float[4];

        r[0] = m[0 + 0 * 4] * v[0] + m[0 + 1 * 4] * v[1] + m[0 + 2 * 4] * v[2] + m[0 + 3 * 4] * v[3];
        r[1] = m[1 + 0 * 4] * v[0] + m[1 + 1 * 4] * v[1] + m[1 + 2 * 4] * v[2] + m[1 + 3 * 4] * v[3];
        r[2] = m[2 + 0 * 4] * v[0] + m[2 + 1 * 4] * v[1] + m[2 + 2 * 4] * v[2] + m[2 + 3 * 4] * v[3];
        r[3] = m[3 + 0 * 4] * v[0] + m[3 + 1 * 4] * v[1] + m[3 + 2 * 4] * v[2] + m[3 + 3 * 4] * v[3];

        return r;
    }

    public static float[] mult(float[] m1, float[] m2) {
        float[] r = new float[16];

        for (int iCol = 0; iCol < 4; iCol++) {
            r[0 + iCol * 4] = m1[0 + 0 * 4] * m2[0 + iCol * 4] + m1[0 + 1 * 4] * m2[1 + iCol * 4] + m1[0 + 2 * 4] * m2[2 + iCol * 4] + m1[0 + 3 * 4] * m2[3 + iCol * 4];
            r[1 + iCol * 4] = m1[1 + 0 * 4] * m2[0 + iCol * 4] + m1[1 + 1 * 4] * m2[1 + iCol * 4] + m1[1 + 2 * 4] * m2[2 + iCol * 4] + m1[1 + 3 * 4] * m2[3 + iCol * 4];
            r[2 + iCol * 4] = m1[2 + 0 * 4] * m2[0 + iCol * 4] + m1[2 + 1 * 4] * m2[1 + iCol * 4] + m1[2 + 2 * 4] * m2[2 + iCol * 4] + m1[2 + 3 * 4] * m2[3 + iCol * 4];
            r[3 + iCol * 4] = m1[3 + 0 * 4] * m2[0 + iCol * 4] + m1[3 + 1 * 4] * m2[1 + iCol * 4] + m1[3 + 2 * 4] * m2[2 + iCol * 4] + m1[3 + 3 * 4] * m2[3 + iCol * 4];
        }

        return r;
    }

    public static float[] multequals(float[] m1, float[] m2) {
        return mult(m2, m1);		// We add m2 to the heap of transformations
    }

    public static float[] inverse(float[] m) {
        float[] r = new float[16];

        float f22Det1 = m[2 + 2 * 4] * m[3 + 3 * 4] - m[2 + 3 * 4] * m[3 + 2 * 4];
        float f22Det2 = m[2 + 1 * 4] * m[3 + 3 * 4] - m[2 + 3 * 4] * m[3 + 1 * 4];
        float f22Det3 = m[2 + 1 * 4] * m[3 + 2 * 4] - m[2 + 2 * 4] * m[3 + 1 * 4];
        float f22Det4 = m[2 + 0 * 4] * m[3 + 3 * 4] - m[2 + 3 * 4] * m[3 + 0 * 4];
        float f22Det5 = m[2 + 0 * 4] * m[3 + 2 * 4] - m[2 + 2 * 4] * m[3 + 0 * 4];
        float f22Det6 = m[2 + 0 * 4] * m[3 + 1 * 4] - m[2 + 1 * 4] * m[3 + 0 * 4];
        float f22Det7 = m[1 + 2 * 4] * m[3 + 3 * 4] - m[1 + 3 * 4] * m[3 + 2 * 4];
        float f22Det8 = m[1 + 1 * 4] * m[3 + 3 * 4] - m[1 + 3 * 4] * m[3 + 1 * 4];
        float f22Det9 = m[1 + 1 * 4] * m[3 + 2 * 4] - m[1 + 2 * 4] * m[3 + 1 * 4];
        float f22Det10 = m[1 + 2 * 4] * m[2 + 3 * 4] - m[1 + 3 * 4] * m[2 + 2 * 4];
        float f22Det11 = m[1 + 1 * 4] * m[2 + 3 * 4] - m[1 + 3 * 4] * m[2 + 1 * 4];
        float f22Det12 = m[1 + 1 * 4] * m[2 + 2 * 4] - m[1 + 2 * 4] * m[2 + 1 * 4];
        float f22Det13 = m[1 + 0 * 4] * m[3 + 3 * 4] - m[1 + 3 * 4] * m[3 + 0 * 4];
        float f22Det14 = m[1 + 0 * 4] * m[3 + 2 * 4] - m[1 + 2 * 4] * m[3 + 0 * 4];
        float f22Det15 = m[1 + 0 * 4] * m[2 + 3 * 4] - m[1 + 3 * 4] * m[2 + 0 * 4];
        float f22Det16 = m[1 + 0 * 4] * m[2 + 2 * 4] - m[1 + 2 * 4] * m[2 + 0 * 4];
        float f22Det17 = m[1 + 0 * 4] * m[3 + 1 * 4] - m[1 + 1 * 4] * m[3 + 0 * 4];
        float f22Det18 = m[1 + 0 * 4] * m[2 + 1 * 4] - m[1 + 1 * 4] * m[2 + 0 * 4];

        float fFirst33Det = m[1 + 1 * 4] * f22Det1 - m[1 + 2 * 4] * f22Det2 + m[1 + 3 * 4] * f22Det3;
        float fSec33Det = m[1 + 0 * 4] * f22Det1 - m[1 + 2 * 4] * f22Det4 + m[1 + 3 * 4] * f22Det5;
        float fThird33Det = m[1 + 0 * 4] * f22Det2 - m[1 + 1 * 4] * f22Det4 + m[1 + 3 * 4] * f22Det6;
        float fFourth33Det = m[1 + 0 * 4] * f22Det3 - m[1 + 1 * 4] * f22Det5 + m[1 + 2 * 4] * f22Det6;

        float fDet44 = m[0 + 0 * 4] * fFirst33Det - m[0 + 1 * 4] * fSec33Det + m[0 + 2 * 4] * fThird33Det - m[0 + 3 * 4] * fFourth33Det;

        float s = 1.0f / fDet44;

        r[0 + 0 * 4] = s * fFirst33Det;
        r[0 + 1 * 4] = -s * (m[0 + 1 * 4] * f22Det1 - m[0 + 2 * 4] * f22Det2 + m[0 + 3 * 4] * f22Det3);
        r[0 + 2 * 4] = s * (m[0 + 1 * 4] * f22Det7 - m[0 + 2 * 4] * f22Det8 + m[0 + 3 * 4] * f22Det9);
        r[0 + 3 * 4] = -s * (m[0 + 1 * 4] * f22Det10 - m[0 + 2 * 4] * f22Det11 + m[0 + 3 * 4] * f22Det12);

        r[1 + 0 * 4] = -s * fSec33Det;
        r[1 + 1 * 4] = s * (m[0 + 0 * 4] * f22Det1 - m[0 + 2 * 4] * f22Det4 + m[0 + 3 * 4] * f22Det5);
        r[1 + 2 * 4] = -s * (m[0 + 0 * 4] * f22Det7 - m[0 + 2 * 4] * f22Det13 + m[0 + 3 * 4] * f22Det14);
        r[1 + 3 * 4] = s * (m[0 + 0 * 4] * f22Det10 - m[0 + 2 * 4] * f22Det15 + m[0 + 3 * 4] * f22Det16);

        r[2 + 0 * 4] = s * fThird33Det;
        r[2 + 1 * 4] = -s * (m[0 + 0 * 4] * f22Det2 - m[0 + 1 * 4] * f22Det4 + m[0 + 3 * 4] * f22Det6);
        r[2 + 2 * 4] = s * (m[0 + 0 * 4] * f22Det8 - m[0 + 1 * 4] * f22Det13 + m[0 + 3 * 4] * f22Det17);
        r[2 + 3 * 4] = -s * (m[0 + 0 * 4] * f22Det11 - m[0 + 1 * 4] * f22Det15 + m[0 + 3 * 4] * f22Det18);

        r[3 + 0 * 4] = -s * fFourth33Det;
        r[3 + 1 * 4] = s * (m[0 + 0 * 4] * f22Det3 - m[0 + 1 * 4] * f22Det5 + m[0 + 2 * 4] * f22Det6);
        r[3 + 2 * 4] = -s * (m[0 + 0 * 4] * f22Det9 - m[0 + 1 * 4] * f22Det14 + m[0 + 2 * 4] * f22Det17);
        r[3 + 3 * 4] = s * (m[0 + 0 * 4] * f22Det12 - m[0 + 1 * 4] * f22Det16 + m[0 + 2 * 4] * f22Det18);

        return r;
    }

    public static float determinant(float[] m) {
        float f22Det1 = m[2 + 2 * 4] * m[3 + 3 * 4] - m[2 + 3 * 4] * m[3 + 2 * 4];
        float f22Det2 = m[2 + 1 * 4] * m[3 + 3 * 4] - m[2 + 3 * 4] * m[3 + 1 * 4];
        float f22Det3 = m[2 + 1 * 4] * m[3 + 2 * 4] - m[2 + 2 * 4] * m[3 + 1 * 4];
        float f22Det4 = m[2 + 0 * 4] * m[3 + 3 * 4] - m[2 + 3 * 4] * m[3 + 0 * 4];
        float f22Det5 = m[2 + 0 * 4] * m[3 + 2 * 4] - m[2 + 2 * 4] * m[3 + 0 * 4];
        float f22Det6 = m[2 + 0 * 4] * m[3 + 1 * 4] - m[2 + 1 * 4] * m[3 + 0 * 4];

        float fFirst33Det = m[1 + 1 * 4] * f22Det1 - m[1 + 2 * 4] * f22Det2 + m[1 + 3 * 4] * f22Det3;
        float fSec33Det = m[1 + 0 * 4] * f22Det1 - m[1 + 2 * 4] * f22Det4 + m[1 + 3 * 4] * f22Det5;
        float fThird33Det = m[1 + 0 * 4] * f22Det2 - m[1 + 1 * 4] * f22Det4 + m[1 + 3 * 4] * f22Det6;
        float fFourth33Det = m[1 + 0 * 4] * f22Det3 - m[1 + 1 * 4] * f22Det5 + m[1 + 2 * 4] * f22Det6;


        return m[0 + 0 * 4] * fFirst33Det - m[0 + 1 * 4] * fSec33Det + m[0 + 2 * 4] * fThird33Det - m[0 + 3 * 4] * fFourth33Det;
    }

    public static float[] transpose(float[] m) {
        float[] r = new float[16];

        r[0 + 0 * 4] = m[0 + 0 * 4];
        r[1 + 0 * 4] = m[0 + 1 * 4];
        r[2 + 0 * 4] = m[0 + 2 * 4];
        r[3 + 0 * 4] = m[0 + 3 * 4];

        r[0 + 1 * 4] = m[1 + 0 * 4];
        r[1 + 1 * 4] = m[1 + 1 * 4];
        r[2 + 1 * 4] = m[1 + 2 * 4];
        r[3 + 1 * 4] = m[1 + 3 * 4];

        r[0 + 2 * 4] = m[2 + 0 * 4];
        r[1 + 2 * 4] = m[2 + 1 * 4];
        r[2 + 2 * 4] = m[2 + 2 * 4];
        r[3 + 2 * 4] = m[2 + 3 * 4];

        r[0 + 3 * 4] = m[3 + 0 * 4];
        r[1 + 3 * 4] = m[3 + 1 * 4];
        r[2 + 3 * 4] = m[3 + 2 * 4];
        r[3 + 3 * 4] = m[3 + 3 * 4];

        return r;
    }

    public static void loadIdentity(float[] pM) {
        pM[0 + 0 * 4] = 1;
        pM[1 + 0 * 4] = 0;
        pM[2 + 0 * 4] = 0;
        pM[3 + 0 * 4] = 0;
        pM[0 + 1 * 4] = 0;
        pM[1 + 1 * 4] = 1;
        pM[2 + 1 * 4] = 0;
        pM[3 + 1 * 4] = 0;
        pM[0 + 2 * 4] = 0;
        pM[1 + 2 * 4] = 0;
        pM[2 + 2 * 4] = 1;
        pM[3 + 2 * 4] = 0;
        pM[0 + 3 * 4] = 0;
        pM[1 + 3 * 4] = 0;
        pM[2 + 3 * 4] = 0;
        pM[3 + 3 * 4] = 1;
    }

    // Don't know what Quat is... obviously a quaternion but...q
    // public static void loadRotation(float[] pM, const Quat &Q ) {
    // 	float tx  = 2*Q.V[0];
    // 	float ty  = 2*Q.V[1];
    // 	float tz  = 2*Q.V[2];
    // 	float twx = tx*Q.s;
    // 	float twy = ty*Q.s;
    // 	float twz = tz*Q.s;
    // 	float txx = tx*Q.V[0];
    // 	float txy = ty*Q.V[0];
    // 	float txz = tz*Q.V[0];
    // 	float tyy = ty*Q.V[1];
    // 	float tyz = tz*Q.V[1];
    // 	float tzz = tz*Q.V[2];
    // 	pM[0+0*4] = 1.0f-tyy-tzz;
    // 	pM[0+1*4] = txy-twz;
    // 	pM[0+2*4] = txz+twy;
    // 	pM[0+3*4] = 0;
    // 	pM[1+0*4] = txy+twz;
    // 	pM[1+1*4] = 1.0f-txx-tzz;
    // 	pM[1+2*4] = tyz-twx;
    // 	pM[1+3*4] = 0;
    // 	pM[2+0*4] = txz-twy;
    // 	pM[2+1*4] = tyz+twx;
    // 	pM[2+2*4] = 1.0f-txx-tyy;
    // 	pM[2+3*4] = 0;
    // 	pM[3+0*4] = 0;
    // 	pM[3+1*4] = 0;
    // 	pM[3+2*4] = 0;
    // 	pM[3+3*4] = 1.0f;
    // }
    // YXZ * p	(Z first)
    public static void loadRotation(float[] pM, float fX, float fY, float fZ) {
        float fCx = (float) Math.cos(fX);
        float fSx = (float) Math.sin(fX);
        float fCy = (float) Math.cos(fY);
        float fSy = (float) Math.sin(fY);
        float fCz = (float) Math.cos(fZ);
        float fSz = (float) Math.sin(fZ);

        pM[0 + 0 * 4] = fSz * fSx * fSy + fCz * fCy;
        pM[1 + 0 * 4] = fSz * fCx;
        pM[2 + 0 * 4] = fSz * fSx * fCy - fCz * fSy;
        pM[3 + 0 * 4] = 0;

        pM[0 + 1 * 4] = fCz * fSx * fSy - fSz * fCy;
        pM[1 + 1 * 4] = fCz * fCx;
        pM[2 + 1 * 4] = fCz * fSx * fCy + fSz * fSy;
        pM[3 + 1 * 4] = 0;

        pM[0 + 2 * 4] = fCx * fSy;
        pM[1 + 2 * 4] = -fSx;
        pM[2 + 2 * 4] = fCx * fCy;
        pM[3 + 2 * 4] = 0;

        pM[0 + 3 * 4] = 0;
        pM[1 + 3 * 4] = 0;
        pM[2 + 3 * 4] = 0;
        pM[3 + 3 * 4] = 1.0f;
    }

    public static void loadRotationVec4AxisAngle(float[] pM, float[] v, float fAngle) {
        float C = (float) Math.cos(fAngle);
        float S = (float) Math.sin(fAngle);
        float x2 = v[0] * v[0];
        float y2 = v[1] * v[1];
        float z2 = v[2] * v[2];
        float xs = v[0] * S;
        float ys = v[1] * S;
        float zs = v[2] * S;
        float xyOneMinusC = v[0] * v[1] * (1.0f - C);
        float zxOneMinusC = v[2] * v[0] * (1.0f - C);
        float yzOneMinusC = v[1] * v[2] * (1.0f - C);

        pM[0 + 0 * 4] = x2 + C * (1.0f - x2);
        pM[1 + 0 * 4] = xyOneMinusC + zs;
        pM[2 + 0 * 4] = zxOneMinusC - ys;
        pM[3 + 0 * 4] = 0;

        pM[0 + 1 * 4] = xyOneMinusC - zs;
        pM[1 + 1 * 4] = y2 + C * (1.0f - y2);
        pM[2 + 1 * 4] = yzOneMinusC + xs;
        pM[3 + 1 * 4] = 0;

        pM[0 + 2 * 4] = zxOneMinusC + ys;
        pM[1 + 2 * 4] = yzOneMinusC - xs;
        pM[2 + 2 * 4] = z2 + C * (1.0f - z2);
        pM[3 + 2 * 4] = 0;

        pM[0 + 3 * 4] = 0;
        pM[1 + 3 * 4] = 0;
        pM[2 + 3 * 4] = 0;
        pM[3 + 3 * 4] = 1.0f;
    }

    public static void setRowVec4(float[] pM, int iRow, float[] v) {
        pM[iRow + 0 * 4] = v[0];
        pM[iRow + 1 * 4] = v[1];
        pM[iRow + 2 * 4] = v[2];
        pM[iRow + 3 * 4] = v[3];
    }

    public static float[] getRow(float[] m, int iRow) {
        float[] v = new float[4];

        v[0] = m[iRow + 0 * 4];
        v[1] = m[iRow + 1 * 4];
        v[2] = m[iRow + 2 * 4];
        v[3] = m[iRow + 3 * 4];

        return v;
    }

    public static void setColumnVec4(float[] pM, int iColumn, float[] v) {
        pM[0 + iColumn * 4] = v[0];
        pM[1 + iColumn * 4] = v[1];
        pM[2 + iColumn * 4] = v[2];
        pM[3 + iColumn * 4] = v[3];
    }

    public static float[] getColumnVec4(float[] m, int iColumn) {
        float[] v = new float[4];

        v[0] = m[0 + iColumn * 4];
        v[1] = m[1 + iColumn * 4];
        v[2] = m[2 + iColumn * 4];
        v[3] = m[3 + iColumn * 4];

        return v;
    }

    public static void copy(float[] from, float[] to) {
        to[0 + 0 * 4] = from[0 + 0 * 4];
        to[1 + 0 * 4] = from[1 + 0 * 4];
        to[2 + 0 * 4] = from[2 + 0 * 4];
        to[3 + 0 * 4] = from[3 + 0 * 4];
        to[0 + 1 * 4] = from[0 + 1 * 4];
        to[1 + 1 * 4] = from[1 + 1 * 4];
        to[2 + 1 * 4] = from[2 + 1 * 4];
        to[3 + 1 * 4] = from[3 + 1 * 4];
        to[0 + 2 * 4] = from[0 + 2 * 4];
        to[1 + 2 * 4] = from[1 + 2 * 4];
        to[2 + 2 * 4] = from[2 + 2 * 4];
        to[3 + 2 * 4] = from[3 + 2 * 4];
        to[0 + 3 * 4] = from[0 + 3 * 4];
        to[1 + 3 * 4] = from[1 + 3 * 4];
        to[2 + 3 * 4] = from[2 + 3 * 4];
        to[3 + 3 * 4] = from[3 + 3 * 4];
    }
}
