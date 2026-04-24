package com.destroystokyo.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ThrownEggHatchEvent extends org.bukkit.event.Event {
    public ThrownEggHatchEvent(org.bukkit.entity.Egg arg0, boolean arg1, byte arg2, org.bukkit.entity.EntityType arg3) {}
    public ThrownEggHatchEvent() {}
    public org.bukkit.entity.Egg getEgg() {
        return null;
    }
    public boolean isHatching() {
        return false;
    }
    public void setHatching(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.ThrownEggHatchEvent.setHatching(Z)V");
    }
    public org.bukkit.entity.EntityType getHatchingType() {
        return null;
    }
    public void setHatchingType(org.bukkit.entity.EntityType arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.ThrownEggHatchEvent.setHatchingType(Lorg/bukkit/entity/EntityType;)V");
    }
    public byte getNumHatches() {
        return (byte) 0;
    }
    public void setNumHatches(byte arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.event.entity.ThrownEggHatchEvent.setNumHatches(B)V");
    }
    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }
    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
