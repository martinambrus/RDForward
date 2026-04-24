package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Animals extends org.bukkit.entity.Breedable {
    java.util.UUID getBreedCause();
    void setBreedCause(java.util.UUID arg0);
    boolean isLoveMode();
    int getLoveModeTicks();
    void setLoveModeTicks(int arg0);
    boolean isBreedItem(org.bukkit.inventory.ItemStack arg0);
    boolean isBreedItem(org.bukkit.Material arg0);
}
