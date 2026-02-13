package com.github.martinambrus.rdforward.world;

import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;

/**
 * Generates a flat world matching RubyDung's default terrain.
 *
 * The surface is at height * 2/3. Everything below is cobblestone,
 * the surface layer is grass, and everything above is air.
 * This matches RubyDung's hardcoded surface rendering height.
 *
 * Supports both finite-world mode (Classic) and chunk-based generation (Alpha).
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
        AlphaChunk chunk = new AlphaChunk(chunkX, chunkZ);
        // Surface at Y=42, matching the Classic finite-world generator
        // which uses DEFAULT_WORLD_HEIGHT(64) * 2/3 = 42.
        int surfaceY = 42;

        for (int x = 0; x < AlphaChunk.WIDTH; x++) {
            for (int z = 0; z < AlphaChunk.DEPTH; z++) {
                for (int y = 0; y < surfaceY; y++) {
                    chunk.setBlock(x, y, z, BlockRegistry.COBBLESTONE);
                }
                chunk.setBlock(x, surfaceY, z, BlockRegistry.GRASS);
                // Sky light is already initialized to full brightness by AlphaChunk constructor
            }
        }
        chunk.setTerrainPopulated(true);
        return chunk;
    }

    @Override
    public boolean supportsChunkGeneration() {
        return true;
    }
}
