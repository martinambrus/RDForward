package com.github.martinambrus.rdforward.world.alpha;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.*;

import java.io.File;
import java.io.IOException;

/**
 * Reads and writes worlds in the Minecraft Alpha level format.
 *
 * This format was used from Infdev 20100327 through Beta 1.2_02.
 * It stores each chunk as an individual GZip-compressed NBT file
 * distributed across a directory tree using base-36 coordinate hashing.
 *
 * Structure:
 *   world/
 *     level.dat          (GZip'd NBT — world metadata)
 *     session.lock        (8-byte timestamp)
 *     <base36(x%64)>/
 *       <base36(z%64)>/
 *         c.<base36(x)>.<base36(z)>.dat  (GZip'd NBT per chunk)
 */
public class AlphaLevelFormat {

    /**
     * Save a chunk to disk in Alpha format.
     */
    public static void saveChunk(File worldDir, AlphaChunk chunk) throws IOException {
        File chunkFile = getChunkFile(worldDir, chunk.getXPos(), chunk.getZPos());
        chunkFile.getParentFile().mkdirs();

        CompoundTag root = new CompoundTag();
        CompoundTag level = new CompoundTag();

        level.putInt("xPos", chunk.getXPos());
        level.putInt("zPos", chunk.getZPos());
        level.putByte("TerrainPopulated", (byte) (chunk.isTerrainPopulated() ? 1 : 0));
        level.putLong("LastUpdate", chunk.getLastUpdate());
        level.putByteArray("Blocks", chunk.getBlocks());
        level.putByteArray("Data", chunk.getData());
        level.putByteArray("BlockLight", chunk.getBlockLight());
        level.putByteArray("SkyLight", chunk.getSkyLight());
        level.putByteArray("HeightMap", chunk.getHeightMap());
        level.put("Entities", new ListTag<CompoundTag>(CompoundTag.class));
        level.put("TileEntities", new ListTag<CompoundTag>(CompoundTag.class));

        root.put("Level", level);

        NBTUtil.write(new NamedTag("", root), chunkFile);
    }

    /**
     * Load a chunk from disk in Alpha format.
     * Returns null if the chunk file doesn't exist.
     */
    public static AlphaChunk loadChunk(File worldDir, int xPos, int zPos) throws IOException {
        File chunkFile = getChunkFile(worldDir, xPos, zPos);
        if (!chunkFile.exists()) {
            return null;
        }

        NamedTag namedTag = NBTUtil.read(chunkFile);
        CompoundTag root = (CompoundTag) namedTag.getTag();
        CompoundTag level = root.getCompoundTag("Level");

        AlphaChunk chunk = new AlphaChunk(
                level.getInt("xPos"),
                level.getInt("zPos")
        );

        chunk.setTerrainPopulated(level.getByte("TerrainPopulated") == 1);
        chunk.setLastUpdate(level.getLong("LastUpdate"));

        // Copy block data arrays
        byte[] blocks = level.getByteArray("Blocks");
        byte[] data = level.getByteArray("Data");
        byte[] blockLight = level.getByteArray("BlockLight");
        byte[] skyLight = level.getByteArray("SkyLight");
        byte[] heightMap = level.getByteArray("HeightMap");

        System.arraycopy(blocks, 0, chunk.getBlocks(), 0, blocks.length);
        System.arraycopy(data, 0, chunk.getData(), 0, data.length);
        System.arraycopy(blockLight, 0, chunk.getBlockLight(), 0, blockLight.length);
        System.arraycopy(skyLight, 0, chunk.getSkyLight(), 0, skyLight.length);
        System.arraycopy(heightMap, 0, chunk.getHeightMap(), 0, heightMap.length);

        return chunk;
    }

    /**
     * Save the level.dat file with world metadata.
     */
    public static void saveLevelDat(File worldDir, long seed, int spawnX, int spawnY, int spawnZ,
                                    long time, long lastPlayed) throws IOException {
        CompoundTag root = new CompoundTag();
        CompoundTag dataTag = new CompoundTag();

        dataTag.putLong("RandomSeed", seed);
        dataTag.putInt("SpawnX", spawnX);
        dataTag.putInt("SpawnY", spawnY);
        dataTag.putInt("SpawnZ", spawnZ);
        dataTag.putLong("Time", time);
        dataTag.putLong("LastPlayed", lastPlayed);
        dataTag.putLong("SizeOnDisk", 0L);

        root.put("Data", dataTag);

        File levelDat = new File(worldDir, "level.dat");
        NBTUtil.write(new NamedTag("", root), levelDat);
    }

    /**
     * Write the session.lock file (8-byte big-endian timestamp).
     */
    public static void writeSessionLock(File worldDir) throws IOException {
        File sessionLock = new File(worldDir, "session.lock");
        java.io.DataOutputStream dos = new java.io.DataOutputStream(
                new java.io.FileOutputStream(sessionLock));
        try {
            dos.writeLong(System.currentTimeMillis());
        } finally {
            dos.close();
        }
    }

    /**
     * Calculate the file path for a chunk at (xPos, zPos)
     * using the Alpha directory hashing scheme.
     */
    static File getChunkFile(File worldDir, int xPos, int zPos) {
        // Directory names: base36(coordinate % 64)
        // For negative numbers, use unsigned modulo (& 63)
        String dir1 = Integer.toString(xPos & 63, 36);
        String dir2 = Integer.toString(zPos & 63, 36);
        String fileName = "c." + Integer.toString(xPos, 36) + "." + Integer.toString(zPos, 36) + ".dat";

        return new File(worldDir, dir1 + File.separator + dir2 + File.separator + fileName);
    }
}
