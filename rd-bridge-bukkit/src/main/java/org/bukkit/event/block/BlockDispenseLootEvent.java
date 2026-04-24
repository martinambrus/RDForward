package org.bukkit.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BlockDispenseLootEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public BlockDispenseLootEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Block arg1, java.util.List arg2, org.bukkit.loot.LootTable arg3) { super((org.bukkit.block.Block) null); }
    public BlockDispenseLootEvent() { super((org.bukkit.block.Block) null); }
    public java.util.List getDispensedLoot() {
        return java.util.Collections.emptyList();
    }
    public void setDispensedLoot(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockDispenseLootEvent.setDispensedLoot(Ljava/util/List;)V");
    }
    public org.bukkit.loot.LootTable getLootTable() {
        return null;
    }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.block.BlockDispenseLootEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
