package org.bukkit.metadata;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MetadataValue {
    java.lang.Object value();
    int asInt();
    float asFloat();
    double asDouble();
    long asLong();
    short asShort();
    byte asByte();
    boolean asBoolean();
    java.lang.String asString();
    org.bukkit.plugin.Plugin getOwningPlugin();
    void invalidate();
}
