package com.destroystokyo.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerClientOptionsChangeEvent extends org.bukkit.event.player.PlayerEvent {
    public PlayerClientOptionsChangeEvent(org.bukkit.entity.Player arg0, java.util.Map arg1) { super((org.bukkit.entity.Player) null); }
    public PlayerClientOptionsChangeEvent() { super((org.bukkit.entity.Player) null); }
    public java.lang.String getLocale() {
        return null;
    }
    public boolean hasLocaleChanged() {
        return false;
    }
    public int getViewDistance() {
        return 0;
    }
    public boolean hasViewDistanceChanged() {
        return false;
    }
    public com.destroystokyo.paper.ClientOption$ChatVisibility getChatVisibility() {
        return null;
    }
    public boolean hasChatVisibilityChanged() {
        return false;
    }
    public boolean hasChatColorsEnabled() {
        return false;
    }
    public boolean hasChatColorsEnabledChanged() {
        return false;
    }
    public com.destroystokyo.paper.SkinParts getSkinParts() {
        return null;
    }
    public boolean hasSkinPartsChanged() {
        return false;
    }
    public org.bukkit.inventory.MainHand getMainHand() {
        return null;
    }
    public boolean hasMainHandChanged() {
        return false;
    }
    public boolean hasTextFilteringEnabled() {
        return false;
    }
    public boolean hasTextFilteringChanged() {
        return false;
    }
    public boolean allowsServerListings() {
        return false;
    }
    public boolean hasAllowServerListingsChanged() {
        return false;
    }
    public com.destroystokyo.paper.ClientOption$ParticleVisibility getParticleVisibility() {
        return null;
    }
    public boolean hasParticleVisibilityChanged() {
        return false;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
