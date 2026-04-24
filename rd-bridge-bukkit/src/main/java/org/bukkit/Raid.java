package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Raid extends org.bukkit.persistence.PersistentDataHolder {
    boolean isStarted();
    long getActiveTicks();
    int getBadOmenLevel();
    void setBadOmenLevel(int arg0);
    org.bukkit.Location getLocation();
    org.bukkit.Raid$RaidStatus getStatus();
    int getSpawnedGroups();
    int getTotalGroups();
    int getTotalWaves();
    void setTotalWaves(int arg0);
    float getTotalHealth();
    java.util.Set getHeroes();
    java.util.List getRaiders();
    int getId();
    org.bukkit.boss.BossBar getBossBar();
}
