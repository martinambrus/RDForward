package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ServerTickManager {
    boolean isRunningNormally();
    boolean isStepping();
    boolean isSprinting();
    boolean isFrozen();
    float getTickRate();
    void setTickRate(float arg0);
    void setFrozen(boolean arg0);
    boolean stepGameIfFrozen(int arg0);
    boolean stopStepping();
    boolean requestGameToSprint(int arg0);
    boolean stopSprinting();
    boolean isFrozen(org.bukkit.entity.Entity arg0);
    int getFrozenTicksToRun();
}
