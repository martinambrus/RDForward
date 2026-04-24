package io.papermc.paper.event.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class WaterBottleSplashEvent extends org.bukkit.event.entity.PotionSplashEvent {
    public WaterBottleSplashEvent(org.bukkit.entity.ThrownPotion arg0, org.bukkit.entity.Entity arg1, org.bukkit.block.Block arg2, org.bukkit.block.BlockFace arg3, java.util.Map arg4, java.util.Set arg5, java.util.Set arg6) { super((org.bukkit.entity.ThrownPotion) null, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null, java.util.Collections.emptyMap()); }
    public WaterBottleSplashEvent() { super((org.bukkit.entity.ThrownPotion) null, (org.bukkit.entity.Entity) null, (org.bukkit.block.Block) null, (org.bukkit.block.BlockFace) null, java.util.Collections.emptyMap()); }
    public java.util.Collection getToDamage() {
        return java.util.Collections.emptyList();
    }
    public void doNotDamageAsWaterSensitive(org.bukkit.entity.LivingEntity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.WaterBottleSplashEvent.doNotDamageAsWaterSensitive(Lorg/bukkit/entity/LivingEntity;)V");
    }
    public void damageAsWaterSensitive(org.bukkit.entity.LivingEntity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.WaterBottleSplashEvent.damageAsWaterSensitive(Lorg/bukkit/entity/LivingEntity;)V");
    }
    public java.util.Collection getToRehydrate() {
        return java.util.Collections.emptyList();
    }
    public java.util.Collection getToExtinguish() {
        return java.util.Collections.emptyList();
    }
    public java.util.Collection getAffectedEntities() {
        return java.util.Collections.emptyList();
    }
    public double getIntensity(org.bukkit.entity.LivingEntity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.WaterBottleSplashEvent.getIntensity(Lorg/bukkit/entity/LivingEntity;)D");
        return 0.0;
    }
    public void setIntensity(org.bukkit.entity.LivingEntity arg0, double arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.event.entity.WaterBottleSplashEvent.setIntensity(Lorg/bukkit/entity/LivingEntity;D)V");
    }
}
