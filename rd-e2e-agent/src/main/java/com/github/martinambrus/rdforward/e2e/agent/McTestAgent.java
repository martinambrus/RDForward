package com.github.martinambrus.rdforward.e2e.agent;

import com.github.martinambrus.rdforward.e2e.agent.mappings.AlphaV1Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.AlphaV13Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.AlphaV14Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.AlphaV2Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.AlphaV3Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.AlphaV4Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.AlphaV5Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.AlphaV6Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.BetaV7Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.BetaV8Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.BetaV9Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.BetaV10Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.BetaV11Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.BetaV12Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.BetaV13Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.BetaV14Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.BetaV17Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.BetaV21Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.DelegatingFieldMappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.FieldMappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV107Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV108Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV109Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV110Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV210Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV315Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV316Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV335Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV338Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV340Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV393Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV4Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV4_173Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV4_175Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV401Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV404Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV47Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV47_181Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV47_182Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV47_184Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV47_188Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV477Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV480Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV485Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV490Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV498Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV5Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV5_178Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV5_1710Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV573Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV575Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV578Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV735Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV736Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV751Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV753Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV754Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV755Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV756Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV757Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.NettyReleaseV758Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.ReleaseV22Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.ReleaseV23Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.ReleaseV28Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.ReleaseV29Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.ReleaseV39Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.ReleaseV39_2Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.ReleaseV47PreNettyMappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.ReleaseV49Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.ReleaseV51Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.ReleaseV60Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.ReleaseV61Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.ReleaseV73Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.ReleaseV74Mappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.ReleaseV78Mappings;
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

import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;

import net.bytebuddy.jar.asm.Label;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

        // Auto-detect Minecraft class name for Netty clients where sub-versions
        // have different ProGuard obfuscation than the base protocol version.
        // Scans Main.class bytecode to find the actual class/method names.
        String[] detected = detectMinecraftClassFromBytecode();
        if (detected != null) {
            if (!detected[0].equals(mappings.minecraftClassName())
                    || !detected[1].equals(mappings.runMethodName())
                    || !detected[2].equals(mappings.tickMethodName())) {
                System.out.println("[McTestAgent] Auto-detect override: class=" + detected[0]
                        + " run=" + detected[1] + " tick=" + detected[2]
                        + " (was: class=" + mappings.minecraftClassName()
                        + " run=" + mappings.runMethodName()
                        + " tick=" + mappings.tickMethodName() + ")");
                mappings = new DelegatingFieldMappings(mappings, detected[0], detected[1], detected[2]);
            }
        }

        // MC 1.17+ uses OpenGL Core Profile where fixed-function calls like
        // glPushMatrix/glPopMatrix are unavailable and cause a JVM FATAL abort.
        // Set a system property so RenderLoopSafetyAdvice can skip GL matrix cleanup.
        if (version.startsWith("release117") || version.startsWith("release118")) {
            System.setProperty("mctestagent.coreprofile", "true");
        }
        // Create status directory early so the orchestrator can find it
        statusDir.mkdirs();

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
        AgentBuilder builder = new AgentBuilder.Default()
                .disableClassFormatChanges()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);

        if (mappings.isLwjgl3()) {
            // LWJGL3 clients: patch GLFW.glfwGetWindowAttrib() to return 1
            // for GLFW_FOCUSED. Xvfb never reports focus, causing pause overlay.
            builder = builder
                    .type(ElementMatchers.named("org.lwjgl.glfw.GLFW"))
                    .transform(new AgentBuilder.Transformer() {
                        @Override
                        public DynamicType.Builder<?> transform(DynamicType.Builder<?> b,
                                TypeDescription typeDescription, ClassLoader classLoader,
                                JavaModule module, ProtectionDomain protectionDomain) {
                            System.out.println("[McTestAgent] Patching GLFW.glfwGetWindowAttrib()");
                            DynamicType.Builder<?> result = b
                                    .visit(Advice.to(GlfwFocusAdvice.class)
                                            .on(ElementMatchers.named("glfwGetWindowAttrib")))
                                    .visit(Advice.to(GlfwWindowCloseAdvice.class)
                                            .on(ElementMatchers.named("glfwWindowShouldClose")));
                            // MC 1.17+ requests Core Profile where legacy GL calls cause
                            // FATAL JVM aborts. Force Compatibility Profile instead.
                            if (System.getProperty("mctestagent.coreprofile") != null) {
                                result = result
                                        .visit(Advice.to(GlfwWindowHintAdvice.class)
                                                .on(ElementMatchers.named("glfwWindowHint")));
                            }
                            return result;
                        }
                    });
        } else {
            // LWJGL2 clients: Override Display.isActive() to return true
            builder = builder
                    .type(ElementMatchers.named("org.lwjgl.opengl.Display"))
                    .transform(new AgentBuilder.Transformer() {
                        @Override
                        public DynamicType.Builder<?> transform(DynamicType.Builder<?> b,
                                TypeDescription typeDescription, ClassLoader classLoader,
                                JavaModule module, ProtectionDomain protectionDomain) {
                            System.out.println("[McTestAgent] Patching Display.isActive()");
                            return b
                                    .visit(Advice.to(DisplayActiveAdvice.class)
                                            .on(ElementMatchers.named("isActive")
                                                    .and(ElementMatchers.takesNoArguments())));
                        }
                    });
        }

        // 1.14+ clients: suppress tessellation NPEs from BlockRenderDispatcher
        // and render-pipeline NPEs from the Minecraft class's render method.
        // The async resource reload may not finish model/texture baking before the
        // first render frame. Without models, block rendering NPEs (caught by
        // BlockRenderSafetyAdvice). In 1.16+, texture atlas NPEs propagate up to
        // Minecraft's render method (caught by RenderLoopSafetyAdvice, which also
        // resets the GL matrix stack and sets a system property so TickHook can
        // wait for rendering to stabilize).
        // Uses a SEPARATE AgentBuilder without disableClassFormatChanges() because
        // REDEFINE strategy cannot add try-catch wrappers (onThrowable needs REBASE).
        if (mappings.blockRenderDispatcherClassName() != null) {
            final String brdClass = mappings.blockRenderDispatcherClassName();
            AgentBuilder rebaseBuilder = new AgentBuilder.Default()
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .type(ElementMatchers.named(brdClass))
                    .transform(new AgentBuilder.Transformer() {
                        @Override
                        public DynamicType.Builder<?> transform(DynamicType.Builder<?> b,
                                TypeDescription typeDescription, ClassLoader classLoader,
                                JavaModule module, ProtectionDomain protectionDomain) {
                            System.out.println("[McTestAgent] Patching BlockRenderDispatcher ("
                                    + brdClass + ") for tessellation safety");
                            return b
                                    .visit(Advice.to(BlockRenderSafetyAdvice.class)
                                            .on(ElementMatchers.not(ElementMatchers.isConstructor())));
                        }
                    });

            // Also patch the Minecraft class render method to catch NPEs that
            // propagate from deeper in the render pipeline (e.g. TextureAtlas
            // in 1.16+). Only the render method is patched — patching all methods
            // causes GL_STACK_OVERFLOW because intermediate methods push the GL
            // matrix stack but exit early (without popping) on caught exceptions.
            if (mappings.renderMethodName() != null) {
                final String renderMethod = mappings.renderMethodName();
                final String mcClass = mappings.minecraftClassName();
                rebaseBuilder = rebaseBuilder
                        .type(ElementMatchers.hasSuperType(
                                ElementMatchers.named(mcClass)))
                        .transform(new AgentBuilder.Transformer() {
                            @Override
                            public DynamicType.Builder<?> transform(DynamicType.Builder<?> b,
                                    TypeDescription typeDescription, ClassLoader classLoader,
                                    JavaModule module, ProtectionDomain protectionDomain) {
                                System.out.println("[McTestAgent] Patching render method "
                                        + renderMethod + "(boolean) on " + mcClass
                                        + " for resource-reload safety");
                                return b
                                        .visit(Advice.to(RenderLoopSafetyAdvice.class)
                                                .on(ElementMatchers.named(renderMethod)
                                                        .and(ElementMatchers.takesArguments(boolean.class))));
                            }
                        });
            }

            // MC 1.17+ (Core Profile): skip GameRenderer's render method entirely.
            // Mesa llvmpipe cannot execute MC 1.17+'s shader-based 3D render pipeline —
            // the JVM dies silently on the first frame that renders world geometry.
            // Network processing and ticks happen in f(boolean) BEFORE the GameRenderer
            // call, so they are not affected. The world loads normally via network
            // packets and TickHook progresses the state machine through normal ticks.
            if (mappings.gameRendererClassName() != null) {
                final String grClass = mappings.gameRendererClassName();
                rebaseBuilder = rebaseBuilder
                        .type(ElementMatchers.named(grClass))
                        .transform(new AgentBuilder.Transformer() {
                            @Override
                            public DynamicType.Builder<?> transform(DynamicType.Builder<?> b,
                                    TypeDescription typeDescription, ClassLoader classLoader,
                                    JavaModule module, ProtectionDomain protectionDomain) {
                                System.out.println("[McTestAgent] Patching GameRenderer ("
                                        + grClass + ") to skip 3D rendering (Core Profile / Mesa)");
                                return b
                                        .visit(Advice.to(GameRendererSkipAdvice.class)
                                                .on(ElementMatchers.takesArguments(
                                                        float.class, long.class, boolean.class)));
                            }
                        });
            }

            rebaseBuilder
                    .with(new AgentBuilder.Listener.Adapter() {
                        @Override
                        public void onError(String typeName, ClassLoader classLoader,
                                JavaModule module, boolean loaded, Throwable throwable) {
                            System.err.println("[McTestAgent] REBASE transform error on " + typeName
                                    + ": " + throwable.getMessage());
                            throwable.printStackTrace();
                        }
                    })
                    .installOn(inst);
        }

        // MC 1.16.4+ checks SocialInteractionsService.serversAllowed() before
        // using CLI --server/--port args. YggdrasilSocialInteractionsService calls
        // the Mojang /privileges API with the access token; with our fake token "0"
        // it returns serversAllowed=false, blocking the auto-connect. Force true.
        builder = builder
                .type(ElementMatchers.named(
                        "com.mojang.authlib.yggdrasil.YggdrasilSocialInteractionsService"))
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> b,
                            TypeDescription typeDescription, ClassLoader classLoader,
                            JavaModule module, ProtectionDomain protectionDomain) {
                        System.out.println("[McTestAgent] Patching YggdrasilSocialInteractionsService"
                                + " to allow servers/chat/realms");
                        return b
                                .visit(Advice.to(SocialInteractionsAdvice.class)
                                        .on(ElementMatchers.named("serversAllowed")
                                                .or(ElementMatchers.named("chatAllowed"))
                                                .or(ElementMatchers.named("realmsAllowed"))));
                    }
                });

        builder
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
            case "alpha1015":
            case "alpha_1_0_15":
                return new AlphaV13Mappings();
            case "alpha1016":
            case "alpha_1_0_16":
                return new AlphaV14Mappings();
            case "alpha1017":
            case "alpha_1_0_17":
                return new AlphaV1Mappings();
            case "alpha110":
            case "alpha_1_1_0":
                return new AlphaV2Mappings();
            case "alpha120":
            case "alpha_1_2_0":
                return new AlphaV3Mappings();
            case "alpha122":
            case "alpha_1_2_2":
                return new AlphaV4Mappings();
            case "alpha123":
            case "alpha_1_2_3":
                return new AlphaV5Mappings();
            case "alpha126":
            case "alpha_1_2_6":
                return new AlphaV6Mappings();
            case "beta10":
            case "beta_1_0":
                return new BetaV7Mappings();
            case "beta12":
            case "beta_1_2":
                return new BetaV8Mappings();
            case "beta13":
            case "beta_1_3":
                return new BetaV9Mappings();
            case "beta14":
            case "beta_1_4":
                return new BetaV10Mappings();
            case "beta15":
            case "beta_1_5":
                return new BetaV11Mappings();
            case "beta16":
            case "beta_1_6":
                return new BetaV12Mappings();
            case "beta17":
            case "beta_1_7":
                return new BetaV13Mappings();
            case "beta173":
            case "beta_1_7_3":
                return new BetaV14Mappings();
            case "beta18":
            case "beta_1_8_1":
                return new BetaV17Mappings();
            case "beta19pre5":
            case "beta_1_9_pre5":
                return new BetaV21Mappings();

            // --- Pre-Netty Release versions (1.0-1.6.4) ---
            case "release10":
            case "release_1_0":
                return new ReleaseV22Mappings();
            case "release11":
            case "release_1_1":
                return new ReleaseV23Mappings();
            case "release121":
            case "release122":
            case "release123":
            case "release_1_2_1":
            case "release_1_2_2":
            case "release_1_2_3":
                return new ReleaseV28Mappings();
            case "release124":
            case "release125":
            case "release_1_2_4":
            case "release_1_2_5":
                return new ReleaseV29Mappings();
            case "release131":
            case "release_1_3_1":
                return new ReleaseV39Mappings();
            case "release132":
            case "release_1_3_2":
                return new ReleaseV39_2Mappings();
            case "release142":
            case "release_1_4_2":
                return new ReleaseV47PreNettyMappings();
            case "release144":
            case "release145":
            case "release_1_4_4":
            case "release_1_4_5":
                return new ReleaseV49Mappings();
            case "release146":
            case "release147":
            case "release_1_4_6":
            case "release_1_4_7":
                return new ReleaseV51Mappings();
            case "release151":
            case "release_1_5_1":
                return new ReleaseV60Mappings();
            case "release152":
            case "release_1_5_2":
                return new ReleaseV61Mappings();
            case "release161":
            case "release_1_6_1":
                return new ReleaseV73Mappings();
            case "release162":
            case "release_1_6_2":
                return new ReleaseV74Mappings();
            case "release164":
            case "release_1_6_4":
                return new ReleaseV78Mappings();

            // --- Netty LWJGL2 Release versions (1.7.2-1.12.2) ---
            case "release172":
            case "release_1_7_2":
                return new NettyReleaseV4Mappings();
            case "release173":
            case "release174":
            case "release_1_7_3":
            case "release_1_7_4":
                return new NettyReleaseV4_173Mappings();
            case "release175":
            case "release_1_7_5":
                return new NettyReleaseV4_175Mappings();
            case "release176":
            case "release177":
            case "release_1_7_6":
            case "release_1_7_7":
                return new NettyReleaseV5Mappings();
            case "release178":
            case "release179":
            case "release_1_7_8":
            case "release_1_7_9":
                return new NettyReleaseV5_178Mappings();
            case "release1710":
            case "release_1_7_10":
                return new NettyReleaseV5_1710Mappings();
            case "release18":
            case "release_1_8":
                return new NettyReleaseV47Mappings();
            case "release181":
            case "release_1_8_1":
                return new NettyReleaseV47_181Mappings();
            case "release182":
            case "release183":
            case "release_1_8_2":
            case "release_1_8_3":
                return new NettyReleaseV47_182Mappings();
            case "release184":
            case "release185":
            case "release186":
            case "release187":
            case "release_1_8_4":
            case "release_1_8_5":
            case "release_1_8_6":
            case "release_1_8_7":
                return new NettyReleaseV47_184Mappings();
            case "release188":
            case "release189":
            case "release_1_8_8":
            case "release_1_8_9":
                return new NettyReleaseV47_188Mappings();
            case "release19":
            case "release_1_9":
                return new NettyReleaseV107Mappings();
            case "release191":
            case "release_1_9_1":
                return new NettyReleaseV108Mappings();
            case "release192":
            case "release193":
            case "release_1_9_2":
            case "release_1_9_3":
                return new NettyReleaseV109Mappings();
            case "release194":
            case "release_1_9_4":
                return new NettyReleaseV110Mappings();
            case "release110":
            case "release1101":
            case "release1102":
            case "release_1_10":
            case "release_1_10_1":
            case "release_1_10_2":
                return new NettyReleaseV210Mappings();
            case "release111":
            case "release_1_11":
                return new NettyReleaseV315Mappings();
            case "release1111":
            case "release_1_11_1":
            case "release1112":
            case "release_1_11_2":
                return new NettyReleaseV316Mappings();
            case "release112":
            case "release_1_12":
                return new NettyReleaseV335Mappings();
            case "release1121":
            case "release_1_12_1":
                return new NettyReleaseV338Mappings();
            case "release1122":
            case "release_1_12_2":
                return new NettyReleaseV340Mappings();

            // --- Netty LWJGL3 Release versions (1.13-1.18.2) ---
            case "release113":
            case "release_1_13":
                return new NettyReleaseV393Mappings();
            case "release1131":
            case "release_1_13_1":
                return new NettyReleaseV401Mappings();
            case "release1132":
            case "release_1_13_2":
                return new NettyReleaseV404Mappings();
            case "release114":
            case "release_1_14":
                return new NettyReleaseV477Mappings();
            case "release1141":
            case "release_1_14_1":
                return new NettyReleaseV480Mappings();
            case "release1142":
            case "release_1_14_2":
                return new NettyReleaseV485Mappings();
            case "release1143":
            case "release_1_14_3":
                return new NettyReleaseV490Mappings();
            case "release1144":
            case "release_1_14_4":
                return new NettyReleaseV498Mappings();
            case "release115":
            case "release_1_15":
                return new NettyReleaseV573Mappings();
            case "release1151":
            case "release_1_15_1":
                return new NettyReleaseV575Mappings();
            case "release1152":
            case "release_1_15_2":
                return new NettyReleaseV578Mappings();
            case "release116":
            case "release_1_16":
                return new NettyReleaseV735Mappings();
            case "release1161":
            case "release_1_16_1":
                return new NettyReleaseV736Mappings();
            case "release1162":
            case "release_1_16_2":
                return new NettyReleaseV751Mappings();
            case "release1163":
            case "release_1_16_3":
                return new NettyReleaseV753Mappings();
            case "release1164":
            case "release1165":
            case "release_1_16_4":
            case "release_1_16_5":
                return new NettyReleaseV754Mappings();
            case "release117":
            case "release_1_17":
                return new NettyReleaseV755Mappings();
            case "release1171":
            case "release_1_17_1":
                return new NettyReleaseV756Mappings();
            case "release118":
            case "release1181":
            case "release_1_18":
            case "release_1_18_1":
                return new NettyReleaseV757Mappings();
            case "release1182":
            case "release_1_18_2":
                return new NettyReleaseV758Mappings();

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
     * Auto-detect Minecraft class name, run method, and tick method by scanning
     * bytecode with ASM (shaded in ByteBuddy). Uses a three-level scan:
     * <ol>
     *   <li>Main.class main() → find Minecraft class + run method</li>
     *   <li>Run method → find per-frame method (()V self-call + backward goto)</li>
     *   <li>Per-frame method → find tick method (()V self-call + iinc)</li>
     * </ol>
     *
     * @return String[3] = {className, runMethodName, tickMethodName}, or null if
     *         detection fails or Main.class doesn't exist (pre-Netty clients).
     */
    private static String[] detectMinecraftClassFromBytecode() {
        InputStream mainStream = ClassLoader.getSystemResourceAsStream(
                "net/minecraft/client/main/Main.class");
        if (mainStream == null) {
            return null;
        }

        try {
            byte[] mainBytes = readStreamFully(mainStream);
            mainStream.close();

            // Phase 1: Find Minecraft class name and run method from Main.main().
            // Collect all NEW'd types, then find the first INVOKEVIRTUAL ()V call
            // whose owner was NEW'd — that's the Minecraft class and its run method.
            final String[] result = new String[3];

            ClassReader cr = new ClassReader(mainBytes);
            cr.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                        String signature, String[] exceptions) {
                    if ("main".equals(name) && "([Ljava/lang/String;)V".equals(descriptor)) {
                        return new MethodVisitor(Opcodes.ASM9) {
                            final Set<String> newTypes = new HashSet<String>();

                            @Override
                            public void visitTypeInsn(int opcode, String type) {
                                if (opcode == Opcodes.NEW) {
                                    newTypes.add(type);
                                }
                            }

                            @Override
                            public void visitMethodInsn(int opcode, String owner, String name,
                                    String descriptor, boolean isInterface) {
                                if (result[0] != null) return; // already found
                                // Match only obfuscated classes (default package, no '/')
                                if (opcode == Opcodes.INVOKEVIRTUAL
                                        && newTypes.contains(owner)
                                        && "()V".equals(descriptor)
                                        && owner.indexOf('/') < 0) {
                                    result[0] = owner;
                                    result[1] = name;
                                }
                            }
                        };
                    }
                    return null;
                }
            }, 0);

            if (result[0] == null || result[1] == null) {
                System.out.println("[McTestAgent] Auto-detect: could not find "
                        + "Minecraft class in Main.main()");
                return null;
            }

            // Load Minecraft class bytecode for phases 2 and 3
            final String mcClassInternal = result[0].replace('.', '/');
            InputStream mcStream = ClassLoader.getSystemResourceAsStream(
                    mcClassInternal + ".class");
            if (mcStream == null) {
                System.out.println("[McTestAgent] Auto-detect: could not load "
                        + result[0]);
                return null;
            }

            byte[] mcBytes = readStreamFully(mcStream);
            mcStream.close();

            // Phase 2: Find per-frame method from run method.
            // The per-frame method is a ()V self-call (invokespecial or invokevirtual)
            // followed by a backward goto (the main game loop).
            final String runMethod = result[1];
            final String[] perFrameMethod = {null};

            ClassReader mcReader = new ClassReader(mcBytes);
            mcReader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                        String signature, String[] exceptions) {
                    if (name.equals(runMethod) && "()V".equals(descriptor)) {
                        return new MethodVisitor(Opcodes.ASM9) {
                            final Set<Label> seenLabels = new HashSet<Label>();
                            String pendingSelfCall = null;

                            @Override
                            public void visitLabel(Label label) {
                                seenLabels.add(label);
                            }

                            @Override
                            public void visitMethodInsn(int opcode, String owner, String name,
                                    String descriptor, boolean isInterface) {
                                pendingSelfCall = null;
                                if ((opcode == Opcodes.INVOKEVIRTUAL
                                        || opcode == Opcodes.INVOKESPECIAL)
                                        && owner.equals(mcClassInternal)
                                        && "()V".equals(descriptor)
                                        && !"<init>".equals(name)) {
                                    pendingSelfCall = name;
                                }
                            }

                            @Override
                            public void visitJumpInsn(int opcode, Label label) {
                                if (opcode == Opcodes.GOTO && pendingSelfCall != null
                                        && seenLabels.contains(label)) {
                                    // Backward goto after self ()V call = main game loop
                                    perFrameMethod[0] = pendingSelfCall;
                                }
                                pendingSelfCall = null;
                            }

                            // Reset pendingSelfCall on non-meta instructions
                            @Override public void visitInsn(int opcode) {
                                pendingSelfCall = null;
                            }
                            @Override public void visitVarInsn(int opcode, int var) {
                                pendingSelfCall = null;
                            }
                            @Override public void visitTypeInsn(int opcode, String type) {
                                pendingSelfCall = null;
                            }
                            @Override public void visitFieldInsn(int opcode, String owner,
                                    String name, String descriptor) {
                                pendingSelfCall = null;
                            }
                            @Override public void visitIntInsn(int opcode, int operand) {
                                pendingSelfCall = null;
                            }
                            @Override public void visitLdcInsn(Object value) {
                                pendingSelfCall = null;
                            }
                        };
                    }
                    return null;
                }
            }, 0);

            if (perFrameMethod[0] == null) {
                System.out.println("[McTestAgent] Auto-detect: could not find per-frame "
                        + "method in " + result[0] + "." + runMethod + "()");
                return null;
            }

            // Phase 3: Find tick method from per-frame method.
            // The tick method is a ()V self-call immediately followed by iinc
            // (the tick count loop: for (i=0; i<timer.ticks; i++) { tick(); }).
            final String pfMethod = perFrameMethod[0];

            mcReader = new ClassReader(mcBytes);
            mcReader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                        String signature, String[] exceptions) {
                    if (name.equals(pfMethod) && "()V".equals(descriptor)) {
                        return new MethodVisitor(Opcodes.ASM9) {
                            String pendingSelfCall = null;

                            @Override
                            public void visitMethodInsn(int opcode, String owner, String name,
                                    String descriptor, boolean isInterface) {
                                pendingSelfCall = null;
                                if ((opcode == Opcodes.INVOKEVIRTUAL
                                        || opcode == Opcodes.INVOKESPECIAL)
                                        && owner.equals(mcClassInternal)
                                        && "()V".equals(descriptor)
                                        && !"<init>".equals(name)) {
                                    pendingSelfCall = name;
                                }
                            }

                            @Override
                            public void visitIincInsn(int var, int increment) {
                                if (pendingSelfCall != null && result[2] == null) {
                                    result[2] = pendingSelfCall;
                                }
                                pendingSelfCall = null;
                            }

                            // Reset on non-meta instructions
                            @Override public void visitInsn(int opcode) {
                                pendingSelfCall = null;
                            }
                            @Override public void visitVarInsn(int opcode, int var) {
                                pendingSelfCall = null;
                            }
                            @Override public void visitTypeInsn(int opcode, String type) {
                                pendingSelfCall = null;
                            }
                            @Override public void visitFieldInsn(int opcode, String owner,
                                    String name, String descriptor) {
                                pendingSelfCall = null;
                            }
                            @Override public void visitIntInsn(int opcode, int operand) {
                                pendingSelfCall = null;
                            }
                            @Override public void visitLdcInsn(Object value) {
                                pendingSelfCall = null;
                            }
                            @Override public void visitJumpInsn(int opcode, Label label) {
                                pendingSelfCall = null;
                            }
                        };
                    }
                    return null;
                }
            }, 0);

            if (result[2] == null) {
                System.out.println("[McTestAgent] Auto-detect: could not find tick method in "
                        + result[0] + "." + pfMethod + "()");
                return null;
            }

            return result;

        } catch (Exception e) {
            System.out.println("[McTestAgent] Auto-detect failed: " + e.getMessage());
            return null;
        }
    }

    private static byte[] readStreamFully(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return bos.toByteArray();
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
     * Advice inlined into GLFW.glfwGetWindowAttrib() — forces GLFW_FOCUSED (0x20001)
     * queries to return 1. LWJGL3 replacement for DisplayActiveAdvice.
     * Xvfb never reports the window as focused, which causes Minecraft to open
     * the pause menu overlay and prevents setIngameFocus() from completing.
     */
    public static class GlfwFocusAdvice {
        @Advice.OnMethodExit
        public static void onExit(@Advice.Argument(1) int attrib,
                @Advice.Return(readOnly = false) int returnValue) {
            // GLFW_FOCUSED = 0x20001
            if (attrib == 0x20001) {
                returnValue = 1;
            }
        }
    }

    /**
     * Advice inlined into GLFW.glfwWindowShouldClose() — always returns false.
     * Prevents the game loop from exiting due to spurious window close signals
     * under Xvfb (e.g. after GL context corruption during resource reload).
     * The E2E test harness controls shutdown via process kill after timeout.
     */
    public static class GlfwWindowCloseAdvice {
        @Advice.OnMethodExit
        public static void onExit(@Advice.Return(readOnly = false) boolean returnValue) {
            returnValue = false;
        }
    }

    /**
     * Advice inlined into GLFW.glfwWindowHint() — changes OpenGL Core Profile
     * requests to Any Profile. MC 1.17+ requests Core Profile where legacy GL
     * functions (glPopMatrix, etc.) are unavailable and cause FATAL JVM aborts.
     * Compatibility Profile supports both legacy and modern GL functions.
     */
    public static class GlfwWindowHintAdvice {
        @Advice.OnMethodEnter
        public static void onEnter(@Advice.Argument(value = 0, readOnly = true) int hint,
                @Advice.Argument(value = 1, readOnly = false) int value) {
            // GLFW_OPENGL_PROFILE = 0x22008
            // Change GLFW_OPENGL_CORE_PROFILE (0x32001) to GLFW_OPENGL_ANY_PROFILE (0)
            if (hint == 0x22008 && value == 0x32001) {
                System.out.println("[McTestAgent] Forcing Compatibility Profile (was Core Profile)");
                value = 0;
            }
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
     * Advice inlined into BlockRenderDispatcher (1.14+) — suppresses exceptions
     * thrown during block tessellation. The async resource reload may not finish
     * model baking before the first render frame tries to compile chunk geometry,
     * causing NPE on null BakedModel. Blocks with missing models simply won't
     * render until the reload completes.
     */
    public static class BlockRenderSafetyAdvice {
        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void onExit(@Advice.Thrown(readOnly = false) Throwable thrown) {
            if (thrown != null) {
                thrown = null;
            }
        }
    }

    /**
     * Advice inlined into the Minecraft class render method (REBASE) to handle
     * NPEs from the texture/model/shader loading pipeline during async resource
     * reload.
     *
     * For MC 1.14-1.16.x: the exit advice catches render exceptions and resets the
     * GL modelview matrix stack to prevent GL_STACK_OVERFLOW accumulation.
     *
     * For MC 1.17+ (Core Profile): 3D rendering is handled by GameRendererSkipAdvice
     * which skips GameRenderer.render() entirely, so f(boolean) runs normally for
     * network processing and ticks but never reaches the shader-based render pipeline.
     */
    public static class RenderLoopSafetyAdvice {

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void onExit(@Advice.Thrown(readOnly = false) Throwable thrown) {
            // Reset the GL modelview matrix stack on every exit (1.14-1.16.x only).
            // MC 1.17+ uses OpenGL Core Profile where glPopMatrix is unavailable
            // and calling it causes a JVM FATAL abort (not a catchable exception).
            if (System.getProperty("mctestagent.coreprofile") == null) {
                try {
                    Class<?> gl11 = Class.forName("org.lwjgl.opengl.GL11");
                    java.lang.reflect.Method pop = gl11.getMethod("glPopMatrix");
                    java.lang.reflect.Method getError = gl11.getMethod("glGetError");
                    java.lang.reflect.Method glDisable = gl11.getMethod("glDisable", int.class);
                    java.lang.reflect.Method glEnable = gl11.getMethod("glEnable", int.class);
                    try { glDisable.invoke(null, 0x92E0); } catch (Throwable ignored) {}
                    for (int drain = 0; drain < 64; drain++) {
                        Object e = getError.invoke(null);
                        if (e instanceof Integer && ((Integer) e) == 0) break;
                    }
                    for (int i = 0; i < 32; i++) {
                        pop.invoke(null);
                        Object err = getError.invoke(null);
                        if (err instanceof Integer && ((Integer) err) != 0) break;
                    }
                    try { glEnable.invoke(null, 0x92E0); } catch (Throwable ignored) {}
                } catch (Throwable t) {
                    // GL not available — safe to ignore
                }
            }
            if (thrown != null) {
                if (System.getProperty("mctestagent.render.logged") == null) {
                    System.setProperty("mctestagent.render.logged", "true");
                    System.out.println("[McTestAgent] Render exception caught: "
                            + thrown.getClass().getName() + ": " + thrown.getMessage());
                }
                thrown = null;
            }
        }
    }

    /**
     * Advice inlined into GameRenderer.render(float, long, boolean) on MC 1.17+
     * Core Profile clients. Skips the entire 3D render pipeline. Mesa's llvmpipe
     * software renderer cannot execute MC 1.17+'s shader-based render pipeline —
     * the JVM dies silently (no crash dump, no shutdown hook, no Java exception)
     * on the first frame that renders world geometry.
     *
     * This is targeted at the GameRenderer level (not the Minecraft class f(boolean))
     * so that network packet processing and tick processing inside f() continue
     * running normally. The world loads via JoinGame network packet, ticks fire
     * the TickHook state machine, and the test scenario completes — all without
     * any 3D rendering.
     */
    public static class GameRendererSkipAdvice {
        @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
        public static boolean onEnter() {
            if (System.getProperty("mctestagent.coreprofile") != null) {
                // Throttle the game loop since render (which normally paces frames)
                // is skipped. Without this, f(boolean) would spin at maximum speed.
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                return true; // non-default → skip render method body
            }
            return false;
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

    /**
     * Advice inlined into YggdrasilSocialInteractionsService — forces
     * serversAllowed/chatAllowed/realmsAllowed to return true. MC 1.16.4+
     * checks these privileges before using CLI --server/--port args; with
     * our fake accessToken "0" the API returns false, blocking auto-connect.
     */
    public static class SocialInteractionsAdvice {
        @Advice.OnMethodExit
        public static void onExit(@Advice.Return(readOnly = false) boolean returnValue) {
            returnValue = true;
        }
    }
}
