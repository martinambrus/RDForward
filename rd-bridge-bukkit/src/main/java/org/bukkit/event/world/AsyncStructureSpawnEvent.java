package org.bukkit.event.world;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class AsyncStructureSpawnEvent extends org.bukkit.event.world.WorldEvent implements org.bukkit.event.Cancellable {
    public AsyncStructureSpawnEvent(org.bukkit.World arg0, org.bukkit.generator.structure.Structure arg1, org.bukkit.util.BoundingBox arg2, int arg3, int arg4) { super((org.bukkit.World) null); }
    public AsyncStructureSpawnEvent() { super((org.bukkit.World) null); }
    public org.bukkit.generator.structure.Structure getStructure() {
        return null;
    }
    public org.bukkit.util.BoundingBox getBoundingBox() {
        return null;
    }
    public int getChunkX() {
        return 0;
    }
    public int getChunkZ() {
        return 0;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.world.AsyncStructureSpawnEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
