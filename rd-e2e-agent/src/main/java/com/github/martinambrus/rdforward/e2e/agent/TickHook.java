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

    private static final int STABILIZE_TICKS = 100;

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
                    if (stabilizeTicks == 1 || stabilizeTicks % 10 == 0
                            || stabilizeTicks >= STABILIZE_TICKS) {
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
                                debugLog.printf("STAB tick=%d/%d normalizedY=%.2f rawY=%.2f X=%.2f Z=%.2f%n",
                                        stabilizeTicks, STABILIZE_TICKS,
                                        dpos[1], rawY, dpos[0], dpos[2]);
                            }
                        }
                    }

                    if (stabilizeTicks >= STABILIZE_TICKS) {
                        System.out.println("[McTestAgent] Stabilized after "
                                + stabilizeTicks + " ticks, starting scenario '"
                                + scenario.getName() + "'");
                        scenarioRunner = new ScenarioRunner(scenario, gameState,
                                inputController, screenshotCapture, statusDir);
                        state = State.RUNNING_SCENARIO;
                        writeStatus();
                    } else if (stabilizeTicks <= 5 || stabilizeTicks % 200 == 0) {
                        System.out.println("[McTestAgent] Stabilizing: tick "
                                + stabilizeTicks + "/" + STABILIZE_TICKS);
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
