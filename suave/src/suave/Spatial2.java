package suave;

import java.awt.Rectangle;
import java.util.*;
import java.awt.geom.Area;

public class Spatial2 {

    double gridCellSize;
    double width;
    double height;
    int iWidth;
    int iHeight;
    double adds = 0;
    double triangles = 0;

    private class Bucket {

        double leftx;
        double rightx;
        double topy;
        double boty;
        ArrayList<Triangle> list = new ArrayList<Triangle>(100);
    }
    ArrayList<Triangle> grid[][];

    public Spatial2(double gridCellSize, double width, double height) {
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
        Debug.debug(1, "Spatial.clear: Empty lists=" + empty + " notEmpty = " + notEmpty + " adds=" + adds + " triangles=" + triangles + " avg adds per triangle = " + (adds / triangles));
        adds = 0;
        triangles = 0;
    }

    public void add(Triangle t) {
        triangles++;
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
                adds++;
                grid[loopx][loopy].add(t);
            }
        }
    }
    HashSet<Triangle> foundSet = new HashSet<Triangle>();

    public void search(Area area, ArrayList<Triangle> resultList) {
        resultList.clear();
        foundSet.clear();
        Rectangle r = area.getBounds();
        int leftx = (int) (r.x / gridCellSize);
        int rightx = (int) ((r.x + r.width + gridCellSize - .00001) / gridCellSize);
        int topy = (int) (r.y / gridCellSize);
        int boty = (int) ((r.x + r.width + gridCellSize - .00001) / gridCellSize);
        for (int loopx = leftx; loopx <= rightx; loopx++) {
            for (int loopy = topy; loopy <= boty; loopy++) {
                if (loopx >= iWidth) {
                    continue;
                }
                if (loopy >= iHeight) {
                    continue;
                }
                foundSet.addAll(grid[loopx][loopy]);
                //		resultList.addAll(grid[loopx][loopy]);
            }
        }
        resultList = new ArrayList(foundSet);
    }
}
