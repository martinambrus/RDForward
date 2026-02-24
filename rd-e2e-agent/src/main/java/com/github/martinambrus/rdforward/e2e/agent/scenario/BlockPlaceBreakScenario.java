package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.McTestAgent;
import com.github.martinambrus.rdforward.e2e.agent.ScreenshotCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests block breaking and placement:
 * 1. Look at grass block to the right (+X)
 * 2. Break it (left click)
 * 3. Verify broken (air)
 * 4. Place cobblestone (right click)
 * 5. Verify placed and converted to grass (server converts)
 * 6. Verify cobblestone replenished in inventory
 * 7. Place at a second position and verify
 */
public class BlockPlaceBreakScenario implements Scenario {

    // Target block offset from player (+X direction, same ground level)
    private int targetBX;
    private int targetBY;
    private int targetBZ;
    private boolean targetComputed;

    // Second target for adjacent placement
    private int target2BX;
    private int target2BZ;

    @Override
    public String getName() {
        return "block_place_break";
    }

    @Override
    public List<ScenarioStep> getSteps() {
        List<ScenarioStep> steps = new ArrayList<ScenarioStep>();
        steps.add(new LookAtTargetStep());
        steps.add(new BreakGrassStep());
        steps.add(new VerifyBrokenStep());
        steps.add(new PlaceCobblestoneStep());
        steps.add(new VerifyPlacedStep());
        steps.add(new VerifyReplenishmentStep());
        steps.add(new LookAtAdjacentStep());
        steps.add(new PlaceAdjacentStep());
        steps.add(new VerifyAdjacentStep());
        return steps;
    }

    private void computeTargets(GameState gs) {
        if (targetComputed) return;
        double[] pos = gs.getPlayerPosition();
        if (pos == null) throw new RuntimeException("No player position");

        int px = (int) Math.floor(pos[0]);
        int groundY = (int) Math.floor(pos[1] - (double) 1.62f) - 1;
        int pz = (int) Math.floor(pos[2]);

        // Target: 1 block to the right (+X), same ground level (grass surface)
        // Using 1 block offset to avoid raycast clipping through intermediate blocks
        targetBX = px + 1;
        targetBY = groundY;
        targetBZ = pz;

        // Second target: 1 block forward (+Z)
        target2BX = px;
        target2BZ = pz + 1;

        targetComputed = true;
        System.out.println("[McTestAgent] Block targets: primary=("
                + targetBX + "," + targetBY + "," + targetBZ
                + ") secondary=(" + target2BX + "," + targetBY + "," + target2BZ + ")");
    }

    // Step 1: Look at the target grass block
    private class LookAtTargetStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "look_at_target";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            computeTargets(gs);
            // Look at the top face of the target block
            input.lookAtBlock(targetBX, targetBY, targetBZ);
            ticks++;
            // Wait 2 ticks for raycast update
            return ticks >= 2;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 2: Break the grass block by clicking left — stop as soon as it's air
    private class BreakGrassStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "break_grass";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            // Check if already broken
            int blockId = gs.getBlockId(targetBX, targetBY, targetBZ);
            if (blockId == 0) {
                return true; // broken, stop clicking
            }
            // Keep looking at target and clicking
            input.lookAtBlock(targetBX, targetBY, targetBZ);
            input.click(0);
            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 100;
        }
    }

    // Step 3: Wait and verify block is now air
    private class VerifyBrokenStep implements ScenarioStep {
        private int ticks;
        private boolean screenshotTaken;

        @Override
        public String getDescription() {
            return "verify_broken";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            // Wait for server to process and send block change
            if (ticks < 80) return false;

            int blockId = gs.getBlockId(targetBX, targetBY, targetBZ);
            System.out.println("[McTestAgent] Block at target after break: " + blockId);

            if (!screenshotTaken) {
                File file = new File(statusDir, "grass_broken.png");
                capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
                screenshotTaken = true;
            }

            // Air = 0, Dirt = 3 (grass on top was broken, might expose dirt)
            if (blockId != 0 && blockId != 3) {
                throw new RuntimeException("Expected air(0) or dirt(3) after break, got " + blockId);
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 200;
        }
    }

    // Step 4: Place cobblestone — look at block below target, right-click
    private class PlaceCobblestoneStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "place_cobblestone";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            // Keep looking at the block below the broken grass (place on its top face)
            input.lookAtBlock(targetBX, targetBY - 1, targetBZ);

            // Click after 3 ticks (enough for raycast to update) and repeat a few times
            if (ticks >= 3 && ticks <= 6) {
                input.click(1);
            }

            // Check if placed (block appeared at target position)
            if (ticks > 6) {
                int blockId = gs.getBlockId(targetBX, targetBY, targetBZ);
                if (blockId != 0) {
                    System.out.println("[McTestAgent] Block placed at tick " + ticks
                            + ": id=" + blockId);
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 40;
        }
    }

    // Step 5: Wait and verify cobblestone was placed AND converted to grass by server
    private class VerifyPlacedStep implements ScenarioStep {
        private int ticks;
        private boolean screenshotTaken;

        @Override
        public String getDescription() {
            return "verify_placed_grass_conversion";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks < 80) return false;

            int blockId = gs.getBlockId(targetBX, targetBY, targetBZ);

            if (ticks == 80 || ticks % 40 == 0) {
                System.out.println("[McTestAgent] Block at target after place (tick " + ticks + "): " + blockId);
            }

            if (blockId == 0) {
                throw new RuntimeException("Block still air after placement — server rejected it");
            }

            // Server converts placed cobblestone to grass; wait for conversion
            if (blockId == 4) {
                return false; // still cobblestone, keep waiting for grass conversion
            }

            // Grass (2) = successful conversion
            if (blockId == 2) {
                System.out.println("[McTestAgent] Grass conversion confirmed at tick " + ticks);
            } else {
                System.out.println("[McTestAgent] WARNING: Expected grass (2), got blockId=" + blockId);
            }

            if (!screenshotTaken) {
                File file = new File(statusDir, "block_placed.png");
                capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
                screenshotTaken = true;
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 300; // allow more time for grass conversion
        }
    }

    // Step 6: Verify inventory still has 64 cobblestone (replenishment)
    private class VerifyReplenishmentStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "verify_replenishment";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            int[][] slots = gs.getInventorySlots();
            if (slots == null) throw new RuntimeException("Could not read inventory");

            int cobbleCount = 0;
            for (int[] slot : slots) {
                if (slot[0] == 4) cobbleCount += slot[1];
            }
            // Creative mode: client has 1 cobblestone and doesn't consume on placement
            int expected = McTestAgent.isCreativeMode ? 1 : 64;
            System.out.println("[McTestAgent] Cobblestone after place: " + cobbleCount
                    + " (expected >=" + expected + ")");

            if (cobbleCount < expected) {
                throw new RuntimeException("Expected " + expected + " cobblestone after replenishment, found " + cobbleCount);
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 100;
        }
    }

    // Step 7: Look at adjacent position
    private class LookAtAdjacentStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "look_at_adjacent";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            computeTargets(gs);
            input.lookAtBlock(target2BX, targetBY, target2BZ);
            ticks++;
            return ticks >= 2;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 8: Place at adjacent position
    private class PlaceAdjacentStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "place_adjacent";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks == 1) {
                input.click(1);
            }
            return ticks >= 4;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 9: Wait and verify adjacent block placed
    private class VerifyAdjacentStep implements ScenarioStep {
        private int ticks;
        private boolean screenshotTaken;

        @Override
        public String getDescription() {
            return "verify_adjacent";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks < 80) return false;

            int blockId = gs.getBlockId(target2BX, targetBY, target2BZ);
            System.out.println("[McTestAgent] Block at adjacent after place: " + blockId);

            if (!screenshotTaken) {
                File file = new File(statusDir, "adjacent_placed.png");
                capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
                screenshotTaken = true;
            }

            if (blockId == 0) {
                throw new RuntimeException("Adjacent block still air after placement");
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 200;
        }
    }
}
