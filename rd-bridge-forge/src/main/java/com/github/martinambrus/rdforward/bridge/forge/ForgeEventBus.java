package com.github.martinambrus.rdforward.bridge.forge;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Minimal {@link IEventBus} implementation. Holds registered listeners in
 * priority order; {@link #post} dispatches to every listener whose event
 * type matches the posted event's class (or a superclass thereof).
 *
 * <p>Two kinds of listener exist: {@code register(Object)} walks the
 * object's methods for {@code @SubscribeEvent} handlers (instance methods
 * on instances, static methods on classes), and
 * {@code addListener(Consumer<T>)} wires a raw lambda.
 */
public class ForgeEventBus implements IEventBus {

    private static final Logger LOG = Logger.getLogger("RDForward/ForgeBridge");

    private final String name;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    public ForgeEventBus(String name) {
        this.name = name;
    }

    public String name() { return name; }

    @Override
    public void register(Object target) {
        if (target instanceof Class<?> cls) {
            registerStatic(cls);
        } else {
            registerInstance(target);
        }
    }

    private void registerInstance(Object target) {
        for (Method m : target.getClass().getMethods()) {
            SubscribeEvent ann = m.getAnnotation(SubscribeEvent.class);
            if (ann == null || Modifier.isStatic(m.getModifiers())) continue;
            bind(m, ann, target);
        }
    }

    private void registerStatic(Class<?> cls) {
        for (Method m : cls.getDeclaredMethods()) {
            SubscribeEvent ann = m.getAnnotation(SubscribeEvent.class);
            if (ann == null || !Modifier.isStatic(m.getModifiers())) continue;
            m.setAccessible(true);
            bind(m, ann, null);
        }
    }

    private void bind(Method m, SubscribeEvent ann, Object instance) {
        Class<?>[] params = m.getParameterTypes();
        if (params.length != 1 || !Event.class.isAssignableFrom(params[0])) {
            LOG.warning("[ForgeBridge] skipping @SubscribeEvent handler with bad signature: " + m);
            return;
        }
        @SuppressWarnings("unchecked")
        Class<? extends Event> eventType = (Class<? extends Event>) params[0];
        listeners.add(new Listener(eventType, ann.priority(), ann.receiveCanceled(), event -> {
            try {
                m.invoke(instance, event);
            } catch (ReflectiveOperationException roe) {
                LOG.warning("[ForgeBridge] listener " + m + " threw: " + roe.getCause());
            }
        }));
        resort();
    }

    @Override
    public <T extends Event> void addListener(Consumer<T> consumer) {
        addListener(EventPriority.NORMAL, false, inferEventType(consumer), consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, Consumer<T> consumer) {
        addListener(priority, false, inferEventType(consumer), consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled, Consumer<T> consumer) {
        addListener(priority, receiveCanceled, inferEventType(consumer), consumer);
    }

    @Override
    public <T extends Event> void addListener(EventPriority priority, boolean receiveCanceled,
                                              Class<T> eventType, Consumer<T> consumer) {
        @SuppressWarnings("unchecked")
        Consumer<Event> erased = (Consumer<Event>) consumer;
        listeners.add(new Listener(eventType, priority, receiveCanceled, erased));
        resort();
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> Class<T> inferEventType(Consumer<T> consumer) {
        // Forge resolves the T type via reflection on the lambda; too much machinery
        // for a stub. Fall back to the base Event type — the dispatcher filters on
        // isAssignableFrom so every consumer wired this way receives every event.
        return (Class<T>) Event.class;
    }

    @Override
    public void unregister(Object target) {
        listeners.removeIf(l -> l.handlesTarget(target));
    }

    @Override
    public <T extends Event> T post(T event) {
        for (Listener l : listeners) {
            if (!l.eventType.isAssignableFrom(event.getClass())) continue;
            if (event.isCanceled() && !l.receiveCanceled) continue;
            l.handler.accept(event);
        }
        return event;
    }

    private void resort() {
        List<Listener> sorted = new ArrayList<>(listeners);
        sorted.sort(Comparator.comparingInt(l -> l.priority.ordinal()));
        listeners.clear();
        listeners.addAll(sorted);
    }

    public int listenerCount() { return listeners.size(); }

    public void clear() { listeners.clear(); }

    private record Listener(
            Class<? extends Event> eventType,
            EventPriority priority,
            boolean receiveCanceled,
            Consumer<Event> handler
    ) {
        boolean handlesTarget(Object target) {
            return false; // raw Consumer handlers have no owning target; register(Object) listeners are cleared by ResourceSweeper
        }
    }
}
