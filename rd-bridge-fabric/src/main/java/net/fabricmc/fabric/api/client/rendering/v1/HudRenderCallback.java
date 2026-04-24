package net.fabricmc.fabric.api.client.rendering.v1;

import com.github.martinambrus.rdforward.api.client.DrawContext;
import com.github.martinambrus.rdforward.api.event.Event;

/**
 * Fabric-compatible HUD render callback. Mods register a listener and
 * draw into the HUD each frame after the built-in HUD pass.
 *
 * <p>Signature deviates from upstream Fabric: the second argument is a
 * plain {@code float tickDelta} (0..1 partial-tick progress), not a
 * {@code RenderTickCounter} — RDForward has no equivalent type.
 *
 * <p>Wired through {@code FabricClientBridge.install()}: a forwarder on
 * {@code ClientEvents.RENDER_HUD} invokes every registered HudRenderCallback
 * listener. Mods do not need to install any bridging code themselves.
 */
@FunctionalInterface
public interface HudRenderCallback {

    Event<HudRenderCallback> EVENT = Event.create(
            (ctx, tickDelta) -> {},
            listeners -> (ctx, tickDelta) -> {
                for (HudRenderCallback l : listeners) l.onHudRender(ctx, tickDelta);
            }
    );

    void onHudRender(DrawContext drawContext, float tickDelta);
}
