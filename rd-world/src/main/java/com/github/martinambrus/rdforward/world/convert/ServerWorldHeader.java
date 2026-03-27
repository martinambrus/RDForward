package com.github.martinambrus.rdforward.world.convert;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Header of a server-world.dat file. Contains format version and world dimensions.
 *
 * Versioned files start with {@link #FORMAT_MAGIC} followed by a version int.
 * Legacy files (pre-versioning) start directly with the width int; these are
 * treated as {@link #FORMAT_V1_FINITE}.
 */
public final class ServerWorldHeader {

    /**
     * Magic number at the start of versioned server-world.dat files.
     * Chosen so it can never be confused with an old-format width int
     * (old files start with width=256, i.e. 0x00000100).
     */
    public static final int FORMAT_MAGIC = 0x52444657; // "RDFW"

    /** Format version 1: finite RubyDung/Classic world (flat block array). */
    public static final int FORMAT_V1_FINITE = 1;

    /** The latest format version written by the current code. */
    public static final int CURRENT_FORMAT_VERSION = FORMAT_V1_FINITE;

    public final int formatVersion;
    public final int width;
    public final int height;
    public final int depth;

    public ServerWorldHeader(int formatVersion, int width, int height, int depth) {
        this.formatVersion = formatVersion;
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    /**
     * Read the header from an already-opened DataInputStream.
     * Handles both versioned (magic+version+dims) and legacy (dims only) formats.
     * After this call the stream is positioned at the start of the block data.
     */
    public static ServerWorldHeader read(DataInputStream dis) throws IOException {
        int first = dis.readInt();
        int formatVersion;
        int width;
        if (first == FORMAT_MAGIC) {
            formatVersion = dis.readInt();
            width = dis.readInt();
        } else {
            formatVersion = FORMAT_V1_FINITE;
            width = first;
        }
        int height = dis.readInt();
        int depth = dis.readInt();
        return new ServerWorldHeader(formatVersion, width, height, depth);
    }

    /**
     * Read just the format version from a server-world.dat file without loading
     * the full world. Returns the format version, or -1 if the file cannot be
     * read or is not a valid server-world.dat.
     */
    public static int readFormatVersion(File file) {
        if (file == null || !file.exists()) return -1;
        try (DataInputStream dis = new DataInputStream(
                new GZIPInputStream(new FileInputStream(file)))) {
            int first = dis.readInt();
            if (first == FORMAT_MAGIC) {
                return dis.readInt();
            }
            return FORMAT_V1_FINITE;
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Write a versioned header (magic + current version + dimensions) to the stream.
     */
    public static void write(java.io.DataOutputStream dos, int width, int height, int depth)
            throws IOException {
        dos.writeInt(FORMAT_MAGIC);
        dos.writeInt(CURRENT_FORMAT_VERSION);
        dos.writeInt(width);
        dos.writeInt(height);
        dos.writeInt(depth);
    }
}
