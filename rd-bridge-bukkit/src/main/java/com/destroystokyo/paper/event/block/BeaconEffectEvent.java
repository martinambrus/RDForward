package com.destroystokyo.paper.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BeaconEffectEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BeaconEffectEvent(org.bukkit.block.Block arg0, org.bukkit.potion.PotionEffect arg1, org.bukkit.entity.Player arg2, boolean arg3) { super((org.bukkit.block.Block) null); }
    public BeaconEffectEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.potion.PotionEffect getEffect() {
        return null;
    }
    public void setEffect(org.bukkit.potion.PotionEffect arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.block.BeaconEffectEvent.setEffect(Lorg/bukkit/potion/PotionEffect;)V");
    }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public boolean isPrimary() {
        return false;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.block.BeaconEffectEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
