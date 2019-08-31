package suave;

import javax.media.opengl.GL;
import com.sun.opengl.util.BufferUtil;

public class RenderableInterleavedVBO implements Renderable {

    public final static int FL_SIZE = BufferUtil.SIZEOF_FLOAT;
    // Vertex Attribute Data - i.e. x,y,z then normalx, normaly, normalz, then texture u,v - so 8 floats.
    public final static int ATTR_V_FLOATS_PER = 3;
    public final static int ATTR_N_FLOATS_PER = 3;
    public final static int ATTR_T_FLOATS_PER = 2;
    public final static int ATTR_SZ_FLOATS = ATTR_V_FLOATS_PER + ATTR_N_FLOATS_PER + ATTR_T_FLOATS_PER;
    public final static int ATTR_SZ_BYTES = ATTR_SZ_FLOATS * FL_SIZE;
    public final static int ATTR_V_OFFSET_BYTES = 0;
    public final static int ATTR_V_OFFSET_FLOATS = 0;
    public final static int ATTR_N_OFFSET_FLOATS = ATTR_V_FLOATS_PER;
    public final static int ATTR_N_OFFSET_BYTES = ATTR_N_OFFSET_FLOATS * FL_SIZE;

    ;
    public final static int ATTR_T_OFFSET_FLOATS = ATTR_V_FLOATS_PER + ATTR_N_FLOATS_PER;
    public final static int ATTR_T_OFFSET_BYTES = ATTR_T_OFFSET_FLOATS * FL_SIZE;
    public final static int INDICE_SIZE_BYTES = BufferUtil.SIZEOF_INT;
    // @TODO: Experimental leftovers, apparently the right thing to do
    // is use the STRIDE2_BYTES constants below.  So we should delete
    // the rest of the STRIDE constants soon - but I'm holding off on
    // the offchance something is still wrong with STRIDE2_BYTES.
//     public final static int ATTR_V_STRIDE1_FLOATS = ATTR_SZ_FLOATS - ATTR_V_FLOATS_PER;
//     public final static int ATTR_N_STRIDE1_FLOATS = ATTR_SZ_FLOATS - ATTR_N_FLOATS_PER;
//     public final static int ATTR_T_STRIDE1_FLOATS = ATTR_SZ_FLOATS - ATTR_T_FLOATS_PER;
//     public final static int ATTR_V_STRIDE1_BYTES = ATTR_V_STRIDE1_FLOATS * FL_SIZE;
//     public final static int ATTR_N_STRIDE1_BYTES = ATTR_N_STRIDE1_FLOATS * FL_SIZE;
//     public final static int ATTR_T_STRIDE1_BYTES = ATTR_T_STRIDE1_FLOATS * FL_SIZE;
//     public final static int ATTR_V_STRIDE2_FLOATS = ATTR_SZ_FLOATS;
//     public final static int ATTR_N_STRIDE2_FLOATS = ATTR_SZ_FLOATS;
//     public final static int ATTR_T_STRIDE2_FLOATS = ATTR_SZ_FLOATS;
    public final static int ATTR_V_STRIDE2_BYTES = ATTR_SZ_FLOATS * FL_SIZE;
    public final static int ATTR_N_STRIDE2_BYTES = ATTR_SZ_FLOATS * FL_SIZE;
    public final static int ATTR_T_STRIDE2_BYTES = ATTR_SZ_FLOATS * FL_SIZE;
    private int textId = 0;
    private int verticeAttributesID = 0;      // Vertex Attributes VBO ID
    private int indicesID = 0;      // indice VBO ID
    private int indicesCount = 0;
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public RenderableInterleavedVBO(int textId, int verticeAttributesID, int indicesID, int indicesCount) {
        this.textId = textId;
        this.verticeAttributesID = verticeAttributesID;
        this.indicesID = indicesID;
        this.indicesCount = indicesCount;
    }

    public void render(GL gl) {
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textId);    // Bind The Texture
        if (null != name) {
            Debug.debug(1, "RenderableInterleavedVBO.render: " + name + " bound textid=" + textId);
        }
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, verticeAttributesID);

        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL.GL_FLOAT, ATTR_V_STRIDE2_BYTES, ATTR_V_OFFSET_BYTES);

        gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        gl.glNormalPointer(GL.GL_FLOAT, ATTR_N_STRIDE2_BYTES, ATTR_N_OFFSET_BYTES);

        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glTexCoordPointer(2, GL.GL_FLOAT, ATTR_T_STRIDE2_BYTES, ATTR_T_OFFSET_BYTES);

        gl.glBindBufferARB(GL.GL_ELEMENT_ARRAY_BUFFER_ARB, indicesID);
        gl.glDrawElements(GL.GL_TRIANGLES, indicesCount, GL.GL_UNSIGNED_INT, 0);

        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glDisable(GL.GL_TEXTURE_2D);
    }

    public void destroy(GL gl) {
        // NOTE: We don't delete the textureID - that's managed by the TextureDB instance.
        int[] foo = new int[1];
        foo[0] = verticeAttributesID;
        gl.glDeleteBuffersARB(1, foo, 0);
        foo[0] = indicesID;
        gl.glDeleteBuffersARB(1, foo, 0);
    }
}
