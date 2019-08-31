package suave;

public class BakerCommand {

    public static enum Type {

        NONE, BAKE_FRAME, RESET_TEXTURE, SWAP_VIEWPOINT
    };
    Type type = Type.NONE;
    VideoFrame videoFrame = null;

    public BakerCommand() {
    }

    public BakerCommand(VideoFrame videoFrame) {
        this.videoFrame = videoFrame;
        type = Type.BAKE_FRAME;
    }
}
