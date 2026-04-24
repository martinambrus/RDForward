package com.destroystokyo.paper.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerAdvancementCriterionGrantEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {
    public PlayerAdvancementCriterionGrantEvent(org.bukkit.entity.Player arg0, org.bukkit.advancement.Advancement arg1, java.lang.String arg2) { super((org.bukkit.entity.Player) null); }
    public PlayerAdvancementCriterionGrantEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.advancement.Advancement getAdvancement() {
        return null;
    }
    public java.lang.String getCriterion() {
        return null;
    }
    public org.bukkit.advancement.AdvancementProgress getAdvancementProgress() {
        return null;
    }
    public boolean isCancelled() {
        return false;
    }
    public void setCancelled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent.setCancelled(Z)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
