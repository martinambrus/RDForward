package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitEventAdapter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerMoveEventFromToTest {

    @BeforeEach
    void clear() {
        ServerEvents.clearAll();
        BukkitEventAdapter.clearAll();
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        BukkitEventAdapter.clearAll();
    }

    static final class MoveTracker implements Listener {
        volatile int hits;
        volatile double fromX, fromY, fromZ;
        volatile double toX, toY, toZ;

        @EventHandler
        public void onMove(PlayerMoveEvent e) {
            hits++;
            fromX = e.getFrom().getX();
            fromY = e.getFrom().getY();
            fromZ = e.getFrom().getZ();
            toX = e.getTo().getX();
            toY = e.getTo().getY();
            toZ = e.getTo().getZ();
        }
    }

    @Test
    void firstMoveHasSameFromAndTo() {
        MoveTracker tracker = new MoveTracker();
        BukkitEventAdapter.register(tracker, "test");

        // First move — no previous position stored
        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove("alice",
                (short) 320, (short) 640, (short) 960, (byte) 0, (byte) 0);

        assertEquals(1, tracker.hits);
        assertEquals(10.0, tracker.toX, 0.01); // 320 / 32
        assertEquals(20.0, tracker.toY, 0.01);
        assertEquals(30.0, tracker.toZ, 0.01);
        // First move: from == to
        assertEquals(tracker.toX, tracker.fromX, 0.01);
        assertEquals(tracker.toY, tracker.fromY, 0.01);
        assertEquals(tracker.toZ, tracker.fromZ, 0.01);
    }

    @Test
    void subsequentMoveHasDifferentFromAndTo() {
        MoveTracker tracker = new MoveTracker();
        BukkitEventAdapter.register(tracker, "test");

        // First move at 10, 20, 30
        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove("alice",
                (short) 320, (short) 640, (short) 960, (byte) 0, (byte) 0);

        // Second move at 40, 50, 60
        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove("alice",
                (short) 1280, (short) 1600, (short) 1920, (byte) 0, (byte) 0);

        assertEquals(2, tracker.hits);
        // from = previous position (10, 20, 30)
        assertEquals(10.0, tracker.fromX, 0.01);
        assertEquals(20.0, tracker.fromY, 0.01);
        assertEquals(30.0, tracker.fromZ, 0.01);
        // to = current position (40, 50, 60)
        assertEquals(40.0, tracker.toX, 0.01);
        assertEquals(50.0, tracker.toY, 0.01);
        assertEquals(60.0, tracker.toZ, 0.01);
    }

    @Test
    void differentPlayersTrackSeparately() {
        MoveTracker tracker = new MoveTracker();
        BukkitEventAdapter.register(tracker, "test");

        // Alice at 10, 20, 30
        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove("alice",
                (short) 320, (short) 640, (short) 960, (byte) 0, (byte) 0);
        // Bob at 100, 200, 300
        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove("bob",
                (short) 3200, (short) 6400, (short) 9600, (byte) 0, (byte) 0);

        // Alice moves to 15, 25, 35
        ServerEvents.PLAYER_MOVE.invoker().onPlayerMove("alice",
                (short) 480, (short) 800, (short) 1120, (byte) 0, (byte) 0);

        assertEquals(3, tracker.hits);
        // Alice's from should be her own previous (10, 20, 30), not Bob's
        assertEquals(10.0, tracker.fromX, 0.01);
        assertEquals(20.0, tracker.fromY, 0.01);
        assertEquals(30.0, tracker.fromZ, 0.01);
        assertEquals(15.0, tracker.toX, 0.01);
        assertEquals(25.0, tracker.toY, 0.01);
        assertEquals(35.0, tracker.toZ, 0.01);
    }
}
