package io.papermc.paper.registry.event;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
interface RegistryEventTypeProvider {
    public static final java.util.Optional PROVIDER = java.util.Optional.empty();
    static io.papermc.paper.registry.event.RegistryEventTypeProvider provider() {
        return null;
    }
    io.papermc.paper.registry.event.type.RegistryEntryAddEventType registryEntryAdd(io.papermc.paper.registry.event.RegistryEventProvider arg0);
    io.papermc.paper.plugin.lifecycle.event.types.LifecycleEventType$Prioritizable registryCompose(io.papermc.paper.registry.event.RegistryEventProvider arg0);
}
