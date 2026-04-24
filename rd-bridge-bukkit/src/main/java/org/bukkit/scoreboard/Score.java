package org.bukkit.scoreboard;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Score {
    org.bukkit.OfflinePlayer getPlayer();
    java.lang.String getEntry();
    org.bukkit.scoreboard.Objective getObjective();
    int getScore();
    void setScore(int arg0);
    boolean isScoreSet();
    org.bukkit.scoreboard.Scoreboard getScoreboard();
    void resetScore() throws java.lang.IllegalStateException;
    boolean isTriggerable();
    void setTriggerable(boolean arg0);
    net.kyori.adventure.text.Component customName();
    void customName(net.kyori.adventure.text.Component arg0);
    io.papermc.paper.scoreboard.numbers.NumberFormat numberFormat();
    void numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat arg0);
}
