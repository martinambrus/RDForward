package com.github.martinambrus.rdforward.e2e.agent;

import com.github.martinambrus.rdforward.e2e.agent.mappings.AlphaV6Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.FieldMappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.RubyDungMappings;
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
 */
public class McTestAgent {

    // Shared state accessed by the Advice classes. MUST be public because Advice
    // is inlined into the target class (e.g. net.minecraft.client.Minecraft),
    // which is in a different package. Package-private causes IllegalAccessError.
    public static volatile FieldMappings mappings;
    public static volatile GameState gameState;
    public static volatile TickHook tickHook;
    public static volatile String serverHost;
    public static volatile int serverPort;
    public static volatile File statusDir;
    public static volatile boolean runAdviceApplied;

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[McTestAgent] Agent loading with args: " + agentArgs);

        Map<String, String> args = parseArgs(agentArgs);
        String version = args.get("version");
        serverHost = args.get("serverHost");
        serverPort = Integer.parseInt(args.getOrDefault("serverPort", "25565"));
        statusDir = new File(args.getOrDefault("statusDir", "/tmp/e2e-status"));

        // Select mappings for the target client version
        mappings = selectMappings(version);

        // Initialize status writer early so errors are visible to orchestrator
        StatusWriter statusWriter = new StatusWriter(statusDir);

        System.out.println("[McTestAgent] Target class: " + mappings.minecraftClassName());
        System.out.println("[McTestAgent] Tick method: " + mappings.tickMethodName());
        System.out.println("[McTestAgent] Server: " + serverHost + ":" + serverPort);

        // Install ByteBuddy transformers
        new AgentBuilder.Default()
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
            default:
                System.out.println("[McTestAgent] Unknown version '" + version
                        + "', defaulting to Alpha 1.2.6 mappings");
                return new AlphaV6Mappings();
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
            StatusWriter sw = new StatusWriter(McTestAgent.statusDir);
            McTestAgent.tickHook = new TickHook(
                    McTestAgent.gameState, sw,
                    new ScreenshotCapture(), McTestAgent.statusDir);

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
}
