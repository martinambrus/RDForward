package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerChangeBeaconEffectEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerChangeBeaconEffectEvent(org.bukkit.entity.Player arg0, org.bukkit.potion.PotionEffectType arg1, org.bukkit.potion.PotionEffectType arg2, org.bukkit.block.Block arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerChangeBeaconEffectEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.potion.PotionEffectType getPrimary() {
        return null;
    }
    public void setPrimary(org.bukkit.potion.PotionEffectType arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent.setPrimary(Lorg/bukkit/potion/PotionEffectType;)V");
    }
    public org.bukkit.potion.PotionEffectType getSecondary() {
        return null;
    }
    public void setSecondary(org.bukkit.potion.PotionEffectType arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent.setSecondary(Lorg/bukkit/potion/PotionEffectType;)V");
    }
    public org.bukkit.block.Block getBeacon() {
        return null;
    }
    public boolean willConsumeItem() {
        return false;
    }
    public void setConsumeItem(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent.setConsumeItem(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerChangeBeaconEffectEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
