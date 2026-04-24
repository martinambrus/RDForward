package org.bukkit.loot;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface LootTable extends org.bukkit.Keyed {
    java.util.Collection populateLoot(java.util.Random arg0, org.bukkit.loot.LootContext arg1);
    void fillInventory(org.bukkit.inventory.Inventory arg0, java.util.Random arg1, org.bukkit.loot.LootContext arg2);
}
