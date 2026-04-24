package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TrialSpawner extends org.bukkit.block.TileState {
    long getCooldownEnd();
    void setCooldownEnd(long arg0);
    long getNextSpawnAttempt();
    void setNextSpawnAttempt(long arg0);
    int getCooldownLength();
    void setCooldownLength(int arg0);
    int getRequiredPlayerRange();
    void setRequiredPlayerRange(int arg0);
    java.util.Collection getTrackedPlayers();
    boolean isTrackingPlayer(org.bukkit.entity.Player arg0);
    void startTrackingPlayer(org.bukkit.entity.Player arg0);
    void stopTrackingPlayer(org.bukkit.entity.Player arg0);
    java.util.Collection getTrackedEntities();
    boolean isTrackingEntity(org.bukkit.entity.Entity arg0);
    void startTrackingEntity(org.bukkit.entity.Entity arg0);
    void stopTrackingEntity(org.bukkit.entity.Entity arg0);
    boolean isOminous();
    void setOminous(boolean arg0);
    org.bukkit.spawner.TrialSpawnerConfiguration getNormalConfiguration();
    org.bukkit.spawner.TrialSpawnerConfiguration getOminousConfiguration();
}
