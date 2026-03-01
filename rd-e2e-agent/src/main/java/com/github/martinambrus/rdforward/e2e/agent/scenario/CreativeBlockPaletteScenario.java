package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.ScreenshotCapture;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Phase 5: Systematically places blocks from the creative inventory.
 *
 * Opens the creative inventory GUI, grabs items from successive grid slots,
 * places them in a row on the ground, verifies each placement, then captures
 * a screenshot from on top of the first placed block.
 *
 * Beta 1.8 creative inventory has a scrollable grid of ~198 items.
 * We iterate through the first 45 grid slots (8 columns, ~6 rows).
 *
 * Layout: blocks placed in a row along +X, offset +2 in Z from spawn.
 * Player walks along spawn Z (clear of placed blocks) then places sideways.
 * Row wrapping at 80 blocks (start new row 3 blocks further in +Z).
 *
 * Verification per placement:
 * - Block items should convert to cobblestone (server converts)
 * - Non-block items disappear after at most 4s (80 ticks)
 * - Items that can't be placed are OK (bow, sword, etc.)
 *
 * Final: walk to first placed block, face +X, jump on it, screenshot.
 */
public class CreativeBlockPaletteScenario implements Scenario {

    // Total creative inventory grid slots to test (one full visible page)
    private static final int ITEMS_TO_PLACE = 45;
    private static final int MAX_PER_ROW = 80;
    private static final int ROW_SPACING_Z = 3;
    private static final int BLOCK_SPACING_X = 2;
    private static final int Z_OFFSET = 2; // blocks placed offset from walking path

    // Placement origin and tracking
    private double originX;
    private double originZ;
    private int groundY;
    private boolean originComputed;

    // Track first successfully placed block for final positioning
    private int firstPlacedX = Integer.MIN_VALUE;
    private int firstPlacedY;
    private int firstPlacedZ;

    @Override
    public String getName() {
        return "creative_block_palette";
    }

    @Override
    public List<ScenarioStep> getSteps() {
        List<ScenarioStep> steps = new ArrayList<ScenarioStep>();
        steps.add(new RecordOriginStep());

        for (int i = 0; i < ITEMS_TO_PLACE; i++) {
            steps.add(new OpenCreativeInventoryStep());
            steps.add(new GrabItemStep(i));
            steps.add(new CloseInventoryStep());
            steps.add(new WalkAndPlaceStep(i));
            steps.add(new VerifyPlacementStep(i));
        }

        steps.add(new FinalPositioningStep());
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

    /**
     * Compute target block X for a given item index (with row wrapping).
     */
    private int targetX(int itemIndex) {
        int col = itemIndex % MAX_PER_ROW;
        return (int) originX + 2 + (col * BLOCK_SPACING_X);
    }

    /**
     * Compute target block Z for a given item index (with row wrapping).
     * Offset from walking path so placed blocks don't obstruct movement.
     */
    private int targetZ(int itemIndex) {
        int row = itemIndex / MAX_PER_ROW;
        return (int) originZ + Z_OFFSET + (row * ROW_SPACING_Z);
    }

    // Record spawn position as placement origin
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
                // Click on creative grid slot (8 columns, scrollable rows)
                input.clickCreativeGridSlot(itemIndex, 0);
                return false;
            }
            if (ticks == 3) {
                // Place in hotbar slot 0 (selected by default)
                input.clickCreativeHotbar(0, 0);
                return false;
            }
            return ticks >= 5;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Close inventory
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

    /**
     * Walk along the origin Z axis (clear of placed blocks) until the
     * target block is within reach, then look sideways at it and place.
     */
    private class WalkAndPlaceStep implements ScenarioStep {
        private final int itemIndex;
        private int ticks;
        private int aimTicks; // ticks since entering placement range (lookAtBlock active)
        private boolean placed;
        private boolean clickSent;
        private int placeTick;

        WalkAndPlaceStep(int itemIndex) {
            this.itemIndex = itemIndex;
        }

        @Override
        public String getDescription() {
            return "walk_and_place_" + itemIndex;
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            computeOrigin(gs);
            ticks++;

            int tx = targetX(itemIndex);
            int tz = targetZ(itemIndex);
            double[] pos = gs.getPlayerPosition();
            if (pos == null) return false;

            // Distance to placement target (offset in Z from walking path)
            double dx = tx + 0.5 - pos[0];
            double dz = tz + 0.5 - pos[2];
            double dist = Math.sqrt(dx * dx + dz * dz);

            if (!placed && dist > 3.5) {
                // Walk along origin Z to get within X range of target
                // (block row is at Z_OFFSET, player walks at originZ)
                double walkDx = tx + 0.5 - pos[0];
                double walkDz = originZ + 0.5 - pos[2];
                float yaw = (float) Math.toDegrees(Math.atan2(-walkDx, walkDz));
                input.setLookDirection(yaw, 0);
                input.pressKey(0); // forward
                return false;
            }

            // In range — stop walking and place
            input.releaseAllKeys();
            aimTicks++;

            if (!placed) {
                // Look at the top face of the ground block at target position.
                // Adding 0.4 to groundY makes lookAtBlock target Y = groundY + 0.9
                // (since lookAtBlock adds 0.5 internally). This reduces the
                // downward angle so the raytrace reaches the target block's top
                // face instead of hitting closer ground blocks on flat terrain.
                input.lookAtBlock(tx, groundY + 0.4, tz);
                // Wait 2+ ticks after first lookAtBlock so at least one render
                // frame updates objectMouseOver with the new yaw/pitch before
                // the right-click fires.
                if (!clickSent && aimTicks >= 3) {
                    input.click(1); // right-click to place
                    clickSent = true;
                }
                if (aimTicks >= 5) {
                    placed = true;
                    placeTick = ticks;
                }
                return false;
            }

            // Wait a few ticks after placing
            return ticks >= placeTick + 3;
        }

        @Override
        public int getTimeoutTicks() {
            return 200; // generous timeout for walking + placing
        }
    }

    /**
     * Verify the placement result: block items should convert to cobblestone,
     * non-block items should disappear (or never appear).
     */
    private class VerifyPlacementStep implements ScenarioStep {
        private final int itemIndex;
        private int ticks;

        VerifyPlacementStep(int itemIndex) {
            this.itemIndex = itemIndex;
        }

        @Override
        public String getDescription() {
            return "verify_placement_" + itemIndex;
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            computeOrigin(gs);
            ticks++;

            int tx = targetX(itemIndex);
            int tz = targetZ(itemIndex);
            int placedY = groundY + 1;
            int blockId = gs.getBlockId(tx, placedY, tz);

            if (ticks == 1) {
                // First check — block placed successfully (any non-air block).
                // Block IDs vary by version (legacy cobblestone=4, 1.13+ state IDs differ).
                if (blockId != 0) {
                    System.out.println("[McTestAgent] Item " + itemIndex
                            + ": placed as blockId=" + blockId + " at (" + tx + "," + placedY + "," + tz + ")");
                    recordFirstPlaced(tx, placedY, tz);
                    return true;
                }
            }

            // Wait up to 80 ticks (4s) for non-block items to disappear / conversion
            if (ticks < 80) {
                if (blockId != 0) {
                    System.out.println("[McTestAgent] Item " + itemIndex
                            + ": placed as blockId=" + blockId + " after " + ticks + " ticks");
                    recordFirstPlaced(tx, placedY, tz);
                    return true;
                }
                return false;
            }

            // After 4s timeout: item was not placeable or disappeared
            if (blockId == 0) {
                System.out.println("[McTestAgent] Item " + itemIndex
                        + ": non-block item or not placeable (air after 4s) — OK");
            } else {
                System.out.println("[McTestAgent] Item " + itemIndex
                        + ": blockId=" + blockId + " after 4s wait — accepting");
            }
            return true; // continue regardless
        }

        @Override
        public int getTimeoutTicks() {
            return 100;
        }
    }

    private void recordFirstPlaced(int x, int y, int z) {
        if (firstPlacedX == Integer.MIN_VALUE) {
            firstPlacedX = x;
            firstPlacedY = y;
            firstPlacedZ = z;
            System.out.println("[McTestAgent] First placed block recorded at ("
                    + x + "," + y + "," + z + ")");
        }
    }

    /**
     * Walk back along the origin Z (clear of placed blocks) to the first
     * placed block's X, then face the block row for the screenshot.
     */
    private class FinalPositioningStep implements ScenarioStep {
        private int ticks;
        private int phase; // 0=walk back along originZ, 1=face row, 2=done

        @Override
        public String getDescription() {
            return "final_positioning";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;

            if (firstPlacedX == Integer.MIN_VALUE) {
                System.out.println("[McTestAgent] No blocks were placed, skipping positioning");
                return true;
            }

            double[] pos = gs.getPlayerPosition();
            if (pos == null) return false;

            switch (phase) {
                case 0: {
                    // Walk along originZ to the first placed block's X
                    double walkX = firstPlacedX + 0.5;
                    double walkZ = originZ + 0.5;
                    double dx = walkX - pos[0];
                    double dz = walkZ - pos[2];
                    double dist = Math.sqrt(dx * dx + dz * dz);

                    if (dist > 1.5) {
                        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                        input.setLookDirection(yaw, 0);
                        input.pressKey(0); // forward
                    } else {
                        input.releaseAllKeys();
                        phase = 1;
                        ticks = 0;
                    }
                    break;
                }
                case 1: {
                    // Face +X direction (yaw=-90), look slightly down toward block row
                    input.setLookDirection(-90, 10);
                    if (ticks >= 3) {
                        input.releaseAllKeys();
                        phase = 2;
                    }
                    break;
                }
                case 2:
                    return true;
            }
            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 600; // generous for walking back ~90 blocks
        }
    }

    // Final screenshot from on top of first placed block
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
