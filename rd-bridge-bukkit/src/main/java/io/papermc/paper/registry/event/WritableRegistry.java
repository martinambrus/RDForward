package io.papermc.paper.registry.event;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface WritableRegistry {
    default void register(io.papermc.paper.registry.TypedKey arg0, java.util.function.Consumer arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.registry.event.WritableRegistry.register(Lio/papermc/paper/registry/TypedKey;Ljava/util/function/Consumer;)V");
    }
    void registerWith(io.papermc.paper.registry.TypedKey arg0, java.util.function.Consumer arg1);
}
