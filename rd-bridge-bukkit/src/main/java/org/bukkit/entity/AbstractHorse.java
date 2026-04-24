package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface AbstractHorse extends org.bukkit.entity.Vehicle, org.bukkit.inventory.InventoryHolder, org.bukkit.entity.Tameable {
    org.bukkit.entity.Horse$Variant getVariant();
    void setVariant(org.bukkit.entity.Horse$Variant arg0);
    int getDomestication();
    void setDomestication(int arg0);
    int getMaxDomestication();
    void setMaxDomestication(int arg0);
    double getJumpStrength();
    void setJumpStrength(double arg0);
    boolean isEatingHaystack();
    void setEatingHaystack(boolean arg0);
    org.bukkit.inventory.AbstractHorseInventory getInventory();
    boolean isEatingGrass();
    void setEatingGrass(boolean arg0);
    boolean isRearing();
    void setRearing(boolean arg0);
    boolean isEating();
    void setEating(boolean arg0);
}
