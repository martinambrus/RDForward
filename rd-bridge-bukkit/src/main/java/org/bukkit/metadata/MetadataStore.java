package org.bukkit.metadata;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MetadataStore {
    void setMetadata(java.lang.Object arg0, java.lang.String arg1, org.bukkit.metadata.MetadataValue arg2);
    java.util.List getMetadata(java.lang.Object arg0, java.lang.String arg1);
    boolean hasMetadata(java.lang.Object arg0, java.lang.String arg1);
    void removeMetadata(java.lang.Object arg0, java.lang.String arg1, org.bukkit.plugin.Plugin arg2);
    void invalidateAll(org.bukkit.plugin.Plugin arg0);
}
