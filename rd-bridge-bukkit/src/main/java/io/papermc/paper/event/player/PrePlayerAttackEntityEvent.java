package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PrePlayerAttackEntityEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PrePlayerAttackEntityEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.Entity arg1, boolean arg2) { super((org.bukkit.entity.Player) null); }
    public PrePlayerAttackEntityEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.entity.Entity getAttacked() {
        return null;
    }
    public boolean willAttack() {
        return false;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PrePlayerAttackEntityEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
