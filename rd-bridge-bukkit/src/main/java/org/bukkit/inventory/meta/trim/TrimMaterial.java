package org.bukkit.inventory.meta.trim;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TrimMaterial extends org.bukkit.Keyed, org.bukkit.Translatable {
    public static final org.bukkit.inventory.meta.trim.TrimMaterial AMETHYST = null;
    public static final org.bukkit.inventory.meta.trim.TrimMaterial COPPER = null;
    public static final org.bukkit.inventory.meta.trim.TrimMaterial DIAMOND = null;
    public static final org.bukkit.inventory.meta.trim.TrimMaterial EMERALD = null;
    public static final org.bukkit.inventory.meta.trim.TrimMaterial GOLD = null;
    public static final org.bukkit.inventory.meta.trim.TrimMaterial IRON = null;
    public static final org.bukkit.inventory.meta.trim.TrimMaterial LAPIS = null;
    public static final org.bukkit.inventory.meta.trim.TrimMaterial NETHERITE = null;
    public static final org.bukkit.inventory.meta.trim.TrimMaterial QUARTZ = null;
    public static final org.bukkit.inventory.meta.trim.TrimMaterial REDSTONE = null;
    public static final org.bukkit.inventory.meta.trim.TrimMaterial RESIN = null;
    net.kyori.adventure.text.Component description();
    java.lang.String getTranslationKey();
    org.bukkit.NamespacedKey getKey();
    default net.kyori.adventure.key.Key key() {
        return null;
    }
}
