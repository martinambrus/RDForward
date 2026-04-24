package com.github.martinambrus.rdforward.client.mod.impl;

import com.github.martinambrus.rdforward.api.client.ClientEvents;
import com.github.martinambrus.rdforward.api.client.GameScreen;
import com.github.martinambrus.rdforward.api.client.ScreenManager;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Holds the single active {@link GameScreen} and fires
 * {@link ClientEvents#SCREEN_OPEN} / {@link ClientEvents#SCREEN_CLOSE} on
 * transitions. Opening a new screen closes the previous one first so the
 * pair of events always balances.
 *
 * <p>The GLFW window handle is captured once by the client and passed
 * through every {@link #openScreen(GameScreen)} call.
 */
public final class RDScreenManager implements ScreenManager {

    private static final Logger LOG = Logger.getLogger(RDScreenManager.class.getName());

    private final AtomicLong windowHandle = new AtomicLong(0L);
    private volatile GameScreen active;

    /** Bind the GLFW window handle the screen manager hands to {@link GameScreen#open(long)}. */
    public void setWindow(long window) { windowHandle.set(window); }

    @Override
    public void openScreen(GameScreen screen) {
        if (screen == null) { closeScreen(); return; }
        GameScreen prev = active;
        if (prev != null) {
            safeClose(prev);
            fireClose(prev);
        }
        try {
            screen.open(windowHandle.get());
        } catch (Throwable t) {
            LOG.warning("[ScreenManager] " + screen.getClass().getName() + ".open() threw: " + t);
        }
        active = screen;
        ClientEvents.SCREEN_OPEN.invoker().onOpen(screen);
    }

    @Override
    public void closeScreen() {
        GameScreen prev = active;
        if (prev == null) return;
        active = null;
        safeClose(prev);
        fireClose(prev);
    }

    @Override
    public GameScreen getActiveScreen() { return active; }

    private void safeClose(GameScreen s) {
        try { s.close(); } catch (Throwable t) {
            LOG.warning("[ScreenManager] " + s.getClass().getName() + ".close() threw: " + t);
        }
    }

    private void fireClose(GameScreen s) {
        ClientEvents.SCREEN_CLOSE.invoker().onClose(s);
    }
}
