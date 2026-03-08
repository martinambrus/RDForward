package com.github.martinambrus.rdforward.e2e.agent;

import com.github.martinambrus.rdforward.e2e.agent.scenario.Scenario;
import com.github.martinambrus.rdforward.e2e.agent.scenario.ScenarioRunner;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * State machine that drives the test scenario from within the client's tick loop.
 * Called on every game tick via ByteBuddy Advice on the tick method.
 *
 * State progression:
 *   INIT -> WAITING_FOR_WORLD -> WAITING_FOR_PLAYER -> STABILIZING
 *        -> RUNNING_SCENARIO -> COMPLETE
 */
public class TickHook {

    public enum State {
        INIT,
        WAITING_FOR_WORLD,
        WAITING_FOR_PLAYER,
        STABILIZING,
        RUNNING_SCENARIO,
        COMPLETE,
        ERROR
    }

    // Pre-LWJGL3 (Alpha through 1.12): fixed 100 ticks = 5 seconds.
    // Alpha/Beta clients have animated textures (water) and day/night cycle
    // that cause the framebuffer to change every frame, so frame-stable
    // detection never converges — use a fixed tick count instead.
    private static final int STABILIZE_TICKS_DEFAULT = 100;
    // LWJGL3 (1.13+): frame-stable detection instead of fixed timeout.
    // Sample framebuffer hash every FRAME_SAMPLE_INTERVAL ticks; when
    // FRAME_STABLE_COUNT consecutive samples match, all chunk meshes are
    // complete. MIN protects against false positives from loading screens;
    // MAX is a safety net for pathological cases.
    private static final int FRAME_SAMPLE_INTERVAL = 20;  // 1 second between samples
    private static final int FRAME_STABLE_COUNT = 3;       // 3 consecutive matches = stable
    private static final int STABILIZE_MIN_LWJGL3 = 100;   // 5 seconds minimum
    private static final int STABILIZE_MAX_LWJGL3 = 1200;  // 60 seconds safety net
    // 1.17+ BFS walk: press FORWARD during ticks 20-25 of stabilization to move
    // the player ~1 block. This changes Math.floor(pos) in the LevelRenderer,
    // triggering needsFullRenderChunkUpdate which re-runs the BFS visibility
    // traversal AFTER all chunks have been loaded from the network.
    // Without this, the initial BFS runs before chunks arrive and marks most
    // sections as invisible; they never get re-evaluated without movement.
    private static final int BFS_WALK_START = 20;
    private static final int BFS_WALK_END = 25;

    private final GameState gameState;
    private final StatusWriter statusWriter;
    private final ScreenshotCapture screenshotCapture;
    private final InputController inputController;
    private final Scenario scenario;
    private final File statusDir;

    private State state = State.INIT;
    private int tickCount;
    private int stabilizeTicks;
    private final boolean isLwjgl3;
    private final List<String> screenshots = new ArrayList<String>();
    private String error;

    // Frame-stable detection for LWJGL3 clients
    private long lastFrameHash;
    private int stableFrameCount;

    private ScenarioRunner scenarioRunner;
    private PrintWriter debugLog;

    public TickHook(GameState gameState, StatusWriter statusWriter,
                    ScreenshotCapture screenshotCapture, InputController inputController,
                    Scenario scenario, File statusDir) {
        this.gameState = gameState;
        this.statusWriter = statusWriter;
        this.screenshotCapture = screenshotCapture;
        this.inputController = inputController;
        this.scenario = scenario;
        this.statusDir = statusDir;
        this.isLwjgl3 = McTestAgent.mappings != null && McTestAgent.mappings.isLwjgl3();
    }

    /**
     * Called on every game tick (from Advice on the tick method).
     * Drives the state machine forward.
     */
    public void onTick() {
        tickCount++;

        try {
            if (tickCount <= 5 || tickCount % 200 == 0) {
                System.out.println("[McTestAgent] Tick " + tickCount + " state=" + state
                        + " world=" + (gameState.getWorld() != null)
                        + " player=" + (gameState.getPlayer() != null));
            }

            switch (state) {
                case INIT:
                    state = State.WAITING_FOR_WORLD;
                    writeStatus();
                    break;

                case WAITING_FOR_WORLD:
                    if (gameState.getWorld() != null) {
                        System.out.println("[McTestAgent] World loaded at tick " + tickCount);
                        // Signal to RenderLoopSafetyAdvice that 3D world rendering
                        // is about to start. On 1.17+ Core Profile clients the next
                        // render call would crash the JVM under Mesa llvmpipe, so
                        // the advice switches to permanent skip once it sees this.
                        System.setProperty("mctestagent.world.loaded", "true");
                        McTestAgent.worldLoaded = true;
                        state = State.WAITING_FOR_PLAYER;
                        writeStatus();
                        // For Core Profile clients: TickAdvice checks this flag
                        // after onTick() returns and throws to abort f() before
                        // the render portion executes (would crash on Mesa).
                    }
                    break;

                case WAITING_FOR_PLAYER:
                    if (gameState.getPlayer() != null) {
                        System.out.println("[McTestAgent] Player spawned at tick " + tickCount);
                        double[] pos = gameState.getPlayerPosition();
                        if (pos != null) {
                            System.out.println("[McTestAgent] Position: "
                                    + pos[0] + ", " + pos[1] + ", " + pos[2]);
                        }
                        state = State.STABILIZING;
                        stabilizeTicks = 0;
                        // Disable SmartCull for 1.17+ clients. SmartCull blocks
                        // the LevelRenderer BFS from propagating through uncompiled
                        // chunk sections (which return all-false VisGraph data).
                        // Under Mesa llvmpipe, async compilation is too slow, causing
                        // a diagonal rendering cutoff.
                        if (McTestAgent.mappings != null
                                && McTestAgent.mappings.smartCullFieldName() != null) {
                            try {
                                Object mc = gameState.getMinecraftInstance();
                                java.lang.reflect.Field smartCullField = mc.getClass()
                                        .getField(McTestAgent.mappings.smartCullFieldName());
                                smartCullField.setBoolean(mc, false);
                                System.out.println("[McTestAgent] SmartCull disabled");
                            } catch (Exception e) {
                                System.out.println("[McTestAgent] Failed to disable SmartCull: " + e);
                            }
                        }
                        writeStatus();
                    }
                    break;

                case STABILIZING:
                    // Dismiss pause menu overlay from Xvfb focus loss so the
                    // framebuffer is clean before the first scenario screenshot
                    inputController.dismissPauseScreen();
                    // Suppress mouse deltas so setLookDirection() below isn't
                    // overwritten by Xvfb spurious mouse movement each frame.
                    inputController.applyInputs();
                    stabilizeTicks++;

                    // LWJGL3 1.17+: set the camera to the scenario's capture
                    // direction during stabilization. The shader-based renderer
                    // only compiles chunk meshes within the camera frustum, so
                    // the stabilization frustum must match the capture direction.
                    if (isLwjgl3) {
                        float[] cam = scenario.getStabilizationCamera();
                        if (cam != null) {
                            inputController.setLookDirection(cam[0], cam[1]);
                        }
                        // Walk forward briefly to trigger 1.17+ LevelRenderer BFS
                        // re-evaluation after chunks have loaded from the network.
                        // Only for 1.17+ (smartCullFieldName != null) — on 1.13-1.16
                        // there is no BFS gate, and leaving FORWARD pressed during
                        // the full stabilization timeout walks the player off the world.
                        boolean is117Plus = McTestAgent.mappings != null
                                && McTestAgent.mappings.smartCullFieldName() != null;
                        if (is117Plus && stabilizeTicks >= BFS_WALK_START
                                && stabilizeTicks < BFS_WALK_END) {
                            inputController.pressKey(InputController.FORWARD);
                        }
                    }

                    // Re-enforce SmartCull=false every tick (in case anything resets it)
                    if (McTestAgent.mappings != null
                            && McTestAgent.mappings.smartCullFieldName() != null) {
                        try {
                            Object mc = gameState.getMinecraftInstance();
                            java.lang.reflect.Field smartCullField = mc.getClass()
                                    .getField(McTestAgent.mappings.smartCullFieldName());
                            boolean val = smartCullField.getBoolean(mc);
                            if (val) {
                                smartCullField.setBoolean(mc, false);
                                System.out.println("[McTestAgent] SmartCull was re-enabled at tick "
                                        + stabilizeTicks + ", forcing OFF again");
                            }
                        } catch (Exception ignored) {}
                    }

                    // Debug: log position every 10 ticks during stabilization
                    if (stabilizeTicks == 1 || stabilizeTicks % 10 == 0) {
                        if (debugLog == null) {
                            try {
                                debugLog = new PrintWriter(new FileWriter(
                                        new File(statusDir, "debug_position.log")), true);
                            } catch (Exception ignored) {}
                        }
                        if (debugLog != null) {
                            double[] dpos = gameState.getPlayerPosition();
                            double rawY = gameState.getRawPosY();
                            if (dpos != null) {
                                debugLog.printf("STAB tick=%d/%d normalizedY=%.2f rawY=%.2f X=%.2f Z=%.2f stable=%d look=%s%n",
                                        stabilizeTicks, isLwjgl3 ? STABILIZE_MAX_LWJGL3 : STABILIZE_TICKS_DEFAULT,
                                        dpos[1], rawY, dpos[0], dpos[2], stableFrameCount,
                                        inputController.getLastLookResult());
                            }
                        }
                    }

                    // LWJGL3: frame-stable detection — sample framebuffer hash
                    // and wait for consecutive identical frames (all chunks meshed).
                    if (isLwjgl3 && stabilizeTicks >= STABILIZE_MIN_LWJGL3
                            && stabilizeTicks % FRAME_SAMPLE_INTERVAL == 0) {
                        int w = gameState.getDisplayWidth();
                        int h = gameState.getDisplayHeight();
                        if (w > 0 && h > 0) {
                            long hash = screenshotCapture.captureFrameHash(w, h);
                            if (hash != 0 && hash == lastFrameHash) {
                                stableFrameCount++;
                            } else {
                                stableFrameCount = 0;
                            }
                            lastFrameHash = hash;
                            if (debugLog != null) {
                                debugLog.printf("FRAME_HASH tick=%d hash=%016x stable=%d/%d%n",
                                        stabilizeTicks, hash, stableFrameCount, FRAME_STABLE_COUNT);
                            }
                        }
                    }

                    boolean stabilized;
                    if (isLwjgl3) {
                        // Frame-stable: proceed when N consecutive frames match,
                        // or hit the safety net timeout
                        stabilized = stableFrameCount >= FRAME_STABLE_COUNT
                                || stabilizeTicks >= STABILIZE_MAX_LWJGL3;
                    } else {
                        stabilized = stabilizeTicks >= STABILIZE_TICKS_DEFAULT;
                    }

                    if (stabilized) {
                        String reason = isLwjgl3
                                ? (stableFrameCount >= FRAME_STABLE_COUNT
                                    ? "frame-stable (" + stableFrameCount + " consecutive matches)"
                                    : "timeout (safety net)")
                                : "tick count";
                        System.out.println("[McTestAgent] Stabilized after "
                                + stabilizeTicks + " ticks (" + reason
                                + "), starting scenario '" + scenario.getName() + "'");
                        // Release any keys left pressed by the BFS walk
                        inputController.releaseAllKeys();
                        scenarioRunner = new ScenarioRunner(scenario, gameState,
                                inputController, screenshotCapture, statusDir);
                        state = State.RUNNING_SCENARIO;
                        writeStatus();
                    } else if (stabilizeTicks <= 5 || stabilizeTicks % 200 == 0) {
                        System.out.println("[McTestAgent] Stabilizing: tick "
                                + stabilizeTicks + (isLwjgl3
                                    ? " (frame-stable=" + stableFrameCount + "/" + FRAME_STABLE_COUNT + ")"
                                    : "/" + STABILIZE_TICKS_DEFAULT));
                    }
                    break;

                case RUNNING_SCENARIO:
                    // Dismiss pause menu overlay from Xvfb focus loss
                    inputController.dismissPauseScreen();
                    // Apply input state before scenario step
                    inputController.applyInputs();

                    // Debug position logging
                    if (debugLog == null) {
                        try {
                            debugLog = new PrintWriter(new FileWriter(
                                    new File(statusDir, "debug_position.log")), true);
                        } catch (Exception ignored) {}
                    }
                    if (debugLog != null) {
                        double[] dpos = gameState.getPlayerPosition();
                        if (dpos != null) {
                            debugLog.printf("tick=%d step=%s Y=%.2f X=%.2f Z=%.2f%n",
                                    tickCount,
                                    scenarioRunner.getCurrentStepDescription(),
                                    dpos[1], dpos[0], dpos[2]);
                        }
                    }

                    ScenarioRunner.RunnerState rs = scenarioRunner.tick();
                    if (rs == ScenarioRunner.RunnerState.COMPLETE) {
                        screenshots.addAll(scenarioRunner.getScreenshots());
                        state = State.COMPLETE;
                        System.out.println("[McTestAgent] Scenario complete at tick " + tickCount);
                    } else if (rs == ScenarioRunner.RunnerState.ERROR) {
                        screenshots.addAll(scenarioRunner.getScreenshots());
                        error = scenarioRunner.getError();
                        state = State.ERROR;
                    }
                    writeStatus();
                    break;

                case COMPLETE:
                case ERROR:
                    // Terminal states — no further action
                    break;
            }
        } catch (Throwable e) {
            error = e.getClass().getName() + ": " + e.getMessage();
            state = State.ERROR;
            System.err.println("[McTestAgent] Error in tick hook: " + error);
            e.printStackTrace();
            writeStatus();
        }
    }

    public State getState() {
        return state;
    }

    private void writeStatus() {
        if (scenarioRunner != null) {
            statusWriter.write(state.name(), tickCount, screenshots,
                    gameState.getPlayerPosition(), error,
                    scenarioRunner.getCurrentStepDescription(),
                    scenarioRunner.getCurrentStepIndex(),
                    scenarioRunner.getTotalSteps(),
                    scenarioRunner.getResults());
        } else {
            statusWriter.write(state.name(), tickCount, screenshots,
                    gameState.getPlayerPosition(), error);
        }
    }
}
