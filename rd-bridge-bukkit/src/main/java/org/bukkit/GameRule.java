package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class GameRule implements net.kyori.adventure.translation.Translatable, io.papermc.paper.world.flag.FeatureDependant, org.bukkit.Keyed {
    public static final org.bukkit.GameRule ANNOUNCE_ADVANCEMENTS = null;
    public static final org.bukkit.GameRule COMMAND_BLOCK_OUTPUT = null;
    public static final org.bukkit.GameRule DISABLE_PLAYER_MOVEMENT_CHECK = null;
    public static final org.bukkit.GameRule DISABLE_ELYTRA_MOVEMENT_CHECK = null;
    public static final org.bukkit.GameRule DO_DAYLIGHT_CYCLE = null;
    public static final org.bukkit.GameRule DO_ENTITY_DROPS = null;
    public static final org.bukkit.GameRule DO_FIRE_TICK = null;
    public static final org.bukkit.GameRule DO_LIMITED_CRAFTING = null;
    public static final org.bukkit.GameRule PROJECTILES_CAN_BREAK_BLOCKS = null;
    public static final org.bukkit.GameRule DO_MOB_LOOT = null;
    public static final org.bukkit.GameRule DO_MOB_SPAWNING = null;
    public static final org.bukkit.GameRule DO_TILE_DROPS = null;
    public static final org.bukkit.GameRule DO_WEATHER_CYCLE = null;
    public static final org.bukkit.GameRule KEEP_INVENTORY = null;
    public static final org.bukkit.GameRule LOG_ADMIN_COMMANDS = null;
    public static final org.bukkit.GameRule MOB_GRIEFING = null;
    public static final org.bukkit.GameRule NATURAL_REGENERATION = null;
    public static final org.bukkit.GameRule REDUCED_DEBUG_INFO = null;
    public static final org.bukkit.GameRule SEND_COMMAND_FEEDBACK = null;
    public static final org.bukkit.GameRule SHOW_DEATH_MESSAGES = null;
    public static final org.bukkit.GameRule SPECTATORS_GENERATE_CHUNKS = null;
    public static final org.bukkit.GameRule DISABLE_RAIDS = null;
    public static final org.bukkit.GameRule DO_INSOMNIA = null;
    public static final org.bukkit.GameRule DO_IMMEDIATE_RESPAWN = null;
    public static final org.bukkit.GameRule DROWNING_DAMAGE = null;
    public static final org.bukkit.GameRule FALL_DAMAGE = null;
    public static final org.bukkit.GameRule FIRE_DAMAGE = null;
    public static final org.bukkit.GameRule FREEZE_DAMAGE = null;
    public static final org.bukkit.GameRule DO_PATROL_SPAWNING = null;
    public static final org.bukkit.GameRule DO_TRADER_SPAWNING = null;
    public static final org.bukkit.GameRule DO_WARDEN_SPAWNING = null;
    public static final org.bukkit.GameRule FORGIVE_DEAD_PLAYERS = null;
    public static final org.bukkit.GameRule UNIVERSAL_ANGER = null;
    public static final org.bukkit.GameRule BLOCK_EXPLOSION_DROP_DECAY = null;
    public static final org.bukkit.GameRule MOB_EXPLOSION_DROP_DECAY = null;
    public static final org.bukkit.GameRule TNT_EXPLOSION_DROP_DECAY = null;
    public static final org.bukkit.GameRule WATER_SOURCE_CONVERSION = null;
    public static final org.bukkit.GameRule LAVA_SOURCE_CONVERSION = null;
    public static final org.bukkit.GameRule GLOBAL_SOUND_EVENTS = null;
    public static final org.bukkit.GameRule DO_VINES_SPREAD = null;
    public static final org.bukkit.GameRule ENDER_PEARLS_VANISH_ON_DEATH = null;
    public static final org.bukkit.GameRule ALLOW_FIRE_TICKS_AWAY_FROM_PLAYER = null;
    public static final org.bukkit.GameRule TNT_EXPLODES = null;
    public static final org.bukkit.GameRule LOCATOR_BAR = null;
    public static final org.bukkit.GameRule PVP = null;
    public static final org.bukkit.GameRule SPAWN_MONSTERS = null;
    public static final org.bukkit.GameRule ALLOW_ENTERING_NETHER_USING_PORTALS = null;
    public static final org.bukkit.GameRule COMMAND_BLOCKS_ENABLED = null;
    public static final org.bukkit.GameRule SPAWNER_BLOCKS_ENABLED = null;
    public static final org.bukkit.GameRule RANDOM_TICK_SPEED = null;
    public static final org.bukkit.GameRule SPAWN_RADIUS = null;
    public static final org.bukkit.GameRule MAX_ENTITY_CRAMMING = null;
    public static final org.bukkit.GameRule MAX_COMMAND_CHAIN_LENGTH = null;
    public static final org.bukkit.GameRule MAX_COMMAND_FORK_COUNT = null;
    public static final org.bukkit.GameRule COMMAND_MODIFICATION_BLOCK_LIMIT = null;
    public static final org.bukkit.GameRule PLAYERS_SLEEPING_PERCENTAGE = null;
    public static final org.bukkit.GameRule SNOW_ACCUMULATION_HEIGHT = null;
    public static final org.bukkit.GameRule PLAYERS_NETHER_PORTAL_DEFAULT_DELAY = null;
    public static final org.bukkit.GameRule PLAYERS_NETHER_PORTAL_CREATIVE_DELAY = null;
    public static final org.bukkit.GameRule MINECART_MAX_SPEED = null;
    public GameRule() {}
    public abstract java.lang.String getName();
    public abstract java.lang.Class getType();
    public abstract java.lang.Object getDefaultValue();
    public static org.bukkit.GameRule getByName(java.lang.String arg0) {
        return null;
    }
    public static org.bukkit.GameRule[] values() {
        return new org.bukkit.GameRule[0];
    }
    public abstract java.lang.String translationKey();
}
