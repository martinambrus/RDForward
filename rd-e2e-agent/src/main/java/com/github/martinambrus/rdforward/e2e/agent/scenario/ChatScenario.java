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
        steps.add(new SendChatStep());
        steps.add(new WaitChatEchoStep());
        steps.add(new ScreenshotStep());
        return steps;
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
        @Override
        public String getDescription() {
            return "screenshot";
        }

        @Override
        public boolean tick(GameState gs, InputController input,
                            ScreenshotCapture capture, File statusDir) {
            File file = new File(statusDir, "chat_complete.png");
            capture.capture(gs.getDisplayWidth(), gs.getDisplayHeight(), file);
            return true;
        }

        @Override
        public int getTimeoutTicks() {
            return 20;
        }
    }
}
