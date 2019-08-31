/*
 * NetByteEncodingException.java
 *
 * Created on August 30, 2007, 9:50 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package kestreldecoder;

public class NetByteEncodingException extends NetByteException {

    /**
    @param msg message to display when Exception.getMessage() is called.
     */
    NetByteEncodingException(String message) {
        super(message);
    }
}
