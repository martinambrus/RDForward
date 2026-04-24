package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class PlayerCustomClickEvent extends org.bukkit.event.Event {
    protected PlayerCustomClickEvent(net.kyori.adventure.key.Key arg0, io.papermc.paper.connection.PlayerCommonConnection arg1) {}
    protected PlayerCustomClickEvent() {}
    public final net.kyori.adventure.key.Key getIdentifier() {
        return null;
    }
    public abstract net.kyori.adventure.nbt.api.BinaryTagHolder getTag();
    public abstract io.papermc.paper.dialog.DialogResponseView getDialogResponseView();
    public final io.papermc.paper.connection.PlayerCommonConnection getCommonConnection() {
        return null;
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
