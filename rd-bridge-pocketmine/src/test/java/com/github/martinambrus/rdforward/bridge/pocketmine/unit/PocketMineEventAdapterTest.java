package com.github.martinambrus.rdforward.bridge.pocketmine.unit;

import com.github.martinambrus.rdforward.api.event.EventPriority;
import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.pocketmine.PocketMineEventAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pocketmine.event.HandleEvent;
import pocketmine.event.Listener;
import pocketmine.event.block.BlockBreakEvent;
import pocketmine.event.block.BlockPlaceEvent;
import pocketmine.event.player.PlayerChatEvent;
import pocketmine.event.player.PlayerJoinEvent;
import pocketmine.event.player.PlayerQuitEvent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PocketMineEventAdapterTest {

    @BeforeEach
    void clearBefore() { ServerEvents.clearAll(); }

    @AfterEach
    void clearAfter() { ServerEvents.clearAll(); }

    public static final class AllEvents implements Listener {
        public final AtomicReference<BlockBreakEvent> lastBreak = new AtomicReference<>();
        public final AtomicReference<BlockPlaceEvent> lastPlace = new AtomicReference<>();
        public final AtomicReference<PlayerChatEvent> lastChat = new AtomicReference<>();
        public final AtomicReference<PlayerJoinEvent> lastJoin = new AtomicReference<>();
        public final AtomicReference<PlayerQuitEvent> lastQuit = new AtomicReference<>();

        @HandleEvent public void onBreak(BlockBreakEvent e) { lastBreak.set(e); }
        @HandleEvent public void onPlace(BlockPlaceEvent e) { lastPlace.set(e); }
        @HandleEvent public void onChat(PlayerChatEvent e) { lastChat.set(e); }
        @HandleEvent public void onJoin(PlayerJoinEvent e) { lastJoin.set(e); }
        @HandleEvent public void onQuit(PlayerQuitEvent e) { lastQuit.set(e); }
    }

    public static final class CancellingChat implements Listener {
        @HandleEvent public void onChat(PlayerChatEvent e) { e.setCancelled(true); }
    }

    public static final class HighestPrio implements Listener {
        public final AtomicReference<EventPriority> observed = new AtomicReference<>();

        @HandleEvent(priority = HandleEvent.EventPriority.HIGHEST)
        public void onChat(PlayerChatEvent e) {}
    }

    public static final class PlainMonitor implements Listener {
        public int calls = 0;
        @HandleEvent(priority = HandleEvent.EventPriority.MONITOR)
        public void onChat(PlayerChatEvent e) { calls++; }
    }

    public static final class BadSignature implements Listener {
        @HandleEvent public void noArgs() {}
        @HandleEvent public void wrongType(String s) {}
    }

    public static final class UnsupportedEventType implements Listener {
        @HandleEvent public void onUnrelated(pocketmine.event.Event e) {}
    }

    @Test
    void registersAllFiveSupportedEventTypes() {
        PocketMineEventAdapter.register(new AllEvents(), "plug");
        assertEquals(1, ServerEvents.BLOCK_BREAK.getListenerInfo().size());
        assertEquals(1, ServerEvents.BLOCK_PLACE.getListenerInfo().size());
        assertEquals(1, ServerEvents.CHAT.getListenerInfo().size());
        assertEquals(1, ServerEvents.PLAYER_JOIN.listenerCount());
        assertEquals(1, ServerEvents.PLAYER_LEAVE.listenerCount());
    }

    @Test
    void blockBreakEventForwardsToHandleEvent() {
        AllEvents l = new AllEvents();
        PocketMineEventAdapter.register(l, "plug");
        ServerEvents.BLOCK_BREAK.invoker().onBlockBreak("alice", 1, 2, 3, 4);
        assertNotNull(l.lastBreak.get());
        assertEquals("alice", l.lastBreak.get().getPlayerName());
        assertEquals(1, l.lastBreak.get().getX());
        assertEquals(4, l.lastBreak.get().getBlockType());
    }

    @Test
    void chatCancellationBubblesBackToRdApi() {
        PocketMineEventAdapter.register(new CancellingChat(), "plug");
        EventResult r = ServerEvents.CHAT.invoker().onChat("alice", "blocked");
        assertEquals(EventResult.CANCEL, r);
    }

    @Test
    void priorityOnAnnotationHonouredInListenerInfo() {
        PocketMineEventAdapter.register(new HighestPrio(), "plug");
        assertEquals(EventPriority.HIGHEST,
                ServerEvents.CHAT.getListenerInfo().get(0).priority());
    }

    @Test
    void monitorSeesEventEvenAfterCancellingListener() {
        PocketMineEventAdapter.register(new CancellingChat(), "plug-cancel");
        PlainMonitor mon = new PlainMonitor();
        PocketMineEventAdapter.register(mon, "plug-mon");
        ServerEvents.CHAT.invoker().onChat("alice", "monitored");
        assertEquals(1, mon.calls,
                "MONITOR handlers always run regardless of upstream cancellation");
    }

    @Test
    void badSignatureMethodsSkippedSilently() {
        PocketMineEventAdapter.register(new BadSignature(), "plug");
        assertEquals(0, ServerEvents.CHAT.getListenerInfo().size());
        assertEquals(0, ServerEvents.PLAYER_JOIN.listenerCount());
    }

    @Test
    void unsupportedEventTypeIsIgnoredNotWired() {
        PocketMineEventAdapter.register(new UnsupportedEventType(), "plug");
        assertEquals(0, ServerEvents.CHAT.getListenerInfo().size());
        assertEquals(0, ServerEvents.BLOCK_BREAK.getListenerInfo().size());
    }

    @Test
    void playerJoinEventDeliversPlayerName() {
        AllEvents l = new AllEvents();
        PocketMineEventAdapter.register(l, "plug");
        ServerEvents.PLAYER_JOIN.invoker().onPlayerJoin("bob", null);
        assertNotNull(l.lastJoin.get());
        assertEquals("bob", l.lastJoin.get().getPlayerName());
    }

    @Test
    void playerQuitEventDeliversPlayerName() {
        AllEvents l = new AllEvents();
        PocketMineEventAdapter.register(l, "plug");
        ServerEvents.PLAYER_LEAVE.invoker().onPlayerLeave("carol");
        assertNotNull(l.lastQuit.get());
        assertEquals("carol", l.lastQuit.get().getPlayerName());
    }

    @Test
    void blockPlaceEventRoundTripsAllCoords() {
        AllEvents l = new AllEvents();
        PocketMineEventAdapter.register(l, "plug");
        ServerEvents.BLOCK_PLACE.invoker().onBlockPlace("dan", 10, 20, 30, 5);
        assertNotNull(l.lastPlace.get());
        assertEquals(10, l.lastPlace.get().getX());
        assertEquals(20, l.lastPlace.get().getY());
        assertEquals(30, l.lastPlace.get().getZ());
        assertEquals(5, l.lastPlace.get().getBlockType());
    }
}
