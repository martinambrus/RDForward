package io.papermc.paper.registry.event;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface RegistryComposeEvent extends io.papermc.paper.registry.event.RegistryEvent {
    io.papermc.paper.registry.event.WritableRegistry registry();
    io.papermc.paper.registry.tag.Tag getOrCreateTag(io.papermc.paper.registry.tag.TagKey arg0);
}
