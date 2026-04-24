package org.bukkit.metadata;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Metadatable {
    void setMetadata(java.lang.String arg0, org.bukkit.metadata.MetadataValue arg1);
    java.util.List getMetadata(java.lang.String arg0);
    boolean hasMetadata(java.lang.String arg0);
    void removeMetadata(java.lang.String arg0, org.bukkit.plugin.Plugin arg1);
}
