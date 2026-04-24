package io.papermc.paper.plugin.lifecycle.event.types;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
interface LifecycleEventTypeProvider {
    public static final java.util.Optional INSTANCE = java.util.Optional.empty();
    static io.papermc.paper.plugin.lifecycle.event.types.LifecycleEventTypeProvider provider() {
        return null;
    }
    io.papermc.paper.plugin.lifecycle.event.types.LifecycleEventType$Monitorable monitor(java.lang.String arg0, java.lang.Class arg1);
    io.papermc.paper.plugin.lifecycle.event.types.LifecycleEventType$Prioritizable prioritized(java.lang.String arg0, java.lang.Class arg1);
    io.papermc.paper.plugin.lifecycle.event.types.TagEventTypeProvider tagProvider();
}
