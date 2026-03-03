package com.github.martinambrus.rdforward.e2e.agent;

import com.github.martinambrus.rdforward.e2e.agent.scenario.Scenario;
import com.github.martinambrus.rdforward.e2e.agent.scenario.ScenarioRunner;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * State machine that drives the test scenario from within the client's tick loop.
 * Called on every game tick via ByteBuddy Advice on the tick method.
 *
 * State progression:
 *   INIT -> WAITING_FOR_WORLD -> WAITING_FOR_PLAYER -> STABILIZING
 *        -> RUNNING_SCENARIO -> COMPLETE
 */
public class TickHook {

    public enum State {
        INIT,
        WAITING_FOR_WORLD,
        WAITING_FOR_PLAYER,
        STABILIZING,
        RUNNING_SCENARIO,
        COMPLETE,
        ERROR
    }

    // Frame-stable detection: sample framebuffer hash every
    // FRAME_SAMPLE_INTERVAL ticks; when FRAME_STABLE_COUNT consecutive
    // samples match, all chunk meshes are complete. MIN protects against
    // false positives from loading screens; MAX is a safety net.
    private static final int FRAME_SAMPLE_INTERVAL = 20;  // 1 second between samples
    private static final int FRAME_STABLE_COUNT = 3;       // 3 consecutive matches = stable
    private static final int STABILIZE_MIN = 100;          // 5 seconds minimum
    private static final int STABILIZE_MAX = 1200;         // 60 seconds safety net

    private final GameState gameState;
    private final StatusWriter statusWriter;
    private final ScreenshotCapture screenshotCapture;
    private final InputController inputController;
    private final Scenario scenario;
    private final File statusDir;

    private State state = State.INIT;
    private int tickCount;
    private int stabilizeTicks;
    private final List<String> screenshots = new ArrayList<String>();
    private String error;

    // Frame-stable detection: works on all client versions (LWJGL2 and LWJGL3)
    private long lastFrameHash;
    private int stableFrameCount;

    private ScenarioRunner scenarioRunner;
    private PrintWriter debugLog;

    public TickHook(GameState gameState, StatusWriter statusWriter,
                    ScreenshotCapture screenshotCapture, InputController inputController,
                    Scenario scenario, File statusDir) {
        this.gameState = gameState;
        this.statusWriter = statusWriter;
        this.screenshotCapture = screenshotCapture;
        this.inputController = inputController;
        this.scenario = scenario;
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
                        // Signal to RenderLoopSafetyAdvice that 3D world rendering
                        // is about to start. On 1.17+ Core Profile clients the next
                        // render call would crash the JVM under Mesa llvmpipe, so
                        // the advice switches to permanent skip once it sees this.
                        System.setProperty("mctestagent.world.loaded", "true");
                        McTestAgent.worldLoaded = true;
                        state = State.WAITING_FOR_PLAYER;
                        writeStatus();
                        // For Core Profile clients: TickAdvice checks this flag
                        // after onTick() returns and throws to abort f() before
                        // the render portion executes (would crash on Mesa).
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
                    // Dismiss pause menu overlay from Xvfb focus loss so the
                    // framebuffer is clean before the first scenario screenshot
                    inputController.dismissPauseScreen();
                    stabilizeTicks++;

                    // Debug: log position every 10 ticks during stabilization
                    if (stabilizeTicks == 1 || stabilizeTicks % 10 == 0) {
                        if (debugLog == null) {
                            try {
                                debugLog = new PrintWriter(new FileWriter(
                                        new File(statusDir, "debug_position.log")), true);
                            } catch (Exception ignored) {}
                        }
                        if (debugLog != null) {
                            double[] dpos = gameState.getPlayerPosition();
                            double rawY = gameState.getRawPosY();
                            if (dpos != null) {
                                debugLog.printf("STAB tick=%d/%d normalizedY=%.2f rawY=%.2f X=%.2f Z=%.2f stable=%d%n",
                                        stabilizeTicks, STABILIZE_MAX,
                                        dpos[1], rawY, dpos[0], dpos[2], stableFrameCount);
                            }
                        }
                    }

                    // Frame-stable detection — sample framebuffer hash and wait
                    // for consecutive identical frames (all chunks meshed).
                    // Works on all client versions (LWJGL2 and LWJGL3) since both
                    // expose GL11.glReadPixels via the same API.
                    if (stabilizeTicks >= STABILIZE_MIN
                            && stabilizeTicks % FRAME_SAMPLE_INTERVAL == 0) {
                        int w = gameState.getDisplayWidth();
                        int h = gameState.getDisplayHeight();
                        if (w > 0 && h > 0) {
                            long hash = screenshotCapture.captureFrameHash(w, h);
                            if (hash != 0 && hash == lastFrameHash) {
                                stableFrameCount++;
                            } else {
                                stableFrameCount = 0;
                            }
                            lastFrameHash = hash;
                            if (debugLog != null) {
                                debugLog.printf("FRAME_HASH tick=%d hash=%016x stable=%d/%d%n",
                                        stabilizeTicks, hash, stableFrameCount, FRAME_STABLE_COUNT);
                            }
                        }
                    }

                    // Proceed when N consecutive frames match, or hit the safety
                    // net timeout.
                    boolean stabilized = stableFrameCount >= FRAME_STABLE_COUNT
                            || stabilizeTicks >= STABILIZE_MAX;

                    if (stabilized) {
                        String reason = stableFrameCount >= FRAME_STABLE_COUNT
                                ? "frame-stable (" + stableFrameCount + " consecutive matches)"
                                : "timeout (safety net)";
                        System.out.println("[McTestAgent] Stabilized after "
                                + stabilizeTicks + " ticks (" + reason
                                + "), starting scenario '" + scenario.getName() + "'");
                        scenarioRunner = new ScenarioRunner(scenario, gameState,
                                inputController, screenshotCapture, statusDir);
                        state = State.RUNNING_SCENARIO;
                        writeStatus();
                    } else if (stabilizeTicks <= 5 || stabilizeTicks % 200 == 0) {
                        System.out.println("[McTestAgent] Stabilizing: tick "
                                + stabilizeTicks
                                + " (frame-stable=" + stableFrameCount + "/" + FRAME_STABLE_COUNT + ")");
                    }
                    break;

                case RUNNING_SCENARIO:
                    // Dismiss pause menu overlay from Xvfb focus loss
                    inputController.dismissPauseScreen();
                    // Apply input state before scenario step
                    inputController.applyInputs();

                    // Debug position logging
                    if (debugLog == null) {
                        try {
                            debugLog = new PrintWriter(new FileWriter(
                                    new File(statusDir, "debug_position.log")), true);
                        } catch (Exception ignored) {}
                    }
                    if (debugLog != null) {
                        double[] dpos = gameState.getPlayerPosition();
                        if (dpos != null) {
                            debugLog.printf("tick=%d step=%s Y=%.2f X=%.2f Z=%.2f%n",
                                    tickCount,
                                    scenarioRunner.getCurrentStepDescription(),
                                    dpos[1], dpos[0], dpos[2]);
                        }
                    }

                    ScenarioRunner.RunnerState rs = scenarioRunner.tick();
                    if (rs == ScenarioRunner.RunnerState.COMPLETE) {
                        screenshots.addAll(scenarioRunner.getScreenshots());
                        state = State.COMPLETE;
                        System.out.println("[McTestAgent] Scenario complete at tick " + tickCount);
                    } else if (rs == ScenarioRunner.RunnerState.ERROR) {
                        screenshots.addAll(scenarioRunner.getScreenshots());
                        error = scenarioRunner.getError();
                        state = State.ERROR;
                    }
                    writeStatus();
                    break;

                case COMPLETE:
                case ERROR:
                    // Terminal states — no further action
                    break;
            }
        } catch (Throwable e) {
            error = e.getClass().getName() + ": " + e.getMessage();
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
        if (scenarioRunner != null) {
            statusWriter.write(state.name(), tickCount, screenshots,
                    gameState.getPlayerPosition(), error,
                    scenarioRunner.getCurrentStepDescription(),
                    scenarioRunner.getCurrentStepIndex(),
                    scenarioRunner.getTotalSteps(),
                    scenarioRunner.getResults());
        } else {
            statusWriter.write(state.name(), tickCount, screenshots,
                    gameState.getPlayerPosition(), error);
        }
    }
}
