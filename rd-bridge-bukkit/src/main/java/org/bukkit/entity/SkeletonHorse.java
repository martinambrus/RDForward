package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SkeletonHorse extends org.bukkit.entity.AbstractHorse {
    boolean isTrapped();
    void setTrapped(boolean arg0);
    int getTrapTime();
    void setTrapTime(int arg0);
    default boolean isTrap() {
        return false;
    }
    default void setTrap(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.SkeletonHorse.setTrap(Z)V");
    }
}
