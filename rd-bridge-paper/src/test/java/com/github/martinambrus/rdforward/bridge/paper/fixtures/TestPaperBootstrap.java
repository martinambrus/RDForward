package com.github.martinambrus.rdforward.bridge.paper.fixtures;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEvents;

/**
 * Minimal bootstrapper fixture. Records its firing to {@link #PROP_ORDER}
 * and registers a Brigadier {@code hello} command against
 * {@link LifecycleEvents#COMMANDS}. The command's executor sets
 * {@link #PROP_BRIG_CMD} and echoes {@code hi from brigadier} back to the
 * sender so tests can verify end-to-end dispatch.
 *
 * <p>{@code createPlugin} returns {@code null} so the loader falls back to
 * instantiating the declared {@code main:} class — which is
 * {@link TestPaperPlugin}.
 */
public class TestPaperBootstrap implements PluginBootstrap {

    public static final String PROP_BOOTSTRAP = "rdforward.test.paper.bootstrap";
    public static final String PROP_BRIG_CMD = "rdforward.test.paper.brigadier_cmd";

    @Override
    public void bootstrap(BootstrapContext context) {
        System.setProperty(PROP_BOOTSTRAP, "true");
        String prior = System.getProperty(TestPaperPlugin.PROP_ORDER, "");
        System.setProperty(TestPaperPlugin.PROP_ORDER, prior.isEmpty() ? "bootstrap" : prior + ",bootstrap");

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            Commands commands = event.registrar();
            commands.register(
                    Commands.literal("hello")
                            .executes(ctx -> {
                                ctx.getSource().getSender().sendMessage("hi from brigadier");
                                System.setProperty(PROP_BRIG_CMD, ctx.getSource().getSender().getName());
                                return Command.SINGLE_SUCCESS;
                            })
                            .build(),
                    "Says hi");
        });
    }
}
