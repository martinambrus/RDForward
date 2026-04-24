package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BanList {
    org.bukkit.BanEntry getBanEntry(java.lang.String arg0);
    org.bukkit.BanEntry getBanEntry(java.lang.Object arg0);
    org.bukkit.BanEntry addBan(java.lang.String arg0, java.lang.String arg1, java.util.Date arg2, java.lang.String arg3);
    org.bukkit.BanEntry addBan(java.lang.Object arg0, java.lang.String arg1, java.util.Date arg2, java.lang.String arg3);
    org.bukkit.BanEntry addBan(java.lang.Object arg0, java.lang.String arg1, java.time.Instant arg2, java.lang.String arg3);
    org.bukkit.BanEntry addBan(java.lang.Object arg0, java.lang.String arg1, java.time.Duration arg2, java.lang.String arg3);
    java.util.Set getBanEntries();
    java.util.Set getEntries();
    boolean isBanned(java.lang.Object arg0);
    boolean isBanned(java.lang.String arg0);
    void pardon(java.lang.Object arg0);
    void pardon(java.lang.String arg0);
}
