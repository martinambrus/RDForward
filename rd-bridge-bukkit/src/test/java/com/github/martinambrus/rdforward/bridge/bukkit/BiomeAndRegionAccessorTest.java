package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.RegionAccessor;
import org.bukkit.UnsafeValues;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the bridge surface that EssentialsX 2.21.2's
 * {@code RandomTeleport} chain ({@code /tpr}) walks through during
 * biome-exclusion checks. Two regression categories matter here:
 * <ol>
 *   <li>{@code Biome} must be an abstract <i>class</i> — Essentials's
 *       {@code LegacyBiomeNameProvider} bytecode emits
 *       {@code invokevirtual Biome.name()}, which raises
 *       {@link IncompatibleClassChangeError} silently inside the
 *       {@link java.util.concurrent.CompletableFuture#thenAccept}
 *       lambda when the JVM finds an interface there. Symptom: /tpr
 *       prints "Teleporting to a random location..." and never moves.</li>
 *   <li>{@code BukkitWorldAdapter} must implement
 *       {@link RegionAccessor} — Essentials's {@code PaperBiomeKeyProvider}
 *       casts {@code (RegionAccessor) block.getWorld()}; without the cast
 *       succeeding the same chain dies with a swallowed CCE.</li>
 * </ol>
 * Each gap is invisible without bytecode-level diagnostics; these tests
 * fail loudly if either regresses.
 */
class BiomeAndRegionAccessorTest {

    private StubRdServer rd;

    @BeforeEach void setUp() {
        rd = new StubRdServer();
        BukkitBridge.install(rd);
    }

    @AfterEach void tearDown() {
        BukkitBridge.uninstall();
    }

    /* ---- Biome shape: must be an abstract class, not interface ---- */

    @Test
    void biomeIsAbstractClassNotInterface() {
        // Essentials's bytecode `invokevirtual Biome.name()` only resolves
        // when Biome is a class. Loading it as an interface raises ICCE
        // at the call site.
        assertFalse(Biome.class.isInterface(),
                "Biome must be a class (EssentialsX emits invokevirtual on Biome.name())");
        assertTrue(Modifier.isAbstract(Biome.class.getModifiers()),
                "Biome should remain abstract — RDForward never instantiates the bare class");
    }

    @Test
    void biomeNameMethodResolvesOnRdStubBiome() throws Exception {
        // Reflection equivalent of `invokevirtual Biome.name()` — the
        // exact dispatch shape Essentials uses. Failing here means the
        // /tpr biome-exclusion check would silently die.
        Biome stub = Block.RD_STUB_BIOME;
        assertNotNull(stub);
        Method name = Biome.class.getMethod("name");
        Object result = name.invoke(stub);
        assertEquals("plains", result);
    }

    /* ---- RegionAccessor: BukkitWorldAdapter must satisfy the cast ---- */

    @Test
    void bukkitWorldAdapterImplementsRegionAccessor() {
        World world = Bukkit.getServer().getWorlds().get(0);
        // The CHECKCAST instruction in PaperBiomeKeyProvider.getBiomeKey
        // — `(RegionAccessor) block.getWorld()` — only succeeds when the
        // runtime class implements RegionAccessor.
        assertTrue(world instanceof RegionAccessor,
                "BukkitWorldAdapter must implement RegionAccessor for PaperBiomeKeyProvider cast");
    }

    @Test
    void blockGetWorldReturnsRegionAccessorTypedWorld() {
        World world = Bukkit.getServer().getWorlds().get(0);
        Block block = world.getBlockAt(0, 0, 0);
        assertNotNull(block);
        // Same cast as PaperBiomeKeyProvider.getBiomeKey — must not throw.
        RegionAccessor ra = (RegionAccessor) block.getWorld();
        assertSame(world, ra,
                "block.getWorld() should return the BukkitWorldAdapter that produced the block");
    }

    @Test
    void regionAccessorStubsReturnSensibleDefaults() {
        RegionAccessor world = (RegionAccessor) Bukkit.getServer().getWorlds().get(0);
        assertNotNull(world.getBiome(0, 0, 0));
        assertNotNull(world.getComputedBiome(0, 0, 0));
        assertNotNull(world.getBlockData(0, 0, 0));
        assertNotNull(world.getBlockState(0, 0, 0));
        assertNotNull(world.getType(0, 0, 0));
        assertNotNull(world.getKey(),
                "getKey() must be non-null — EssentialsX reads it on registry-keyed lookups");
        assertNotNull(world.getEntities());
        assertNotNull(world.getLivingEntities());
        // Highest-block-Y form added by RegionAccessor (with HeightMap)
        // must bridge to World's int form rather than throwing.
        assertEquals(world.getHighestBlockYAt(0, 0),
                world.getHighestBlockYAt(0, 0, null));
    }

    /* ---- UnsafeValues.getBiomeKey proxy: non-null NamespacedKey ---- */

    @Test
    void getUnsafeReturnsProxyWithBiomeKeySpecialCase() {
        UnsafeValues unsafe = Bukkit.getUnsafe();
        assertNotNull(unsafe);
        // PaperBiomeKeyProvider.getBiomeKey calls toString() on the
        // returned key — a null return would NPE inside the Essentials
        // CompletableFuture chain (silent failure).
        NamespacedKey key = unsafe.getBiomeKey(
                (RegionAccessor) Bukkit.getServer().getWorlds().get(0),
                0, 0, 0);
        assertNotNull(key);
        assertEquals("minecraft:plains", key.toString());
    }

    /* ---- Integration: full PaperBiomeKeyProvider call shape ---- */

    @Test
    void paperBiomeKeyProviderCallShapeCompletesWithoutException() {
        // Reproduces the exact call sequence EssentialsX runs inside its
        // CompletableFuture lambda. Any failure in this chain (ICCE, CCE,
        // NPE) is silently swallowed in production — here it surfaces as
        // a test failure so a future stub change can't reintroduce the
        // /tpr hang.
        World world = Bukkit.getServer().getWorlds().get(0);
        Block block = world.getBlockAt(0, 0, 0);

        // 1. LegacyBiomeNameProvider.getBiomeName: block.getBiome().name()
        Biome biome = block.getBiome();
        assertNotNull(biome);
        String enumKey = biome.name().toLowerCase(java.util.Locale.ENGLISH);
        assertNotNull(enumKey);

        // 2. PaperBiomeKeyProvider.getBiomeKey:
        //    Bukkit.getUnsafe().getBiomeKey((RegionAccessor) block.getWorld(), x, y, z)
        NamespacedKey key = Bukkit.getUnsafe().getBiomeKey(
                (RegionAccessor) block.getWorld(),
                block.getX(), block.getY(), block.getZ());
        assertNotNull(key);

        // 3. Essentials's `excluded.contains(biomeKey.toString())` —
        //    must produce a non-null string for the HashSet lookup.
        assertNotNull(key.toString());
    }
}
