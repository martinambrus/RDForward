package io.papermc.paper.event.world;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class StructuresLocateEvent extends org.bukkit.event.world.WorldEvent implements org.bukkit.event.Cancellable {
    public StructuresLocateEvent(org.bukkit.World arg0, org.bukkit.Location arg1, java.util.List arg2, int arg3, boolean arg4) { super((org.bukkit.World) null); }
    public StructuresLocateEvent() { super((org.bukkit.World) null); }
    public org.bukkit.Location getOrigin() {
        return null;
    }
    public io.papermc.paper.event.world.StructuresLocateEvent$Result getResult() {
        return null;
    }
    public void setResult(io.papermc.paper.event.world.StructuresLocateEvent$Result arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.world.StructuresLocateEvent.setResult(Lio/papermc/paper/event/world/StructuresLocateEvent$Result;)V");
    }
    public java.util.List getStructures() {
        return java.util.Collections.emptyList();
    }
    public void setStructures(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.world.StructuresLocateEvent.setStructures(Ljava/util/List;)V");
    }
    public int getRadius() {
        return 0;
    }
    public void setRadius(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.world.StructuresLocateEvent.setRadius(I)V");
    }
    public boolean shouldFindUnexplored() {
        return false;
    }
    public void setFindUnexplored(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.world.StructuresLocateEvent.setFindUnexplored(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.world.StructuresLocateEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
