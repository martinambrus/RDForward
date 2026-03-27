package com.github.martinambrus.rdforward.world.convert;

/**
 * Supported world storage formats.
 *
 * Each format represents a distinct on-disk layout for Minecraft world data.
 * The conversion framework uses these values to detect source formats and
 * find conversion paths between formats.
 */
public enum WorldFormat {
    /** Original RubyDung level.dat: GZip'd raw byte[] blocks, no header. Block ID 1 = all solid. */
    RUBYDUNG,
    /** RDForward server-world.dat: GZip'd [magic][version][width][height][depth][blocks]. See {@link ServerWorldHeader}. */
    RUBYDUNG_SERVER,
    /** Per-chunk GZip'd NBT files in base-36 directory tree with level.dat. */
    ALPHA,
    /** 32x32-chunk .mcr region files (Beta 1.3+). */
    MCREGION
}
