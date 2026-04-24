package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface LightningStrike extends org.bukkit.entity.Entity {
    boolean isEffect();
    int getFlashes();
    void setFlashes(int arg0);
    int getLifeTicks();
    void setLifeTicks(int arg0);
    org.bukkit.entity.Player getCausingPlayer();
    void setCausingPlayer(org.bukkit.entity.Player arg0);
    org.bukkit.entity.LightningStrike$Spigot spigot();
    int getFlashCount();
    void setFlashCount(int arg0);
    org.bukkit.entity.Entity getCausingEntity();
}
