package io.papermc.paper.registry.data.dialog.body;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ItemDialogBody extends io.papermc.paper.registry.data.dialog.body.DialogBody {
    org.bukkit.inventory.ItemStack item();
    io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody description();
    boolean showDecorations();
    boolean showTooltip();
    int width();
    int height();
}
