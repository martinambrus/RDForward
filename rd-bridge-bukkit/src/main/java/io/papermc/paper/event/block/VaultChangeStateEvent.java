package io.papermc.paper.event.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class VaultChangeStateEvent extends org.bukkit.event.block.BlockEvent implements org.bukkit.event.Cancellable {
    public VaultChangeStateEvent(org.bukkit.block.Block arg0, org.bukkit.entity.Player arg1, org.bukkit.block.data.type.Vault$State arg2, org.bukkit.block.data.type.Vault$State arg3) { super((org.bukkit.block.Block) null); }
    public VaultChangeStateEvent() { super((org.bukkit.block.Block) null); }
    public org.bukkit.entity.Player getPlayer() {
        return null;
    }
    public org.bukkit.block.data.type.Vault$State getCurrentState() {
        return null;
    }
    public org.bukkit.block.data.type.Vault$State getNewState() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.block.VaultChangeStateEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
