package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.McTestAgent;
import com.github.martinambrus.rdforward.e2e.agent.ScreenshotCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Cross-client scenario: two clients connect to the same server and verify
 * chat visibility and player rendering between them.
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

    // Yaw to face the other player, saved for re-use before final screenshots
    private float facingOtherYaw;

    private SyncBarrier getBarrier() {
        return new SyncBarrier(McTestAgent.syncDir, McTestAgent.role);
    }

    // ======================== PRIMARY STEPS ========================

    private List<ScenarioStep> getPrimarySteps() {
        List<ScenarioStep> steps = new ArrayList<ScenarioStep>();
        steps.add(new PrimaryWaitWorldReady());
        steps.add(new PrimaryWaitSecondaryJoined());
        steps.add(new PrimaryWalkBackward());
        steps.add(new PrimaryTurnToSecondary());
        steps.add(new PrimaryWaitSecondaryChat());
        steps.add(new PrimarySendChat());
        steps.add(new PrimaryWaitSecondarySawChat());
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

    private class PrimaryScreenshot implements ScenarioStep {
        private int ticks;
        private long startTimeMs;
        @Override public String getDescription() { return "screenshot"; }
        @Override public int getTimeoutTicks() { return 600; }
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
        steps.add(new SecondaryWalkBackward());
        steps.add(new SecondaryTurnToPrimary());
        steps.add(new SecondarySendChat());
        steps.add(new SecondaryWaitPrimaryChat());
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

    private class SecondaryWalkBackward implements ScenarioStep {
        private int ticks;
        @Override public String getDescription() { return "walk_backward"; }
        @Override public int getTimeoutTicks() { return 80; }
        @Override public boolean tick(GameState gs, InputController input,
                                      ScreenshotCapture capture, File statusDir) {
            ticks++;
            if (input.isRubyDung()) {
                // RD: move 1 block in -Z (away from primary who went +Z).
                if (ticks == 1) {
                    input.movePlayerPosition(0, 0, -1.0f);
                }
                return ticks >= 5;
            }
            // Face south (0° = +Z), so BACK key walks -Z (away from primary).
            input.setLookDirection(0f, 0f);
            if (ticks == 1) {
                input.pressKey(InputController.BACK);
            }
            // Walk for ~20 ticks (~1 block backward = -Z)
            if (ticks >= 20) {
                input.releaseKey(InputController.BACK);
                return true;
            }
            return false;
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

    private class SecondaryScreenshot implements ScenarioStep {
        private int ticks;
        private long startTimeMs;
        @Override public String getDescription() { return "screenshot"; }
        @Override public int getTimeoutTicks() { return 600; }
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
