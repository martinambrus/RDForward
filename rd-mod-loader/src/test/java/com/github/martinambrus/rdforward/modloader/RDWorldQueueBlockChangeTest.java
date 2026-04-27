package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.world.BlockType;
import com.github.martinambrus.rdforward.api.world.BlockTypes;
import com.github.martinambrus.rdforward.modloader.impl.RDWorld;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.server.ServerWorld;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * RDWorld.setBlock(BlockType) must queue the change so the tick loop's
 * broadcast pipeline (ServerTickLoop -> processPendingBlockChanges ->
 * playerManager.broadcastWrite) picks it up. A previous version of the
 * adapter wrote directly via ServerWorld.setBlock(byte) which mutated
 * storage but never broadcast — plugin writes (WorldEdit //set) ran
 * server-side but no client ever saw the change.
 */
class RDWorldQueueBlockChangeTest {

    @Test
    void setBlockQueuesAndDoesNotMutateStorageImmediately() {
        ServerWorld backing = new ServerWorld(32, 32, 32);
        RDWorld world = new RDWorld(backing);

        // Pre-condition: target cell is air.
        assertEquals((byte) BlockTypes.AIR.getId(), backing.getBlock(5, 5, 5));

        // Queue a change. The change must NOT yet be visible in
        // storage — the tick loop hasn't drained yet.
        boolean queued = world.setBlock(5, 5, 5, BlockTypes.PLANKS);
        assertEquals(true, queued, "setBlock should report queued");
        assertEquals((byte) BlockTypes.AIR.getId(), backing.getBlock(5, 5, 5),
                "queue path must defer storage to processPendingBlockChanges");

        // Drain the queue (simulating one tick). Now the block is
        // visible AND the broadcast packet was emitted.
        List<SetBlockServerPacket> applied = backing.processPendingBlockChanges();
        assertEquals(1, applied.size());
        assertEquals(BlockTypes.PLANKS.getId(), applied.get(0).getBlockType());
        assertEquals((byte) BlockTypes.PLANKS.getId(), backing.getBlock(5, 5, 5));
    }

    @Test
    void outOfBoundsSetBlockReturnsFalseWithoutQueueing() {
        ServerWorld backing = new ServerWorld(32, 32, 32);
        RDWorld world = new RDWorld(backing);

        boolean ok = world.setBlock(-1, 0, 0, BlockTypes.STONE);
        assertEquals(false, ok);

        List<SetBlockServerPacket> applied = backing.processPendingBlockChanges();
        assertEquals(0, applied.size(), "no entry should have been queued");
    }

    @Test
    void queuedSetBlocksApplyInOrderAndBroadcastEachChange() {
        ServerWorld backing = new ServerWorld(32, 32, 32);
        RDWorld world = new RDWorld(backing);

        BlockType[] sequence = {BlockTypes.PLANKS, BlockTypes.STONE, BlockTypes.COBBLE};
        for (int i = 0; i < sequence.length; i++) {
            world.setBlock(i, 5, 5, sequence[i]);
        }

        List<SetBlockServerPacket> applied = backing.processPendingBlockChanges();
        assertEquals(3, applied.size());
        for (int i = 0; i < sequence.length; i++) {
            assertEquals(sequence[i].getId(), applied.get(i).getBlockType());
            assertEquals(i, applied.get(i).getX());
            assertNotEquals((byte) BlockTypes.AIR.getId(), backing.getBlock(i, 5, 5));
        }
    }
}
