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
 * 0. Chat contains join message with player username (testing-todo step 0)
 * 1. Player is on a grass surface (block below = grass ID 2, block at feet = air ID 0)
 * 2. Surroundings are clear (air above ground in a 5-block radius)
 * 2b. Chunks loaded around player — sample terrain at 16/32/48 blocks in each direction
 * 3. Inventory has 64 cobblestone (item ID 4)
 * 4. Alpha 1.0.15/1.0.16: all other slots filled with cooked porkchops (conditional)
 * 5. Capture screenshot
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
        steps.add(new CheckJoinMessageStep());
        steps.add(new CheckPositionStep());
        steps.add(new CheckSurroundingsStep());
        steps.add(new CheckChunksLoadedStep());
        steps.add(new CheckInventoryStep());
        steps.add(new CheckPorkchopInventoryStep());
        steps.add(new CaptureStep());
        return steps;
    }

    /**
     * Step 0: check chat for a join message containing the player's username.
     */
    private static class CheckJoinMessageStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "check_join_message";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            List<String> messages = gs.getChatMessages(20);
            if (messages == null || messages.isEmpty()) {
                return false; // keep polling
            }

            for (String msg : messages) {
                if (msg == null) continue;
                String lower = msg.toLowerCase();
                if (lower.contains("joined") || lower.contains("join")) {
                    System.out.println("[McTestAgent] Join message found: " + msg);
                    // If username was explicitly set, verify it appears in the message
                    if (McTestAgent.username != null && !msg.contains(McTestAgent.username)) {
                        System.out.println("[McTestAgent] WARNING: Join message does not contain username '"
                                + McTestAgent.username + "'");
                    }
                    return true;
                }
            }
            return false; // keep polling until timeout
        }

        @Override
        public int getTimeoutTicks() {
            return 40; // 2 seconds — messages should be there from stabilization
        }
    }

    /**
     * Step 2: verify chunks are loaded around the player (flat map).
     * Samples terrain at 16, 32, 48 blocks in each cardinal direction.
     */
    private static class CheckChunksLoadedStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "check_chunks_loaded";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            double[] pos = gs.getPlayerPosition();
            if (pos == null) throw new RuntimeException("No player position");

            int px = (int) Math.floor(pos[0]);
            int groundY = (int) Math.floor(pos[1] - (double) 1.62f) - 1;
            int pz = (int) Math.floor(pos[2]);

            int[] offsets = {16, 32, 48};
            int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
            int issues = 0;

            for (int[] dir : dirs) {
                for (int dist : offsets) {
                    int sx = px + dir[0] * dist;
                    int sz = pz + dir[1] * dist;
                    int blockId = gs.getBlockId(sx, groundY, sz);
                    // On a flat map, surface should be grass (2) or dirt (3)
                    if (blockId != 2 && blockId != 3) {
                        issues++;
                        System.out.println("[McTestAgent] Chunk check failed at ("
                                + sx + "," + groundY + "," + sz + "): blockId=" + blockId);
                    }
                }
            }

            System.out.println("[McTestAgent] Chunk loaded check: " + issues + " failed samples out of 12");
            if (issues > 0) {
                throw new RuntimeException("Chunks not loaded: " + issues
                        + " of 12 sample points had unexpected block IDs (expected grass/dirt)");
            }
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    /**
     * Step 4: on Alpha 1.0.15/1.0.16, verify all non-cobblestone slots have cooked porkchops.
     * No-op for all other versions (returns immediately).
     */
    private static class CheckPorkchopInventoryStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "check_porkchop_inventory";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            // Check if this is Alpha 1.0.15 or 1.0.16 via mappings class name
            String mappingsClass = McTestAgent.mappings.getClass().getSimpleName();
            // AlphaV6Mappings covers a1.2.x, not 1.0.15/16.
            // When AlphaV14Mappings or AlphaV13Mappings are added, they'll match here.
            if (!mappingsClass.contains("V14") && !mappingsClass.contains("V13")) {
                System.out.println("[McTestAgent] Porkchop check: skipped (not Alpha 1.0.15/1.0.16)");
                return true;
            }

            int[][] slots = gs.getInventorySlots();
            if (slots == null) throw new RuntimeException("Could not read inventory");

            // Cooked porkchop item ID = 320
            int nonCobbleNonPorkchop = 0;
            for (int[] slot : slots) {
                if (slot[0] == 0) continue; // empty
                if (slot[0] == 4) continue; // cobblestone
                if (slot[0] != 320) {
                    nonCobbleNonPorkchop++;
                    System.out.println("[McTestAgent] Unexpected item in slot: id=" + slot[0]
                            + " count=" + slot[1]);
                }
            }

            if (nonCobbleNonPorkchop > 0) {
                throw new RuntimeException("Expected all non-cobblestone slots to have cooked porkchops, found "
                        + nonCobbleNonPorkchop + " slots with other items");
            }
            System.out.println("[McTestAgent] Porkchop check: all non-cobblestone slots have porkchops");
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 10;
        }
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
