package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BanEntry {
    java.lang.String getTarget();
    java.lang.Object getBanTarget();
    java.util.Date getCreated();
    void setCreated(java.util.Date arg0);
    java.lang.String getSource();
    void setSource(java.lang.String arg0);
    java.util.Date getExpiration();
    void setExpiration(java.util.Date arg0);
    java.lang.String getReason();
    void setReason(java.lang.String arg0);
    void save();
    void remove();
}
