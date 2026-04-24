package com.destroystokyo.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerAttackEntityCooldownResetEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerAttackEntityCooldownResetEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.Entity arg1, float arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerAttackEntityCooldownResetEvent() { super((org.bukkit.entity.Player) null); }
    public float getCooledAttackStrength() {
        return 0.0f;
    }
    public org.bukkit.entity.Entity getAttackedEntity() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerAttackEntityCooldownResetEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
