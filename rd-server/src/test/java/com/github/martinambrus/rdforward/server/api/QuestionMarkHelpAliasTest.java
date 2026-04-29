package com.github.martinambrus.rdforward.server.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins that the {@code ?} alias dispatches the same handler as
 * {@code help}. CraftBukkit's {@code HelpCommand} registers
 * {@code "?"} as an alias for help, and Essentials's banner instructs
 * console operators to "type ? to view help" — without the alias,
 * typing {@code ?} in the console resolves to nothing and prints
 * "Unknown command".
 *
 * <p>Tests {@link CommandRegistry} dispatch directly. The actual
 * registration happens in {@code RDServer.registerBuiltInCommands},
 * which is private and exercised by the live server boot path; the
 * test stand-in registers the alias the same way.
 */
class QuestionMarkHelpAliasTest {

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
    void questionMarkDispatchesHelpHandler() {
        AtomicInteger calls = new AtomicInteger();
        Command helpHandler = ctx -> calls.incrementAndGet();
        CommandRegistry.register("help", "Show available commands", helpHandler);
        CommandRegistry.register("?", "Alias for help", helpHandler);

        // Console-typed "?": dispatch must resolve.
        boolean found = CommandRegistry.dispatch("?", "CONSOLE", true, replySender);
        assertTrue(found, "? must resolve to a registered command");
        assertEquals(1, calls.get(), "? must invoke the help handler exactly once");
    }

    @Test
    void questionMarkAndHelpShareHandler() {
        AtomicInteger calls = new AtomicInteger();
        Command helpHandler = ctx -> calls.incrementAndGet();
        CommandRegistry.register("help", "Show available commands", helpHandler);
        CommandRegistry.register("?", "Alias for help", helpHandler);

        CommandRegistry.dispatch("help", "CONSOLE", true, replySender);
        CommandRegistry.dispatch("?", "CONSOLE", true, replySender);
        assertEquals(2, calls.get(),
                "both /help and /? must hit the same shared handler");
    }
}
