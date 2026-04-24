package net.minecraftforge.eventbus.api;

import java.util.function.Consumer;

/**
 * Stub of Forge's event-bus interface. Implemented by
 * {@code ForgeEventBus} in the bridge; calls on this interface route to
 * rd-api events or to the mod's own handlers.
 */
public interface IEventBus {

    void register(Object target);

    <T extends Event> void addListener(Consumer<T> consumer);

    <T extends Event> void addListener(EventPriority priority, Consumer<T> consumer);

    <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Consumer<T> consumer);

    <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Class<T> eventType, Consumer<T> consumer);

    void unregister(Object target);

    <T extends Event> T post(T event);
}
