package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface FallingBlock extends org.bukkit.entity.Entity {
    org.bukkit.Material getMaterial();
    org.bukkit.block.data.BlockData getBlockData();
    void setBlockData(org.bukkit.block.data.BlockData arg0);
    org.bukkit.block.BlockState getBlockState();
    void setBlockState(org.bukkit.block.BlockState arg0);
    boolean getDropItem();
    void setDropItem(boolean arg0);
    boolean getCancelDrop();
    void setCancelDrop(boolean arg0);
    boolean canHurtEntities();
    void setHurtEntities(boolean arg0);
    float getDamagePerBlock();
    void setDamagePerBlock(float arg0);
    int getMaxDamage();
    void setMaxDamage(int arg0);
    default org.bukkit.Location getSourceLoc() {
        return null;
    }
    boolean doesAutoExpire();
    void shouldAutoExpire(boolean arg0);
}
