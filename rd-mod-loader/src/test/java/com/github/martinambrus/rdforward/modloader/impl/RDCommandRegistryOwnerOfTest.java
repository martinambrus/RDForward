package com.github.martinambrus.rdforward.modloader.impl;

import com.github.martinambrus.rdforward.api.command.Command;
import com.github.martinambrus.rdforward.modloader.admin.CommandConflictResolver;
import com.github.martinambrus.rdforward.server.api.CommandRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pinning regression test for the BukkitBridge conflict warning, which
 * reads {@link com.github.martinambrus.rdforward.api.command.CommandRegistry#ownerOf(String)}
 * to surface the existing claimant when a plugin's command name is
 * already taken. The default-method default returns null; the modloader
 * impl must delegate through {@link CommandConflictResolver#resolve} so
 * the warning shows the actual mod id (e.g. "claimed by 'Essentials'")
 * instead of the generic "claimed by another plugin".
 *
 * <p>Also pins that overrides flow through {@code ownerOf} so the
 * post-claim re-check in {@code BukkitPluginWrapper.registerCommands}
 * suppresses the warning when an op has already pinned the alias to
 * this plugin via {@code /commands assign}.
 */
class RDCommandRegistryOwnerOfTest {

    private RDCommandRegistry registry;
    private final Command noOp = ctx -> {};

    @BeforeEach
    void setUp(@TempDir Path dir) {
        clearStatics();
        CommandConflictResolver.install(dir.resolve("overrides.json"));
        registry = new RDCommandRegistry();
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
        CommandConflictResolver.unclaimAll(CommandConflictResolver.SERVER_OWNER);
        CommandConflictResolver.clearOverride("kick");
    }

    @Test
    void ownerOfReturnsNullForUnregisteredName() {
        assertNull(registry.ownerOf("kick"));
    }

    @Test
    void ownerOfReturnsClaimingMod() {
        registry.register("modA", "kick", "Kick player", noOp);
        assertEquals("modA", registry.ownerOf("kick"));
    }

    @Test
    void firstClaimWinsOnConflict() {
        registry.register("modA", "kick", "Kick player", noOp);
        registry.register("modB", "kick", "Kick player", noOp);
        assertEquals("modA", registry.ownerOf("kick"),
                "first claimant retains the bare alias when no override is in effect");
    }

    @Test
    void overrideRedirectsOwnerOf() {
        // Mirrors the operator workflow: /commands assign kick modB
        // while both mods have already claimed. ownerOf must report the
        // pinned mod so the bridge skips its conflict warning.
        registry.register("modA", "kick", "Kick player", noOp);
        registry.register("modB", "kick", "Kick player", noOp);
        CommandConflictResolver.setOverride("kick", "modB");
        assertEquals("modB", registry.ownerOf("kick"),
                "an active override must surface through ownerOf so warnings respect operator intent");
    }

    @Test
    void ownerOfReadsServerBuiltInsAfterInstall() {
        // Install snapshots existing rd-server commands as __server__
        // claims. ownerOf must surface that pseudo mod id so the bridge
        // can label the conflict source as the server itself.
        com.github.martinambrus.rdforward.server.api.Command serverNoOp = ctx -> {};
        CommandRegistry.register("kick", "Kick a player", serverNoOp);
        // Re-install picks up the new built-in.
        CommandConflictResolver.install(java.nio.file.Path.of(System.getProperty("java.io.tmpdir"), "ownerof-test-overrides.json"));
        assertEquals(CommandConflictResolver.SERVER_OWNER, registry.ownerOf("kick"));
    }

    @Test
    void ownerOfNullSafe() {
        assertNull(registry.ownerOf(null));
    }
}
