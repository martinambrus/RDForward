// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit;

/**
 * Subset of Bukkit's {@code Material} enum covering the block types
 * RDForward surfaces. Entries beyond RDForward's block set map to
 * {@link #AIR} via {@link com.github.martinambrus.rdforward.bridge.bukkit.MaterialMapper}
 * at runtime. Plugins that switch on Material for unsupported blocks
 * receive AIR and silently noop on any related checks.
 */
public enum Material {
    AIR(0),
    STONE(1),
    GRASS_BLOCK(2),
    DIRT(3),
    COBBLESTONE(4),
    OAK_PLANKS(5),
    OAK_SAPLING(6),
    BEDROCK(7),
    WATER(8),
    LAVA(10),
    SAND(12),
    GRAVEL(13),
    GOLD_ORE(14),
    IRON_ORE(15),
    COAL_ORE(16),
    OAK_LOG(17),
    OAK_LEAVES(18),
    GLASS(20),
    TNT(46),
    /* ---- Pre-Flattening (1.7.x) aliases. Declared AFTER the canonical
     *  modern entries so {@link #getMaterial(int)} resolves shared
     *  legacyIds (e.g. id 6) to the modern name. Each alias exists so
     *  legacy plugins (Essentials 2.8.x's {@code Util.<clinit>}) can
     *  resolve them as static fields without {@link NoSuchFieldError}.
     *  None of these need to map to a real RDForward block — unmapped
     *  cases fall through to {@link
     *  com.github.martinambrus.rdforward.api.world.BlockTypes#AIR} via
     *  {@link com.github.martinambrus.rdforward.bridge.bukkit.MaterialMapper}.
     *  ---- */
    SAPLING(6),
    STATIONARY_WATER(9),
    STATIONARY_LAVA(11),
    BED(26),
    POWERED_RAIL(27),
    DETECTOR_RAIL(28),
    LONG_GRASS(31),
    DEAD_BUSH(32),
    YELLOW_FLOWER(37),
    RED_ROSE(38),
    BROWN_MUSHROOM(39),
    RED_MUSHROOM(40),
    STEP(44),
    TORCH(50),
    FIRE(51),
    REDSTONE_WIRE(55),
    SIGN_POST(63),
    WOODEN_DOOR(64),
    LADDER(65),
    RAILS(66),
    WALL_SIGN(68),
    LEVER(69),
    STONE_PLATE(70),
    IRON_DOOR_BLOCK(71),
    WOOD_PLATE(72),
    REDSTONE_TORCH_OFF(75),
    REDSTONE_TORCH_ON(76),
    STONE_BUTTON(77),
    SUGAR_CANE_BLOCK(83),
    CAKE(92),
    DIODE_BLOCK_OFF(93),
    DIODE_BLOCK_ON(94),
    TRAP_DOOR(96),
    PUMPKIN_STEM(104),
    MELON_STEM(105),
    VINE(106),
    WATER_LILY(111),
    NETHER_WARTS(115),
    SEEDS(295),
    SIGN(323),
    WOOD_DOOR(324),
    IRON_DOOR(330),
    REDSTONE(331),
    DIODE(356),
    PUMPKIN_SEEDS(361),
    MELON_SEEDS(362);

    private final int legacyId;
    Material(int legacyId) { this.legacyId = legacyId; }

    /** Pre-1.13 numeric block id. Real Bukkit's {@code Material} carried
     *  the same numeric id on every entry until the Flattening; legacy
     *  plugins (WorldEdit 5.6.1, EssentialsX pre-2.x, classic Bukkit
     *  command parsers) call {@link #getMaterial(int)} to resolve user
     *  input like {@code /set 4} → {@link #COBBLESTONE}. */
    public int getId() { return legacyId; }

    /** True if this material represents empty space. Mirrors upstream helper. */
    public boolean isAir() { return this == AIR; }

    /** True if this material is a solid block. Stub treats every non-AIR/WATER/LAVA as solid. */
    public boolean isSolid() { return this != AIR && this != WATER && this != LAVA; }

    /** True if this material is a block (placeable in a world).  All
     *  Material constants RDForward surfaces are blocks; we do not yet
     *  model item-only Materials. WorldEdit 5.6.1's {@code
     *  BukkitWorld.isValidBlockType} calls this after {@link
     *  #getMaterial(int)} to decide whether the user-provided id is
     *  actually placeable. */
    public boolean isBlock() { return true; }

    /** Resolve a Material by pre-Flattening numeric id. Returns
     *  {@code null} for ids outside our supported set so callers like
     *  WorldEdit's {@code /set <id>} parser can fail validation cleanly
     *  rather than placing AIR. WE 5.6.1's {@code
     *  BukkitWorld.isValidBlockType} explicitly checks
     *  {@code Material.getMaterial(id) != null && .isBlock()} — without
     *  this method, the call site throws {@link NoSuchMethodError} and
     *  the entire //set command bails. */
    public static Material getMaterial(int legacyId) {
        for (Material m : VALUES) {
            if (m.legacyId == legacyId) return m;
        }
        return null;
    }

    /** Resolve a Material by name. {@link #valueOf(String)} throws on
     *  miss; this lenient variant returns {@code null}, matching the
     *  upstream Bukkit signature plugins compile against. */
    public static Material getMaterial(String name) {
        if (name == null) return null;
        try { return Material.valueOf(name.toUpperCase(java.util.Locale.ROOT)); }
        catch (IllegalArgumentException e) { return null; }
    }

    /** Lenient name lookup: strips whitespace, replaces spaces and
     *  hyphens with underscores, drops a {@code "minecraft:"} prefix,
     *  uppercases. LogBlock 1.41's {@code Config} reads block names
     *  from YAML and feeds them through {@code Material.matchMaterial};
     *  without this method the plugin {@link NoSuchMethodError}s in
     *  {@code onLoad} and aborts the whole boot. Returns {@code null}
     *  on no match (same shape as {@link #getMaterial(String)}).
     *
     *  <p>Pre-1.13 Bukkit accepted numeric strings ({@code "0"} → AIR);
     *  LogBlock's stock {@code materials.yml} ships with such entries.
     *  Fall through to {@link #getMaterial(int)} when the trimmed input
     *  parses as an integer so the plugin doesn't log SEVERE on every
     *  load. */
    public static Material matchMaterial(String name) {
        if (name == null) return null;
        String key = name.trim();
        if (key.isEmpty()) return null;
        try {
            return getMaterial(Integer.parseInt(key));
        } catch (NumberFormatException ignored) {
            // not a numeric id — fall through to name lookup
        }
        if (key.regionMatches(true, 0, "minecraft:", 0, 10)) {
            key = key.substring(10);
        }
        key = key.replace(' ', '_').replace('-', '_').toUpperCase(java.util.Locale.ROOT);
        return getMaterial(key);
    }

    /** Bukkit 1.13+ overload; the second arg toggles the legacy
     *  pre-Flattening lookup. RDForward only models post-Flattening
     *  Materials, so the flag is ignored — both modes go through
     *  {@link #matchMaterial(String)}. */
    public static Material matchMaterial(String name, boolean legacyName) {
        return matchMaterial(name);
    }

    private static final Material[] VALUES = values();
}
