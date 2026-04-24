package com.github.martinambrus.rdforward.api.mod;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Central registry of per-mod resource sweep hooks. Subsystems that hold
 * mod-owned state (e.g. rd-client's {@code KeyBindingRegistry},
 * {@code OverlayRegistry}) register a function here so the mod loader can
 * drop everything a mod owns when it is disabled or reloaded — without
 * rd-mod-loader having a compile-time dependency on rd-client.
 *
 * <p>Each hook accepts a mod id and returns the number of entries it removed.
 * Hooks are invoked once per disable from {@link #sweep(String)}.
 */
public final class ResourceSweeper {

    private ResourceSweeper() {}

    private static final List<ToIntFunction<String>> HOOKS = new ArrayList<>();

    /** Register a sweep hook. Called from a subsystem's static initializer. */
    public static synchronized void register(ToIntFunction<String> hook) {
        HOOKS.add(hook);
    }

    /** Invoke every registered hook with {@code modId}; sum the counts. */
    public static int sweep(String modId) {
        int total = 0;
        List<ToIntFunction<String>> snapshot;
        synchronized (ResourceSweeper.class) {
            snapshot = new ArrayList<>(HOOKS);
        }
        for (ToIntFunction<String> h : snapshot) {
            try {
                total += h.applyAsInt(modId);
            } catch (RuntimeException ignored) {
                // a buggy hook must not block other sweepers
            }
        }
        return total;
    }

    /** Number of registered hooks — test-only. */
    public static synchronized int hookCount() {
        return HOOKS.size();
    }
}
