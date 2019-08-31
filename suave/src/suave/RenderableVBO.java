package suave;

import javax.media.opengl.GL;

public class RenderableVBO implements Renderable {

    private int textId = 0;
    private int verticesID = 0;      // Vertices VBO ID
    private int normalsID = 0;      // normals VBO ID
    private int texCoordsID = 0;      // texture coords VBO ID
    private int indicesID = 0;      // indice VBO ID
    private int indicesCount = 0;

    public RenderableVBO(int textId, int verticesID, int normalsID, int texCoordsID, int indicesID, int indicesCount) {
        this.textId = textId;
        this.verticesID = verticesID;
        this.normalsID = normalsID;
        this.texCoordsID = texCoordsID;
        this.indicesID = indicesID;
        this.indicesCount = indicesCount;
    }

    public void render(GL gl) {
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textId);    // Bind The Texture

        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);

        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, texCoordsID);
        gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);     // Set The TexCoord Pointer To The TexCoord Buffer
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, normalsID);
        gl.glNormalPointer(GL.GL_FLOAT, 0, 0);     // Set The Vertex Pointer To The Vertex Buffer
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, verticesID);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);     // Set The Vertex Pointer To The Vertex Buffer

        gl.glDrawElements(GL.GL_TRIANGLES, indicesCount, GL.GL_UNSIGNED_INT, 0);

        gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
        gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
    }

    public void destroy(GL gl) {
        // NOTE: We don't delete the textureID - that's managed by the TextureDB instance.
        int[] foo = new int[1];
        foo[0] = verticesID;
        gl.glDeleteBuffersARB(1, foo, 0);
        foo[0] = normalsID;
        gl.glDeleteBuffersARB(1, foo, 0);
        foo[0] = texCoordsID;
        gl.glDeleteBuffersARB(1, foo, 0);
        foo[0] = indicesID;
        gl.glDeleteBuffersARB(1, foo, 0);
    }
}
