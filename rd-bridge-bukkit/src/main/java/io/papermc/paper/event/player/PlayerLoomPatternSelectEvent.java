package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerLoomPatternSelectEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerLoomPatternSelectEvent(org.bukkit.entity.Player arg0, org.bukkit.inventory.LoomInventory arg1, org.bukkit.block.banner.PatternType arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerLoomPatternSelectEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.inventory.LoomInventory getLoomInventory() {
        return null;
    }
    public org.bukkit.block.banner.PatternType getPatternType() {
        return null;
    }
    public void setPatternType(org.bukkit.block.banner.PatternType arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerLoomPatternSelectEvent.setPatternType(Lorg/bukkit/block/banner/PatternType;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerLoomPatternSelectEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
