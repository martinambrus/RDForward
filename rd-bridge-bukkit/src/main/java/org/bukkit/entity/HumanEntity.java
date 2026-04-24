package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface HumanEntity extends org.bukkit.entity.LivingEntity, org.bukkit.entity.AnimalTamer, org.bukkit.inventory.InventoryHolder {
    org.bukkit.inventory.EntityEquipment getEquipment();
    java.lang.String getName();
    org.bukkit.inventory.PlayerInventory getInventory();
    org.bukkit.inventory.Inventory getEnderChest();
    org.bukkit.inventory.MainHand getMainHand();
    boolean setWindowProperty(org.bukkit.inventory.InventoryView$Property arg0, int arg1);
    int getEnchantmentSeed();
    void setEnchantmentSeed(int arg0);
    org.bukkit.inventory.InventoryView getOpenInventory();
    org.bukkit.inventory.InventoryView openInventory(org.bukkit.inventory.Inventory arg0);
    org.bukkit.inventory.InventoryView openWorkbench(org.bukkit.Location arg0, boolean arg1);
    org.bukkit.inventory.InventoryView openEnchanting(org.bukkit.Location arg0, boolean arg1);
    void openInventory(org.bukkit.inventory.InventoryView arg0);
    default org.bukkit.inventory.InventoryView openMerchant(org.bukkit.entity.Villager arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.HumanEntity.openMerchant(Lorg/bukkit/entity/Villager;Z)Lorg/bukkit/inventory/InventoryView;");
        return null;
    }
    org.bukkit.inventory.InventoryView openMerchant(org.bukkit.inventory.Merchant arg0, boolean arg1);
    org.bukkit.inventory.InventoryView openAnvil(org.bukkit.Location arg0, boolean arg1);
    org.bukkit.inventory.InventoryView openCartographyTable(org.bukkit.Location arg0, boolean arg1);
    org.bukkit.inventory.InventoryView openGrindstone(org.bukkit.Location arg0, boolean arg1);
    org.bukkit.inventory.InventoryView openLoom(org.bukkit.Location arg0, boolean arg1);
    org.bukkit.inventory.InventoryView openSmithingTable(org.bukkit.Location arg0, boolean arg1);
    org.bukkit.inventory.InventoryView openStonecutter(org.bukkit.Location arg0, boolean arg1);
    default void closeInventory() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.HumanEntity.closeInventory()V");
    }
    void closeInventory(org.bukkit.event.inventory.InventoryCloseEvent$Reason arg0);
    org.bukkit.inventory.ItemStack getItemInHand();
    void setItemInHand(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.inventory.ItemStack getItemOnCursor();
    void setItemOnCursor(org.bukkit.inventory.ItemStack arg0);
    boolean hasCooldown(org.bukkit.Material arg0);
    int getCooldown(org.bukkit.Material arg0);
    default void setCooldown(org.bukkit.Material arg0, int arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.HumanEntity.setCooldown(Lorg/bukkit/Material;I)V");
    }
    void setHurtDirection(float arg0);
    boolean isDeeplySleeping();
    boolean hasCooldown(org.bukkit.inventory.ItemStack arg0);
    int getCooldown(org.bukkit.inventory.ItemStack arg0);
    void setCooldown(org.bukkit.inventory.ItemStack arg0, int arg1);
    int getCooldown(net.kyori.adventure.key.Key arg0);
    void setCooldown(net.kyori.adventure.key.Key arg0, int arg1);
    int getSleepTicks();
    default org.bukkit.Location getPotentialBedLocation() {
        return null;
    }
    org.bukkit.Location getPotentialRespawnLocation();
    org.bukkit.entity.FishHook getFishHook();
    boolean sleep(org.bukkit.Location arg0, boolean arg1);
    void wakeup(boolean arg0);
    void startRiptideAttack(int arg0, float arg1, org.bukkit.inventory.ItemStack arg2);
    org.bukkit.Location getBedLocation();
    org.bukkit.GameMode getGameMode();
    void setGameMode(org.bukkit.GameMode arg0);
    boolean isBlocking();
    boolean isHandRaised();
    int getExpToLevel();
    org.bukkit.entity.Entity releaseLeftShoulderEntity();
    org.bukkit.entity.Entity releaseRightShoulderEntity();
    float getAttackCooldown();
    default boolean discoverRecipe(org.bukkit.NamespacedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.HumanEntity.discoverRecipe(Lorg/bukkit/NamespacedKey;)Z");
        return false;
    }
    int discoverRecipes(java.util.Collection arg0);
    default boolean undiscoverRecipe(org.bukkit.NamespacedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.HumanEntity.undiscoverRecipe(Lorg/bukkit/NamespacedKey;)Z");
        return false;
    }
    int undiscoverRecipes(java.util.Collection arg0);
    boolean hasDiscoveredRecipe(org.bukkit.NamespacedKey arg0);
    java.util.Set getDiscoveredRecipes();
    org.bukkit.entity.Entity getShoulderEntityLeft();
    void setShoulderEntityLeft(org.bukkit.entity.Entity arg0);
    org.bukkit.entity.Entity getShoulderEntityRight();
    void setShoulderEntityRight(org.bukkit.entity.Entity arg0);
    default void openSign(org.bukkit.block.Sign arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.HumanEntity.openSign(Lorg/bukkit/block/Sign;)V");
    }
    void openSign(org.bukkit.block.Sign arg0, org.bukkit.block.sign.Side arg1);
    boolean dropItem(boolean arg0);
    default org.bukkit.entity.Item dropItem(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.HumanEntity.dropItem(I)Lorg/bukkit/entity/Item;");
        return null;
    }
    default org.bukkit.entity.Item dropItem(int arg0, int arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.HumanEntity.dropItem(II)Lorg/bukkit/entity/Item;");
        return null;
    }
    org.bukkit.entity.Item dropItem(int arg0, int arg1, boolean arg2, java.util.function.Consumer arg3);
    default org.bukkit.entity.Item dropItem(org.bukkit.inventory.EquipmentSlot arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.HumanEntity.dropItem(Lorg/bukkit/inventory/EquipmentSlot;)Lorg/bukkit/entity/Item;");
        return null;
    }
    default org.bukkit.entity.Item dropItem(org.bukkit.inventory.EquipmentSlot arg0, int arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.HumanEntity.dropItem(Lorg/bukkit/inventory/EquipmentSlot;I)Lorg/bukkit/entity/Item;");
        return null;
    }
    org.bukkit.entity.Item dropItem(org.bukkit.inventory.EquipmentSlot arg0, int arg1, boolean arg2, java.util.function.Consumer arg3);
    default org.bukkit.entity.Item dropItem(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.HumanEntity.dropItem(Lorg/bukkit/inventory/ItemStack;)Lorg/bukkit/entity/Item;");
        return null;
    }
    org.bukkit.entity.Item dropItem(org.bukkit.inventory.ItemStack arg0, boolean arg1, java.util.function.Consumer arg2);
    float getExhaustion();
    void setExhaustion(float arg0);
    float getSaturation();
    void setSaturation(float arg0);
    int getFoodLevel();
    void setFoodLevel(int arg0);
    int getSaturatedRegenRate();
    void setSaturatedRegenRate(int arg0);
    int getUnsaturatedRegenRate();
    void setUnsaturatedRegenRate(int arg0);
    int getStarvationRate();
    void setStarvationRate(int arg0);
    org.bukkit.Location getLastDeathLocation();
    void setLastDeathLocation(org.bukkit.Location arg0);
    org.bukkit.entity.Firework fireworkBoost(org.bukkit.inventory.ItemStack arg0);
}
