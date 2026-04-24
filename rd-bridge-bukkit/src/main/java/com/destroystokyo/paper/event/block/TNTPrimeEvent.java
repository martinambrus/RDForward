package com.destroystokyo.paper.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class TNTPrimeEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public TNTPrimeEvent(org.bukkit.block.Block arg0, com.destroystokyo.paper.event.block.TNTPrimeEvent$PrimeReason arg1, org.bukkit.entity.Entity arg2) { super((org.bukkit.block.Block) null); }
    public TNTPrimeEvent() { super((org.bukkit.block.Block) null); }
    public com.destroystokyo.paper.event.block.TNTPrimeEvent$PrimeReason getReason() {
        return null;
    }
    public org.bukkit.entity.Entity getPrimerEntity() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.block.TNTPrimeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
