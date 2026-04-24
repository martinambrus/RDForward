package org.bukkit.entity.minecart;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ExplosiveMinecart extends org.bukkit.entity.Minecart, org.bukkit.entity.Explosive {
    void setFuseTicks(int arg0);
    int getFuseTicks();
    float getExplosionSpeedFactor();
    void setExplosionSpeedFactor(float arg0);
    void ignite();
    boolean isIgnited();
    void explode();
    void explode(double arg0);
}
