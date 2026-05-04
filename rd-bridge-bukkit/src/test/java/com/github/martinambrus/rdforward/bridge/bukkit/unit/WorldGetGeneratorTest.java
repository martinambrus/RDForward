package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.ChunkGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Confirms {@link World#getGenerator()} is a default method returning null
 * so bSpace's {@code SpaceWorldHandler.loadSpaceWorlds} survives the
 * startup world scan without NoSuchMethodError.
 */
class WorldGetGeneratorTest {

    @Test
    void getGeneratorIsNullByDefault() {
        assertNull(new StubWorld().getGenerator());
    }

    @Test
    void getGeneratorIsDefaultMethod() throws NoSuchMethodException {
        assertTrue(World.class.getDeclaredMethod("getGenerator").isDefault());
    }

    @Test
    void methodSignatureMatchesBukkit() throws NoSuchMethodException {
        assertNotNull(World.class.getDeclaredMethod("getGenerator"));
        // Return type must be ChunkGenerator (or subclass)
        Class<?> rt = World.class.getDeclaredMethod("getGenerator").getReturnType();
        assertTrue(ChunkGenerator.class.isAssignableFrom(rt));
    }

    private static class StubWorld implements World {
        @Override public String getName() { return "stub"; }
        @Override public Block getBlockAt(int x, int y, int z) { return null; }
        @Override public boolean setBlockType(int x, int y, int z, Material type) { return true; }
        @Override public int getMaxHeight() { return 256; }
        @Override public long getTime() { return 0; }
        @Override public void setTime(long time) {}
    }
}
