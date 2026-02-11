package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.world.BlockRegistry;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Server-authoritative world state.
 *
 * Holds the block array for the entire world. All block modifications
 * must go through this class so the server remains the source of truth.
 *
 * Uses RubyDung's flat array layout: index = (y * depth + z) * width + x.
 * The world dimensions match the original RubyDung default (256x64x256).
 *
 * Thread safety: block get/set are synchronized so the tick loop and
 * Netty I/O threads can safely access world state.
 */
public class ServerWorld {

    private static final String SAVE_FILE = "server-world.dat";

    private final int width;
    private final int height;
    private final int depth;
    private final byte[] blocks;
    private volatile boolean dirty = false;

    public ServerWorld(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.blocks = new byte[width * height * depth];
    }

    /**
     * Generate a simple flat world matching RubyDung's default terrain.
     * RubyDung fills y=0 to y=depth*2/3 as solid and renders the surface
     * texture at exactly y=depth*2/3. We must match this height so the
     * client's hardcoded surface rendering looks correct.
     */
    public void generateFlatWorld() {
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
                    blocks[blockIndex(x, y, z)] = blockType;
                }
            }
        }
    }

    /**
     * Get the block type at the given coordinates.
     * Returns AIR (0) if out of bounds.
     */
    public synchronized byte getBlock(int x, int y, int z) {
        if (!inBounds(x, y, z)) {
            return (byte) BlockRegistry.AIR;
        }
        return blocks[blockIndex(x, y, z)];
    }

    /**
     * Set a block at the given coordinates.
     * Returns true if the block was changed, false if out of bounds or same value.
     */
    public synchronized boolean setBlock(int x, int y, int z, byte blockType) {
        if (!inBounds(x, y, z)) {
            return false;
        }
        int index = blockIndex(x, y, z);
        if (blocks[index] == blockType) {
            return false;
        }
        blocks[index] = blockType;
        dirty = true;
        return true;
    }

    /**
     * Check if coordinates are within world bounds.
     */
    public boolean inBounds(int x, int y, int z) {
        return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
    }

    /**
     * Serialize the world for Classic protocol level transfer.
     *
     * MC Classic world format:
     *   [4 bytes] total volume (int, big-endian)
     *   [width * height * depth bytes] block data
     *   All GZip compressed
     *
     * The block ordering in Classic is: for x, for z, for y — i.e., XZY.
     */
    public byte[] serializeForClassicProtocol() throws IOException {
        int volume = width * height * depth;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos);
             DataOutputStream dos = new DataOutputStream(gzip)) {
            // Write volume as 4-byte big-endian int
            dos.writeInt(volume);
            // Write blocks in XZY order (Classic protocol order)
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    for (int y = 0; y < height; y++) {
                        dos.writeByte(blocks[blockIndex(x, y, z)]);
                    }
                }
            }
        }
        return baos.toByteArray();
    }

    /**
     * Load world from disk. Returns true if a saved world was loaded.
     */
    public boolean load() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            return false;
        }
        try (DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            int savedWidth = dis.readInt();
            int savedHeight = dis.readInt();
            int savedDepth = dis.readInt();
            if (savedWidth != width || savedHeight != height || savedDepth != depth) {
                System.err.println("Saved world dimensions (" + savedWidth + "x" + savedHeight + "x" + savedDepth
                    + ") don't match server (" + width + "x" + height + "x" + depth + "), generating fresh world");
                return false;
            }
            dis.readFully(blocks);
            dirty = false;
            System.out.println("Loaded world from " + SAVE_FILE);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to load world from " + SAVE_FILE + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Save world to disk (GZip compressed).
     */
    public synchronized void save() {
        try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(SAVE_FILE)))) {
            dos.writeInt(width);
            dos.writeInt(height);
            dos.writeInt(depth);
            dos.write(blocks);
            dirty = false;
            System.out.println("World saved to " + SAVE_FILE);
        } catch (IOException e) {
            System.err.println("Failed to save world: " + e.getMessage());
        }
    }

    /**
     * Save only if the world has been modified since last save.
     */
    public void saveIfDirty() {
        if (dirty) {
            save();
        }
    }

    private int blockIndex(int x, int y, int z) {
        return (y * depth + z) * width + x;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getDepth() { return depth; }
}
