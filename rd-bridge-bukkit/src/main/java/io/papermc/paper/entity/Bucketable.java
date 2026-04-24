package io.papermc.paper.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Bucketable extends org.bukkit.entity.Entity {
    boolean isFromBucket();
    void setFromBucket(boolean arg0);
    org.bukkit.inventory.ItemStack getBaseBucketItem();
    org.bukkit.Sound getPickupSound();
}
