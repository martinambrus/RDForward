package org.bukkit.event.world;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class LootGenerateEvent extends org.bukkit.event.world.WorldEvent implements org.bukkit.event.Cancellable {
    public LootGenerateEvent(org.bukkit.World arg0, org.bukkit.entity.Entity arg1, org.bukkit.inventory.InventoryHolder arg2, org.bukkit.loot.LootTable arg3, org.bukkit.loot.LootContext arg4, java.util.List arg5, boolean arg6) { super((org.bukkit.World) null); }
    public LootGenerateEvent() { super((org.bukkit.World) null); }
    public org.bukkit.entity.Entity getEntity() {
        return null;
    }
    public org.bukkit.inventory.InventoryHolder getInventoryHolder() {
        return null;
    }
    public org.bukkit.loot.LootTable getLootTable() {
        return null;
    }
    public org.bukkit.loot.LootContext getLootContext() {
        return null;
    }
    public void setLoot(java.util.Collection arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.LootGenerateEvent.setLoot(Ljava/util/Collection;)V");
    }
    public java.util.List getLoot() {
        return java.util.Collections.emptyList();
    }
    public boolean isPlugin() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.LootGenerateEvent.setCancelled(Z)V");
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
