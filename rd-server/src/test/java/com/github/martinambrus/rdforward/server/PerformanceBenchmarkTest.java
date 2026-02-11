package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.world.FlatWorldGenerator;
import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance benchmarks for critical server paths.
 *
 * These tests verify that core operations complete within acceptable
 * time budgets and measure throughput for capacity planning.
 * Times are printed to stdout for CI visibility.
 */
class PerformanceBenchmarkTest {

    @Test
    void chunkSerializationThroughput() throws IOException {
        // Measure how fast we can serialize chunks (key for join bandwidth)
        AlphaChunk chunk = new AlphaChunk(0, 0);
        // Fill with varied terrain
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.setBlock(x, 0, z, 7);  // Bedrock
                for (int y = 1; y < 60; y++) {
                    chunk.setBlock(x, y, z, 1); // Stone
                }
                chunk.setBlock(x, 60, z, 2); // Grass
            }
        }

        // Warmup
        for (int i = 0; i < 50; i++) {
            chunk.serializeForAlphaProtocol();
        }

        // Measure
        int iterations = 200;
        long start = System.nanoTime();
        int totalBytes = 0;
        for (int i = 0; i < iterations; i++) {
            byte[] data = chunk.serializeForAlphaProtocol();
            totalBytes += data.length;
        }
        long elapsed = System.nanoTime() - start;

        double msPerChunk = (elapsed / 1_000_000.0) / iterations;
        double chunksPerSec = iterations / (elapsed / 1_000_000_000.0);
        double avgCompressedKB = (totalBytes / (double) iterations) / 1024.0;

        System.out.printf("[PERF] Chunk serialize: %.2f ms/chunk, %.0f chunks/sec, %.1f KB avg compressed%n",
                msPerChunk, chunksPerSec, avgCompressedKB);

        // Sanity: a single chunk should serialize in under 10ms
        assertTrue(msPerChunk < 10.0,
                "Chunk serialization too slow: " + msPerChunk + " ms");
    }

    @Test
    void classicWorldSerializationThroughput() throws IOException {
        // Small world for testing (64x32x64 = 131072 blocks)
        ServerWorld world = new ServerWorld(64, 32, 64);
        world.generate(new FlatWorldGenerator(), 0L);

        // Warmup
        for (int i = 0; i < 5; i++) {
            world.serializeForClassicProtocol();
        }

        // Measure
        int iterations = 20;
        long start = System.nanoTime();
        int totalBytes = 0;
        for (int i = 0; i < iterations; i++) {
            byte[] data = world.serializeForClassicProtocol();
            totalBytes += data.length;
        }
        long elapsed = System.nanoTime() - start;

        double msPerWorld = (elapsed / 1_000_000.0) / iterations;
        double avgKB = (totalBytes / (double) iterations) / 1024.0;

        System.out.printf("[PERF] Classic world serialize (64x32x64): %.2f ms, %.1f KB compressed%n",
                msPerWorld, avgKB);

        // Should complete well within a second
        assertTrue(msPerWorld < 1000.0,
                "World serialization too slow: " + msPerWorld + " ms");
    }

    @Test
    void blockChangeProcessingThroughput() {
        ServerWorld world = new ServerWorld(256, 64, 256);
        world.generate(new FlatWorldGenerator(), 0L);

        // Queue many block changes
        int totalChanges = 10_000;
        for (int i = 0; i < totalChanges; i++) {
            int x = i % 256;
            int z = (i / 256) % 256;
            int y = 30 + (i % 10);
            world.queueBlockChange(x, y, z, (byte) ((i % 49) + 1));
        }

        // Measure processing
        long start = System.nanoTime();
        List<SetBlockServerPacket> applied = world.processPendingBlockChanges();
        long elapsed = System.nanoTime() - start;

        double msTotal = elapsed / 1_000_000.0;
        double usPerChange = (elapsed / 1000.0) / applied.size();

        System.out.printf("[PERF] Block changes: %d applied in %.2f ms (%.2f us/change)%n",
                applied.size(), msTotal, usPerChange);

        assertTrue(applied.size() > 0);
        // Should process 10K block changes in well under a second
        assertTrue(msTotal < 1000.0,
                "Block change processing too slow: " + msTotal + " ms for " + totalChanges);
    }

    @Test
    void serverTickSimulation() {
        // Simulate a tick loop: process block changes from "N clients"
        ServerWorld world = new ServerWorld(256, 64, 256);
        world.generate(new FlatWorldGenerator(), 0L);

        int simulatedClients = 50;
        int tickCount = 100;
        int changesPerClientPerTick = 2;

        long totalApplied = 0;
        long start = System.nanoTime();

        for (int tick = 0; tick < tickCount; tick++) {
            // Each client sends a few block changes per tick
            for (int client = 0; client < simulatedClients; client++) {
                for (int c = 0; c < changesPerClientPerTick; c++) {
                    int x = (tick * simulatedClients + client) % 256;
                    int z = (client * 3 + c) % 256;
                    world.queueBlockChange(x, 30 + (tick % 30), z, (byte) (1 + (c % 49)));
                }
            }
            List<SetBlockServerPacket> applied = world.processPendingBlockChanges();
            totalApplied += applied.size();
        }

        long elapsed = System.nanoTime() - start;
        double msPerTick = (elapsed / 1_000_000.0) / tickCount;
        double msTotal = elapsed / 1_000_000.0;

        System.out.printf("[PERF] Tick sim (%d clients, %d ticks): %.2f ms/tick, %.0f total applied, %.1f ms total%n",
                simulatedClients, tickCount, msPerTick, (double) totalApplied, msTotal);

        // 20 TPS = 50ms budget per tick. Processing should take well under that.
        assertTrue(msPerTick < 50.0,
                "Tick processing too slow for 20 TPS: " + msPerTick + " ms/tick");
    }

    @Test
    void worldGenerationTime() {
        // Measure how long it takes to generate a standard world
        FlatWorldGenerator generator = new FlatWorldGenerator();
        int width = 256, height = 64, depth = 256;
        byte[] blocks = new byte[width * height * depth];

        // Warmup
        generator.generate(blocks, width, height, depth, 0L);

        // Measure
        int iterations = 5;
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            generator.generate(blocks, width, height, depth, i);
        }
        long elapsed = System.nanoTime() - start;

        double msPerGenerate = (elapsed / 1_000_000.0) / iterations;

        System.out.printf("[PERF] Flat world gen (256x64x256): %.2f ms%n", msPerGenerate);

        // World generation should complete in a reasonable time
        assertTrue(msPerGenerate < 5000.0,
                "World generation too slow: " + msPerGenerate + " ms");
    }
}
