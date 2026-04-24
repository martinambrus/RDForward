package org.bukkit.attribute;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface AttributeInstance {
    org.bukkit.attribute.Attribute getAttribute();
    double getBaseValue();
    void setBaseValue(double arg0);
    java.util.Collection getModifiers();
    org.bukkit.attribute.AttributeModifier getModifier(net.kyori.adventure.key.Key arg0);
    void removeModifier(net.kyori.adventure.key.Key arg0);
    org.bukkit.attribute.AttributeModifier getModifier(java.util.UUID arg0);
    void removeModifier(java.util.UUID arg0);
    void addModifier(org.bukkit.attribute.AttributeModifier arg0);
    void addTransientModifier(org.bukkit.attribute.AttributeModifier arg0);
    void removeModifier(org.bukkit.attribute.AttributeModifier arg0);
    double getValue();
    double getDefaultValue();
}
