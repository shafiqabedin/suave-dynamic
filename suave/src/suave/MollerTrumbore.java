package suave;

// This is based on the paper "Fast, Minimum Storage Ray/Triangle
// Intersection" by Tomas Moller and Ben Trumbore
//
// Note, this code is the non backface culling version.
public class MollerTrumbore {

    public final static float EPSILON = 0.000001f;

    // orig and dir are the ray in barycentric coords.  orig is the
    // origin of the ray, dir is the direction vector.  tuv is used to
    // return the thit location.  u and v are the coordinates of the
    // hit in barycentric coordinates of the triangle, while t is the
    // distance.  From the paper;
    // 
    // http://www.cs.virginia.edu/~gfx/Courses/2003/ImageSynthesis/papers/Acceleration/Fast%20MinimumStorage%20RayTriangle%20Intersection.pdf
    // 
    // > A point, T(u,v), on a triangle is given by
    // > T(u,v) = (1 - u - v) * V0 + u * V1 + v * V2
    // 
    // > This means that the barycentric coordinates (u,v) and the distance, t,
    // > from the ray origin to the intersection point, can be found using the
    // > linear system of equations above.
    //
    // I'm not sure exactly what 't' is - maybe a percentage of the line?
    // 
    // This method returns 0 if no intersection or 1 if intersection,
    // and the location of the hit (as described above) in the tuv
    // array.
    public static int intersectTriangle(float orig[], float dir[],
            float vert0[], float vert1[], float vert2[],
            float tuv[]) {
        float[] edge1 = new float[3];
        float[] edge2 = new float[3];
        float[] tvec = new float[3];
        float[] pvec = new float[3];
        float[] qvec = new float[3];
        float det;
        float inv_det;

        // find vectors for two edges sharing vert0

        // edge1 = vert1 - vert0
        Vec3f.sub(edge1, vert1, vert0);
        // edge2 = vert2 - vert0
        Vec3f.sub(edge2, vert2, vert0);

        // begin calculating determinant - also used to calculate U parameter
        // pvec = dir x edge2
        Vec3f.cross(pvec, dir, edge2);

        // if determinant is near zero, ray lies in plane of triangle
        det = Vec3f.dot(edge1, pvec);

        if (det > -EPSILON && det < EPSILON) {
            //	    Debug.debug(1, "in plane");
            return 0;
        }
        inv_det = 1.0f / det;

        // calculate distance from vert0 to ray origin
        // tvec = orig - vert0
        Vec3f.sub(tvec, orig, vert0);

        // calculate U parameter and test bounds
        tuv[1] = Vec3f.dot(tvec, pvec) * inv_det;
        if (tuv[1] < 0.0 || tuv[1] > 1.0) {
            //	    Debug.debug(1, "out of bounds 1");
            return 0;
        }

        // prepare to test V parameter
        // qvec = tvec x edg1
        Vec3f.cross(qvec, tvec, edge1);

        // calculate V parameter and test bounds
        tuv[2] = Vec3f.dot(dir, qvec) * inv_det;
        if (tuv[2] < 0.0 || tuv[1] + tuv[2] > 1.0) {
            //	    Debug.debug(1, "out of bounds 2");
            return 0;
        }

        // calculate t, ray intersects triangle
        tuv[0] = Vec3f.dot(edge2, qvec) * inv_det;

        return 1;
    }

    public static int intersectTriangle(float orig[], float dir[],
            Triangle t,
            float tuv[]) {
        float[] vert0 = {t.v1.x, t.v1.y, t.v1.z};
        float[] vert1 = {t.v2.x, t.v2.y, t.v2.z};
        float[] vert2 = {t.v3.x, t.v3.y, t.v3.z};
        return intersectTriangle(orig, dir, vert0, vert1, vert2, tuv);
    }
}
