package com.github.martinambrus.rdforward.modloader.admin;

import com.github.martinambrus.rdforward.server.api.Command;
import com.github.martinambrus.rdforward.server.api.CommandRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandConflictResolverTest {

    private Command noOp;

    @BeforeEach
    void setUp() {
        clearStatics();
        noOp = ctx -> {};
    }

    @AfterEach
    void tearDown() {
        clearStatics();
    }

    private void clearStatics() {
        for (String name : CommandRegistry.getCommands().keySet().toArray(String[]::new)) {
            CommandRegistry.unregister(name);
        }
        CommandConflictResolver.unclaimAll("modA");
        CommandConflictResolver.unclaimAll("modB");
        CommandConflictResolver.unclaimAll("modC");
        CommandConflictResolver.unclaimAll(CommandConflictResolver.SERVER_OWNER);
        CommandConflictResolver.clearOverride("heal");
        CommandConflictResolver.clearOverride("spawn");
    }

    @Test
    void firstClaimWinsWhenNoConflict(@TempDir Path dir) {
        CommandConflictResolver.install(dir.resolve("overrides.json"));
        CommandConflictResolver.claim("modA", "heal", "Heal player", 0, noOp);

        assertEquals("modA", CommandConflictResolver.resolve("heal"));
        assertNotNull(CommandRegistry.getCommands().get("heal"));
    }

    @Test
    void serverClaimBeatsMod(@TempDir Path dir) {
        CommandRegistry.register("spawn", "Go to spawn", noOp);
        CommandConflictResolver.install(dir.resolve("overrides.json"));
        CommandConflictResolver.claim("modA", "spawn", "Mod spawn", 0, noOp);

        assertEquals(CommandConflictResolver.SERVER_OWNER, CommandConflictResolver.resolve("spawn"));
    }

    @Test
    void explicitOverrideWinsOverServerAndFirstClaim(@TempDir Path dir) {
        CommandRegistry.register("spawn", "Go to spawn", noOp);
        CommandConflictResolver.install(dir.resolve("overrides.json"));
        CommandConflictResolver.claim("modA", "spawn", "modA spawn", 0, noOp);
        CommandConflictResolver.claim("modB", "spawn", "modB spawn", 0, noOp);

        CommandConflictResolver.setOverride("spawn", "modB");
        assertEquals("modB", CommandConflictResolver.resolve("spawn"));
    }

    @Test
    void conflictedNamesListsMultiClaimedOnly(@TempDir Path dir) {
        CommandConflictResolver.install(dir.resolve("overrides.json"));
        CommandConflictResolver.claim("modA", "heal", "", 0, noOp);
        CommandConflictResolver.claim("modA", "unique", "", 0, noOp);
        CommandConflictResolver.claim("modB", "heal", "", 0, noOp);

        assertEquals(1, CommandConflictResolver.conflictedNames().size());
        assertTrue(CommandConflictResolver.conflictedNames().contains("heal"));
    }

    @Test
    void unclaimDropsWinnerAndPicksNext(@TempDir Path dir) {
        CommandConflictResolver.install(dir.resolve("overrides.json"));
        CommandConflictResolver.claim("modA", "heal", "A", 0, noOp);
        CommandConflictResolver.claim("modB", "heal", "B", 0, noOp);

        assertEquals("modA", CommandConflictResolver.resolve("heal"));
        CommandConflictResolver.unclaim("modA", "heal");
        assertEquals("modB", CommandConflictResolver.resolve("heal"));
    }

    @Test
    void unclaimOfLastClaimantRemovesBareAlias(@TempDir Path dir) {
        CommandConflictResolver.install(dir.resolve("overrides.json"));
        CommandConflictResolver.claim("modA", "solo", "", 0, noOp);
        assertNotNull(CommandRegistry.getCommands().get("solo"));

        CommandConflictResolver.unclaim("modA", "solo");
        assertNull(CommandConflictResolver.resolve("solo"));
        assertFalse(CommandRegistry.getCommands().containsKey("solo"));
    }

    @Test
    void unclaimAllClearsEveryClaimByOwner(@TempDir Path dir) {
        CommandConflictResolver.install(dir.resolve("overrides.json"));
        CommandConflictResolver.claim("modA", "one", "", 0, noOp);
        CommandConflictResolver.claim("modA", "two", "", 0, noOp);
        CommandConflictResolver.claim("modB", "two", "", 0, noOp);

        int dropped = CommandConflictResolver.unclaimAll("modA");
        assertEquals(2, dropped);
        assertNull(CommandConflictResolver.resolve("one"));
        assertEquals("modB", CommandConflictResolver.resolve("two"));
    }

    @Test
    void overrideClearRevertsToDefaultRule(@TempDir Path dir) {
        CommandConflictResolver.install(dir.resolve("overrides.json"));
        CommandConflictResolver.claim("modA", "heal", "A", 0, noOp);
        CommandConflictResolver.claim("modB", "heal", "B", 0, noOp);

        CommandConflictResolver.setOverride("heal", "modB");
        assertEquals("modB", CommandConflictResolver.resolve("heal"));

        CommandConflictResolver.clearOverride("heal");
        assertEquals("modA", CommandConflictResolver.resolve("heal"));
    }

    @Test
    void overridePersistsToJson(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("overrides.json");
        CommandConflictResolver.install(file);
        CommandConflictResolver.claim("modA", "heal", "A", 0, noOp);
        CommandConflictResolver.claim("modB", "heal", "B", 0, noOp);
        CommandConflictResolver.setOverride("heal", "modB");

        String json = Files.readString(file);
        assertTrue(json.contains("heal"));
        assertTrue(json.contains("modB"));
    }

    @Test
    void overrideReloadsFromFile(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("overrides.json");
        Files.writeString(file, "{\"overrides\":{\"heal\":\"modB\"}}");

        CommandConflictResolver.install(file);
        CommandConflictResolver.claim("modA", "heal", "A", 0, noOp);
        CommandConflictResolver.claim("modB", "heal", "B", 0, noOp);

        assertEquals("modB", CommandConflictResolver.resolve("heal"));
    }

    @Test
    void opLevelCommandBindsAsOpCommand(@TempDir Path dir) {
        CommandConflictResolver.install(dir.resolve("overrides.json"));
        CommandConflictResolver.claim("modA", "kick", "Kick player", 3, noOp);

        CommandRegistry.RegisteredCommand reg = CommandRegistry.getCommands().get("kick");
        assertNotNull(reg);
        assertEquals(3, reg.requiredOpLevel);
    }

    @Test
    void reconcileDropsOverrideForMissingMod(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("overrides.json");
        Files.writeString(file, "{\"overrides\":{\"heal\":\"modGone\"}}");
        CommandConflictResolver.install(file);
        CommandConflictResolver.claim("modA", "heal", "A", 0, noOp);

        int dropped = CommandConflictResolver.reconcile(id -> "modA".equals(id));
        assertEquals(1, dropped);
        assertEquals("modA", CommandConflictResolver.resolve("heal"),
                "after override drop, default rules pick the single remaining claimant");
        assertFalse(Files.readString(file).contains("modGone"),
                "persisted file should no longer reference the missing mod");
    }

    @Test
    void reconcileKeepsOverrideForPresentMod(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("overrides.json");
        Files.writeString(file, "{\"overrides\":{\"heal\":\"modB\"}}");
        CommandConflictResolver.install(file);
        CommandConflictResolver.claim("modA", "heal", "A", 0, noOp);
        CommandConflictResolver.claim("modB", "heal", "B", 0, noOp);

        int dropped = CommandConflictResolver.reconcile(id -> true);
        assertEquals(0, dropped);
        assertEquals("modB", CommandConflictResolver.resolve("heal"));
    }

    @Test
    void reconcileIgnoresServerOwnerOverride(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("overrides.json");
        Files.writeString(file, "{\"overrides\":{\"spawn\":\"__server__\"}}");
        CommandRegistry.register("spawn", "Go to spawn", noOp);
        CommandConflictResolver.install(file);

        int dropped = CommandConflictResolver.reconcile(id -> false);
        assertEquals(0, dropped, "server-owner overrides must never be dropped on reconcile");
        assertEquals(CommandConflictResolver.SERVER_OWNER, CommandConflictResolver.resolve("spawn"));
    }

    @Test
    void installSnapshotCapturesExistingServerCommands(@TempDir Path dir) {
        CommandRegistry.register("preexisting", "Was here first", noOp);
        CommandConflictResolver.install(dir.resolve("overrides.json"));

        assertEquals(CommandConflictResolver.SERVER_OWNER,
                CommandConflictResolver.resolve("preexisting"));
    }
}
