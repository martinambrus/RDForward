package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    @Test
    void setPluginDefaultsExecutorToOwner() {
        // Real paper-api PluginCommand initialises executor to the
        // owning plugin (because JavaPlugin implements CommandExecutor).
        // mcbans 4.3.5 overrides JavaPlugin.onCommand directly without
        // calling getCommand(name).setExecutor(...); without this default
        // BukkitPluginWrapper.registerCommands skipped every command and
        // /kick, /ban etc never reached the registry.
        FixturePlugin owner = new FixturePlugin();
        PluginCommand cmd = new PluginCommand("ping");
        assertNull(cmd.getExecutor(), "executor null until owner is wired");
        cmd.setPlugin(owner);
        assertNotNull(cmd.getExecutor(),
                "setPlugin must default executor to plugin so JavaPlugin.onCommand routes the call");
        assertSame(owner, cmd.getExecutor(),
                "default executor must be the owning plugin itself");
    }

    @Test
    void twoArgConstructorAlsoDefaultsExecutor() {
        FixturePlugin owner = new FixturePlugin();
        PluginCommand cmd = new PluginCommand("ping", owner);
        assertSame(owner, cmd.getExecutor(),
                "two-arg ctor must mirror real paper-api and seed executor with the owner");
    }

    @Test
    void explicitSetExecutorWinsOverDefault() {
        FixturePlugin owner = new FixturePlugin();
        PluginCommand cmd = new PluginCommand("ping");
        cmd.setPlugin(owner);
        CommandExecutor custom = (sender, command, label, args) -> true;
        cmd.setExecutor(custom);
        assertSame(custom, cmd.getExecutor(),
                "an explicit setExecutor call (LuckPerms-style) must override the plugin default");
    }

    @Test
    void setPluginDoesNotOverwriteExplicitExecutor() {
        // Plugin order in real Bukkit: setExecutor can be called BEFORE
        // setPlugin in synthetic command paths. Loader runs setPlugin
        // last so a pre-set executor must survive.
        FixturePlugin owner = new FixturePlugin();
        PluginCommand cmd = new PluginCommand("ping");
        CommandExecutor custom = (sender, command, label, args) -> true;
        cmd.setExecutor(custom);
        cmd.setPlugin(owner);
        assertSame(custom, cmd.getExecutor(),
                "setPlugin only fills executor when null; existing assignment must persist");
    }
}
