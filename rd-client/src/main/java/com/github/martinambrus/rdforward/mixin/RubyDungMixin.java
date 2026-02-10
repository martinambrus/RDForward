package com.github.martinambrus.rdforward.mixin;

import com.mojang.rubydung.RubyDung;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects RDForward initialization into the RubyDung game loop.
 * The {@code run()} method is the game's main entry (called from the game thread).
 */
@Mixin(RubyDung.class)
public class RubyDungMixin {

    @Inject(method = "run", at = @At("HEAD"))
    private void onGameStart(CallbackInfo ci) {
        System.out.println();
        System.out.println("========================================");
        System.out.println(" RDForward " + getVersion());
        System.out.println(" Fabric Loader initialized");
        System.out.println("========================================");
        System.out.println();
    }

    private static String getVersion() {
        try {
            return net.fabricmc.loader.api.FabricLoader.getInstance()
                .getModContainer("rdforward")
                .map(mod -> "v" + mod.getMetadata().getVersion().getFriendlyString())
                .orElse("(dev)");
        } catch (Exception e) {
            return "(dev)";
        }
    }
}
