// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event;

/**
 * Pre-Bukkit-1.x {@code Event.Type} enum. Removed from Bukkit years
 * ago (replaced by per-event {@code HandlerList} +
 * {@code @EventHandler}) but legacy plugins (LogBlockQuestioner 0.02,
 * OpenWarp v1.1) still embed these constants in their bytecode
 * constant pool.
 *
 * <p>Constants are linkage-only: RDForward's bridge does not route
 * the legacy enum form into actual event dispatch (see
 * {@link org.bukkit.plugin.PluginManager#registerEvent}), so plugins
 * that subscribe via this enum load cleanly but their callbacks stay
 * dormant. The set mirrors the pre-1.x Bukkit enum for forward
 * compatibility with other ancient plugins; missing names can be
 * added without behavioural risk.
 */
@SuppressWarnings({"unused"})
public enum Event$Type {
    // Player events
    PLAYER_JOIN, PLAYER_QUIT, PLAYER_KICK, PLAYER_CHAT, PLAYER_COMMAND_PREPROCESS,
    PLAYER_MOVE, PLAYER_TELEPORT, PLAYER_INTERACT, PLAYER_INTERACT_ENTITY,
    PLAYER_LOGIN, PLAYER_PRELOGIN, PLAYER_RESPAWN, PLAYER_ITEM_HELD,
    PLAYER_DROP_ITEM, PLAYER_PICKUP_ITEM, PLAYER_TOGGLE_SNEAK, PLAYER_TOGGLE_SPRINT,
    PLAYER_BED_ENTER, PLAYER_BED_LEAVE, PLAYER_BUCKET_EMPTY, PLAYER_BUCKET_FILL,
    PLAYER_VELOCITY, PLAYER_EGG_THROW, PLAYER_ANIMATION, PLAYER_FISH,
    PLAYER_INVENTORY, PLAYER_LIST_NAME, PLAYER_PORTAL, PLAYER_GAME_MODE_CHANGE,
    PLAYER_CHANGED_WORLD,

    // Block events
    BLOCK_PLACE, BLOCK_BREAK, BLOCK_DAMAGE, BLOCK_BURN, BLOCK_PHYSICS,
    BLOCK_FROMTO, BLOCK_IGNITE, BLOCK_FORM, BLOCK_FADE, BLOCK_GROW,
    BLOCK_SPREAD, BLOCK_REDSTONE, BLOCK_DISPENSE, BLOCK_PISTON_EXTEND,
    BLOCK_PISTON_RETRACT, LEAVES_DECAY, REDSTONE_CHANGE, SIGN_CHANGE,
    SNOW_FORM,

    // Entity events
    ENTITY_DEATH, ENTITY_DAMAGE, ENTITY_TARGET, ENTITY_INTERACT,
    ENTITY_REGAIN_HEALTH, ENTITY_EXPLODE, ENTITY_SHOOT_BOW, ENTITY_TELEPORT,
    ENTITY_COMBUST, ENTITY_TAME, ENTITY_PORTAL_ENTER, EXPLOSION_PRIME,
    CREATURE_SPAWN, ITEM_DESPAWN, ITEM_SPAWN, PROJECTILE_HIT,
    FOOD_LEVEL_CHANGE, PIG_ZAP, SHEEP_DYE_WOOL, SHEEP_REGROW_WOOL,
    SLIME_SPLIT,

    // World events
    WORLD_SAVE, WORLD_LOAD, WORLD_UNLOAD, WORLD_INIT, CHUNK_LOAD,
    CHUNK_UNLOAD, CHUNK_POPULATED, SPAWN_CHANGE, PORTAL_CREATE,
    STRUCTURE_GROW,

    // Inventory events
    INVENTORY_OPEN, INVENTORY_CLOSE, FURNACE_BURN, FURNACE_SMELT,

    // Vehicle events
    VEHICLE_CREATE, VEHICLE_DESTROY, VEHICLE_ENTER, VEHICLE_EXIT,
    VEHICLE_DAMAGE, VEHICLE_COLLISION_BLOCK, VEHICLE_COLLISION_ENTITY,
    VEHICLE_MOVE, VEHICLE_UPDATE,

    // Weather events
    WEATHER_CHANGE, THUNDER_CHANGE, LIGHTNING_STRIKE,

    // Server / plugin events
    SERVER_LIST_PING, SERVER_COMMAND, MAP_INITIALIZE,
    PLUGIN_ENABLE, PLUGIN_DISABLE, CUSTOM_EVENT
}
