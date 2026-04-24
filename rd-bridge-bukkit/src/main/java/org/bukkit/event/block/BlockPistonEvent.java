package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class BlockPistonEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    protected BlockPistonEvent(org.bukkit.block.Block arg0, org.bukkit.block.BlockFace arg1) { super((org.bukkit.block.Block) null); }
    protected BlockPistonEvent() { super((org.bukkit.block.Block) null); }
    public boolean isSticky() {
        return false;
    }
    public org.bukkit.block.BlockFace getDirection() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockPistonEvent.setCancelled(Z)V");
    }
}
