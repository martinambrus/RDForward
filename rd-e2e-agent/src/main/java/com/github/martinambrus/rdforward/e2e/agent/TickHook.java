package com.github.martinambrus.rdforward.e2e.agent;

import com.github.martinambrus.rdforward.e2e.agent.scenario.Scenario;
import com.github.martinambrus.rdforward.e2e.agent.scenario.ScenarioRunner;

import java.io.File;
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
                    // Dismiss pause menu overlay from Xvfb focus loss so the
                    // framebuffer is clean before the first scenario screenshot
                    inputController.dismissPauseScreen();
                    stabilizeTicks++;
                    if (stabilizeTicks >= STABILIZE_TICKS) {
                        System.out.println("[McTestAgent] Stabilized after "
                                + STABILIZE_TICKS + " ticks, starting scenario '"
                                + scenario.getName() + "'");
                        scenarioRunner = new ScenarioRunner(scenario, gameState,
                                inputController, screenshotCapture, statusDir);
                        state = State.RUNNING_SCENARIO;
                        writeStatus();
                    }
                    break;

                case RUNNING_SCENARIO:
                    // Dismiss pause menu overlay from Xvfb focus loss
                    inputController.dismissPauseScreen();
                    // Apply input state before scenario step
                    inputController.applyInputs();

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
