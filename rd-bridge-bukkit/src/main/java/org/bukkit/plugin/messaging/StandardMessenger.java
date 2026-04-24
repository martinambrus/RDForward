package org.bukkit.plugin.messaging;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class StandardMessenger implements org.bukkit.plugin.messaging.Messenger {
    public StandardMessenger() {}
    public boolean isReservedChannel(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.isReservedChannel(Ljava/lang/String;)Z");
        return false;
    }
    public void registerOutgoingPluginChannel(org.bukkit.plugin.Plugin arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.registerOutgoingPluginChannel(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;)V");
    }
    public void unregisterOutgoingPluginChannel(org.bukkit.plugin.Plugin arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.unregisterOutgoingPluginChannel(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;)V");
    }
    public void unregisterOutgoingPluginChannel(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.unregisterOutgoingPluginChannel(Lorg/bukkit/plugin/Plugin;)V");
    }
    public org.bukkit.plugin.messaging.PluginMessageListenerRegistration registerIncomingPluginChannel(org.bukkit.plugin.Plugin arg0, java.lang.String arg1, org.bukkit.plugin.messaging.PluginMessageListener arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.registerIncomingPluginChannel(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;Lorg/bukkit/plugin/messaging/PluginMessageListener;)Lorg/bukkit/plugin/messaging/PluginMessageListenerRegistration;");
        return null;
    }
    public void unregisterIncomingPluginChannel(org.bukkit.plugin.Plugin arg0, java.lang.String arg1, org.bukkit.plugin.messaging.PluginMessageListener arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.unregisterIncomingPluginChannel(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;Lorg/bukkit/plugin/messaging/PluginMessageListener;)V");
    }
    public void unregisterIncomingPluginChannel(org.bukkit.plugin.Plugin arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.unregisterIncomingPluginChannel(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;)V");
    }
    public void unregisterIncomingPluginChannel(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.unregisterIncomingPluginChannel(Lorg/bukkit/plugin/Plugin;)V");
    }
    public java.util.Set getOutgoingChannels() {
        return java.util.Collections.emptySet();
    }
    public java.util.Set getOutgoingChannels(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.getOutgoingChannels(Lorg/bukkit/plugin/Plugin;)Ljava/util/Set;");
        return java.util.Collections.emptySet();
    }
    public java.util.Set getIncomingChannels() {
        return java.util.Collections.emptySet();
    }
    public java.util.Set getIncomingChannels(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.getIncomingChannels(Lorg/bukkit/plugin/Plugin;)Ljava/util/Set;");
        return java.util.Collections.emptySet();
    }
    public java.util.Set getIncomingChannelRegistrations(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.getIncomingChannelRegistrations(Lorg/bukkit/plugin/Plugin;)Ljava/util/Set;");
        return java.util.Collections.emptySet();
    }
    public java.util.Set getIncomingChannelRegistrations(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.getIncomingChannelRegistrations(Ljava/lang/String;)Ljava/util/Set;");
        return java.util.Collections.emptySet();
    }
    public java.util.Set getIncomingChannelRegistrations(org.bukkit.plugin.Plugin arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.getIncomingChannelRegistrations(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;)Ljava/util/Set;");
        return java.util.Collections.emptySet();
    }
    public boolean isRegistrationValid(org.bukkit.plugin.messaging.PluginMessageListenerRegistration arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.isRegistrationValid(Lorg/bukkit/plugin/messaging/PluginMessageListenerRegistration;)Z");
        return false;
    }
    public boolean isIncomingChannelRegistered(org.bukkit.plugin.Plugin arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.isIncomingChannelRegistered(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;)Z");
        return false;
    }
    public boolean isOutgoingChannelRegistered(org.bukkit.plugin.Plugin arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.isOutgoingChannelRegistered(Lorg/bukkit/plugin/Plugin;Ljava/lang/String;)Z");
        return false;
    }
    public void dispatchIncomingMessage(org.bukkit.entity.Player arg0, java.lang.String arg1, byte[] arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.dispatchIncomingMessage(Lorg/bukkit/entity/Player;Ljava/lang/String;[B)V");
    }
    public void dispatchIncomingMessage(io.papermc.paper.connection.PlayerConnection arg0, java.lang.String arg1, byte[] arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.dispatchIncomingMessage(Lio/papermc/paper/connection/PlayerConnection;Ljava/lang/String;[B)V");
    }
    public static void validateChannel(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.validateChannel(Ljava/lang/String;)V");
    }
    public static java.lang.String validateAndCorrectChannel(java.lang.String arg0) {
        return null;
    }
    public static void validatePluginMessage(org.bukkit.plugin.messaging.Messenger arg0, org.bukkit.plugin.Plugin arg1, java.lang.String arg2, byte[] arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.StandardMessenger.validatePluginMessage(Lorg/bukkit/plugin/messaging/Messenger;Lorg/bukkit/plugin/Plugin;Ljava/lang/String;[B)V");
    }
}
