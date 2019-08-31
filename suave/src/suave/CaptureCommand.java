/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.io.Serializable;

/**
 *
 * @author owens
 */
public class CaptureCommand implements Serializable {

    int id;
    PosRot posRot;
    boolean saveToFile = false;
    String filename;

    public CaptureCommand(int id, PosRot posRot) {
        this.id = id;
        this.posRot = posRot;
    }

    public CaptureCommand(int id, PosRot posRot, String filename) {
        this.id = id;
        this.posRot = posRot;
        this.filename = filename;
        saveToFile = true;
    }

    public String toString() {
        return "CaptureCommand: id=" + id + ", posRot = " + posRot;
    }
}
