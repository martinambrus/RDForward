package com.github.martinambrus.rdforward.server.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CommandRegistry: registration, dispatch, permissions, argument parsing.
 */
class CommandRegistryTest {

    private List<String> replies;
    private CommandContext.CommandSender replySender;

    @BeforeEach
    void setUp() {
        // Clear the static registry between tests by re-registering
        // We'll use unique command names per test to avoid cross-contamination
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

        // Should not throw
        boolean found = CommandRegistry.dispatch("testcmd10", "Player1", false, replySender);
        assertTrue(found);
        assertFalse(replies.isEmpty());
        assertTrue(replies.get(0).contains("error") || replies.get(0).contains("Error"));
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
}
