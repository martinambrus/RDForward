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
}
