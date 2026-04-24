package com.github.martinambrus.rdforward.bridge.forge;

import com.github.martinambrus.rdforward.api.event.EventPriority;
import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.BlockBreakCallback;
import com.github.martinambrus.rdforward.api.event.server.BlockPlaceCallback;
import com.github.martinambrus.rdforward.api.event.server.ChatCallback;
import com.github.martinambrus.rdforward.api.event.server.PlayerJoinCallback;
import com.github.martinambrus.rdforward.api.event.server.PlayerLeaveCallback;
import com.github.martinambrus.rdforward.api.event.server.ServerStartedCallback;
import com.github.martinambrus.rdforward.api.event.server.ServerStoppingCallback;
import com.github.martinambrus.rdforward.api.event.server.ServerTickCallback;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.api.server.Server;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.event.server.ServerStartingEvent;
import net.minecraftforge.fml.event.server.ServerStoppedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Installs forwarders from rd-api {@code ServerEvents} into
 * {@link MinecraftForge#EVENT_BUS}. Each forwarder constructs a Forge-shaped
 * event from the rd-api callback parameters, posts it to the global bus,
 * and checks the event's {@code isCanceled()} flag to thread cancellation
 * back to rd-api.
 *
 * <p>Install is idempotent and reversible — {@link #uninstall()} removes
 * every listener the bridge registered and clears the global bus.
 */
public final class ForgeBridge {

    private static final Logger LOG = Logger.getLogger("RDForward/ForgeBridge");

    static final String OWNER = "__forge_bridge__";

    private static volatile Server installed;
    private static final List<Runnable> detachers = new ArrayList<>();

    private ForgeBridge() {}

    public static synchronized void install(Server rdServer) {
        if (installed != null) return;
        installed = rdServer;
        wireCancellable();
        wirePlain();
        LOG.fine("[ForgeBridge] installed " + detachers.size() + " forwarders");
    }

    public static synchronized void uninstall() {
        if (installed == null) return;
        for (Runnable r : detachers) r.run();
        detachers.clear();
        if (MinecraftForge.EVENT_BUS instanceof ForgeEventBus bus) bus.clear();
        installed = null;
    }

    public static boolean isInstalled() { return installed != null; }

    private static void wireCancellable() {
        BlockBreakCallback breakCb = (name, x, y, z, blockType) -> {
            BlockEvent.BreakEvent ev = new BlockEvent.BreakEvent(name, x, y, z, blockType);
            MinecraftForge.EVENT_BUS.post(ev);
            return ev.isCanceled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.BLOCK_BREAK.register(EventPriority.NORMAL, breakCb, OWNER);
        detachers.add(() -> ServerEvents.BLOCK_BREAK.unregisterByOwner(OWNER));

        BlockPlaceCallback placeCb = (name, x, y, z, blockType) -> {
            BlockEvent.EntityPlaceEvent ev = new BlockEvent.EntityPlaceEvent(name, x, y, z, blockType);
            MinecraftForge.EVENT_BUS.post(ev);
            return ev.isCanceled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.BLOCK_PLACE.register(EventPriority.NORMAL, placeCb, OWNER);
        detachers.add(() -> ServerEvents.BLOCK_PLACE.unregisterByOwner(OWNER));

        ChatCallback chatCb = (name, message) -> {
            ServerChatEvent ev = new ServerChatEvent(name, message);
            MinecraftForge.EVENT_BUS.post(ev);
            return ev.isCanceled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.CHAT.register(EventPriority.NORMAL, chatCb, OWNER);
        detachers.add(() -> ServerEvents.CHAT.unregisterByOwner(OWNER));
    }

    private static void wirePlain() {
        PlayerJoinCallback joinCb = (name, version) ->
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedInEvent(name));
        ServerEvents.PLAYER_JOIN.register(joinCb);
        detachers.add(() -> ServerEvents.PLAYER_JOIN.unregister(joinCb));

        PlayerLeaveCallback leaveCb = name ->
                MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerLoggedOutEvent(name));
        ServerEvents.PLAYER_LEAVE.register(leaveCb);
        detachers.add(() -> ServerEvents.PLAYER_LEAVE.unregister(leaveCb));

        ServerTickCallback tickCb = tickCount ->
                MinecraftForge.EVENT_BUS.post(new TickEvent.ServerTickEvent(TickEvent.Phase.START));
        ServerEvents.SERVER_TICK.register(tickCb);
        detachers.add(() -> ServerEvents.SERVER_TICK.unregister(tickCb));

        ServerStartedCallback startedCb = () ->
                MinecraftForge.EVENT_BUS.post(new ServerStartingEvent());
        ServerEvents.SERVER_STARTED.register(startedCb);
        detachers.add(() -> ServerEvents.SERVER_STARTED.unregister(startedCb));

        ServerStoppingCallback stoppingCb = () ->
                MinecraftForge.EVENT_BUS.post(new ServerStoppedEvent());
        ServerEvents.SERVER_STOPPING.register(stoppingCb);
        detachers.add(() -> ServerEvents.SERVER_STOPPING.unregister(stoppingCb));
    }
}
