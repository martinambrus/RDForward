package org.bukkit.boss;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface DragonBattle {
    org.bukkit.entity.EnderDragon getEnderDragon();
    org.bukkit.boss.BossBar getBossBar();
    org.bukkit.Location getEndPortalLocation();
    boolean generateEndPortal(boolean arg0);
    boolean hasBeenPreviouslyKilled();
    void setPreviouslyKilled(boolean arg0);
    void initiateRespawn();
    boolean initiateRespawn(java.util.Collection arg0);
    org.bukkit.boss.DragonBattle$RespawnPhase getRespawnPhase();
    boolean setRespawnPhase(org.bukkit.boss.DragonBattle$RespawnPhase arg0);
    void resetCrystals();
    int getGatewayCount();
    boolean spawnNewGateway();
    void spawnNewGateway(io.papermc.paper.math.Position arg0);
    java.util.List getRespawnCrystals();
    java.util.List getHealingCrystals();
}
