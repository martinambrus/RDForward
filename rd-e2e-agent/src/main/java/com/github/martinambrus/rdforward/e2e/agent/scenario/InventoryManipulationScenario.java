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
    // Whether emptySlot contains porkchops that need to be cleared first
    private boolean needsClearSlot;

    @Override
    public String getName() {
        return "inventory_manipulation";
    }

    @Override
    public List<ScenarioStep> getSteps() {
        List<ScenarioStep> steps = new ArrayList<ScenarioStep>();
        if (McTestAgent.isCreativeMode) {
            // Setup: grab cobblestone from creative inventory into hotbar slot 1
            steps.add(new OpenInventoryStep());
            steps.add(new GrabCobblestoneFromCreativeStep());
            steps.add(new CloseInventoryStep());
            steps.add(new WaitAfterCloseStep());
            // Full split/throw/verify cycle (same steps as survival)
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
        } else {
            steps.add(new OpenInventoryStep());
            steps.add(new ClearSlotStep());
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
     * Returns true if the current client is Alpha (immediate replenishment).
     * Alpha mappings class names contain "AlphaV".
     */
    private boolean isAlphaClient() {
        String mappingsClass = McTestAgent.mappings.getClass().getSimpleName();
        return mappingsClass.contains("Alpha");
    }

    /**
     * Click a hotbar slot, using the correct method for creative vs survival mode.
     * Creative inventory has different container dimensions (176x208 vs 176x166).
     */
    private void clickHotbarSlot(int windowSlot, int button, InputController input) {
        if (McTestAgent.isCreativeMode) {
            input.clickCreativeHotbar(windowSlot - 36, button);
        } else {
            input.clickInventorySlot(windowSlot, button);
        }
    }

    /**
     * Get a list of individual cobblestone stack sizes in the inventory.
     */
    private List<Integer> getCobblestoneStacks(GameState gs) {
        int[][] slots = gs.getInventorySlots();
        List<Integer> stacks = new ArrayList<Integer>();
        if (slots == null) return stacks;
        for (int[] slot : slots) {
            if (slot[0] == 4 && slot[1] > 0) {
                stacks.add(slot[1]);
            }
        }
        return stacks;
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

        if (McTestAgent.isCreativeMode) {
            // Creative inventory only shows grid + hotbar, no main inventory area
            for (int i = 0; i < 9; i++) {
                int windowSlot = 36 + i;
                if (slots[i][0] == 4 && cobbleSlot == -1) cobbleSlot = windowSlot;
                if (slots[i][0] == 0 && emptySlot == -1) emptySlot = windowSlot;
            }
        } else {
            for (int i = 0; i < slots.length; i++) {
                int windowSlot = (i < 9) ? (36 + i) : i;
                if (slots[i][0] == 4 && cobbleSlot == -1) {
                    cobbleSlot = windowSlot;
                }
                if (slots[i][0] == 0 && emptySlot == -1) {
                    emptySlot = windowSlot;
                }
            }
        }

        if (cobbleSlot == -1) throw new RuntimeException("No cobblestone found in inventory");

        // Alpha 1.0.15/1.0.16: server fills all slots with cooked porkchops (320).
        // Use a porkchop slot as fallback — it will be cleared before the split flow.
        if (emptySlot == -1) {
            for (int i = 0; i < slots.length; i++) {
                if (slots[i][0] != 0 && slots[i][0] != 4) {
                    int windowSlot = (i < 9) ? (36 + i) : i;
                    if (windowSlot != cobbleSlot) {
                        emptySlot = windowSlot;
                        needsClearSlot = true;
                        break;
                    }
                }
            }
        }

        if (emptySlot == -1) throw new RuntimeException("No empty slot found in inventory");

        System.out.println("[McTestAgent] Cobble slot=" + cobbleSlot + " empty slot=" + emptySlot
                + (needsClearSlot ? " (needs clear)" : ""));
    }

    // Clears a non-empty slot (e.g. porkchops) by picking up and dropping outside.
    // No-op if the slot was already empty.
    private class ClearSlotStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "clear_slot";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            if (!needsClearSlot) return true;
            ticks++;
            if (ticks == 1) {
                System.out.println("[McTestAgent] Clearing slot " + emptySlot);
                clickHotbarSlot(emptySlot, 0, input); // left-click picks up all
                return false;
            }
            if (ticks == 3) {
                input.clickOutsideInventory(0); // left-click outside drops all
                return false;
            }
            if (ticks >= 5) {
                needsClearSlot = false;
                return true;
            }
            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Grab cobblestone from creative grid slot 0 and place in hotbar slot 1
    private class GrabCobblestoneFromCreativeStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "grab_cobble_creative";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks == 1) {
                input.clickCreativeGridSlot(0, 0); // cobblestone at grid index 0
                return false;
            }
            if (ticks == 3) {
                input.clickCreativeHotbar(1, 0); // place in hotbar slot 1
                return false;
            }
            if (ticks >= 5) {
                int[][] slots = gs.getInventorySlots();
                if (slots != null && slots[1][0] == 4) {
                    System.out.println("[McTestAgent] Cobblestone placed in hotbar slot 1: "
                            + slots[1][1] + " stack");
                } else {
                    throw new RuntimeException("Failed to place cobblestone in hotbar slot 1");
                }
                return true;
            }
            return false;
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
                clickHotbarSlot(emptySlot, 1, input);
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
                clickHotbarSlot(cobbleSlot, 1, input); // right-click picks up half
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
                clickHotbarSlot(emptySlot, 0, input); // left-click places all
                return false;
            }
            return ticks >= 3;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 5: Verify split and screenshot
    // Survival: 2 stacks totaling 64. Creative: 2 stacks totaling 2 (server gives 1 + grab gives 1).
    private class VerifySplitStep implements ScenarioStep {
        private int ticks;
        private boolean verified;

        @Override
        public String getDescription() {
            return "verify_split";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;

            if (!verified) {
                int total = gs.getTotalCobblestone();
                int expected = McTestAgent.isCreativeMode ? 2 : 64;
                System.out.println("[McTestAgent] Cobblestone after split: " + total);
                if (total != expected) {
                    throw new RuntimeException("Expected " + expected
                            + " total cobblestone after split, found " + total);
                }
                verified = true;
                return false; // wait for GUI to render the updated state
            }

            // Wait for the inventory GUI to redraw before capturing.
            // Some versions need extra ticks for the placed stack to visually
            // settle into the slot (floating item animation).
            if (ticks < 20) return false;

            File file = new File(statusDir, "inventory_split.png");
            capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 30;
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
                clickHotbarSlot(emptySlot, 1, input); // right-click = pick up half
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

    // Step 9: Verify cobblestone after first throw
    // Alpha: total >= 64 (immediate replenishment)
    // Non-Alpha survival: exactly 1x32 + 1x16
    // Creative: total = 1 (no replenishment)
    private class VerifyAfterThrow16Step implements ScenarioStep {
        @Override
        public String getDescription() {
            return "verify_after_throw_16";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            int total = gs.getTotalCobblestone();
            List<Integer> stacks = getCobblestoneStacks(gs);
            System.out.println("[McTestAgent] Cobblestone after throw: total=" + total
                    + " stacks=" + stacks);

            if (McTestAgent.isCreativeMode) {
                // Creative: no replenishment, 1 cobblestone remains
                if (total != 1) {
                    throw new RuntimeException("Creative: expected 1 cobblestone after throw, found " + total);
                }
            } else if (isAlphaClient()) {
                if (total < 64) {
                    throw new RuntimeException("Alpha: expected 64 cobblestone after replenish, found " + total);
                }
            } else {
                // Non-Alpha survival: expect 1x32 + 1x16 = 48 total
                if (total != 48) {
                    throw new RuntimeException("Non-Alpha: expected 48 cobblestone (1x32+1x16), found " + total);
                }
                boolean has32 = stacks.contains(32);
                boolean has16 = stacks.contains(16);
                if (!has32 || !has16 || stacks.size() != 2) {
                    throw new RuntimeException("Non-Alpha: expected stacks [32,16], found " + stacks);
                }
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
                clickHotbarSlot(cobbleSlot, 1, input);
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
                clickHotbarSlot(cobbleSlot, 0, input); // left-click to put back
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

    // Step 14: Verify cobblestone after second throw
    // Alpha: total >= 64 (immediate replenishment)
    // Non-Alpha survival: exactly 1x31 + 1x16
    // Creative: total = 0 (no replenishment, all cobblestone thrown)
    private class VerifyAfterThrow1Step implements ScenarioStep {
        @Override
        public String getDescription() {
            return "verify_after_throw_1";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            int total = gs.getTotalCobblestone();
            List<Integer> stacks = getCobblestoneStacks(gs);
            System.out.println("[McTestAgent] Cobblestone after throw 1: total=" + total
                    + " stacks=" + stacks);

            if (McTestAgent.isCreativeMode) {
                // Creative: no replenishment, all cobblestone thrown away
                if (total != 0) {
                    throw new RuntimeException("Creative: expected 0 cobblestone after throws, found " + total);
                }
            } else if (isAlphaClient()) {
                if (total < 64) {
                    throw new RuntimeException("Alpha: expected 64 cobblestone after replenish, found " + total);
                }
            } else {
                // Non-Alpha survival: expect 1x31 + 1x16 = 47 total
                if (total != 47) {
                    throw new RuntimeException("Non-Alpha: expected 47 cobblestone (1x31+1x16), found " + total);
                }
                boolean has31 = stacks.contains(31);
                boolean has16 = stacks.contains(16);
                if (!has31 || !has16 || stacks.size() != 2) {
                    throw new RuntimeException("Non-Alpha: expected stacks [31,16], found " + stacks);
                }
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

    // Step 17: Verify final cobblestone after closing inventory
    // Alpha: total >= 64
    // Non-Alpha survival (Beta): exactly 1x64 (server replenishes on close)
    // Creative: total = 0 (no replenishment on close)
    private class VerifyFinalInventoryStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "verify_final_inventory";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            int total = gs.getTotalCobblestone();
            List<Integer> stacks = getCobblestoneStacks(gs);
            System.out.println("[McTestAgent] Final cobblestone: total=" + total
                    + " stacks=" + stacks);

            if (McTestAgent.isCreativeMode) {
                // Creative: no replenishment, all cobblestone was thrown away
                if (total != 0) {
                    throw new RuntimeException("Creative: expected 0 cobblestone at end, found " + total);
                }
            } else if (isAlphaClient()) {
                if (total < 64) {
                    throw new RuntimeException("Alpha: expected 64 cobblestone at end, found " + total);
                }
            } else {
                // Non-Alpha survival: server replenishes to 1x64 on inventory close
                if (total != 64) {
                    throw new RuntimeException("Non-Alpha: expected 64 cobblestone at end, found " + total);
                }
                if (stacks.size() != 1 || stacks.get(0) != 64) {
                    throw new RuntimeException("Non-Alpha: expected exactly 1x64 stack, found " + stacks);
                }
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
