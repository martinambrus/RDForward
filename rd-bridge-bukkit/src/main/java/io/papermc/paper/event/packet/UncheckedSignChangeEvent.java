package io.papermc.paper.event.packet;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class UncheckedSignChangeEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public UncheckedSignChangeEvent(org.bukkit.entity.Player arg0, io.papermc.paper.math.BlockPosition arg1, org.bukkit.block.sign.Side arg2, java.util.List arg3) { super((org.bukkit.entity.Player) null); }
    public UncheckedSignChangeEvent() { super((org.bukkit.entity.Player) null); }
    public io.papermc.paper.math.BlockPosition getEditedBlockPosition() {
        return null;
    }
    public org.bukkit.block.sign.Side getSide() {
        return null;
    }
    public java.util.List lines() {
        return java.util.Collections.emptyList();
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.packet.UncheckedSignChangeEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
