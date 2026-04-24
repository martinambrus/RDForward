// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.minecraftforge.fml.javafmlmod;

import net.minecraftforge.eventbus.api.IEventBus;

/**
 * Stub of Forge's {@code FMLJavaModLoadingContext}. The real Forge loader
 * sets an instance before invoking a mod constructor and clears it after;
 * mods call {@link #get()} from their constructor to grab their per-mod
 * event bus. Our {@code ForgeModLoader} mirrors that contract via a
 * {@code ThreadLocal}.
 */
public final class FMLJavaModLoadingContext {

    private static final ThreadLocal<FMLJavaModLoadingContext> CURRENT = new ThreadLocal<>();

    private final IEventBus modEventBus;

    public FMLJavaModLoadingContext(IEventBus modEventBus) {
        this.modEventBus = modEventBus;
    }

    public IEventBus getModEventBus() { return modEventBus; }

    public static FMLJavaModLoadingContext get() {
        FMLJavaModLoadingContext ctx = CURRENT.get();
        if (ctx == null) {
            throw new IllegalStateException(
                    "FMLJavaModLoadingContext.get() called outside a mod constructor");
        }
        return ctx;
    }

    public static void setCurrent(FMLJavaModLoadingContext ctx) {
        if (ctx == null) CURRENT.remove(); else CURRENT.set(ctx);
    }
}
