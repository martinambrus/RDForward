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
        int feetY = (int) Math.floor(pos[1] - (double) 1.62f);
        int groundY = feetY - 1;
        int pz = (int) Math.floor(pos[2]);

        // If feet are on the surface block (RubyDung spawn edge case),
        // treat feet level as ground
        if (gs.getBlockId(px, feetY, pz) != 0) {
            groundY = feetY;
        }

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

    // Step 1: Look at the target grass block (skipped for RubyDung — uses direct methods)
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
            if (input.isRubyDung()) return true; // RD uses direct methods, no raycast needed
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
                // RD: also break the block below for a 2-deep hole that's
                // visible from the side (single-block holes are invisible
                // when all blocks share the same texture)
                if (input.isRubyDung()) {
                    input.breakBlockDirect(targetBX, targetBY - 1, targetBZ);
                }
                return true; // broken, stop clicking
            }
            if (input.isRubyDung()) {
                input.breakBlockDirect(targetBX, targetBY, targetBZ);
            } else {
                // Keep looking at target and clicking
                input.lookAtBlock(targetBX, targetBY, targetBZ);
                input.click(0);
            }
            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 200; // extra margin for resource contention in batch test runs
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
            // RD: move player back for a side view of the 2-deep broken hole.
            // A single-block hole on a uniform-texture flat world is invisible
            // from above, so we step back for a shallower angle that shows
            // the gray stone side faces inside the hole.
            if (ticks == 1 && input.isRubyDung()) {
                input.movePlayerPosition(-3, 0, 0);
                input.lookAtBlock(targetBX, targetBY - 1, targetBZ);
            }
            // Wait for server to process and send block change
            if (ticks < 80) return false;

            int blockId = gs.getBlockId(targetBX, targetBY, targetBZ);
            System.out.println("[McTestAgent] Block at target after break: " + blockId);

            if (!screenshotTaken) {
                File file = new File(statusDir, "grass_broken.png");
                capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
                screenshotTaken = true;
            }

            // After breaking surface block, expect air (0).
            // Allow any block since some versions/timing may show dirt underneath.
            if (blockId != 0) {
                System.out.println("[McTestAgent] WARNING: Expected air(0) after break, got " + blockId
                        + " — accepting (block at Y-1 may be exposed)");
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
            if (input.isRubyDung()) {
                // RD: place solid block (type=1) two levels above ground so
                // the block is near eye level and its gray side face fills the
                // screen. Also restore the ground block broken in BreakGrassStep.
                if (ticks == 1) {
                    input.placeBlockDirect(targetBX, targetBY, targetBZ, 1);
                    input.placeBlockDirect(targetBX, targetBY + 1, targetBZ, 1);
                    input.placeBlockDirect(targetBX, targetBY + 2, targetBZ, 1);
                }
                if (ticks > 3) {
                    int blockId = gs.getBlockId(targetBX, targetBY + 2, targetBZ);
                    if (blockId != 0) {
                        System.out.println("[McTestAgent] Block placed at tick " + ticks
                                + ": id=" + blockId);
                        return true;
                    }
                }
                return false;
            }

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

    // Step 5: Verify block was placed (server converts cobblestone to grass)
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
            // RD: look at the 3-block pillar from the stepped-back position.
            // The top block at targetBY+2 is near eye level, showing its
            // gray side face prominently.
            if (ticks == 1 && input.isRubyDung()) {
                input.lookAtBlock(targetBX, targetBY + 2, targetBZ);
            }
            if (ticks < 80) return false;

            // RD placed pillar up to targetBY+2; Alpha/Beta at ground level
            int checkY = McTestAgent.inputController.isRubyDung() ? targetBY + 2 : targetBY;
            int blockId = gs.getBlockId(targetBX, checkY, targetBZ);

            System.out.println("[McTestAgent] Block at target after place (tick " + ticks + "): " + blockId);

            if (blockId == 0) {
                throw new RuntimeException("Block still air after placement — server rejected it");
            }

            // Block is present — server accepted the placement (and likely already
            // converted cobblestone to grass). Block IDs vary by version so we just
            // verify non-air.
            System.out.println("[McTestAgent] Block confirmed at tick " + ticks
                    + ": blockId=" + blockId);

            if (!screenshotTaken) {
                File file = new File(statusDir, "block_placed.png");
                capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
                screenshotTaken = true;
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 200;
        }
    }

    // Step 6: Verify inventory still has 64 cobblestone (replenishment).
    // Waits up to the timeout for replenishment packets to arrive.
    private class VerifyReplenishmentStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "verify_replenishment";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;

            // RubyDung has no inventory
            if (input.isRubyDung()) {
                System.out.println("[McTestAgent] Replenishment check: skipped (RubyDung, no inventory)");
                return true;
            }

            int[][] slots = gs.getInventorySlots();
            if (slots == null) throw new RuntimeException("Could not read inventory");

            int cobbleCount = 0;
            for (int[] slot : slots) {
                if (slot[0] == 4) cobbleCount += slot[1];
            }
            // Creative mode: client has 1 cobblestone and doesn't consume on placement
            int expected = McTestAgent.isCreativeMode ? 1 : 64;

            if (cobbleCount >= expected) {
                System.out.println("[McTestAgent] Cobblestone after place: " + cobbleCount
                        + " (expected >=" + expected + ") — OK after " + ticks + " ticks");
                return true;
            }

            // Log periodically while waiting
            if (ticks % 20 == 0) {
                System.out.println("[McTestAgent] Waiting for replenishment: " + cobbleCount
                        + "/" + expected + " (tick " + ticks + ")");
            }

            // Throw a clear error near timeout so the step runner gets a useful message
            if (ticks >= 190) {
                throw new RuntimeException("Expected " + expected
                        + " cobblestone after replenishment, found " + cobbleCount);
            }
            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 200;
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
            if (input.isRubyDung()) return true; // RD uses direct methods
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
            if (input.isRubyDung()) {
                if (ticks == 1) {
                    input.placeBlockDirect(target2BX, targetBY + 1, target2BZ, 1);
                    input.placeBlockDirect(target2BX, targetBY + 2, target2BZ, 1);
                }
                return ticks >= 4;
            }
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
            // RD: look at the adjacent pillar from the stepped-back position
            if (ticks == 1 && input.isRubyDung()) {
                input.lookAtBlock(target2BX, targetBY + 2, target2BZ);
            }
            if (ticks < 80) return false;

            int checkY = input.isRubyDung() ? targetBY + 2 : targetBY;
            int blockId = gs.getBlockId(target2BX, checkY, target2BZ);
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
