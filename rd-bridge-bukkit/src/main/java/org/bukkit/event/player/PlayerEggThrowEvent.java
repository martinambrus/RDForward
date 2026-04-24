package org.bukkit.event.player;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerEggThrowEvent extends org.bukkit.event.player.PlayerEvent {
    public PlayerEggThrowEvent(org.bukkit.entity.Player arg0, org.bukkit.entity.Egg arg1, boolean arg2, byte arg3, org.bukkit.entity.EntityType arg4) { super((org.bukkit.entity.Player) null); }
    public PlayerEggThrowEvent() { super((org.bukkit.entity.Player) null); }
    public org.bukkit.entity.Egg getEgg() {
        return null;
    }
    public boolean isHatching() {
        return false;
    }
    public void setHatching(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerEggThrowEvent.setHatching(Z)V");
    }
    public org.bukkit.entity.EntityType getHatchingType() {
        return null;
    }
    public void setHatchingType(org.bukkit.entity.EntityType arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerEggThrowEvent.setHatchingType(Lorg/bukkit/entity/EntityType;)V");
    }
    public byte getNumHatches() {
        return (byte) 0;
    }
    public void setNumHatches(byte arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.event.player.PlayerEggThrowEvent.setNumHatches(B)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
