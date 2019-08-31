package suave;

import javax.media.opengl.GL;

public class RenderableDisplayList implements Renderable {

    int textId = 0;
    private int displayList = 0;

    public RenderableDisplayList(int displayList, int textId) {
        this.displayList = displayList;
        this.textId = textId;
    }

    public void render(GL gl) {
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textId);    // Bind The Texture
        gl.glCallList(displayList);
    }

    public void destroy(GL gl) {
        // @TODO: actually free the display list - I really wnat to
        // deprecate this object though.
    }
}
