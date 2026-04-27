// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.metadata;

/**
 * Bukkit-shaped {@code FixedMetadataValue}. Forwards the owning plugin
 * to {@link MetadataValueAdapter} (the previous auto-generated stub
 * dropped it) and stores the value so {@link #value()} round-trips.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class FixedMetadataValue extends org.bukkit.metadata.LazyMetadataValue {
    private final java.lang.Object stored;
    public FixedMetadataValue(org.bukkit.plugin.Plugin arg0, java.lang.Object arg1) {
        super(arg0, (java.util.concurrent.Callable) null);
        this.stored = arg1;
    }
    public FixedMetadataValue() {
        super((org.bukkit.plugin.Plugin) null, (java.util.concurrent.Callable) null);
        this.stored = null;
    }
    public void invalidate() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.metadata.FixedMetadataValue.invalidate()V");
    }
    public java.lang.Object value() {
        return stored;
    }
}
