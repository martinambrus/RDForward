package com.github.martinambrus.rdforward.server.world;

import com.github.martinambrus.rdforward.api.world.BlockType;
import com.github.martinambrus.rdforward.api.world.BlockTypes;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class VersionedBlockPolicyTest {

    @BeforeEach
    @AfterEach
    void wipeRegistryCache() {
        BlockReplacementRegistry.resetForTests();
    }

    @Test
    void blockInTargetVocabularyPassesThrough() {
        VersionedBlockPolicy p = new VersionedBlockPolicy(ProtocolVersion.ALPHA_1_2_5);
        // STONE was always present — nothing in any replacement file
        // claims to introduce it — so the policy returns it as-is.
        assertSame(BlockTypes.STONE, p.coerce(0, 0, 0, BlockTypes.STONE));
    }

    @Test
    void airAlwaysReturnsAirVerbatim() {
        VersionedBlockPolicy p = new VersionedBlockPolicy(ProtocolVersion.ALPHA_1_2_5);
        assertSame(BlockTypes.AIR, p.coerce(0, 0, 0, BlockTypes.AIR));
    }

    @Test
    void blockIntroducedInB173CoercesViaChainForAlphaTarget() {
        // minecraft:piston is registered in /replacements/b1.7.3.yml
        // pointing at minecraft:cobblestone. ALPHA_1_2_5 predates
        // 1.7.3, so the policy walks the chain and lands on COBBLE
        // (which is in Alpha's vocabulary).
        VersionedBlockPolicy p = new VersionedBlockPolicy(ProtocolVersion.ALPHA_1_2_5);
        BlockType piston = new NamedBlock(100, "minecraft:piston");
        BlockType result = p.coerce(0, 0, 0, piston);
        assertEquals(BlockTypes.COBBLE.getName(), result.getName(),
                "piston should coerce down to cobblestone via b1.7.3 delta");
    }

    @Test
    void coercionIsMemoizedPerInstance() {
        VersionedBlockPolicy p = new VersionedBlockPolicy(ProtocolVersion.ALPHA_1_2_5);
        BlockType piston = new NamedBlock(100, "minecraft:piston");
        BlockType first = p.coerce(0, 0, 0, piston);
        BlockType second = p.coerce(5, 5, 5, piston);
        // computeIfAbsent stores the same resolved instance — same
        // reference both times.
        assertSame(first, second, "memo should return the same instance for the same source");
    }

    @Test
    void unknownBlockNotInAnyFilePassesThrough() {
        // A name that no version file mentions — registry says
        // "introducedAfter == false" because it's "always there" by
        // default. Policy returns it verbatim.
        VersionedBlockPolicy p = new VersionedBlockPolicy(ProtocolVersion.ALPHA_1_2_5);
        BlockType custom = new NamedBlock(150, "myMod:custom_block");
        BlockType result = p.coerce(0, 0, 0, custom);
        assertSame(custom, result);
    }

    @Test
    void getTargetReturnsConstructorArg() {
        VersionedBlockPolicy p = new VersionedBlockPolicy(ProtocolVersion.BETA_1_3);
        assertSame(ProtocolVersion.BETA_1_3, p.getTarget());
    }

    /** Test-only BlockType with arbitrary id + namespaced name. */
    private record NamedBlock(int id, String name) implements BlockType {
        @Override public int getId() { return id; }
        @Override public String getName() { return name; }
    }
}
