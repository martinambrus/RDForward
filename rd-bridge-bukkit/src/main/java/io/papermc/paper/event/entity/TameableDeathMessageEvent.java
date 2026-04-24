package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class TameableDeathMessageEvent extends org.bukkit.event.entity.EntityEvent implements org.bukkit.event.Cancellable {
    public TameableDeathMessageEvent(org.bukkit.entity.Tameable arg0, net.kyori.adventure.text.Component arg1) { super((org.bukkit.entity.Entity) null); }
    public TameableDeathMessageEvent() { super((org.bukkit.entity.Entity) null); }
    public void deathMessage(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.TameableDeathMessageEvent.deathMessage(Lnet/kyori/adventure/text/Component;)V");
    }
    public net.kyori.adventure.text.Component deathMessage() {
        return null;
    }
    public org.bukkit.entity.Tameable getEntity() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.TameableDeathMessageEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
