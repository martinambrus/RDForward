package org.bukkit.boss;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BossBar {
    java.lang.String getTitle();
    void setTitle(java.lang.String arg0);
    org.bukkit.boss.BarColor getColor();
    void setColor(org.bukkit.boss.BarColor arg0);
    org.bukkit.boss.BarStyle getStyle();
    void setStyle(org.bukkit.boss.BarStyle arg0);
    void removeFlag(org.bukkit.boss.BarFlag arg0);
    void addFlag(org.bukkit.boss.BarFlag arg0);
    boolean hasFlag(org.bukkit.boss.BarFlag arg0);
    void setProgress(double arg0);
    double getProgress();
    void addPlayer(org.bukkit.entity.Player arg0);
    void removePlayer(org.bukkit.entity.Player arg0);
    void removeAll();
    java.util.List getPlayers();
    void setVisible(boolean arg0);
    boolean isVisible();
    void show();
    void hide();
}
