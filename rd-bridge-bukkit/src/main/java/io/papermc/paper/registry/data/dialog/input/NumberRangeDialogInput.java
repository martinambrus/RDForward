package io.papermc.paper.registry.data.dialog.input;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface NumberRangeDialogInput extends io.papermc.paper.registry.data.dialog.input.DialogInput {
    int width();
    net.kyori.adventure.text.Component label();
    java.lang.String labelFormat();
    float start();
    float end();
    java.lang.Float initial();
    java.lang.Float step();
}
