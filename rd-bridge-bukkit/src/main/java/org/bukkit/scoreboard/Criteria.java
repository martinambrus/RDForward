package org.bukkit.scoreboard;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Criteria {
    public static final org.bukkit.scoreboard.Criteria DUMMY = null;
    public static final org.bukkit.scoreboard.Criteria TRIGGER = null;
    public static final org.bukkit.scoreboard.Criteria DEATH_COUNT = null;
    public static final org.bukkit.scoreboard.Criteria PLAYER_KILL_COUNT = null;
    public static final org.bukkit.scoreboard.Criteria TOTAL_KILL_COUNT = null;
    public static final org.bukkit.scoreboard.Criteria HEALTH = null;
    public static final org.bukkit.scoreboard.Criteria FOOD = null;
    public static final org.bukkit.scoreboard.Criteria AIR = null;
    public static final org.bukkit.scoreboard.Criteria ARMOR = null;
    public static final org.bukkit.scoreboard.Criteria XP = null;
    public static final org.bukkit.scoreboard.Criteria LEVEL = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_BLACK = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_DARK_BLUE = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_DARK_GREEN = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_DARK_AQUA = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_DARK_RED = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_DARK_PURPLE = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_GOLD = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_GRAY = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_DARK_GRAY = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_BLUE = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_GREEN = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_AQUA = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_RED = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_LIGHT_PURPLE = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_YELLOW = null;
    public static final org.bukkit.scoreboard.Criteria TEAM_KILL_WHITE = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_BLACK = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_DARK_BLUE = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_DARK_GREEN = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_DARK_AQUA = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_DARK_RED = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_DARK_PURPLE = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_GOLD = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_GRAY = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_DARK_GRAY = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_BLUE = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_GREEN = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_AQUA = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_RED = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_LIGHT_PURPLE = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_YELLOW = null;
    public static final org.bukkit.scoreboard.Criteria KILLED_BY_TEAM_WHITE = null;
    java.lang.String getName();
    boolean isReadOnly();
    org.bukkit.scoreboard.RenderType getDefaultRenderType();
    static org.bukkit.scoreboard.Criteria statistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1) {
        return null;
    }
    static org.bukkit.scoreboard.Criteria statistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1) {
        return null;
    }
    static org.bukkit.scoreboard.Criteria statistic(org.bukkit.Statistic arg0) {
        return null;
    }
    static org.bukkit.scoreboard.Criteria create(java.lang.String arg0) {
        return null;
    }
}
