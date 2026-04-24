// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.fabric.api.client.model.loading.v1;

import com.github.martinambrus.rdforward.api.event.Event;

/**
 * Noop stub: RDForward does not use a block-model pipeline, so the plugin
 * hook exists only so Fabric client mods that register model plugins
 * compile and load. The {@link #REGISTER} event accepts listeners but
 * never fires them.
 */
public interface ModelLoadingPlugin {

    @FunctionalInterface
    interface Callback {
        void onRegister(ModelLoadingPlugin plugin);
    }

    /** Noop event — RDForward never invokes registered plugins. */
    Event<Callback> REGISTER = Event.create(
            plugin -> {},
            listeners -> plugin -> {}
    );

    /** Noop — plugin entrypoint returned when mods call into upstream Fabric. */
    void onInitializeModelLoader(Object pluginContext);
}
