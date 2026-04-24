package org.bukkit.event.world;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class StructureGrowEvent extends org.bukkit.event.world.WorldEvent implements org.bukkit.event.Cancellable {
    public StructureGrowEvent(org.bukkit.Location arg0, org.bukkit.TreeType arg1, boolean arg2, org.bukkit.entity.Player arg3, java.util.List arg4) { super((org.bukkit.World) null); }
    public StructureGrowEvent() { super((org.bukkit.World) null); }
    public org.bukkit.Location getLocation() {
        return null;
    }
    public org.bukkit.TreeType getSpecies() {
        return null;
    }
    public boolean isFromBonemeal() {
        return false;
    }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public java.util.List getBlocks() {
        return java.util.Collections.emptyList();
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.StructureGrowEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
