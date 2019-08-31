/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package suave;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author sha33
 */
public class DynamoHashmap {

    private static HashMap dynamoHashmap = new HashMap();
    public int framesIdx = 0;

    public ArrayList getdynamoImageArray(double[] ogl) {
        // Get a set of the entries
        Set set = dynamoHashmap.entrySet();
// Get an iterator
        Iterator i = set.iterator();
        ArrayList fileArray = new ArrayList();
// Display elements
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry) i.next();
            Path2D path = new Path2D.Double();
            float[] coords = (float[]) me.getValue();
            path.moveTo(coords[0], coords[2]);
            //create path
            path.lineTo(coords[0], coords[2]);
            path.lineTo(coords[6], coords[8]);
            path.lineTo(coords[3], coords[5]);
            path.lineTo(coords[9], coords[11]);

            Debug.debug(4, "DynamoHashmap : " + coords[0] + " - " + coords[2]);

            if (path.contains(ogl[0], ogl[2])) {
                fileArray.add(me.getKey());
            }


//            Debug.debug(4, "DynamoHashmap : " + me.getKey());
        }
        return fileArray;
    }

    public void setdynamoHashmap(String filename, float minX_0, float minX_1, float minX_2, float maxX_0, float maxX_1, float maxX_2, float minZ_0, float minZ_1, float minZ_2, float maxZ_0, float maxZ_1, float maxZ_2) {
//        dynamoHashmap.put("John Doe", new Double(3434.34));
        float[] coordinates = {minX_0, minX_1, minX_2, maxX_0, maxX_1, maxX_2, minZ_0, minZ_1, minZ_2, maxZ_0, maxZ_1, maxZ_2};
        dynamoHashmap.put(filename, coordinates);
        framesIdx++;

    }

    public int getdynamoHashIndex() {
        return framesIdx;
    }
}
