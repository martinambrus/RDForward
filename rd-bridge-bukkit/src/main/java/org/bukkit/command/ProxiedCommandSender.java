package org.bukkit.command;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ProxiedCommandSender extends org.bukkit.command.CommandSender, net.kyori.adventure.audience.ForwardingAudience$Single {
    org.bukkit.command.CommandSender getCaller();
    org.bukkit.command.CommandSender getCallee();
    default void sendMessage(net.kyori.adventure.identity.Identity arg0, net.kyori.adventure.text.Component arg1, net.kyori.adventure.audience.MessageType arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.command.ProxiedCommandSender.sendMessage(Lnet/kyori/adventure/identity/Identity;Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/audience/MessageType;)V");
    }
    default net.kyori.adventure.audience.Audience audience() {
        return null;
    }
}
