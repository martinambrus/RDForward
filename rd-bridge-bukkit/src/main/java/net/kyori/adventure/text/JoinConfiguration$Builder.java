package net.kyori.adventure.text;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface JoinConfiguration$Builder extends net.kyori.adventure.builder.AbstractBuilder, net.kyori.adventure.util.Buildable$Builder {
    net.kyori.adventure.text.JoinConfiguration$Builder prefix(net.kyori.adventure.text.ComponentLike arg0);
    net.kyori.adventure.text.JoinConfiguration$Builder suffix(net.kyori.adventure.text.ComponentLike arg0);
    net.kyori.adventure.text.JoinConfiguration$Builder separator(net.kyori.adventure.text.ComponentLike arg0);
    net.kyori.adventure.text.JoinConfiguration$Builder lastSeparator(net.kyori.adventure.text.ComponentLike arg0);
    net.kyori.adventure.text.JoinConfiguration$Builder lastSeparatorIfSerial(net.kyori.adventure.text.ComponentLike arg0);
    net.kyori.adventure.text.JoinConfiguration$Builder convertor(java.util.function.Function arg0);
    net.kyori.adventure.text.JoinConfiguration$Builder predicate(java.util.function.Predicate arg0);
    net.kyori.adventure.text.JoinConfiguration$Builder parentStyle(net.kyori.adventure.text.format.Style arg0);
}
