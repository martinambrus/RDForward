package com.destroystokyo.paper.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockDestroyEvent extends org.bukkit.event.block.BlockExpEvent implements org.bukkit.event.Cancellable {
    public BlockDestroyEvent(org.bukkit.block.Block arg0, org.bukkit.block.data.BlockData arg1, org.bukkit.block.data.BlockData arg2, int arg3, boolean arg4) { super((org.bukkit.block.Block) null, 0); }
    public BlockDestroyEvent() { super((org.bukkit.block.Block) null, 0); }
    public org.bukkit.block.data.BlockData getEffectBlock() {
        return null;
    }
    public void setEffectBlock(org.bukkit.block.data.BlockData arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.block.BlockDestroyEvent.setEffectBlock(Lorg/bukkit/block/data/BlockData;)V");
    }
    public org.bukkit.block.data.BlockData getNewState() {
        return null;
    }
    public boolean willDrop() {
        return false;
    }
    public void setWillDrop(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.block.BlockDestroyEvent.setWillDrop(Z)V");
    }
    public boolean playEffect() {
        return false;
    }
    public void setPlayEffect(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.block.BlockDestroyEvent.setPlayEffect(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.block.BlockDestroyEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
