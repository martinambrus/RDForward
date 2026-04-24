package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Raider extends org.bukkit.entity.Monster {
    void setRaid(org.bukkit.Raid arg0);
    org.bukkit.Raid getRaid();
    int getWave();
    void setWave(int arg0);
    org.bukkit.block.Block getPatrolTarget();
    void setPatrolTarget(org.bukkit.block.Block arg0);
    boolean isPatrolLeader();
    void setPatrolLeader(boolean arg0);
    boolean isCanJoinRaid();
    void setCanJoinRaid(boolean arg0);
    int getTicksOutsideRaid();
    void setTicksOutsideRaid(int arg0);
    boolean isCelebrating();
    void setCelebrating(boolean arg0);
    org.bukkit.Sound getCelebrationSound();
}
