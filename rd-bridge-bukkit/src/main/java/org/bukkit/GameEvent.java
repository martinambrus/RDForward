package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class GameEvent implements org.bukkit.Keyed {
    public static final org.bukkit.GameEvent BLOCK_ACTIVATE = null;
    public static final org.bukkit.GameEvent BLOCK_ATTACH = null;
    public static final org.bukkit.GameEvent BLOCK_CHANGE = null;
    public static final org.bukkit.GameEvent BLOCK_CLOSE = null;
    public static final org.bukkit.GameEvent BLOCK_DEACTIVATE = null;
    public static final org.bukkit.GameEvent BLOCK_DESTROY = null;
    public static final org.bukkit.GameEvent BLOCK_DETACH = null;
    public static final org.bukkit.GameEvent BLOCK_OPEN = null;
    public static final org.bukkit.GameEvent BLOCK_PLACE = null;
    public static final org.bukkit.GameEvent CONTAINER_CLOSE = null;
    public static final org.bukkit.GameEvent CONTAINER_OPEN = null;
    public static final org.bukkit.GameEvent DRINK = null;
    public static final org.bukkit.GameEvent EAT = null;
    public static final org.bukkit.GameEvent ELYTRA_GLIDE = null;
    public static final org.bukkit.GameEvent ENTITY_ACTION = null;
    public static final org.bukkit.GameEvent ENTITY_DAMAGE = null;
    public static final org.bukkit.GameEvent ENTITY_DIE = null;
    public static final org.bukkit.GameEvent ENTITY_DISMOUNT = null;
    public static final org.bukkit.GameEvent ENTITY_INTERACT = null;
    public static final org.bukkit.GameEvent ENTITY_MOUNT = null;
    public static final org.bukkit.GameEvent ENTITY_PLACE = null;
    public static final org.bukkit.GameEvent EQUIP = null;
    public static final org.bukkit.GameEvent EXPLODE = null;
    public static final org.bukkit.GameEvent FLAP = null;
    public static final org.bukkit.GameEvent FLUID_PICKUP = null;
    public static final org.bukkit.GameEvent FLUID_PLACE = null;
    public static final org.bukkit.GameEvent HIT_GROUND = null;
    public static final org.bukkit.GameEvent INSTRUMENT_PLAY = null;
    public static final org.bukkit.GameEvent ITEM_INTERACT_FINISH = null;
    public static final org.bukkit.GameEvent ITEM_INTERACT_START = null;
    public static final org.bukkit.GameEvent JUKEBOX_PLAY = null;
    public static final org.bukkit.GameEvent JUKEBOX_STOP_PLAY = null;
    public static final org.bukkit.GameEvent LIGHTNING_STRIKE = null;
    public static final org.bukkit.GameEvent NOTE_BLOCK_PLAY = null;
    public static final org.bukkit.GameEvent PRIME_FUSE = null;
    public static final org.bukkit.GameEvent PROJECTILE_LAND = null;
    public static final org.bukkit.GameEvent PROJECTILE_SHOOT = null;
    public static final org.bukkit.GameEvent RESONATE_1 = null;
    public static final org.bukkit.GameEvent RESONATE_2 = null;
    public static final org.bukkit.GameEvent RESONATE_3 = null;
    public static final org.bukkit.GameEvent RESONATE_4 = null;
    public static final org.bukkit.GameEvent RESONATE_5 = null;
    public static final org.bukkit.GameEvent RESONATE_6 = null;
    public static final org.bukkit.GameEvent RESONATE_7 = null;
    public static final org.bukkit.GameEvent RESONATE_8 = null;
    public static final org.bukkit.GameEvent RESONATE_9 = null;
    public static final org.bukkit.GameEvent RESONATE_10 = null;
    public static final org.bukkit.GameEvent RESONATE_11 = null;
    public static final org.bukkit.GameEvent RESONATE_12 = null;
    public static final org.bukkit.GameEvent RESONATE_13 = null;
    public static final org.bukkit.GameEvent RESONATE_14 = null;
    public static final org.bukkit.GameEvent RESONATE_15 = null;
    public static final org.bukkit.GameEvent SCULK_SENSOR_TENDRILS_CLICKING = null;
    public static final org.bukkit.GameEvent SHEAR = null;
    public static final org.bukkit.GameEvent SHRIEK = null;
    public static final org.bukkit.GameEvent SPLASH = null;
    public static final org.bukkit.GameEvent STEP = null;
    public static final org.bukkit.GameEvent SWIM = null;
    public static final org.bukkit.GameEvent TELEPORT = null;
    public static final org.bukkit.GameEvent UNEQUIP = null;
    public static final org.bukkit.GameEvent BLOCK_PRESS = null;
    public static final org.bukkit.GameEvent BLOCK_SWITCH = null;
    public static final org.bukkit.GameEvent BLOCK_UNPRESS = null;
    public static final org.bukkit.GameEvent BLOCK_UNSWITCH = null;
    public static final org.bukkit.GameEvent DISPENSE_FAIL = null;
    public static final org.bukkit.GameEvent DRINKING_FINISH = null;
    public static final org.bukkit.GameEvent ELYTRA_FREE_FALL = null;
    public static final org.bukkit.GameEvent ENTITY_DAMAGED = null;
    public static final org.bukkit.GameEvent ENTITY_DYING = null;
    public static final org.bukkit.GameEvent ENTITY_KILLED = null;
    public static final org.bukkit.GameEvent ENTITY_ROAR = null;
    public static final org.bukkit.GameEvent ENTITY_SHAKE = null;
    public static final org.bukkit.GameEvent MOB_INTERACT = null;
    public static final org.bukkit.GameEvent PISTON_CONTRACT = null;
    public static final org.bukkit.GameEvent PISTON_EXTEND = null;
    public static final org.bukkit.GameEvent RAVAGER_ROAR = null;
    public static final org.bukkit.GameEvent RING_BELL = null;
    public static final org.bukkit.GameEvent SHULKER_CLOSE = null;
    public static final org.bukkit.GameEvent SHULKER_OPEN = null;
    public static final org.bukkit.GameEvent WOLF_SHAKING = null;
    public GameEvent() {}
    public static org.bukkit.GameEvent getByKey(org.bukkit.NamespacedKey arg0) {
        return null;
    }
    public static java.util.Collection values() {
        return java.util.Collections.emptyList();
    }
    public abstract int getRange();
    public abstract int getVibrationLevel();
}
