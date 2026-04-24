package com.github.martinambrus.rdforward.api.client;

/**
 * Controls the currently-open {@link GameScreen}. Opening a new screen
 * closes the previous one. Firing {@code ClientEvents.SCREEN_OPEN} /
 * {@code SCREEN_CLOSE} is the manager's responsibility.
 */
public interface ScreenManager {

    void openScreen(GameScreen screen);

    void closeScreen();

    /** @return the active screen, or {@code null} if none is open. */
    GameScreen getActiveScreen();
}
