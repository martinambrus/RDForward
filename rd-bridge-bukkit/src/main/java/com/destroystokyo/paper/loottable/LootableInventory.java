package com.destroystokyo.paper.loottable;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface LootableInventory extends org.bukkit.loot.Lootable {
    boolean isRefillEnabled();
    boolean hasBeenFilled();
    default boolean hasPlayerLooted(org.bukkit.entity.Player arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.loottable.LootableInventory.hasPlayerLooted(Lorg/bukkit/entity/Player;)Z");
        return false;
    }
    boolean canPlayerLoot(java.util.UUID arg0);
    boolean hasPlayerLooted(java.util.UUID arg0);
    default java.lang.Long getLastLooted(org.bukkit.entity.Player arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.loottable.LootableInventory.getLastLooted(Lorg/bukkit/entity/Player;)Ljava/lang/Long;");
        return null;
    }
    java.lang.Long getLastLooted(java.util.UUID arg0);
    default boolean setHasPlayerLooted(org.bukkit.entity.Player arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.destroystokyo.paper.loottable.LootableInventory.setHasPlayerLooted(Lorg/bukkit/entity/Player;Z)Z");
        return false;
    }
    boolean setHasPlayerLooted(java.util.UUID arg0, boolean arg1);
    boolean hasPendingRefill();
    long getLastFilled();
    long getNextRefill();
    long setNextRefill(long arg0);
}
