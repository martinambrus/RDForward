package com.github.martinambrus.rdforward.world;

import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;

/**
 * Faithful port of the original RubyDung (rd-132211) terrain generator.
 *
 * The original Level constructor fills every block below height*2/3 with
 * block type 1 (solid) and everything above with 0 (air), producing a
 * perfectly flat world. There is no random height variation, no biomes,
 * and only two block types.
 *
 * When used with a server that speaks the Alpha protocol, block type 1
 * is mapped to {@link BlockRegistry#STONE} so that Alpha clients render
 * the terrain correctly. The surface layer gets grass for visual parity
 * with later versions.
 *
 * This generator only supports finite-world mode.
 */
public class RubyDungWorldGenerator implements WorldGenerator {

    @Override
    public String getName() {
        return "RubyDung Classic";
    }

    @Override
    public void generate(byte[] blocks, int width, int height, int depth, long seed) {
        // Exact replica of com.mojang.rubydung.level.Level constructor:
        //   this.blocks[i] = (byte)(y <= d * 2 / 3 ? 1 : 0);
        //
        // Note: RubyDung calls its vertical axis "depth" and its z-axis
        // "height". In the WorldGenerator interface, "height" is the
        // vertical axis. The surface is at height*2/3 which matches
        // the original's d*2/3 with d=64 -> surface at y=42.
        //
        // The original stores block type 1 for all solid blocks, but
        // the renderer draws grass on the top face and cobblestone on
        // the sides/bottom. We reproduce that visually by using the
        // appropriate block IDs from BlockRegistry.
        int surfaceY = height * 2 / 3;

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                for (int y = 0; y < height; y++) {
                    int idx = WorldGenerator.blockIndex(x, y, z, width, depth);
                    if (y < surfaceY) {
                        blocks[idx] = (byte) BlockRegistry.COBBLESTONE;
                    } else if (y == surfaceY) {
                        blocks[idx] = (byte) BlockRegistry.GRASS;
                    } else {
                        blocks[idx] = (byte) BlockRegistry.AIR;
                    }
                }
            }
        }
    }

    @Override
    public AlphaChunk generateChunk(int chunkX, int chunkZ, long seed) {
        throw new UnsupportedOperationException(
            "RubyDungWorldGenerator does not support chunk-based generation");
    }

    @Override
    public boolean supportsChunkGeneration() {
        return false;
    }
}
