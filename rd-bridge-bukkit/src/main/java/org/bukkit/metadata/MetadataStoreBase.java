package org.bukkit.metadata;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class MetadataStoreBase {
    public MetadataStoreBase() {}
    public void setMetadata(java.lang.Object arg0, java.lang.String arg1, org.bukkit.metadata.MetadataValue arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.metadata.MetadataStoreBase.setMetadata(Ljava/lang/Object;Ljava/lang/String;Lorg/bukkit/metadata/MetadataValue;)V");
    }
    public java.util.List getMetadata(java.lang.Object arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.metadata.MetadataStoreBase.getMetadata(Ljava/lang/Object;Ljava/lang/String;)Ljava/util/List;");
        return java.util.Collections.emptyList();
    }
    public boolean hasMetadata(java.lang.Object arg0, java.lang.String arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.metadata.MetadataStoreBase.hasMetadata(Ljava/lang/Object;Ljava/lang/String;)Z");
        return false;
    }
    public void removeMetadata(java.lang.Object arg0, java.lang.String arg1, org.bukkit.plugin.Plugin arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.metadata.MetadataStoreBase.removeMetadata(Ljava/lang/Object;Ljava/lang/String;Lorg/bukkit/plugin/Plugin;)V");
    }
    public void invalidateAll(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.metadata.MetadataStoreBase.invalidateAll(Lorg/bukkit/plugin/Plugin;)V");
    }
    public void removeAll(org.bukkit.plugin.Plugin arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.metadata.MetadataStoreBase.removeAll(Lorg/bukkit/plugin/Plugin;)V");
    }
    protected abstract java.lang.String disambiguate(java.lang.Object arg0, java.lang.String arg1);
}
