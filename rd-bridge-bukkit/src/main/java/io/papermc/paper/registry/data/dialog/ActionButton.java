package io.papermc.paper.registry.data.dialog;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ActionButton {
    static io.papermc.paper.registry.data.dialog.ActionButton create(net.kyori.adventure.text.Component arg0, net.kyori.adventure.text.Component arg1, int arg2, io.papermc.paper.registry.data.dialog.action.DialogAction arg3) {
        return null;
    }
    static io.papermc.paper.registry.data.dialog.ActionButton$Builder builder(net.kyori.adventure.text.Component arg0) {
        return null;
    }
    net.kyori.adventure.text.Component label();
    net.kyori.adventure.text.Component tooltip();
    int width();
    io.papermc.paper.registry.data.dialog.action.DialogAction action();
}
