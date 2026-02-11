package com.github.martinambrus.rdforward.world.alpha;

import net.querz.nbt.tag.*;

/**
 * Wraps an NBT CompoundTag representing a Minecraft Alpha entity.
 *
 * Uses raw NBT storage for round-trip fidelity — all fields are preserved
 * even for entity types we don't explicitly handle. Convenience methods
 * provide typed access to the common fields shared by all entities.
 *
 * Alpha entity NBT structure (common fields):
 *   id: String — entity type name ("Pig", "Zombie", "Item", "Arrow", etc.)
 *   Pos: List[3 doubles] — x, y, z world position
 *   Motion: List[3 doubles] — x, y, z velocity
 *   Rotation: List[2 floats] — yaw, pitch
 *   FallDistance: Float
 *   Fire: Short — remaining fire ticks (-1 = not on fire)
 *   Air: Short — remaining air ticks (300 = full)
 *   OnGround: Byte (boolean)
 *
 * Type-specific fields (preserved in raw NBT):
 *   Items: Item compound (id, Count, Damage) + Age short
 *   Mobs: Health, HurtTime, AttackTime, DeathTime shorts
 *   Projectiles: xTile, yTile, zTile shorts, inTile byte, shake byte
 */
public class AlphaEntity {

    private final CompoundTag nbt;

    /**
     * Create from an existing NBT compound (e.g., loaded from disk).
     */
    public AlphaEntity(CompoundTag nbt) {
        this.nbt = nbt;
    }

    /**
     * Create a new entity with minimal required fields.
     */
    public AlphaEntity(String id, double x, double y, double z) {
        this.nbt = new CompoundTag();
        nbt.putString("id", id);

        ListTag<DoubleTag> pos = new ListTag<DoubleTag>(DoubleTag.class);
        pos.addDouble(x);
        pos.addDouble(y);
        pos.addDouble(z);
        nbt.put("Pos", pos);

        ListTag<DoubleTag> motion = new ListTag<DoubleTag>(DoubleTag.class);
        motion.addDouble(0.0);
        motion.addDouble(0.0);
        motion.addDouble(0.0);
        nbt.put("Motion", motion);

        ListTag<FloatTag> rotation = new ListTag<FloatTag>(FloatTag.class);
        rotation.addFloat(0.0f);
        rotation.addFloat(0.0f);
        nbt.put("Rotation", rotation);

        nbt.putFloat("FallDistance", 0.0f);
        nbt.putShort("Fire", (short) -1);
        nbt.putShort("Air", (short) 300);
        nbt.putByte("OnGround", (byte) 0);
    }

    /**
     * Get the entity type identifier (e.g., "Pig", "Zombie", "Item").
     */
    public String getId() {
        return nbt.getString("id");
    }

    /**
     * Get the entity position as [x, y, z].
     */
    public double[] getPos() {
        ListTag<?> pos = nbt.getListTag("Pos");
        if (pos == null || pos.size() < 3) return new double[]{0, 0, 0};
        return new double[]{
            ((DoubleTag) pos.get(0)).asDouble(),
            ((DoubleTag) pos.get(1)).asDouble(),
            ((DoubleTag) pos.get(2)).asDouble()
        };
    }

    /**
     * Set the entity position.
     */
    public void setPos(double x, double y, double z) {
        ListTag<DoubleTag> pos = new ListTag<DoubleTag>(DoubleTag.class);
        pos.addDouble(x);
        pos.addDouble(y);
        pos.addDouble(z);
        nbt.put("Pos", pos);
    }

    /**
     * Get the entity velocity as [x, y, z].
     */
    public double[] getMotion() {
        ListTag<?> motion = nbt.getListTag("Motion");
        if (motion == null || motion.size() < 3) return new double[]{0, 0, 0};
        return new double[]{
            ((DoubleTag) motion.get(0)).asDouble(),
            ((DoubleTag) motion.get(1)).asDouble(),
            ((DoubleTag) motion.get(2)).asDouble()
        };
    }

    /**
     * Get the entity rotation as [yaw, pitch] in degrees.
     */
    public float[] getRotation() {
        ListTag<?> rot = nbt.getListTag("Rotation");
        if (rot == null || rot.size() < 2) return new float[]{0, 0};
        return new float[]{
            ((FloatTag) rot.get(0)).asFloat(),
            ((FloatTag) rot.get(1)).asFloat()
        };
    }

    /**
     * Check if this entity is on the ground.
     */
    public boolean isOnGround() {
        return nbt.getByte("OnGround") != 0;
    }

    /**
     * Get the raw NBT compound for serialization.
     */
    public CompoundTag toNbt() {
        return nbt;
    }
}
