package io.papermc.paper.connection;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PlayerLoginConnection extends io.papermc.paper.connection.ReadablePlayerCookieConnection {
    com.destroystokyo.paper.profile.PlayerProfile getAuthenticatedProfile();
    com.destroystokyo.paper.profile.PlayerProfile getUnsafeProfile();
}
