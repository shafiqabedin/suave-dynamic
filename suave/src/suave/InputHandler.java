package suave;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

class InputHandler extends KeyAdapter {

    private GLCamera camera;
    private Feed feed;
    private Baker baker;
    private Model model;
    private UAVCamera uavCamera;
    private Renderer renderer;
    private GeoTexture terrainTexture;
    private boolean superUser;

    public InputHandler(GLDisplay display, GLCamera camera, Feed feed, Baker baker, Model model, UAVCamera uavCamera, Renderer renderer, GeoTexture terrainTexture, boolean superUser) {
        this.camera = camera;
        this.feed = feed;
        this.baker = baker;
        this.model = model;
        this.uavCamera = uavCamera;
        this.renderer = renderer;
        this.terrainTexture = terrainTexture;
        this.superUser = superUser;

        // @TODO: Two things - one, this help overlay never actually displays so
        // this is kinda pointless - need to fix that.  And two, it appears to be "out of date".
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "xRotMinus");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "xRotPlus");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "xRotReset");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "yRotPlus");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "yRotMinus");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "yRotReset");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), "zRotPlus");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), "zRotMinus");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), "zRotReset");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), "xPosPlus");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0), "xPosMinus");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_O, 0), "xPosReset");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_J, 0), "yPosPlus");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_K, 0), "yPosMinus");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_L, 0), "yPosReset");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0), "zPosPlus");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, 0), "zPosMinus");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, 0), "zPosReset");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "rotSpeedReset");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, 0), "moveSpeedPlus");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, 0), "moveSpeedMinus");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_0, 0), "moveSpeedReset");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "Reset Texture");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), "Paint video frame");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "Feed Next Frame");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "Feed Previous Frame");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "Feed Next 20th Frame");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "Feed Previous 20th Frame");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "Feed move log line forward (in time) one");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "Feed move log line backward (in time) one");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), "Feed increase XRotFactor (pitch of camera)");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), "Feed decrease XRotFactor (pitch of camera)");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0), "Feed recalculate logline/frame correspondence");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_T, 0), "Feed flip between manual mode and streaming");

        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), "Swap current viewpoint and viewpoint of last frame baked");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), "Turn UAV model rendering on/off");

        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, 0), "Increase UAV Camera Angle");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0), "Decrease UAV Camera Angle");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0), "Increase UAV Y Pos");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0), "Decrease UAV Y Pos");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0), "Increase UAV X Pos");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0), "Decrease UAV Y Pos");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, 0), "Set user camera to southeast viewpoint");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "Turn on/off drawing of units using flags");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "Screen Shot");
        display.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "Follow UAV0");
    }

    public void keyPressed(KeyEvent e) {
        processKeyEvent(e, true);
    }

    public void keyReleased(KeyEvent e) {
        processKeyEvent(e, false);
    }

    private void screenshot() {
        PosRot pr = uavCamera.getPosRot();
        CaptureCommand cc = new CaptureCommand(1, pr);
        Message msg = new Message(null, cc);
        renderer.addMessage(msg);
    }

    private void processKeyEvent(KeyEvent e, boolean pressed) {
        processKeyEventSafe(e, pressed);
        if (superUser) {
            processKeyEventSuperUser(e, pressed);
        }
    }
    boolean controlPressed = false;

    private void processKeyEventSafe(KeyEvent e, boolean pressed) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SHIFT:
                camera.shift(pressed);
                break;
            case KeyEvent.VK_8:
            case KeyEvent.VK_NUMPAD8:
                camera.xRotMinus(pressed);
                break;
            case KeyEvent.VK_I:
            case KeyEvent.VK_NUMPAD5:
                camera.xRotPlus(pressed);
                break;
            case KeyEvent.VK_J:
            case KeyEvent.VK_NUMPAD1:
                camera.xRotReset(pressed);
                break;
            case KeyEvent.VK_U:
            case KeyEvent.VK_NUMPAD4:
                camera.yRotMinus(pressed);
                break;
            case KeyEvent.VK_O:
            case KeyEvent.VK_NUMPAD6:
                camera.yRotPlus(pressed);
                break;
            case KeyEvent.VK_K:
            case KeyEvent.VK_NUMPAD2:
                camera.yRotReset(pressed);
                break;
            case KeyEvent.VK_7:
            case KeyEvent.VK_NUMPAD7:
                camera.zRotPlus(pressed);
                break;
            case KeyEvent.VK_9:
            case KeyEvent.VK_NUMPAD9:
                camera.zRotMinus(pressed);
                break;
            case KeyEvent.VK_L:
            case KeyEvent.VK_NUMPAD3:
                camera.zRotReset(pressed);
                break;
            case KeyEvent.VK_D:
                camera.xPosPlus(pressed);
                break;
            case KeyEvent.VK_A:
                camera.xPosMinus(pressed);
                break;
            case KeyEvent.VK_X:
                camera.xPosReset(pressed);
                break;
            case KeyEvent.VK_Q:
                camera.yPosPlus(pressed);
                break;
            case KeyEvent.VK_E:
                camera.yPosMinus(pressed);
                break;
            case KeyEvent.VK_C:
                camera.yPosReset(pressed);
                break;
            case KeyEvent.VK_S:
                camera.zPosPlus(pressed);
                break;
            case KeyEvent.VK_W:
                camera.zPosMinus(pressed);
                break;
            case KeyEvent.VK_2:
                camera.forwardLevel(pressed);
                break;
            case KeyEvent.VK_1:
                camera.backwardLevel(pressed);
                screenshot();
                break;
            case KeyEvent.VK_Z:
                camera.zPosReset(pressed);
                break;
            case KeyEvent.VK_R:
                camera.rotSpeedReset(pressed);
                break;
            case KeyEvent.VK_UP:
                camera.moveSpeedPlus(pressed);
                break;
            case KeyEvent.VK_DOWN:
                camera.moveSpeedMinus(pressed);
                break;
            case KeyEvent.VK_T:
                camera.moveSpeedReset(pressed);
                break;

            case KeyEvent.VK_SEMICOLON:
                if (null != feed) {
                    feed.plusXRotFactor(pressed);
                }
                break;
            case KeyEvent.VK_QUOTE:
                if (null != feed) {
                    feed.minusXRotFactor(pressed);
                }
                break;

            case KeyEvent.VK_N:
                model.uavRenderingOnOff(pressed);
                break;

            case KeyEvent.VK_DELETE:
                uavCamera.incYPos();
                break;
            case KeyEvent.VK_PAGE_DOWN:
                uavCamera.decYPos();
                break;
            case KeyEvent.VK_HOME:
                uavCamera.incXPos();
                break;
            case KeyEvent.VK_END:
                uavCamera.decXPos();
                break;
            case KeyEvent.VK_BACK_QUOTE:
                if (pressed) {
                    model.toggleDrawUAVs();
                }
                break;
            case KeyEvent.VK_BACK_SLASH:
                if (pressed) {
                    ExtraRenderables.toggleDisplayExtraRenderables();
                }
                break;
            case KeyEvent.VK_3:
                if (pressed) {
                    camera.changeView(pressed);
                }
                break;
        }
    }

    private void processKeyEventSuperUser(KeyEvent e, boolean pressed) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            controlPressed = pressed;
            return;
        }
        if (!controlPressed) {
            return;
        }
        switch (e.getKeyCode()) {
            case KeyEvent.VK_NUMPAD0:
                camera.setToSouthEastViewPoint(pressed);
                break;
            case KeyEvent.VK_Y:
                baker.queueResetTexture();
                break;
            case KeyEvent.VK_V:
                if (null != feed) {
                    feed.paintVideoFrame(pressed);
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (null != feed) {
                    feed.bakeNextFrame(pressed);
                }
                break;
            case KeyEvent.VK_LEFT:
                if (null != feed) {
                    feed.bakePrevFrame(pressed);
                }
                break;
            case KeyEvent.VK_PLUS:
                if (null != feed) {
                    feed.bakeNext20Frame(pressed);
                }
                break;
            case KeyEvent.VK_MINUS:
                if (null != feed) {
                    feed.bakePrev20Frame(pressed);
                }
                break;
            case KeyEvent.VK_OPEN_BRACKET:
                if (null != feed) {
                    feed.loglineForward(pressed);
                }
                break;
            case KeyEvent.VK_CLOSE_BRACKET:
                if (null != feed) {
                    feed.loglineBackward(pressed);
                }
                break;
            case KeyEvent.VK_G:
                if (null != feed) {
                    feed.recalculateLoglineFrame(pressed);
                }
                break;
            case KeyEvent.VK_F:
                if (null != feed) {
                    feed.flipManualMode(pressed);
                }
                break;
            case KeyEvent.VK_B:
                baker.swapViewpoint(pressed);
                break;
            case KeyEvent.VK_INSERT:
                uavCamera.incCameraYAngle();
                break;
            case KeyEvent.VK_PAGE_UP:
                uavCamera.decCameraYAngle();
                break;
            case KeyEvent.VK_P:
                model.flipFlags(pressed);
                break;
            case KeyEvent.VK_1:
                screenshot();
                break;
            case KeyEvent.VK_2:
                model.followUav0(pressed, camera);
                break;
            case KeyEvent.VK_H:
//                if(pressed)terrainTexture.snapshot();
                if (pressed) {
                    new Thread() {

                        public void run() {
                            while (true) {
                                terrainTexture.snapshot();
                                try {
                                    Thread.sleep(10000);
                                } catch (InterruptedException e) {
                                }

                            }
                        }
                    }.start();
                }
                break;

        }
    }
}
