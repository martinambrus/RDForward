package com.github.martinambrus.rdforward.world;

import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;

/**
 * Pluggable world generation interface.
 *
 * Supports two generation modes:
 *
 * 1. Finite-world generation: fills a flat byte array in one pass.
 *    Used by RubyDung-style worlds with fixed dimensions (e.g., 256x64x256).
 *    The server calls {@link #generate} once at world creation time.
 *
 * 2. Chunk-based generation: produces individual 16x128x16 chunks on demand.
 *    Used by Alpha-style infinite worlds where chunks are generated as
 *    players explore. The server calls {@link #generateChunk} per chunk.
 *
 * A generator supports one or both modes. Use {@link #supportsChunkGeneration}
 * to check which mode is available. Future Alpha generators will implement
 * chunk-based generation using Minecraft Alpha's actual terrain algorithms
 * (Perlin noise terrain, caves, ores, trees, etc.).
 *
 * Implementations must be thread-safe for chunk generation (the server may
 * request multiple chunks concurrently from I/O threads).
 */
public interface WorldGenerator {

    /**
     * Human-readable name (e.g., "Flat", "RubyDung Classic", "Alpha Terrain").
     */
    String getName();

    /**
     * Generate terrain into the given block array for a finite-size world.
     *
     * Block array uses ServerWorld's YZX ordering:
     *   index = (y * depth + z) * width + x
     *
     * @param blocks the block array to fill (pre-allocated, zeroed)
     * @param width  world width (X axis)
     * @param height world height (Y axis)
     * @param depth  world depth (Z axis)
     * @param seed   world seed for reproducible generation
     */
    void generate(byte[] blocks, int width, int height, int depth, long seed);

    /**
     * Generate a single 16x128x16 chunk for infinite-world mode.
     *
     * Only called if {@link #supportsChunkGeneration} returns true.
     * The returned chunk should have its block, metadata, and light
     * arrays populated. Entities and tile entities may also be added.
     *
     * @param chunkX chunk X coordinate (block X / 16)
     * @param chunkZ chunk Z coordinate (block Z / 16)
     * @param seed   world seed for reproducible generation
     * @return the generated chunk
     * @throws UnsupportedOperationException if chunk generation is not supported
     */
    AlphaChunk generateChunk(int chunkX, int chunkZ, long seed);

    /**
     * Whether this generator supports chunk-based (infinite) world generation.
     *
     * If true, the server uses {@link #generateChunk} for on-demand chunk loading.
     * If false, the server uses {@link #generate} to fill the entire world at startup.
     */
    boolean supportsChunkGeneration();

    /**
     * Calculate the block array index for ServerWorld's YZX ordering.
     * Provided as a convenience so generators don't need to duplicate the formula.
     */
    static int blockIndex(int x, int y, int z, int width, int depth) {
        return (y * depth + z) * width + x;
    }
}
