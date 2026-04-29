package org.bukkit;

/**
 * Stub implementation of {@link Tag} used as the value of every
 * {@code Tag.X} static field on the bridge. Returns an empty member
 * set so plugins like CoreProtect that iterate {@code
 * Tag.BUTTONS.getValues()} during {@code onEnable} class-load and
 * loop over zero elements rather than NPE on a null tag constant.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public final class EmptyBukkitTag implements org.bukkit.Tag {
    @Override
    public org.bukkit.NamespacedKey getKey() {
        return null;
    }
    @Override
    public boolean isTagged(org.bukkit.Keyed arg0) {
        return false;
    }
    @Override
    public java.util.Set getValues() {
        return java.util.Collections.emptySet();
    }
}
