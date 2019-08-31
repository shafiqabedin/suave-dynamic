package suave;

import java.awt.Rectangle;
import java.util.*;
import java.awt.geom.Area;

// The point of this class is to make lookup of atriangles in 2D faster
// - i.e. we have a triangle, we want to see if any triangles are in
// front of it, give us a candidate list of those that _might_ be in
// front.  It's a bit of hack, I'd be better off with a proper quad
// tree that can store boxes.  
public class Spatial {

    double gridCellSize;
    double width;
    double height;
    int iWidth;
    int iHeight;
    double totalListEntries = 0;
    double trianglesAdded = 0;
    int spatialSearchCounter = 1;
    ArrayList<Triangle> grid[][];

    public Spatial(double gridCellSize, double width, double height) {
        this.gridCellSize = gridCellSize;
        this.width = width;
        this.height = height;

        // yes this might have one extra row or column unnecessarily.
        iWidth = (int) ((width / gridCellSize) + 1);
        iHeight = (int) ((height / gridCellSize) + 1);
        grid = new ArrayList[iWidth][iHeight];
        Debug.debug(1, "Spatial.init: Creating spatial grid size " + iWidth + " by " + iHeight);
        for (int loopx = 0; loopx < iWidth; loopx++) {
            for (int loopy = 0; loopy < iHeight; loopy++) {
                grid[loopx][loopy] = new ArrayList<Triangle>(1000);
            }
        }
        Debug.debug(1, "Spatial.init: Done creating spatial grid size " + iWidth + " by " + iHeight);
    }

    // clear the hash.
    public void clear() {
        int empty = 0;
        int notEmpty = 0;
        for (int loopx = 0; loopx < iWidth; loopx++) {
            for (int loopy = 0; loopy < iHeight; loopy++) {
                if (grid[loopx][loopy].size() == 0) {
                    empty++;
                } else {
                    notEmpty++;
                }
                grid[loopx][loopy].clear();
            }
        }
        Debug.debug(1, "Spatial.clear: Empty lists=" + empty + " notEmpty = " + notEmpty + " totalListEntries=" + totalListEntries + " trianglesAdded=" + trianglesAdded + " totalListEntries/trianglesAdded = " + (totalListEntries / trianglesAdded));
        totalListEntries = 0;
        trianglesAdded = 0;
    }

    public void add(Triangle t) {
        trianglesAdded++;
        Rectangle r = t.area.getBounds();
        int leftx = (int) (r.x / gridCellSize);
        int rightx = (int) ((r.x + r.width + gridCellSize - .00001) / gridCellSize);
        int topy = (int) (r.y / gridCellSize);
        int boty = (int) ((r.y + r.height + gridCellSize - .00001) / gridCellSize);
        if (leftx < 0) {
            leftx = 0;
        }
        if (topy < 0) {
            topy = 0;
        }
        if (rightx > iWidth) {
            rightx = iWidth;
        }
        if (boty > iHeight) {
            boty = iHeight;
        }
        for (int loopx = leftx; loopx < rightx; loopx++) {
            for (int loopy = topy; loopy < boty; loopy++) {
                totalListEntries++;
                grid[loopx][loopy].add(t);
            }
        }
    }

    public void search(Area area, ArrayList<Triangle> resultList) {
        spatialSearchCounter++;
        resultList.clear();

        Rectangle r = area.getBounds();
        int leftx = (int) (r.x / gridCellSize);
        int rightx = (int) ((r.x + r.width + gridCellSize - .00001) / gridCellSize);
        int topy = (int) (r.y / gridCellSize);
        int boty = (int) ((r.x + r.width + gridCellSize - .00001) / gridCellSize);
        Triangle t = null;
        for (int loopx = leftx; loopx <= rightx; loopx++) {
            for (int loopy = topy; loopy <= boty; loopy++) {
                if (loopx >= iWidth) {
                    continue;
                }
                if (loopy >= iHeight) {
                    continue;
                }
                if (loopx < 0) {
                    loopx = 0;
                }
                if (loopy < 0) {
                    loopy = 0;
                }

                for (int loopi = 0; loopi < grid[loopx][loopy].size(); loopi++) {
                    t = grid[loopx][loopy].get(loopi);
                    if (t.spatialSearchCounter < spatialSearchCounter) {
                        t.spatialSearchCounter = spatialSearchCounter;
                        resultList.add(t);
                    }
                }
            }
        }
    }
}
