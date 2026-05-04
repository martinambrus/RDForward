package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test for HomeSpawnPlus dynamic command dispatch. HSP
 * registers PluginCommand instances via commandMap.register(); the
 * bridge's mirrorDynamicCommand calls cmd.execute(). Without the
 * override in PluginCommand, the base-class no-op swallowed every
 * invocation.
 */
class PluginCommandExecuteTest {

    @Test
    void executeDelegatesToExecutorOnCommand() {
        PluginCommand cmd = new PluginCommand("test");
        boolean[] called = {false};
        cmd.setExecutor((sender, command, label, args) -> {
            called[0] = true;
            assertEquals("test", label);
            assertArrayEquals(new String[]{"a", "b"}, args);
            return true;
        });
        CommandSender sender = new StubSender();
        assertTrue(cmd.execute(sender, "test", new String[]{"a", "b"}));
        assertTrue(called[0], "executor.onCommand must be called");
    }

    @Test
    void executeReturnsExecutorResultTrue() {
        PluginCommand cmd = new PluginCommand("test");
        cmd.setExecutor((s, c, l, a) -> true);
        assertTrue(cmd.execute(new StubSender(), "test", new String[0]));
    }

    @Test
    void executeReturnsExecutorResultFalse() {
        PluginCommand cmd = new PluginCommand("test");
        cmd.setExecutor((s, c, l, a) -> false);
        assertFalse(cmd.execute(new StubSender(), "test", new String[0]));
    }

    @Test
    void executeReturnsFalseWhenNoExecutor() {
        PluginCommand cmd = new PluginCommand("test");
        assertFalse(cmd.execute(new StubSender(), "test", new String[0]),
                "execute must return false when no executor is set");
    }

    private static class StubSender implements CommandSender {
        @Override public String getName() { return "TestSender"; }
        @Override public void sendMessage(String message) {}
        @Override public boolean isOp() { return true; }
        @Override public void setOp(boolean op) {}
        @Override public boolean isPermissionSet(String name) { return false; }
        @Override public boolean isPermissionSet(org.bukkit.permissions.Permission p) { return false; }
        @Override public boolean hasPermission(String name) { return true; }
        @Override public boolean hasPermission(org.bukkit.permissions.Permission p) { return true; }
        @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin p, String n, boolean v) { return null; }
        @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin p) { return null; }
        @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin p, String n, boolean v, int t) { return null; }
        @Override public org.bukkit.permissions.PermissionAttachment addAttachment(org.bukkit.plugin.Plugin p, int t) { return null; }
        @Override public void removeAttachment(org.bukkit.permissions.PermissionAttachment a) {}
        @Override public void recalculatePermissions() {}
        @Override public java.util.Set<org.bukkit.permissions.PermissionAttachmentInfo> getEffectivePermissions() { return java.util.Collections.emptySet(); }
    }
}
