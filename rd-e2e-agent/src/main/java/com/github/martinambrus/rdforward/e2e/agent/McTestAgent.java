package com.github.martinambrus.rdforward.e2e.agent;

import com.github.martinambrus.rdforward.e2e.agent.mappings.AlphaV6Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.BetaV17Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.FieldMappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.RubyDungMappings;
import com.github.martinambrus.rdforward.e2e.agent.scenario.BlockPlaceBreakScenario;
import com.github.martinambrus.rdforward.e2e.agent.scenario.ChatScenario;
import com.github.martinambrus.rdforward.e2e.agent.scenario.ColumnBuildScenario;
import com.github.martinambrus.rdforward.e2e.agent.scenario.CrossClientScenario;
import com.github.martinambrus.rdforward.e2e.agent.scenario.CreativeBlockPaletteScenario;
import com.github.martinambrus.rdforward.e2e.agent.scenario.EnvironmentCheckScenario;
import com.github.martinambrus.rdforward.e2e.agent.scenario.InventoryManipulationScenario;
import com.github.martinambrus.rdforward.e2e.agent.scenario.QDropScenario;
import com.github.martinambrus.rdforward.e2e.agent.scenario.Scenario;
import com.github.martinambrus.rdforward.e2e.agent.scenario.VoidFallScenario;
import com.github.martinambrus.rdforward.e2e.agent.scenario.WorldLoadedScenario;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

/**
 * Java agent entry point. Injected into the Minecraft client JVM via -javaagent.
 * Installs ByteBuddy Advice on the run() and tick methods to drive test scenarios.
 *
 * Agent args format: key=value,key=value
 * Required keys: version, serverHost, serverPort, statusDir
 * Optional keys: scenario (default: world_loaded)
 */
public class McTestAgent {

    // Shared state accessed by the Advice classes. MUST be public because Advice
    // is inlined into the target class (e.g. net.minecraft.client.Minecraft),
    // which is in a different package. Package-private causes IllegalAccessError.
    public static volatile FieldMappings mappings;
    public static volatile GameState gameState;
    public static volatile InputController inputController;
    public static volatile TickHook tickHook;
    public static volatile String serverHost;
    public static volatile int serverPort;
    public static volatile File statusDir;
    public static volatile String scenarioName;
    public static volatile boolean isCreativeMode;
    public static volatile String username;
    public static volatile String role;       // "primary" or "secondary" (null for single-client tests)
    public static volatile File syncDir;      // shared sync directory (null for single-client tests)
    public static volatile boolean runAdviceApplied;

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[McTestAgent] Agent loading with args: " + agentArgs);

        Map<String, String> args = parseArgs(agentArgs);
        String version = args.get("version");
        serverHost = args.get("serverHost");
        serverPort = Integer.parseInt(args.getOrDefault("serverPort", "25565"));
        statusDir = new File(args.getOrDefault("statusDir", "/tmp/e2e-status"));
        scenarioName = args.getOrDefault("scenario", "world_loaded");
        isCreativeMode = "true".equals(args.getOrDefault("creative", "false"));
        username = args.get("username"); // null if not specified
        role = args.get("role");             // null for single-client tests
        String syncPath = args.get("syncDir");
        syncDir = syncPath != null ? new File(syncPath) : null;

        // Select mappings for the target client version
        mappings = selectMappings(version);

        // Initialize status writer early so errors are visible to orchestrator
        StatusWriter statusWriter = new StatusWriter(statusDir);

        System.out.println("[McTestAgent] Target class: " + mappings.minecraftClassName());
        System.out.println("[McTestAgent] Tick method: " + mappings.tickMethodName());
        System.out.println("[McTestAgent] Server: " + serverHost + ":" + serverPort);
        System.out.println("[McTestAgent] Creative mode: " + isCreativeMode);
        System.out.println("[McTestAgent] Scenario: " + scenarioName);

        // Install ByteBuddy transformers.
        // disableClassFormatChanges() switches from REBASE to REDEFINE type strategy,
        // which prevents ByteBuddy from rebuilding the full class definition. This is
        // needed for Fabric/Mixin-transformed classes (RubyDung) where @MixinMerged
        // annotations on injected fields cause "Cannot add" errors during REBASE.
        // RETRANSFORMATION allows retransforming classes already loaded by Fabric's
        // KnotClassLoader.
        new AgentBuilder.Default()
                .disableClassFormatChanges()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                // Override Display.isActive() to return true — Xvfb never reports
                // the window as active, which causes the pause menu overlay and
                // prevents setIngameFocus() from working.
                .type(ElementMatchers.named("org.lwjgl.opengl.Display"))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                            TypeDescription typeDescription, ClassLoader classLoader,
                            JavaModule module, ProtectionDomain protectionDomain) {
                        System.out.println("[McTestAgent] Patching Display.isActive()");
                        return builder
                                .visit(Advice.to(DisplayActiveAdvice.class)
                                        .on(ElementMatchers.named("isActive")
                                                .and(ElementMatchers.takesNoArguments())));
                    }
                })
                // Suppress RubyDung's pulsing block highlight overlay (only
                // matches on RD clients — class doesn't exist on Alpha/Beta)
                .type(ElementMatchers.named("com.mojang.rubydung.level.LevelRenderer"))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                            TypeDescription typeDescription, ClassLoader classLoader,
                            JavaModule module, ProtectionDomain protectionDomain) {
                        System.out.println("[McTestAgent] Patching LevelRenderer.renderHit()");
                        return builder
                                .visit(Advice.to(RenderHitAdvice.class)
                                        .on(ElementMatchers.named("renderHit")));
                    }
                })
                // Hook Minecraft class run() and tick methods
                .type(ElementMatchers.hasSuperType(
                        ElementMatchers.named(mappings.minecraftClassName())))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                            TypeDescription typeDescription, ClassLoader classLoader,
                            JavaModule module, ProtectionDomain protectionDomain) {
                        System.out.println("[McTestAgent] Transforming: " + typeDescription.getName());
                        return builder
                                // Hook run() to set server host/port before startGame runs
                                .visit(Advice.to(RunAdvice.class)
                                        .on(ElementMatchers.named(mappings.runMethodName())
                                                .and(ElementMatchers.takesNoArguments())))
                                // Hook tick method to drive the state machine
                                .visit(Advice.to(TickAdvice.class)
                                        .on(ElementMatchers.named(mappings.tickMethodName())
                                                .and(ElementMatchers.takesNoArguments())));
                    }
                })
                .with(new AgentBuilder.Listener.Adapter() {
                    @Override
                    public void onError(String typeName, ClassLoader classLoader,
                            JavaModule module, boolean loaded, Throwable throwable) {
                        System.err.println("[McTestAgent] Transform error on " + typeName
                                + ": " + throwable.getMessage());
                        throwable.printStackTrace();
                    }
                })
                .installOn(inst);

        System.out.println("[McTestAgent] ByteBuddy transformers installed");
    }

    private static FieldMappings selectMappings(String version) {
        if (version == null) version = "alpha126";
        switch (version) {
            case "rubydung":
            case "rd132211":
                return new RubyDungMappings();
            case "alpha126":
            case "alpha_1_2_6":
                return new AlphaV6Mappings();
            case "beta18":
            case "beta_1_8_1":
                return new BetaV17Mappings();
            default:
                System.out.println("[McTestAgent] Unknown version '" + version
                        + "', defaulting to Alpha 1.2.6 mappings");
                return new AlphaV6Mappings();
        }
    }

    public static Scenario createScenario(String name) {
        if (name == null) name = "world_loaded";
        switch (name) {
            case "world_loaded":
                return new WorldLoadedScenario();
            case "environment_check":
                return new EnvironmentCheckScenario();
            case "block_place_break":
                return new BlockPlaceBreakScenario();
            case "column_build":
                return new ColumnBuildScenario();
            case "q_drop":
                return new QDropScenario();
            case "chat":
                return new ChatScenario();
            case "inventory_manipulation":
                return new InventoryManipulationScenario();
            case "void_fall":
                return new VoidFallScenario();
            case "creative_block_palette":
                return new CreativeBlockPaletteScenario();
            case "cross_client":
                return new CrossClientScenario();
            default:
                System.out.println("[McTestAgent] Unknown scenario '" + name
                        + "', defaulting to world_loaded");
                return new WorldLoadedScenario();
        }
    }

    private static Map<String, String> parseArgs(String agentArgs) {
        Map<String, String> args = new HashMap<String, String>();
        if (agentArgs == null || agentArgs.isEmpty()) return args;
        for (String pair : agentArgs.split(",")) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                args.put(pair.substring(0, eq).trim(), pair.substring(eq + 1).trim());
            }
        }
        return args;
    }

    /**
     * Advice inlined into Display.isActive() — forces it to return true.
     * Xvfb never reports the window as active, which causes Minecraft to open
     * the pause menu overlay and prevents setIngameFocus() from completing.
     */
    public static class DisplayActiveAdvice {
        @Advice.OnMethodExit
        public static void onExit(@Advice.Return(readOnly = false) boolean returnValue) {
            returnValue = true;
        }
    }

    /**
     * Advice inlined into run() — initializes agent state and (for clients that
     * require it) sets server host/port fields before startGame runs.
     * For modded clients (RubyDung), server connection is handled by CLI args
     * and the Fabric Mixin, so field-setting is skipped.
     */
    public static class RunAdvice {
        @Advice.OnMethodEnter
        public static void onEnter(@Advice.This Object self) {
            if (McTestAgent.runAdviceApplied) return;
            McTestAgent.runAdviceApplied = true;

            McTestAgent.gameState = new GameState(McTestAgent.mappings, self);
            McTestAgent.inputController = new InputController(
                    McTestAgent.gameState, McTestAgent.mappings);
            StatusWriter sw = new StatusWriter(McTestAgent.statusDir);
            Scenario scenario = McTestAgent.createScenario(McTestAgent.scenarioName);
            ScreenshotCapture sc = new ScreenshotCapture();
            sc.setClassLoader(self.getClass().getClassLoader());
            sc.setInputController(McTestAgent.inputController);
            McTestAgent.tickHook = new TickHook(
                    McTestAgent.gameState, sw,
                    sc, McTestAgent.inputController,
                    scenario, McTestAgent.statusDir);

            // Set custom username if specified (before login handshake)
            if (McTestAgent.username != null) {
                McTestAgent.gameState.setUsername(McTestAgent.username);
            }

            // Set server host/port via reflection for clients that need it
            // (null field names = connection handled by other means, e.g. CLI args)
            if (McTestAgent.mappings.serverHostFieldName() != null) {
                System.out.println("[McTestAgent] RunAdvice: setting server fields "
                        + McTestAgent.serverHost + ":" + McTestAgent.serverPort);
                McTestAgent.gameState.setServerHost(McTestAgent.serverHost);
                McTestAgent.gameState.setServerPort(McTestAgent.serverPort);
            } else {
                System.out.println("[McTestAgent] RunAdvice: server connection "
                        + "handled by client (CLI args/Mixin)");
            }
        }
    }

    /**
     * Advice inlined into the tick method — drives the TickHook state machine
     * after each game tick completes.
     */
    public static class TickAdvice {
        @Advice.OnMethodExit
        public static void onExit() {
            TickHook hook = McTestAgent.tickHook;
            if (hook != null) {
                hook.onTick();
            }
        }
    }

    /**
     * Advice inlined into LevelRenderer.renderHit() — suppresses the pulsing
     * block highlight overlay. The highlight uses Math.sin() animation that
     * varies between frames, causing screenshot baseline comparison failures.
     * Since there's no human watching during e2e tests, skip it entirely.
     */
    public static class RenderHitAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter() {
            return true;
        }
    }
}
