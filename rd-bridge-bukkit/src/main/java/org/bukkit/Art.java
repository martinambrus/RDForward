package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Art extends org.bukkit.util.OldEnum, org.bukkit.Keyed {
    public static final org.bukkit.Art ALBAN = null;
    public static final org.bukkit.Art AZTEC = null;
    public static final org.bukkit.Art AZTEC2 = null;
    public static final org.bukkit.Art BACKYARD = null;
    public static final org.bukkit.Art BAROQUE = null;
    public static final org.bukkit.Art BOMB = null;
    public static final org.bukkit.Art BOUQUET = null;
    public static final org.bukkit.Art BURNING_SKULL = null;
    public static final org.bukkit.Art BUST = null;
    public static final org.bukkit.Art CAVEBIRD = null;
    public static final org.bukkit.Art CHANGING = null;
    public static final org.bukkit.Art COTAN = null;
    public static final org.bukkit.Art COURBET = null;
    public static final org.bukkit.Art CREEBET = null;
    public static final org.bukkit.Art DENNIS = null;
    public static final org.bukkit.Art DONKEY_KONG = null;
    public static final org.bukkit.Art EARTH = null;
    public static final org.bukkit.Art ENDBOSS = null;
    public static final org.bukkit.Art FERN = null;
    public static final org.bukkit.Art FIGHTERS = null;
    public static final org.bukkit.Art FINDING = null;
    public static final org.bukkit.Art FIRE = null;
    public static final org.bukkit.Art GRAHAM = null;
    public static final org.bukkit.Art HUMBLE = null;
    public static final org.bukkit.Art KEBAB = null;
    public static final org.bukkit.Art LOWMIST = null;
    public static final org.bukkit.Art MATCH = null;
    public static final org.bukkit.Art MEDITATIVE = null;
    public static final org.bukkit.Art ORB = null;
    public static final org.bukkit.Art OWLEMONS = null;
    public static final org.bukkit.Art PASSAGE = null;
    public static final org.bukkit.Art PIGSCENE = null;
    public static final org.bukkit.Art PLANT = null;
    public static final org.bukkit.Art POINTER = null;
    public static final org.bukkit.Art POND = null;
    public static final org.bukkit.Art POOL = null;
    public static final org.bukkit.Art PRAIRIE_RIDE = null;
    public static final org.bukkit.Art SEA = null;
    public static final org.bukkit.Art SKELETON = null;
    public static final org.bukkit.Art SKULL_AND_ROSES = null;
    public static final org.bukkit.Art STAGE = null;
    public static final org.bukkit.Art SUNFLOWERS = null;
    public static final org.bukkit.Art SUNSET = null;
    public static final org.bukkit.Art TIDES = null;
    public static final org.bukkit.Art UNPACKED = null;
    public static final org.bukkit.Art VOID = null;
    public static final org.bukkit.Art WANDERER = null;
    public static final org.bukkit.Art WASTELAND = null;
    public static final org.bukkit.Art WATER = null;
    public static final org.bukkit.Art WIND = null;
    public static final org.bukkit.Art WITHER = null;
    int getBlockWidth();
    int getBlockHeight();
    int getId();
    org.bukkit.NamespacedKey getKey();
    default net.kyori.adventure.key.Key key() {
        return null;
    }
    net.kyori.adventure.text.Component title();
    net.kyori.adventure.text.Component author();
    net.kyori.adventure.key.Key assetId();
    static org.bukkit.Art getById(int arg0) {
        return null;
    }
    static org.bukkit.Art getByName(java.lang.String arg0) {
        return null;
    }
    static org.bukkit.Art valueOf(java.lang.String arg0) {
        return null;
    }
    static org.bukkit.Art[] values() {
        return new org.bukkit.Art[0];
    }
}
