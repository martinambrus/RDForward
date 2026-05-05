// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.command;

/**
 * Bukkit-shaped sender of a command. Concrete implementations are
 * {@link org.bukkit.entity.Player} (in-game player) and
 * {@link ConsoleCommandSender} (server console). Extends
 * {@link org.bukkit.permissions.Permissible} to match paper-api so
 * plugin bytecode that calls {@code sender.hasPermission(String)} via
 * {@code invokeinterface} on a {@link CommandSender}-typed reference
 * resolves to a real interface method (avoiding
 * {@link NoSuchMethodError} at link time).
 */
public interface CommandSender extends org.bukkit.permissions.Permissible {

    String getName();

    void sendMessage(String message);

    /** Multi-line convenience overload — real Bukkit fans the array out
     *  one element per line. MChannels 4.x's {@code sendDefaultMessage}
     *  calls this with a help-text array; without the declaration the
     *  command {@code NoSuchMethodError}s on every invocation. */
    default void sendMessage(String[] messages) {
        if (messages == null) return;
        for (String m : messages) {
            if (m != null) sendMessage(m);
        }
    }

    /** Pre-Adventure raw-message sink. Real Bukkit's {@code sendRawMessage}
     *  bypasses the per-sender chat formatting pipeline and writes the
     *  argument verbatim. RDForward has no such pipeline, so this just
     *  delegates to {@link #sendMessage(String)}. mChat 3.x's
     *  {@code MessageUtil.log} calls this on the console sender during
     *  {@code onEnable} — without a declaration the plugin
     *  {@code NoSuchMethodError}s at link time. */
    default void sendRawMessage(String message) {
        sendMessage(message);
    }

    /** @return true if this sender has operator privileges. */
    boolean isOp();

    /** Spigot inner class for rich-chat messaging. Plugins call
     *  {@code sender.spigot().sendMessage(BaseComponent)}. */
    @SuppressWarnings({"unchecked", "rawtypes", "unused"})
    class Spigot {
        public Spigot() {}
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent arg0) {
            com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.CommandSender$Spigot.sendMessage(Lnet/md_5/bungee/api/chat/BaseComponent;)V");
        }
        public void sendMessage(net.md_5.bungee.api.chat.BaseComponent[] arg0) {
            com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.CommandSender$Spigot.sendMessage([Lnet/md_5/bungee/api/chat/BaseComponent;)V");
        }
        public void sendMessage(java.util.UUID arg0, net.md_5.bungee.api.chat.BaseComponent arg1) {
            com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.CommandSender$Spigot.sendMessage(Ljava/util/UUID;Lnet/md_5/bungee/api/chat/BaseComponent;)V");
        }
        public void sendMessage(java.util.UUID arg0, net.md_5.bungee.api.chat.BaseComponent[] arg1) {
            com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.CommandSender$Spigot.sendMessage(Ljava/util/UUID;[Lnet/md_5/bungee/api/chat/BaseComponent;)V");
        }
    }
}
