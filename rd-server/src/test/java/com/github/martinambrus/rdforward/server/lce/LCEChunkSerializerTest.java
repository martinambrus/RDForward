package com.github.martinambrus.rdforward.server.lce;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LCE RLE encoding/decoding and compression pipeline.
 */
class LCEChunkSerializerTest {

    // === RLE Round-Trip Tests ===

    @Test
    void rleRoundTripAllZeros() {
        byte[] input = new byte[1024];
        assertRleRoundTrip(input);
    }

    @Test
    void rleRoundTripAllOnes() {
        byte[] input = new byte[512];
        Arrays.fill(input, (byte) 1);
        assertRleRoundTrip(input);
    }

    @Test
    void rleRoundTripAll0xFF() {
        byte[] input = new byte[256];
        Arrays.fill(input, (byte) 0xFF);
        assertRleRoundTrip(input);
    }

    @Test
    void rleRoundTripMixed() {
        byte[] input = new byte[300];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 256);
        }
        assertRleRoundTrip(input);
    }

    @Test
    void rleRoundTripSingle0xFF() {
        byte[] input = new byte[]{(byte) 0xFF};
        assertRleRoundTrip(input);
    }

    @Test
    void rleRoundTripTwo0xFF() {
        byte[] input = new byte[]{(byte) 0xFF, (byte) 0xFF};
        assertRleRoundTrip(input);
    }

    @Test
    void rleRoundTripThree0xFF() {
        byte[] input = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        assertRleRoundTrip(input);
    }

    @Test
    void rleRoundTripFour0xFF() {
        // 4+ 0xFF bytes should use the run-length encoding path
        byte[] input = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        assertRleRoundTrip(input);
    }

    @Test
    void rleRoundTripLongRun() {
        // Maximum run length (256 bytes of the same value)
        byte[] input = new byte[256];
        Arrays.fill(input, (byte) 42);
        assertRleRoundTrip(input);
    }

    @Test
    void rleRoundTripExceedsMaxRun() {
        // 257+ bytes forces two separate runs
        byte[] input = new byte[300];
        Arrays.fill(input, (byte) 99);
        assertRleRoundTrip(input);
    }

    @Test
    void rleRoundTripAlternatingBytes() {
        // Worst case for RLE - no runs
        byte[] input = new byte[200];
        for (int i = 0; i < input.length; i++) {
            input[i] = (byte) (i % 2 == 0 ? 0xAA : 0x55);
        }
        assertRleRoundTrip(input);
    }

    @Test
    void rleRoundTripRandomData() {
        Random rng = new Random(42);
        byte[] input = new byte[4096];
        rng.nextBytes(input);
        assertRleRoundTrip(input);
    }

    @Test
    void rleRoundTripChunkSized() {
        // Simulate realistic chunk data: mostly zeros with scattered blocks
        byte[] input = new byte[65536 + 32768 + 256]; // blockIDs + nibbles + biomes
        Random rng = new Random(123);
        // Place some stone blocks in lower area
        for (int i = 0; i < 4096; i++) {
            int idx = rng.nextInt(16384);
            input[idx] = 1; // stone
        }
        // Set some biome data
        for (int i = input.length - 256; i < input.length; i++) {
            input[i] = 1; // plains
        }
        assertRleRoundTrip(input);
    }

    // === RLE Encoding Properties ===

    @Test
    void rleEncodesRunsEfficiently() {
        // 256 zeros should compress significantly
        byte[] input = new byte[256];
        byte[] encoded = LCEChunkSerializer.rleEncode(input);
        assertTrue(encoded.length < input.length,
                "RLE should compress 256 zeros, got " + encoded.length + " bytes");
    }

    @Test
    void rleHandles0xFFEscapeCorrectly() {
        // Single 0xFF should encode as [0xFF, 0x00] (2 bytes)
        byte[] input = new byte[]{(byte) 0xFF};
        byte[] encoded = LCEChunkSerializer.rleEncode(input);
        assertEquals(2, encoded.length, "Single 0xFF should encode to 2 bytes");
        assertEquals((byte) 0xFF, encoded[0]);
        assertEquals(0, encoded[1]);
    }

    @Test
    void rleNoExpansionForLiterals() {
        // Non-0xFF bytes with no runs should pass through literally
        byte[] input = new byte[]{1, 2, 3, 4, 5};
        byte[] encoded = LCEChunkSerializer.rleEncode(input);
        assertArrayEquals(input, encoded, "Non-0xFF bytes with no runs should pass through literally");
    }

    // === Full Compression Pipeline ===

    @Test
    void compressRLEZlibProducesValidZlib() throws DataFormatException {
        byte[] raw = new byte[1024];
        Arrays.fill(raw, (byte) 42);
        byte[] compressed = LCEChunkSerializer.compressRLEZlib(raw);

        // Decompress zlib
        Inflater inflater = new Inflater();
        inflater.setInput(compressed);
        byte[] rleData = new byte[raw.length * 2];
        int rleLen = inflater.inflate(rleData);
        inflater.end();
        assertTrue(rleLen > 0, "Zlib decompression should produce output");

        // Decode RLE
        byte[] trimmedRle = Arrays.copyOf(rleData, rleLen);
        byte[] decoded = LCEChunkSerializer.rleDecode(trimmedRle, raw.length);
        assertNotNull(decoded, "RLE decode should succeed");
        assertArrayEquals(raw, decoded, "Full pipeline round-trip should match");
    }

    @Test
    void compressRLEZlibEmptyInput() {
        byte[] raw = new byte[0];
        byte[] compressed = LCEChunkSerializer.compressRLEZlib(raw);
        assertNotNull(compressed);
    }

    // === Helper ===

    private void assertRleRoundTrip(byte[] input) {
        byte[] encoded = LCEChunkSerializer.rleEncode(input);
        byte[] decoded = LCEChunkSerializer.rleDecode(encoded, input.length);
        assertNotNull(decoded, "RLE decode returned null for input length " + input.length);
        assertArrayEquals(input, decoded,
                "RLE round-trip failed for input length " + input.length);
    }
}
