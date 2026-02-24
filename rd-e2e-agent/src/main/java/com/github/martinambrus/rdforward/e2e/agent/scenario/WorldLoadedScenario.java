package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.ScreenshotCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Default scenario: capture a "world_loaded.png" screenshot.
 * Preserves Phase 1 (AlphaLoginScreenshotTest) behavior.
 */
public class WorldLoadedScenario implements Scenario {

    @Override
    public String getName() {
        return "world_loaded";
    }

    @Override
    public List<ScenarioStep> getSteps() {
        List<ScenarioStep> steps = new ArrayList<ScenarioStep>();
        steps.add(new CaptureStep());
        return steps;
    }

    private static class CaptureStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "capture_world_loaded";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            int w = gs.getDisplayWidth();
            int h = gs.getDisplayHeight();
            File file = new File(statusDir, "world_loaded.png");
            boolean ok = capture.capture(w, h, file);
            if (!ok) {
                throw new RuntimeException("Screenshot capture failed");
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 100; // should be instant
        }
    }
}
