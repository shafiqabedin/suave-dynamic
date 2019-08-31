package suave;

import java.util.ArrayList;

public class GeomUtil {

    public static ArrayList<Triangle> sphere(boolean reverseVertexOrderFlag, int maxSubdivisionLevel, float radius) {

        ArrayList<Triangle> triangleList = null;
        ArrayList<Triangle> newTriangleList = null;
        ArrayList<Vertex> verticeList = new ArrayList<Vertex>();
        int i;
        int level;		/* Current subdivision level */

        Vertex XPLUS = new Vertex(1, 0, 0);	/* X */
        Vertex XMIN = new Vertex(-1, 0, 0);	/* -X */
        Vertex YPLUS = new Vertex(0, 1, 0);	/* Y */
        Vertex YMIN = new Vertex(0, -1, 0);	/* -Y */
        Vertex ZPLUS = new Vertex(0, 0, 1);	/* Z */
        Vertex ZMIN = new Vertex(0, 0, -1);	/* -Z */

        triangleList = new ArrayList<Triangle>();
        triangleList.add(new Triangle(new Vertex(XPLUS), new Vertex(ZPLUS), new Vertex(YPLUS)));
        triangleList.add(new Triangle(new Vertex(YPLUS), new Vertex(ZPLUS), new Vertex(XMIN)));
        triangleList.add(new Triangle(new Vertex(XMIN), new Vertex(ZPLUS), new Vertex(YMIN)));
        triangleList.add(new Triangle(new Vertex(YMIN), new Vertex(ZPLUS), new Vertex(XPLUS)));
        triangleList.add(new Triangle(new Vertex(XPLUS), new Vertex(YPLUS), new Vertex(ZMIN)));
        triangleList.add(new Triangle(new Vertex(YPLUS), new Vertex(XMIN), new Vertex(ZMIN)));
        triangleList.add(new Triangle(new Vertex(XMIN), new Vertex(YMIN), new Vertex(ZMIN)));
        triangleList.add(new Triangle(new Vertex(YMIN), new Vertex(XPLUS), new Vertex(ZMIN)));

        for (int loopi = 0; loopi < triangleList.size(); loopi++) {
            Triangle t = triangleList.get(loopi);
            t.v1.normalize();
            t.v2.normalize();
            t.v3.normalize();
            verticeList.add(t.v1);
            verticeList.add(t.v2);
            verticeList.add(t.v3);
        }

        if (reverseVertexOrderFlag) {
            for (i = 0; i < triangleList.size(); i++) {
                Vertex tmp;
                Triangle t = triangleList.get(i);
                tmp = t.v1;
                t.v1 = t.v3;
                t.v3 = tmp;
            }
        }

        /* Subdivide each starting triangle (maxSubdivisionLevel - 1) times */
        for (level = 1; level < maxSubdivisionLevel; level++) {
            //	    Debug.debug(1,"GeomUtil.constructor: Subdividing to level "+level);

            int newListSize = triangleList.size() * 4;
            newTriangleList = new ArrayList<Triangle>(newListSize);

            /* Subdivide each triangle in the old approximation and normalize
             *  the new points thus generated to lie on the surface of the unit
             *  sphere.
             * Each input triangle with vertices labelled [0,1,2] as shown
             *  below will be turned into four new triangles:
             *
             *			Make new points
             *			    a = (0+2)/2
             *			    b = (0+1)/2
             *			    c = (1+2)/2
             *        1
             *       /\                Normalize a, b, c
             *      /  \
             *    b/____\ c            Construct new triangles
             *    /\    /\             [0,b,a]
             *   /  \  /  \            [b,1,c]
             *  /____\/____\           [a,b,c]
             * 0      a     2          [a,c,2]
             */
            for (int loopi = 0; loopi < triangleList.size(); loopi++) {
                Triangle oldt = triangleList.get(loopi);

                Vertex a, b, c;

                oldt.v1.normalize();
                oldt.v2.normalize();
                oldt.v3.normalize();
                a = midpoint(oldt.v1, oldt.v3);
                b = midpoint(oldt.v1, oldt.v2);
                c = midpoint(oldt.v2, oldt.v3);
                verticeList.add(a);
                verticeList.add(b);
                verticeList.add(c);

                newTriangleList.add(new Triangle(oldt.v1, b, a));
                newTriangleList.add(new Triangle(b, oldt.v2, c));
                newTriangleList.add(new Triangle(a, b, c));
                newTriangleList.add(new Triangle(a, c, oldt.v3));
            }

            /* Continue subdividing new triangles */
            triangleList = newTriangleList;
        }
//  	for(int loopi = 0; loopi < triangleList.size(); loopi++) {
//  	    Triangle t = triangleList.get(loopi);
//  	    t.v1.normalize();
//  	    t.v2.normalize();
//  	    t.v3.normalize();
//  	}
        float[] normal = new float[3];
        for (int loopi = 0; loopi < triangleList.size(); loopi++) {
            Triangle t = triangleList.get(loopi);
            // Note: vertex - origin = normal, and since the sphere
            // origin is at 0,0,0 the coordinates of the vertex are
            // already a (possibly unnormalized) normal.

            normal[0] = t.v1.x;
            normal[1] = t.v1.y;
            normal[2] = t.v1.z;
            Vec3f.normalize(normal);
            t.v1.u = (float) (Math.asin(normal[0]) / Math.PI + 0.5);
            t.v1.v = (float) (Math.asin(normal[1]) / Math.PI + 0.5);

            normal[0] = t.v2.x;
            normal[1] = t.v2.y;
            normal[2] = t.v2.z;
            Vec3f.normalize(normal);
            t.v2.u = (float) (Math.asin(normal[0]) / Math.PI + 0.5);
            t.v2.v = (float) (Math.asin(normal[1]) / Math.PI + 0.5);

            normal[0] = t.v3.x;
            normal[1] = t.v3.y;
            normal[2] = t.v3.z;
            Vec3f.normalize(normal);
            t.v3.u = (float) (Math.asin(normal[0]) / Math.PI + 0.5);
            t.v3.v = (float) (Math.asin(normal[1]) / Math.PI + 0.5);
        }
        for (int loopi = 0; loopi < verticeList.size(); loopi++) {
            verticeList.get(loopi).x *= radius;
            verticeList.get(loopi).y *= radius;
            verticeList.get(loopi).z *= radius;
        }
        //	Debug.debug(1,"GeomUtil.constructor: Finished subdividing sphere, now have "+triangleList.size()+" triangles.");
        return triangleList;
    }

    /* Return the midpoint on the line between two points */
    private static Vertex midpoint(Vertex a, Vertex b) {
        float x = (a.x + b.x) * 0.5f;
        float y = (a.y + b.y) * 0.5f;
        float z = (a.z + b.z) * 0.5f;
        float u = (a.u + b.u) * 0.5f;
        float v = (a.v + b.v) * 0.5f;
        float normalx = (a.normalx + b.normalx) * 0.5f;
        float normaly = (a.normaly + b.normaly) * 0.5f;
        float normalz = (a.normalz + b.normalz) * 0.5f;
        return new Vertex(x, y, z, u, v, normalx, normaly, normalz);
    }

    public static ArrayList<Triangle> subdivide(ArrayList<Triangle> triangleList, int maxSubdivisionLevel) {
        ArrayList<Triangle> newTriangleList = null;
        int level;		/* Current subdivision level */

        /* Subdivide each starting triangle (maxSubdivisionLevel - 1) times */
        for (level = 0; level < maxSubdivisionLevel; level++) {
            int newListSize = triangleList.size() * 4;
            newTriangleList = new ArrayList<Triangle>(newListSize);

            // This is the same subdivision as in the sphere routine,
            // without the normalization part.
            for (int loopi = 0; loopi < triangleList.size(); loopi++) {
                Triangle oldt = triangleList.get(loopi);
                Vertex a, b, c;
                a = midpoint(oldt.v1, oldt.v3);
                b = midpoint(oldt.v1, oldt.v2);
                c = midpoint(oldt.v2, oldt.v3);
                newTriangleList.add(new Triangle(oldt.v1, b, a));
                newTriangleList.add(new Triangle(b, oldt.v2, c));
                newTriangleList.add(new Triangle(a, b, c));
                newTriangleList.add(new Triangle(a, c, oldt.v3));
            }
            triangleList = newTriangleList;
        }
        return triangleList;
    }

    public static ArrayList<Triangle> quad(Vertex v1, Vertex v2, Vertex v3, Vertex v4) {
        ArrayList<Triangle> triangleList = new ArrayList<Triangle>();
        triangleList.add(new Triangle(v1, v2, v3));
        triangleList.add(new Triangle(v1, v3, v4));
        return triangleList;
    }

    public static ArrayList<Triangle> quad(float[][] xyzuvCorners) {
        Vertex v1 = new Vertex(xyzuvCorners[0][0], xyzuvCorners[0][1], xyzuvCorners[0][2], xyzuvCorners[0][3], xyzuvCorners[0][4]);
        Vertex v2 = new Vertex(xyzuvCorners[1][0], xyzuvCorners[1][1], xyzuvCorners[1][2], xyzuvCorners[1][3], xyzuvCorners[1][4]);
        Vertex v3 = new Vertex(xyzuvCorners[2][0], xyzuvCorners[2][1], xyzuvCorners[2][2], xyzuvCorners[2][3], xyzuvCorners[2][4]);
        Vertex v4 = new Vertex(xyzuvCorners[3][0], xyzuvCorners[3][1], xyzuvCorners[3][2], xyzuvCorners[3][3], xyzuvCorners[3][4]);
        return quad(v1, v2, v3, v4);
    }

    public static ArrayList<Triangle> ring(double outerRadius, double innerRadius, int numSegments) {
        ArrayList<Triangle> triangleList = new ArrayList<Triangle>();
        ArrayList<Vertex> verticeList = new ArrayList<Vertex>();


        float[][] outer = new float[numSegments][2];
        float[][] inner = new float[numSegments][2];
        double segmentRadians = (Math.PI * 2) / numSegments;
        double halfSegmentRadians = segmentRadians / 2;
        for (int loopi = 0; loopi < numSegments; loopi++) {
            double outerRad = loopi * segmentRadians;
            double innerRad = loopi * segmentRadians + halfSegmentRadians;
            verticeList.add(new Vertex((float) (Math.cos(outerRad) * outerRadius), 0, (float) (Math.sin(outerRad) * outerRadius)));
            verticeList.add(new Vertex((float) (Math.cos(innerRad) * innerRadius), 0, (float) (Math.sin(innerRad) * innerRadius)));
        }
        Vertex v1;
        Vertex v2;
        Vertex v3;
        for (int loopi = 0; loopi < verticeList.size(); loopi++) {
            v1 = verticeList.get(loopi);
            if ((loopi + 1) >= verticeList.size()) {
                v2 = verticeList.get(loopi + 1 - verticeList.size());
            } else {
                v2 = verticeList.get(loopi + 1);
            }
            if ((loopi + 2) >= verticeList.size()) {
                v3 = verticeList.get(loopi + 2 - verticeList.size());
            } else {
                v3 = verticeList.get(loopi + 2);
            }
            triangleList.add(new Triangle(v1, v2, v3));
        }
        return triangleList;
    }
}
