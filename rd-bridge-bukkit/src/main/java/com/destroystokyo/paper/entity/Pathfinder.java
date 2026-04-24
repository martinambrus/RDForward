package com.destroystokyo.paper.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Pathfinder {
    org.bukkit.entity.Mob getEntity();
    void stopPathfinding();
    boolean hasPath();
    com.destroystokyo.paper.entity.Pathfinder$PathResult getCurrentPath();
    com.destroystokyo.paper.entity.Pathfinder$PathResult findPath(org.bukkit.Location arg0);
    default com.destroystokyo.paper.entity.Pathfinder$PathResult findPath(org.bukkit.entity.LivingEntity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.entity.Pathfinder.findPath(Lorg/bukkit/entity/LivingEntity;)Lcom/destroystokyo/paper/entity/Pathfinder$PathResult;");
        return null;
    }
    com.destroystokyo.paper.entity.Pathfinder$PathResult findPath(org.bukkit.entity.Entity arg0);
    default boolean moveTo(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.entity.Pathfinder.moveTo(Lorg/bukkit/Location;)Z");
        return false;
    }
    default boolean moveTo(org.bukkit.Location arg0, double arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.entity.Pathfinder.moveTo(Lorg/bukkit/Location;D)Z");
        return false;
    }
    default boolean moveTo(org.bukkit.entity.LivingEntity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.entity.Pathfinder.moveTo(Lorg/bukkit/entity/LivingEntity;)Z");
        return false;
    }
    default boolean moveTo(org.bukkit.entity.LivingEntity arg0, double arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.entity.Pathfinder.moveTo(Lorg/bukkit/entity/LivingEntity;D)Z");
        return false;
    }
    default boolean moveTo(org.bukkit.entity.Entity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.entity.Pathfinder.moveTo(Lorg/bukkit/entity/Entity;)Z");
        return false;
    }
    default boolean moveTo(org.bukkit.entity.Entity arg0, double arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.entity.Pathfinder.moveTo(Lorg/bukkit/entity/Entity;D)Z");
        return false;
    }
    default boolean moveTo(com.destroystokyo.paper.entity.Pathfinder$PathResult arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.entity.Pathfinder.moveTo(Lcom/destroystokyo/paper/entity/Pathfinder$PathResult;)Z");
        return false;
    }
    boolean moveTo(com.destroystokyo.paper.entity.Pathfinder$PathResult arg0, double arg1);
    boolean canOpenDoors();
    void setCanOpenDoors(boolean arg0);
    boolean canPassDoors();
    void setCanPassDoors(boolean arg0);
    boolean canFloat();
    void setCanFloat(boolean arg0);
}
