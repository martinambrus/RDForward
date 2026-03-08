package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.McTestAgent;
import com.github.martinambrus.rdforward.e2e.agent.ScreenshotCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Cross-client scenario: two clients (primary=Alpha, secondary=Beta) connect
 * to the same server and verify chat visibility, block placement/breaking
 * propagation between them.
 *
 * Both clients run this same scenario class; behavior diverges based on
 * McTestAgent.role ("primary" or "secondary"). Coordination uses SyncBarrier
 * for file-based signaling.
 */
public class CrossClientScenario implements Scenario {

    @Override
    public String getName() {
        return "cross_client";
    }

    @Override
    public List<ScenarioStep> getSteps() {
        String role = McTestAgent.role;
        if ("secondary".equals(role)) {
            return getSecondarySteps();
        }
        return getPrimarySteps();
    }

    // Block coords computed by this client's place step, reused for breaking
    private int myBlockX, myBlockY, myBlockZ;

    // Yaw to face the other player, saved for re-use before final screenshots
    private float facingOtherYaw;

    private SyncBarrier getBarrier() {
        return new SyncBarrier(McTestAgent.syncDir, McTestAgent.role);
    }

    /**
     * Compute block placement target relative to current player position.
     * Places 1 block to the right (+X) on top of ground. Always within reach.
     */
    private void computeMyBlockCoords(GameState gs) {
        double[] pos = gs.getPlayerPosition();
        if (pos == null) throw new RuntimeException("No player position for block coords");
        int px = (int) Math.floor(pos[0]);
        int feetY = (int) Math.floor(pos[1] - (double) 1.62f);
        int groundY = feetY - 1;
        int pz = (int) Math.floor(pos[2]);
        // If feet are on the surface block (spawn edge case with 64-height world),
        // treat feet level as ground so we place on top of it
        if (gs.getBlockId(px, feetY, pz) != 0) {
            groundY = feetY;
        }
        myBlockX = px + 1;
        myBlockY = groundY + 1; // on top of ground
        myBlockZ = pz;
        System.out.println("[McTestAgent] Computed block target: ("
                + myBlockX + "," + myBlockY + "," + myBlockZ
                + ") from player pos (" + pos[0] + "," + pos[1] + "," + pos[2] + ")");
    }

    /**
     * Reusable screenshot step with a specific name.
     * Used to capture screenshots at cross-client interaction points (steps 25-30).
     */
    private class NamedScreenshotStep implements ScenarioStep {
        private final String name;
        NamedScreenshotStep(String name) { this.name = name; }
        @Override public String getDescription() { return name; }
        @Override public int getTimeoutTicks() { return 20; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            File file = new File(statusDir, name + ".png");
            capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
            System.out.println("[McTestAgent] Named screenshot: " + name + ".png");
            return true;
        }
    }

    // ======================== PRIMARY STEPS ========================

    private List<ScenarioStep> getPrimarySteps() {
        List<ScenarioStep> steps = new ArrayList<ScenarioStep>();
        steps.add(new PrimaryWaitWorldReady());
        steps.add(new PrimaryWaitSecondaryJoined());
        steps.add(new PrimaryWalkBackward());
        steps.add(new PrimaryTurnToSecondary());
        steps.add(new PrimaryWaitSecondaryChat());
        steps.add(new NamedScreenshotStep("cross_step25_chat_received"));  // step 25
        steps.add(new PrimarySendChat());
        steps.add(new NamedScreenshotStep("cross_step26_chat_sent"));      // step 26
        steps.add(new PrimaryWaitSecondarySawChat());
        steps.add(new PrimaryPlaceBlock());
        steps.add(new NamedScreenshotStep("cross_step27_block_placed"));   // step 27
        steps.add(new PrimaryWaitSecondarySawBlock());
        steps.add(new PrimaryBreakBlock());
        steps.add(new NamedScreenshotStep("cross_step28_block_broken"));   // step 28
        steps.add(new PrimaryWaitSecondarySawBreak());
        steps.add(new PrimaryWaitSecondaryBlockPlaced());
        steps.add(new PrimaryVerifySecondaryBlock());
        steps.add(new NamedScreenshotStep("cross_step29_secondary_block")); // step 29
        steps.add(new PrimaryWaitSecondaryBlockBroken());
        steps.add(new PrimaryVerifySecondaryBreak());
        steps.add(new NamedScreenshotStep("cross_step30_secondary_break")); // step 30
        steps.add(new PrimaryScreenshot());
        return steps;
    }

    private class PrimaryWaitWorldReady implements ScenarioStep {
        @Override public String getDescription() { return "wait_world_ready"; }
        @Override public int getTimeoutTicks() { return 20; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            getBarrier().signal("world_ready");
            return true;
        }
    }

    private class PrimaryWaitSecondaryJoined implements ScenarioStep {
        @Override public String getDescription() { return "wait_secondary_joined"; }
        // RubyDung ticks per-frame (~60 FPS), not at 20 TPS, so tick counts
        // translate to much shorter wall time. 12000 ticks ≈ 200s at 60 FPS,
        // enough for LWJGL3/Modern secondaries that need ~90-120s to stabilize.
        @Override public int getTimeoutTicks() { return 12000; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            return getBarrier().waitFor("world_ready");
        }
    }

    private class PrimaryWalkBackward implements ScenarioStep {
        private int ticks;
        @Override public String getDescription() { return "walk_backward"; }
        @Override public int getTimeoutTicks() { return 80; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (input.isRubyDung()) {
                // RD: move away from spawn by 2 blocks in +Z.
                if (ticks == 1) {
                    input.movePlayerPosition(0, 0, +2.0f);
                }
                return ticks >= 5;
            }
            // Set deterministic facing direction before walking.
            // Face north (180° = -Z), so BACK key walks +Z away from spawn.
            // This avoids random walk direction from GLFW mouse deltas under Xvfb.
            input.setLookDirection(180f, 0f);
            if (ticks == 1) {
                input.pressKey(InputController.BACK);
            }
            // Walk for ~40 ticks (~2 blocks backward = +Z)
            if (ticks >= 40) {
                input.releaseKey(InputController.BACK);
                return true;
            }
            return false;
        }
    }

    private class PrimaryTurnToSecondary implements ScenarioStep {
        private int ticks;
        @Override public String getDescription() { return "turn_to_secondary"; }
        @Override public int getTimeoutTicks() { return 20; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            ticks++;
            // Primary walked +Z from spawn. Secondary is at spawn (-Z relative).
            // Face north to look at secondary.
            // Alpha/Beta: 180° = north (-Z). RubyDung: 0° = north (-Z).
            facingOtherYaw = input.isRubyDung() ? 0f : 180f;
            input.setLookDirection(facingOtherYaw, 0f);
            return ticks >= 3;
        }
    }

    private class PrimaryWaitSecondaryChat implements ScenarioStep {
        @Override public String getDescription() { return "wait_secondary_chat"; }
        @Override public int getTimeoutTicks() { return 600; } // 30 seconds
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            // Maintain camera direction during wait to prevent LWJGL mouse drift
            input.setLookDirection(facingOtherYaw, 0f);
            List<String> messages = gs.getChatMessages(20);
            for (String msg : messages) {
                if (msg.contains("HERE I AM")) {
                    System.out.println("[McTestAgent] Primary saw secondary chat: " + msg);
                    return true;
                }
            }
            return false;
        }
    }

    private class PrimarySendChat implements ScenarioStep {
        @Override public String getDescription() { return "send_own_chat"; }
        @Override public int getTimeoutTicks() { return 20; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            input.sendChatMessage("I SEE YOU");
            getBarrier().signal("primary_chat_sent");
            return true;
        }
    }

    private class PrimaryWaitSecondarySawChat implements ScenarioStep {
        @Override public String getDescription() { return "wait_secondary_saw_chat"; }
        @Override public int getTimeoutTicks() { return 600; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            input.setLookDirection(facingOtherYaw, 0f);
            return getBarrier().waitFor("saw_primary_chat");
        }
    }

    private class PrimaryPlaceBlock implements ScenarioStep {
        private int ticks;
        @Override public String getDescription() { return "place_block_right"; }
        @Override public int getTimeoutTicks() { return 200; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            ticks++;
            // Compute block coords on first tick (relative to current position)
            if (ticks == 1) {
                computeMyBlockCoords(gs);
                if (input.isRubyDung()) {
                    input.placeBlockDirect(myBlockX, myBlockY, myBlockZ, 1);
                }
            }
            if (!input.isRubyDung()) {
                // Look at the ground block where we want to place on top
                input.lookAtBlock(myBlockX, myBlockY - 1, myBlockZ);
                // Right-click every other tick (matches single-version pattern).
                // Clicking every tick triggers the client's rightClickDelay which
                // suppresses subsequent clicks for 4 ticks.
                if (ticks >= 3 && ticks <= 60 && ticks % 2 == 1) {
                    input.click(1);
                }
            }
            if (ticks > 6 || (input.isRubyDung() && ticks > 3)) {
                int placed = gs.getBlockId(myBlockX, myBlockY, myBlockZ);
                if (placed != 0) {
                    System.out.println("[McTestAgent] Primary placed block id=" + placed
                            + " at (" + myBlockX + "," + myBlockY + "," + myBlockZ + ")");
                    SyncBarrier barrier = getBarrier();
                    barrier.writeData("block_x", String.valueOf(myBlockX));
                    barrier.writeData("block_y", String.valueOf(myBlockY));
                    barrier.writeData("block_z", String.valueOf(myBlockZ));
                    barrier.signal("primary_block_placed");
                    return true;
                }
            }
            return false;
        }
    }

    private class PrimaryWaitSecondarySawBlock implements ScenarioStep {
        @Override public String getDescription() { return "wait_secondary_saw_block"; }
        @Override public int getTimeoutTicks() { return 600; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            input.setLookDirection(facingOtherYaw, 0f);
            return getBarrier().waitFor("saw_primary_block");
        }
    }

    private class PrimaryBreakBlock implements ScenarioStep {
        private int ticks;
        @Override public String getDescription() { return "break_placed_block"; }
        @Override public int getTimeoutTicks() { return 100; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            ticks++;
            int blockId = gs.getBlockId(myBlockX, myBlockY, myBlockZ);
            if (blockId == 0) {
                System.out.println("[McTestAgent] Primary block broken at tick " + ticks);
                getBarrier().signal("primary_block_broken");
                return true;
            }
            if (input.isRubyDung()) {
                input.breakBlockDirect(myBlockX, myBlockY, myBlockZ);
            } else {
                input.lookAtBlock(myBlockX, myBlockY, myBlockZ);
                input.click(0);
            }
            return false;
        }
    }

    private class PrimaryWaitSecondarySawBreak implements ScenarioStep {
        @Override public String getDescription() { return "wait_secondary_saw_break"; }
        @Override public int getTimeoutTicks() { return 600; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            input.setLookDirection(facingOtherYaw, 0f);
            return getBarrier().waitFor("saw_primary_break");
        }
    }

    private class PrimaryWaitSecondaryBlockPlaced implements ScenarioStep {
        @Override public String getDescription() { return "wait_secondary_block_placed"; }
        @Override public int getTimeoutTicks() { return 600; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            input.setLookDirection(facingOtherYaw, 0f);
            return getBarrier().waitFor("secondary_block_placed");
        }
    }

    private class PrimaryVerifySecondaryBlock implements ScenarioStep {
        @Override public String getDescription() { return "verify_secondary_block"; }
        @Override public int getTimeoutTicks() { return 200; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            SyncBarrier barrier = getBarrier();
            String sx = barrier.readData("block_x");
            String sy = barrier.readData("block_y");
            String sz = barrier.readData("block_z");
            if (sx == null || sy == null || sz == null) return false;
            int bx = Integer.parseInt(sx);
            int by = Integer.parseInt(sy);
            int bz = Integer.parseInt(sz);
            int blockId = gs.getBlockId(bx, by, bz);
            System.out.println("[McTestAgent] Primary sees secondary block at ("
                    + bx + "," + by + "," + bz + ") id=" + blockId);
            if (blockId != 0) {
                barrier.signal("saw_secondary_block");
                return true;
            }
            return false;
        }
    }

    private class PrimaryWaitSecondaryBlockBroken implements ScenarioStep {
        @Override public String getDescription() { return "wait_secondary_block_broken"; }
        @Override public int getTimeoutTicks() { return 600; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            input.setLookDirection(facingOtherYaw, 0f);
            return getBarrier().waitFor("secondary_block_broken");
        }
    }

    private class PrimaryVerifySecondaryBreak implements ScenarioStep {
        @Override public String getDescription() { return "verify_secondary_break"; }
        @Override public int getTimeoutTicks() { return 200; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            SyncBarrier barrier = getBarrier();
            String sx = barrier.readData("block_x");
            String sy = barrier.readData("block_y");
            String sz = barrier.readData("block_z");
            if (sx == null || sy == null || sz == null) return false;
            int bx = Integer.parseInt(sx);
            int by = Integer.parseInt(sy);
            int bz = Integer.parseInt(sz);
            int blockId = gs.getBlockId(bx, by, bz);
            System.out.println("[McTestAgent] Primary sees secondary break at ("
                    + bx + "," + by + "," + bz + ") id=" + blockId);
            if (blockId == 0) {
                barrier.signal("saw_secondary_break");
                return true;
            }
            return false;
        }
    }

    private class PrimaryScreenshot implements ScenarioStep {
        private int ticks;
        private long startTimeMs;
        @Override public String getDescription() { return "screenshot"; }
        @Override public int getTimeoutTicks() { return 200; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            if (ticks == 0) startTimeMs = System.currentTimeMillis();
            ticks++;
            // Set look direction every tick to override accumulated mouse drift.
            // LWJGL mouse deltas can leak through between game's Mouse.setGrabbed(true)
            // in tick() and our suppression in TickAdvice. Wall-time wait ensures
            // enough render cycles for the camera direction to stabilize.
            input.setLookDirection(facingOtherYaw, 0f);
            if (input.isRubyDung()) {
                if (System.currentTimeMillis() - startTimeMs < 3000) return false;
            } else {
                if (ticks < 20) return false;
            }
            File file = new File(statusDir, "cross_client_primary.png");
            capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
            return true;
        }
    }

    // ======================== SECONDARY STEPS ========================

    private List<ScenarioStep> getSecondarySteps() {
        List<ScenarioStep> steps = new ArrayList<ScenarioStep>();
        steps.add(new SecondaryWaitWorldReady());
        steps.add(new SecondaryWaitPrimaryJoined());
        steps.add(new SecondaryTurnToPrimary());
        steps.add(new SecondarySendChat());
        steps.add(new NamedScreenshotStep("cross_step25_chat_sent"));      // step 25
        steps.add(new SecondaryWaitPrimaryChat());
        steps.add(new NamedScreenshotStep("cross_step26_chat_received"));  // step 26
        steps.add(new SecondaryWaitPrimaryBlockPlaced());
        steps.add(new SecondaryVerifyPrimaryBlock());
        steps.add(new NamedScreenshotStep("cross_step27_primary_block"));  // step 27
        steps.add(new SecondaryWaitPrimaryBlockBroken());
        steps.add(new SecondaryVerifyPrimaryBreak());
        steps.add(new NamedScreenshotStep("cross_step28_primary_break"));  // step 28
        steps.add(new SecondaryPlaceBlock());
        steps.add(new NamedScreenshotStep("cross_step29_block_placed"));   // step 29
        steps.add(new SecondaryWaitPrimarySawBlock());
        steps.add(new SecondaryBreakBlock());
        steps.add(new NamedScreenshotStep("cross_step30_block_broken"));   // step 30
        steps.add(new SecondaryWaitPrimarySawBreak());
        steps.add(new SecondaryScreenshot());
        return steps;
    }

    private class SecondaryWaitWorldReady implements ScenarioStep {
        @Override public String getDescription() { return "wait_world_ready"; }
        @Override public int getTimeoutTicks() { return 20; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            getBarrier().signal("world_ready");
            return true;
        }
    }

    private class SecondaryWaitPrimaryJoined implements ScenarioStep {
        @Override public String getDescription() { return "wait_primary_joined"; }
        @Override public int getTimeoutTicks() { return 600; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            return getBarrier().waitFor("world_ready");
        }
    }

    /**
     * Turn secondary to face the primary (who walked backward from spawn).
     * Uses yaw+180 from spawn yaw, same logic as primary's turn.
     */
    private class SecondaryTurnToPrimary implements ScenarioStep {
        private int ticks;
        @Override public String getDescription() { return "turn_to_primary"; }
        @Override public int getTimeoutTicks() { return 20; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            ticks++;
            // Secondary is at spawn. Primary walked +Z from spawn.
            // Face south to look at primary.
            // Alpha/Beta: 0° = south (+Z). RubyDung: 180° = south (+Z).
            facingOtherYaw = input.isRubyDung() ? 180f : 0f;
            input.setLookDirection(facingOtherYaw, 0f);
            return ticks >= 3;
        }
    }

    private class SecondarySendChat implements ScenarioStep {
        private int ticks;
        @Override public String getDescription() { return "send_chat"; }
        @Override public int getTimeoutTicks() { return 40; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks < 5) return false;
            input.sendChatMessage("HERE I AM");
            getBarrier().signal("secondary_chat_sent");
            return true;
        }
    }

    private class SecondaryWaitPrimaryChat implements ScenarioStep {
        @Override public String getDescription() { return "wait_primary_chat"; }
        @Override public int getTimeoutTicks() { return 600; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            input.setLookDirection(facingOtherYaw, 0f);
            List<String> messages = gs.getChatMessages(20);
            for (String msg : messages) {
                if (msg.contains("I SEE YOU")) {
                    System.out.println("[McTestAgent] Secondary saw primary chat: " + msg);
                    getBarrier().signal("saw_primary_chat");
                    return true;
                }
            }
            return false;
        }
    }

    private class SecondaryWaitPrimaryBlockPlaced implements ScenarioStep {
        @Override public String getDescription() { return "wait_primary_block_placed"; }
        @Override public int getTimeoutTicks() { return 600; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            input.setLookDirection(facingOtherYaw, 0f);
            return getBarrier().waitFor("primary_block_placed");
        }
    }

    private class SecondaryVerifyPrimaryBlock implements ScenarioStep {
        @Override public String getDescription() { return "verify_primary_block"; }
        @Override public int getTimeoutTicks() { return 200; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            SyncBarrier barrier = getBarrier();
            String sx = barrier.readData("block_x");
            String sy = barrier.readData("block_y");
            String sz = barrier.readData("block_z");
            if (sx == null || sy == null || sz == null) return false;
            int bx = Integer.parseInt(sx);
            int by = Integer.parseInt(sy);
            int bz = Integer.parseInt(sz);
            int blockId = gs.getBlockId(bx, by, bz);
            System.out.println("[McTestAgent] Secondary sees primary block at ("
                    + bx + "," + by + "," + bz + ") id=" + blockId);
            if (blockId != 0) {
                barrier.signal("saw_primary_block");
                return true;
            }
            return false;
        }
    }

    private class SecondaryWaitPrimaryBlockBroken implements ScenarioStep {
        @Override public String getDescription() { return "wait_primary_block_broken"; }
        @Override public int getTimeoutTicks() { return 600; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            input.setLookDirection(facingOtherYaw, 0f);
            return getBarrier().waitFor("primary_block_broken");
        }
    }

    private class SecondaryVerifyPrimaryBreak implements ScenarioStep {
        @Override public String getDescription() { return "verify_primary_break"; }
        @Override public int getTimeoutTicks() { return 200; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            SyncBarrier barrier = getBarrier();
            String sx = barrier.readData("block_x");
            String sy = barrier.readData("block_y");
            String sz = barrier.readData("block_z");
            if (sx == null || sy == null || sz == null) return false;
            int bx = Integer.parseInt(sx);
            int by = Integer.parseInt(sy);
            int bz = Integer.parseInt(sz);
            int blockId = gs.getBlockId(bx, by, bz);
            System.out.println("[McTestAgent] Secondary sees primary break at ("
                    + bx + "," + by + "," + bz + ") id=" + blockId);
            if (blockId == 0) {
                barrier.signal("saw_primary_break");
                return true;
            }
            return false;
        }
    }

    private class SecondaryPlaceBlock implements ScenarioStep {
        private int ticks;
        @Override public String getDescription() { return "place_block_right"; }
        @Override public int getTimeoutTicks() { return 200; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (ticks == 1) {
                computeMyBlockCoords(gs);
                if (input.isRubyDung()) {
                    input.placeBlockDirect(myBlockX, myBlockY, myBlockZ, 1);
                }
            }
            if (!input.isRubyDung()) {
                input.lookAtBlock(myBlockX, myBlockY - 1, myBlockZ);
                // Right-click every other tick (matches single-version pattern).
                if (ticks >= 3 && ticks <= 60 && ticks % 2 == 1) {
                    input.click(1);
                }
                // Debug: log state every 10 ticks
                if (ticks % 10 == 0 || ticks <= 5) {
                    int belowId = gs.getBlockId(myBlockX, myBlockY - 1, myBlockZ);
                    int targetId = gs.getBlockId(myBlockX, myBlockY, myBlockZ);
                    int[][] slots = gs.getInventorySlots();
                    String heldItem = "no_inv";
                    if (slots != null && slots.length > 0) {
                        heldItem = "slot0=[id=" + slots[0][0] + ",count=" + slots[0][1] + "]";
                    }
                    double[] pos = gs.getPlayerPosition();
                    String posStr = pos != null
                            ? String.format("pos=(%.2f,%.2f,%.2f)", pos[0], pos[1], pos[2])
                            : "pos=null";
                    System.out.println("[McTestAgent] PlaceDebug tick=" + ticks
                            + " " + posStr
                            + " below(" + myBlockX + "," + (myBlockY-1) + "," + myBlockZ + ")=" + belowId
                            + " target(" + myBlockX + "," + myBlockY + "," + myBlockZ + ")=" + targetId
                            + " hitResult=" + gs.getHitResultInfo()
                            + " clickInfo=" + input.getRightClickDebug()
                            + " held=" + heldItem);
                }
            }
            if (ticks > 6 || (input.isRubyDung() && ticks > 3)) {
                int placed = gs.getBlockId(myBlockX, myBlockY, myBlockZ);
                if (placed != 0) {
                    System.out.println("[McTestAgent] Secondary placed block id=" + placed
                            + " at (" + myBlockX + "," + myBlockY + "," + myBlockZ + ")");
                    SyncBarrier barrier = getBarrier();
                    barrier.writeData("block_x", String.valueOf(myBlockX));
                    barrier.writeData("block_y", String.valueOf(myBlockY));
                    barrier.writeData("block_z", String.valueOf(myBlockZ));
                    barrier.signal("secondary_block_placed");
                    return true;
                }
            }
            return false;
        }
    }

    private class SecondaryWaitPrimarySawBlock implements ScenarioStep {
        @Override public String getDescription() { return "wait_primary_saw_block"; }
        @Override public int getTimeoutTicks() { return 600; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            input.setLookDirection(facingOtherYaw, 0f);
            return getBarrier().waitFor("saw_secondary_block");
        }
    }

    private class SecondaryBreakBlock implements ScenarioStep {
        private int ticks;
        @Override public String getDescription() { return "break_placed_block"; }
        @Override public int getTimeoutTicks() { return 100; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            ticks++;
            int blockId = gs.getBlockId(myBlockX, myBlockY, myBlockZ);
            if (blockId == 0) {
                System.out.println("[McTestAgent] Secondary block broken at tick " + ticks);
                getBarrier().signal("secondary_block_broken");
                return true;
            }
            if (input.isRubyDung()) {
                input.breakBlockDirect(myBlockX, myBlockY, myBlockZ);
            } else {
                input.lookAtBlock(myBlockX, myBlockY, myBlockZ);
                input.click(0);
            }
            return false;
        }
    }

    private class SecondaryWaitPrimarySawBreak implements ScenarioStep {
        @Override public String getDescription() { return "wait_primary_saw_break"; }
        @Override public int getTimeoutTicks() { return 600; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            input.setLookDirection(facingOtherYaw, 0f);
            return getBarrier().waitFor("saw_secondary_break");
        }
    }

    private class SecondaryScreenshot implements ScenarioStep {
        private int ticks;
        private long startTimeMs;
        @Override public String getDescription() { return "screenshot"; }
        @Override public int getTimeoutTicks() { return 200; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            if (ticks == 0) startTimeMs = System.currentTimeMillis();
            ticks++;
            // Set look direction every tick to override accumulated mouse drift.
            input.setLookDirection(facingOtherYaw, 0f);
            if (input.isRubyDung()) {
                if (System.currentTimeMillis() - startTimeMs < 3000) return false;
            } else {
                if (ticks < 20) return false;
            }
            File file = new File(statusDir, "cross_client_secondary.png");
            capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
            return true;
        }
    }
}
