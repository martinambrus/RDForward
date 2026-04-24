package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BlockState extends org.bukkit.metadata.Metadatable {
    org.bukkit.block.Block getBlock();
    org.bukkit.material.MaterialData getData();
    org.bukkit.block.data.BlockData getBlockData();
    org.bukkit.block.BlockState copy();
    org.bukkit.block.BlockState copy(org.bukkit.Location arg0);
    org.bukkit.Material getType();
    byte getLightLevel();
    org.bukkit.World getWorld();
    int getX();
    int getY();
    int getZ();
    org.bukkit.Location getLocation();
    org.bukkit.Location getLocation(org.bukkit.Location arg0);
    org.bukkit.Chunk getChunk();
    void setData(org.bukkit.material.MaterialData arg0);
    void setBlockData(org.bukkit.block.data.BlockData arg0);
    void setType(org.bukkit.Material arg0);
    boolean update();
    boolean update(boolean arg0);
    boolean update(boolean arg0, boolean arg1);
    byte getRawData();
    void setRawData(byte arg0);
    boolean isPlaced();
    boolean isCollidable();
    default java.util.Collection getDrops() {
        return java.util.Collections.emptyList();
    }
    default java.util.Collection getDrops(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.BlockState.getDrops(Lorg/bukkit/inventory/ItemStack;)Ljava/util/Collection;");
        return java.util.Collections.emptyList();
    }
    java.util.Collection getDrops(org.bukkit.inventory.ItemStack arg0, org.bukkit.entity.Entity arg1);
    boolean isSuffocating();
}
