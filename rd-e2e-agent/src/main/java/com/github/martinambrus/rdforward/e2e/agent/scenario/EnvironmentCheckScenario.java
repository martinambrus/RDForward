package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.McTestAgent;
import com.github.martinambrus.rdforward.e2e.agent.ScreenshotCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifies the spawn environment:
 * 1. Player is on a grass surface (block below = grass ID 2, block at feet = air ID 0)
 * 2. Surroundings are clear (air above ground in a 5-block radius)
 * 3. Inventory has 64 cobblestone (item ID 4)
 * 4. Capture screenshot
 *
 * Results are written to the ScenarioRunner for status reporting.
 */
public class EnvironmentCheckScenario implements Scenario {

    @Override
    public String getName() {
        return "environment_check";
    }

    @Override
    public List<ScenarioStep> getSteps() {
        List<ScenarioStep> steps = new ArrayList<ScenarioStep>();
        steps.add(new CheckPositionStep());
        steps.add(new CheckSurroundingsStep());
        steps.add(new CheckInventoryStep());
        steps.add(new CaptureStep());
        return steps;
    }

    private static class CheckPositionStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "check_position";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            int blockBelow = gs.getBlockBelowFeet();
            int blockAtFeet = gs.getBlockAtFeet();

            System.out.println("[McTestAgent] Environment: blockBelow=" + blockBelow
                    + " blockAtFeet=" + blockAtFeet);

            // Grass = 2, Air = 0
            if (blockBelow != 2) {
                throw new RuntimeException("Expected grass (2) below feet, got " + blockBelow);
            }
            if (blockAtFeet != 0) {
                throw new RuntimeException("Expected air (0) at feet, got " + blockAtFeet);
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 100;
        }
    }

    private static class CheckSurroundingsStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "check_surroundings";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            double[] pos = gs.getPlayerPosition();
            if (pos == null) throw new RuntimeException("No player position");

            int px = (int) Math.floor(pos[0]);
            // Eye-level Y -> feet Y -> block below feet
            int groundY = (int) Math.floor(pos[1] - (double) 1.62f) - 1;
            int pz = (int) Math.floor(pos[2]);

            // Scan 5 blocks each direction: above ground should be air
            int issues = 0;
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    // Check 2 blocks above ground (feet + head level)
                    for (int dy = 1; dy <= 2; dy++) {
                        int blockId = gs.getBlockId(px + dx, groundY + dy, pz + dz);
                        if (blockId != 0) { // not air
                            issues++;
                        }
                    }
                }
            }

            System.out.println("[McTestAgent] Surroundings check: " + issues + " non-air blocks found");
            if (issues > 0) {
                throw new RuntimeException("Surroundings not clear: " + issues + " non-air blocks in radius");
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 100;
        }
    }

    private static class CheckInventoryStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "check_inventory";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            int[][] slots = gs.getInventorySlots();
            if (slots == null) throw new RuntimeException("Could not read inventory");

            // Count cobblestone (item ID 4)
            int cobbleCount = 0;
            for (int[] slot : slots) {
                if (slot[0] == 4) {
                    cobbleCount += slot[1];
                }
            }

            int expected = McTestAgent.isCreativeMode ? 1 : 64;
            System.out.println("[McTestAgent] Inventory cobblestone: " + cobbleCount
                    + " (expected >=" + expected + ")");
            if (cobbleCount < expected) {
                throw new RuntimeException("Expected " + expected + " cobblestone, found " + cobbleCount);
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 100;
        }
    }

    private static class CaptureStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "screenshot";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            int w = gs.getDisplayWidth();
            int h = gs.getDisplayHeight();
            File file = new File(statusDir, "environment_check.png");
            boolean ok = capture.capture(w, h, file);
            if (!ok) {
                throw new RuntimeException("Screenshot capture failed");
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 100;
        }
    }
}
