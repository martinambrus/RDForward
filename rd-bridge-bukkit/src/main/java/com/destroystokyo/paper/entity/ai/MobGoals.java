package com.destroystokyo.paper.entity.ai;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MobGoals {
    void addGoal(org.bukkit.entity.Mob arg0, int arg1, com.destroystokyo.paper.entity.ai.Goal arg2);
    void removeGoal(org.bukkit.entity.Mob arg0, com.destroystokyo.paper.entity.ai.Goal arg1);
    void removeAllGoals(org.bukkit.entity.Mob arg0);
    void removeAllGoals(org.bukkit.entity.Mob arg0, com.destroystokyo.paper.entity.ai.GoalType arg1);
    void removeGoal(org.bukkit.entity.Mob arg0, com.destroystokyo.paper.entity.ai.GoalKey arg1);
    boolean hasGoal(org.bukkit.entity.Mob arg0, com.destroystokyo.paper.entity.ai.GoalKey arg1);
    com.destroystokyo.paper.entity.ai.Goal getGoal(org.bukkit.entity.Mob arg0, com.destroystokyo.paper.entity.ai.GoalKey arg1);
    java.util.Collection getGoals(org.bukkit.entity.Mob arg0, com.destroystokyo.paper.entity.ai.GoalKey arg1);
    java.util.Collection getAllGoals(org.bukkit.entity.Mob arg0);
    java.util.Collection getAllGoals(org.bukkit.entity.Mob arg0, com.destroystokyo.paper.entity.ai.GoalType arg1);
    java.util.Collection getAllGoalsWithout(org.bukkit.entity.Mob arg0, com.destroystokyo.paper.entity.ai.GoalType arg1);
    java.util.Collection getRunningGoals(org.bukkit.entity.Mob arg0);
    java.util.Collection getRunningGoals(org.bukkit.entity.Mob arg0, com.destroystokyo.paper.entity.ai.GoalType arg1);
    java.util.Collection getRunningGoalsWithout(org.bukkit.entity.Mob arg0, com.destroystokyo.paper.entity.ai.GoalType arg1);
}
