package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.ScreenshotCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Executes scenario steps sequentially, tracking progress and results.
 */
public class ScenarioRunner {

    public enum RunnerState {
        RUNNING, COMPLETE, ERROR
    }

    private final Scenario scenario;
    private final GameState gameState;
    private final InputController inputController;
    private final ScreenshotCapture screenshotCapture;
    private final File statusDir;

    private final List<ScenarioStep> steps;
    private int currentStepIndex;
    private int stepTickCount;
    private RunnerState state = RunnerState.RUNNING;
    private String error;

    private final List<String> screenshots = new ArrayList<String>();
    private final Map<String, String> results = new LinkedHashMap<String, String>();

    public ScenarioRunner(Scenario scenario, GameState gameState,
                          InputController inputController,
                          ScreenshotCapture screenshotCapture, File statusDir) {
        this.scenario = scenario;
        this.gameState = gameState;
        this.inputController = inputController;
        this.screenshotCapture = screenshotCapture;
        this.statusDir = statusDir;
        this.steps = scenario.getSteps();
    }

    /**
     * Execute one tick of the current step. Returns the runner state.
     */
    public RunnerState tick() {
        if (state != RunnerState.RUNNING) return state;
        if (currentStepIndex >= steps.size()) {
            state = RunnerState.COMPLETE;
            return state;
        }

        ScenarioStep step = steps.get(currentStepIndex);
        stepTickCount++;

        // Check timeout
        int timeout = step.getTimeoutTicks();
        if (timeout > 0 && stepTickCount > timeout) {
            error = "Step '" + step.getDescription() + "' timed out after "
                    + stepTickCount + " ticks (limit: " + timeout + ")";
            state = RunnerState.ERROR;
            System.err.println("[McTestAgent] " + error);
            return state;
        }

        try {
            boolean done = step.tick(gameState, inputController, screenshotCapture, statusDir);
            if (done) {
                System.out.println("[McTestAgent] Step '" + step.getDescription()
                        + "' completed in " + stepTickCount + " ticks");

                // Auto-screenshot after every completed step (testing-todo step -1)
                autoScreenshot(step.getDescription(), currentStepIndex);

                currentStepIndex++;
                stepTickCount = 0;

                if (currentStepIndex >= steps.size()) {
                    state = RunnerState.COMPLETE;
                    System.out.println("[McTestAgent] Scenario '"
                            + scenario.getName() + "' complete");
                }
            }
        } catch (Exception e) {
            error = "Step '" + step.getDescription() + "': "
                    + e.getClass().getSimpleName() + ": " + e.getMessage();
            state = RunnerState.ERROR;
            System.err.println("[McTestAgent] " + error);
            e.printStackTrace();
        }

        return state;
    }

    public RunnerState getState() {
        return state;
    }

    public String getError() {
        return error;
    }

    public String getCurrentStepDescription() {
        if (currentStepIndex < steps.size()) {
            return steps.get(currentStepIndex).getDescription();
        }
        return "done";
    }

    public int getCurrentStepIndex() {
        return currentStepIndex;
    }

    public int getTotalSteps() {
        return steps.size();
    }

    public List<String> getScreenshots() {
        return screenshots;
    }

    public void addScreenshot(String name) {
        screenshots.add(name);
    }

    public Map<String, String> getResults() {
        return results;
    }

    public void putResult(String key, String value) {
        results.put(key, value);
    }

    /**
     * Capture a screenshot after each step completes.
     * Naming: {scenarioName}_step{index}_{sanitizedDescription}.png
     */
    private void autoScreenshot(String stepDescription, int stepIndex) {
        try {
            int w = gameState.getDisplayWidth();
            int h = gameState.getDisplayHeight();
            if (w <= 0 || h <= 0) return;

            String safeName = stepDescription
                    .replaceAll("[^a-zA-Z0-9_]", "_")
                    .replaceAll("_+", "_")
                    .toLowerCase();
            if (safeName.length() > 60) {
                safeName = safeName.substring(0, 60);
            }
            String filename = scenario.getName() + "_step" + stepIndex + "_" + safeName + ".png";
            File ssFile = new File(statusDir, filename);
            if (screenshotCapture.capture(w, h, ssFile)) {
                screenshots.add(ssFile.getName());
                System.out.println("[McTestAgent] Auto-screenshot: " + filename);
            }
        } catch (Exception ex) {
            System.err.println("[McTestAgent] Auto-screenshot failed: " + ex.getMessage());
        }
    }
}
