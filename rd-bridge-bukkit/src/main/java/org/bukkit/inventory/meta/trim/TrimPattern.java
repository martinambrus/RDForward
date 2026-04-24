package org.bukkit.inventory.meta.trim;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TrimPattern extends org.bukkit.Keyed, org.bukkit.Translatable {
    public static final org.bukkit.inventory.meta.trim.TrimPattern BOLT = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern COAST = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern DUNE = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern EYE = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern FLOW = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern HOST = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern RAISER = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern RIB = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern SENTRY = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern SHAPER = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern SILENCE = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern SNOUT = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern SPIRE = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern TIDE = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern VEX = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern WARD = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern WAYFINDER = null;
    public static final org.bukkit.inventory.meta.trim.TrimPattern WILD = null;
    net.kyori.adventure.text.Component description();
    java.lang.String getTranslationKey();
    org.bukkit.NamespacedKey getKey();
    default net.kyori.adventure.key.Key key() {
        return null;
    }
}
