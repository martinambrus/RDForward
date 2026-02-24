package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.ScreenshotCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase 5: Systematically places blocks from the creative inventory.
 *
 * Opens the creative inventory GUI, grabs items from successive grid slots,
 * places them in a row on the ground, then captures a screenshot.
 *
 * Beta 1.8 creative inventory has a scrollable grid of ~198 items.
 * We place a representative sample (first 20 items from the grid)
 * to verify block placement from creative inventory works correctly.
 *
 * Layout: blocks placed in a row along +X from spawn, one block apart.
 */
public class CreativeBlockPaletteScenario implements Scenario {

    // Number of creative inventory items to test
    private static final int ITEMS_TO_PLACE = 20;

    // Placement tracking
    private double originX;
    private double originZ;
    private int groundY;
    private boolean originComputed;

    @Override
    public String getName() {
        return "creative_block_palette";
    }

    @Override
    public List<ScenarioStep> getSteps() {
        List<ScenarioStep> steps = new ArrayList<ScenarioStep>();
        steps.add(new RecordOriginStep());

        // For each item: open inventory, grab item to hotbar, close, place, wait
        for (int i = 0; i < ITEMS_TO_PLACE; i++) {
            steps.add(new OpenCreativeInventoryStep());
            steps.add(new GrabItemStep(i));
            steps.add(new CloseInventoryStep());
            steps.add(new LookAndPlaceStep(i));
            steps.add(new WaitPlaceStep());
        }

        steps.add(new FinalScreenshotStep());
        return steps;
    }

    private void computeOrigin(GameState gs) {
        if (originComputed) return;
        double[] pos = gs.getPlayerPosition();
        if (pos == null) throw new RuntimeException("No player position");
        originX = Math.floor(pos[0]);
        originZ = Math.floor(pos[2]);
        groundY = (int) Math.floor(pos[1] - (double) 1.62f) - 1;
        originComputed = true;
        System.out.println("[McTestAgent] Creative palette origin: ("
                + originX + "," + groundY + "," + originZ + ")");
    }

    // Step 1: Record spawn position as placement origin
    private class RecordOriginStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "record_origin";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            computeOrigin(gs);
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Open creative inventory
    private class OpenCreativeInventoryStep implements ScenarioStep {
        private int ticks;
        private boolean opened;

        @Override
        public String getDescription() {
            return "open_creative_inv";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (!opened) {
                input.openInventory();
                opened = true;
                return false;
            }
            if (ticks < 3) return false;
            Class<?> screen = gs.getCurrentScreenClass();
            if (screen == null) {
                throw new RuntimeException("Creative inventory did not open");
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 40;
        }
    }

    // Grab item from creative grid slot and put in hotbar slot 0
    // Creative inventory grid slots in Beta 1.8: slots 0-44 are the grid area.
    // We click on successive grid positions.
    private class GrabItemStep implements ScenarioStep {
        private final int itemIndex;
        private int ticks;

        GrabItemStep(int itemIndex) {
            this.itemIndex = itemIndex;
        }

        @Override
        public String getDescription() {
            return "grab_item_" + itemIndex;
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks == 1) {
                // Click on creative grid slot (left click to pick up stack)
                input.clickInventorySlot(itemIndex, 0);
                return false;
            }
            if (ticks == 3) {
                // Place in hotbar slot 0 (window slot 36 in standard container)
                input.clickInventorySlot(36, 0);
                return false;
            }
            return ticks >= 5;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Close the inventory screen
    private class CloseInventoryStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "close_inventory";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            input.closeScreen();
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Look at placement target and right-click to place
    private class LookAndPlaceStep implements ScenarioStep {
        private final int itemIndex;
        private int ticks;

        LookAndPlaceStep(int itemIndex) {
            this.itemIndex = itemIndex;
        }

        @Override
        public String getDescription() {
            return "place_item_" + itemIndex;
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            // Place along +X axis, each item 2 blocks apart
            int targetX = (int) originX + 2 + (itemIndex * 2);
            int targetZ = (int) originZ;

            // Look at the ground block at target position
            input.lookAtBlock(targetX, groundY, targetZ);

            if (ticks >= 3 && ticks <= 5) {
                input.click(1); // right-click to place
            }
            return ticks >= 8;
        }

        @Override
        public int getTimeoutTicks() {
            return 40;
        }
    }

    // Brief wait between placements
    private class WaitPlaceStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "wait_place";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            return ticks >= 5;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Final screenshot
    private class FinalScreenshotStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "screenshot";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            File file = new File(statusDir, "creative_palette.png");
            capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }
}
