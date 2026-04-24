package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerBedFailEnterEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerBedFailEnterEvent(org.bukkit.entity.Player arg0, io.papermc.paper.event.player.PlayerBedFailEnterEvent$FailReason arg1, org.bukkit.block.Block arg2, boolean arg3, net.kyori.adventure.text.Component arg4, io.papermc.paper.block.bed.BedEnterAction arg5) { super((org.bukkit.entity.Player) null); }
    public PlayerBedFailEnterEvent() { super((org.bukkit.entity.Player) null); }
    public io.papermc.paper.event.player.PlayerBedFailEnterEvent$FailReason getFailReason() {
        return null;
    }
    public io.papermc.paper.block.bed.BedEnterAction enterAction() {
        return null;
    }
    public org.bukkit.block.Block getBed() {
        return null;
    }
    public boolean getWillExplode() {
        return false;
    }
    public void setWillExplode(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerBedFailEnterEvent.setWillExplode(Z)V");
    }
    public net.kyori.adventure.text.Component getMessage() {
        return null;
    }
    public void setMessage(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerBedFailEnterEvent.setMessage(Lnet/kyori/adventure/text/Component;)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerBedFailEnterEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
