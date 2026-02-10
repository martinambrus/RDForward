package com.github.martinambrus.rdforward;

/**
 * Bootstrap launcher for the Fabric-enabled fat JAR distribution.
 * Sets Fabric system properties, then launches via Fabric Loader's KnotClient
 * (which discovers our GameProvider and Mixins).
 * LWJGL 3 automatically extracts native libraries from classpath resources.
 */
public class FabricNativeLauncher {

    public static void main(String[] args) throws Exception {
        // Fabric Loader settings for fat JAR mode
        System.setProperty("fabric.gameVersion", "rd-132211");

        // Launch through Fabric Loader's Knot (applies Mixins, loads mods)
        net.fabricmc.loader.impl.launch.knot.KnotClient.main(args);
    }
}
