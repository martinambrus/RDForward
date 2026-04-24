package net.kyori.adventure.text.flattener;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ComponentFlattener$Builder extends net.kyori.adventure.builder.AbstractBuilder, net.kyori.adventure.util.Buildable$Builder {
    net.kyori.adventure.text.flattener.ComponentFlattener$Builder mapper(java.lang.Class arg0, java.util.function.Function arg1);
    net.kyori.adventure.text.flattener.ComponentFlattener$Builder complexMapper(java.lang.Class arg0, java.util.function.BiConsumer arg1);
    net.kyori.adventure.text.flattener.ComponentFlattener$Builder unknownMapper(java.util.function.Function arg0);
    net.kyori.adventure.text.flattener.ComponentFlattener$Builder nestingLimit(int arg0);
}
