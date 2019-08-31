package suave;

import java.util.*;

public class FrameList {

    private HashMap<Integer, String> frameMap = null;
    private ArrayList<Integer> frameIndexList = null;

    public FrameList() {
    }

    public void addFrame(VideoFrame frame) {
        if (null == frameMap) {
            frameMap = new HashMap<Integer, String>();
            frameIndexList = new ArrayList<Integer>();
        }
        if (null == frameMap.get(frame.frameIndex)) {
            frameMap.put(frame.frameIndex, frame.filename);
            frameIndexList.add(frame.frameIndex);
        }
    }

    public ArrayList<Integer> getFrameList() {
        if (null == frameIndexList) {
            return null;
        }
        return frameIndexList;
// 	ArrayList<Integer> keyList = new ArrayList<Integer>(frameMap.keySet());
// 	Collections.sort(keyList);
// 	ArrayList<String> fileNameList = new ArrayList<String>(keyList.size());
// 	for(Integer key: keyList) {
// 	    fileNameList.add(frameMap.get(key));
// 	}
// 	return fileNameList;
    }
}
