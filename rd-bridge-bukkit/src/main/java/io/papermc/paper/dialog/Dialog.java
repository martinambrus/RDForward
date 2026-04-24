package io.papermc.paper.dialog;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Dialog extends org.bukkit.Keyed, net.kyori.adventure.dialog.DialogLike {
    public static final io.papermc.paper.dialog.Dialog CUSTOM_OPTIONS = null;
    public static final io.papermc.paper.dialog.Dialog QUICK_ACTIONS = null;
    public static final io.papermc.paper.dialog.Dialog SERVER_LINKS = null;
    static io.papermc.paper.dialog.Dialog create(java.util.function.Consumer arg0) {
        return null;
    }
    org.bukkit.NamespacedKey getKey();
    default net.kyori.adventure.key.Key key() {
        return null;
    }
}
