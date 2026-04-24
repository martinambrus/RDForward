package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockReceiveGameEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BlockReceiveGameEvent(org.bukkit.GameEvent arg0, org.bukkit.block.Block arg1, org.bukkit.entity.Entity arg2) { super((org.bukkit.block.Block) null); }
    public BlockReceiveGameEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.GameEvent getEvent() {
        return null;
    }
    public org.bukkit.entity.Entity getEntity() {
        return null;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockReceiveGameEvent.setCancelled(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
