package com.github.martinambrus.rdforward.server.world;

import com.github.martinambrus.rdforward.api.world.BlockTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockCoercionLogTest {

    @BeforeEach
    @AfterEach
    void wipe() {
        BlockCoercionLog.resetForTests();
    }

    @Test
    void firstCoercionIsRecordedSubsequentSilent() {
        assertFalse(BlockCoercionLog.hasLogged("overworld", BlockTypes.PLANKS));
        BlockCoercionLog.logOnce("overworld", BlockTypes.PLANKS, BlockTypes.COBBLE);
        assertTrue(BlockCoercionLog.hasLogged("overworld", BlockTypes.PLANKS));
        // Second invocation is a no-op as far as the dedup state is
        // concerned — hasLogged stays true.
        BlockCoercionLog.logOnce("overworld", BlockTypes.PLANKS, BlockTypes.COBBLE);
        assertTrue(BlockCoercionLog.hasLogged("overworld", BlockTypes.PLANKS));
    }

    @Test
    void differentWorldsDedupIndependently() {
        BlockCoercionLog.logOnce("overworld", BlockTypes.PLANKS, BlockTypes.COBBLE);
        assertTrue(BlockCoercionLog.hasLogged("overworld", BlockTypes.PLANKS));
        // A different world has its own dedup state.
        assertFalse(BlockCoercionLog.hasLogged("mining", BlockTypes.PLANKS));
        BlockCoercionLog.logOnce("mining", BlockTypes.PLANKS, BlockTypes.COBBLE);
        assertTrue(BlockCoercionLog.hasLogged("mining", BlockTypes.PLANKS));
    }

    @Test
    void differentSourceBlocksDedupIndependently() {
        BlockCoercionLog.logOnce("overworld", BlockTypes.PLANKS, BlockTypes.COBBLE);
        assertTrue(BlockCoercionLog.hasLogged("overworld", BlockTypes.PLANKS));
        assertFalse(BlockCoercionLog.hasLogged("overworld", BlockTypes.TNT));
    }

    @Test
    void nullBlockTypeIsSafeNoop() {
        BlockCoercionLog.logOnce("overworld", null, BlockTypes.COBBLE);
        BlockCoercionLog.logOnce("overworld", BlockTypes.PLANKS, null);
        assertFalse(BlockCoercionLog.hasLogged("overworld", null));
    }

    @Test
    void blankWorldNameFallsBackToUnknownKey() {
        BlockCoercionLog.logOnce("", BlockTypes.PLANKS, BlockTypes.COBBLE);
        BlockCoercionLog.logOnce(null, BlockTypes.PLANKS, BlockTypes.COBBLE);
        // Both fold into the "<unknown>" bucket; second call is dedup'd.
        assertTrue(BlockCoercionLog.hasLogged("<unknown>", BlockTypes.PLANKS));
    }
}
