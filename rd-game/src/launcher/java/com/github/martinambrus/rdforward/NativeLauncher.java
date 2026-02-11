package com.github.martinambrus.rdforward;

/**
 * Bootstrap launcher for the fat JAR distribution.
 * LWJGL 3 automatically extracts native libraries from classpath resources,
 * so no manual extraction is needed.
 */
public class NativeLauncher {

    public static void main(String[] args) throws Exception {
        com.mojang.rubydung.RubyDung.main(args);
    }
}
