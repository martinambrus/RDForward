package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Creeper extends org.bukkit.entity.Monster {
    boolean isPowered();
    void setPowered(boolean arg0);
    void setMaxFuseTicks(int arg0);
    int getMaxFuseTicks();
    void setFuseTicks(int arg0);
    int getFuseTicks();
    void setExplosionRadius(int arg0);
    int getExplosionRadius();
    void explode();
    void ignite(org.bukkit.entity.Entity arg0);
    void ignite();
    org.bukkit.entity.Entity getIgniter();
    void setIgnited(boolean arg0);
    boolean isIgnited();
}
