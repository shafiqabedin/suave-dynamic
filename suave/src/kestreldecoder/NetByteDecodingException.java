/*
 * NetByteDecodingException.java
 *
 * Created on August 30, 2007, 9:48 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package kestreldecoder;

public class NetByteDecodingException extends NetByteException {

    /**
    @param msg message to display when Exception.getMessage() is called.
     */
    NetByteDecodingException(String message) {
        super(message);
    }
}
