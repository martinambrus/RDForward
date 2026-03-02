package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.McTestAgent;
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
        steps.add(new WaitRenderStep());
        steps.add(new CaptureStep());
        return steps;
    }

    /**
     * Wait for chunk meshes to finish building before capture.
     * LWJGL3 clients: uses frame-stable detection (consecutive identical
     * framebuffer hashes) instead of a fixed timeout. This adapts to
     * any CPU speed and parallel test load.
     */
    private static class WaitRenderStep implements ScenarioStep {
        private int ticks;
        private long lastHash;
        private int stableCount;

        @Override
        public String getDescription() {
            return "wait_render";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            boolean isLwjgl3 = McTestAgent.mappings != null && McTestAgent.mappings.isLwjgl3();
            if (!isLwjgl3) return true; // pre-LWJGL3: no extra wait needed

            // Sample every 20 ticks after minimum 20 ticks
            if (ticks >= 20 && ticks % 20 == 0) {
                int w = gs.getDisplayWidth();
                int h = gs.getDisplayHeight();
                if (w > 0 && h > 0) {
                    long hash = capture.captureFrameHash(w, h);
                    if (hash != 0 && hash == lastHash) {
                        stableCount++;
                    } else {
                        stableCount = 0;
                    }
                    lastHash = hash;
                }
            }
            return stableCount >= 3;
        }

        @Override
        public int getTimeoutTicks() {
            return 1200; // 60 second safety net
        }
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
