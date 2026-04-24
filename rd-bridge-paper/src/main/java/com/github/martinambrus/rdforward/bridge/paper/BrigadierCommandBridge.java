package com.github.martinambrus.rdforward.bridge.paper;

import com.github.martinambrus.rdforward.api.command.CommandRegistry;

import java.util.logging.Logger;

/**
 * Flushes {@link CollectingCommandsRegistrar} entries to the rd-api
 * {@link CommandRegistry}. The new paper-api 26.1.2+ Brigadier surface
 * changed shape (generics erased in our stubs, different return types)
 * enough that the original tree-walker cannot compile against it. Since
 * no plugin we currently test uses Paper's Brigadier registration, this
 * is a no-op placeholder — entries are accepted by
 * {@link CollectingCommandsRegistrar} and quietly discarded. Revisit when
 * a fixture plugin actually requires Brigadier dispatch.
 */
public final class BrigadierCommandBridge {

    private static final Logger LOG = Logger.getLogger("RDForward/PaperBridge");

    private BrigadierCommandBridge() {}

    public static void registerWithRdApi(CollectingCommandsRegistrar collected,
                                         CommandRegistry registry,
                                         String pluginName) {
        if (registry == null || collected == null) return;
        int count = collected.entries().size();
        if (count > 0) {
            LOG.fine("[PaperBridge] Dropping " + count + " Brigadier command(s) from "
                    + pluginName + " — Brigadier dispatch not yet wired.");
        }
    }
}
