package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class EntityCompostItemEvent extends io.papermc.paper.event.block.CompostItemEvent implements org.bukkit.event.Cancellable {
    public EntityCompostItemEvent(org.bukkit.entity.Entity arg0, org.bukkit.block.Block arg1, org.bukkit.inventory.ItemStack arg2, boolean arg3) { super((org.bukkit.block.Block) null, (org.bukkit.inventory.ItemStack) null, false); }
    public EntityCompostItemEvent() { super((org.bukkit.block.Block) null, (org.bukkit.inventory.ItemStack) null, false); }
    public org.bukkit.entity.Entity getEntity() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.EntityCompostItemEvent.setCancelled(Z)V");
    }
}
