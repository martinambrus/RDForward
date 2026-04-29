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
    MELON_SEEDS(362),
    /* ---- Modern (post-Flattening) Material entries used by third-party
     *  plugins (CoreProtect 23.1, etc.). legacyId -1 because these don't
     *  map to a pre-1.13 numeric id. Values exist as static fields so
     *  plugin <clinit> blocks resolve them; runtime semantics are the
     *  Hybrid Stub Contract — Material lookups against rd-server's block
     *  set fall through to AIR via MaterialMapper. ---- */
    ACACIA_BUTTON(-1),
    ACACIA_DOOR(-1),
    ACACIA_FENCE_GATE(-1),
    ACACIA_PRESSURE_PLATE(-1),
    ACACIA_SAPLING(-1),
    ACACIA_SIGN(-1),
    ACACIA_TRAPDOOR(-1),
    ACACIA_WALL_SIGN(-1),
    ACTIVATOR_RAIL(-1),
    ALLIUM(-1),
    AMETHYST_CLUSTER(-1),
    ANVIL(-1),
    ARMOR_STAND(-1),
    ARROW(-1),
    AZALEA(-1),
    AZURE_BLUET(-1),
    BAMBOO(-1),
    BAMBOO_SAPLING(-1),
    BARREL(-1),
    BARRIER(-1),
    BEACON(-1),
    BEETROOTS(-1),
    BEETROOT_SEEDS(-1),
    BELL(-1),
    BIG_DRIPLEAF(-1),
    BIG_DRIPLEAF_STEM(-1),
    BIRCH_BUTTON(-1),
    BIRCH_DOOR(-1),
    BIRCH_FENCE_GATE(-1),
    BIRCH_PRESSURE_PLATE(-1),
    BIRCH_SAPLING(-1),
    BIRCH_SIGN(-1),
    BIRCH_TRAPDOOR(-1),
    BIRCH_WALL_SIGN(-1),
    BLACK_BANNER(-1),
    BLACK_BED(-1),
    BLACK_CANDLE(-1),
    BLACK_CANDLE_CAKE(-1),
    BLACK_DYE(-1),
    BLACK_SHULKER_BOX(-1),
    BLACK_WALL_BANNER(-1),
    BLACK_WOOL(-1),
    BLAST_FURNACE(-1),
    BLUE_BANNER(-1),
    BLUE_BED(-1),
    BLUE_CANDLE(-1),
    BLUE_CANDLE_CAKE(-1),
    BLUE_DYE(-1),
    BLUE_ORCHID(-1),
    BLUE_SHULKER_BOX(-1),
    BLUE_WALL_BANNER(-1),
    BLUE_WOOL(-1),
    BONE_MEAL(-1),
    BOW(-1),
    BREWING_STAND(-1),
    BROWN_BANNER(-1),
    BROWN_BED(-1),
    BROWN_CANDLE(-1),
    BROWN_CANDLE_CAKE(-1),
    BROWN_DYE(-1),
    BROWN_SHULKER_BOX(-1),
    BROWN_WALL_BANNER(-1),
    BROWN_WOOL(-1),
    BRUSH(-1),
    BUCKET(-1),
    BUNDLE(-1),
    CACTUS(-1),
    CAMPFIRE(-1),
    CANDLE(-1),
    CANDLE_CAKE(-1),
    CARROT(-1),
    CARTOGRAPHY_TABLE(-1),
    CAVE_AIR(-1),
    CAVE_VINES(-1),
    CAVE_VINES_PLANT(-1),
    CHAIN_COMMAND_BLOCK(-1),
    CHEST(-1),
    CHIPPED_ANVIL(-1),
    CHISELED_BOOKSHELF(-1),
    CHORUS_FLOWER(-1),
    CHORUS_PLANT(-1),
    CLAY(-1),
    COBWEB(-1),
    COCOA(-1),
    COMMAND_BLOCK(-1),
    COMPARATOR(-1),
    CORNFLOWER(-1),
    CRAFTER(-1),
    CRAFTING_TABLE(-1),
    CREEPER_HEAD(-1),
    CREEPER_WALL_HEAD(-1),
    CRIMSON_BUTTON(-1),
    CRIMSON_DOOR(-1),
    CRIMSON_FENCE_GATE(-1),
    CRIMSON_FUNGUS(-1),
    CRIMSON_PRESSURE_PLATE(-1),
    CRIMSON_ROOTS(-1),
    CRIMSON_SIGN(-1),
    CRIMSON_TRAPDOOR(-1),
    CRIMSON_WALL_SIGN(-1),
    CROSSBOW(-1),
    CRYING_OBSIDIAN(-1),
    CYAN_BANNER(-1),
    CYAN_BED(-1),
    CYAN_CANDLE(-1),
    CYAN_CANDLE_CAKE(-1),
    CYAN_DYE(-1),
    CYAN_SHULKER_BOX(-1),
    CYAN_WALL_BANNER(-1),
    CYAN_WOOL(-1),
    DAMAGED_ANVIL(-1),
    DANDELION(-1),
    DARK_OAK_BUTTON(-1),
    DARK_OAK_DOOR(-1),
    DARK_OAK_FENCE_GATE(-1),
    DARK_OAK_PRESSURE_PLATE(-1),
    DARK_OAK_SAPLING(-1),
    DARK_OAK_SIGN(-1),
    DARK_OAK_TRAPDOOR(-1),
    DARK_OAK_WALL_SIGN(-1),
    DAYLIGHT_DETECTOR(-1),
    DECORATED_POT(-1),
    DIAMOND_ORE(-1),
    DIRT_PATH(-1),
    DISPENSER(-1),
    DRAGON_EGG(-1),
    DRAGON_HEAD(-1),
    DRAGON_WALL_HEAD(-1),
    DROPPER(-1),
    EGG(-1),
    EMERALD_ORE(-1),
    ENCHANTING_TABLE(-1),
    ENDER_CHEST(-1),
    ENDER_PEARL(-1),
    END_CRYSTAL(-1),
    END_STONE(-1),
    EXPERIENCE_BOTTLE(-1),
    FARMLAND(-1),
    FERN(-1),
    FILLED_MAP(-1),
    FIREWORK_ROCKET(-1),
    FIREWORK_STAR(-1),
    FIRE_CHARGE(-1),
    FLINT_AND_STEEL(-1),
    FLOWERING_AZALEA(-1),
    FURNACE(-1),
    GLOWSTONE(-1),
    GLOW_ITEM_FRAME(-1),
    GLOW_LICHEN(-1),
    GRAY_BANNER(-1),
    GRAY_BED(-1),
    GRAY_CANDLE(-1),
    GRAY_CANDLE_CAKE(-1),
    GRAY_DYE(-1),
    GRAY_SHULKER_BOX(-1),
    GRAY_WALL_BANNER(-1),
    GRAY_WOOL(-1),
    GREEN_BANNER(-1),
    GREEN_BED(-1),
    GREEN_CANDLE(-1),
    GREEN_CANDLE_CAKE(-1),
    GREEN_DYE(-1),
    GREEN_SHULKER_BOX(-1),
    GREEN_WALL_BANNER(-1),
    GREEN_WOOL(-1),
    GRINDSTONE(-1),
    HANGING_ROOTS(-1),
    HEAVY_WEIGHTED_PRESSURE_PLATE(-1),
    HONEYCOMB(-1),
    HOPPER(-1),
    ICE(-1),
    IRON_TRAPDOOR(-1),
    ITEM_FRAME(-1),
    JACK_O_LANTERN(-1),
    JUKEBOX(-1),
    JUNGLE_BUTTON(-1),
    JUNGLE_DOOR(-1),
    JUNGLE_FENCE_GATE(-1),
    JUNGLE_PRESSURE_PLATE(-1),
    JUNGLE_SAPLING(-1),
    JUNGLE_SIGN(-1),
    JUNGLE_TRAPDOOR(-1),
    JUNGLE_WALL_SIGN(-1),
    KELP(-1),
    LANTERN(-1),
    LAPIS_ORE(-1),
    LARGE_AMETHYST_BUD(-1),
    LARGE_FERN(-1),
    LAVA_BUCKET(-1),
    LAVA_CAULDRON(-1),
    LEATHER_BOOTS(-1),
    LEATHER_CHESTPLATE(-1),
    LEATHER_HELMET(-1),
    LEATHER_HORSE_ARMOR(-1),
    LEATHER_LEGGINGS(-1),
    LECTERN(-1),
    LIGHT(-1),
    LIGHT_BLUE_BANNER(-1),
    LIGHT_BLUE_BED(-1),
    LIGHT_BLUE_CANDLE(-1),
    LIGHT_BLUE_CANDLE_CAKE(-1),
    LIGHT_BLUE_DYE(-1),
    LIGHT_BLUE_SHULKER_BOX(-1),
    LIGHT_BLUE_WALL_BANNER(-1),
    LIGHT_BLUE_WOOL(-1),
    LIGHT_GRAY_BANNER(-1),
    LIGHT_GRAY_BED(-1),
    LIGHT_GRAY_CANDLE(-1),
    LIGHT_GRAY_CANDLE_CAKE(-1),
    LIGHT_GRAY_DYE(-1),
    LIGHT_GRAY_SHULKER_BOX(-1),
    LIGHT_GRAY_WALL_BANNER(-1),
    LIGHT_GRAY_WOOL(-1),
    LIGHT_WEIGHTED_PRESSURE_PLATE(-1),
    LILAC(-1),
    LILY_OF_THE_VALLEY(-1),
    LILY_PAD(-1),
    LIME_BANNER(-1),
    LIME_BED(-1),
    LIME_CANDLE(-1),
    LIME_CANDLE_CAKE(-1),
    LIME_DYE(-1),
    LIME_SHULKER_BOX(-1),
    LIME_WALL_BANNER(-1),
    LIME_WOOL(-1),
    LINGERING_POTION(-1),
    LOOM(-1),
    MAGENTA_BANNER(-1),
    MAGENTA_BED(-1),
    MAGENTA_CANDLE(-1),
    MAGENTA_CANDLE_CAKE(-1),
    MAGENTA_DYE(-1),
    MAGENTA_SHULKER_BOX(-1),
    MAGENTA_WALL_BANNER(-1),
    MAGENTA_WOOL(-1),
    MAGMA_BLOCK(-1),
    MANGROVE_BUTTON(-1),
    MANGROVE_DOOR(-1),
    MANGROVE_FENCE_GATE(-1),
    MANGROVE_PRESSURE_PLATE(-1),
    MANGROVE_PROPAGULE(-1),
    MANGROVE_SIGN(-1),
    MANGROVE_TRAPDOOR(-1),
    MANGROVE_WALL_SIGN(-1),
    MAP(-1),
    MEDIUM_AMETHYST_BUD(-1),
    MELON(-1),
    MOVING_PISTON(-1),
    MYCELIUM(-1),
    NETHERRACK(-1),
    NETHER_PORTAL(-1),
    NETHER_SPROUTS(-1),
    NETHER_WART(-1),
    NOTE_BLOCK(-1),
    OAK_BUTTON(-1),
    OAK_DOOR(-1),
    OAK_FENCE_GATE(-1),
    OAK_PRESSURE_PLATE(-1),
    OAK_SIGN(-1),
    OAK_TRAPDOOR(-1),
    OAK_WALL_SIGN(-1),
    OBSIDIAN(-1),
    ORANGE_BANNER(-1),
    ORANGE_BED(-1),
    ORANGE_CANDLE(-1),
    ORANGE_CANDLE_CAKE(-1),
    ORANGE_DYE(-1),
    ORANGE_SHULKER_BOX(-1),
    ORANGE_TULIP(-1),
    ORANGE_WALL_BANNER(-1),
    ORANGE_WOOL(-1),
    OXEYE_DAISY(-1),
    PAINTING(-1),
    PEONY(-1),
    PINK_BANNER(-1),
    PINK_BED(-1),
    PINK_CANDLE(-1),
    PINK_CANDLE_CAKE(-1),
    PINK_DYE(-1),
    PINK_SHULKER_BOX(-1),
    PINK_TULIP(-1),
    PINK_WALL_BANNER(-1),
    PINK_WOOL(-1),
    PISTON(-1),
    PISTON_HEAD(-1),
    PLAYER_HEAD(-1),
    PLAYER_WALL_HEAD(-1),
    POINTED_DRIPSTONE(-1),
    POLISHED_BLACKSTONE_BUTTON(-1),
    POLISHED_BLACKSTONE_PRESSURE_PLATE(-1),
    POPPY(-1),
    POTATO(-1),
    POTION(-1),
    POWDER_SNOW(-1),
    POWDER_SNOW_BUCKET(-1),
    PUMPKIN(-1),
    PURPLE_BANNER(-1),
    PURPLE_BED(-1),
    PURPLE_CANDLE(-1),
    PURPLE_CANDLE_CAKE(-1),
    PURPLE_DYE(-1),
    PURPLE_SHULKER_BOX(-1),
    PURPLE_WALL_BANNER(-1),
    PURPLE_WOOL(-1),
    RAIL(-1),
    REDSTONE_BLOCK(-1),
    REDSTONE_LAMP(-1),
    REDSTONE_ORE(-1),
    REDSTONE_TORCH(-1),
    REDSTONE_WALL_TORCH(-1),
    RED_BANNER(-1),
    RED_BED(-1),
    RED_CANDLE(-1),
    RED_CANDLE_CAKE(-1),
    RED_DYE(-1),
    RED_SHULKER_BOX(-1),
    RED_TULIP(-1),
    RED_WALL_BANNER(-1),
    RED_WOOL(-1),
    REPEATER(-1),
    REPEATING_COMMAND_BLOCK(-1),
    RESPAWN_ANCHOR(-1),
    ROSE_BUSH(-1),
    SANDSTONE(-1),
    SCAFFOLDING(-1),
    SCULK(-1),
    SCULK_SENSOR(-1),
    SCULK_SHRIEKER(-1),
    SCULK_VEIN(-1),
    SHEARS(-1),
    SHROOMLIGHT(-1),
    SHULKER_BOX(-1),
    SKELETON_SKULL(-1),
    SKELETON_WALL_SKULL(-1),
    SMALL_AMETHYST_BUD(-1),
    SMALL_DRIPLEAF(-1),
    SMITHING_TABLE(-1),
    SMOKER(-1),
    SNOW(-1),
    SNOWBALL(-1),
    SOUL_CAMPFIRE(-1),
    SOUL_FIRE(-1),
    SOUL_LANTERN(-1),
    SOUL_SAND(-1),
    SOUL_SOIL(-1),
    SOUL_TORCH(-1),
    SOUL_WALL_TORCH(-1),
    SPAWNER(-1),
    SPECTRAL_ARROW(-1),
    SPLASH_POTION(-1),
    SPRUCE_BUTTON(-1),
    SPRUCE_DOOR(-1),
    SPRUCE_FENCE_GATE(-1),
    SPRUCE_PRESSURE_PLATE(-1),
    SPRUCE_SAPLING(-1),
    SPRUCE_SIGN(-1),
    SPRUCE_TRAPDOOR(-1),
    SPRUCE_WALL_SIGN(-1),
    STICKY_PISTON(-1),
    STONECUTTER(-1),
    STONE_PRESSURE_PLATE(-1),
    SUGAR_CANE(-1),
    SUNFLOWER(-1),
    SUSPICIOUS_GRAVEL(-1),
    SUSPICIOUS_SAND(-1),
    SUSPICIOUS_STEW(-1),
    SWEET_BERRY_BUSH(-1),
    TALL_GRASS(-1),
    TARGET(-1),
    TIPPED_ARROW(-1),
    TORCHFLOWER_CROP(-1),
    TORCHFLOWER_SEEDS(-1),
    TRAPPED_CHEST(-1),
    TRIDENT(-1),
    TRIPWIRE_HOOK(-1),
    TURTLE_EGG(-1),
    TWISTING_VINES(-1),
    VOID_AIR(-1),
    WALL_TORCH(-1),
    WARPED_BUTTON(-1),
    WARPED_DOOR(-1),
    WARPED_FENCE_GATE(-1),
    WARPED_FUNGUS(-1),
    WARPED_PRESSURE_PLATE(-1),
    WARPED_ROOTS(-1),
    WARPED_SIGN(-1),
    WARPED_TRAPDOOR(-1),
    WARPED_WALL_SIGN(-1),
    WATER_BUCKET(-1),
    WEEPING_VINES(-1),
    WHEAT(-1),
    WHEAT_SEEDS(-1),
    WHITE_BANNER(-1),
    WHITE_BED(-1),
    WHITE_CANDLE(-1),
    WHITE_CANDLE_CAKE(-1),
    WHITE_DYE(-1),
    WHITE_SHULKER_BOX(-1),
    WHITE_TULIP(-1),
    WHITE_WALL_BANNER(-1),
    WHITE_WOOL(-1),
    WITHER_ROSE(-1),
    WITHER_SKELETON_SKULL(-1),
    WITHER_SKELETON_WALL_SKULL(-1),
    YELLOW_BANNER(-1),
    YELLOW_BED(-1),
    YELLOW_CANDLE(-1),
    YELLOW_CANDLE_CAKE(-1),
    YELLOW_DYE(-1),
    YELLOW_SHULKER_BOX(-1),
    YELLOW_WALL_BANNER(-1),
    YELLOW_WOOL(-1),
    ZOMBIE_HEAD(-1),
    ZOMBIE_WALL_HEAD(-1);

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

    /** @return the namespaced key {@code minecraft:<lowercase_name>}.
     *  CoreProtect's {@code BlockPlaceLogger} persists
     *  {@code material.getKey()} as the canonical block descriptor in
     *  the audit DB; without this method every place is logged with a
     *  {@link NoSuchMethodError}. */
    public org.bukkit.NamespacedKey getKey() {
        return new org.bukkit.NamespacedKey(
                org.bukkit.NamespacedKey.MINECRAFT,
                name().toLowerCase(java.util.Locale.ROOT));
    }

    /** @return a snapshot {@link org.bukkit.block.data.BlockData} for
     *  this material. CoreProtect's {@code BlockUtils.createBlockData}
     *  rebuilds the placed-block descriptor for DB serialization;
     *  RDForward has no block-state model, so the data is just the
     *  material wrapped in a {@link
     *  com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlockData}. */
    public org.bukkit.block.data.BlockData createBlockData() {
        return new com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlockData(this);
    }

    /** Bukkit 1.13+ overload accepting a state-string suffix. RDForward
     *  has no state model, so the suffix is ignored and the result
     *  matches {@link #createBlockData()}. */
    public org.bukkit.block.data.BlockData createBlockData(String data) {
        return createBlockData();
    }

    /** True if this material falls when unsupported (sand, gravel,
     *  anvils, concrete powder, scaffolding, dragon egg, dripstone,
     *  suspicious sand/gravel). CoreProtect's {@code BlockUtil.gravityScan}
     *  walks blocks above a placement and calls {@code hasGravity()} to
     *  decide whether to record them — without this method the listener
     *  {@link NoSuchMethodError}s on every block place. */
    public boolean hasGravity() {
        switch (this) {
            case SAND:
            case GRAVEL:
            case ANVIL:
            case CHIPPED_ANVIL:
            case DAMAGED_ANVIL:
            case SCAFFOLDING:
            case DRAGON_EGG:
            case POINTED_DRIPSTONE:
            case SUSPICIOUS_GRAVEL:
            case SUSPICIOUS_SAND:
                return true;
            default:
                return false;
        }
    }

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
        if (legacyId < 0) return null;
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
