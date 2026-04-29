package com.github.martinambrus.rdforward.server.api;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.PlayerManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the contract that the GriefProtection budget-depleted notification
 * fires on the LAST protected placement (the one that consumes the final
 * slot), not on the FIRST unprotected placement that follows. Otherwise
 * the player has already laid an unprotected block before being warned.
 */
class GriefProtectionDepletionTimingTest {

    private CapturingPlayerManager pm;
    private int savedBudget;
    private int savedAccrual;

    @BeforeEach
    void setUp() {
        savedBudget = BlockOwnerRegistry.INITIAL_BUDGET;
        savedAccrual = BlockOwnerRegistry.ACCRUAL_PER_HOUR;
        BlockOwnerRegistry.clearForTesting();
        BlockOwnerRegistry.INITIAL_BUDGET = 3;
        BlockOwnerRegistry.ACCRUAL_PER_HOUR = 0;
        ServerEvents.BLOCK_PLACE.clearListeners();
        ServerEvents.BLOCK_BREAK.clearListeners();
        pm = new CapturingPlayerManager();
        pm.addPlayer("zath", null, null, ProtocolVersion.RELEASE_1_21_5);
        // No serverWorld / chunkManager — block-owner storage is a no-op,
        // which is enough for budget accounting + notification timing.
        GriefProtection.init(0, pm, null, null);
    }

    @AfterEach
    void tearDown() {
        ServerEvents.BLOCK_PLACE.clearListeners();
        ServerEvents.BLOCK_BREAK.clearListeners();
        BlockOwnerRegistry.clearForTesting();
        BlockOwnerRegistry.INITIAL_BUDGET = savedBudget;
        BlockOwnerRegistry.ACCRUAL_PER_HOUR = savedAccrual;
        // Restore the default rate-tracker cap. setUp called init(0, ...)
        // to disable the rate tracker for this scenario; without restoring
        // the static, GriefProtection.RateTracker instances constructed
        // by other tests in the same JVM start with tokens=0 and fail.
        GriefProtection.init(17, pm, null, null);
        GriefProtection.disable();
    }

    @Test
    void depletionMessageFiresOnLastProtectedPlacement() {
        // Place block #1 (used 1/3) — no depletion message.
        ServerEvents.BLOCK_PLACE.invoker().onBlockPlace("zath", 0, 64, 0, 1);
        assertFalse(containsDepletion(pm.chats),
                "depletion message must NOT fire while budget remains; got " + pm.chats);

        // Place block #2 (used 2/3) — still no depletion.
        ServerEvents.BLOCK_PLACE.invoker().onBlockPlace("zath", 1, 64, 0, 1);
        assertFalse(containsDepletion(pm.chats),
                "depletion message must NOT fire while budget remains; got " + pm.chats);

        // Place block #3 (used 3/3) — this is the LAST protected block.
        // Depletion message must fire NOW so the player is warned BEFORE
        // they place an unprotected block.
        ServerEvents.BLOCK_PLACE.invoker().onBlockPlace("zath", 2, 64, 0, 1);
        assertTrue(containsDepletion(pm.chats),
                "depletion message MUST fire on the placement that consumes "
                + "the final slot; got " + pm.chats);
    }

    @Test
    void depletionMessageDoesNotDuplicateOnSubsequentUnprotectedPlacements() {
        // Burn through budget.
        for (int i = 0; i < 3; i++) {
            ServerEvents.BLOCK_PLACE.invoker().onBlockPlace("zath", i, 64, 0, 1);
        }
        int afterDepleting = countDepletionLines(pm.chats);
        assertTrue(afterDepleting >= 1, "expected at least one depletion line after slot 3; got " + pm.chats);

        // Subsequent unprotected placement attempts must be throttled —
        // not re-fire the depletion banner on every block.
        ServerEvents.BLOCK_PLACE.invoker().onBlockPlace("zath", 10, 64, 0, 1);
        ServerEvents.BLOCK_PLACE.invoker().onBlockPlace("zath", 11, 64, 0, 1);
        int afterMorePlacements = countDepletionLines(pm.chats);
        assertTrue(afterMorePlacements == afterDepleting,
                "depletion banner must not duplicate on follow-up placements within the throttle window; "
                + "before=" + afterDepleting + ", after=" + afterMorePlacements + ", chats=" + pm.chats);
    }

    private static boolean containsDepletion(List<String> chats) {
        for (String s : chats) if (s.contains("Budget depleted")) return true;
        return false;
    }

    private static int countDepletionLines(List<String> chats) {
        int n = 0;
        for (String s : chats) if (s.contains("Budget depleted")) n++;
        return n;
    }

    /** PlayerManager subclass that captures every {@link #sendChat} call
     *  so the test can assert the exact placement at which the depletion
     *  banner fires. */
    private static final class CapturingPlayerManager extends PlayerManager {
        final List<String> chats = new ArrayList<>();

        @Override
        public void sendChat(ConnectedPlayer player, String message) {
            chats.add(message);
        }
    }
}
