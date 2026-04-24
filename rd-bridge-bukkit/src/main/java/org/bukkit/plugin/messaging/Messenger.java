package org.bukkit.plugin.messaging;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Messenger {
    public static final int MAX_MESSAGE_SIZE = 1048576;
    public static final int MAX_CHANNEL_SIZE = 0;
    boolean isReservedChannel(java.lang.String arg0);
    void registerOutgoingPluginChannel(org.bukkit.plugin.Plugin arg0, java.lang.String arg1);
    void unregisterOutgoingPluginChannel(org.bukkit.plugin.Plugin arg0, java.lang.String arg1);
    void unregisterOutgoingPluginChannel(org.bukkit.plugin.Plugin arg0);
    org.bukkit.plugin.messaging.PluginMessageListenerRegistration registerIncomingPluginChannel(org.bukkit.plugin.Plugin arg0, java.lang.String arg1, org.bukkit.plugin.messaging.PluginMessageListener arg2);
    void unregisterIncomingPluginChannel(org.bukkit.plugin.Plugin arg0, java.lang.String arg1, org.bukkit.plugin.messaging.PluginMessageListener arg2);
    void unregisterIncomingPluginChannel(org.bukkit.plugin.Plugin arg0, java.lang.String arg1);
    void unregisterIncomingPluginChannel(org.bukkit.plugin.Plugin arg0);
    java.util.Set getOutgoingChannels();
    java.util.Set getOutgoingChannels(org.bukkit.plugin.Plugin arg0);
    java.util.Set getIncomingChannels();
    java.util.Set getIncomingChannels(org.bukkit.plugin.Plugin arg0);
    java.util.Set getIncomingChannelRegistrations(org.bukkit.plugin.Plugin arg0);
    java.util.Set getIncomingChannelRegistrations(java.lang.String arg0);
    java.util.Set getIncomingChannelRegistrations(org.bukkit.plugin.Plugin arg0, java.lang.String arg1);
    boolean isRegistrationValid(org.bukkit.plugin.messaging.PluginMessageListenerRegistration arg0);
    boolean isIncomingChannelRegistered(org.bukkit.plugin.Plugin arg0, java.lang.String arg1);
    boolean isOutgoingChannelRegistered(org.bukkit.plugin.Plugin arg0, java.lang.String arg1);
    void dispatchIncomingMessage(org.bukkit.entity.Player arg0, java.lang.String arg1, byte[] arg2);
    void dispatchIncomingMessage(io.papermc.paper.connection.PlayerConnection arg0, java.lang.String arg1, byte[] arg2);
}
