package suave;

import java.util.*;

public class Rasterize {

    Triangle[] triangleAry = null;
    TextureReader.Texture texture = null;
    double textureWidth;
    double textureHeight;

    public Rasterize(TextureReader.Texture texture) {
        this.texture = texture;
        this.textureWidth = texture.getWidth();
        this.textureHeight = texture.getHeight();
        Debug.debug(1,"Rasterize.init: texture is " + textureWidth + " by " + textureHeight);
    }

    // Buh, what am I doing here?  Ok, here's what I'm doing;
    //
    // when the mesh is created the u and v were already set for each
    // vertice.  So, for a particular triangle, here's what we are
    // doing;
    //
    // First we get the extents of the box covering the triangle
    // -i.e. the max and min u values and the max and min v values
    //
    // second, we floor/ceil the max and min just to make sure we
    // don't miss anything;
    //
    // then we iterate over every texel/pixel position in the texture,
    private void rasterize(Triangle t) {
        // Scale to the texture size.  Otherwise our U,V values are
        // all 0 to 1, and we end up stepping from 0 to 1 and not
        // doing anything.  We want to step by the texel size.  Hmm, I
        // suppose instead of scaling up the u/v we could step by
        // 1/textureWidth and 1/textureHeight.  Hmm.

        // scale the u,v coods to image coords
        float Au = (float) (t.v1.u * textureWidth);
        float Av = (float) (t.v1.v * textureHeight);
        float Bu = (float) (t.v2.u * textureWidth);
        float Bv = (float) (t.v2.v * textureHeight);
        float Cu = (float) (t.v3.u * textureWidth);
        float Cv = (float) (t.v3.v * textureHeight);

        float umin;
        float umax;
        float vmin;
        float vmax;

        // Calculate the max and min texel positions in u and v
        umin = Math.min(Math.min(Au, Bu), Cu);
        umax = Math.max(Math.max(Au, Bu), Cu);
        vmin = Math.min(Math.min(Av, Bv), Cv);
        vmax = Math.max(Math.max(Av, Bv), Cv);

        // floor/ceil to make sure we include any texels on the edges
        umin = (float) Math.floor(umin);
        vmin = (float) Math.floor(vmin);
        umax = (float) Math.ceil(umax);
        vmax = (float) Math.ceil(vmax);

        ArrayList<Float> coordList = new ArrayList<Float>();

        // iterate over each pixel inside the extents
        for (float loopv = vmin; loopv <= vmax; loopv++) {
            for (float loopu = umin; loopu <= umax; loopu++) {
                // A is t.v1
                // B is t.v2
                // C is t.v3
                // P is loopu, loopv

                // test point in triangle, in 2d

                // v0 = C - A
                float v0x = Cu - Au;
                float v0y = Cv - Av;
                // v1 = B - A
                float v1x = Bu - Au;
                float v1y = Bv - Av;
                // v2 = P - A
                float v2x = loopu - Au;
                float v2y = loopv - Av;
                // dot00 = dot(v0, v0)
                float dot00 = (v0x * v0x) + (v0y * v0y);
                // dot01 = dot(v0, v1)
                float dot01 = (v0x * v1x) + (v0y * v1y);
                // dot02 = dot(v0, v2)
                float dot02 = (v0x * v2x) + (v0y * v2y);
                // dot11 = dot(v1, v1)
                float dot11 = (v1x * v1x) + (v1y * v1y);
                // dot12 = dot(v1, v2)
                float dot12 = (v1x * v2x) + (v1y * v2y);
                // Compute barycentric coordinates
                float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
                float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
                float v = (dot00 * dot12 - dot01 * dot02) * invDenom;
                // check if point is in triangle
                if ((u > 0) && (v > 0) && (u + v < 1)) {
                    // since the barycentric coordinates of this point
                    // in the triangle are;
                    //
                    // P = A + u * (C - A) + v * (B - A)
                    //
                    // we can use u and v to calculate corresponding
                    // point in model space;
                    float modelx = t.v1.x + u * (t.v3.x - t.v1.x) + v * (t.v2.x - t.v1.x);
                    float modely = t.v1.y + u * (t.v3.y - t.v1.y) + v * (t.v2.y - t.v1.y);
                    float modelz = t.v1.z + u * (t.v3.z - t.v1.z) + v * (t.v2.z - t.v1.z);

                    // store the u,v coords and corresponding model coords of this texel
                    coordList.add((float) (loopu / textureWidth));
                    coordList.add((float) (loopv / textureHeight));
                    coordList.add(modelx);
                    coordList.add(modely);
                    coordList.add(modelz);
                }
            }
        }
        t.rasterizedUvxyz = new float[coordList.size()];
        for (int loopi = 0; loopi < coordList.size(); loopi++) {
            t.rasterizedUvxyz[loopi] = coordList.get(loopi);
        }
    }

    public void update(ArrayList<Triangle> triangles) {
        if (null == triangles) {
            return;
        }
        if (triangles.size() <= 0) {
            return;
        }

        Debug.debug(1,"Rasterize.update: STARTING RASTERIZING");
        long startTime = System.currentTimeMillis();

        triangleAry = triangles.toArray(new Triangle[1]);
        for (int loopi = 0; loopi < triangleAry.length; loopi++) {
            rasterize(triangleAry[loopi]);
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        Debug.debug(1,"Rasterize.update: DONE RASTERIZING, elapsed time=" + elapsedTime);

    }
}
