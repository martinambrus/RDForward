package org.bukkit.block.data;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BlockData extends java.lang.Cloneable {
    org.bukkit.Material getMaterial();
    java.lang.String getAsString();
    java.lang.String getAsString(boolean arg0);
    org.bukkit.block.data.BlockData merge(org.bukkit.block.data.BlockData arg0);
    boolean matches(org.bukkit.block.data.BlockData arg0);
    org.bukkit.block.data.BlockData clone();
    org.bukkit.SoundGroup getSoundGroup();
    int getLightEmission();
    boolean isOccluding();
    boolean requiresCorrectToolForDrops();
    boolean isPreferredTool(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.block.PistonMoveReaction getPistonMoveReaction();
    boolean isSupported(org.bukkit.block.Block arg0);
    boolean isSupported(org.bukkit.Location arg0);
    boolean isFaceSturdy(org.bukkit.block.BlockFace arg0, org.bukkit.block.BlockSupport arg1);
    org.bukkit.util.VoxelShape getCollisionShape(org.bukkit.Location arg0);
    org.bukkit.Color getMapColor();
    org.bukkit.Material getPlacementMaterial();
    void rotate(org.bukkit.block.structure.StructureRotation arg0);
    void mirror(org.bukkit.block.structure.Mirror arg0);
    void copyTo(org.bukkit.block.data.BlockData arg0);
    org.bukkit.block.BlockState createBlockState();
    default float getDestroySpeed(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.data.BlockData.getDestroySpeed(Lorg/bukkit/inventory/ItemStack;)F");
        return 0.0f;
    }
    float getDestroySpeed(org.bukkit.inventory.ItemStack arg0, boolean arg1);
    boolean isRandomlyTicked();
    boolean isReplaceable();
}
