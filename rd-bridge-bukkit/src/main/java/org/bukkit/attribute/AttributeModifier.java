package org.bukkit.attribute;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class AttributeModifier implements org.bukkit.configuration.serialization.ConfigurationSerializable, org.bukkit.Keyed {
    public AttributeModifier(java.lang.String arg0, double arg1, org.bukkit.attribute.AttributeModifier$Operation arg2) {}
    public AttributeModifier(java.util.UUID arg0, java.lang.String arg1, double arg2, org.bukkit.attribute.AttributeModifier$Operation arg3) {}
    public AttributeModifier(java.util.UUID arg0, java.lang.String arg1, double arg2, org.bukkit.attribute.AttributeModifier$Operation arg3, org.bukkit.inventory.EquipmentSlot arg4) {}
    public AttributeModifier(java.util.UUID arg0, java.lang.String arg1, double arg2, org.bukkit.attribute.AttributeModifier$Operation arg3, org.bukkit.inventory.EquipmentSlotGroup arg4) {}
    public AttributeModifier(org.bukkit.NamespacedKey arg0, double arg1, org.bukkit.attribute.AttributeModifier$Operation arg2) {}
    public AttributeModifier(org.bukkit.NamespacedKey arg0, double arg1, org.bukkit.attribute.AttributeModifier$Operation arg2, org.bukkit.inventory.EquipmentSlotGroup arg3) {}
    public AttributeModifier() {}
    public java.util.UUID getUniqueId() {
        return null;
    }
    public org.bukkit.NamespacedKey getKey() {
        return null;
    }
    public java.lang.String getName() {
        return null;
    }
    public double getAmount() {
        return 0.0;
    }
    public org.bukkit.attribute.AttributeModifier$Operation getOperation() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlot getSlot() {
        return null;
    }
    public org.bukkit.inventory.EquipmentSlotGroup getSlotGroup() {
        return null;
    }
    public java.util.Map serialize() {
        return java.util.Collections.emptyMap();
    }
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.attribute.AttributeModifier.equals(Ljava/lang/Object;)Z");
        return false;
    }
    public int hashCode() {
        return 0;
    }
    public java.lang.String toString() {
        return null;
    }
    public static org.bukkit.attribute.AttributeModifier deserialize(java.util.Map arg0) {
        return null;
    }
}
