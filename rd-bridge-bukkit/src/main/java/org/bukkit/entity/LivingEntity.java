package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface LivingEntity extends org.bukkit.attribute.Attributable, org.bukkit.entity.Damageable, org.bukkit.projectiles.ProjectileSource, io.papermc.paper.entity.Frictional {
    double getEyeHeight();
    double getEyeHeight(boolean arg0);
    org.bukkit.Location getEyeLocation();
    java.util.List getLineOfSight(java.util.Set arg0, int arg1);
    org.bukkit.block.Block getTargetBlock(java.util.Set arg0, int arg1);
    default org.bukkit.block.Block getTargetBlock(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.getTargetBlock(I)Lorg/bukkit/block/Block;");
        return null;
    }
    org.bukkit.block.Block getTargetBlock(int arg0, com.destroystokyo.paper.block.TargetBlockInfo$FluidMode arg1);
    default org.bukkit.block.BlockFace getTargetBlockFace(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.getTargetBlockFace(I)Lorg/bukkit/block/BlockFace;");
        return null;
    }
    org.bukkit.block.BlockFace getTargetBlockFace(int arg0, com.destroystokyo.paper.block.TargetBlockInfo$FluidMode arg1);
    org.bukkit.block.BlockFace getTargetBlockFace(int arg0, org.bukkit.FluidCollisionMode arg1);
    default com.destroystokyo.paper.block.TargetBlockInfo getTargetBlockInfo(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.getTargetBlockInfo(I)Lcom/destroystokyo/paper/block/TargetBlockInfo;");
        return null;
    }
    com.destroystokyo.paper.block.TargetBlockInfo getTargetBlockInfo(int arg0, com.destroystokyo.paper.block.TargetBlockInfo$FluidMode arg1);
    default org.bukkit.entity.Entity getTargetEntity(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.getTargetEntity(I)Lorg/bukkit/entity/Entity;");
        return null;
    }
    org.bukkit.entity.Entity getTargetEntity(int arg0, boolean arg1);
    default com.destroystokyo.paper.entity.TargetEntityInfo getTargetEntityInfo(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.getTargetEntityInfo(I)Lcom/destroystokyo/paper/entity/TargetEntityInfo;");
        return null;
    }
    default org.bukkit.util.RayTraceResult rayTraceEntities(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.rayTraceEntities(I)Lorg/bukkit/util/RayTraceResult;");
        return null;
    }
    com.destroystokyo.paper.entity.TargetEntityInfo getTargetEntityInfo(int arg0, boolean arg1);
    org.bukkit.util.RayTraceResult rayTraceEntities(int arg0, boolean arg1);
    java.util.List getLastTwoTargetBlocks(java.util.Set arg0, int arg1);
    default org.bukkit.block.Block getTargetBlockExact(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.getTargetBlockExact(I)Lorg/bukkit/block/Block;");
        return null;
    }
    org.bukkit.block.Block getTargetBlockExact(int arg0, org.bukkit.FluidCollisionMode arg1);
    default org.bukkit.util.RayTraceResult rayTraceBlocks(double arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.rayTraceBlocks(D)Lorg/bukkit/util/RayTraceResult;");
        return null;
    }
    org.bukkit.util.RayTraceResult rayTraceBlocks(double arg0, org.bukkit.FluidCollisionMode arg1);
    int getRemainingAir();
    void setRemainingAir(int arg0);
    int getMaximumAir();
    void setMaximumAir(int arg0);
    org.bukkit.inventory.ItemStack getItemInUse();
    int getItemInUseTicks();
    void setItemInUseTicks(int arg0);
    int getArrowCooldown();
    void setArrowCooldown(int arg0);
    int getArrowsInBody();
    default void setArrowsInBody(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.setArrowsInBody(I)V");
    }
    void setArrowsInBody(int arg0, boolean arg1);
    default void setNextArrowRemoval(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.setNextArrowRemoval(I)V");
    }
    default int getNextArrowRemoval() {
        return 0;
    }
    int getBeeStingerCooldown();
    void setBeeStingerCooldown(int arg0);
    int getBeeStingersInBody();
    void setBeeStingersInBody(int arg0);
    default void setNextBeeStingerRemoval(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.setNextBeeStingerRemoval(I)V");
    }
    default int getNextBeeStingerRemoval() {
        return 0;
    }
    int getMaximumNoDamageTicks();
    void setMaximumNoDamageTicks(int arg0);
    double getLastDamage();
    void setLastDamage(double arg0);
    int getNoDamageTicks();
    void setNoDamageTicks(int arg0);
    int getNoActionTicks();
    void setNoActionTicks(int arg0);
    org.bukkit.entity.Player getKiller();
    void setKiller(org.bukkit.entity.Player arg0);
    default boolean addPotionEffect(org.bukkit.potion.PotionEffect arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.addPotionEffect(Lorg/bukkit/potion/PotionEffect;)Z");
        return false;
    }
    boolean addPotionEffect(org.bukkit.potion.PotionEffect arg0, boolean arg1);
    boolean addPotionEffects(java.util.Collection arg0);
    boolean hasPotionEffect(org.bukkit.potion.PotionEffectType arg0);
    org.bukkit.potion.PotionEffect getPotionEffect(org.bukkit.potion.PotionEffectType arg0);
    void removePotionEffect(org.bukkit.potion.PotionEffectType arg0);
    java.util.Collection getActivePotionEffects();
    boolean clearActivePotionEffects();
    boolean hasLineOfSight(org.bukkit.entity.Entity arg0);
    boolean hasLineOfSight(org.bukkit.Location arg0);
    boolean getRemoveWhenFarAway();
    void setRemoveWhenFarAway(boolean arg0);
    org.bukkit.inventory.EntityEquipment getEquipment();
    void setCanPickupItems(boolean arg0);
    boolean getCanPickupItems();
    boolean isLeashed();
    org.bukkit.entity.Entity getLeashHolder() throws java.lang.IllegalStateException;
    boolean setLeashHolder(org.bukkit.entity.Entity arg0);
    boolean isGliding();
    void setGliding(boolean arg0);
    boolean isSwimming();
    void setSwimming(boolean arg0);
    boolean isRiptiding();
    void setRiptiding(boolean arg0);
    boolean isSleeping();
    boolean isClimbing();
    void setAI(boolean arg0);
    boolean hasAI();
    void attack(org.bukkit.entity.Entity arg0);
    void swingMainHand();
    void swingOffHand();
    void playHurtAnimation(float arg0);
    void setCollidable(boolean arg0);
    boolean isCollidable();
    java.util.Set getCollidableExemptions();
    java.lang.Object getMemory(org.bukkit.entity.memory.MemoryKey arg0);
    void setMemory(org.bukkit.entity.memory.MemoryKey arg0, java.lang.Object arg1);
    org.bukkit.Sound getHurtSound();
    org.bukkit.Sound getDeathSound();
    org.bukkit.Sound getFallDamageSound(int arg0);
    org.bukkit.Sound getFallDamageSoundSmall();
    org.bukkit.Sound getFallDamageSoundBig();
    org.bukkit.Sound getDrinkingSound(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.Sound getEatingSound(org.bukkit.inventory.ItemStack arg0);
    boolean canBreatheUnderwater();
    org.bukkit.entity.EntityCategory getCategory();
    default int getArrowsStuck() {
        return 0;
    }
    default void setArrowsStuck(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.setArrowsStuck(I)V");
    }
    default int getShieldBlockingDelay() {
        return 0;
    }
    default void setShieldBlockingDelay(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.setShieldBlockingDelay(I)V");
    }
    float getSidewaysMovement();
    float getUpwardsMovement();
    float getForwardsMovement();
    void startUsingItem(org.bukkit.inventory.EquipmentSlot arg0);
    void completeUsingActiveItem();
    org.bukkit.inventory.ItemStack getActiveItem();
    void clearActiveItem();
    int getActiveItemRemainingTime();
    void setActiveItemRemainingTime(int arg0);
    boolean hasActiveItem();
    int getActiveItemUsedTime();
    org.bukkit.inventory.EquipmentSlot getActiveItemHand();
    default int getItemUseRemainingTime() {
        return 0;
    }
    default int getHandRaisedTime() {
        return 0;
    }
    default boolean isHandRaised() {
        return false;
    }
    default org.bukkit.inventory.EquipmentSlot getHandRaised() {
        return null;
    }
    boolean isJumping();
    void setJumping(boolean arg0);
    default void playPickupItemAnimation(org.bukkit.entity.Item arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.playPickupItemAnimation(Lorg/bukkit/entity/Item;)V");
    }
    void playPickupItemAnimation(org.bukkit.entity.Item arg0, int arg1);
    float getHurtDirection();
    void setHurtDirection(float arg0);
    default void swingHand(org.bukkit.inventory.EquipmentSlot arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.LivingEntity.swingHand(Lorg/bukkit/inventory/EquipmentSlot;)V");
    }
    void knockback(double arg0, double arg1, double arg2);
    void broadcastSlotBreak(org.bukkit.inventory.EquipmentSlot arg0);
    void broadcastSlotBreak(org.bukkit.inventory.EquipmentSlot arg0, java.util.Collection arg1);
    org.bukkit.inventory.ItemStack damageItemStack(org.bukkit.inventory.ItemStack arg0, int arg1);
    void damageItemStack(org.bukkit.inventory.EquipmentSlot arg0, int arg1);
    float getBodyYaw();
    void setBodyYaw(float arg0);
    boolean canUseEquipmentSlot(org.bukkit.inventory.EquipmentSlot arg0);
    io.papermc.paper.world.damagesource.CombatTracker getCombatTracker();
    void setWaypointStyle(net.kyori.adventure.key.Key arg0);
    void setWaypointColor(org.bukkit.Color arg0);
    net.kyori.adventure.key.Key getWaypointStyle();
    org.bukkit.Color getWaypointColor();
}
