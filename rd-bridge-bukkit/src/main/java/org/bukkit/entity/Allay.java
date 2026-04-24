package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Allay extends org.bukkit.entity.Creature, org.bukkit.inventory.InventoryHolder {
    boolean canDuplicate();
    void setCanDuplicate(boolean arg0);
    long getDuplicationCooldown();
    void setDuplicationCooldown(long arg0);
    void resetDuplicationCooldown();
    boolean isDancing();
    void startDancing(org.bukkit.Location arg0);
    void startDancing();
    void stopDancing();
    org.bukkit.entity.Allay duplicateAllay();
    org.bukkit.Location getJukebox();
}
