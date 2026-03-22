package com.github.martinambrus.rdforward.server.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CommandRegistry: registration, dispatch, permissions, argument parsing.
 */
class CommandRegistryTest {

    @TempDir
    File tempDir;

    private List<String> replies;
    private CommandContext.CommandSender replySender;

    @BeforeEach
    void setUp() {
        CommandRegistry.clearForTesting();
        PermissionManager.load(tempDir);
        replies = new ArrayList<>();
        replySender = replies::add;
    }

    @Test
    void registerAndDispatchCommand() {
        AtomicReference<String> received = new AtomicReference<>();
        CommandRegistry.register("testcmd1", "A test command", ctx -> received.set("executed"));

        boolean found = CommandRegistry.dispatch("testcmd1", "Player1", false, replySender);
        assertTrue(found);
        assertEquals("executed", received.get());
    }

    @Test
    void dispatchUnknownCommandReturnsFalse() {
        boolean found = CommandRegistry.dispatch("nonexistent_cmd_xyz", "Player1", false, replySender);
        assertFalse(found);
    }

    @Test
    void commandNameIsCaseInsensitive() {
        AtomicReference<String> received = new AtomicReference<>();
        CommandRegistry.register("TestCmd2", "Test", ctx -> received.set("ok"));

        assertTrue(CommandRegistry.dispatch("testcmd2", "Player1", false, replySender));
        assertEquals("ok", received.get());
    }

    @Test
    void commandReceivesArguments() {
        AtomicReference<String[]> receivedArgs = new AtomicReference<>();
        CommandRegistry.register("testcmd3", "Test", ctx -> receivedArgs.set(ctx.getArgs()));

        CommandRegistry.dispatch("testcmd3 arg1 arg2 arg3", "Player1", false, replySender);
        assertArrayEquals(new String[]{"arg1", "arg2", "arg3"}, receivedArgs.get());
    }

    @Test
    void commandWithNoArgsGetsEmptyArray() {
        AtomicReference<String[]> receivedArgs = new AtomicReference<>();
        CommandRegistry.register("testcmd4", "Test", ctx -> receivedArgs.set(ctx.getArgs()));

        CommandRegistry.dispatch("testcmd4", "Player1", false, replySender);
        assertEquals(0, receivedArgs.get().length);
    }

    @Test
    void opCommandBlocksNonOpPlayer() {
        AtomicReference<String> received = new AtomicReference<>("not_executed");
        CommandRegistry.registerOp("testcmd5", "Op only", ctx -> received.set("executed"));

        CommandRegistry.dispatch("testcmd5", "NonOpPlayer", false, replySender);
        assertEquals("not_executed", received.get());
        assertFalse(replies.isEmpty());
        assertTrue(replies.get(0).contains("permission"));
    }

    @Test
    void opCommandAllowedForConsole() {
        AtomicReference<String> received = new AtomicReference<>();
        CommandRegistry.registerOp("testcmd6", "Op only", ctx -> received.set("executed"));

        CommandRegistry.dispatch("testcmd6", "CONSOLE", true, replySender);
        assertEquals("executed", received.get());
    }

    @Test
    void commandContextHasSenderInfo() {
        AtomicReference<CommandContext> receivedCtx = new AtomicReference<>();
        CommandRegistry.register("testcmd7", "Test", receivedCtx::set);

        CommandRegistry.dispatch("testcmd7", "Alice", false, replySender);
        CommandContext ctx = receivedCtx.get();
        assertEquals("Alice", ctx.getSenderName());
        assertFalse(ctx.isConsole());
    }

    @Test
    void consoleCommandContextIsConsole() {
        AtomicReference<CommandContext> receivedCtx = new AtomicReference<>();
        CommandRegistry.register("testcmd8", "Test", receivedCtx::set);

        CommandRegistry.dispatch("testcmd8", "CONSOLE", true, replySender);
        assertTrue(receivedCtx.get().isConsole());
    }

    @Test
    void commandReplyReachesReplySender() {
        CommandRegistry.register("testcmd9", "Test", ctx -> ctx.reply("Hello back!"));

        CommandRegistry.dispatch("testcmd9", "Player1", false, replySender);
        assertEquals(1, replies.size());
        assertEquals("Hello back!", replies.get(0));
    }

    @Test
    void commandExceptionCaughtGracefully() {
        CommandRegistry.register("testcmd10", "Test", ctx -> {
            throw new RuntimeException("Something went wrong");
        });

        // Should not throw — player gets generic message
        boolean found = CommandRegistry.dispatch("testcmd10", "Player1", false, replySender);
        assertTrue(found);
        assertFalse(replies.isEmpty());
        assertTrue(replies.get(0).contains("internal error"));
    }

    @Test
    void getCommandsReturnsRegisteredCommands() {
        CommandRegistry.register("testcmd11", "Description here", ctx -> {});
        assertTrue(CommandRegistry.getCommands().containsKey("testcmd11"));
        assertEquals("Description here", CommandRegistry.getCommands().get("testcmd11").description);
    }

    @Test
    void getCommandsIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () ->
                CommandRegistry.getCommands().put("illegal", null));
    }

    // --- Op level tests ---

    private void withTempOp(String player, int level, Runnable test) {
        PermissionManager.addOp(player, level);
        try {
            test.run();
        } finally {
            PermissionManager.removeOp(player);
        }
    }

    @Test
    void level2CommandAllowsLevel2Op() {
        AtomicReference<String> received = new AtomicReference<>("not_executed");
        CommandRegistry.registerOp("testcmd_lv2a", "Level 2 cmd", 2, ctx -> received.set("executed"));

        withTempOp("Level2Player", 2, () -> {
            CommandRegistry.dispatch("testcmd_lv2a", "Level2Player", false, replySender);
            assertEquals("executed", received.get());
        });
    }

    @Test
    void level3CommandBlocksLevel2Op() {
        AtomicReference<String> received = new AtomicReference<>("not_executed");
        CommandRegistry.registerOp("testcmd_lv3block", "Level 3 cmd", 3, ctx -> received.set("executed"));

        withTempOp("Level2Only", 2, () -> {
            CommandRegistry.dispatch("testcmd_lv3block", "Level2Only", false, replySender);
            assertEquals("not_executed", received.get());
            assertFalse(replies.isEmpty());
            assertTrue(replies.get(0).contains("permission"));
        });
    }

    @Test
    void level4CommandAllowsLevel4Op() {
        AtomicReference<String> received = new AtomicReference<>("not_executed");
        CommandRegistry.registerOp("testcmd_lv4a", "Level 4 cmd", 4, ctx -> received.set("executed"));

        withTempOp("AdminPlayer", 4, () -> {
            CommandRegistry.dispatch("testcmd_lv4a", "AdminPlayer", false, replySender);
            assertEquals("executed", received.get());
        });
    }

    @Test
    void level4CommandBlocksLevel3Op() {
        AtomicReference<String> received = new AtomicReference<>("not_executed");
        CommandRegistry.registerOp("testcmd_lv4block", "Level 4 cmd", 4, ctx -> received.set("executed"));

        withTempOp("ModPlayer", 3, () -> {
            CommandRegistry.dispatch("testcmd_lv4block", "ModPlayer", false, replySender);
            assertEquals("not_executed", received.get());
            assertFalse(replies.isEmpty());
            assertTrue(replies.get(0).contains("permission"));
        });
    }

    @Test
    void higherLevelAllowsLowerLevelCommand() {
        AtomicReference<String> received = new AtomicReference<>("not_executed");
        CommandRegistry.registerOp("testcmd_lvhi", "Level 2 cmd", 2, ctx -> received.set("executed"));

        withTempOp("HighLevelPlayer", 4, () -> {
            CommandRegistry.dispatch("testcmd_lvhi", "HighLevelPlayer", false, replySender);
            assertEquals("executed", received.get());
        });
    }

    @Test
    void registeredCommandExposesRequiredOpLevel() {
        CommandRegistry.registerOp("testcmd_lvinfo", "Info cmd", 2, ctx -> {});
        CommandRegistry.RegisteredCommand cmd = CommandRegistry.getCommands().get("testcmd_lvinfo");
        assertEquals(2, cmd.requiredOpLevel);
    }

    @Test
    void publicCommandHasZeroOpLevel() {
        CommandRegistry.register("testcmd_pub", "Public cmd", ctx -> {});
        CommandRegistry.RegisteredCommand cmd = CommandRegistry.getCommands().get("testcmd_pub");
        assertEquals(0, cmd.requiredOpLevel);
    }

    // --- Edge case tests ---

    @Test
    void registerOpRejectsZeroOpLevel() {
        assertThrows(IllegalArgumentException.class, () ->
                CommandRegistry.registerOp("bad_lv0", "Bad", 0, ctx -> {}));
    }

    @Test
    void registerOpRejectsNegativeOpLevel() {
        assertThrows(IllegalArgumentException.class, () ->
                CommandRegistry.registerOp("bad_neg", "Bad", -1, ctx -> {}));
    }

    @Test
    void registerOpRejectsExcessiveOpLevel() {
        assertThrows(IllegalArgumentException.class, () ->
                CommandRegistry.registerOp("bad_high", "Bad", 5, ctx -> {}));
    }

    @Test
    void duplicateRegistrationOverwrites() {
        CommandRegistry.register("dup_cmd", "First", ctx -> ctx.reply("first"));
        CommandRegistry.register("dup_cmd", "Second", ctx -> ctx.reply("second"));

        CommandRegistry.dispatch("dup_cmd", "Player1", false, replySender);
        assertEquals("second", replies.get(0));
        assertEquals("Second", CommandRegistry.getCommands().get("dup_cmd").description);
    }

    @Test
    void opLevelIsCaseInsensitiveForUsernames() {
        AtomicReference<String> received = new AtomicReference<>("not_executed");
        CommandRegistry.registerOp("testcmd_case", "Op cmd", 2, ctx -> received.set("executed"));

        // Add op as "Alice" but dispatch as "alice" — should still work
        PermissionManager.addOp("Alice", 2);
        try {
            CommandRegistry.dispatch("testcmd_case", "alice", false, replySender);
            assertEquals("executed", received.get());
        } finally {
            PermissionManager.removeOp("Alice");
        }
    }

    @Test
    void addOpRejectsColonInUsername() {
        assertThrows(IllegalArgumentException.class, () ->
                PermissionManager.addOp("evil:name", 2));
    }

    @Test
    void addOpRejectsEmptyUsername() {
        assertThrows(IllegalArgumentException.class, () ->
                PermissionManager.addOp("", 2));
    }
}
