package com.destroystokyo.paper.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Pathfinder$PathResult {
    java.util.List getPoints();
    int getNextPointIndex();
    org.bukkit.Location getNextPoint();
    org.bukkit.Location getFinalPoint();
    boolean canReachFinalPoint();
}
