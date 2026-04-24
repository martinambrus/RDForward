package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Registry extends java.lang.Iterable {
    public static final org.bukkit.Registry ADVANCEMENT = null;
    public static final org.bukkit.Registry ART = null;
    public static final org.bukkit.Registry ATTRIBUTE = null;
    public static final org.bukkit.Registry BANNER_PATTERN = null;
    public static final org.bukkit.Registry BIOME = null;
    public static final org.bukkit.Registry BLOCK = null;
    public static final org.bukkit.Registry BOSS_BARS = null;
    public static final org.bukkit.Registry CAT_VARIANT = null;
    public static final org.bukkit.Registry ENCHANTMENT = null;
    public static final org.bukkit.Registry ENTITY_TYPE = null;
    public static final org.bukkit.Registry INSTRUMENT = null;
    public static final org.bukkit.Registry ITEM = null;
    public static final org.bukkit.Registry LOOT_TABLES = null;
    public static final org.bukkit.Registry MATERIAL = null;
    public static final org.bukkit.Registry MENU = null;
    public static final org.bukkit.Registry MOB_EFFECT = null;
    public static final org.bukkit.Registry PARTICLE_TYPE = null;
    public static final org.bukkit.Registry POTION = null;
    public static final org.bukkit.Registry STATISTIC = null;
    public static final org.bukkit.Registry STRUCTURE = null;
    public static final org.bukkit.Registry STRUCTURE_TYPE = null;
    public static final org.bukkit.Registry SOUND_EVENT = null;
    public static final org.bukkit.Registry TRIM_MATERIAL = null;
    public static final org.bukkit.Registry TRIM_PATTERN = null;
    public static final org.bukkit.Registry DAMAGE_TYPE = null;
    public static final org.bukkit.Registry JUKEBOX_SONG = null;
    public static final org.bukkit.Registry VILLAGER_PROFESSION = null;
    public static final org.bukkit.Registry VILLAGER_TYPE = null;
    public static final org.bukkit.Registry MEMORY_MODULE_TYPE = null;
    public static final org.bukkit.Registry FLUID = null;
    public static final org.bukkit.Registry FROG_VARIANT = null;
    public static final org.bukkit.Registry WOLF_VARIANT = null;
    public static final org.bukkit.Registry MAP_DECORATION_TYPE = null;
    public static final org.bukkit.Registry GAME_EVENT = null;
    public static final org.bukkit.Registry DATA_COMPONENT_TYPE = null;
    public static final org.bukkit.Registry GAME_RULE = null;
    public static final org.bukkit.Registry EFFECT = null;
    public static final org.bukkit.Registry POTION_EFFECT_TYPE = null;
    public static final org.bukkit.Registry SOUNDS = null;
    org.bukkit.Keyed get(org.bukkit.NamespacedKey arg0);
    default org.bukkit.Keyed get(net.kyori.adventure.key.Key arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.get(Lnet/kyori/adventure/key/Key;)Lorg/bukkit/Keyed;");
        return null;
    }
    default org.bukkit.Keyed get(io.papermc.paper.registry.TypedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.get(Lio/papermc/paper/registry/TypedKey;)Lorg/bukkit/Keyed;");
        return null;
    }
    default org.bukkit.Keyed getOrThrow(net.kyori.adventure.key.Key arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.getOrThrow(Lnet/kyori/adventure/key/Key;)Lorg/bukkit/Keyed;");
        return null;
    }
    default org.bukkit.Keyed getOrThrow(io.papermc.paper.registry.TypedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.getOrThrow(Lio/papermc/paper/registry/TypedKey;)Lorg/bukkit/Keyed;");
        return null;
    }
    default org.bukkit.NamespacedKey getKeyOrThrow(org.bukkit.Keyed arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.getKeyOrThrow(Lorg/bukkit/Keyed;)Lorg/bukkit/NamespacedKey;");
        return null;
    }
    org.bukkit.NamespacedKey getKey(org.bukkit.Keyed arg0);
    boolean hasTag(io.papermc.paper.registry.tag.TagKey arg0);
    io.papermc.paper.registry.tag.Tag getTag(io.papermc.paper.registry.tag.TagKey arg0);
    default java.util.Collection getTagValues(io.papermc.paper.registry.tag.TagKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.getTagValues(Lio/papermc/paper/registry/tag/TagKey;)Ljava/util/Collection;");
        return java.util.Collections.emptyList();
    }
    java.util.Collection getTags();
    default org.bukkit.Keyed getOrThrow(org.bukkit.NamespacedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.getOrThrow(Lorg/bukkit/NamespacedKey;)Lorg/bukkit/Keyed;");
        return null;
    }
    java.util.stream.Stream stream();
    java.util.stream.Stream keyStream();
    default org.bukkit.Keyed match(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.match(Ljava/lang/String;)Lorg/bukkit/Keyed;");
        return null;
    }
    int size();
}
