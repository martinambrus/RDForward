package com.github.martinambrus.rdforward.server;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Launcher wrapper for old Minecraft Alpha clients (1.1.x and earlier).
 *
 * These clients use a {@code Comparator} that violates the general contract
 * required by Java 7+'s TimSort (the default since JDK 7). This causes a
 * {@code java.lang.IllegalArgumentException: Comparison method violates its
 * general contract!} crash shortly after launch.
 *
 * The fix is to set the system property {@code java.util.Arrays.useLegacyMergeSort}
 * to {@code "true"} <b>before</b> any Minecraft classes are loaded (the property
 * is read once, on first use of {@code Arrays.sort}). This launcher sets the
 * property, then loads the Minecraft JAR via a {@link URLClassLoader} and
 * reflectively invokes {@code net.minecraft.client.Minecraft.main()}.
 *
 * <p>Usage:
 * <pre>
 *   java -cp rd-server-all.jar com.github.martinambrus.rdforward.server.AlphaClientLauncher [path/to/minecraft.jar]
 * </pre>
 * If no path is given, the launcher looks for {@code minecraft.jar} in the
 * current working directory.
 */
public class AlphaClientLauncher {

    private static final String MINECRAFT_MAIN_CLASS = "net.minecraft.client.Minecraft";
    private static final String DEFAULT_JAR_NAME = "minecraft.jar";

    public static void main(String[] args) {
        // --- 1. Set legacy merge sort BEFORE any Minecraft class is loaded ---
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

        // --- 2. Resolve the path to the Minecraft Alpha JAR ---
        File jarFile;
        if (args.length > 0) {
            jarFile = new File(args[0]);
        } else {
            jarFile = new File(DEFAULT_JAR_NAME);
        }

        if (!jarFile.isFile()) {
            System.err.println("Error: Minecraft Alpha JAR not found: " + jarFile.getAbsolutePath());
            System.err.println();
            System.err.println("Usage: java " + AlphaClientLauncher.class.getName() + " [path/to/minecraft.jar]");
            System.err.println();
            System.err.println("If no path is provided, the launcher looks for '" + DEFAULT_JAR_NAME + "'");
            System.err.println("in the current working directory.");
            System.exit(1);
        }

        // --- 3. Load the JAR and launch Minecraft ---
        try {
            URL jarUrl = jarFile.toURI().toURL();
            // Use the system classloader as parent so that java.* classes are available
            URLClassLoader loader = new URLClassLoader(new URL[]{jarUrl}, ClassLoader.getSystemClassLoader());

            // Set the context classloader so that any thread spawned by Minecraft
            // (e.g. resource loading) inherits access to the JAR's classes.
            Thread.currentThread().setContextClassLoader(loader);

            Class<?> minecraftClass = loader.loadClass(MINECRAFT_MAIN_CLASS);
            Method mainMethod = minecraftClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) new String[]{});
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Could not find class '" + MINECRAFT_MAIN_CLASS + "' in " + jarFile.getName());
            System.err.println("Make sure the JAR is a Minecraft Alpha client (not a server or launcher).");
            System.exit(1);
        } catch (NoSuchMethodException e) {
            System.err.println("Error: Class '" + MINECRAFT_MAIN_CLASS + "' does not have a main(String[]) method.");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error launching Minecraft Alpha client:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
