package org.bukkit.inventory.meta.components;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface EquippableComponent extends org.bukkit.configuration.serialization.ConfigurationSerializable {
    org.bukkit.inventory.EquipmentSlot getSlot();
    void setSlot(org.bukkit.inventory.EquipmentSlot arg0);
    org.bukkit.Sound getEquipSound();
    void setEquipSound(org.bukkit.Sound arg0);
    org.bukkit.NamespacedKey getModel();
    void setModel(org.bukkit.NamespacedKey arg0);
    org.bukkit.NamespacedKey getCameraOverlay();
    void setCameraOverlay(org.bukkit.NamespacedKey arg0);
    java.util.Collection getAllowedEntities();
    void setAllowedEntities(org.bukkit.entity.EntityType arg0);
    void setAllowedEntities(java.util.Collection arg0);
    void setAllowedEntities(org.bukkit.Tag arg0);
    boolean isDispensable();
    void setDispensable(boolean arg0);
    boolean isSwappable();
    void setSwappable(boolean arg0);
    boolean isDamageOnHurt();
    void setDamageOnHurt(boolean arg0);
    boolean isEquipOnInteract();
    void setEquipOnInteract(boolean arg0);
    boolean canBeSheared();
    void setCanBeSheared(boolean arg0);
    org.bukkit.Sound getShearingSound();
    void setShearingSound(org.bukkit.Sound arg0);
}
