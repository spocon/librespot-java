package xyz.gianlu.librespot.debug;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gianlu
 */
public final class TimingsDebugger {
    private final static Logger LOGGER = Logger.getLogger(TimingsDebugger.class);
    private static final Map<String, Long> timings = new HashMap<>();
    private static final int LINE_WIDTH = 100;
    private static boolean enabled;

    public static void start(@NotNull String name) {
        if (!enabled) return;

        if (timings.containsKey(name)) LOGGER.warn(String.format("Key '%s' is already present, overwriting!", name));

        long start = System.nanoTime();
        timings.put(name, start);
    }

    public static void end(@NotNull String name) {
        if (!enabled) return;

        long end = System.nanoTime();

        Long start = timings.remove(name);
        if (start == null) {
            LOGGER.warn(String.format("No start for key %s.", name));
            return;
        }

        long diff = end - start;
        StringBuilder msg = new StringBuilder(String.format(" %s took %.3fms ", name, diff / 1000_000f));
        int length = msg.length();

        msg.insert(0, "<<");
        for (int i = 2; i < (LINE_WIDTH - length) / 2; i++)
            msg.insert(i, '=');

        length = msg.length();
        for (int i = length; i < LINE_WIDTH - 2; i++)
            msg.insert(i, '=');

        msg.append(">>");

        LOGGER.info(msg.toString());
    }

    public static void init(boolean enabled) {
        TimingsDebugger.enabled = enabled;
    }
}
