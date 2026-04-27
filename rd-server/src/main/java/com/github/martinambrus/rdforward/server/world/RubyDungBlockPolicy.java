package com.github.martinambrus.rdforward.server.world;

import com.github.martinambrus.rdforward.api.world.BlockPolicy;
import com.github.martinambrus.rdforward.api.world.BlockType;
import com.github.martinambrus.rdforward.api.world.BlockTypes;

/**
 * RubyDung-specific block policy. RubyDung's vocabulary is just air,
 * grass, and cobblestone (the original game had stone but our migration
 * pipeline already converts pre-existing stone to cobble — see
 * {@code RDServer.migrateRubyDungBlocks}). Anything a plugin or mod
 * tries to place is coerced to one of those three:
 *
 * <ul>
 *   <li>AIR stays AIR — block-break must work normally so players can
 *       remove blocks they placed.</li>
 *   <li>Any non-air block at the world's grass-layer Y becomes
 *       {@link BlockTypes#GRASS} — RubyDung treats the surface row as
 *       grass, regardless of what was placed.</li>
 *   <li>Everywhere else, any non-air block becomes
 *       {@link BlockTypes#COBBLE}.</li>
 * </ul>
 *
 * <p>The grass-layer Y is computed from the world's height in
 * {@code RDServer} (matches {@code RubyDungWorldGenerator}'s surface
 * placement at {@code height * 2 / 3}) and passed in at construction so
 * future world-shape changes don't require touching this class.
 */
public final class RubyDungBlockPolicy implements BlockPolicy {

    private final int grassLayerY;

    public RubyDungBlockPolicy(int grassLayerY) {
        this.grassLayerY = grassLayerY;
    }

    @Override
    public BlockType coerce(int x, int y, int z, BlockType requested) {
        if (requested == null || requested.isAir()) return BlockTypes.AIR;
        if (y == grassLayerY) return BlockTypes.GRASS;
        return BlockTypes.COBBLE;
    }
}
