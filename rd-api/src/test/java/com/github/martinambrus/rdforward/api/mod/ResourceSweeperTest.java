package com.github.martinambrus.rdforward.api.mod;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the central hook dispatcher: registered hooks see every sweep
 * call, exceptions from one hook do not starve the others, and the
 * returned count sums contributions across hooks.
 */
class ResourceSweeperTest {

    @Test
    void sweepInvokesEveryRegisteredHookAndSumsCounts() {
        int baseline = ResourceSweeper.hookCount();
        AtomicInteger hook1Calls = new AtomicInteger();
        AtomicInteger hook2Calls = new AtomicInteger();
        ResourceSweeper.register(modId -> { hook1Calls.incrementAndGet(); return 3; });
        ResourceSweeper.register(modId -> { hook2Calls.incrementAndGet(); return 4; });

        int total = ResourceSweeper.sweep("any-mod");

        // We cannot isolate from other tests' hooks — assert at least our contribution.
        assertTrue(total >= 7, "total must include the 3+4 our hooks returned, got " + total);
        assertEquals(1, hook1Calls.get());
        assertEquals(1, hook2Calls.get());
        assertEquals(baseline + 2, ResourceSweeper.hookCount());
    }

    @Test
    void hookExceptionDoesNotBlockOtherHooks() {
        AtomicInteger survivor = new AtomicInteger();
        ResourceSweeper.register(modId -> { throw new RuntimeException("boom"); });
        ResourceSweeper.register(modId -> { survivor.incrementAndGet(); return 1; });

        ResourceSweeper.sweep("mod-survives-crash");

        assertEquals(1, survivor.get(),
                "a hook that throws must not prevent subsequent hooks from running");
    }
}
