package com.github.martinambrus.rdforward;

import java.util.ArrayList;
import java.util.List;

/**
 * Bootstrap launcher for the Fabric-enabled fat JAR distribution.
 * Sets Fabric system properties, then launches via Fabric Loader's KnotClient
 * (which discovers our GameProvider and Mixins).
 * LWJGL 3 automatically extracts native libraries from classpath resources.
 *
 * Usage:
 *   java -jar rd-client-all.jar [--server=host:port] [--username=Name]
 *
 * Starts in single player by default. Press Ctrl+M in-game to toggle multiplayer.
 * If --server is provided, the game starts in multiplayer mode automatically.
 * JVM properties (-Drdforward.server, -Drdforward.username) also work
 * but must be placed before -jar on the command line.
 */
public class FabricNativeLauncher {

    public static void main(String[] args) throws Exception {
        // Fabric Loader settings for fat JAR mode
        System.setProperty("fabric.gameVersion", "rd-132211");

        // Parse our flags from args, pass the rest through to Fabric/game
        List<String> remaining = new ArrayList<>();
        for (String arg : args) {
            if (arg.startsWith("--server=")) {
                System.setProperty("rdforward.server", arg.substring("--server=".length()));
            } else if (arg.startsWith("--username=")) {
                System.setProperty("rdforward.username", arg.substring("--username=".length()));
            } else {
                remaining.add(arg);
            }
        }

        // Launch through Fabric Loader's Knot (applies Mixins, loads mods)
        net.fabricmc.loader.impl.launch.knot.KnotClient.main(remaining.toArray(new String[0]));
    }
}
