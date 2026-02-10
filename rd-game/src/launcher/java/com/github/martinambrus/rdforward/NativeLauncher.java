package com.github.martinambrus.rdforward;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Bootstrap launcher for the fat JAR distribution.
 * Extracts LWJGL native libraries from the JAR to a temporary directory,
 * sets the library path, then launches RubyDung.
 */
public class NativeLauncher {

    public static void main(String[] args) throws Exception {
        Path tempDir = extractNatives();

        // Tell LWJGL where to find the native libraries
        System.setProperty("org.lwjgl.librarypath", tempDir.toAbsolutePath().toString());

        // Clean up natives on exit
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
                } catch (IOException ignored) {
                }
            }
        }));

        // Launch the game
        com.mojang.rubydung.RubyDung.main(args);
    }

    private static Path extractNatives() throws IOException, URISyntaxException {
        String os = System.getProperty("os.name").toLowerCase();
        String nativePrefix;
        if (os.contains("win")) {
            nativePrefix = "natives/windows/";
        } else if (os.contains("mac") || os.contains("darwin")) {
            nativePrefix = "natives/osx/";
        } else {
            nativePrefix = "natives/linux/";
        }

        Path tempDir = Files.createTempDirectory("rdforward-natives");

        // Locate the running JAR file
        Path jarPath = Paths.get(NativeLauncher.class
            .getProtectionDomain()
            .getCodeSource()
            .getLocation()
            .toURI());

        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith(nativePrefix) && !entry.isDirectory()) {
                    String fileName = entry.getName().substring(
                        entry.getName().lastIndexOf('/') + 1);
                    Path dest = tempDir.resolve(fileName);
                    try (InputStream in = jar.getInputStream(entry)) {
                        Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }

        return tempDir;
    }
}
