package io.papermc.paper.registry.data.dialog;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface DialogBase {
    static io.papermc.paper.registry.data.dialog.DialogBase create(net.kyori.adventure.text.Component arg0, net.kyori.adventure.text.Component arg1, boolean arg2, boolean arg3, io.papermc.paper.registry.data.dialog.DialogBase$DialogAfterAction arg4, java.util.List arg5, java.util.List arg6) {
        return null;
    }
    static io.papermc.paper.registry.data.dialog.DialogBase$Builder builder(net.kyori.adventure.text.Component arg0) {
        return null;
    }
    net.kyori.adventure.text.Component title();
    net.kyori.adventure.text.Component externalTitle();
    boolean canCloseWithEscape();
    boolean pause();
    io.papermc.paper.registry.data.dialog.DialogBase$DialogAfterAction afterAction();
    java.util.List body();
    java.util.List inputs();
}
