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
        steps.add(new SetCameraStep());
        steps.add(new CaptureStep());
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
     * Set the camera to look at the horizon so terrain is visible in the screenshot.
     * RubyDung's mouse handler can accumulate deltas under Xvfb that point the
     * camera at the sky before suppressMouseLook() kicks in. This step explicitly
     * resets the camera direction.
     */
    private static class SetCameraStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "set_camera";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            // Look north (yaw=0 for RD, yaw=180 for Alpha) with a slight
            // downward pitch (10°) to show terrain and horizon.
            input.setLookDirection(input.isRubyDung() ? 0f : 180f, 10f);
            ticks++;
            // Hold for 2 ticks so the renderer picks up the new direction.
            return ticks >= 2;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
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
