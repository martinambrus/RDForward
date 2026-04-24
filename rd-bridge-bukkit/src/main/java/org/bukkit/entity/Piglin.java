package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Piglin extends org.bukkit.entity.PiglinAbstract, org.bukkit.inventory.InventoryHolder, com.destroystokyo.paper.entity.RangedEntity {
    boolean isAbleToHunt();
    void setIsAbleToHunt(boolean arg0);
    boolean addBarterMaterial(org.bukkit.Material arg0);
    boolean removeBarterMaterial(org.bukkit.Material arg0);
    boolean addMaterialOfInterest(org.bukkit.Material arg0);
    boolean removeMaterialOfInterest(org.bukkit.Material arg0);
    java.util.Set getInterestList();
    java.util.Set getBarterList();
    void setChargingCrossbow(boolean arg0);
    boolean isChargingCrossbow();
    void setDancing(boolean arg0);
    void setDancing(long arg0);
    boolean isDancing();
}
