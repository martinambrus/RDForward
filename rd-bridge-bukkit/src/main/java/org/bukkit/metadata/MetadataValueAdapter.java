// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.metadata;

/**
 * Bukkit-shaped {@code MetadataValueAdapter}. Captures the owning
 * {@link org.bukkit.plugin.Plugin} passed to the constructor so
 * {@link #getOwningPlugin()} returns a non-null reference. The auto-
 * generated previous stub dropped the plugin and returned {@code null},
 * which broke any plugin code that compared the result via
 * {@code Object.equals} — notably LoginSecurity's per-tick session sweeps
 * which call {@code value.getOwningPlugin().equals(myPlugin)} and
 * crash with NPE when the cached value's owner is {@code null}.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class MetadataValueAdapter implements org.bukkit.metadata.MetadataValue {
    private final org.bukkit.plugin.Plugin owningPlugin;
    protected MetadataValueAdapter(org.bukkit.plugin.Plugin arg0) { this.owningPlugin = arg0; }
    protected MetadataValueAdapter() { this.owningPlugin = null; }
    public org.bukkit.plugin.Plugin getOwningPlugin() {
        return owningPlugin;
    }
    public int asInt() {
        return 0;
    }
    public float asFloat() {
        return 0.0f;
    }
    public double asDouble() {
        return 0.0;
    }
    public long asLong() {
        return 0L;
    }
    public short asShort() {
        return (short) 0;
    }
    public byte asByte() {
        return (byte) 0;
    }
    public boolean asBoolean() {
        return false;
    }
    public java.lang.String asString() {
        return null;
    }
}
