package org.bukkit.enchantments;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class Enchantment implements org.bukkit.Keyed, org.bukkit.Translatable, net.kyori.adventure.translation.Translatable {
    public static final org.bukkit.enchantments.Enchantment PROTECTION = null;
    public static final org.bukkit.enchantments.Enchantment FIRE_PROTECTION = null;
    public static final org.bukkit.enchantments.Enchantment FEATHER_FALLING = null;
    public static final org.bukkit.enchantments.Enchantment BLAST_PROTECTION = null;
    public static final org.bukkit.enchantments.Enchantment PROJECTILE_PROTECTION = null;
    public static final org.bukkit.enchantments.Enchantment RESPIRATION = null;
    public static final org.bukkit.enchantments.Enchantment AQUA_AFFINITY = null;
    public static final org.bukkit.enchantments.Enchantment THORNS = null;
    public static final org.bukkit.enchantments.Enchantment DEPTH_STRIDER = null;
    public static final org.bukkit.enchantments.Enchantment FROST_WALKER = null;
    public static final org.bukkit.enchantments.Enchantment BINDING_CURSE = null;
    public static final org.bukkit.enchantments.Enchantment SHARPNESS = null;
    public static final org.bukkit.enchantments.Enchantment SMITE = null;
    public static final org.bukkit.enchantments.Enchantment BANE_OF_ARTHROPODS = null;
    public static final org.bukkit.enchantments.Enchantment KNOCKBACK = null;
    public static final org.bukkit.enchantments.Enchantment FIRE_ASPECT = null;
    public static final org.bukkit.enchantments.Enchantment LOOTING = null;
    public static final org.bukkit.enchantments.Enchantment SWEEPING_EDGE = null;
    public static final org.bukkit.enchantments.Enchantment EFFICIENCY = null;
    public static final org.bukkit.enchantments.Enchantment SILK_TOUCH = null;
    public static final org.bukkit.enchantments.Enchantment UNBREAKING = null;
    public static final org.bukkit.enchantments.Enchantment FORTUNE = null;
    public static final org.bukkit.enchantments.Enchantment POWER = null;
    public static final org.bukkit.enchantments.Enchantment PUNCH = null;
    public static final org.bukkit.enchantments.Enchantment FLAME = null;
    public static final org.bukkit.enchantments.Enchantment INFINITY = null;
    public static final org.bukkit.enchantments.Enchantment LUCK_OF_THE_SEA = null;
    public static final org.bukkit.enchantments.Enchantment LURE = null;
    public static final org.bukkit.enchantments.Enchantment LOYALTY = null;
    public static final org.bukkit.enchantments.Enchantment IMPALING = null;
    public static final org.bukkit.enchantments.Enchantment RIPTIDE = null;
    public static final org.bukkit.enchantments.Enchantment CHANNELING = null;
    public static final org.bukkit.enchantments.Enchantment MULTISHOT = null;
    public static final org.bukkit.enchantments.Enchantment QUICK_CHARGE = null;
    public static final org.bukkit.enchantments.Enchantment PIERCING = null;
    public static final org.bukkit.enchantments.Enchantment DENSITY = null;
    public static final org.bukkit.enchantments.Enchantment BREACH = null;
    public static final org.bukkit.enchantments.Enchantment WIND_BURST = null;
    public static final org.bukkit.enchantments.Enchantment MENDING = null;
    public static final org.bukkit.enchantments.Enchantment VANISHING_CURSE = null;
    public static final org.bukkit.enchantments.Enchantment SOUL_SPEED = null;
    public static final org.bukkit.enchantments.Enchantment SWIFT_SNEAK = null;
    public static final org.bukkit.enchantments.Enchantment LUNGE = null;
    public Enchantment() {}
    public abstract java.lang.String getName();
    public abstract int getMaxLevel();
    public abstract int getStartLevel();
    public abstract org.bukkit.enchantments.EnchantmentTarget getItemTarget();
    public abstract boolean isTreasure();
    public abstract boolean isCursed();
    public abstract boolean conflictsWith(org.bukkit.enchantments.Enchantment arg0);
    public abstract boolean canEnchantItem(org.bukkit.inventory.ItemStack arg0);
    public abstract net.kyori.adventure.text.Component displayName(int arg0);
    public abstract boolean isTradeable();
    public abstract boolean isDiscoverable();
    public abstract int getMinModifiedCost(int arg0);
    public abstract int getMaxModifiedCost(int arg0);
    public abstract int getAnvilCost();
    public abstract io.papermc.paper.enchantments.EnchantmentRarity getRarity();
    public abstract float getDamageIncrease(int arg0, org.bukkit.entity.EntityCategory arg1);
    public abstract float getDamageIncrease(int arg0, org.bukkit.entity.EntityType arg1);
    public java.util.Set getActiveSlots() {
        return java.util.Collections.emptySet();
    }
    public abstract java.util.Set getActiveSlotGroups();
    public abstract net.kyori.adventure.text.Component description();
    public abstract io.papermc.paper.registry.set.RegistryKeySet getSupportedItems();
    public abstract io.papermc.paper.registry.set.RegistryKeySet getPrimaryItems();
    public abstract int getWeight();
    public abstract io.papermc.paper.registry.set.RegistryKeySet getExclusiveWith();
    public abstract java.lang.String translationKey();
    public static org.bukkit.enchantments.Enchantment getByKey(org.bukkit.NamespacedKey arg0) {
        return null;
    }
    public static org.bukkit.enchantments.Enchantment getByName(java.lang.String arg0) {
        return null;
    }
    public static org.bukkit.enchantments.Enchantment[] values() {
        return new org.bukkit.enchantments.Enchantment[0];
    }
}
