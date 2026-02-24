package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.McTestAgent;
import com.github.martinambrus.rdforward.e2e.agent.ScreenshotCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests inventory manipulation: opening GUI, clicking slots, splitting stacks,
 * dropping items outside, and verifying replenishment.
 *
 * Inventory slot mapping (mainInventory array -> window slot):
 *   mainInventory[0-8]  = hotbar   = window slots 36-44
 *   mainInventory[9-35] = main inv = window slots 9-35
 */
public class InventoryManipulationScenario implements Scenario {

    // Window slot with cobblestone (hotbar 0 = window 36)
    private int cobbleSlot = 36;
    // First empty slot found after opening
    private int emptySlot = -1;

    @Override
    public String getName() {
        return "inventory_manipulation";
    }

    @Override
    public List<ScenarioStep> getSteps() {
        List<ScenarioStep> steps = new ArrayList<ScenarioStep>();
        if (McTestAgent.isCreativeMode) {
            // Creative mode: simplified test (1 cobblestone, no split/throw)
            steps.add(new OpenInventoryStep());
            steps.add(new CreativeClickEmptyStep());
            steps.add(new CloseInventoryStep());
            steps.add(new WaitAfterCloseStep());
            steps.add(new ScreenshotStep());
        } else {
            steps.add(new OpenInventoryStep());
            steps.add(new ClickEmptySlotStep());
            steps.add(new SplitStackStep());
            steps.add(new PlaceSplitStep());
            steps.add(new VerifySplitStep());
            steps.add(new PickUpHalfStep());
            steps.add(new ThrowHalfOutsideStep());
            steps.add(new WaitReplenish16Step());
            steps.add(new VerifyAfterThrow16Step());
            steps.add(new PickUpHalfAgainStep());
            steps.add(new ThrowOneOutsideStep());
            steps.add(new PutBackStep());
            steps.add(new WaitReplenish1Step());
            steps.add(new VerifyAfterThrow1Step());
            steps.add(new CloseInventoryStep());
            steps.add(new WaitAfterCloseStep());
            steps.add(new VerifyFinalInventoryStep());
            steps.add(new ScreenshotStep());
        }
        return steps;
    }

    /**
     * Find the window slot for cobblestone and the first empty slot.
     * mainInventory[0-8] = window 36-44, mainInventory[9-35] = window 9-35.
     */
    private void findSlots(GameState gs) {
        int[][] slots = gs.getInventorySlots();
        if (slots == null) throw new RuntimeException("Could not read inventory");

        cobbleSlot = -1;
        emptySlot = -1;

        for (int i = 0; i < slots.length; i++) {
            int windowSlot = (i < 9) ? (36 + i) : i;
            if (slots[i][0] == 4 && cobbleSlot == -1) {
                cobbleSlot = windowSlot;
            }
            if (slots[i][0] == 0 && emptySlot == -1) {
                emptySlot = windowSlot;
            }
        }

        if (cobbleSlot == -1) throw new RuntimeException("No cobblestone found in inventory");
        if (emptySlot == -1) throw new RuntimeException("No empty slot found in inventory");

        System.out.println("[McTestAgent] Cobble slot=" + cobbleSlot + " empty slot=" + emptySlot);
    }

    // Creative mode: click an empty slot (no-crash validation), then close
    private class CreativeClickEmptyStep implements ScenarioStep {
        private int ticks;
        private boolean clicked;

        @Override
        public String getDescription() {
            return "creative_click_empty";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (!clicked) {
                // Click outside the grid area (no-crash test)
                Class<?> screen = gs.getCurrentScreenClass();
                if (screen == null) return false; // wait for screen
                input.clickInventorySlot(9, 1); // slot 9 is typically empty in creative
                clicked = true;
                return false;
            }
            return ticks >= 3;
        }

        @Override
        public int getTimeoutTicks() {
            return 40;
        }
    }

    // Step 1: Open inventory
    private class OpenInventoryStep implements ScenarioStep {
        private int ticks;
        private boolean opened;

        @Override
        public String getDescription() {
            return "open_inventory";
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
            // Wait a tick for the screen to open
            if (ticks < 3) return false;

            Class<?> screen = gs.getCurrentScreenClass();
            if (screen == null) {
                throw new RuntimeException("Inventory screen did not open");
            }
            System.out.println("[McTestAgent] Inventory screen open: " + screen.getName());

            findSlots(gs);
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 40;
        }
    }

    // Step 2: Right-click empty slot (no-crash validation)
    private class ClickEmptySlotStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "click_empty_slot";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks == 1) {
                input.clickInventorySlot(emptySlot, 1);
                return false;
            }
            return ticks >= 3;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 3: Right-click cobblestone to split (pick up 32, leave 32)
    private class SplitStackStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "split_stack";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks == 1) {
                input.clickInventorySlot(cobbleSlot, 1); // right-click picks up half
                return false;
            }
            return ticks >= 3;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 4: Left-click on empty slot to place split stack
    private class PlaceSplitStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "place_split";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks == 1) {
                input.clickInventorySlot(emptySlot, 0); // left-click places all
                return false;
            }
            return ticks >= 3;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 5: Verify split (2 stacks totaling 64) and screenshot
    private class VerifySplitStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "verify_split";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            int total = gs.getTotalCobblestone();
            System.out.println("[McTestAgent] Cobblestone after split: " + total);
            if (total != 64) {
                throw new RuntimeException("Expected 64 total cobblestone after split, found " + total);
            }

            File file = new File(statusDir, "inventory_split.png");
            capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 6: Right-click the split-target 32 stack (picks up 16)
    private class PickUpHalfStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "pick_up_half";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks == 1) {
                input.clickInventorySlot(emptySlot, 1); // right-click = pick up half
                return false;
            }
            return ticks >= 3;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 7: Left-click outside to drop all 16 from cursor
    private class ThrowHalfOutsideStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "throw_half_outside";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks == 1) {
                input.clickOutsideInventory(0); // left-click outside = drop all
                return false;
            }
            return ticks >= 3;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 8: Wait for replenishment of 16 items
    private class WaitReplenish16Step implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "wait_replenish_16";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            return ticks >= 30;
        }

        @Override
        public int getTimeoutTicks() {
            return 60;
        }
    }

    // Step 9: Verify 64 total after throwing 16
    private class VerifyAfterThrow16Step implements ScenarioStep {
        @Override
        public String getDescription() {
            return "verify_after_throw_16";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            int total = gs.getTotalCobblestone();
            System.out.println("[McTestAgent] Cobblestone after throw 16: " + total);
            if (total < 64) {
                throw new RuntimeException("Expected 64 cobblestone after replenish, found " + total);
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 10: Right-click a cobblestone stack to pick up half
    private class PickUpHalfAgainStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "pick_up_half_again";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks == 1) {
                // Re-find slots since replenishment may have changed layout
                findSlots(gs);
                input.clickInventorySlot(cobbleSlot, 1);
                return false;
            }
            return ticks >= 3;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 11: Right-click outside to drop 1 from cursor
    private class ThrowOneOutsideStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "throw_one_outside";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks == 1) {
                input.clickOutsideInventory(1); // right-click outside = drop one
                return false;
            }
            return ticks >= 3;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 12: Left-click on source slot to return remaining cursor items
    private class PutBackStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "put_back";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks == 1) {
                input.clickInventorySlot(cobbleSlot, 0); // left-click to put back
                return false;
            }
            return ticks >= 3;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 13: Wait for replenishment of 1 item
    private class WaitReplenish1Step implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "wait_replenish_1";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            return ticks >= 30;
        }

        @Override
        public int getTimeoutTicks() {
            return 60;
        }
    }

    // Step 14: Verify 64 total after throwing 1
    private class VerifyAfterThrow1Step implements ScenarioStep {
        @Override
        public String getDescription() {
            return "verify_after_throw_1";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            int total = gs.getTotalCobblestone();
            System.out.println("[McTestAgent] Cobblestone after throw 1: " + total);
            if (total < 64) {
                throw new RuntimeException("Expected 64 cobblestone after replenish, found " + total);
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 15: Close inventory
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

    // Step 16: Wait after close for any final replenishment
    private class WaitAfterCloseStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "wait_after_close";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            return ticks >= 30;
        }

        @Override
        public int getTimeoutTicks() {
            return 60;
        }
    }

    // Step 17: Verify final 64 total cobblestone
    private class VerifyFinalInventoryStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "verify_final_inventory";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            int total = gs.getTotalCobblestone();
            System.out.println("[McTestAgent] Final cobblestone: " + total);
            if (total < 64) {
                throw new RuntimeException("Expected 64 cobblestone at end, found " + total);
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 18: Final screenshot
    private class ScreenshotStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "screenshot";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            File file = new File(statusDir, "inventory_complete.png");
            capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }
}
