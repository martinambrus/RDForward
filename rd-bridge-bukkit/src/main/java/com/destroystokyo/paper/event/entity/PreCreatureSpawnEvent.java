package com.destroystokyo.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PreCreatureSpawnEvent extends org.bukkit.event.Event implements org.bukkit.event.Cancellable {
    public PreCreatureSpawnEvent(org.bukkit.Location arg0, org.bukkit.entity.EntityType arg1, org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason arg2) {}
    public PreCreatureSpawnEvent() {}
    public org.bukkit.Location getSpawnLocation() {
        return null;
    }
    public org.bukkit.entity.EntityType getType() {
        return null;
    }
    public org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason getReason() {
        return null;
    }
    public boolean shouldAbortSpawn() {
        return false;
    }
    public void setShouldAbortSpawn(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent.setShouldAbortSpawn(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
