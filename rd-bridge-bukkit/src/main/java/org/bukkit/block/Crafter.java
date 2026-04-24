package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Crafter extends org.bukkit.block.Container, com.destroystokyo.paper.loottable.LootableBlockInventory {
    int getCraftingTicks();
    void setCraftingTicks(int arg0);
    boolean isSlotDisabled(int arg0);
    void setSlotDisabled(int arg0, boolean arg1);
    boolean isTriggered();
    void setTriggered(boolean arg0);
}
