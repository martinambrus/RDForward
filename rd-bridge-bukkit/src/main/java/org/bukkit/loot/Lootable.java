package org.bukkit.loot;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Lootable {
    void setLootTable(org.bukkit.loot.LootTable arg0);
    org.bukkit.loot.LootTable getLootTable();
    void setLootTable(org.bukkit.loot.LootTable arg0, long arg1);
    default boolean hasLootTable() {
        return false;
    }
    default void clearLootTable() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.loot.Lootable.clearLootTable()V");
    }
    void setSeed(long arg0);
    long getSeed();
}
