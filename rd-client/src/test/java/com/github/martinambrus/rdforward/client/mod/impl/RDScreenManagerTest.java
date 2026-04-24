package com.github.martinambrus.rdforward.client.mod.impl;

import com.github.martinambrus.rdforward.api.client.ClientEvents;
import com.github.martinambrus.rdforward.api.client.GameScreen;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RDScreenManagerTest {

    private final List<GameScreen> opens = new ArrayList<>();
    private final List<GameScreen> closes = new ArrayList<>();

    @BeforeEach
    void clear() {
        ClientEvents.SCREEN_OPEN.clearListeners();
        ClientEvents.SCREEN_CLOSE.clearListeners();
        opens.clear();
        closes.clear();
        ClientEvents.SCREEN_OPEN.register(opens::add);
        ClientEvents.SCREEN_CLOSE.register(closes::add);
    }

    @AfterEach
    void reset() {
        ClientEvents.SCREEN_OPEN.clearListeners();
        ClientEvents.SCREEN_CLOSE.clearListeners();
    }

    private static GameScreen noopScreen(AtomicBoolean openedFlag, AtomicBoolean closedFlag) {
        return new GameScreen() {
            @Override public void open(long window) { openedFlag.set(true); }
            @Override public void close() { closedFlag.set(true); }
            @Override public boolean isActive() { return true; }
            @Override public void render(int w, int h) {}
            @Override public boolean handleKey(int k, int s, int a, int m) { return false; }
            @Override public void handleChar(int c) {}
            @Override public boolean handleClick(int b, int a, double x, double y) { return false; }
            @Override public void cleanup() {}
        };
    }

    @Test
    void openScreenFiresOpenAndCallsOpen() {
        RDScreenManager mgr = new RDScreenManager();
        mgr.setWindow(42L);
        AtomicBoolean opened = new AtomicBoolean();
        GameScreen s = noopScreen(opened, new AtomicBoolean());

        mgr.openScreen(s);

        assertTrue(opened.get(), "open() must be called on the new screen");
        assertSame(s, mgr.getActiveScreen());
        assertEquals(List.of(s), opens);
        assertTrue(closes.isEmpty());
    }

    @Test
    void openingSecondScreenClosesFirstFirst() {
        RDScreenManager mgr = new RDScreenManager();
        AtomicBoolean openedA = new AtomicBoolean(), closedA = new AtomicBoolean();
        AtomicBoolean openedB = new AtomicBoolean(), closedB = new AtomicBoolean();
        GameScreen a = noopScreen(openedA, closedA);
        GameScreen b = noopScreen(openedB, closedB);

        mgr.openScreen(a);
        mgr.openScreen(b);

        assertTrue(closedA.get(), "opening a new screen must close the previous one");
        assertTrue(openedB.get());
        assertSame(b, mgr.getActiveScreen());
        assertEquals(List.of(a, b), opens);
        assertEquals(List.of(a), closes, "only the replaced screen should have fired close");
    }

    @Test
    void closeScreenFiresCloseAndClearsActive() {
        RDScreenManager mgr = new RDScreenManager();
        AtomicBoolean closed = new AtomicBoolean();
        GameScreen s = noopScreen(new AtomicBoolean(), closed);

        mgr.openScreen(s);
        mgr.closeScreen();

        assertTrue(closed.get());
        assertNull(mgr.getActiveScreen());
        assertEquals(List.of(s), closes);
    }

    @Test
    void closeWhenNothingActiveIsNoOp() {
        RDScreenManager mgr = new RDScreenManager();
        mgr.closeScreen();
        assertNull(mgr.getActiveScreen());
        assertTrue(closes.isEmpty());
    }

    @Test
    void openScreenNullCallsCloseOnCurrent() {
        RDScreenManager mgr = new RDScreenManager();
        AtomicBoolean closed = new AtomicBoolean();
        GameScreen s = noopScreen(new AtomicBoolean(), closed);
        mgr.openScreen(s);

        mgr.openScreen(null);
        assertTrue(closed.get(), "openScreen(null) should be treated as closeScreen()");
        assertNull(mgr.getActiveScreen());
    }

    @Test
    void throwingOpenStillRegistersScreenAndFiresEvent() {
        RDScreenManager mgr = new RDScreenManager();
        GameScreen throwing = new GameScreen() {
            @Override public void open(long window) { throw new RuntimeException("boom"); }
            @Override public void close() {}
            @Override public boolean isActive() { return true; }
            @Override public void render(int w, int h) {}
            @Override public boolean handleKey(int k, int s, int a, int m) { return false; }
            @Override public void handleChar(int c) {}
            @Override public boolean handleClick(int b, int a, double x, double y) { return false; }
            @Override public void cleanup() {}
        };

        mgr.openScreen(throwing);
        assertSame(throwing, mgr.getActiveScreen(),
                "ScreenManager must not let a throwing open() corrupt its state");
        assertEquals(List.of(throwing), opens);
    }
}
