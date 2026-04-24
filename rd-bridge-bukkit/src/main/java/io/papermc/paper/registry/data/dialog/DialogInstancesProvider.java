package io.papermc.paper.registry.data.dialog;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface DialogInstancesProvider {
    static io.papermc.paper.registry.data.dialog.DialogInstancesProvider instance() {
        return null;
    }
    io.papermc.paper.registry.data.dialog.DialogBase$Builder dialogBaseBuilder(net.kyori.adventure.text.Component arg0);
    io.papermc.paper.registry.data.dialog.ActionButton$Builder actionButtonBuilder(net.kyori.adventure.text.Component arg0);
    io.papermc.paper.registry.data.dialog.action.DialogAction$CustomClickAction register(io.papermc.paper.registry.data.dialog.action.DialogActionCallback arg0, net.kyori.adventure.text.event.ClickCallback$Options arg1);
    io.papermc.paper.registry.data.dialog.action.DialogAction$StaticAction staticAction(net.kyori.adventure.text.event.ClickEvent arg0);
    io.papermc.paper.registry.data.dialog.action.DialogAction$CommandTemplateAction commandTemplate(java.lang.String arg0);
    io.papermc.paper.registry.data.dialog.action.DialogAction$CustomClickAction customClick(net.kyori.adventure.key.Key arg0, net.kyori.adventure.nbt.api.BinaryTagHolder arg1);
    io.papermc.paper.registry.data.dialog.body.ItemDialogBody$Builder itemDialogBodyBuilder(org.bukkit.inventory.ItemStack arg0);
    io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody plainMessageDialogBody(net.kyori.adventure.text.Component arg0);
    io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody plainMessageDialogBody(net.kyori.adventure.text.Component arg0, int arg1);
    io.papermc.paper.registry.data.dialog.input.BooleanDialogInput$Builder booleanBuilder(java.lang.String arg0, net.kyori.adventure.text.Component arg1);
    io.papermc.paper.registry.data.dialog.input.NumberRangeDialogInput$Builder numberRangeBuilder(java.lang.String arg0, net.kyori.adventure.text.Component arg1, float arg2, float arg3);
    io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput$Builder singleOptionBuilder(java.lang.String arg0, net.kyori.adventure.text.Component arg1, java.util.List arg2);
    io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput$OptionEntry singleOptionEntry(java.lang.String arg0, net.kyori.adventure.text.Component arg1, boolean arg2);
    io.papermc.paper.registry.data.dialog.input.TextDialogInput$Builder textBuilder(java.lang.String arg0, net.kyori.adventure.text.Component arg1);
    io.papermc.paper.registry.data.dialog.input.TextDialogInput$MultilineOptions multilineOptions(java.lang.Integer arg0, java.lang.Integer arg1);
    io.papermc.paper.registry.data.dialog.type.ConfirmationType confirmation(io.papermc.paper.registry.data.dialog.ActionButton arg0, io.papermc.paper.registry.data.dialog.ActionButton arg1);
    io.papermc.paper.registry.data.dialog.type.DialogListType$Builder dialogList(io.papermc.paper.registry.set.RegistrySet arg0);
    io.papermc.paper.registry.data.dialog.type.MultiActionType$Builder multiAction(java.util.List arg0);
    io.papermc.paper.registry.data.dialog.type.NoticeType notice();
    io.papermc.paper.registry.data.dialog.type.NoticeType notice(io.papermc.paper.registry.data.dialog.ActionButton arg0);
    io.papermc.paper.registry.data.dialog.type.ServerLinksType serverLinks(io.papermc.paper.registry.data.dialog.ActionButton arg0, int arg1, int arg2);
}
