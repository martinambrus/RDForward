package org.bukkit.profile;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PlayerProfile extends java.lang.Cloneable, org.bukkit.configuration.serialization.ConfigurationSerializable {
    java.util.UUID getUniqueId();
    java.lang.String getName();
    org.bukkit.profile.PlayerTextures getTextures();
    void setTextures(org.bukkit.profile.PlayerTextures arg0);
    boolean isComplete();
    java.util.concurrent.CompletableFuture update();
    org.bukkit.profile.PlayerProfile clone();
}
