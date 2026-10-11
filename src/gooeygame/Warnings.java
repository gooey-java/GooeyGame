package gooeygame;

import java.util.HashSet;
import java.util.Set;

/** Prints each friendly warning only once so the console doesn't flood. */
final class Warnings {
    private static final Set<String> shown = new HashSet<String>();

    private Warnings() { }

    static void once(String key, String message) {
        if (shown.add(key)) {
            System.err.println("[GooeyGame] " + message);
        }
    }
}
