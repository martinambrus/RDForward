package com.github.martinambrus.rdforward.e2e.agent.scenario;

import com.github.martinambrus.rdforward.e2e.agent.GameState;
import com.github.martinambrus.rdforward.e2e.agent.InputController;
import com.github.martinambrus.rdforward.e2e.agent.ScreenshotCapture;

import java.io.File;

/**
 * A single step within a test scenario.
 */
public interface ScenarioStep {

    String getDescription();

    /**
     * Called once per tick while this step is active.
     *
     * @return true when the step is complete, false to continue on next tick
     */
    boolean tick(GameState gs, InputController input, ScreenshotCapture capture, File statusDir);

    /**
     * Maximum ticks before this step times out. 0 = no limit.
     */
    int getTimeoutTicks();
}
