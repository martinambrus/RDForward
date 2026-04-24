package org.bukkit.scoreboard;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Team extends net.kyori.adventure.audience.ForwardingAudience {
    java.lang.String getName();
    net.kyori.adventure.text.Component displayName();
    void displayName(net.kyori.adventure.text.Component arg0);
    net.kyori.adventure.text.Component prefix();
    void prefix(net.kyori.adventure.text.Component arg0);
    net.kyori.adventure.text.Component suffix();
    void suffix(net.kyori.adventure.text.Component arg0);
    boolean hasColor();
    net.kyori.adventure.text.format.TextColor color();
    void color(net.kyori.adventure.text.format.NamedTextColor arg0);
    java.lang.String getDisplayName();
    void setDisplayName(java.lang.String arg0);
    java.lang.String getPrefix();
    void setPrefix(java.lang.String arg0);
    java.lang.String getSuffix();
    void setSuffix(java.lang.String arg0);
    org.bukkit.ChatColor getColor();
    void setColor(org.bukkit.ChatColor arg0);
    boolean allowFriendlyFire();
    void setAllowFriendlyFire(boolean arg0);
    boolean canSeeFriendlyInvisibles();
    void setCanSeeFriendlyInvisibles(boolean arg0);
    org.bukkit.scoreboard.NameTagVisibility getNameTagVisibility();
    void setNameTagVisibility(org.bukkit.scoreboard.NameTagVisibility arg0);
    java.util.Set getPlayers();
    java.util.Set getEntries();
    int getSize();
    org.bukkit.scoreboard.Scoreboard getScoreboard();
    void addPlayer(org.bukkit.OfflinePlayer arg0);
    void addEntry(java.lang.String arg0);
    default void addEntities(org.bukkit.entity.Entity[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.scoreboard.Team.addEntities([Lorg/bukkit/entity/Entity;)V");
    }
    void addEntities(java.util.Collection arg0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException;
    default void addEntries(java.lang.String[] arg0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.scoreboard.Team.addEntries([Ljava/lang/String;)V");
    }
    void addEntries(java.util.Collection arg0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException;
    boolean removePlayer(org.bukkit.OfflinePlayer arg0);
    boolean removeEntry(java.lang.String arg0);
    default boolean removeEntities(org.bukkit.entity.Entity[] arg0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.scoreboard.Team.removeEntities([Lorg/bukkit/entity/Entity;)Z");
        return false;
    }
    boolean removeEntities(java.util.Collection arg0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException;
    default boolean removeEntries(java.lang.String[] arg0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.scoreboard.Team.removeEntries([Ljava/lang/String;)Z");
        return false;
    }
    boolean removeEntries(java.util.Collection arg0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException;
    void unregister();
    boolean hasPlayer(org.bukkit.OfflinePlayer arg0);
    boolean hasEntry(java.lang.String arg0);
    org.bukkit.scoreboard.Team$OptionStatus getOption(org.bukkit.scoreboard.Team$Option arg0);
    void setOption(org.bukkit.scoreboard.Team$Option arg0, org.bukkit.scoreboard.Team$OptionStatus arg1);
    void addEntity(org.bukkit.entity.Entity arg0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException;
    boolean removeEntity(org.bukkit.entity.Entity arg0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException;
    boolean hasEntity(org.bukkit.entity.Entity arg0) throws java.lang.IllegalStateException, java.lang.IllegalArgumentException;
}
