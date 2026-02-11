package com.github.martinambrus.rdforward.world;

import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;

import java.util.Random;

/**
 * Procedural terrain generator targeting Minecraft Alpha's world style.
 *
 * Generation follows the same pipeline as Alpha 1.2.x:
 *
 *   Phase 1 — Base terrain shape     (Perlin noise heightmap + 3D density)
 *   Phase 2 — Cave carving           (worm-based tunnels)
 *   Phase 3 — Surface layer          (grass, dirt, sand, gravel by biome)
 *   Phase 4 — Ore distribution       (veins of coal, iron, gold, diamond, etc.)
 *   Phase 5 — Fluid filling          (water at sea level, lava deep underground)
 *   Phase 6 — Tree placement         (oak trees on grass with clearance checks)
 *   Phase 7 — Lighting               (heightmap + sky/block light propagation)
 *
 * Currently a scaffold — phases 1, 3, 5, and 7 have minimal working
 * implementations that produce playable terrain. Phases 2, 4, and 6 are
 * stubs to be filled with Alpha-accurate logic later.
 *
 * Supports both finite-world mode ({@link #generate}) and chunk-based mode
 * ({@link #generateChunk}) so it works with both the RubyDung client and
 * the Alpha chunk protocol.
 *
 * Thread-safe: each call creates its own Random from deterministic
 * chunk-position-derived seeds, and the shared PerlinNoise is read-only.
 */
public class AlphaWorldGenerator implements WorldGenerator {

    // -- Terrain constants (matching Minecraft Alpha) --

    /** Sea level — water fills all air below this height */
    public static final int SEA_LEVEL = 64;

    /** Bottom bedrock layers are unbreakable */
    private static final int BEDROCK_LAYERS = 5;

    /** Base surface height before noise is applied */
    private static final int BASE_HEIGHT = 64;

    /** Maximum height variation from noise (+/-) */
    private static final int HEIGHT_VARIATION = 20;

    /** Terrain noise frequency — lower = broader hills */
    private static final double TERRAIN_SCALE = 0.01;

    /** Number of noise octaves for terrain — more = finer detail */
    private static final int TERRAIN_OCTAVES = 6;

    /** Amplitude falloff per octave */
    private static final double TERRAIN_PERSISTENCE = 0.5;

    /** Depth of dirt/grass layer above stone */
    private static final int SURFACE_DEPTH = 4;

    // -- Ore generation parameters (vein count per chunk, Y range, vein size) --
    // These match Minecraft Alpha's distribution. Each entry:
    //   { blockId, attemptsPerChunk, minY, maxY, veinSize }

    private static final int[][] ORE_CONFIG = {
        { BlockRegistry.COAL_ORE,     20,  0, 128, 16 },
        { BlockRegistry.IRON_ORE,     20,  0,  64,  8 },
        { BlockRegistry.GOLD_ORE,      2,  0,  32,  8 },
        { BlockRegistry.DIAMOND_ORE,   1,  0,  16,  7 },
        { BlockRegistry.REDSTONE_ORE,  8,  0,  16,  7 },
        { BlockRegistry.LAPIS_ORE,     1,  0,  32,  6 },
    };

    @Override
    public String getName() {
        return "Alpha Terrain";
    }

    @Override
    public boolean supportsChunkGeneration() {
        return true;
    }

    // ========================================================================
    // Finite-world generation (RubyDung-compatible 256x64x256)
    // ========================================================================

    @Override
    public void generate(byte[] blocks, int width, int height, int depth, long seed) {
        PerlinNoise noise = new PerlinNoise(seed);
        Random rng = new Random(seed);

        // Scale sea level proportionally to world height (Alpha=128, RubyDung=64)
        int scaledSeaLevel = height / 2;
        int scaledBaseHeight = height / 2;
        int scaledVariation = height / 6;

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                // Phase 1: terrain height from Perlin noise
                double n = noise.octaveNoise(
                    x * TERRAIN_SCALE, z * TERRAIN_SCALE,
                    TERRAIN_OCTAVES, TERRAIN_PERSISTENCE
                );
                int surfaceY = scaledBaseHeight + (int) (n * scaledVariation);
                surfaceY = Math.max(1, Math.min(height - 1, surfaceY));

                for (int y = 0; y < height; y++) {
                    int idx = WorldGenerator.blockIndex(x, y, z, width, depth);
                    byte block;

                    if (y == 0) {
                        block = (byte) BlockRegistry.BEDROCK;
                    } else if (y < surfaceY - SURFACE_DEPTH) {
                        block = (byte) BlockRegistry.STONE;
                    } else if (y < surfaceY) {
                        block = (byte) BlockRegistry.DIRT;
                    } else if (y == surfaceY) {
                        if (y < scaledSeaLevel - 1) {
                            // Underwater surfaces get sand/gravel
                            block = (byte) BlockRegistry.SAND;
                        } else {
                            block = (byte) BlockRegistry.GRASS;
                        }
                    } else if (y <= scaledSeaLevel) {
                        block = (byte) BlockRegistry.STILL_WATER;
                    } else {
                        block = (byte) BlockRegistry.AIR;
                    }

                    blocks[idx] = block;
                }
            }
        }

        // TODO Phase 2: carveCaves (worm tunnels through stone)
        // TODO Phase 4: addOres (vein placement in stone)
        // TODO Phase 6: placeTrees (oak trees on grass blocks)
    }

    // ========================================================================
    // Chunk-based generation (Alpha-style 16x128x16 chunks)
    // ========================================================================

    @Override
    public AlphaChunk generateChunk(int chunkX, int chunkZ, long seed) {
        AlphaChunk chunk = new AlphaChunk(chunkX, chunkZ);
        PerlinNoise noise = new PerlinNoise(seed);

        // Deterministic per-chunk RNG for feature placement
        long chunkSeed = seed ^ ((long) chunkX * 341873128712L + (long) chunkZ * 132897987541L);
        Random chunkRng = new Random(chunkSeed);

        // Phase 1 — Base terrain shape
        int[] heightMap = shapeTerrainBase(chunk, chunkX, chunkZ, noise);

        // Phase 2 — Cave carving
        carveCaves(chunk, chunkX, chunkZ, seed, chunkRng);

        // Phase 3 — Surface layer (grass, dirt, sand based on height vs sea level)
        buildSurface(chunk, chunkX, chunkZ, heightMap, chunkRng);

        // Phase 4 — Ore distribution
        addOres(chunk, chunkRng);

        // Phase 5 — Fill water at sea level, lava at the bottom
        fillFluids(chunk, heightMap);

        // Phase 6 — Trees and vegetation
        placeTrees(chunk, chunkX, chunkZ, heightMap, chunkRng);

        // Phase 7 — Calculate lighting (heightmap + sky light)
        calculateLighting(chunk, heightMap);

        chunk.setTerrainPopulated(true);
        return chunk;
    }

    // ========================================================================
    // Phase 1: Base terrain shape
    // ========================================================================

    /**
     * Generate the base terrain heightmap using multi-octave Perlin noise
     * and fill the chunk with stone up to the terrain surface.
     *
     * TODO: Replace with Alpha's actual 3D density function that uses
     * two noise fields (terrain shape + terrain detail) interpolated
     * vertically to produce overhangs, floating islands, and varied
     * terrain types. The current implementation uses a simple 2D
     * heightmap which produces smooth hills but no overhangs.
     *
     * @return per-column surface heights (16x16 array, index = z*16+x)
     */
    private int[] shapeTerrainBase(AlphaChunk chunk, int chunkX, int chunkZ, PerlinNoise noise) {
        int[] heightMap = new int[AlphaChunk.WIDTH * AlphaChunk.DEPTH];

        for (int localX = 0; localX < AlphaChunk.WIDTH; localX++) {
            for (int localZ = 0; localZ < AlphaChunk.DEPTH; localZ++) {
                double worldX = chunkX * 16.0 + localX;
                double worldZ = chunkZ * 16.0 + localZ;

                // Multi-octave noise for terrain height
                double n = noise.octaveNoise(
                    worldX * TERRAIN_SCALE, worldZ * TERRAIN_SCALE,
                    TERRAIN_OCTAVES, TERRAIN_PERSISTENCE
                );

                int surfaceY = BASE_HEIGHT + (int) (n * HEIGHT_VARIATION);
                surfaceY = Math.max(1, Math.min(AlphaChunk.HEIGHT - 2, surfaceY));
                heightMap[localZ * AlphaChunk.WIDTH + localX] = surfaceY;

                // Fill bedrock + stone up to surface
                for (int y = 0; y <= surfaceY; y++) {
                    if (y < BEDROCK_LAYERS) {
                        // Bedrock with random holes in upper layers
                        chunk.setBlock(localX, y, localZ, BlockRegistry.BEDROCK);
                    } else {
                        chunk.setBlock(localX, y, localZ, BlockRegistry.STONE);
                    }
                }
            }
        }

        return heightMap;
    }

    // ========================================================================
    // Phase 2: Cave carving
    // ========================================================================

    /**
     * Carve cave systems through existing stone.
     *
     * TODO: Implement Minecraft Alpha's cave algorithm:
     * - Start N random "worm" origins per chunk region (8x8 chunk area)
     * - Each worm travels in a random direction, carving a roughly
     *   circular tunnel cross-section (radius 1-4 blocks)
     * - Worms branch occasionally, creating interconnected systems
     * - Worms that break through to the surface create ravine-like
     *   openings; worms below sea level should not carve into water
     * - Large caves: occasional expanded caverns (radius up to 7)
     * - Carving only replaces stone, not ores or fluids
     */
    private void carveCaves(AlphaChunk chunk, int chunkX, int chunkZ, long seed, Random rng) {
        // TODO: worm-based cave carving
        // Key parameters from Alpha:
        //   - Cave frequency: ~15 worm attempts per 8x8 chunk region
        //   - Worm length: 100-200 blocks
        //   - Tunnel radius: 1.0-3.0 (varies along worm path)
        //   - Branch probability: ~0.25 per segment
        //   - Y range: 8 to sea level
    }

    // ========================================================================
    // Phase 3: Surface layer
    // ========================================================================

    /**
     * Replace the top few blocks of stone with biome-appropriate surface
     * materials (grass + dirt above water, sand + sandstone at beaches).
     *
     * TODO: Implement biome-based surface selection:
     * - Desert biome: sand surface, sandstone subsurface
     * - Taiga biome: grass with snow layer on top
     * - Beach detection: grass→sand transition near sea level
     * - Gravel beaches (random alternate to sand)
     * - Mycelium in mushroom biomes
     *
     * Current implementation uses simple height-based rules:
     * surface at/above sea level = grass+dirt, below = sand+dirt.
     */
    private void buildSurface(AlphaChunk chunk, int chunkX, int chunkZ, int[] heightMap, Random rng) {
        for (int localX = 0; localX < AlphaChunk.WIDTH; localX++) {
            for (int localZ = 0; localZ < AlphaChunk.DEPTH; localZ++) {
                int surfaceY = heightMap[localZ * AlphaChunk.WIDTH + localX];

                // Determine surface block based on height relative to sea level
                int surfaceBlock;
                int subsurfaceBlock;

                if (surfaceY >= SEA_LEVEL) {
                    // Above water — grass on top, dirt below
                    surfaceBlock = BlockRegistry.GRASS;
                    subsurfaceBlock = BlockRegistry.DIRT;
                } else if (surfaceY >= SEA_LEVEL - 3) {
                    // Beach zone — sand with occasional gravel
                    surfaceBlock = rng.nextInt(4) == 0 ? BlockRegistry.GRAVEL : BlockRegistry.SAND;
                    subsurfaceBlock = BlockRegistry.SAND;
                } else {
                    // Deep underwater — dirt/gravel
                    surfaceBlock = BlockRegistry.DIRT;
                    subsurfaceBlock = BlockRegistry.DIRT;
                }

                // Apply surface layer (top block + a few blocks of subsurface)
                if (surfaceY > 0) {
                    chunk.setBlock(localX, surfaceY, localZ, surfaceBlock);
                    for (int d = 1; d <= SURFACE_DEPTH && surfaceY - d > 0; d++) {
                        int y = surfaceY - d;
                        if (chunk.getBlock(localX, y, localZ) == BlockRegistry.STONE) {
                            chunk.setBlock(localX, y, localZ, subsurfaceBlock);
                        }
                    }
                }
            }
        }
    }

    // ========================================================================
    // Phase 4: Ore distribution
    // ========================================================================

    /**
     * Place ore veins throughout the stone layers.
     *
     * TODO: Implement Minecraft Alpha's ore vein algorithm:
     * - For each ore type, attempt N placements per chunk
     * - Each vein is a roughly ellipsoidal cluster of blocks
     * - Vein shape: pick two random points within the chunk column,
     *   walk between them placing ore in a sphere of varying radius
     * - Only replace stone blocks (don't overwrite air, dirt, etc.)
     * - Distribution per chunk (from ORE_CONFIG):
     *     Coal:     20 attempts, Y 0-128, size 16
     *     Iron:     20 attempts, Y 0-64,  size 8
     *     Gold:      2 attempts, Y 0-32,  size 8
     *     Diamond:   1 attempt,  Y 0-16,  size 7
     *     Redstone:  8 attempts, Y 0-16,  size 7
     *     Lapis:     1 attempt,  Y 0-32,  size 6
     */
    private void addOres(AlphaChunk chunk, Random rng) {
        // TODO: ore vein placement
        // For each entry in ORE_CONFIG:
        //   for (int attempt = 0; attempt < attemptsPerChunk; attempt++) {
        //       int centerX = rng.nextInt(16);
        //       int centerY = minY + rng.nextInt(maxY - minY);
        //       int centerZ = rng.nextInt(16);
        //       generateVein(chunk, rng, blockId, centerX, centerY, centerZ, veinSize);
        //   }
    }

    // ========================================================================
    // Phase 5: Fluid filling
    // ========================================================================

    /**
     * Fill air pockets below sea level with water, and deep underground
     * with lava.
     *
     * TODO: Implement proper fluid simulation:
     * - Water fills all air at and below SEA_LEVEL
     * - Lava fills air below Y=11 (matching Alpha's lava lakes)
     * - Cave openings near water should have water flow inward
     * - Lava lakes on the surface (rare, randomly placed)
     *
     * Current implementation does basic sea-level water fill only.
     */
    private void fillFluids(AlphaChunk chunk, int[] heightMap) {
        for (int localX = 0; localX < AlphaChunk.WIDTH; localX++) {
            for (int localZ = 0; localZ < AlphaChunk.DEPTH; localZ++) {
                int surfaceY = heightMap[localZ * AlphaChunk.WIDTH + localX];

                // Fill air between surface and sea level with water
                for (int y = surfaceY + 1; y <= SEA_LEVEL; y++) {
                    if (chunk.getBlock(localX, y, localZ) == BlockRegistry.AIR) {
                        chunk.setBlock(localX, y, localZ, BlockRegistry.STILL_WATER);
                    }
                }

                // TODO: lava at y <= 10 in air pockets (caves)
            }
        }
    }

    // ========================================================================
    // Phase 6: Tree placement
    // ========================================================================

    /**
     * Place trees on valid grass blocks.
     *
     * TODO: Implement Minecraft Alpha's tree placement:
     * - Oak trees: 4-block trunk (LOG) + 2-layer leaf canopy (LEAVES)
     *   Trunk height: 4-6 (random)
     *   Canopy: 2x2 to 4x4 leaves centered on trunk top
     * - Birch trees (later Alpha versions): white bark variant
     * - Placement rules:
     *   - Must be on GRASS block
     *   - Minimum 2 blocks from chunk edge (leaves may cross chunks)
     *   - No overlap with other trees (check canopy area)
     *   - Sufficient vertical clearance (trunk height + 2)
     * - Density: ~2-4 trees per chunk in forest biomes,
     *   0-1 in plains, 0 in desert
     * - Cross-chunk decoration: trees near edges may need to write
     *   leaves into neighboring chunks (handled by terrainPopulated flag)
     */
    private void placeTrees(AlphaChunk chunk, int chunkX, int chunkZ,
                            int[] heightMap, Random rng) {
        // TODO: tree placement
        // Pseudocode:
        //   int treeCount = 2 + rng.nextInt(4);
        //   for (int i = 0; i < treeCount; i++) {
        //       int tx = 2 + rng.nextInt(12);  // avoid chunk edges
        //       int tz = 2 + rng.nextInt(12);
        //       int ty = heightMap[tz * 16 + tx] + 1;
        //       if (canPlaceTree(chunk, tx, ty, tz)) {
        //           placeOakTree(chunk, tx, ty, tz, 4 + rng.nextInt(3));
        //       }
        //   }
    }

    // ========================================================================
    // Phase 7: Lighting
    // ========================================================================

    /**
     * Calculate heightmap-based sky lighting for the chunk.
     *
     * TODO: Implement full Alpha lighting:
     * - Sky light: full brightness (15) for all blocks above heightmap,
     *   propagate downward with attenuation through transparent blocks
     * - Block light: propagate from light-emitting blocks (torches=14,
     *   glowstone=15, lava=15, fire=15, redstone torch=7)
     * - Light propagation: BFS flood-fill from each light source,
     *   decreasing by 1 per block (or more for water/leaves)
     * - Cross-chunk propagation at borders
     *
     * Current implementation sets basic heightmap sky light.
     */
    private void calculateLighting(AlphaChunk chunk, int[] heightMap) {
        byte[] skyLight = chunk.getSkyLight();

        for (int localX = 0; localX < AlphaChunk.WIDTH; localX++) {
            for (int localZ = 0; localZ < AlphaChunk.DEPTH; localZ++) {
                int surfaceY = heightMap[localZ * AlphaChunk.WIDTH + localX];

                // Full sky light (15) for all blocks above the surface
                for (int y = AlphaChunk.HEIGHT - 1; y > surfaceY; y--) {
                    int idx = AlphaChunk.blockIndex(localX, y, localZ);
                    int byteIdx = idx / 2;
                    if ((idx & 1) == 0) {
                        skyLight[byteIdx] = (byte) ((skyLight[byteIdx] & 0xF0) | 15);
                    } else {
                        skyLight[byteIdx] = (byte) ((skyLight[byteIdx] & 0x0F) | (15 << 4));
                    }
                }

                // Blocks at and below surface get no sky light (overwrite the
                // default 0xFF that AlphaChunk constructor sets)
                for (int y = surfaceY; y >= 0; y--) {
                    int idx = AlphaChunk.blockIndex(localX, y, localZ);
                    int byteIdx = idx / 2;
                    if ((idx & 1) == 0) {
                        skyLight[byteIdx] = (byte) (skyLight[byteIdx] & 0xF0);
                    } else {
                        skyLight[byteIdx] = (byte) (skyLight[byteIdx] & 0x0F);
                    }
                }
            }
        }
    }
}
