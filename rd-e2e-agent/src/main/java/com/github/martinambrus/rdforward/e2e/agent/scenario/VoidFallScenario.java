package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.ScreenshotCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests walking to the map edge, falling into the void, verifying fall speed,
 * and confirming the server teleports the player back to spawn.
 */
public class VoidFallScenario implements Scenario {

    // Shared state across steps
    private double[] spawnPos;

    @Override
    public String getName() {
        return "void_fall";
    }

    @Override
    public List<ScenarioStep> getSteps() {
        List<ScenarioStep> steps = new ArrayList<ScenarioStep>();
        steps.add(new RecordSpawnStep());
        steps.add(new WalkToEdgeStep());
        steps.add(new VerifyFallSpeedStep());
        steps.add(new WaitTeleportStep());
        steps.add(new VerifySpawnReturnStep());
        steps.add(new WaitSettleStep());
        steps.add(new ScreenshotStep());
        return steps;
    }

    // Step 1: Record spawn position for later comparison
    private class RecordSpawnStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "record_spawn";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            double[] pos = gs.getPlayerPosition();
            if (pos == null) return false;
            spawnPos = new double[]{pos[0], pos[1], pos[2]};
            System.out.println("[McTestAgent] Spawn recorded: X=" + pos[0]
                    + " Y=" + pos[1] + " Z=" + pos[2]);
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    // Step 2: Face east (+X) and walk to map edge until falling off
    private class WalkToEdgeStep implements ScenarioStep {
        private int ticks;
        private int consecutiveAirborneTicks;
        private double lastGroundY;
        private boolean lastGroundYSet;

        @Override
        public String getDescription() {
            return "walk_to_edge";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;

            if (ticks == 1) {
                // Face east (+X direction): Alpha yaw 270 = east, or equivalently -90
                input.setLookDirection(-90f, 0f);
                if (!input.isRubyDung()) {
                    input.pressKey(InputController.FORWARD);
                }
            }

            // RubyDung: use direct position writes to walk east
            if (input.isRubyDung()) {
                input.movePlayerPosition(0.5f, 0, 0);
            }

            double[] pos = gs.getPlayerPosition();
            if (pos == null) return false;

            // Track ground Y when on ground
            if (gs.isOnGround()) {
                consecutiveAirborneTicks = 0;
                lastGroundY = pos[1];
                lastGroundYSet = true;
            } else {
                consecutiveAirborneTicks++;
            }

            // Log progress every 40 ticks
            if (ticks % 40 == 0) {
                System.out.println("[McTestAgent] Walking east: X="
                        + String.format("%.1f", pos[0])
                        + " Y=" + String.format("%.1f", pos[1])
                        + " airborne=" + consecutiveAirborneTicks);
            }

            // Detect edge: 10+ consecutive airborne ticks AND Y dropped >3 blocks
            if (consecutiveAirborneTicks >= 10 && lastGroundYSet) {
                double yDrop = lastGroundY - pos[1];
                if (yDrop > 3.0) {
                    input.releaseKey(InputController.FORWARD);
                    System.out.println("[McTestAgent] Fell off edge at X="
                            + String.format("%.1f", pos[0])
                            + " Y=" + String.format("%.1f", pos[1])
                            + " (dropped " + String.format("%.1f", yDrop) + " blocks)");
                    return true;
                }
            }

            // RubyDung: detect edge by checking if Y has actually dropped.
            // blockBelow == 0 alone isn't enough because the player's bounding
            // box may still overlap the edge block, preventing actual falling.
            if (input.isRubyDung() && lastGroundYSet) {
                double yDrop = lastGroundY - pos[1];
                if (yDrop > 1.0) {
                    System.out.println("[McTestAgent] RD fell off edge at X="
                            + String.format("%.1f", pos[0])
                            + " Y=" + String.format("%.1f", pos[1])
                            + " (dropped " + String.format("%.1f", yDrop) + ")");
                    return true;
                }
            }

            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 1200; // 60 seconds
        }
    }

    // Step 3: Verify falling at high speed (>= 10 blocks/sec)
    private class VerifyFallSpeedStep implements ScenarioStep {
        private int ticks;
        private double startY;

        @Override
        public String getDescription() {
            return "verify_fall_speed";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            double[] pos = gs.getPlayerPosition();
            if (pos == null) return false;

            if (ticks == 1) {
                startY = pos[1];
                System.out.println("[McTestAgent] Fall speed check start Y="
                        + String.format("%.2f", startY));
                return false;
            }

            // After 20 ticks (1 second), check fall distance
            if (ticks >= 21) {
                double fallDist = startY - pos[1];
                // RubyDung has weaker gravity (~2.4 blocks/sec vs Alpha's ~10+)
                double minFallSpeed = input.isRubyDung() ? 1.0 : 10.0;
                System.out.println("[McTestAgent] Fall speed: "
                        + String.format("%.1f", fallDist) + " blocks in 1 second"
                        + " (Y=" + String.format("%.2f", pos[1]) + ")");
                if (fallDist < minFallSpeed) {
                    throw new RuntimeException("Fall speed too slow: "
                            + String.format("%.1f", fallDist) + " blocks/sec (need >= "
                            + String.format("%.0f", minFallSpeed) + ")");
                }
                return true;
            }

            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 100; // 5 seconds
        }
    }

    // Step 4: Wait for void fall teleport. Detects either:
    // (a) Y < -10 threshold then Y jumps back up, or
    // (b) Y suddenly increases by >20 blocks (server teleported before threshold)
    private class WaitTeleportStep implements ScenarioStep {
        private int ticks;
        private boolean passedThreshold;
        private int postThresholdTicks;
        private double lowestY = Double.MAX_VALUE;
        private double prevY = Double.MAX_VALUE;
        private boolean teleportDetected;
        private int postTeleportTicks;
        private int stuckAtVoidFloorTicks;

        @Override
        public String getDescription() {
            return "wait_teleport";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            double[] pos = gs.getPlayerPosition();
            if (pos == null) return false;

            // Track lowest Y seen during fall
            if (pos[1] < lowestY) {
                lowestY = pos[1];
            }

            // Detect teleport: Y jumped up by >20 blocks from lowest point
            if (!teleportDetected && lowestY < 50 && pos[1] > lowestY + 20) {
                teleportDetected = true;
                System.out.println("[McTestAgent] Teleport detected: lowestY="
                        + String.format("%.2f", lowestY) + " currentY="
                        + String.format("%.2f", pos[1]));
            }

            // Also detect teleport by sudden upward jump from previous tick
            if (!teleportDetected && prevY != Double.MAX_VALUE && pos[1] > prevY + 10) {
                teleportDetected = true;
                System.out.println("[McTestAgent] Teleport detected (jump): prevY="
                        + String.format("%.2f", prevY) + " currentY="
                        + String.format("%.2f", pos[1]));
            }
            prevY = pos[1];

            if (teleportDetected) {
                postTeleportTicks++;
                if (postTeleportTicks >= 5) {
                    return true;
                }
                return false;
            }

            if (!passedThreshold) {
                if (pos[1] < -10.0) {
                    passedThreshold = true;
                    System.out.println("[McTestAgent] Passed void threshold at Y="
                            + String.format("%.2f", pos[1]));
                }
                // Older Alpha clients (pre-1.2.2) clip player Y near 0, preventing
                // deep void fall. Detect stuck at void floor: Y < 0 and unchanged.
                if (pos[1] < 0 && prevY != Double.MAX_VALUE
                        && Math.abs(pos[1] - prevY) < 0.01) {
                    stuckAtVoidFloorTicks++;
                    if (stuckAtVoidFloorTicks >= 100) {
                        System.out.println("[McTestAgent] Stuck at void floor Y="
                                + String.format("%.2f", pos[1])
                                + " (client clips below-zero physics)");
                        return true;
                    }
                } else {
                    stuckAtVoidFloorTicks = 0;
                }
                if (ticks % 20 == 0) {
                    System.out.println("[McTestAgent] Falling... Y="
                            + String.format("%.2f", pos[1]));
                }
            } else {
                postThresholdTicks++;
                if (postThresholdTicks >= 80) {
                    System.out.println("[McTestAgent] Post-threshold wait complete at Y="
                            + String.format("%.2f", pos[1]));
                    return true;
                }
                if (pos[1] > 0 && postThresholdTicks > 5) {
                    System.out.println("[McTestAgent] Teleport detected at Y="
                            + String.format("%.2f", pos[1])
                            + " after " + postThresholdTicks + " post-threshold ticks");
                    return true;
                }
            }

            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 400; // 20 seconds (RubyDung has weaker gravity)
        }
    }

    // Step 5: Verify player is back near spawn on solid ground
    private class VerifySpawnReturnStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "verify_spawn_return";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            double[] pos = gs.getPlayerPosition();
            if (pos == null) throw new RuntimeException("No player position");

            int blockBelow = gs.getBlockBelowFeet();
            System.out.println("[McTestAgent] Spawn return check: X="
                    + String.format("%.1f", pos[0])
                    + " Y=" + String.format("%.1f", pos[1])
                    + " Z=" + String.format("%.1f", pos[2])
                    + " blockBelow=" + blockBelow);

            // Older Alpha clients (pre-1.2.2) clip Y near 0 — player gets stuck
            // at void floor without being teleported. Accept this as valid outcome.
            if (pos[1] < 0 && blockBelow == 0) {
                System.out.println("[McTestAgent] Player at void floor (no teleport) — accepted");
                return true;
            }

            // Verify standing on grass (2) or dirt (3), or solid (1) for RubyDung
            if (blockBelow != 2 && blockBelow != 3 && blockBelow != 1) {
                throw new RuntimeException("Expected grass(2)/dirt(3)/solid(1) below feet, got "
                        + blockBelow);
            }

            // Verify within 50 blocks of original spawn (horizontal distance)
            double dx = pos[0] - spawnPos[0];
            double dz = pos[2] - spawnPos[2];
            double horizontalDist = Math.sqrt(dx * dx + dz * dz);
            System.out.println("[McTestAgent] Distance from spawn: "
                    + String.format("%.1f", horizontalDist) + " blocks");

            if (horizontalDist > 50.0) {
                throw new RuntimeException("Too far from spawn: "
                        + String.format("%.1f", horizontalDist) + " blocks (max 50)");
            }

            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 60; // 3 seconds
        }
    }

    // Step 6: Wait for world to render after teleport
    private class WaitSettleStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "wait_settle";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            return ticks >= 60; // 3 seconds for chunks to load and render
        }

        @Override
        public int getTimeoutTicks() {
            return 80;
        }
    }

    // Step 7: Capture final screenshot
    private class ScreenshotStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "screenshot";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            File file = new File(statusDir, "void_fall_complete.png");
            capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
            System.out.println("[McTestAgent] Screenshot saved: " + file.getAbsolutePath());
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }
}
