package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pinning regression test for LuckPerms's command system, which casts
 * loaded commands to {@link PluginIdentifiableCommand} and calls
 * {@link PluginIdentifiableCommand#getPlugin()} to look up the owning
 * plugin during {@code onEnable}. Without the owner field the cast still
 * succeeded but the call returned {@code null}, leading to
 * {@code NoSuchMethodError} or downstream NPEs.
 */
class PluginCommandOwnerTest {

    private static final class FixturePlugin extends JavaPlugin {}

    @Test
    void implementsPluginIdentifiableCommand() {
        PluginCommand cmd = new PluginCommand("ping");
        assertTrue(cmd instanceof PluginIdentifiableCommand,
                "PluginCommand must implement PluginIdentifiableCommand for plugin casts");
    }

    @Test
    void ownerNullByDefault() {
        assertNull(new PluginCommand("ping").getPlugin(),
                "freshly constructed command has no owner until the loader sets one");
    }

    @Test
    void setPluginExposesViaGetPlugin() {
        FixturePlugin owner = new FixturePlugin();
        PluginCommand cmd = new PluginCommand("ping");
        cmd.setPlugin(owner);
        assertSame(owner, cmd.getPlugin(),
                "setPlugin must publish the owner so PluginIdentifiableCommand.getPlugin() returns it");
    }

    @Test
    void twoArgConstructorEagerlyAssignsOwner() {
        FixturePlugin owner = new FixturePlugin();
        PluginCommand cmd = new PluginCommand("ping", owner);
        assertSame(owner, cmd.getPlugin());
    }
}
