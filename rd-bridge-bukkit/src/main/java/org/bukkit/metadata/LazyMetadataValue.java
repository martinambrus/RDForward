// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.metadata;

/**
 * Bukkit-shaped {@code LazyMetadataValue}. Forwards the owning plugin
 * to {@link MetadataValueAdapter} (the previous auto-generated stub
 * dropped it). Without this, plugins comparing
 * {@code value.getOwningPlugin().equals(myPlugin)} crash with NPE on
 * every per-tick metadata sweep — LoginSecurity's
 * {@code SessionManager} hits this on every action it queues.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class LazyMetadataValue extends org.bukkit.metadata.MetadataValueAdapter {
    public LazyMetadataValue(org.bukkit.plugin.Plugin arg0, java.util.concurrent.Callable arg1) { super(arg0); }
    public LazyMetadataValue(org.bukkit.plugin.Plugin arg0, org.bukkit.metadata.LazyMetadataValue$CacheStrategy arg1, java.util.concurrent.Callable arg2) { super(arg0); }
    protected LazyMetadataValue(org.bukkit.plugin.Plugin arg0) { super(arg0); }
    public LazyMetadataValue() { super((org.bukkit.plugin.Plugin) null); }
    public java.lang.Object value() {
        return null;
    }
    public void invalidate() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.metadata.LazyMetadataValue.invalidate()V");
    }
}
