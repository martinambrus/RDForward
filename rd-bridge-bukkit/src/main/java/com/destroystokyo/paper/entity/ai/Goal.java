package com.destroystokyo.paper.entity.ai;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Goal {
    boolean shouldActivate();
    default boolean shouldStayActive() {
        return false;
    }
    default void start() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.entity.ai.Goal.start()V");
    }
    default void stop() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.entity.ai.Goal.stop()V");
    }
    default void tick() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.entity.ai.Goal.tick()V");
    }
    com.destroystokyo.paper.entity.ai.GoalKey getKey();
    java.util.EnumSet getTypes();
}
