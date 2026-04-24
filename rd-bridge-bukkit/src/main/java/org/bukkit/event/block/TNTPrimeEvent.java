package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class TNTPrimeEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public TNTPrimeEvent(org.bukkit.block.Block arg0, org.bukkit.event.block.TNTPrimeEvent$PrimeCause arg1, org.bukkit.entity.Entity arg2, org.bukkit.block.Block arg3) { super((org.bukkit.block.Block) null); }
    public TNTPrimeEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.event.block.TNTPrimeEvent$PrimeCause getCause() {
        return null;
    }
    public org.bukkit.entity.Entity getPrimingEntity() {
        return null;
    }
    public org.bukkit.block.Block getPrimingBlock() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.TNTPrimeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
