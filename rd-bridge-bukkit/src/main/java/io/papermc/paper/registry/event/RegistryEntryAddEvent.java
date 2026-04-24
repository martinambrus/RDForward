package io.papermc.paper.registry.event;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface RegistryEntryAddEvent extends io.papermc.paper.registry.event.RegistryEvent {
    io.papermc.paper.registry.RegistryBuilder builder();
    io.papermc.paper.registry.TypedKey key();
    io.papermc.paper.registry.tag.Tag getOrCreateTag(io.papermc.paper.registry.tag.TagKey arg0);
}
