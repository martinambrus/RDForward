package net.kyori.adventure.text.serializer.legacy;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface LegacyComponentSerializer$Builder extends net.kyori.adventure.builder.AbstractBuilder, net.kyori.adventure.util.Buildable$Builder {
    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder character(char arg0);
    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder hexCharacter(char arg0);
    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder extractUrls();
    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder extractUrls(java.util.regex.Pattern arg0);
    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder extractUrls(net.kyori.adventure.text.format.Style arg0);
    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder extractUrls(java.util.regex.Pattern arg0, net.kyori.adventure.text.format.Style arg1);
    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder hexColors();
    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder useUnusualXRepeatedCharacterHexFormat();
    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder flattener(net.kyori.adventure.text.flattener.ComponentFlattener arg0);
    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder formats(java.util.List arg0);
    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer build();
}
