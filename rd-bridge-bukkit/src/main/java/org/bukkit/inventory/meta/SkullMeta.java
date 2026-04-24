package org.bukkit.inventory.meta;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SkullMeta extends org.bukkit.inventory.meta.ItemMeta {
    java.lang.String getOwner();
    boolean hasOwner();
    boolean setOwner(java.lang.String arg0);
    void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile arg0);
    com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile();
    org.bukkit.OfflinePlayer getOwningPlayer();
    boolean setOwningPlayer(org.bukkit.OfflinePlayer arg0);
    org.bukkit.profile.PlayerProfile getOwnerProfile();
    void setOwnerProfile(org.bukkit.profile.PlayerProfile arg0);
    void setNoteBlockSound(org.bukkit.NamespacedKey arg0);
    org.bukkit.NamespacedKey getNoteBlockSound();
    org.bukkit.inventory.meta.SkullMeta clone();
}
