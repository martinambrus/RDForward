package io.papermc.paper.connection;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PlayerCommonConnection extends io.papermc.paper.connection.WritablePlayerCookieConnection, io.papermc.paper.connection.ReadablePlayerCookieConnection, org.bukkit.plugin.messaging.PluginMessageRecipient {
    void sendReportDetails(java.util.Map arg0);
    void sendLinks(org.bukkit.ServerLinks arg0);
    void transfer(java.lang.String arg0, int arg1);
    java.lang.Object getClientOption(com.destroystokyo.paper.ClientOption arg0);
}
