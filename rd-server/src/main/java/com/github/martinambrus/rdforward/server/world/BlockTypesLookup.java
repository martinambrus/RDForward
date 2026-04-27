package com.github.martinambrus.rdforward.server.world;

import com.github.martinambrus.rdforward.api.world.BlockType;
import com.github.martinambrus.rdforward.api.world.BlockTypes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Reverse lookup from namespaced block name (e.g.
 * {@code "minecraft:cobblestone"}) back to a {@link BlockType}. The
 * source-of-truth is the constants declared on {@link BlockTypes};
 * unknown names fall back to {@link BlockTypes#COBBLE} (per the
 * RubyDung-friendly fallback rule).
 *
 * <p>The map is built lazily once per JVM via reflection over the
 * public-static-final {@link BlockType} fields on {@link BlockTypes}.
 * Reflection is used so the lookup table stays in sync if new constants
 * are added later — no hand-maintained switch to drift.
 */
final class BlockTypesLookup {

    private static volatile Map<String, BlockType> BY_NAME;

    private BlockTypesLookup() {}

    /** Resolve a namespaced block name to its canonical {@link BlockType}.
     *  Names that don't match any known constant fall back to
     *  {@link BlockTypes#COBBLE} — RubyDung worlds don't have stone for
     *  the first few versions, so cobblestone is the safer universal
     *  fallback. */
    static BlockType byName(String name) {
        if (name == null || name.isBlank()) return BlockTypes.COBBLE;
        Map<String, BlockType> map = BY_NAME;
        if (map == null) {
            synchronized (BlockTypesLookup.class) {
                map = BY_NAME;
                if (map == null) {
                    map = build();
                    BY_NAME = map;
                }
            }
        }
        BlockType hit = map.get(name);
        return hit != null ? hit : BlockTypes.COBBLE;
    }

    private static Map<String, BlockType> build() {
        Map<String, BlockType> out = new HashMap<>();
        for (Field f : BlockTypes.class.getDeclaredFields()) {
            int mods = f.getModifiers();
            if (!Modifier.isPublic(mods) || !Modifier.isStatic(mods) || !Modifier.isFinal(mods)) continue;
            if (!BlockType.class.isAssignableFrom(f.getType())) continue;
            try {
                BlockType bt = (BlockType) f.get(null);
                if (bt != null && bt.getName() != null) out.put(bt.getName(), bt);
            } catch (IllegalAccessException ignored) {
                // public-static-final fields don't reach here in practice.
            }
        }
        return out;
    }
}
