package io.papermc.paper.registry.event.type;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface RegistryEntryAddConfiguration extends io.papermc.paper.plugin.lifecycle.event.handler.configuration.PrioritizedLifecycleEventHandlerConfiguration {
    default io.papermc.paper.registry.event.type.RegistryEntryAddConfiguration filter(io.papermc.paper.registry.TypedKey arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.registry.event.type.RegistryEntryAddConfiguration.filter(Lio/papermc/paper/registry/TypedKey;)Lio/papermc/paper/registry/event/type/RegistryEntryAddConfiguration;");
        return this;
    }
    io.papermc.paper.registry.event.type.RegistryEntryAddConfiguration filter(java.util.function.Predicate arg0);
    io.papermc.paper.registry.event.type.RegistryEntryAddConfiguration priority(int arg0);
    io.papermc.paper.registry.event.type.RegistryEntryAddConfiguration monitor();
}
