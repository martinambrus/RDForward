// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

/**
 * Snapshot {@link BlockData} backed by a {@link Material}. CoreProtect
 * (and other modern plugins) call {@code block.getBlockData().getAsString()}
 * to record a Mojang-compat block descriptor in their audit log;
 * RDForward does not model block states (orientation, growth stage,
 * waterlogged, etc.) so the descriptor is just the namespaced material
 * name with no property bracket suffix.
 */
public final class BukkitBlockData implements BlockData {

    private final Material material;

    public BukkitBlockData(Material material) {
        this.material = material == null ? Material.AIR : material;
    }

    @Override public Material getMaterial() { return material; }

    /** {@code "minecraft:<lowercase_name>"} — matches the Mojang
     *  block-state string CoreProtect persists. */
    @Override public String getAsString() {
        return "minecraft:" + material.name().toLowerCase(java.util.Locale.ROOT);
    }

    @Override public String getAsString(boolean hideUnspecified) { return getAsString(); }

    @Override public BlockData merge(BlockData other) { return this; }
    @Override public boolean matches(BlockData other) {
        return other != null && other.getMaterial() == this.material;
    }
    @Override public BlockData clone() { return new BukkitBlockData(material); }
    @Override public org.bukkit.SoundGroup getSoundGroup() { return null; }
    @Override public int getLightEmission() { return 0; }
    @Override public boolean isOccluding() { return false; }
    @Override public boolean requiresCorrectToolForDrops() { return false; }
    @Override public boolean isPreferredTool(org.bukkit.inventory.ItemStack tool) { return true; }
    @Override public org.bukkit.block.PistonMoveReaction getPistonMoveReaction() { return null; }
    @Override public boolean isSupported(org.bukkit.block.Block block) { return true; }
    @Override public boolean isSupported(org.bukkit.Location loc) { return true; }
    @Override public boolean isFaceSturdy(org.bukkit.block.BlockFace face, org.bukkit.block.BlockSupport support) { return false; }
    @Override public org.bukkit.util.VoxelShape getCollisionShape(org.bukkit.Location loc) { return null; }
    @Override public org.bukkit.Color getMapColor() { return null; }
    @Override public Material getPlacementMaterial() { return material; }
    @Override public void rotate(org.bukkit.block.structure.StructureRotation rot) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.data.BlockData.rotate");
    }
    @Override public void mirror(org.bukkit.block.structure.Mirror mirror) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.data.BlockData.mirror");
    }
    @Override public void copyTo(BlockData target) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.data.BlockData.copyTo");
    }
    @Override public org.bukkit.block.BlockState createBlockState() {
        return new BukkitBlockState(null, 0, 0, 0, material);
    }
    @Override public float getDestroySpeed(org.bukkit.inventory.ItemStack tool, boolean considerEnchants) { return 0f; }
    @Override public boolean isRandomlyTicked() { return false; }
    @Override public boolean isReplaceable() { return material == Material.AIR; }
}
