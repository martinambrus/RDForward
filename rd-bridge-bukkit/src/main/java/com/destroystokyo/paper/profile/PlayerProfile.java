package com.destroystokyo.paper.profile;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PlayerProfile extends org.bukkit.profile.PlayerProfile, net.kyori.adventure.text.object.PlayerHeadObjectContents$SkinSource {
    java.lang.String getName();
    java.lang.String setName(java.lang.String arg0);
    java.util.UUID getId();
    java.util.UUID setId(java.util.UUID arg0);
    org.bukkit.profile.PlayerTextures getTextures();
    void setTextures(org.bukkit.profile.PlayerTextures arg0);
    java.util.Set getProperties();
    boolean hasProperty(java.lang.String arg0);
    void setProperty(com.destroystokyo.paper.profile.ProfileProperty arg0);
    void setProperties(java.util.Collection arg0);
    boolean removeProperty(java.lang.String arg0);
    default boolean removeProperty(com.destroystokyo.paper.profile.ProfileProperty arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.profile.PlayerProfile.removeProperty(Lcom/destroystokyo/paper/profile/ProfileProperty;)Z");
        return false;
    }
    default boolean removeProperties(java.util.Collection arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.profile.PlayerProfile.removeProperties(Ljava/util/Collection;)Z");
        return false;
    }
    void clearProperties();
    boolean isComplete();
    boolean completeFromCache();
    boolean completeFromCache(boolean arg0);
    boolean completeFromCache(boolean arg0, boolean arg1);
    default boolean complete() {
        return false;
    }
    boolean complete(boolean arg0);
    boolean complete(boolean arg0, boolean arg1);
    java.util.concurrent.CompletableFuture update();
    default boolean hasTextures() {
        return false;
    }
    com.destroystokyo.paper.profile.PlayerProfile clone();
    default void applySkinToPlayerHeadContents(net.kyori.adventure.text.object.PlayerHeadObjectContents$Builder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.profile.PlayerProfile.applySkinToPlayerHeadContents(Lnet/kyori/adventure/text/object/PlayerHeadObjectContents$Builder;)V");
    }
}
