package org.bukkit.potion;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class PotionEffectType implements org.bukkit.Keyed, org.bukkit.Translatable, net.kyori.adventure.translation.Translatable, io.papermc.paper.world.flag.FeatureDependant {
    public static final org.bukkit.potion.PotionEffectType SPEED = null;
    public static final org.bukkit.potion.PotionEffectType SLOWNESS = null;
    public static final org.bukkit.potion.PotionEffectType HASTE = null;
    public static final org.bukkit.potion.PotionEffectType MINING_FATIGUE = null;
    public static final org.bukkit.potion.PotionEffectType STRENGTH = null;
    public static final org.bukkit.potion.PotionEffectType INSTANT_HEALTH = null;
    public static final org.bukkit.potion.PotionEffectType INSTANT_DAMAGE = null;
    public static final org.bukkit.potion.PotionEffectType JUMP_BOOST = null;
    public static final org.bukkit.potion.PotionEffectType NAUSEA = null;
    public static final org.bukkit.potion.PotionEffectType REGENERATION = null;
    public static final org.bukkit.potion.PotionEffectType RESISTANCE = null;
    public static final org.bukkit.potion.PotionEffectType FIRE_RESISTANCE = null;
    public static final org.bukkit.potion.PotionEffectType WATER_BREATHING = null;
    public static final org.bukkit.potion.PotionEffectType INVISIBILITY = null;
    public static final org.bukkit.potion.PotionEffectType BLINDNESS = null;
    public static final org.bukkit.potion.PotionEffectType NIGHT_VISION = null;
    public static final org.bukkit.potion.PotionEffectType HUNGER = null;
    public static final org.bukkit.potion.PotionEffectType WEAKNESS = null;
    public static final org.bukkit.potion.PotionEffectType POISON = null;
    public static final org.bukkit.potion.PotionEffectType WITHER = null;
    public static final org.bukkit.potion.PotionEffectType HEALTH_BOOST = null;
    public static final org.bukkit.potion.PotionEffectType ABSORPTION = null;
    public static final org.bukkit.potion.PotionEffectType SATURATION = null;
    public static final org.bukkit.potion.PotionEffectType GLOWING = null;
    public static final org.bukkit.potion.PotionEffectType LEVITATION = null;
    public static final org.bukkit.potion.PotionEffectType LUCK = null;
    public static final org.bukkit.potion.PotionEffectType UNLUCK = null;
    public static final org.bukkit.potion.PotionEffectType SLOW_FALLING = null;
    public static final org.bukkit.potion.PotionEffectType CONDUIT_POWER = null;
    public static final org.bukkit.potion.PotionEffectType DOLPHINS_GRACE = null;
    public static final org.bukkit.potion.PotionEffectType BAD_OMEN = null;
    public static final org.bukkit.potion.PotionEffectType HERO_OF_THE_VILLAGE = null;
    public static final org.bukkit.potion.PotionEffectType DARKNESS = null;
    public static final org.bukkit.potion.PotionEffectType TRIAL_OMEN = null;
    public static final org.bukkit.potion.PotionEffectType RAID_OMEN = null;
    public static final org.bukkit.potion.PotionEffectType WIND_CHARGED = null;
    public static final org.bukkit.potion.PotionEffectType WEAVING = null;
    public static final org.bukkit.potion.PotionEffectType OOZING = null;
    public static final org.bukkit.potion.PotionEffectType INFESTED = null;
    public static final org.bukkit.potion.PotionEffectType BREATH_OF_THE_NAUTILUS = null;
    public PotionEffectType() {}
    public abstract org.bukkit.potion.PotionEffect createEffect(int arg0, int arg1);
    public abstract boolean isInstant();
    public abstract org.bukkit.potion.PotionEffectTypeCategory getCategory();
    public abstract org.bukkit.Color getColor();
    public abstract double getDurationModifier();
    public abstract int getId();
    public abstract java.lang.String getName();
    public static org.bukkit.potion.PotionEffectType getByKey(org.bukkit.NamespacedKey arg0) {
        return null;
    }
    public static org.bukkit.potion.PotionEffectType getById(int arg0) {
        return null;
    }
    public static org.bukkit.potion.PotionEffectType getByName(java.lang.String arg0) {
        return null;
    }
    public static org.bukkit.potion.PotionEffectType[] values() {
        return new org.bukkit.potion.PotionEffectType[0];
    }
    public abstract java.util.Map getEffectAttributes();
    public abstract double getAttributeModifierAmount(org.bukkit.attribute.Attribute arg0, int arg1);
    public abstract org.bukkit.potion.PotionEffectType$Category getEffectCategory();
}
