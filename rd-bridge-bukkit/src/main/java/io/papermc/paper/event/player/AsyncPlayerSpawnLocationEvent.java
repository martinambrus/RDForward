package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class AsyncPlayerSpawnLocationEvent extends org.bukkit.event.Event {
    public AsyncPlayerSpawnLocationEvent(io.papermc.paper.connection.PlayerConfigurationConnection arg0, org.bukkit.Location arg1, boolean arg2) {}
    public AsyncPlayerSpawnLocationEvent() {}
    public io.papermc.paper.connection.PlayerConfigurationConnection getConnection() {
        return null;
    }
    public org.bukkit.Location getSpawnLocation() {
        return null;
    }
    public void setSpawnLocation(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.AsyncPlayerSpawnLocationEvent.setSpawnLocation(Lorg/bukkit/Location;)V");
    }
    public boolean isNewPlayer() {
        return false;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
