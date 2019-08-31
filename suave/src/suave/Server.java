/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author owens
 */
public class Server implements Runnable {

    private int serverPort;
    private Renderer renderer;
    private ServerSocket serverSocket;
    private Thread myThread;
    BlockingQueue<Message> incomingMsgQ;

    public Server(int port, BlockingQueue<Message> incomingMsgQ) {
        this.serverPort = port;
        this.incomingMsgQ = incomingMsgQ;
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            Debug.debug(5, "Server.constructor: Could not open local comms server: " + e);
            System.exit(-1);
        }
        myThread = new Thread(this);
    }

    public void start() {
        myThread.start();
    }

    public void run() {

        Debug.debug(1, "Server.run: waiting for connections.");
        while (true) {
            try {
                Client c = new Client(incomingMsgQ, serverSocket.accept());
                c.start();
            } catch (IOException e) {
                Debug.debug(5, "Server.run: exception " + e);
                break;
            }
        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            Debug.debug(3, "Server.run: Could not close socket.");
        }
    }
}
