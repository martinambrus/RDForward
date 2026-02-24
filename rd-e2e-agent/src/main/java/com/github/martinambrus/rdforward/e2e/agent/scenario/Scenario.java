package com.github.martinambrus.rdforward.e2e.agent.scenario;

import java.util.List;

/**
 * A test scenario consisting of a sequence of steps to execute
 * within the Minecraft client's tick loop.
 */
public interface Scenario {

    String getName();

    List<ScenarioStep> getSteps();
}
