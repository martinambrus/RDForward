package com.github.martinambrus.rdforward.server.world;

import com.github.martinambrus.rdforward.api.world.BlockType;
import com.github.martinambrus.rdforward.api.world.BlockTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class RubyDungBlockPolicyTest {

    private static final int GRASS_Y = 42;
    private final RubyDungBlockPolicy policy = new RubyDungBlockPolicy(GRASS_Y);

    @Test
    void airPassesThrough() {
        assertSame(BlockTypes.AIR, policy.coerce(0, 0, 0, BlockTypes.AIR));
        assertSame(BlockTypes.AIR, policy.coerce(0, GRASS_Y, 0, BlockTypes.AIR));
    }

    @Test
    void nullRequestBecomesAir() {
        assertSame(BlockTypes.AIR, policy.coerce(0, 0, 0, null));
    }

    @Test
    void grassLayerForcesGrass() {
        assertSame(BlockTypes.GRASS, policy.coerce(10, GRASS_Y, 10, BlockTypes.STONE));
        assertSame(BlockTypes.GRASS, policy.coerce(10, GRASS_Y, 10, BlockTypes.PLANKS));
        assertSame(BlockTypes.GRASS, policy.coerce(10, GRASS_Y, 10, BlockTypes.TNT));
    }

    @Test
    void everywhereElseBecomesCobble() {
        assertSame(BlockTypes.COBBLE, policy.coerce(10, GRASS_Y - 1, 10, BlockTypes.STONE));
        assertSame(BlockTypes.COBBLE, policy.coerce(10, GRASS_Y + 1, 10, BlockTypes.PLANKS));
        assertSame(BlockTypes.COBBLE, policy.coerce(10, 0, 10, BlockTypes.TNT));
    }

    @Test
    void unknownBlockTypeAlsoCoerces() {
        BlockType unknown = BlockTypes.byId(200); // id outside well-known constants
        assertSame(BlockTypes.GRASS, policy.coerce(0, GRASS_Y, 0, unknown));
        assertSame(BlockTypes.COBBLE, policy.coerce(0, GRASS_Y - 1, 0, unknown));
    }

    @Test
    void grassLayerIsExclusiveToConfiguredY() {
        // y = GRASS_Y - 1 and y = GRASS_Y + 1 are NOT the grass layer.
        assertSame(BlockTypes.COBBLE, policy.coerce(0, GRASS_Y - 1, 0, BlockTypes.DIRT));
        assertSame(BlockTypes.COBBLE, policy.coerce(0, GRASS_Y + 1, 0, BlockTypes.DIRT));
        // Only exactly at GRASS_Y.
        assertSame(BlockTypes.GRASS, policy.coerce(0, GRASS_Y, 0, BlockTypes.DIRT));
    }

    @Test
    void cobbleAndGrassAlsoFollowTheRule() {
        // Rule applies regardless of input — even cobble ON the grass
        // layer becomes grass (consistent with "the surface is always
        // grass in RubyDung" semantics).
        assertSame(BlockTypes.GRASS, policy.coerce(0, GRASS_Y, 0, BlockTypes.COBBLE));
        // Cobble below stays cobble (identity in this case, but for
        // the right reason).
        assertSame(BlockTypes.COBBLE, policy.coerce(0, 0, 0, BlockTypes.COBBLE));
    }
}
