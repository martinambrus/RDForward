package com.github.martinambrus.rdforward.e2e.agent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * State machine that drives the test scenario from within the client's tick loop.
 * Called on every game tick via ByteBuddy Advice on the tick method.
 *
 * State progression:
 *   INIT -> WAITING_FOR_WORLD -> WAITING_FOR_PLAYER -> STABILIZING -> CAPTURING -> COMPLETE
 */
public class TickHook {

    public enum State {
        INIT,
        WAITING_FOR_WORLD,
        WAITING_FOR_PLAYER,
        STABILIZING,
        CAPTURING,
        COMPLETE,
        ERROR
    }

    private static final int STABILIZE_TICKS = 100;

    private final GameState gameState;
    private final StatusWriter statusWriter;
    private final ScreenshotCapture screenshotCapture;
    private final File statusDir;

    private State state = State.INIT;
    private int tickCount;
    private int stabilizeTicks;
    private final List<String> screenshots = new ArrayList<String>();
    private String error;

    public TickHook(GameState gameState, StatusWriter statusWriter,
                    ScreenshotCapture screenshotCapture, File statusDir) {
        this.gameState = gameState;
        this.statusWriter = statusWriter;
        this.screenshotCapture = screenshotCapture;
        this.statusDir = statusDir;
    }

    /**
     * Called on every game tick (from Advice on the tick method).
     * Drives the state machine forward.
     */
    public void onTick() {
        tickCount++;

        try {
            if (tickCount <= 5 || tickCount % 200 == 0) {
                System.out.println("[McTestAgent] Tick " + tickCount + " state=" + state
                        + " world=" + (gameState.getWorld() != null)
                        + " player=" + (gameState.getPlayer() != null));
            }

            switch (state) {
                case INIT:
                    state = State.WAITING_FOR_WORLD;
                    writeStatus();
                    break;

                case WAITING_FOR_WORLD:
                    if (gameState.getWorld() != null) {
                        System.out.println("[McTestAgent] World loaded at tick " + tickCount);
                        state = State.WAITING_FOR_PLAYER;
                        writeStatus();
                    }
                    break;

                case WAITING_FOR_PLAYER:
                    if (gameState.getPlayer() != null) {
                        System.out.println("[McTestAgent] Player spawned at tick " + tickCount);
                        double[] pos = gameState.getPlayerPosition();
                        if (pos != null) {
                            System.out.println("[McTestAgent] Position: "
                                    + pos[0] + ", " + pos[1] + ", " + pos[2]);
                        }
                        state = State.STABILIZING;
                        stabilizeTicks = 0;
                        writeStatus();
                    }
                    break;

                case STABILIZING:
                    stabilizeTicks++;
                    if (stabilizeTicks >= STABILIZE_TICKS) {
                        System.out.println("[McTestAgent] Stabilized after "
                                + STABILIZE_TICKS + " ticks, capturing screenshot");
                        state = State.CAPTURING;
                        writeStatus();
                    }
                    break;

                case CAPTURING:
                    int w = gameState.getDisplayWidth();
                    int h = gameState.getDisplayHeight();
                    File screenshotFile = new File(statusDir, "world_loaded.png");
                    boolean ok = screenshotCapture.capture(w, h, screenshotFile);
                    if (ok) {
                        screenshots.add("world_loaded.png");
                        state = State.COMPLETE;
                        System.out.println("[McTestAgent] Capture complete at tick " + tickCount);
                    } else {
                        error = "Screenshot capture failed";
                        state = State.ERROR;
                    }
                    writeStatus();
                    break;

                case COMPLETE:
                case ERROR:
                    // Terminal states — no further action
                    break;
            }
        } catch (Exception e) {
            error = e.getClass().getSimpleName() + ": " + e.getMessage();
            state = State.ERROR;
            System.err.println("[McTestAgent] Error in tick hook: " + error);
            e.printStackTrace();
            writeStatus();
        }
    }

    public State getState() {
        return state;
    }

    private void writeStatus() {
        statusWriter.write(state.name(), tickCount, screenshots,
                gameState.getPlayerPosition(), error);
    }
}
