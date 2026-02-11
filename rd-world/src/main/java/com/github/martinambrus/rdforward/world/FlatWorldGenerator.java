package com.github.martinambrus.rdforward.world;

import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;

/**
 * Generates a flat world matching RubyDung's default terrain.
 *
 * The surface is at height * 2/3. Everything below is cobblestone,
 * the surface layer is grass, and everything above is air.
 * This matches RubyDung's hardcoded surface rendering height.
 *
 * This generator only supports finite-world mode (the entire block
 * array is filled in one pass). Chunk-based generation is not supported.
 */
public class FlatWorldGenerator implements WorldGenerator {

    @Override
    public String getName() {
        return "Flat";
    }

    @Override
    public void generate(byte[] blocks, int width, int height, int depth, long seed) {
        int surfaceY = height * 2 / 3;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                for (int y = 0; y < height; y++) {
                    byte blockType;
                    if (y < surfaceY) {
                        blockType = (byte) BlockRegistry.COBBLESTONE;
                    } else if (y == surfaceY) {
                        blockType = (byte) BlockRegistry.GRASS;
                    } else {
                        blockType = (byte) BlockRegistry.AIR;
                    }
                    blocks[WorldGenerator.blockIndex(x, y, z, width, depth)] = blockType;
                }
            }
        }
    }

    @Override
    public AlphaChunk generateChunk(int chunkX, int chunkZ, long seed) {
        throw new UnsupportedOperationException("FlatWorldGenerator does not support chunk-based generation");
    }

    @Override
    public boolean supportsChunkGeneration() {
        return false;
    }
}
