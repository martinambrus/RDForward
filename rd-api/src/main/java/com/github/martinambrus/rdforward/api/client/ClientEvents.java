package com.github.martinambrus.rdforward.api.client;

import com.github.martinambrus.rdforward.api.event.Event;
import com.github.martinambrus.rdforward.api.event.EventResult;

/**
 * Client-side event definitions. Signature-compatible with Fabric's
 * {@code Event<T>} pattern — mods register listeners via
 * {@code EVENT.register(callback)} and the client fires them by calling
 * {@code EVENT.invoker().onX(...)}.
 *
 * <p>Cancellable events ({@code KEY_PRESS}, {@code MOUSE_CLICK},
 * {@code CHAT_RECEIVED}, {@code CHAT_SEND}) use {@link EventResult}:
 * returning anything other than {@link EventResult#PASS} stops dispatch
 * and consumes the event.
 */
public final class ClientEvents {

    private ClientEvents() {}

    public static final Event<ClientReady> CLIENT_READY = Event.create(
            () -> {},
            listeners -> () -> {
                for (ClientReady l : listeners) l.onReady();
            }
    );

    public static final Event<ClientStopping> CLIENT_STOPPING = Event.create(
            () -> {},
            listeners -> () -> {
                for (ClientStopping l : listeners) l.onStopping();
            }
    );

    /** Fires once per game tick (20 TPS), after player physics. */
    public static final Event<ClientTick> CLIENT_TICK = Event.create(
            () -> {},
            listeners -> () -> {
                for (ClientTick l : listeners) l.onTick();
            }
    );

    /**
     * Fires after 3D world rendering and hit highlight, before the 2D HUD
     * pass. The GL modelview matrix is still in camera space — suitable
     * for world-space overlays like waypoint markers.
     */
    public static final Event<RenderWorld> RENDER_WORLD = Event.create(
            partialTick -> {},
            listeners -> partialTick -> {
                for (RenderWorld l : listeners) l.onRenderWorld(partialTick);
            }
    );

    /**
     * Fires during the 2D HUD pass, after built-in HUD and registered
     * {@link GameOverlay}s, before buffer swap.
     */
    public static final Event<RenderHud> RENDER_HUD = Event.create(
            ctx -> {},
            listeners -> ctx -> {
                for (RenderHud l : listeners) l.onRenderHud(ctx);
            }
    );

    /** Cancellable: returning non-{@code PASS} swallows the key event. */
    public static final Event<KeyPress> KEY_PRESS = Event.create(
            (keyCode, mods) -> EventResult.PASS,
            listeners -> (keyCode, mods) -> {
                for (KeyPress l : listeners) {
                    EventResult result = l.onKeyPress(keyCode, mods);
                    if (result != EventResult.PASS) return result;
                }
                return EventResult.PASS;
            }
    );

    public static final Event<KeyRelease> KEY_RELEASE = Event.create(
            (keyCode, mods) -> {},
            listeners -> (keyCode, mods) -> {
                for (KeyRelease l : listeners) l.onKeyRelease(keyCode, mods);
            }
    );

    /** Cancellable: returning non-{@code PASS} swallows the click. */
    public static final Event<MouseClick> MOUSE_CLICK = Event.create(
            (button, pressed, x, y) -> EventResult.PASS,
            listeners -> (button, pressed, x, y) -> {
                for (MouseClick l : listeners) {
                    EventResult result = l.onClick(button, pressed, x, y);
                    if (result != EventResult.PASS) return result;
                }
                return EventResult.PASS;
            }
    );

    /** Cancellable: returning non-{@code PASS} hides the received message. */
    public static final Event<ChatReceived> CHAT_RECEIVED = Event.create(
            message -> EventResult.PASS,
            listeners -> message -> {
                for (ChatReceived l : listeners) {
                    EventResult result = l.onReceived(message);
                    if (result != EventResult.PASS) return result;
                }
                return EventResult.PASS;
            }
    );

    /** Cancellable: returning non-{@code PASS} prevents the send. */
    public static final Event<ChatSend> CHAT_SEND = Event.create(
            message -> EventResult.PASS,
            listeners -> message -> {
                for (ChatSend l : listeners) {
                    EventResult result = l.onSend(message);
                    if (result != EventResult.PASS) return result;
                }
                return EventResult.PASS;
            }
    );

    public static final Event<ServerConnect> SERVER_CONNECT = Event.create(
            (host, port) -> {},
            listeners -> (host, port) -> {
                for (ServerConnect l : listeners) l.onConnect(host, port);
            }
    );

    public static final Event<ServerDisconnect> SERVER_DISCONNECT = Event.create(
            reason -> {},
            listeners -> reason -> {
                for (ServerDisconnect l : listeners) l.onDisconnect(reason);
            }
    );

    public static final Event<ScreenOpen> SCREEN_OPEN = Event.create(
            screen -> {},
            listeners -> screen -> {
                for (ScreenOpen l : listeners) l.onOpen(screen);
            }
    );

    public static final Event<ScreenClose> SCREEN_CLOSE = Event.create(
            screen -> {},
            listeners -> screen -> {
                for (ScreenClose l : listeners) l.onClose(screen);
            }
    );

    @FunctionalInterface public interface ClientReady { void onReady(); }
    @FunctionalInterface public interface ClientStopping { void onStopping(); }
    @FunctionalInterface public interface ClientTick { void onTick(); }
    @FunctionalInterface public interface RenderWorld { void onRenderWorld(float partialTick); }
    @FunctionalInterface public interface RenderHud { void onRenderHud(DrawContext ctx); }
    @FunctionalInterface public interface KeyPress { EventResult onKeyPress(int keyCode, int mods); }
    @FunctionalInterface public interface KeyRelease { void onKeyRelease(int keyCode, int mods); }
    @FunctionalInterface public interface MouseClick { EventResult onClick(int button, boolean pressed, double x, double y); }
    @FunctionalInterface public interface ChatReceived { EventResult onReceived(String message); }
    @FunctionalInterface public interface ChatSend { EventResult onSend(String message); }
    @FunctionalInterface public interface ServerConnect { void onConnect(String host, int port); }
    @FunctionalInterface public interface ServerDisconnect { void onDisconnect(String reason); }
    @FunctionalInterface public interface ScreenOpen { void onOpen(GameScreen screen); }
    @FunctionalInterface public interface ScreenClose { void onClose(GameScreen screen); }
}
