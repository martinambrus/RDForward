package org.bukkit.inventory.meta.components;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ToolComponent$ToolRule extends org.bukkit.configuration.serialization.ConfigurationSerializable {
    java.util.Collection getBlocks();
    void setBlocks(org.bukkit.Material arg0);
    void setBlocks(java.util.Collection arg0);
    void setBlocks(org.bukkit.Tag arg0);
    java.lang.Float getSpeed();
    void setSpeed(java.lang.Float arg0);
    java.lang.Boolean isCorrectForDrops();
    void setCorrectForDrops(java.lang.Boolean arg0);
}
