package org.bukkit.inventory.meta.components;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ToolComponent extends org.bukkit.configuration.serialization.ConfigurationSerializable {
    float getDefaultMiningSpeed();
    void setDefaultMiningSpeed(float arg0);
    int getDamagePerBlock();
    void setDamagePerBlock(int arg0);
    java.util.List getRules();
    void setRules(java.util.List arg0);
    org.bukkit.inventory.meta.components.ToolComponent$ToolRule addRule(org.bukkit.Material arg0, java.lang.Float arg1, java.lang.Boolean arg2);
    org.bukkit.inventory.meta.components.ToolComponent$ToolRule addRule(java.util.Collection arg0, java.lang.Float arg1, java.lang.Boolean arg2);
    org.bukkit.inventory.meta.components.ToolComponent$ToolRule addRule(org.bukkit.Tag arg0, java.lang.Float arg1, java.lang.Boolean arg2);
    boolean removeRule(org.bukkit.inventory.meta.components.ToolComponent$ToolRule arg0);
}
