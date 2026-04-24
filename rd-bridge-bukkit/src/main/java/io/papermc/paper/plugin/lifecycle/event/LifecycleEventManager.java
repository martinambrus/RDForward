package io.papermc.paper.plugin.lifecycle.event;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface LifecycleEventManager {
    default void registerEventHandler(io.papermc.paper.plugin.lifecycle.event.types.LifecycleEventType arg0, io.papermc.paper.plugin.lifecycle.event.handler.LifecycleEventHandler arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager.registerEventHandler(Lio/papermc/paper/plugin/lifecycle/event/types/LifecycleEventType;Lio/papermc/paper/plugin/lifecycle/event/handler/LifecycleEventHandler;)V");
    }
    void registerEventHandler(io.papermc.paper.plugin.lifecycle.event.handler.configuration.LifecycleEventHandlerConfiguration arg0);
}
