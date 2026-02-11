package com.github.martinambrus.rdforward.world.alpha;

import net.querz.nbt.tag.CompoundTag;

/**
 * Wraps an NBT CompoundTag representing a Minecraft Alpha tile entity
 * (block entity).
 *
 * Uses raw NBT storage for round-trip fidelity — all fields are preserved
 * even for tile entity types we don't explicitly handle.
 *
 * Alpha tile entity NBT structure (common fields):
 *   id: String — tile entity type ("Chest", "Furnace", "Sign", "MobSpawner", etc.)
 *   x: Int — block X coordinate
 *   y: Int — block Y coordinate
 *   z: Int — block Z coordinate
 *
 * Type-specific fields (preserved in raw NBT):
 *   Chest/Furnace: Items list of {Slot: byte, id: short, Count: byte, Damage: short}
 *   Furnace: BurnTime, CookTime shorts
 *   Sign: Text1, Text2, Text3, Text4 strings
 *   MobSpawner: EntityId string, Delay short
 *   NoteBlock: note byte
 */
public class AlphaTileEntity {

    private final CompoundTag nbt;

    /**
     * Create from an existing NBT compound (e.g., loaded from disk).
     */
    public AlphaTileEntity(CompoundTag nbt) {
        this.nbt = nbt;
    }

    /**
     * Create a new tile entity with minimal required fields.
     */
    public AlphaTileEntity(String id, int x, int y, int z) {
        this.nbt = new CompoundTag();
        nbt.putString("id", id);
        nbt.putInt("x", x);
        nbt.putInt("y", y);
        nbt.putInt("z", z);
    }

    /**
     * Get the tile entity type identifier (e.g., "Chest", "Furnace", "Sign").
     */
    public String getId() {
        return nbt.getString("id");
    }

    /**
     * Get the block X coordinate.
     */
    public int getX() {
        return nbt.getInt("x");
    }

    /**
     * Get the block Y coordinate.
     */
    public int getY() {
        return nbt.getInt("y");
    }

    /**
     * Get the block Z coordinate.
     */
    public int getZ() {
        return nbt.getInt("z");
    }

    /**
     * Get the raw NBT compound for serialization or direct field access.
     */
    public CompoundTag toNbt() {
        return nbt;
    }
}
