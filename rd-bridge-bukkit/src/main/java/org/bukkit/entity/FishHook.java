package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface FishHook extends org.bukkit.entity.Projectile {
    int getMinWaitTime();
    void setMinWaitTime(int arg0);
    int getMaxWaitTime();
    void setMaxWaitTime(int arg0);
    void setWaitTime(int arg0, int arg1);
    int getMinLureTime();
    void setMinLureTime(int arg0);
    int getMaxLureTime();
    void setMaxLureTime(int arg0);
    void setLureTime(int arg0, int arg1);
    float getMinLureAngle();
    void setMinLureAngle(float arg0);
    float getMaxLureAngle();
    void setMaxLureAngle(float arg0);
    void setLureAngle(float arg0, float arg1);
    boolean getApplyLure();
    void setApplyLure(boolean arg0);
    double getBiteChance();
    void setBiteChance(double arg0) throws java.lang.IllegalArgumentException;
    boolean isInOpenWater();
    org.bukkit.entity.Entity getHookedEntity();
    void setHookedEntity(org.bukkit.entity.Entity arg0);
    boolean pullHookedEntity();
    boolean isSkyInfluenced();
    void setSkyInfluenced(boolean arg0);
    boolean isRainInfluenced();
    void setRainInfluenced(boolean arg0);
    org.bukkit.entity.FishHook$HookState getState();
    int getWaitTime();
    void setWaitTime(int arg0);
    int getTimeUntilBite();
    void setTimeUntilBite(int arg0) throws java.lang.IllegalArgumentException;
    void resetFishingState();
    int retrieve(org.bukkit.inventory.EquipmentSlot arg0);
}
