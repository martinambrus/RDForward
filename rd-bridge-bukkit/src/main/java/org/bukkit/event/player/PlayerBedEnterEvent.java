package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerBedEnterEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerBedEnterEvent(org.bukkit.entity.Player arg0, org.bukkit.block.Block arg1, org.bukkit.event.player.PlayerBedEnterEvent$BedEnterResult arg2, io.papermc.paper.block.bed.BedEnterAction arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerBedEnterEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.block.Block getBed() {
        return null;
    }
    public org.bukkit.event.player.PlayerBedEnterEvent$BedEnterResult getBedEnterResult() {
        return null;
    }
    public io.papermc.paper.block.bed.BedEnterAction enterAction() {
        return null;
    }
    public org.bukkit.event.Event$Result useBed() {
        return null;
    }
    public void setUseBed(org.bukkit.event.Event$Result arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerBedEnterEvent.setUseBed(Lorg/bukkit/event/Event$Result;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerBedEnterEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
