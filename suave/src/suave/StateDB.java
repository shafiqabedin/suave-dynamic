package suave;

import java.util.*;

public class StateDB {

    private int keySequence = 1;

    private String createKey() {
        synchronized (stateMap) {
            return Integer.toString(keySequence++);
        }
    }
    private HashMap<String, State> stateMap = new HashMap<String, State>();
    private boolean dirty = false;

    public boolean isDirty() {
        synchronized (stateMap) {
            return dirty;
        }
    }

    public StateDB() {
    }

    public void clear() {
        synchronized (stateMap) {
            stateMap.clear();
            dirty = true;
        }
    }

    public State[] getStates() {
        synchronized (stateMap) {
            return stateMap.values().toArray(new State[1]);
        }
    }

    public void remove(State obj) {
        remove(obj.getKey());
    }

    public void remove(String key) {
        synchronized (stateMap) {
            stateMap.remove(key);
            dirty = true;
        }
    }

    public void put(State obj) {
        synchronized (stateMap) {
            stateMap.put(obj.getKey(), obj);
            dirty = true;
        }
    }

    public State get(String key) {
        synchronized (stateMap) {
            return (State) stateMap.get(key);
        }
    }
//     public Iterator getIter() {
//         synchronized(stateMap) {
//             return stateMap.values().iterator();
//         }
//     }
}
