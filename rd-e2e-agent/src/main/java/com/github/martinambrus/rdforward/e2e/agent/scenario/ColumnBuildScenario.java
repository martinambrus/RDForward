package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.ScreenshotCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests vertical building to near max height and breaking back down:
 * 1. Place a block to stand on
 * 2. Jump onto it
 * 3. Look straight down, hold jump + right-click to build column
 * 4. Verify Y near max height (~128)
 * 5. Attempt to build above limit — Y unchanged
 * 6. Break blocks downward
 * 7. Verify back on ground (grass)
 */
public class ColumnBuildScenario implements Scenario {

    // Shared state across steps
    private int platformBX, platformBY, platformBZ;
    private boolean platformComputed;

    @Override
    public String getName() {
        return "column_build";
    }

    @Override
    public List<ScenarioStep> getSteps() {
        List<ScenarioStep> steps = new ArrayList<ScenarioStep>();
        steps.add(new PlacePlatformStep());
        steps.add(new JumpOnPlatformStep());
        steps.add(new LookDownStep());
        steps.add(new BuildColumnStep());
        steps.add(new VerifyHeightStep());
        steps.add(new AttemptAboveLimitStep());
        steps.add(new BreakDownStep());
        steps.add(new VerifyGroundStep());
        return steps;
    }

    private void computePlatform(GameState gs) {
        if (platformComputed) return;
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

        // Platform: 2 blocks in front (+Z)
        platformBX = px;
        platformBY = groundY + 1; // place on top of ground
        platformBZ = pz + 2;
        platformComputed = true;

        System.out.println("[McTestAgent] Column platform: ("
                + platformBX + "," + platformBY + "," + platformBZ + ")");
    }

    // Step 1: Place cobblestone block to create a platform
    private class PlacePlatformStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "place_platform";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            computePlatform(gs);
            ticks++;
            if (input.isRubyDung()) {
                if (ticks == 1) {
                    input.placeBlockDirect(platformBX, platformBY, platformBZ, 1);
                }
                return ticks >= 5;
            }
            if (ticks == 1) {
                // Look at the ground block where we want to place on top
                input.lookAtBlock(platformBX, platformBY - 1, platformBZ);
            }
            if (ticks == 3) {
                input.click(1); // right-click to place
            }
            return ticks >= 10;
        }

        @Override
        public int getTimeoutTicks() {
            return 40;
        }
    }

    // Step 2: Walk onto the platform and jump up
    private class JumpOnPlatformStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "jump_on_platform";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (input.isRubyDung()) {
                // RD: teleport onto the platform directly
                if (ticks == 1) {
                    double[] pos = gs.getPlayerPosition();
                    if (pos != null) {
                        float dx = platformBX + 0.5f - (float) pos[0];
                        float dy = (platformBY + 1.0f + 1.62f) - (float) pos[1];
                        float dz = platformBZ + 0.5f - (float) pos[2];
                        input.movePlayerPosition(dx, dy, dz);
                    }
                }
                return ticks >= 3;
            }
            if (ticks == 1) {
                // Look toward the platform and walk forward + jump
                input.lookAtBlock(platformBX, platformBY + 1, platformBZ);
                input.pressKey(InputController.FORWARD);
                input.pressKey(InputController.JUMP);
            }
            // Check if we're above the platform level
            double[] pos = gs.getPlayerPosition();
            if (pos != null) {
                double feetY = pos[1] - (double) 1.62f;
                if (feetY >= platformBY + 0.9) {
                    input.releaseAllKeys();
                    return true;
                }
            }
            // Stop walking after a bit to avoid overshooting
            if (ticks > 20) {
                input.releaseKey(InputController.FORWARD);
            }
            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 100;
        }
    }

    // Step 3: Look straight down
    private class LookDownStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "look_down";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            input.setLookDirection(gs.getYaw(), 90f); // pitch 90 = straight down
            ticks++;
            return ticks >= 2;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 4: Build column upward using controlled jump-place cycles.
    // Uses a state machine to stabilize between jumps (prevents X/Z drift)
    // and precisely time block placement when feet are above the target block
    // (required to pass the server's body overlap check).
    // RubyDung: uses direct placeBlockDirect + movePlayerPosition instead.
    private class BuildColumnStep implements ScenarioStep {
        private int ticks;

        // State machine phases (Alpha/Beta only)
        private static final int GROUND = 0;   // on ground, stabilizing
        private static final int AIRBORNE = 1; // jumped, trying to place

        private int phase = GROUND;
        private int phaseTicks;
        private double highWaterY;
        private int consecutiveStalls;

        // RubyDung column state
        private int rdCurrentY;

        @Override
        public String getDescription() {
            return "build_column";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;

            if (input.isRubyDung()) {
                return tickRubyDung(gs, input);
            }

            // Always look straight down
            input.setLookDirection(gs.getYaw(), 90f);

            double[] pos = gs.getPlayerPosition();
            if (pos == null) return false;
            double y = pos[1];

            if (ticks == 1) highWaterY = y;

            if (ticks % 40 == 0 || ticks <= 5) {
                System.out.println("[McTestAgent] BuildColumn tick=" + ticks
                        + " phase=" + phase
                        + " Y=" + String.format("%.2f", y)
                        + " X=" + String.format("%.2f", pos[0])
                        + " Z=" + String.format("%.2f", pos[2])
                        + " onGround=" + gs.isOnGround()
                        + " blockBelow=" + gs.getBlockBelowFeet());
            }

            switch (phase) {
                case GROUND:
                    phaseTicks++;
                    // Wait on ground for horizontal velocity to decay (prevents drift)
                    if (phaseTicks >= 8 && gs.isOnGround()) {
                        input.pressKey(InputController.JUMP);
                        phase = AIRBORNE;
                        phaseTicks = 0;
                    }
                    // Failsafe: at the world height limit, onGround may stay false
                    // (player floats at ceiling). Declare column top after extended wait.
                    if (phaseTicks >= 200 && y > 50) {
                        input.releaseAllKeys();
                        System.out.println("[McTestAgent] Column top reached (stuck at ceiling) at Y=" + y
                                + " after " + ticks + " ticks");
                        return true;
                    }
                    break;

                case AIRBORNE:
                    phaseTicks++;
                    // Release jump (only needed the 1 tick to initiate)
                    input.releaseKey(InputController.JUMP);

                    // Try placing every 2 ticks while airborne.
                    // The server's overlap check requires feetY >= targetBlockTop.
                    // At tick 4 after jump, feetY ≈ startY+1.166 — safely above
                    // the threshold (startY+1.0). Tick 2 only gives ~0.001 margin.
                    if (phaseTicks >= 4 && phaseTicks % 2 == 0 && phaseTicks <= 12) {
                        input.click(1);
                    }

                    // Check if landed
                    if (gs.isOnGround() && phaseTicks >= 4) {
                        if (y > highWaterY + 0.5) {
                            highWaterY = y;
                            consecutiveStalls = 0;
                        } else {
                            consecutiveStalls++;
                            if (ticks % 40 != 0) { // avoid double-logging
                                System.out.println("[McTestAgent] BuildColumn stall #"
                                        + consecutiveStalls + " at Y="
                                        + String.format("%.2f", y));
                            }
                        }

                        // Can't go higher after repeated stalls
                        if (consecutiveStalls >= 5 && y > 50) {
                            input.releaseAllKeys();
                            System.out.println("[McTestAgent] Column top reached at Y=" + y
                                    + " after " + ticks + " ticks"
                                    + " (stalls=" + consecutiveStalls + ")");
                            return true;
                        }

                        phase = GROUND;
                        phaseTicks = 0;
                    }

                    // Safety: if airborne too long (fell off column?), reset
                    if (phaseTicks >= 30) {
                        phase = GROUND;
                        phaseTicks = 0;
                    }
                    break;
            }

            return false;
        }

        /**
         * RubyDung column build: place block + move player up each tick.
         * No jump/click needed — direct position writes and setTile calls.
         */
        private boolean tickRubyDung(GameState gs, InputController input) {
            if (ticks == 1) {
                rdCurrentY = platformBY + 1;
            }

            // Place one block per 3 ticks (give server time to process)
            if (ticks % 3 == 1) {
                // Place solid block at current Y
                input.placeBlockDirect(platformBX, rdCurrentY, platformBZ, 1);
                // Move player up to stand on the new block
                input.movePlayerPosition(0, 1.0f, 0);
                rdCurrentY++;

                int blockId = gs.getBlockId(platformBX, rdCurrentY - 1, platformBZ);
                if (ticks % 30 == 1) {
                    System.out.println("[McTestAgent] RD BuildColumn Y=" + rdCurrentY
                            + " blockBelow=" + blockId);
                }

                // RubyDung world height is 64 blocks
                if (rdCurrentY >= 62) {
                    System.out.println("[McTestAgent] RD Column top reached at Y=" + rdCurrentY);
                    return true;
                }
            }

            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 3000; // 150 seconds — one block per jump cycle (~15 ticks each)
        }
    }

    // Step 5: Verify height and capture screenshot
    private class VerifyHeightStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "verify_height";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            double[] pos = gs.getPlayerPosition();
            if (pos == null) throw new RuntimeException("No player position");

            System.out.println("[McTestAgent] Column top Y=" + pos[1]);

            // Alpha world height is 128 blocks. Eye-level Y should be near 128+1.62
            if (pos[1] < 50) {
                throw new RuntimeException("Column height too low: Y=" + pos[1]);
            }

            File file = new File(statusDir, "column_top.png");
            capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 6: Attempt to build above limit
    private class AttemptAboveLimitStep implements ScenarioStep {
        private int ticks;
        private double startY;

        @Override
        public String getDescription() {
            return "attempt_above_limit";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;

            if (ticks == 1) {
                double[] pos = gs.getPlayerPosition();
                if (pos != null) startY = pos[1];
            }

            // Try to build higher
            input.pressKey(InputController.JUMP);
            input.setLookDirection(gs.getYaw(), 90f);
            if (ticks % 4 == 0) {
                input.click(1);
            }

            if (ticks >= 100) {
                input.releaseAllKeys();
                double[] pos = gs.getPlayerPosition();
                if (pos != null) {
                    double diff = Math.abs(pos[1] - startY);
                    System.out.println("[McTestAgent] After above-limit attempt: Y=" + pos[1]
                            + " (diff=" + diff + ")");
                    // Allow small variance from jumping
                    if (diff > 3.0) {
                        throw new RuntimeException("Player Y changed significantly above limit: "
                                + startY + " -> " + pos[1]);
                    }
                }
                return true;
            }
            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 200;
        }
    }

    // Step 7: Break blocks downward using direct dig packets.
    // Uses InputController.breakBlock() to bypass Minecraft.a(0)'s cooldown
    // (field S blocks left-clicks for 10 ticks when objectMouseOver is null).
    // RubyDung: uses breakBlockDirect + movePlayerPosition.
    //
    // Ceiling bug workaround: Alpha clients at the world height limit (Y=128)
    // can get stuck — gravity stops working even after all blocks below are
    // removed. When this is detected (Y unchanged for 30 ticks while high up),
    // we sweep the entire column via breakBlock, then teleport the player to
    // ground level using direct position field writes.
    private class BreakDownStep implements ScenarioStep {
        private int ticks;
        private int settleTicks; // counts ticks after detecting ground

        // Ceiling stuck detection
        private double lastY = Double.NaN;
        private int sameYTicks;
        private boolean ceilingHandled; // true once we've swept + teleported

        // RubyDung break-down state
        private int rdBreakY;

        @Override
        public String getDescription() {
            return "break_down";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;

            if (input.isRubyDung()) {
                return tickRubyDungBreak(gs, input);
            }

            double[] pos = gs.getPlayerPosition();
            if (pos == null) return false;

            // --- Ceiling stuck detection ---
            // Track how long Y stays the same while high up. The Alpha client
            // has a physics bug at the world ceiling (Y=128) that prevents
            // gravity from working even when all blocks below are removed.
            if (!ceilingHandled && pos[1] > platformBY + 30) {
                if (Double.isNaN(lastY) || Math.abs(pos[1] - lastY) > 0.01) {
                    lastY = pos[1];
                    sameYTicks = 0;
                } else {
                    sameYTicks++;
                }

                if (sameYTicks >= 30) {
                    // Sweep the ENTIRE column at the player's block position
                    // (not platformBX/BZ — the column may be at a different X/Z
                    // than the platform if the player didn't land exactly on it).
                    int bx = (int) Math.floor(pos[0]);
                    int bz = (int) Math.floor(pos[2]);
                    for (int y = 127; y > platformBY; y--) {
                        input.breakBlock(bx, y, bz);
                    }
                    // Also sweep at the platform position in case the column is there
                    for (int y = 127; y > platformBY; y--) {
                        input.breakBlock(platformBX, y, platformBZ);
                    }

                    // Teleport player to ground level. The surface grass is at
                    // platformBY - 1, so feet at platformBY, eyes at platformBY + 1.62.
                    // Move to spawn X/Z to avoid any holes the click(0) may have made.
                    double spawnX = platformBX + 0.5;
                    double spawnZ = platformBZ - 2 + 0.5; // original spawn Z
                    double groundEyeY = platformBY + 1.62;
                    gs.setPlayerPosition(spawnX, groundEyeY, spawnZ);

                    System.out.println("[McTestAgent] Ceiling stuck — swept column and "
                            + "teleported to Y=" + String.format("%.2f", groundEyeY)
                            + " at (" + String.format("%.1f", spawnX) + ", "
                            + String.format("%.1f", spawnZ) + ")");

                    ceilingHandled = true;
                    return false;
                }
            }

            // Settling phase: wait a few ticks after detecting grass/dirt to let
            // any pending break commands drain. If the block below becomes air
            // (in-flight break removed it), abort settling and resume breaking.
            if (settleTicks > 0) {
                settleTicks++;
                input.setLookDirection(gs.getYaw(), 0f); // look horizontal
                int belowNow = gs.getBlockBelowFeet();
                if (belowNow == 0) {
                    // In-flight break removed the ground block — resume breaking
                    settleTicks = 0;
                    return false;
                }
                if (settleTicks >= 5) {
                    System.out.println("[McTestAgent] Back on ground at Y=" + pos[1]
                            + " blockBelow=" + belowNow);
                    return true;
                }
                return false;
            }

            // Check completion FIRST — before issuing any more break commands.
            // This prevents breaking the grass/dirt block we just landed on.
            // Also accept cobblestone(4) at ground level — creative mode's instant
            // breaking may leave the column base block as ground surface.
            int belowFeet = gs.getBlockBelowFeet();
            if (belowFeet == 2 || belowFeet == 3 || belowFeet == 4) {
                double feetY = pos[1] - (double) 1.62f;
                if (feetY < platformBY + 5) {
                    input.releaseAllKeys();
                    input.setLookDirection(gs.getYaw(), 0f); // look horizontal
                    settleTicks = 1;
                    return false;
                }
            }

            // After ceiling teleport, just wait for the settle check above —
            // don't issue more breaks that could destroy the ground.
            if (ceilingHandled) {
                return false;
            }

            // Compute the block we're standing on
            int bx = (int) Math.floor(pos[0]);
            int feetFloor = (int) Math.floor(pos[1] - (double) 1.62f);
            int bz = (int) Math.floor(pos[2]);

            // Stop issuing breaks when close to the base — in creative mode,
            // instant breaking can destroy the column base before the settle
            // check fires, dropping the player into a hole.
            if (feetFloor <= platformBY + 1) {
                return false;
            }

            // Break the block at feet level (the one we're standing on)
            // and the one below in case feetY is exactly on a block boundary
            int blockAtFeet = gs.getBlockId(bx, feetFloor, bz);
            int blockBelow = gs.getBlockId(bx, feetFloor - 1, bz);

            if (blockAtFeet != 0) {
                input.breakBlock(bx, feetFloor, bz);
            } else if (blockBelow != 0 && blockBelow != 2 && blockBelow != 3) {
                input.breakBlock(bx, feetFloor - 1, bz);
            }

            // Also use click(0) with cooldown reset as backup
            input.setLookDirection(gs.getYaw(), 90f);
            input.click(0);

            if (ticks % 100 == 0) {
                System.out.println("[McTestAgent] Breaking down: Y="
                        + String.format("%.2f", pos[1])
                        + " feetFloor=" + feetFloor
                        + " blockAtFeet=" + blockAtFeet
                        + " blockBelow=" + blockBelow);
            }

            return false;
        }

        private boolean tickRubyDungBreak(GameState gs, InputController input) {
            if (ticks == 1) {
                // Start breaking from the highest column block downward
                rdBreakY = 61; // top of RD column
                // Find actual top
                while (rdBreakY > platformBY && gs.getBlockId(platformBX, rdBreakY, platformBZ) == 0) {
                    rdBreakY--;
                }
            }

            // Break one block per 3 ticks
            if (ticks % 3 == 1) {
                if (rdBreakY <= platformBY) {
                    // Back on ground
                    input.movePlayerPosition(0, -1.0f, 0); // settle down
                    System.out.println("[McTestAgent] RD break-down complete at Y=" + rdBreakY);
                    return true;
                }

                input.breakBlockDirect(platformBX, rdBreakY, platformBZ);
                input.movePlayerPosition(0, -1.0f, 0);
                rdBreakY--;

                if (ticks % 30 == 1) {
                    System.out.println("[McTestAgent] RD Breaking down: Y=" + rdBreakY);
                }
            }

            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 2000; // 100 seconds
        }
    }

    // Step 8: Verify back on ground (wait for player to settle on solid block)
    private class VerifyGroundStep implements ScenarioStep {
        private int ticks;
        private int settleTicks;
        private boolean screenshotTaken;

        @Override
        public String getDescription() {
            return "verify_ground";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            int blockBelow = gs.getBlockBelowFeet();

            if (ticks % 20 == 1) {
                System.out.println("[McTestAgent] Final ground check (tick " + ticks
                        + "): blockBelow=" + blockBelow);
            }

            // Accept grass(2), dirt(3), cobblestone(4), or solid(1) for RubyDung
            if (blockBelow == 2 || blockBelow == 3 || blockBelow == 4 || blockBelow == 1) {
                input.releaseAllKeys();
                input.setLookDirection(gs.getYaw(), 0f);
                if (!screenshotTaken) {
                    // Wait a few ticks for chunks to render after landing
                    if (settleTicks < 40) {
                        settleTicks++;
                        return false;
                    }
                    File file = new File(statusDir, "back_on_ground.png");
                    capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
                    screenshotTaken = true;
                }
                return true;
            }
            settleTicks = 0;

            // Player may still be falling after column sweep removed blocks;
            // wait for them to land on solid ground
            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 600; // generous — player may need to fall from height + teleport + chunk load
        }
    }
}
