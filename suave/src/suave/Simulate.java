/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import suave.StateEnums.StateType;

/**
 *
 * @author owens
 */
public class Simulate implements Runnable {

    private final static int SIMULATE_SLEEP_TIME = 5000; // Real big to slow things down for testing, later make it much smaller
    private Baker baker;
    private StateDB stateDB;
    private BlockingQueue<Message> inQueue;
    private Client client;
    private Thread myThread;
    final Thread inQueueThread = new Thread() {

        // @override
        public void run() {

            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                Message msg = null;
                try {
                    Debug.debug(1, "Simulate.inQueueThread.run: read thread calling inQueue.take()");
                    msg = inQueue.take();
                    Debug.debug(1, "Simulate.inQueueThread.run: read thread returned from calling inQueue.take()");
                } catch (InterruptedException e) {
                }
                if (null == msg) {
                    Debug.debug(1, "Simulate.inQueueThread.run: read thread msg is null, skipping");
                    continue;
                }
                if (msg.message instanceof CaptureReply) {
                    CaptureReply cr = (CaptureReply) msg.message;
                    CaptureCommand cc = cr.captureCommand;
                    VideoFrame videoFrame = new VideoFrame();

                    videoFrame.timeMs = -1;
                    videoFrame.frameIndex = -1;

                    videoFrame.x = cc.posRot.xPos;
                    videoFrame.y = cc.posRot.yPos;
                    videoFrame.z = cc.posRot.zPos;
                    videoFrame.xRot = cc.posRot.xRot;
                    videoFrame.yRot = cc.posRot.yRot;
                    videoFrame.zRot = cc.posRot.zRot;
                    videoFrame.img = ((CaptureReply) msg.message).getImage();

                    baker.queueCommand(new BakerCommand(videoFrame));
                } else {
                    Debug.debug(1, "Simulate.inQueueThread.run: read thread msg.message payload of unknown class =" + msg.message.getClass().getName());

                }
            }
        }
    };

    public Simulate(Baker baker, StateDB stateDB, String serverName, int serverPort) {
        this.baker = baker;
        this.stateDB = stateDB;
        inQueue = new LinkedBlockingQueue<Message>();
        client = new Client(inQueue, serverName, serverPort);
        myThread = new Thread(this);
    }

    public void start() {
        client.start();
        inQueueThread.start();
        myThread.start();
    }

    public void run() {
        while (true) {
            try {
                Thread.sleep(SIMULATE_SLEEP_TIME);
            } catch (InterruptedException e) {
            }
            checkStateUpdates();
        }
    }

    private void checkStateUpdates() {
        Debug.debug(1, "Simulate.checkStateUpdate:  Entering.");

        State[] stateAry = stateDB.getStates();
        if (null == stateAry) {
            Debug.debug(1, "Simulate.checkStateUpdate:  null stateAry from stateDB, returning.");
            return;
        }

        for (int loopi = 0; loopi < stateAry.length; loopi++) {
            State state = stateAry[loopi];

            if (null == state) {
                continue;
            }

            String key = null;
            StateType type = StateType.UNKNOWN;

            synchronized (state) {
                // @TODO: THis was cut and pasted from Model, we can't
                // clear the dirty flag both places or there will be problems.
                //                state.setDirty(false);
                key = state.getKey();
                type = state.getType();
            }
            if (type != StateType.UAV) {
//                Debug.debug(1, "Simulate.checkStateUpdate:  Skipping non-UAV state for key= " + key + " type= " + type);
                continue;
            }
            // NOTE: Since SanjayaListener converts everything to OpenGL coords before creating 
            // or updating State objects  we don't need to rearrange anything here - but this
            // does assume client and server both have the same coordinate system, i.e. same origin, etc.
            PosRot pr = new PosRot();
            pr.setPos(state.getXPos(), state.getYPos(), state.getZPos());
            pr.setRot(state.getXRot(), state.getYRot(), state.getZRot());
            // since we have uav position/rotation, add 30 to rotation around x axis to account for camera angle (pointing down)
            pr.xRot += 30;
            CaptureCommand cc = new CaptureCommand(1, pr);
            Debug.debug(1, "Simulate.checkStateUpdate:  Sending capture command = " + cc);
            client.addMessage(new Message(null, cc));
        }
    }

    private class ImagerySource {

        String key;
        PosRot pr = new PosRot();

        public ImagerySource(String key) {
            this.key = key;
        }
    }
}
