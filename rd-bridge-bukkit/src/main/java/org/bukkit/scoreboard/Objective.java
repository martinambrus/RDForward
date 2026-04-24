package org.bukkit.scoreboard;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Objective {
    java.lang.String getName();
    net.kyori.adventure.text.Component displayName();
    void displayName(net.kyori.adventure.text.Component arg0);
    java.lang.String getDisplayName();
    void setDisplayName(java.lang.String arg0);
    java.lang.String getCriteria();
    org.bukkit.scoreboard.Criteria getTrackedCriteria();
    boolean isModifiable();
    org.bukkit.scoreboard.Scoreboard getScoreboard();
    void unregister();
    void setDisplaySlot(org.bukkit.scoreboard.DisplaySlot arg0);
    org.bukkit.scoreboard.DisplaySlot getDisplaySlot();
    void setRenderType(org.bukkit.scoreboard.RenderType arg0);
    org.bukkit.scoreboard.RenderType getRenderType();
    org.bukkit.scoreboard.Score getScore(org.bukkit.OfflinePlayer arg0);
    org.bukkit.scoreboard.Score getScore(java.lang.String arg0);
    org.bukkit.scoreboard.Score getScoreFor(org.bukkit.entity.Entity arg0) throws java.lang.IllegalArgumentException, java.lang.IllegalStateException;
    boolean willAutoUpdateDisplay();
    void setAutoUpdateDisplay(boolean arg0);
    io.papermc.paper.scoreboard.numbers.NumberFormat numberFormat();
    void numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat arg0);
}
