package com.destroystokyo.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerNaturallySpawnCreaturesEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerNaturallySpawnCreaturesEvent(org.bukkit.entity.Player arg0, byte arg1) { super((org.bukkit.entity.Player) null); }
    public PlayerNaturallySpawnCreaturesEvent() { super((org.bukkit.entity.Player) null); }
    public byte getSpawnRadius() {
        return (byte) 0;
    }
    public void setSpawnRadius(byte arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent.setSpawnRadius(B)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.PlayerNaturallySpawnCreaturesEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
