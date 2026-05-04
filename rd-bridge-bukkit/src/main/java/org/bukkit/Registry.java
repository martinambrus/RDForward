package org.bukkit;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;
import org.bukkit.entity.EntityType;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Registry extends java.lang.Iterable<Keyed> {
    public static final Registry ADVANCEMENT = null;
    public static final Registry ART = null;
    public static final Registry ATTRIBUTE = null;
    public static final Registry BANNER_PATTERN = null;
    public static final Registry BIOME = null;
    public static final Registry BLOCK = null;
    public static final Registry BOSS_BARS = null;
    public static final Registry CAT_VARIANT = null;
    public static final Registry ENCHANTMENT = null;
    public static final Registry ENTITY_TYPE = new SimpleRegistry(EntityType.values());
    public static final Registry INSTRUMENT = null;
    public static final Registry ITEM = null;
    public static final Registry LOOT_TABLES = null;
    public static final Registry MATERIAL = new SimpleRegistry(Material.values());
    public static final Registry MENU = null;
    public static final Registry MOB_EFFECT = null;
    public static final Registry PARTICLE_TYPE = null;
    public static final Registry POTION = null;
    public static final Registry STATISTIC = null;
    public static final Registry STRUCTURE = null;
    public static final Registry STRUCTURE_TYPE = null;
    public static final Registry SOUND_EVENT = null;
    public static final Registry TRIM_MATERIAL = null;
    public static final Registry TRIM_PATTERN = null;
    public static final Registry DAMAGE_TYPE = null;
    public static final Registry JUKEBOX_SONG = null;
    public static final Registry VILLAGER_PROFESSION = null;
    public static final Registry VILLAGER_TYPE = null;
    public static final Registry MEMORY_MODULE_TYPE = null;
    public static final Registry FLUID = null;
    public static final Registry FROG_VARIANT = null;
    public static final Registry WOLF_VARIANT = null;
    public static final Registry MAP_DECORATION_TYPE = null;
    public static final Registry GAME_EVENT = null;
    public static final Registry DATA_COMPONENT_TYPE = null;
    public static final Registry GAME_RULE = null;
    public static final Registry EFFECT = null;
    public static final Registry POTION_EFFECT_TYPE = null;
    public static final Registry SOUNDS = null;
    Keyed get(NamespacedKey arg0);
    default Keyed get(net.kyori.adventure.key.Key arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.get(Lnet/kyori/adventure/key/Key;)Lorg/bukkit/Keyed;");
        return null;
    }
    default Keyed get(io.papermc.paper.registry.TypedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.get(Lio/papermc/paper/registry/TypedKey;)Lorg/bukkit/Keyed;");
        return null;
    }
    default Keyed getOrThrow(net.kyori.adventure.key.Key arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.getOrThrow(Lnet/kyori/adventure/key/Key;)Lorg/bukkit/Keyed;");
        return null;
    }
    default Keyed getOrThrow(io.papermc.paper.registry.TypedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.getOrThrow(Lio/papermc/paper/registry/TypedKey;)Lorg/bukkit/Keyed;");
        return null;
    }
    default NamespacedKey getKeyOrThrow(Keyed arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.getKeyOrThrow(Lorg/bukkit/Keyed;)Lorg/bukkit/NamespacedKey;");
        return null;
    }
    NamespacedKey getKey(Keyed arg0);
    boolean hasTag(io.papermc.paper.registry.tag.TagKey arg0);
    io.papermc.paper.registry.tag.Tag getTag(io.papermc.paper.registry.tag.TagKey arg0);
    default Collection getTagValues(io.papermc.paper.registry.tag.TagKey arg0) {
        return Collections.emptyList();
    }
    Collection getTags();
    default Keyed getOrThrow(NamespacedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.getOrThrow(Lorg/bukkit/NamespacedKey;)Lorg/bukkit/Keyed;");
        return null;
    }
    Stream stream();
    Stream keyStream();
    default Keyed match(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Registry.match(Ljava/lang/String;)Lorg/bukkit/Keyed;");
        return null;
    }
    int size();

    /** Minimal Registry backed by an enum's values. Supports iteration
     *  (forEach) and stream so WorldEdit 7.x can populate its internal
     *  registries from Material.values() / EntityType.values(). */
    final class SimpleRegistry implements Registry {
        private final Keyed[] values;

        SimpleRegistry(Keyed[] values) {
            this.values = values;
        }

        @Override
        public Iterator<Keyed> iterator() {
            return (Iterator<Keyed>) (Iterator) Arrays.asList(values).iterator();
        }

        @Override
        public Stream stream() {
            return Arrays.stream(values);
        }

        @Override
        public Stream keyStream() {
            return stream().map(k -> ((Keyed) k).getKey());
        }

        @Override
        public int size() {
            return values.length;
        }

        @Override
        public Keyed get(NamespacedKey key) {
            for (Keyed v : values) {
                if (v.getKey().equals(key)) return v;
            }
            return null;
        }

        @Override
        public NamespacedKey getKey(Keyed keyed) {
            return keyed.getKey();
        }

        @Override
        public boolean hasTag(io.papermc.paper.registry.tag.TagKey arg0) {
            return false;
        }

        @Override
        public io.papermc.paper.registry.tag.Tag getTag(io.papermc.paper.registry.tag.TagKey arg0) {
            return null;
        }

        @Override
        public Collection getTags() {
            return Collections.emptyList();
        }
    }
}
