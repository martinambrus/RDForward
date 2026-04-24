package org.bukkit.profile;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PlayerTextures {
    boolean isEmpty();
    void clear();
    java.net.URL getSkin();
    void setSkin(java.net.URL arg0);
    void setSkin(java.net.URL arg0, org.bukkit.profile.PlayerTextures$SkinModel arg1);
    org.bukkit.profile.PlayerTextures$SkinModel getSkinModel();
    java.net.URL getCape();
    void setCape(java.net.URL arg0);
    long getTimestamp();
    boolean isSigned();
}
