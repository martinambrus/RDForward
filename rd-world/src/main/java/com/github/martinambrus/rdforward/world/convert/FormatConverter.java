package com.github.martinambrus.rdforward.world.convert;

import java.io.File;
import java.io.IOException;

/**
 * Single-step converter between two world formats.
 *
 * Implementations convert world data from {@link #sourceFormat()} to
 * {@link #targetFormat()}. The {@link ConversionRegistry} chains multiple
 * converters to reach formats that are not directly connected.
 */
public interface FormatConverter {

    /** The format this converter reads from. */
    WorldFormat sourceFormat();

    /** The format this converter writes to. */
    WorldFormat targetFormat();

    /**
     * Convert the world at inputPath to the target format, writing to outputPath.
     *
     * @param inputPath  source world file or directory
     * @param outputPath target directory for converted world
     * @param seed       world seed for level.dat (if applicable)
     * @throws IOException if reading or writing fails
     */
    void convert(File inputPath, File outputPath, long seed) throws IOException;
}
