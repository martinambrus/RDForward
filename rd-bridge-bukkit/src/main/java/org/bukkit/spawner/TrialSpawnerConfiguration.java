package org.bukkit.spawner;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TrialSpawnerConfiguration extends org.bukkit.spawner.BaseSpawner {
    float getBaseSpawnsBeforeCooldown();
    void setBaseSpawnsBeforeCooldown(float arg0);
    float getBaseSimultaneousEntities();
    void setBaseSimultaneousEntities(float arg0);
    float getAdditionalSpawnsBeforeCooldown();
    void setAdditionalSpawnsBeforeCooldown(float arg0);
    float getAdditionalSimultaneousEntities();
    void setAdditionalSimultaneousEntities(float arg0);
    java.util.Map getPossibleRewards();
    void addPossibleReward(org.bukkit.loot.LootTable arg0, int arg1);
    void removePossibleReward(org.bukkit.loot.LootTable arg0);
    void setPossibleRewards(java.util.Map arg0);
}
