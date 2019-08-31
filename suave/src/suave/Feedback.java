package suave;

import java.util.*;



import java.nio.FloatBuffer;
import javax.media.opengl.GL;
import com.sun.opengl.util.BufferUtil;

public class Feedback {

    public final static int feedbackBufferSize = 102400;
    public ArrayList<Triangle> triangles;

    public Feedback(ArrayList<Triangle> triangles) {
        this.triangles = triangles;

        boolean testing = false;
        boolean testing2 = false;
        if (testing) {
            triangles.clear();
            Triangle t;
            Vertex v1, v2, v3;

            for (int loopi = 0; loopi < 20; loopi++) {
                v1 = new Vertex(0 + (loopi * 60), 0 + (loopi * 60), 0);
                v2 = new Vertex(0 + (loopi * 65), 50 + (loopi * 60), 0);
                v3 = new Vertex(50 + (loopi * 65), 50 + (loopi * 60), 0);
                t = new Triangle(v1, v2, v3);
                triangles.add(t);
            }
        } else if (testing2) {
            Triangle t;
            Vertex v1, v2, v3;

            v1 = new Vertex(0, 0, -500);
            v2 = new Vertex(0, 500, -500);
            v3 = new Vertex(500, 500, -500);
            t = new Triangle(v1, v2, v3);
            triangles.add(t);
        }
    }
    public int fbTriangleNonVisibleCount = 0;
    public int fbTriangleVisibleCount = 0;
    public int fbTriangleSplitCount = 0;

    public void init(GL gl) {
    }

    public void render(GL gl) {
        float feedBuffer[] = new float[feedbackBufferSize];
        FloatBuffer feedBuf = BufferUtil.newFloatBuffer(feedbackBufferSize);
        int size;

        gl.glFeedbackBuffer(feedbackBufferSize, GL.GL_3D_COLOR, feedBuf);
        gl.glRenderMode(GL.GL_FEEDBACK);
        drawGeometry(gl, GL.GL_FEEDBACK);

        size = gl.glRenderMode(GL.GL_RENDER);
        feedBuf.get(feedBuffer);

        for (int loopi = 0; loopi < triangles.size(); loopi++) {
            triangles.get(loopi).reset();
        }

        System.out.println("Feedback.render: PROCESSING FEEDBACK BUFFER");
        parseBuffer(gl, size, feedBuffer);
        System.out.println("Feedback.render: DONE PROCESSING FEEDBACK BUFFER");
    }

    public void drawGeometry(GL gl, int mode) {
// 	int count = 0;
// 	for(int loopi = 0; loopi < 50; loopi++) {
// 	    if (mode == GL.GL_FEEDBACK) gl.glPassThrough(count++);
// 	    gl.glBegin(GL.GL_TRIANGLES);
// 	    gl.glNormal3f(0.0f, 0.0f, 1.0f);
// 	    gl.glVertex3f((count*70)+30.0f, (count*70)+30.0f, 0.0f);
// 	    gl.glVertex3f((count*70)+50.0f, (count*70)+60.0f, 0.0f);
// 	    gl.glVertex3f((count*70)+70.0f, (count*70)+40.0f, 0.0f);
// 	    gl.glEnd();
// 	    count++;
// 	}

        for (int loopi = 0; loopi < triangles.size(); loopi++) {
            if (mode == GL.GL_FEEDBACK) {
                gl.glPassThrough(loopi);
            }
            Triangle t = triangles.get(loopi);
            gl.glBegin(GL.GL_TRIANGLES);
            gl.glNormal3f(t.normalx, t.normaly, t.normalz);
            gl.glVertex3f(t.v1.x, t.v1.y, t.v1.z);
            gl.glVertex3f(t.v2.x, t.v2.y, t.v2.z);
            gl.glVertex3f(t.v3.x, t.v3.y, t.v3.z);
            gl.glEnd();
        }
        if (mode == GL.GL_FEEDBACK) {
            System.out.println("Feedback.drawGeometry: drew " + triangles.size() + " triangles.");
        }
    }

    int handleTriangle(Triangle tri, int size, int count, float[] buffer) {
        if (null == tri) {
            System.out.print("Feedback.handleTriangle: ERROR: triangle is null!");
        }
        int numVertexes = (int) buffer[count];
        count++;	// Skip over the num vertexes
        int used = 1;
        if (numVertexes != 3) {
            System.out.print("Feedback.handleTriangle: ERROR: Poly has more or less than 3 vertices!");
            System.out.print("POLY size " + numVertexes + " ");
            for (int loopj = 0; loopj < numVertexes; loopj++) {
                used += print3DColorVertex(size, count + used, buffer);
                System.out.print("   ");
            }
            System.out.println();
        } else {

            tri.visibleCount++;
            if (tri.visibleCount > 1) {
                tri = tri.addTriangle();
            }

            // Hopefully they will be in the same order as when we
            // created them...
            float x, y, z;

            //	    System.out.print("                                                                                    ");
            //	    print3DColorVertex(size, count, buffer);
            tri.v1.screenx = buffer[count++];
            tri.v1.screeny = buffer[count++];
            tri.v1.screenz = buffer[count++];
            count += 4;	// skip colors

            //	    System.out.print(" ");
            //	    print3DColorVertex(size, count, buffer);
            tri.v2.screenx = buffer[count++];
            tri.v2.screeny = buffer[count++];
            tri.v2.screenz = buffer[count++];
            count += 4;	// skip colors

            //	    System.out.print(" ");
            //	    print3DColorVertex(size, count, buffer);
            tri.v3.screenx = buffer[count++];
            tri.v3.screeny = buffer[count++];
            tri.v3.screenz = buffer[count++];
            count += 4;	// skip colors

            used += (3 * 7);
            //	    System.out.println();
            //	    System.out.println("Triangle = "+tri);
        }

        return used;
    }

    // NOTE: This prints 7 values because we specified GL_3D_COLOR in
    // the call to glFeedbackBuffer.  If we change that value, then we
    // will get a different number of floats.

    /* Write contents of one vertex to stdout. */
    int print3DColorVertex(int size, int count, float[] buffer) {
        // Only print the coordinates, skip the 4 values for RGBA
        System.out.print(" (" + buffer[count] + ", " + buffer[count + 1] + ", " + buffer[count = 2] + ")");
        // but still tell our caller to skip all 7 values.
        return 7;
    }

    int print3DColorLine(int size, int count, float[] buffer) {
        int used = 0;
        System.out.print("LINE ");
        used += print3DColorVertex(size, count, buffer);
        used += print3DColorVertex(size, count, buffer);
        System.out.println();
        return used;
    }

    int print3DColorPolygon(int size, int count, float[] buffer) {
        int used = 1;
        int numVertexes = (int) buffer[count];

        System.out.print("POLY size " + numVertexes + " ");
        for (int loopj = 0; loopj < numVertexes; loopj++) {
            used += print3DColorVertex(size, count + used, buffer);
            System.out.print("   ");
        }
        System.out.println();
        return used;
    }

    int print3DColorBitmap(int size, int count, float[] buffer) {
        return 0;
    }

    int print3DColorPixel(int size, int count, float[] buffer) {
        return 0;
    }

    /* Write contents of entire buffer. (Parse tokens!) */
    private void parseBuffer(GL gl, int size, float[] buffer) {
        int count;
        float token;

        int lastPassThrough = 0;
        count = 0;
        while (count < size) {
            token = buffer[count];
            count++;
            if (token == GL.GL_PASS_THROUGH_TOKEN) {
                //		System.out.print("GL.GL_PASS_THROUGH_TOKEN");
                //		System.out.println("\t " + buffer[count]);
                lastPassThrough = (int) buffer[count];
                count++;
                continue;
            }

            if (token == GL.GL_POINT_TOKEN) {
                System.out.println("GL.GL_POINT_TOKEN");
                count += print3DColorVertex(size, count, buffer);
                System.out.println();
            } else if (token == GL.GL_LINE_TOKEN) {
                System.out.println("GL.GL_LINE_TOKEN ");
                count += print3DColorLine(size, count, buffer);
            } else if (token == GL.GL_LINE_RESET_TOKEN) {
                System.out.println("GL.GL_LINE_RESET_TOKEN ");
                count += print3DColorLine(size, count, buffer);
            } // Polygon 	GL_POLYGON_TOKEN 	n vertex vertex ... vertex
            else if (token == GL.GL_POLYGON_TOKEN) {
                //		System.out.println("GL.GL_POLYGON_TOKEN ");
                try {
                    Triangle t = triangles.get(lastPassThrough);
                    count += handleTriangle(t, size, count, buffer);
                } catch (java.lang.IndexOutOfBoundsException e) {
                    System.out.println("Last Passthrough = " + lastPassThrough + " is out of range for our list of triangles.");
                }
                //		count += print3DColorPolygon(size, count, buffer);
            } // Bitmap 	GL_BITMAP_TOKEN 	vertex
            else if (token == GL.GL_BITMAP_TOKEN) {
                System.out.println("GL.GL_BITMAP_TOKEN ");
                count += print3DColorBitmap(size, count, buffer);
            } // Pixel Rectangle 	GL_DRAW_PIXEL_TOKEN
            else if (token == GL.GL_DRAW_PIXEL_TOKEN) {
                System.out.println("GL.GL_DRAW_PIXEL_TOKEN ");
                count += print3DColorPixel(size, count, buffer);
            } // GL_COPY_PIXEL_TOKEN 	vertex
            else if (token == GL.GL_COPY_PIXEL_TOKEN) {
                System.out.println("GL.GL_COPY_PIXEL_TOKEN ");
                count += print3DColorPixel(size, count, buffer);
            } else {
                System.out.println("Don't recognize Feedback token=" + token);
            }
        }

        fbTriangleNonVisibleCount = 0;
        fbTriangleVisibleCount = 0;
        fbTriangleSplitCount = 0;
        for (int loopi = 0; loopi < triangles.size(); loopi++) {
            Triangle t = triangles.get(loopi);
            if (t.visibleCount < 1) {
                fbTriangleNonVisibleCount++;
            } else if (t.visibleCount == 1) {
                fbTriangleVisibleCount++;
            } else if (t.visibleCount > 1) {
                fbTriangleSplitCount++;
            }
        }
        System.out.println("Feedback.printBuffer: non visible=" + fbTriangleNonVisibleCount + ", one visible=" + fbTriangleVisibleCount + ", more than one (split?) visible=" + fbTriangleSplitCount + ", total counted=" + (fbTriangleNonVisibleCount + fbTriangleVisibleCount + fbTriangleSplitCount) + " triangle list size=" + triangles.size() + " should be equal to total counted");
    }
}
