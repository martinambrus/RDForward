package org.bukkit.plugin.messaging;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PluginMessageListener {
    void onPluginMessageReceived(java.lang.String arg0, org.bukkit.entity.Player arg1, byte[] arg2);
    default void onPluginMessageReceived(java.lang.String arg0, io.papermc.paper.connection.PlayerConnection arg1, byte[] arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.messaging.PluginMessageListener.onPluginMessageReceived(Ljava/lang/String;Lio/papermc/paper/connection/PlayerConnection;[B)V");
    }
}
