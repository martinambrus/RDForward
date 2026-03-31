package com.github.martinambrus.rdforward.server.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ServerProperties parsing, defaults, validation.
 * Uses @TempDir for file isolation and clearForTesting() for cleanup.
 */
class ServerPropertiesTest {

    @TempDir
    File tempDir;

    @AfterEach
    void cleanUp() {
        ServerProperties.clearForTesting();
    }

    // --- Default value tests ---

    @Test
    void defaultsLoadedWhenNoFileExists() {
        ServerProperties.load(tempDir);

        assertEquals(25565, ServerProperties.getServerPort());
        assertEquals(19132, ServerProperties.getBedrockPort());
        assertEquals(1, ServerProperties.getGameMode()); // creative
        assertEquals(1, ServerProperties.getDifficulty()); // easy
        assertEquals(128, ServerProperties.getMaxPlayers());
        assertEquals(5, ServerProperties.getViewDistance());
        assertEquals("A Minecraft Server", ServerProperties.getMotd());
        assertEquals("world", ServerProperties.getLevelName());
        assertEquals(16, ServerProperties.getSpawnProtection());
        assertFalse(ServerProperties.isWhiteList());
        assertFalse(ServerProperties.isEnforceWhitelist());
        assertFalse(ServerProperties.isOnlineMode());
        assertFalse(ServerProperties.isEnableCommandBlock());
        assertTrue(ServerProperties.isPvp());
    }

    @Test
    void defaultsFileCreatedOnFirstLoad() {
        ServerProperties.load(tempDir);
        File propsFile = new File(tempDir, "server.properties");
        assertTrue(propsFile.exists(), "server.properties should be created if it doesn't exist");
    }

    // --- File override tests ---

    @Test
    void fileValuesOverrideDefaults() throws IOException {
        writeProps("server-port=12345\nmotd=Test MOTD\nmax-players=10\n");

        ServerProperties.load(tempDir);

        assertEquals(12345, ServerProperties.getServerPort());
        assertEquals("Test MOTD", ServerProperties.getMotd());
        assertEquals(10, ServerProperties.getMaxPlayers());
    }

    @Test
    void gamemodeAcceptsNameAndNumber() throws IOException {
        // Test name
        writeProps("gamemode=survival\n");
        ServerProperties.load(tempDir);
        assertEquals(0, ServerProperties.getGameMode());
        assertEquals("Survival", ServerProperties.getGameModeName());

        // Test number
        ServerProperties.clearForTesting();
        writeProps("gamemode=2\n");
        ServerProperties.load(tempDir);
        assertEquals(2, ServerProperties.getGameMode());
        assertEquals("Adventure", ServerProperties.getGameModeName());
    }

    @Test
    void difficultyAcceptsNameAndNumber() throws IOException {
        writeProps("difficulty=hard\n");
        ServerProperties.load(tempDir);
        assertEquals(3, ServerProperties.getDifficulty());
        assertEquals("Hard", ServerProperties.getDifficultyName());

        ServerProperties.clearForTesting();
        writeProps("difficulty=0\n");
        ServerProperties.load(tempDir);
        assertEquals(0, ServerProperties.getDifficulty());
        assertEquals("Peaceful", ServerProperties.getDifficultyName());
    }

    // --- Validation tests ---

    @Test
    void portOutOfRangeFallsBackToDefault() throws IOException {
        writeProps("server-port=99999\n");
        ServerProperties.load(tempDir);
        assertEquals(25565, ServerProperties.getServerPort());
    }

    @Test
    void viewDistanceClampedTo1Through32() throws IOException {
        writeProps("view-distance=100\n");
        ServerProperties.load(tempDir);
        assertTrue(ServerProperties.getViewDistance() <= 32,
                "View distance should be clamped to max 32");

        ServerProperties.clearForTesting();
        writeProps("view-distance=0\n");
        ServerProperties.load(tempDir);
        assertTrue(ServerProperties.getViewDistance() >= 1,
                "View distance should be clamped to min 1");
    }

    @Test
    void spawnProtectionNonNegative() throws IOException {
        writeProps("spawn-protection=0\n");
        ServerProperties.load(tempDir);
        assertEquals(0, ServerProperties.getSpawnProtection());
    }

    @Test
    void levelNameRejectsPathTraversal() throws IOException {
        writeProps("level-name=../../../etc\n");
        ServerProperties.load(tempDir);
        // Should fall back to default "world" due to path traversal
        String name = ServerProperties.getLevelName();
        assertFalse(name.contains(".."),
                "Level name should reject path traversal, got: " + name);
    }

    @Test
    void levelSeedParsesNumericAndString() throws IOException {
        writeProps("level-seed=12345\n");
        ServerProperties.load(tempDir);
        assertEquals(12345L, ServerProperties.getLevelSeed());

        ServerProperties.clearForTesting();
        writeProps("level-seed=myseed\n");
        ServerProperties.load(tempDir);
        assertEquals((long) "myseed".hashCode(), ServerProperties.getLevelSeed());

        ServerProperties.clearForTesting();
        writeProps("level-seed=\n");
        ServerProperties.load(tempDir);
        assertEquals(0L, ServerProperties.getLevelSeed());
    }

    @Test
    void worldDimensionsMinimumOne() throws IOException {
        writeProps("world-width=0\nworld-height=0\nworld-depth=0\n");
        ServerProperties.load(tempDir);
        assertTrue(ServerProperties.getWorldWidth() >= 1);
        assertTrue(ServerProperties.getWorldHeight() >= 1);
        assertTrue(ServerProperties.getWorldDepth() >= 1);
    }

    // --- White-list runtime toggle ---

    @Test
    void setWhiteListChangesState() {
        ServerProperties.load(tempDir);
        assertFalse(ServerProperties.isWhiteList());

        ServerProperties.setWhiteList(true);
        assertTrue(ServerProperties.isWhiteList());

        ServerProperties.setWhiteList(false);
        assertFalse(ServerProperties.isWhiteList());
    }

    // --- Keep-alive settings ---

    @Test
    void keepAliveDefaults() {
        ServerProperties.load(tempDir);
        assertEquals(15, ServerProperties.getKeepAliveIntervalSeconds());
        assertEquals(30, ServerProperties.getKeepAliveTimeoutSeconds());
    }

    @Test
    void keepAliveMinimumOne() throws IOException {
        writeProps("keep-alive-interval=0\nkeep-alive-timeout=0\n");
        ServerProperties.load(tempDir);
        assertTrue(ServerProperties.getKeepAliveIntervalSeconds() >= 1);
        assertTrue(ServerProperties.getKeepAliveTimeoutSeconds() >= 1);
    }

    // --- Boolean parsing ---

    @Test
    void booleanParsing() throws IOException {
        writeProps("online-mode=true\nwhite-list=true\npvp=false\n");
        ServerProperties.load(tempDir);
        assertTrue(ServerProperties.isOnlineMode());
        assertTrue(ServerProperties.isWhiteList());
        assertFalse(ServerProperties.isPvp());
    }

    // --- System property override ---

    @Test
    void systemPropertyOverridesFileValue() throws IOException {
        writeProps("view-distance=3\n");
        System.setProperty("e2e.viewDistance", "10");
        try {
            ServerProperties.load(tempDir);
            assertEquals(10, ServerProperties.getViewDistance(),
                    "System property should override file value");
        } finally {
            System.clearProperty("e2e.viewDistance");
        }
    }

    // --- Grief protection config ---

    @Test
    void griefProtectionDefaults() {
        ServerProperties.load(tempDir);
        assertEquals(200, ServerProperties.getGriefInitialBudget());
        assertEquals(100, ServerProperties.getGriefAccrualPerHour());
        assertEquals(50_000, ServerProperties.getGriefMaxBudget());
        assertEquals(30, ServerProperties.getGriefExpiryDays());
        assertEquals(5.0, ServerProperties.getGriefThresholdWarn());
        assertEquals(10.0, ServerProperties.getGriefThresholdKick());
        assertEquals(20.0, ServerProperties.getGriefThresholdTempban());
    }

    @Test
    void griefProtectionConfigOverride() throws IOException {
        writeProps("grief-initial-budget=500\ngrief-accrual-per-hour=200\n"
                + "grief-max-budget=100000\ngrief-expiry-days=60\n"
                + "grief-threshold-warn=3.0\ngrief-threshold-kick=8.0\n"
                + "grief-threshold-tempban=15.0\n");
        ServerProperties.load(tempDir);
        assertEquals(500, ServerProperties.getGriefInitialBudget());
        assertEquals(200, ServerProperties.getGriefAccrualPerHour());
        assertEquals(100_000, ServerProperties.getGriefMaxBudget());
        assertEquals(60, ServerProperties.getGriefExpiryDays());
        assertEquals(3.0, ServerProperties.getGriefThresholdWarn());
        assertEquals(8.0, ServerProperties.getGriefThresholdKick());
        assertEquals(15.0, ServerProperties.getGriefThresholdTempban());
    }

    // --- Helpers ---

    private void writeProps(String content) throws IOException {
        try (FileWriter fw = new FileWriter(new File(tempDir, "server.properties"))) {
            fw.write(content);
        }
    }
}
