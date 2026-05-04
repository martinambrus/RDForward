package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_4_R1.CraftServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the v1_4_R1 CraftServer stub exists and is constructable.
 * HomeSpawnPlus references this type; the LegacyCraftServerTransformer
 * rewrites plugin bytecode to v1_21_R1, but the stub must exist on the
 * classpath so the original (pre-transform) class references resolve.
 */
class CraftServerV4StubTest {

    @Test
    void craftServerV4IsConstructable() {
        assertDoesNotThrow(() -> new CraftServer(new StubRdServer()));
    }

    @Test
    void craftServerV4GetCommandMapReturnsNonNull() {
        CraftServer craft = new CraftServer(new StubRdServer());
        assertNotNull(craft.getCommandMap());
    }

    @Test
    void craftServerV4GetCommandMapReturnsSimpleCommandMap() {
        CraftServer craft = new CraftServer(new StubRdServer());
        assertInstanceOf(SimpleCommandMap.class, craft.getCommandMap());
    }
}
