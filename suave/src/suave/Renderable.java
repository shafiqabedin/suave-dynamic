package suave;

import javax.media.opengl.GL;

// @TODO: maybe add init()?  Maybe add 'initted()' or 'readyToDraw()'
// or something for things that load/create themselves in their own
// thread?
public interface Renderable {

    public void render(GL gl);

    public void destroy(GL gl);
}
