package com.github.martinambrus.rdforward.world.convert;

import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;
import com.github.martinambrus.rdforward.world.alpha.AlphaLevelFormat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for WorldFormatDetector: verifies auto-detection of
 * RubyDung (original), RubyDung Server, Alpha, and McRegion world formats.
 */
class WorldFormatDetectorTest {

    @TempDir
    File tempDir;

    @Test
    void detectsServerWorldDat() throws IOException {
        int width = 16, height = 64, depth = 16;
        byte[] blocks = new byte[width * height * depth];

        File worldFile = new File(tempDir, "server-world.dat");
        try (DataOutputStream dos = new DataOutputStream(
                new GZIPOutputStream(new FileOutputStream(worldFile)))) {
            dos.writeInt(width);
            dos.writeInt(height);
            dos.writeInt(depth);
            dos.write(blocks);
        }

        assertEquals(WorldFormat.RUBYDUNG_SERVER, WorldFormatDetector.detect(worldFile));
    }

    @Test
    void detectsOriginalRubyDungLevelDat() throws IOException {
        // Original RubyDung: GZip'd raw blocks, no header, 256*64*256 = 4,194,304 bytes
        int w = 256, h = 64, d = 256;
        byte[] blocks = new byte[w * h * d];
        // Fill like original: block 1 below surface
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < d; y++) {
                for (int z = 0; z < h; z++) {
                    int i = (y * h + z) * w + x;
                    blocks[i] = (byte) (y <= d * 2 / 3 ? 1 : 0);
                }
            }
        }

        File levelDat = new File(tempDir, "level.dat");
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(levelDat))) {
            gos.write(blocks);
        }

        assertEquals(WorldFormat.RUBYDUNG, WorldFormatDetector.detect(levelDat));
    }

    @Test
    void detectsAlphaDirectory() throws IOException {
        File alphaDir = new File(tempDir, "alpha-world");
        alphaDir.mkdirs();

        AlphaLevelFormat.saveLevelDat(alphaDir, 0L, 0, 64, 0, 0L, System.currentTimeMillis());

        AlphaChunk chunk = new AlphaChunk(0, 0);
        chunk.setBlock(0, 0, 0, 1);
        AlphaLevelFormat.saveChunk(alphaDir, chunk);

        assertEquals(WorldFormat.ALPHA, WorldFormatDetector.detect(alphaDir));
    }

    @Test
    void detectsMcRegionDirectory() throws IOException {
        File mcrDir = new File(tempDir, "mcregion-world");
        File regionSubDir = new File(mcrDir, "region");
        regionSubDir.mkdirs();

        File mcrFile = new File(regionSubDir, "r.0.0.mcr");
        try (RandomAccessFile raf = new RandomAccessFile(mcrFile, "rw")) {
            raf.write(new byte[8192]);
        }

        assertEquals(WorldFormat.MCREGION, WorldFormatDetector.detect(mcrDir));
    }

    @Test
    void returnsNullForUnknown() {
        File emptyDir = new File(tempDir, "empty");
        emptyDir.mkdirs();

        assertNull(WorldFormatDetector.detect(emptyDir));
    }

    @Test
    void returnsNullForNonExistent() {
        File nonExistent = new File(tempDir, "does-not-exist");
        assertNull(WorldFormatDetector.detect(nonExistent));
    }

    @Test
    void returnsNullForNull() {
        assertNull(WorldFormatDetector.detect(null));
    }

    @Test
    void doesNotDetectNonDatFile() throws IOException {
        File txtFile = new File(tempDir, "readme.txt");
        try (FileOutputStream fos = new FileOutputStream(txtFile)) {
            fos.write("hello".getBytes());
        }

        assertNull(WorldFormatDetector.detect(txtFile));
    }

    @Test
    void doesNotDetectAlphaWithoutLevelDat() throws IOException {
        File alphaDir = new File(tempDir, "alpha-no-leveldat");
        alphaDir.mkdirs();

        AlphaChunk chunk = new AlphaChunk(0, 0);
        AlphaLevelFormat.saveChunk(alphaDir, chunk);

        assertNull(WorldFormatDetector.detect(alphaDir));
    }

    @Test
    void serverFormatNotConfusedWithOriginal() throws IOException {
        // A server-world.dat with header should NOT be detected as original RubyDung
        int width = 32, height = 64, depth = 32;
        byte[] blocks = new byte[width * height * depth];

        File worldFile = new File(tempDir, "test.dat");
        try (DataOutputStream dos = new DataOutputStream(
                new GZIPOutputStream(new FileOutputStream(worldFile)))) {
            dos.writeInt(width);
            dos.writeInt(height);
            dos.writeInt(depth);
            dos.write(blocks);
        }

        // Should detect as server format, not original
        assertEquals(WorldFormat.RUBYDUNG_SERVER, WorldFormatDetector.detect(worldFile));
    }
}
