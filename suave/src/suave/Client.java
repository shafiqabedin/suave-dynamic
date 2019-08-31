/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author owens
 */
public class Client implements Runnable {

    private String serverName = null;
    private int serverPort = -1;
    private BlockingQueue<Message> inQueue;
    private Socket mySocket = null;
    private boolean server = false;
    private Thread myThread = new Thread(this);
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    private BlockingQueue<Message> outQueue = new LinkedBlockingQueue<Message>();

    public Client(BlockingQueue<Message> inQueue, Socket socket) {
        this.inQueue = inQueue;
        this.mySocket = socket;
        this.server = true;
    }

    public Client(BlockingQueue<Message> inQueue, String serverName, int serverPort) {
        this.inQueue = inQueue;
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.server = false;
    }

    public void start() {
        myThread.start();
    }

    public void addMessage(Message msg) {
        outQueue.add(msg);
    }

    private boolean setupSocket() {
        if (!server) {
            try {
                mySocket = new Socket(serverName, serverPort);
            } catch (IOException e) {
                //Debug.debug(5, "Client.run: Could not open socket to servername " + serverName + " port " + serverPort + " : " + e);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
                return false;
            }
        }
        try {
            out = new ObjectOutputStream(mySocket.getOutputStream());
            in = new ObjectInputStream(mySocket.getInputStream());
        } catch (IOException e) {
            //Debug.debug(5, "Client.run: exception " + e);
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
            }
            return false;
        }
        return true;
    }

    // @Override
    public void run() {
        sendThread.start();

        while (true) {
            if (!setupSocket()) {
                continue;
            }

            Object o = null;
            boolean done = false;
            int errors = 0;
            do {
                try {
                    //Debug.debug(1, "Client.run: Trying to read object from socket");
                    o = in.readUnshared();
                    //Debug.debug(1, "Client.run: queing object from socket on inqueue, object = " + o.toString());
                    Message msg = new Message(this, o);
                    inQueue.add(msg);
                } catch (ClassNotFoundException e) {
                    //Debug.debug(1, "Client.run: object we read is of unknown class " + e);
                    errors++;
                } catch (EOFException e) {
                    //Debug.debug(1, "Client.run: End of file, server closed");
                    done = true;
                } catch (SocketException e) {
                    //Debug.debug(3, "Client.run: Socket exception: " + e);
                    //done = true;
                    errors++;
                } catch (IOException e) {
                }


            } while (o != null && !done && errors < 4);
            if (o == null) {
                //Debug.debug(3, "Client.run: resetting because reading object from in returned null");
            } else if (done) {
                //Debug.debug(3, "Client.run: resetting because done is true");
            } else if (errors >= 10) {
                //Debug.debug(3, "Client.run: resetting because errors = " + errors + " is >= 4");
            } else {
                //Debug.debug(3, "Client.run: resetting because of unknown reasons");
            }
            if (server) {
                //Debug.debug(3, "Client.run: since this is a client object in the server, run will now return.");
                return;
            } else {
                //Debug.debug(3, "Client.run: since this is a client object in the client, run will try to re-connect.");
            }
        }
    }
    Thread sendThread = new Thread() {

        // @Override
        public void run() {
            while (true) {
                Message msg = null;
                try {
                    msg = outQueue.take();
                    //Debug.debug(1, "Client.sendThread.run: writing object to socket = " + msg.message);
                    out.writeUnshared(msg.message);
                    out.flush();
                    //Debug.debug(1, "Client.sendThread.run: Done writing object to socket = " + msg.message);
                } catch (SocketException e) {
                    //Debug.debug(3, "Client.sendThread.run: closed socket: " + e);
                    //Debug.debug(5, "Client.sendThread.run: dropping object instead of sending to server, o=" + msg.message.toString());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                    }
                } catch (IOException e) {
                    //Debug.debug(5, "Client.sendThread.run: Write failed : " + e);
                    //Debug.debug(5, "Client.sendThread.run: dropping object instead of sending to server, o=" + msg.message.toString());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                    }
                } catch (Exception e) {
                    Debug.debug(5, "Client.sendThread.run: Error sending to server " + e);
                    Debug.debug(5, "Client.sendThread.run: dropping object instead of sending to server, o=" + msg.message.toString());
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    };

    public static void main(String argv[]) {
        JFrame frame = new JFrame("Test server/client");
        JPanel panel = new JPanel();
        final JLabel label = new JLabel();
        panel.add(label);
        frame.add(panel);
        label.setSize(720, 480);
        panel.setSize(720, 480);
        frame.setSize(720, 480);
        frame.addWindowFocusListener(new WindowAdapter() {

            // @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        frame.setVisible(true);
        BlockingQueue<Message> inQueue = new LinkedBlockingQueue<Message>();
        final Client client = new Client(inQueue, "localhost", 6655);
        client.start();
        final Thread runThread = new Thread() {

            public void run() {
                float xPosDefault = 237.0f;
                float yPosDefault = 527.0f;
                float zPosDefault = -95.0f;
                float xRotDefault = -339.0f;
                float yRotDefault = 355.0f;
                float zRotDefault = 0.0f;

                float xPos = xPosDefault;
                float yPos = yPosDefault;
                float zPos = zPosDefault;
                float xRot = xRotDefault;
                float yRot = yRotDefault;
                float zRot = zRotDefault;

                while (true) {
                    PosRot pr = new PosRot();
                    pr.xPos = xPos;
                    pr.yPos = yPos;
                    pr.zPos = zPos;
                    pr.xRot = xRot;
                    pr.yRot = yRot;
                    pr.zRot = zRot;
                    client.addMessage(new Message(null, new CaptureCommand(1, pr)));

                    // attempt to make this synchronous so it doesn't overload the server.
                    //                    try {
                    //                        wait();
                    //                    } catch (InterruptedException ex) {
                    //
                    //                    }
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                    }
                    xRot += 1;
                    xPos += 1;
                }
            }
        };
        runThread.start();
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            Message msg = null;
            try {
                System.err.println("Client.main: read thread calling inQueue.take()");
                msg = inQueue.take();
//                runThread.notifyAll();
                System.err.println("Client.main: read thread returned from calling inQueue.take()");
            } catch (InterruptedException e) {
            }
            if (null == msg) {
                System.err.println("Client.main: read thread msg is null, skipping");
                continue;
            }
            if (msg.message instanceof CaptureReply) {
                final Image img = ((CaptureReply) msg.message).getImage();
                try {
                    javax.swing.SwingUtilities.invokeAndWait(new Runnable() {

                        public void run() {
                            label.setIcon(new ImageIcon(img));
                        }
                    });
                } catch (Exception e) {
                    System.err.println("Client.main: Exception trying to update image on label, e=" + e);
                    e.printStackTrace();
                }
            } else {
                System.err.println("Client.main: read thread msg.message payload of unknown class =" + msg.message.getClass().getName());

            }
        }
    }
}
