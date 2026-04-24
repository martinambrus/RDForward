package io.papermc.paper.registry.data.dialog;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface DialogBase$Builder {
    io.papermc.paper.registry.data.dialog.DialogBase$Builder externalTitle(net.kyori.adventure.text.Component arg0);
    io.papermc.paper.registry.data.dialog.DialogBase$Builder canCloseWithEscape(boolean arg0);
    io.papermc.paper.registry.data.dialog.DialogBase$Builder pause(boolean arg0);
    io.papermc.paper.registry.data.dialog.DialogBase$Builder afterAction(io.papermc.paper.registry.data.dialog.DialogBase$DialogAfterAction arg0);
    io.papermc.paper.registry.data.dialog.DialogBase$Builder body(java.util.List arg0);
    io.papermc.paper.registry.data.dialog.DialogBase$Builder inputs(java.util.List arg0);
    io.papermc.paper.registry.data.dialog.DialogBase build();
}
