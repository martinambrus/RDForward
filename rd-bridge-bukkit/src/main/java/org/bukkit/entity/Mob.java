package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Mob extends org.bukkit.entity.LivingEntity, org.bukkit.loot.Lootable, io.papermc.paper.entity.Leashable {
    boolean shouldDespawnInPeaceful();
    void setDespawnInPeacefulOverride(net.kyori.adventure.util.TriState arg0);
    net.kyori.adventure.util.TriState getDespawnInPeacefulOverride();
    org.bukkit.inventory.EntityEquipment getEquipment();
    com.destroystokyo.paper.entity.Pathfinder getPathfinder();
    boolean isInDaylight();
    void lookAt(org.bukkit.Location arg0);
    void lookAt(org.bukkit.Location arg0, float arg1, float arg2);
    void lookAt(org.bukkit.entity.Entity arg0);
    void lookAt(org.bukkit.entity.Entity arg0, float arg1, float arg2);
    void lookAt(double arg0, double arg1, double arg2);
    void lookAt(double arg0, double arg1, double arg2, float arg3, float arg4);
    int getHeadRotationSpeed();
    int getMaxHeadPitch();
    void setTarget(org.bukkit.entity.LivingEntity arg0);
    org.bukkit.entity.LivingEntity getTarget();
    void setAware(boolean arg0);
    boolean isAware();
    org.bukkit.Sound getAmbientSound();
    default void setLootTable(org.bukkit.loot.LootTable arg0, long arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Mob.setLootTable(Lorg/bukkit/loot/LootTable;J)V");
    }
    boolean isAggressive();
    void setAggressive(boolean arg0);
    boolean isLeftHanded();
    void setLeftHanded(boolean arg0);
    int getPossibleExperienceReward();
}
