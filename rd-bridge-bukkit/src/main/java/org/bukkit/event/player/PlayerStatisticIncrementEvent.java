package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerStatisticIncrementEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerStatisticIncrementEvent(org.bukkit.entity.Player arg0, org.bukkit.Statistic arg1, int arg2, int arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerStatisticIncrementEvent(org.bukkit.entity.Player arg0, org.bukkit.Statistic arg1, int arg2, int arg3, org.bukkit.entity.EntityType arg4) { super((org.bukkit.entity.Player) null); }
    public PlayerStatisticIncrementEvent(org.bukkit.entity.Player arg0, org.bukkit.Statistic arg1, int arg2, int arg3, org.bukkit.Material arg4) { super((org.bukkit.entity.Player) null); }
    public PlayerStatisticIncrementEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.Statistic getStatistic() {
        return null;
    }
    public int getPreviousValue() {
        return 0;
    }
    public int getNewValue() {
        return 0;
    }
    public org.bukkit.entity.EntityType getEntityType() {
        return null;
    }
    public org.bukkit.Material getMaterial() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerStatisticIncrementEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
