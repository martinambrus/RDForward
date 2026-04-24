package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface OfflinePlayer extends org.bukkit.permissions.ServerOperator, org.bukkit.entity.AnimalTamer, org.bukkit.configuration.serialization.ConfigurationSerializable, io.papermc.paper.persistence.PersistentDataViewHolder, net.kyori.adventure.text.object.PlayerHeadObjectContents$SkinSource {
    boolean isOnline();
    boolean isConnected();
    java.lang.String getName();
    java.util.UUID getUniqueId();
    com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile();
    boolean isBanned();
    default org.bukkit.BanEntry banPlayer(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.OfflinePlayer.banPlayer(Ljava/lang/String;)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayer(java.lang.String arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.OfflinePlayer.banPlayer(Ljava/lang/String;Ljava/lang/String;)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayer(java.lang.String arg0, java.util.Date arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.OfflinePlayer.banPlayer(Ljava/lang/String;Ljava/util/Date;)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayer(java.lang.String arg0, java.util.Date arg1, java.lang.String arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.OfflinePlayer.banPlayer(Ljava/lang/String;Ljava/util/Date;Ljava/lang/String;)Lorg/bukkit/BanEntry;");
        return null;
    }
    default org.bukkit.BanEntry banPlayer(java.lang.String arg0, java.util.Date arg1, java.lang.String arg2, boolean arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.OfflinePlayer.banPlayer(Ljava/lang/String;Ljava/util/Date;Ljava/lang/String;Z)Lorg/bukkit/BanEntry;");
        return null;
    }
    org.bukkit.BanEntry ban(java.lang.String arg0, java.util.Date arg1, java.lang.String arg2);
    org.bukkit.BanEntry ban(java.lang.String arg0, java.time.Instant arg1, java.lang.String arg2);
    org.bukkit.BanEntry ban(java.lang.String arg0, java.time.Duration arg1, java.lang.String arg2);
    boolean isWhitelisted();
    void setWhitelisted(boolean arg0);
    org.bukkit.entity.Player getPlayer();
    long getFirstPlayed();
    long getLastPlayed();
    boolean hasPlayedBefore();
    default org.bukkit.Location getBedSpawnLocation() {
        return null;
    }
    long getLastLogin();
    long getLastSeen();
    default org.bukkit.Location getRespawnLocation() {
        return null;
    }
    org.bukkit.Location getRespawnLocation(boolean arg0);
    void incrementStatistic(org.bukkit.Statistic arg0) throws java.lang.IllegalArgumentException;
    void decrementStatistic(org.bukkit.Statistic arg0) throws java.lang.IllegalArgumentException;
    void incrementStatistic(org.bukkit.Statistic arg0, int arg1) throws java.lang.IllegalArgumentException;
    void decrementStatistic(org.bukkit.Statistic arg0, int arg1) throws java.lang.IllegalArgumentException;
    void setStatistic(org.bukkit.Statistic arg0, int arg1) throws java.lang.IllegalArgumentException;
    int getStatistic(org.bukkit.Statistic arg0) throws java.lang.IllegalArgumentException;
    void incrementStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1) throws java.lang.IllegalArgumentException;
    void decrementStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1) throws java.lang.IllegalArgumentException;
    int getStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1) throws java.lang.IllegalArgumentException;
    void incrementStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1, int arg2) throws java.lang.IllegalArgumentException;
    void decrementStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1, int arg2) throws java.lang.IllegalArgumentException;
    void setStatistic(org.bukkit.Statistic arg0, org.bukkit.Material arg1, int arg2) throws java.lang.IllegalArgumentException;
    void incrementStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1) throws java.lang.IllegalArgumentException;
    void decrementStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1) throws java.lang.IllegalArgumentException;
    int getStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1) throws java.lang.IllegalArgumentException;
    void incrementStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1, int arg2) throws java.lang.IllegalArgumentException;
    void decrementStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1, int arg2);
    void setStatistic(org.bukkit.Statistic arg0, org.bukkit.entity.EntityType arg1, int arg2);
    org.bukkit.Location getLastDeathLocation();
    org.bukkit.Location getLocation();
    io.papermc.paper.persistence.PersistentDataContainerView getPersistentDataContainer();
    default void applySkinToPlayerHeadContents(net.kyori.adventure.text.object.PlayerHeadObjectContents$Builder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.OfflinePlayer.applySkinToPlayerHeadContents(Lnet/kyori/adventure/text/object/PlayerHeadObjectContents$Builder;)V");
    }
}
