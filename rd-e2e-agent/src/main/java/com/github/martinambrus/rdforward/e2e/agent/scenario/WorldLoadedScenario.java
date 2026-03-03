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
        steps.add(new SetCameraAndCaptureStep());
        return steps;
    }

    /**
     * Wait for chunk meshes to finish building before capture.
     * Pre-LWJGL3: no extra wait needed (chunks are loaded by stabilization).
     * LWJGL3: fixed 60-tick (3 second) wait for chunk mesh building.
     */
    private static class WaitRenderStep implements ScenarioStep {
        private int ticks;

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

            // LWJGL3: wait 60 ticks (3 seconds) for chunk mesh building
            return ticks >= 60;
        }

        @Override
        public int getTimeoutTicks() {
            return 200;
        }
    }

    /**
     * Set the camera to look at the horizon and capture the screenshot.
     * Sets camera direction every tick to ensure it sticks, then captures after
     * enough wall time for chunk rendering and render cycles.
     *
     * RubyDung needs extra time for chunk mesh building after spawn.
     * Alpha/Beta clients render chunks synchronously during stabilization,
     * but still need a few ticks for the camera direction to take effect
     * (the game loop runs all ticks before rendering, so the framebuffer
     * captured by glReadPixels reflects the previous render cycle).
     */
    private static class SetCameraAndCaptureStep implements ScenarioStep {
        private int ticks;
        private long startTimeMs;

        @Override
        public String getDescription() {
            return "set_camera_and_capture";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            if (ticks == 0) startTimeMs = System.currentTimeMillis();
            ticks++;
            // Look north (yaw=0 for RD, yaw=180 for Alpha) with a slight
            // downward pitch (10°) to show terrain and horizon.
            input.setLookDirection(input.isRubyDung() ? 0f : 180f, 10f);

            // RubyDung needs wall-time for chunk mesh building.
            // Alpha/Beta need at least a few ticks for render cycle.
            if (input.isRubyDung()) {
                long elapsed = System.currentTimeMillis() - startTimeMs;
                if (elapsed < 3000) return false; // 3 seconds for chunk rendering
            } else {
                if (ticks < 10) return false;
            }

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
            return 400; // up to ~7 seconds
        }
    }
}
