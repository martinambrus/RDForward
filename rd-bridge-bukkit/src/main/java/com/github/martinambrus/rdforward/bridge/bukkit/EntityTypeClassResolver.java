// @rdforward:preserve - hand-tuned, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps {@link org.bukkit.entity.EntityType} enum names to the matching
 * {@code org.bukkit.entity.<X>} interface. Real Bukkit's
 * {@code EntityType.getEntityClass()} returns this; Essentials's
 * {@code /spawnmob} feeds the result into {@code World.spawn(Location,
 * Class)} then checkcasts to {@code LivingEntity}, so a null/raw-Entity
 * fallback breaks the command end-to-end.
 *
 * <p>Lookup strategy: convert the {@code SNAKE_CASE} enum name to
 * {@code CamelCase} ({@code PIG} -> {@code Pig},
 * {@code IRON_GOLEM} -> {@code IronGolem},
 * {@code ZOMBIFIED_PIGLIN} -> {@code ZombifiedPiglin}) and resolve via
 * {@link Class#forName} against the bridge classloader. Results are
 * cached and a few special-cased aliases (where the modern enum name
 * doesn't match the interface name) are pre-registered.
 */
public final class EntityTypeClassResolver {

    private EntityTypeClassResolver() {}

    /** Pre-registered aliases for enum-to-interface mismatches. The key is
     *  the enum constant name; the value is the interface name (no
     *  package prefix). Empty string means "no Entity-shaped interface
     *  exists for this type — return null". */
    private static final Map<String, String> ALIASES = new HashMap<>();
    static {
        // Legacy CB-1.x aliases (exposed as enum constants for symbolic
        // resolution) — point them at their modern interface so the
        // spawn pipeline behaves equivalently.
        // Modern enum names -> legacy interface names. The bridge ships
        // PigZombie/MushroomCow/Snowman as the entity interfaces (the
        // modern Mooshroom/ZombifiedPiglin/SnowGolem types have not been
        // added as separate stubs); both the modern and legacy enum
        // entries point to the same backing interface.
        ALIASES.put("ZOMBIFIED_PIGLIN", "PigZombie");
        ALIASES.put("MOOSHROOM", "MushroomCow");
        ALIASES.put("SNOW_GOLEM", "Snowman");
        ALIASES.put("PIG_ZOMBIE", "PigZombie");
        ALIASES.put("MUSHROOM_COW", "MushroomCow");
        ALIASES.put("SNOWMAN", "Snowman");
        // Modern enum names whose interface differs from a naive
        // CamelCase conversion. Only entries we've actually seen
        // referenced by plugins are listed.
        ALIASES.put("LIGHTNING", "LightningStrike");
        ALIASES.put("LIGHTNING_BOLT", "LightningStrike");
        ALIASES.put("FIREWORK_ROCKET", "Firework");
        ALIASES.put("PRIMED_TNT", "TNTPrimed");
        ALIASES.put("TNT", "TNTPrimed");
        ALIASES.put("DROPPED_ITEM", "Item");
        ALIASES.put("LEASH_HITCH", "LeashHitch");
        ALIASES.put("ENDER_CRYSTAL", "EnderCrystal");
        ALIASES.put("END_CRYSTAL", "EnderCrystal");
        // Non-Entity types (unspawnable / non-living) — empty marker
        // so we don't waste reflection on guaranteed misses.
        ALIASES.put("UNKNOWN", "");
    }

    private static final ConcurrentHashMap<String, Class<?>> CACHE = new ConcurrentHashMap<>();
    private static final Class<?> SENTINEL_NONE = NoneMarker.class;
    private interface NoneMarker {}

    public static Class<?> resolve(String enumName) {
        if (enumName == null || enumName.isEmpty()) return null;
        Class<?> cached = CACHE.get(enumName);
        if (cached == SENTINEL_NONE) return null;
        if (cached != null) return cached;
        Class<?> resolved = doResolve(enumName);
        CACHE.put(enumName, resolved == null ? SENTINEL_NONE : resolved);
        return resolved;
    }

    private static Class<?> doResolve(String enumName) {
        String iface = ALIASES.get(enumName);
        if (iface != null && iface.isEmpty()) return null;
        if (iface == null) iface = toCamelCase(enumName);
        try {
            Class<?> c = Class.forName("org.bukkit.entity." + iface,
                    false, EntityTypeClassResolver.class.getClassLoader());
            // Only return classes that actually inherit from Entity —
            // a stray match against a non-Entity type would push the
            // spawn pipeline into an unrelated interface chain.
            if (Entity.class.isAssignableFrom(c)) return c;
        } catch (ClassNotFoundException ignored) {}
        return null;
    }

    /** {@code "PIG"} -> {@code "Pig"};
     *  {@code "IRON_GOLEM"} -> {@code "IronGolem"};
     *  {@code "ZOMBIFIED_PIGLIN"} -> {@code "ZombifiedPiglin"}. */
    private static String toCamelCase(String snake) {
        StringBuilder out = new StringBuilder(snake.length());
        boolean upperNext = true;
        for (int i = 0; i < snake.length(); i++) {
            char c = snake.charAt(i);
            if (c == '_') { upperNext = true; continue; }
            if (upperNext) {
                out.append(Character.toUpperCase(c));
                upperNext = false;
            } else {
                out.append(Character.toLowerCase(c));
            }
        }
        return out.toString();
    }
}
