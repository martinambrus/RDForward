package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ServerWorld: block operations, bounds checking,
 * Classic protocol serialization, and pending block changes.
 */
class ServerWorldTest {

    @Test
    void newWorldIsAllAir() {
        ServerWorld world = new ServerWorld(16, 8, 16);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 16; z++) {
                    assertEquals(0, world.getBlock(x, y, z));
                }
            }
        }
    }

    @Test
    void setAndGetBlock() {
        ServerWorld world = new ServerWorld(16, 8, 16);
        assertTrue(world.setBlock(5, 3, 8, (byte) 1));
        assertEquals(1, world.getBlock(5, 3, 8));
    }

    @Test
    void setBlockReturnsFalseForSameValue() {
        ServerWorld world = new ServerWorld(16, 8, 16);
        assertTrue(world.setBlock(0, 0, 0, (byte) 1));
        assertFalse(world.setBlock(0, 0, 0, (byte) 1)); // No change
    }

    @Test
    void outOfBoundsGetReturnsAir() {
        ServerWorld world = new ServerWorld(16, 8, 16);
        assertEquals(0, world.getBlock(-1, 0, 0));
        assertEquals(0, world.getBlock(16, 0, 0));
        assertEquals(0, world.getBlock(0, -1, 0));
        assertEquals(0, world.getBlock(0, 8, 0));
        assertEquals(0, world.getBlock(0, 0, -1));
        assertEquals(0, world.getBlock(0, 0, 16));
    }

    @Test
    void outOfBoundsSetReturnsFalse() {
        ServerWorld world = new ServerWorld(16, 8, 16);
        assertFalse(world.setBlock(-1, 0, 0, (byte) 1));
        assertFalse(world.setBlock(16, 0, 0, (byte) 1));
        assertFalse(world.setBlock(0, -1, 0, (byte) 1));
        assertFalse(world.setBlock(0, 8, 0, (byte) 1));
    }

    @Test
    void inBoundsChecks() {
        ServerWorld world = new ServerWorld(16, 8, 16);
        assertTrue(world.inBounds(0, 0, 0));
        assertTrue(world.inBounds(15, 7, 15));
        assertFalse(world.inBounds(16, 0, 0));
        assertFalse(world.inBounds(0, 8, 0));
        assertFalse(world.inBounds(-1, 0, 0));
    }

    @Test
    void dimensions() {
        ServerWorld world = new ServerWorld(64, 32, 128);
        assertEquals(64, world.getWidth());
        assertEquals(32, world.getHeight());
        assertEquals(128, world.getDepth());
    }

    @Test
    void queueAndProcessBlockChanges() {
        ServerWorld world = new ServerWorld(16, 8, 16);
        world.queueBlockChange(1, 1, 1, (byte) 5);
        world.queueBlockChange(2, 2, 2, (byte) 3);

        List<SetBlockServerPacket> applied = world.processPendingBlockChanges();
        assertEquals(2, applied.size());
        assertEquals(5, world.getBlock(1, 1, 1));
        assertEquals(3, world.getBlock(2, 2, 2));
    }

    @Test
    void duplicateBlockChangeNotApplied() {
        ServerWorld world = new ServerWorld(16, 8, 16);
        world.setBlock(1, 1, 1, (byte) 5);
        world.queueBlockChange(1, 1, 1, (byte) 5); // Same value

        List<SetBlockServerPacket> applied = world.processPendingBlockChanges();
        assertEquals(0, applied.size()); // No actual change
    }

    @Test
    void processBlockChangesReturnsPacketsWithCorrectCoordinates() {
        ServerWorld world = new ServerWorld(16, 8, 16);
        world.queueBlockChange(3, 4, 5, (byte) 7);

        List<SetBlockServerPacket> applied = world.processPendingBlockChanges();
        assertEquals(1, applied.size());
        SetBlockServerPacket pkt = applied.get(0);
        assertEquals(3, pkt.getX());
        assertEquals(4, pkt.getY());
        assertEquals(5, pkt.getZ());
        assertEquals(7, pkt.getBlockType());
    }

    @Test
    void emptyQueueReturnsEmptyList() {
        ServerWorld world = new ServerWorld(16, 8, 16);
        List<SetBlockServerPacket> applied = world.processPendingBlockChanges();
        assertTrue(applied.isEmpty());
    }

    @Test
    void serializeForClassicProtocol() throws IOException {
        ServerWorld world = new ServerWorld(4, 4, 4);
        world.setBlock(1, 2, 3, (byte) 10);

        byte[] compressed = world.serializeForClassicProtocol();
        assertTrue(compressed.length > 0);

        // Decompress and verify
        DataInputStream dis = new DataInputStream(
                new GZIPInputStream(new ByteArrayInputStream(compressed)));
        int volume = dis.readInt();
        assertEquals(64, volume); // 4*4*4

        byte[] blocks = new byte[volume];
        dis.readFully(blocks);

        // Classic ordering is XZY: index at (1,2,3) = x*(depth*height) + z*height + y = 1*16 + 3*4 + 2 = 30
        assertEquals(10, blocks[30] & 0xFF);
    }

    @Test
    void cornerBlocksSurviveRoundTrip() {
        ServerWorld world = new ServerWorld(16, 8, 16);
        world.setBlock(0, 0, 0, (byte) 1);
        world.setBlock(15, 7, 15, (byte) 2);
        assertEquals(1, world.getBlock(0, 0, 0));
        assertEquals(2, world.getBlock(15, 7, 15));
    }
}
