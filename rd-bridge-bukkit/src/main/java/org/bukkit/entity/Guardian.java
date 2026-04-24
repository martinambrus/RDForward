package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Guardian extends org.bukkit.entity.Monster {
    boolean setLaser(boolean arg0);
    boolean hasLaser();
    int getLaserDuration();
    void setLaserTicks(int arg0);
    int getLaserTicks();
    boolean isElder();
    void setElder(boolean arg0);
    boolean isMoving();
}
