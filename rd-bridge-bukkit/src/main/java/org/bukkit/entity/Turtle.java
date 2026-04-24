package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Turtle extends org.bukkit.entity.Animals {
    boolean hasEgg();
    boolean isLayingEgg();
    org.bukkit.Location getHome();
    void setHome(org.bukkit.Location arg0);
    boolean isGoingHome();
    default boolean isDigging() {
        return false;
    }
    void setHasEgg(boolean arg0);
}
