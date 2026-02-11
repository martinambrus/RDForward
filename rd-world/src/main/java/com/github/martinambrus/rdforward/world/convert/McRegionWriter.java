package com.github.martinambrus.rdforward.world.convert;

import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;
import com.github.martinambrus.rdforward.world.alpha.AlphaLevelFormat;

import net.querz.nbt.io.NBTOutputStream;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Deflater;

/**
 * Writes Minecraft chunks to the McRegion (.mcr) file format.
 *
 * McRegion was introduced in Beta 1.3 and stores 32x32 chunks per region
 * file, dramatically reducing the number of filesystem entries compared
 * to Alpha's per-chunk files.
 *
 * Region file layout:
 *   Bytes 0-4095:     Location table (1024 entries x 4 bytes)
 *                      Bits 0-23: offset in 4KB sectors, Bits 24-31: size in sectors
 *   Bytes 4096-8191:  Timestamp table (1024 entries x 4 bytes, Unix seconds)
 *   Bytes 8192+:      Chunk data (each chunk: 4-byte length + 1-byte compression + data)
 *                      Padded to 4KB sector boundaries
 *
 * File naming: r.<regionX>.<regionZ>.mcr
 *   regionX = chunkX >> 5 (floor divide by 32)
 *   regionZ = chunkZ >> 5
 *
 * This class can convert an Alpha-format world directory to McRegion format,
 * or write individual chunks incrementally.
 */
public class McRegionWriter {

    /** Each sector is 4KB */
    private static final int SECTOR_SIZE = 4096;

    /** Header occupies the first 2 sectors (locations + timestamps) */
    private static final int HEADER_SECTORS = 2;

    /** Compression type: 2 = Zlib (deflate) */
    private static final byte COMPRESSION_ZLIB = 2;

    /**
     * Convert an Alpha-format world directory to McRegion format.
     *
     * Scans the Alpha directory tree for all chunk files, groups them
     * by region, and writes .mcr files.
     *
     * @param alphaDir    Alpha world directory (containing base-36 chunk folders)
     * @param outputDir   output directory for .mcr files
     * @throws IOException if reading or writing fails
     */
    public void convertAlphaToRegion(File alphaDir, File outputDir) throws IOException {
        outputDir.mkdirs();

        // Collect all chunk files from Alpha directory tree
        System.out.println("Scanning Alpha world for chunks...");
        List<int[]> chunkCoords = scanAlphaChunks(alphaDir);
        System.out.println("Found " + chunkCoords.size() + " chunks");

        if (chunkCoords.isEmpty()) {
            System.out.println("No chunks found in " + alphaDir.getAbsolutePath());
            return;
        }

        // Group chunks by region
        Map<Long, List<int[]>> regionMap = new HashMap<>();
        for (int[] coord : chunkCoords) {
            int regionX = coord[0] >> 5;
            int regionZ = coord[1] >> 5;
            long key = ((long) regionX << 32) | (regionZ & 0xFFFFFFFFL);
            regionMap.computeIfAbsent(key, k -> new ArrayList<>()).add(coord);
        }

        System.out.println("Writing " + regionMap.size() + " region file(s)...");
        int written = 0;

        for (Map.Entry<Long, List<int[]>> entry : regionMap.entrySet()) {
            int regionX = (int) (entry.getKey() >> 32);
            int regionZ = (int) (entry.getKey() & 0xFFFFFFFFL);
            List<int[]> regionChunks = entry.getValue();

            writeRegionFile(alphaDir, outputDir, regionX, regionZ, regionChunks);
            written += regionChunks.size();
            System.out.println("  Region r." + regionX + "." + regionZ + ".mcr: "
                + regionChunks.size() + " chunks (" + written + "/" + chunkCoords.size() + " total)");
        }

        // Copy level.dat if it exists
        File levelDat = new File(alphaDir, "level.dat");
        if (levelDat.exists()) {
            java.nio.file.Files.copy(levelDat.toPath(),
                new File(outputDir, "level.dat").toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Copied level.dat");
        }

        System.out.println("McRegion conversion complete: " + written + " chunks in "
            + regionMap.size() + " region file(s)");
    }

    /**
     * Write a single .mcr region file containing the given chunks.
     */
    private void writeRegionFile(File alphaDir, File outputDir,
                                  int regionX, int regionZ,
                                  List<int[]> chunkCoords) throws IOException {
        String fileName = "r." + regionX + "." + regionZ + ".mcr";
        File regionFile = new File(outputDir, "region" + File.separator + fileName);
        regionFile.getParentFile().mkdirs();

        // Location table: [offset(3 bytes) | sectorCount(1 byte)] per chunk slot
        int[] offsets = new int[1024];
        int[] sizes = new int[1024];
        int[] timestamps = new int[1024];

        // Compress all chunk data first to know sizes
        byte[][] compressedChunks = new byte[1024][];

        for (int[] coord : chunkCoords) {
            int chunkX = coord[0];
            int chunkZ = coord[1];
            int slot = (chunkX & 31) + (chunkZ & 31) * 32;

            AlphaChunk chunk = AlphaLevelFormat.loadChunk(alphaDir, chunkX, chunkZ);
            if (chunk == null) continue;

            byte[] chunkNbt = serializeChunkNbt(chunk);
            compressedChunks[slot] = chunkNbt;
            timestamps[slot] = (int) (System.currentTimeMillis() / 1000);
        }

        // Calculate sector offsets
        int currentSector = HEADER_SECTORS; // start after the 8KB header
        for (int slot = 0; slot < 1024; slot++) {
            if (compressedChunks[slot] == null) continue;

            // Chunk data on disk: 4-byte length + 1-byte compression type + data
            int dataLength = compressedChunks[slot].length + 5;
            int sectorCount = (dataLength + SECTOR_SIZE - 1) / SECTOR_SIZE;

            offsets[slot] = currentSector;
            sizes[slot] = sectorCount;
            currentSector += sectorCount;
        }

        // Write the region file
        try (RandomAccessFile raf = new RandomAccessFile(regionFile, "rw")) {
            // Write location table (4 bytes per slot)
            for (int slot = 0; slot < 1024; slot++) {
                if (offsets[slot] == 0) {
                    raf.writeInt(0);
                } else {
                    // Pack: offset in top 24 bits, sector count in bottom 8 bits
                    raf.writeInt((offsets[slot] << 8) | (sizes[slot] & 0xFF));
                }
            }

            // Write timestamp table (4 bytes per slot)
            for (int slot = 0; slot < 1024; slot++) {
                raf.writeInt(timestamps[slot]);
            }

            // Write chunk data at the correct sector offsets
            for (int slot = 0; slot < 1024; slot++) {
                if (compressedChunks[slot] == null) continue;

                // Seek to the correct sector position
                long sectorOffset = (long) offsets[slot] * SECTOR_SIZE;
                raf.seek(sectorOffset);

                // Write: length (4 bytes) + compression type (1 byte) + data
                int length = compressedChunks[slot].length + 1; // +1 for compression byte
                raf.writeInt(length);
                raf.writeByte(COMPRESSION_ZLIB);
                raf.write(compressedChunks[slot]);

                // Pad to sector boundary
                int dataSize = 4 + 1 + compressedChunks[slot].length;
                int padding = sizes[slot] * SECTOR_SIZE - dataSize;
                if (padding > 0) {
                    raf.write(new byte[padding]);
                }
            }
        }
    }

    /**
     * Serialize a chunk's NBT data and compress with Zlib (deflate).
     * Returns the compressed bytes (without the length/compression header).
     */
    private byte[] serializeChunkNbt(AlphaChunk chunk) throws IOException {
        // Build the same NBT structure as AlphaLevelFormat.saveChunk
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

        ListTag<CompoundTag> entitiesTag = new ListTag<>(CompoundTag.class);
        for (int i = 0; i < chunk.getEntities().size(); i++) {
            entitiesTag.add(chunk.getEntities().get(i).toNbt());
        }
        level.put("Entities", entitiesTag);

        ListTag<CompoundTag> tileEntitiesTag = new ListTag<>(CompoundTag.class);
        for (int i = 0; i < chunk.getTileEntities().size(); i++) {
            tileEntitiesTag.add(chunk.getTileEntities().get(i).toNbt());
        }
        level.put("TileEntities", tileEntitiesTag);

        root.put("Level", level);

        // Serialize NBT to uncompressed bytes
        ByteArrayOutputStream nbtBytes = new ByteArrayOutputStream();
        try (NBTOutputStream nbtOut = new NBTOutputStream(nbtBytes)) {
            nbtOut.writeTag(new NamedTag("", root), 512);
        }

        // Compress with zlib (deflate)
        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(compressed, new Deflater(Deflater.BEST_SPEED));
        dos.write(nbtBytes.toByteArray());
        dos.close();

        return compressed.toByteArray();
    }

    /**
     * Scan an Alpha world directory tree and return all chunk coordinates found.
     * Walks the base-36 hashed directory structure.
     */
    private List<int[]> scanAlphaChunks(File worldDir) {
        List<int[]> coords = new ArrayList<>();

        File[] level1Dirs = worldDir.listFiles(File::isDirectory);
        if (level1Dirs == null) return coords;

        for (File dir1 : level1Dirs) {
            // Skip known non-chunk directories
            if (dir1.getName().equals("region") || dir1.getName().equals("players")) continue;

            File[] level2Dirs = dir1.listFiles(File::isDirectory);
            if (level2Dirs == null) continue;

            for (File dir2 : level2Dirs) {
                File[] chunkFiles = dir2.listFiles((d, name) ->
                    name.startsWith("c.") && name.endsWith(".dat"));
                if (chunkFiles == null) continue;

                for (File cf : chunkFiles) {
                    int[] parsed = parseChunkFileName(cf.getName());
                    if (parsed != null) {
                        coords.add(parsed);
                    }
                }
            }
        }

        return coords;
    }

    /**
     * Parse chunk coordinates from an Alpha chunk filename.
     * Format: c.<base36(x)>.<base36(z)>.dat
     */
    private int[] parseChunkFileName(String name) {
        // Strip "c." prefix and ".dat" suffix
        if (!name.startsWith("c.") || !name.endsWith(".dat")) return null;
        String inner = name.substring(2, name.length() - 4);
        int dot = inner.indexOf('.');
        if (dot < 0) return null;

        try {
            int x = Integer.parseInt(inner.substring(0, dot), 36);
            int z = Integer.parseInt(inner.substring(dot + 1), 36);
            return new int[]{x, z};
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
