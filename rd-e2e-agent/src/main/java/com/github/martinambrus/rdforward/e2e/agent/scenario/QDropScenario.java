package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.McTestAgent;
import com.github.martinambrus.rdforward.e2e.agent.ScreenshotCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests Q-drop (item dropping) and cobblestone replenishment:
 * 1. Verify initial 64 cobblestone
 * 2. Drop one item via InputController.dropCurrentItem()
 * 3. Wait for server replenishment (1s debounce + network)
 * 4. Verify cobblestone back to 64
 * 5. Screenshot
 */
public class QDropScenario implements Scenario {

    @Override
    public String getName() {
        return "q_drop";
    }

    @Override
    public List<ScenarioStep> getSteps() {
        List<ScenarioStep> steps = new ArrayList<ScenarioStep>();
        steps.add(new VerifyInitialInventoryStep());
        steps.add(new DropItemStep());
        steps.add(new WaitReplenishmentStep());
        steps.add(new VerifyReplenishedStep());
        steps.add(new ScreenshotStep());
        return steps;
    }

    private static class VerifyInitialInventoryStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "verify_initial_inventory";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            int total = gs.getTotalCobblestone();
            int expected = McTestAgent.isCreativeMode ? 1 : 64;
            System.out.println("[McTestAgent] Initial cobblestone: " + total
                    + " (expected >=" + expected + ")");
            if (total < expected) {
                throw new RuntimeException("Expected " + expected + " cobblestone initially, found " + total);
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    private static class DropItemStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "drop_item";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            input.dropCurrentItem();
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    private static class WaitReplenishmentStep implements ScenarioStep {
        private int lastTotal = -1;
        private int stableTicks;

        @Override
        public String getDescription() {
            return "wait_replenishment";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            // Poll inventory each tick. Replenishment is complete when
            // the cobblestone total stops changing for 5 consecutive ticks.
            // If no inventory (RD), pass through immediately.
            int total = gs.getTotalCobblestone();
            if (total < 0) return true;
            if (total == lastTotal) {
                stableTicks++;
            } else {
                stableTicks = 0;
            }
            lastTotal = total;
            return stableTicks >= 5;
        }

        @Override
        public int getTimeoutTicks() {
            return 200; // 10s safety net for slow replenishment
        }
    }

    private static class VerifyReplenishedStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "verify_replenished";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            int total = gs.getTotalCobblestone();
            int expected = McTestAgent.isCreativeMode ? 1 : 64;
            System.out.println("[McTestAgent] Cobblestone after replenish: " + total
                    + " (expected >=" + expected + ")");
            if (total < expected) {
                throw new RuntimeException("Expected " + expected
                        + " cobblestone after replenishment, found " + total);
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    private static class ScreenshotStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "screenshot";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            File file = new File(statusDir, "q_drop_complete.png");
            capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }
}
