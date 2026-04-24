package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class CauldronLevelChangeEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public CauldronLevelChangeEvent(org.bukkit.block.Block arg0, org.bukkit.entity.Entity arg1, org.bukkit.event.block.CauldronLevelChangeEvent$ChangeReason arg2, org.bukkit.block.BlockState arg3) { super((org.bukkit.block.Block) null); }
    public CauldronLevelChangeEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.entity.Entity getEntity() {
        return null;
    }
    public org.bukkit.event.block.CauldronLevelChangeEvent$ChangeReason getReason() {
        return null;
    }
    public org.bukkit.block.BlockState getNewState() {
        return null;
    }
    public int getOldLevel() {
        return 0;
    }
    public int getNewLevel() {
        return 0;
    }
    public void setNewLevel(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.CauldronLevelChangeEvent.setNewLevel(I)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.CauldronLevelChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
