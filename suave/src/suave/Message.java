/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

/**
 *
 * @author owens
 */
public class Message {

    Client client;
    Object message;

    public Message(Client client, Object message) {
        this.client = client;
        this.message = message;
    }
}
