// @rdforward:preserve - hand-tuned: legacy effect aliases populated with concrete name-carrying stubs so Essentials Pre-2.14's Potions.<clinit> + getByName chain resolves
package org.bukkit.potion;

/**
 * Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar with
 * hand-tuned non-null constants. The generated form left every constant
 * as {@code null}, which made Essentials Pre-2.14's
 * {@code Potions.<clinit>} populate its lookup map with null values —
 * Commandpotion then NPE'd on {@code map.get(name).getName()}.
 *
 * <p>Each constant is now a concrete {@code Stub} instance carrying the
 * canonical effect name, so {@code getName()} is non-null. Pre-1.20.5
 * legacy aliases (SLOW, FAST_DIGGING, ...) point to the modern
 * equivalents — same instance, just a second binding so plugin code
 * that read e.g. {@code SLOW} stores a useful reference.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class PotionEffectType implements org.bukkit.Keyed, org.bukkit.Translatable, net.kyori.adventure.translation.Translatable, io.papermc.paper.world.flag.FeatureDependant {

    public static final org.bukkit.potion.PotionEffectType SPEED = new Stub("SPEED");
    public static final org.bukkit.potion.PotionEffectType SLOWNESS = new Stub("SLOWNESS");
    public static final org.bukkit.potion.PotionEffectType HASTE = new Stub("HASTE");
    public static final org.bukkit.potion.PotionEffectType MINING_FATIGUE = new Stub("MINING_FATIGUE");
    public static final org.bukkit.potion.PotionEffectType STRENGTH = new Stub("STRENGTH");
    public static final org.bukkit.potion.PotionEffectType INSTANT_HEALTH = new Stub("INSTANT_HEALTH");
    public static final org.bukkit.potion.PotionEffectType INSTANT_DAMAGE = new Stub("INSTANT_DAMAGE");
    public static final org.bukkit.potion.PotionEffectType JUMP_BOOST = new Stub("JUMP_BOOST");
    public static final org.bukkit.potion.PotionEffectType NAUSEA = new Stub("NAUSEA");
    public static final org.bukkit.potion.PotionEffectType REGENERATION = new Stub("REGENERATION");
    public static final org.bukkit.potion.PotionEffectType RESISTANCE = new Stub("RESISTANCE");
    public static final org.bukkit.potion.PotionEffectType FIRE_RESISTANCE = new Stub("FIRE_RESISTANCE");
    public static final org.bukkit.potion.PotionEffectType WATER_BREATHING = new Stub("WATER_BREATHING");
    public static final org.bukkit.potion.PotionEffectType INVISIBILITY = new Stub("INVISIBILITY");
    public static final org.bukkit.potion.PotionEffectType BLINDNESS = new Stub("BLINDNESS");
    public static final org.bukkit.potion.PotionEffectType NIGHT_VISION = new Stub("NIGHT_VISION");
    public static final org.bukkit.potion.PotionEffectType HUNGER = new Stub("HUNGER");
    public static final org.bukkit.potion.PotionEffectType WEAKNESS = new Stub("WEAKNESS");
    public static final org.bukkit.potion.PotionEffectType POISON = new Stub("POISON");
    public static final org.bukkit.potion.PotionEffectType WITHER = new Stub("WITHER");
    public static final org.bukkit.potion.PotionEffectType HEALTH_BOOST = new Stub("HEALTH_BOOST");
    public static final org.bukkit.potion.PotionEffectType ABSORPTION = new Stub("ABSORPTION");
    public static final org.bukkit.potion.PotionEffectType SATURATION = new Stub("SATURATION");
    public static final org.bukkit.potion.PotionEffectType GLOWING = new Stub("GLOWING");
    public static final org.bukkit.potion.PotionEffectType LEVITATION = new Stub("LEVITATION");
    public static final org.bukkit.potion.PotionEffectType LUCK = new Stub("LUCK");
    public static final org.bukkit.potion.PotionEffectType UNLUCK = new Stub("UNLUCK");
    public static final org.bukkit.potion.PotionEffectType SLOW_FALLING = new Stub("SLOW_FALLING");
    public static final org.bukkit.potion.PotionEffectType CONDUIT_POWER = new Stub("CONDUIT_POWER");
    public static final org.bukkit.potion.PotionEffectType DOLPHINS_GRACE = new Stub("DOLPHINS_GRACE");
    public static final org.bukkit.potion.PotionEffectType BAD_OMEN = new Stub("BAD_OMEN");
    public static final org.bukkit.potion.PotionEffectType HERO_OF_THE_VILLAGE = new Stub("HERO_OF_THE_VILLAGE");
    public static final org.bukkit.potion.PotionEffectType DARKNESS = new Stub("DARKNESS");
    public static final org.bukkit.potion.PotionEffectType TRIAL_OMEN = new Stub("TRIAL_OMEN");
    public static final org.bukkit.potion.PotionEffectType RAID_OMEN = new Stub("RAID_OMEN");
    public static final org.bukkit.potion.PotionEffectType WIND_CHARGED = new Stub("WIND_CHARGED");
    public static final org.bukkit.potion.PotionEffectType WEAVING = new Stub("WEAVING");
    public static final org.bukkit.potion.PotionEffectType OOZING = new Stub("OOZING");
    public static final org.bukkit.potion.PotionEffectType INFESTED = new Stub("INFESTED");
    public static final org.bukkit.potion.PotionEffectType BREATH_OF_THE_NAUTILUS = new Stub("BREATH_OF_THE_NAUTILUS");

    // Pre-1.20.5 aliases — same instance, second binding. Essentials
    // Pre-2.14 reads these into a name->effect map; pointing at the
    // modern equivalent means the map gets a non-null value and the
    // downstream effect.getName() call returns the modern canonical
    // name (e.g. "SLOWNESS" rather than null).
    public static final org.bukkit.potion.PotionEffectType SLOW = SLOWNESS;
    public static final org.bukkit.potion.PotionEffectType FAST_DIGGING = HASTE;
    public static final org.bukkit.potion.PotionEffectType SLOW_DIGGING = MINING_FATIGUE;
    public static final org.bukkit.potion.PotionEffectType INCREASE_DAMAGE = STRENGTH;
    public static final org.bukkit.potion.PotionEffectType HEAL = INSTANT_HEALTH;
    public static final org.bukkit.potion.PotionEffectType HARM = INSTANT_DAMAGE;
    public static final org.bukkit.potion.PotionEffectType JUMP = JUMP_BOOST;
    public static final org.bukkit.potion.PotionEffectType CONFUSION = NAUSEA;
    public static final org.bukkit.potion.PotionEffectType DAMAGE_RESISTANCE = RESISTANCE;

    /** Single name-keyed lookup for getByName / getByKey / values. */
    private static final java.util.Map<String, org.bukkit.potion.PotionEffectType> BY_NAME = new java.util.HashMap<>();
    static {
        org.bukkit.potion.PotionEffectType[] modern = {
            SPEED, SLOWNESS, HASTE, MINING_FATIGUE, STRENGTH, INSTANT_HEALTH,
            INSTANT_DAMAGE, JUMP_BOOST, NAUSEA, REGENERATION, RESISTANCE,
            FIRE_RESISTANCE, WATER_BREATHING, INVISIBILITY, BLINDNESS,
            NIGHT_VISION, HUNGER, WEAKNESS, POISON, WITHER, HEALTH_BOOST,
            ABSORPTION, SATURATION, GLOWING, LEVITATION, LUCK, UNLUCK,
            SLOW_FALLING, CONDUIT_POWER, DOLPHINS_GRACE, BAD_OMEN,
            HERO_OF_THE_VILLAGE, DARKNESS, TRIAL_OMEN, RAID_OMEN,
            WIND_CHARGED, WEAVING, OOZING, INFESTED, BREATH_OF_THE_NAUTILUS
        };
        for (org.bukkit.potion.PotionEffectType t : modern) {
            BY_NAME.put(t.getName().toUpperCase(java.util.Locale.ROOT), t);
        }
        // Legacy synonyms accepted by getByName(String).
        BY_NAME.put("SLOW", SLOWNESS);
        BY_NAME.put("FAST_DIGGING", HASTE);
        BY_NAME.put("SLOW_DIGGING", MINING_FATIGUE);
        BY_NAME.put("INCREASE_DAMAGE", STRENGTH);
        BY_NAME.put("HEAL", INSTANT_HEALTH);
        BY_NAME.put("HARM", INSTANT_DAMAGE);
        BY_NAME.put("JUMP", JUMP_BOOST);
        BY_NAME.put("CONFUSION", NAUSEA);
        BY_NAME.put("DAMAGE_RESISTANCE", RESISTANCE);
    }

    public PotionEffectType() {}

    public abstract org.bukkit.potion.PotionEffect createEffect(int arg0, int arg1);
    public abstract boolean isInstant();
    public abstract org.bukkit.potion.PotionEffectTypeCategory getCategory();
    public abstract org.bukkit.Color getColor();
    public abstract double getDurationModifier();
    public abstract int getId();
    public abstract java.lang.String getName();

    public static org.bukkit.potion.PotionEffectType getByKey(org.bukkit.NamespacedKey key) {
        if (key == null) return null;
        String k = key.getKey();
        return k == null ? null : BY_NAME.get(k.toUpperCase(java.util.Locale.ROOT));
    }
    public static org.bukkit.potion.PotionEffectType getById(int arg0) {
        return null;
    }
    public static org.bukkit.potion.PotionEffectType getByName(java.lang.String name) {
        if (name == null) return null;
        return BY_NAME.get(name.toUpperCase(java.util.Locale.ROOT));
    }
    public static org.bukkit.potion.PotionEffectType[] values() {
        return BY_NAME.values().toArray(new org.bukkit.potion.PotionEffectType[0]);
    }
    public abstract java.util.Map getEffectAttributes();
    public abstract double getAttributeModifierAmount(org.bukkit.attribute.Attribute arg0, int arg1);
    public abstract org.bukkit.potion.PotionEffectType$Category getEffectCategory();

    /** Concrete backing for the static constants. Stores only the
     *  canonical name; every other accessor returns a safe default
     *  (no color, no attributes, instant=false). */
    private static final class Stub extends PotionEffectType {
        private final String storedName;
        Stub(String name) { this.storedName = name; }
        @Override public org.bukkit.potion.PotionEffect createEffect(int duration, int amplifier) { return null; }
        @Override public boolean isInstant() { return false; }
        @Override public org.bukkit.potion.PotionEffectTypeCategory getCategory() { return null; }
        @Override public org.bukkit.Color getColor() { return null; }
        @Override public double getDurationModifier() { return 1.0d; }
        @Override public int getId() { return 0; }
        @Override public String getName() { return storedName; }
        @Override public java.util.Map getEffectAttributes() { return java.util.Collections.emptyMap(); }
        @Override public double getAttributeModifierAmount(org.bukkit.attribute.Attribute a, int level) { return 0.0d; }
        @Override public org.bukkit.potion.PotionEffectType$Category getEffectCategory() { return null; }
        @Override public org.bukkit.NamespacedKey getKey() {
            return new org.bukkit.NamespacedKey(
                    org.bukkit.NamespacedKey.MINECRAFT,
                    storedName.toLowerCase(java.util.Locale.ROOT));
        }
        @Override public net.kyori.adventure.key.Key key() { return null; }
        @Override public String getTranslationKey() {
            return "effect.minecraft." + storedName.toLowerCase(java.util.Locale.ROOT);
        }
        @Override public String translationKey() { return getTranslationKey(); }
    }
}
