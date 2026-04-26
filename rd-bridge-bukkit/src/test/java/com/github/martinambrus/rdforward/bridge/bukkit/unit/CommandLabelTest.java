package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.command.Command;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the {@link Command#getLabel()} contract that LuckPerms's
 * Bukkit command system relies on during {@code onEnable}: a freshly
 * constructed command must report its label as its primary name, and
 * {@code setLabel} must overwrite that value while still falling back to
 * the name when {@code null} is passed.
 */
class CommandLabelTest {

    @Test
    void labelDefaultsToName() {
        Command cmd = new Command("ping");
        assertEquals("ping", cmd.getLabel(),
                "fresh command must report its name as the active label");
    }

    @Test
    void setLabelOverwritesAndReturnsTrue() {
        Command cmd = new Command("ping");
        assertTrue(cmd.setLabel("pong"), "stub setLabel always succeeds");
        assertEquals("pong", cmd.getLabel());
    }

    @Test
    void setLabelNullFallsBackToName() {
        Command cmd = new Command("ping");
        cmd.setLabel("custom");
        cmd.setLabel(null);
        assertEquals("ping", cmd.getLabel(),
                "passing null restores the default — matches paper-api's null-tolerant behaviour");
    }
}
