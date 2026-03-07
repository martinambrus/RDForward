package com.github.martinambrus.rdforward.e2e.agent.scenario;

import java.util.List;

/**
 * A test scenario consisting of a sequence of steps to execute
 * within the Minecraft client's tick loop.
 */
public interface Scenario {

    String getName();

    List<ScenarioStep> getSteps();

    /**
     * Camera direction to set during the stabilization phase so that
     * the client's chunk mesher compiles chunks in the capture direction.
     * 1.17+ shader-based renderers only compile chunks in the camera frustum,
     * so stabilization must face the same direction as the final capture.
     *
     * @return {yaw, pitch} in degrees, or null to leave camera unchanged
     */
    default float[] getStabilizationCamera() {
        return new float[]{180f, 10f};
    }
}
