package io.papermc.paper.registry.data.dialog.input;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TextDialogInput extends io.papermc.paper.registry.data.dialog.input.DialogInput {
    int width();
    net.kyori.adventure.text.Component label();
    boolean labelVisible();
    java.lang.String initial();
    int maxLength();
    io.papermc.paper.registry.data.dialog.input.TextDialogInput$MultilineOptions multiline();
}
