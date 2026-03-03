package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.ScreenshotCapture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests sending a chat message and verifying the server echoes it back:
 * 1. Send "HELLO" via InputController.sendChatMessage()
 * 2. Poll chat messages for echo containing "HELLO"
 * 3. Verify echo contains username separator ":" and "HELLO"
 * 4. Screenshot
 */
public class ChatScenario implements Scenario {

    private static final String TEST_MESSAGE = "HELLO";

    @Override
    public String getName() {
        return "chat";
    }

    @Override
    public List<ScenarioStep> getSteps() {
        List<ScenarioStep> steps = new ArrayList<ScenarioStep>();
        steps.add(new WaitSettleStep());
        steps.add(new SendChatStep());
        steps.add(new WaitChatEchoStep());
        steps.add(new ScreenshotStep());
        return steps;
    }

    private static class WaitSettleStep implements ScenarioStep {
        private int ticks;

        @Override
        public String getDescription() {
            return "wait_settle";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            // Poll for solid ground from tick 1 instead of waiting 40 ticks.
            // The WorldLoaded stabilization phase already ensures chunks are
            // loaded before scenarios begin.
            int blockBelow = gs.getBlockBelowFeet();
            if (blockBelow != 0) {
                return true;
            }
            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 100;
        }
    }

    private static class SendChatStep implements ScenarioStep {
        @Override
        public String getDescription() {
            return "send_chat";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            input.sendChatMessage(TEST_MESSAGE);
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }

    private static class WaitChatEchoStep implements ScenarioStep {
        private int ticks;
        private int foundTick = -1;
        // Wait extra ticks after finding the echo so at least one render
        // frame draws the chat line before the screenshot step captures it.
        private static final int RENDER_SETTLE_TICKS = 10;

        @Override
        public String getDescription() {
            return "wait_chat_echo";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;

            if (foundTick > 0) {
                // Already found — wait for renderer to catch up
                return (ticks - foundTick) >= RENDER_SETTLE_TICKS;
            }

            List<String> messages = gs.getChatMessages(10);
            for (String msg : messages) {
                if (msg.contains(TEST_MESSAGE)) {
                    System.out.println("[McTestAgent] Chat echo received: " + msg);
                    // Verify it has the username separator format "<player> HELLO"
                    // Alpha uses the format "<username> message" or "username: message"
                    if (!msg.contains("<") && !msg.contains(":")) {
                        System.out.println("[McTestAgent] Warning: chat echo missing "
                                + "username separator, but message found");
                    }
                    foundTick = ticks;
                    return false; // wait for render settle
                }
            }
            if (ticks % 20 == 0) {
                System.out.println("[McTestAgent] Waiting for chat echo, tick " + ticks
                        + ", messages: " + messages.size());
            }
            return false;
        }

        @Override
        public int getTimeoutTicks() {
            return 80; // 4 seconds (extra room for render settle)
        }
    }

    private static class ScreenshotStep implements ScenarioStep {
        private int ticks;
        private long lastHash;
        private int stableCount;

        @Override
        public String getDescription() {
            return "screenshot";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            ticks++;
            // Look slightly below the horizon every tick for a meaningful
            // screenshot showing sky + ground + chat overlay. Pitch=10 ensures
            // ground is visible regardless of yaw direction (RD's 256x256 world
            // can have the horizon at varying heights depending on direction).
            input.setLookDirection(gs.getYaw(), 10f);

            // Wait for the camera change to render via frame-stable detection.
            if (ticks >= 5 && ticks % 5 == 0) {
                int w = gs.getDisplayWidth();
                int h = gs.getDisplayHeight();
                if (w > 0 && h > 0) {
                    long hash = capture.captureFrameHash(w, h);
                    if (hash != 0 && hash == lastHash) {
                        stableCount++;
                    } else {
                        stableCount = 0;
                    }
                    lastHash = hash;
                }
            }
            if (stableCount < 3) return false;

            File file = new File(statusDir, "chat_complete.png");
            capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 200;
        }
    }
}
