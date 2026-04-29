// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

/**
 * Bukkit-shaped {@code Block} interface. Plugins compiled against
 * Bukkit/Paper expect this type to be an INTERFACE — WorldEdit 5.6.1's
 * {@code BukkitWorld.getBlockData} performs an {@code invokeinterface}
 * on the result of {@code world.getBlockAt(...)}, which raises
 * {@link IncompatibleClassChangeError} when this type is a class.
 *
 * Bridge implementation lives in
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlock}.
 */
public interface Block {

    World getWorld();

    int getX();

    int getY();

    int getZ();

    Material getType();

    /** Set the block type. Forwards to the enclosing world. */
    void setType(Material type);

    Location getLocation();

    default boolean isEmpty() {
        Material t = getType();
        return t == null || t == Material.AIR;
    }

    /** Pre-Flattening numeric block id of this block. WorldEdit 5.6.1's
     *  {@code BukkitWorld.getBlockType} reads this through
     *  {@code World.getBlockTypeIdAt} -> Block.getType().getId(); the
     *  direct {@code getTypeId()} signature is provided here for plugins
     *  that hold a Block reference instead of recomputing a
     *  world-coordinate lookup. */
    default int getTypeId() {
        Material t = getType();
        return t == null ? 0 : t.getId();
    }

    /** Pre-Flattening data nibble. RDForward does not model block data
     *  (orientation, growth stage, etc.) — return 0 so WE 5.6.1's
     *  {@code BukkitWorld.getBlockData} reads a stable default. */
    default byte getData() {
        return 0;
    }

    /** Set the block by legacy id. Resolves through {@link Material#getMaterial(int)};
     *  unknown ids fall through to AIR. */
    default boolean setTypeId(int legacyId) {
        Material m = Material.getMaterial(legacyId);
        setType(m == null ? Material.AIR : m);
        return true;
    }

    /** Real Bukkit signature WE 5.6.1's {@code rawSetBlock} hits during
     *  {@code //set}. Data nibble is dropped on the floor — RDForward
     *  has no data-aware block storage — and physics flag is ignored. */
    default boolean setTypeIdAndData(int legacyId, byte data, boolean applyPhysics) {
        return setTypeId(legacyId);
    }

    /** Noop — see {@link #getData()}. */
    default void setData(byte data) {
    }

    /** Noop variant carrying a physics flag. */
    default void setData(byte data, boolean applyPhysics) {
    }

    /** @return a snapshot {@link BlockState} for this block. CoreProtect's
     *  {@code BlockBreakListener.processBlockBreak} captures the state to
     *  log the broken block's material; without this method the listener
     *  {@link NoSuchMethodError}s on every break. */
    default BlockState getState() {
        return new com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlockState(
                getWorld(), getX(), getY(), getZ(), getType());
    }

    /** @return a snapshot {@link org.bukkit.block.data.BlockData} for
     *  this block. CoreProtect's {@code BlockPlaceListener} stores the
     *  placed block's namespaced descriptor via
     *  {@code getBlockData().getAsString()}; RDForward has no per-block
     *  state model, so the descriptor is just {@code "minecraft:<name>"}. */
    default org.bukkit.block.data.BlockData getBlockData() {
        return new com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlockData(getType());
    }

    /** Block adjacent to this one in the given direction. CoreProtect's
     *  inspector path computes the placement target as
     *  {@code clickedBlock.getRelative(blockFace)} and queries audit
     *  history there; without this method the call {@link
     *  NoSuchMethodError}s mid-inspect. Uses the face's
     *  {@link BlockFace#getModX()}/{@code getModY()}/{@code getModZ()}
     *  offsets — {@link BlockFace#SELF} returns this block. */
    default Block getRelative(BlockFace face) {
        if (face == null) return this;
        return new com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlock(
                getWorld(),
                getX() + face.getModX(),
                getY() + face.getModY(),
                getZ() + face.getModZ(),
                getType());
    }

    /** Set this block's data. CoreProtect's
     *  {@code BlockUtils.setTypeAndData} hits this signature during
     *  rollback to restore a recorded block. RDForward has no per-block
     *  state model, so we forward only the underlying material — the
     *  physics flag is ignored (no neighbor updates fire here). */
    default void setBlockData(org.bukkit.block.data.BlockData data, boolean applyPhysics) {
        if (data == null) { setType(Material.AIR); return; }
        setType(data.getMaterial());
    }

    /** Convenience overload — physics defaults to true on real Bukkit
     *  but is dropped here since RDForward doesn't model neighbor ticks. */
    default void setBlockData(org.bukkit.block.data.BlockData data) {
        setBlockData(data, true);
    }

    /** @return whether entities can pass through this block. CoreProtect's
     *  {@code BlockUtils.passableBlock} reads this during
     *  {@code Teleport.performSafeTeleport} (called from rollback's
     *  per-chunk processing) to find a safe Y above the rollback origin.
     *  RDForward has no per-block collision model, so AIR is the only
     *  passable type. */
    default boolean isPassable() {
        Material t = getType();
        return t == null || t == Material.AIR;
    }

    /** Snapshot {@link org.bukkit.Chunk} containing this block. CoreProtect's
     *  {@code RollbackProcessor.processChunk} calls
     *  {@code block.getChunk()} then feeds it to
     *  {@code World.isChunkLoaded(Chunk)} and reads
     *  {@code chunk.getEntities()} during rollback iteration. RDForward
     *  has no per-chunk addressing in the rd-api world, so we synthesize
     *  the chunk coords from the block's world coords (>>4). */
    default org.bukkit.Chunk getChunk() {
        return new com.github.martinambrus.rdforward.bridge.bukkit.BukkitChunk(
                getWorld(), getX() >> 4, getZ() >> 4);
    }

    /** Convenience overload — explicit step distance along a face. */
    default Block getRelative(BlockFace face, int distance) {
        if (face == null) return this;
        return new com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlock(
                getWorld(),
                getX() + face.getModX() * distance,
                getY() + face.getModY() * distance,
                getZ() + face.getModZ() * distance,
                getType());
    }
}
