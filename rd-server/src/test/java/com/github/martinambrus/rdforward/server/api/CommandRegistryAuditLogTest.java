package com.github.martinambrus.rdforward.server.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Player-issued slash commands must land in the server console for
 * security review (operators want to see what was run after the fact,
 * even if the command itself doesn't echo). Console-issued commands
 * are not re-logged here — the console already echoed them.
 */
class CommandRegistryAuditLogTest {

    @TempDir
    File tempDir;

    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();
    private PrintStream originalOut;
    private List<String> replies;
    private CommandContext.CommandSender replySender;

    @BeforeEach
    void setUp() {
        CommandRegistry.clearForTesting();
        PermissionManager.load(tempDir);
        originalOut = System.out;
        System.setOut(new PrintStream(captured, true));
        replies = new ArrayList<>();
        replySender = replies::add;
        // Register a noop command so dispatch finds something — audit
        // log fires for unknown commands too, so we want both paths.
        CommandRegistry.register("auditme", "audit fixture", ctx -> {});
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void dispatchFromPlayerLogsCommandToConsole() {
        CommandRegistry.dispatch("auditme arg1 arg2", "alpha", false, replySender);
        String stdout = captured.toString();
        assertTrue(stdout.contains("[CMD] alpha: /auditme arg1 arg2"),
                "expected audit line; got: " + stdout);
    }

    @Test
    void dispatchFromConsoleDoesNotLogAuditLine() {
        CommandRegistry.dispatch("auditme silent", "@console", true, replySender);
        String stdout = captured.toString();
        assertFalse(stdout.contains("[CMD]"),
                "console commands must not produce an audit line; got: " + stdout);
    }

    @Test
    void unknownPlayerCommandStillAuditLogs() {
        // The audit log lives at the top of dispatch — unmatched
        // commands (returning false) must still appear in the log so
        // operators see attempted misuse.
        CommandRegistry.dispatch("does-not-exist", "alpha", false, replySender);
        String stdout = captured.toString();
        assertTrue(stdout.contains("[CMD] alpha: /does-not-exist"),
                "unknown commands must still be audited; got: " + stdout);
    }
}
