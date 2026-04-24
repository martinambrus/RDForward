package io.papermc.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerNameEntityEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerNameEntityEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.LivingEntity arg1, net.kyori.adventure.text.Component arg2, boolean arg3) { super((org.bukkit.entity.Player) null); }
    public PlayerNameEntityEvent() { super((org.bukkit.entity.Player) null); }
    public net.kyori.adventure.text.Component getName() {
        return null;
    }
    public void setName(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerNameEntityEvent.setName(Lnet/kyori/adventure/text/Component;)V");
    }
    public org.bukkit.entity.LivingEntity getEntity() {
        return null;
    }
    public void setEntity(org.bukkit.entity.LivingEntity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerNameEntityEvent.setEntity(Lorg/bukkit/entity/LivingEntity;)V");
    }
    public boolean isPersistent() {
        return false;
    }
    public void setPersistent(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerNameEntityEvent.setPersistent(Z)V");
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.player.PlayerNameEntityEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
