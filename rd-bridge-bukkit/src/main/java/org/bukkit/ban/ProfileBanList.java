package org.bukkit.ban;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ProfileBanList extends org.bukkit.BanList {
    org.bukkit.BanEntry addBan(org.bukkit.profile.PlayerProfile arg0, java.lang.String arg1, java.util.Date arg2, java.lang.String arg3);
    org.bukkit.BanEntry addBan(com.destroystokyo.paper.profile.PlayerProfile arg0, java.lang.String arg1, java.util.Date arg2, java.lang.String arg3);
    org.bukkit.BanEntry getBanEntry(org.bukkit.profile.PlayerProfile arg0);
    boolean isBanned(org.bukkit.profile.PlayerProfile arg0);
    void pardon(org.bukkit.profile.PlayerProfile arg0);
    org.bukkit.BanEntry addBan(org.bukkit.profile.PlayerProfile arg0, java.lang.String arg1, java.time.Instant arg2, java.lang.String arg3);
    org.bukkit.BanEntry addBan(org.bukkit.profile.PlayerProfile arg0, java.lang.String arg1, java.time.Duration arg2, java.lang.String arg3);
    default org.bukkit.BanEntry addBan(java.lang.Object arg0, java.lang.String arg1, java.util.Date arg2, java.lang.String arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.ban.ProfileBanList.addBan(Ljava/lang/Object;Ljava/lang/String;Ljava/util/Date;Ljava/lang/String;)Lorg/bukkit/BanEntry;");
        return null;
    }
}
