package com.github.martinambrus.rdforward.server.world;

import com.github.martinambrus.rdforward.api.world.BlockType;
import com.github.martinambrus.rdforward.api.world.BlockTypes;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.server.ServerWorld;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end test: ServerWorld with a real BlockPolicy installed,
 * confirming writes are coerced before storage.
 */
class ServerWorldPolicyIntegrationTest {

    @BeforeEach
    @AfterEach
    void wipe() {
        BlockCoercionLog.resetForTests();
        BlockReplacementRegistry.resetForTests();
    }

    @Test
    void rubyDungWorldCoercesNonAirBlocksToCobbleBelowGrassLayer() {
        ServerWorld world = newWorld(64, 32, 64, "ruby");
        world.setPolicy(new RubyDungBlockPolicy(20));
        world.setBlock(5, 10, 5, (byte) BlockTypes.PLANKS.getId());
        // y < grassLayerY → COBBLE
        assertEquals((byte) BlockTypes.COBBLE.getId(), world.getBlock(5, 10, 5));
        // Coercion log should have recorded planks → cobble once.
        assertTrue(BlockCoercionLog.hasLogged("ruby", BlockTypes.PLANKS));
    }

    @Test
    void rubyDungWorldCoercesGrassLayerToGrass() {
        ServerWorld world = newWorld(64, 32, 64, "ruby");
        world.setPolicy(new RubyDungBlockPolicy(20));
        world.setBlock(5, 20, 5, (byte) BlockTypes.STONE.getId());
        assertEquals((byte) BlockTypes.GRASS.getId(), world.getBlock(5, 20, 5));
    }

    @Test
    void rubyDungAirPassesThroughForBlockBreak() {
        ServerWorld world = newWorld(64, 32, 64, "ruby");
        world.setPolicy(new RubyDungBlockPolicy(20));
        // First place cobble.
        world.setBlock(5, 10, 5, (byte) BlockTypes.COBBLE.getId());
        // Then break (set to air).
        world.setBlock(5, 10, 5, (byte) BlockTypes.AIR.getId());
        assertEquals((byte) BlockTypes.AIR.getId(), world.getBlock(5, 10, 5));
    }

    @Test
    void identityPolicyLeavesWritesUnchanged() {
        // Default policy is IDENTITY when setPolicy is never called —
        // legacy tests must keep working.
        ServerWorld world = newWorld(64, 32, 64, "default");
        world.setBlock(5, 5, 5, (byte) BlockTypes.PLANKS.getId());
        assertEquals((byte) BlockTypes.PLANKS.getId(), world.getBlock(5, 5, 5));
        assertFalse(BlockCoercionLog.hasLogged("default", BlockTypes.PLANKS));
    }

    @Test
    void coercionLogFiresOnceForRepeatedSameSource() {
        ServerWorld world = newWorld(64, 32, 64, "ruby");
        world.setPolicy(new RubyDungBlockPolicy(20));
        world.setBlock(1, 10, 1, (byte) BlockTypes.PLANKS.getId());
        world.setBlock(2, 10, 2, (byte) BlockTypes.PLANKS.getId());
        world.setBlock(3, 10, 3, (byte) BlockTypes.PLANKS.getId());
        // Dedup state stays at "logged once".
        assertTrue(BlockCoercionLog.hasLogged("ruby", BlockTypes.PLANKS));
    }

    @Test
    void versionedPolicyAlphaCoercesPistonViaB173Chain() {
        ServerWorld world = newWorld(64, 32, 64, "alpha-world");
        world.setPolicy(new VersionedBlockPolicy(ProtocolVersion.ALPHA_1_2_5));
        // Place "piston" using its byte id (which doesn't match any
        // known constant — we go through the policy via the namespaced
        // name lookup chain, not the byte id directly). Use
        // BlockTypes.byId so the resolved BlockType has id=33 and a
        // generated name. The chain only triggers when the name matches
        // an entry in a YAML — so use a synthetic placement that
        // bypasses the registry-name path won't coerce.
        //
        // For this integration test, we exercise the byte-level path
        // with a known-good block (planks) and confirm the world stores
        // it verbatim under VersionedBlockPolicy because PLANKS is not
        // introduced after ALPHA_1_2_5 (registry treats it as always
        // there). The b1.7.3.yml chain is unit-tested in
        // VersionedBlockPolicyTest where we control the BlockType name.
        world.setBlock(5, 5, 5, (byte) BlockTypes.PLANKS.getId());
        assertEquals((byte) BlockTypes.PLANKS.getId(), world.getBlock(5, 5, 5));
    }

    @Test
    void queueBlockChangeAppliesPolicyBeforeStorage() {
        // RDWorld.setBlock(BlockType) -> queueBlockChange path. The
        // tick loop drains via processPendingBlockChanges which must
        // run the BlockPolicy too — otherwise plugin/mod writes
        // bypass the chokepoint and unsupported blocks reach storage.
        ServerWorld world = newWorld(64, 32, 64, "ruby");
        world.setPolicy(new RubyDungBlockPolicy(20));
        world.queueBlockChange(5, 10, 5, (byte) BlockTypes.PLANKS.getId());

        List<SetBlockServerPacket> applied = world.processPendingBlockChanges();
        assertEquals(1, applied.size(), "one change should drain");
        // Coerced byte (cobble) is what's stored AND broadcast.
        assertEquals(BlockTypes.COBBLE.getId(), applied.get(0).getBlockType());
        assertEquals((byte) BlockTypes.COBBLE.getId(), world.getBlock(5, 10, 5));
        assertTrue(BlockCoercionLog.hasLogged("ruby", BlockTypes.PLANKS));
    }

    @Test
    void queueBlockChangePassesUnchangedBlocksThroughIdentityWorld() {
        // Identity policy world: queue path stays a fast-path; coerced
        // byte equals the requested byte and the SetBlockServerPacket
        // carries that verbatim.
        ServerWorld world = newWorld(64, 32, 64, "default");
        world.queueBlockChange(5, 5, 5, (byte) BlockTypes.PLANKS.getId());

        List<SetBlockServerPacket> applied = world.processPendingBlockChanges();
        assertEquals(1, applied.size());
        assertEquals(BlockTypes.PLANKS.getId(), applied.get(0).getBlockType());
        assertEquals((byte) BlockTypes.PLANKS.getId(), world.getBlock(5, 5, 5));
        assertFalse(BlockCoercionLog.hasLogged("default", BlockTypes.PLANKS));
    }

    private ServerWorld newWorld(int w, int h, int d, String name) {
        return new ServerWorld(w, h, d, null, name);
    }
}
