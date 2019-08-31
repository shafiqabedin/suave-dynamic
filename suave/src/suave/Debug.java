package suave;

import java.util.logging.Logger;

public class Debug {

    private static final Logger logger = Logger.getLogger(Debug.class.getName());

    public static void debug(int level, String msg) {
        logger.info(msg);
        System.err.println(level + " : " + msg);
    }
}
