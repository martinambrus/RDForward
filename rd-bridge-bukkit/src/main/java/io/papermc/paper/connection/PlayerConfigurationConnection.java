package io.papermc.paper.connection;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PlayerConfigurationConnection extends io.papermc.paper.connection.PlayerCommonConnection {
    net.kyori.adventure.audience.Audience getAudience();
    com.destroystokyo.paper.profile.PlayerProfile getProfile();
    void clearChat();
    void completeReconfiguration();
}
