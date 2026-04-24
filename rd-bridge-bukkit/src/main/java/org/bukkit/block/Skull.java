package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Skull extends org.bukkit.block.TileState {
    io.papermc.paper.datacomponent.item.ResolvableProfile getProfile();
    void setProfile(io.papermc.paper.datacomponent.item.ResolvableProfile arg0);
    boolean hasOwner();
    java.lang.String getOwner();
    boolean setOwner(java.lang.String arg0);
    org.bukkit.OfflinePlayer getOwningPlayer();
    void setOwningPlayer(org.bukkit.OfflinePlayer arg0);
    void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile arg0);
    com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile();
    org.bukkit.profile.PlayerProfile getOwnerProfile();
    void setOwnerProfile(org.bukkit.profile.PlayerProfile arg0);
    org.bukkit.NamespacedKey getNoteBlockSound();
    void setNoteBlockSound(org.bukkit.NamespacedKey arg0);
    org.bukkit.block.BlockFace getRotation();
    void setRotation(org.bukkit.block.BlockFace arg0);
    org.bukkit.SkullType getSkullType();
    void setSkullType(org.bukkit.SkullType arg0);
    net.kyori.adventure.text.Component customName();
    void customName(net.kyori.adventure.text.Component arg0);
}
