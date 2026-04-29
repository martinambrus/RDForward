// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.meta;

import org.bukkit.inventory.meta.SkullMeta;

/**
 * Concrete {@link SkullMeta} backing returned by
 * {@link org.bukkit.inventory.ItemStack#getItemMeta()} when the stack's
 * material is a player head (legacy {@code Material.SKULL_ITEM} with
 * durability 3, or modern {@code Material.PLAYER_HEAD}). Stores the
 * owner's name so Essentials's /skull command can round-trip
 * {@code setOwner(name)} -> {@code hasOwner()}.
 *
 * <p>The richer profile fields (Mojang texture, OfflinePlayer linkage)
 * are not modelled — Essentials only cares about the name string and
 * whether an owner exists, and no other bridged plugin currently writes
 * skull profiles.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BridgeSkullMeta extends BridgeItemMeta implements SkullMeta {

    private String owner;

    @Override public String getOwner() { return owner; }
    @Override public boolean hasOwner() { return owner != null && !owner.isEmpty(); }
    @Override public boolean setOwner(String arg0) { this.owner = arg0; return true; }

    @Override public void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile arg0) {}
    @Override public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() { return null; }

    @Override public org.bukkit.OfflinePlayer getOwningPlayer() { return null; }
    @Override public boolean setOwningPlayer(org.bukkit.OfflinePlayer arg0) {
        this.owner = arg0 == null ? null : arg0.getName();
        return true;
    }

    @Override public org.bukkit.profile.PlayerProfile getOwnerProfile() { return null; }
    @Override public void setOwnerProfile(org.bukkit.profile.PlayerProfile arg0) {}

    @Override public void setNoteBlockSound(org.bukkit.NamespacedKey arg0) {}
    @Override public org.bukkit.NamespacedKey getNoteBlockSound() { return null; }

    @Override
    public SkullMeta clone() {
        return (SkullMeta) super.clone();
    }
}
