/*
 * NetByteException.java
 *
 * Created on August 30, 2007, 9:51 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package kestreldecoder;

public class NetByteException extends Exception {

    /**
    @param msg message to display when Exception.getMessage() is called.
     */
    NetByteException(String message) {
        super(message);
    }
}
