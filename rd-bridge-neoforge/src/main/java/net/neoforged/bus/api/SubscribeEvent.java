package net.neoforged.bus.api;

/** Auto-generated stub from bus-7.2.0.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public @interface SubscribeEvent {
    net.neoforged.bus.api.EventPriority priority() default net.neoforged.bus.api.EventPriority.NORMAL;
    boolean receiveCanceled() default false;
}
