package com.github.martinambrus.rdforward.e2e;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Downloads client JARs and LWJGL 2 natives from Mojang CDN (cached after first
 * run),
 * then launches the Minecraft client subprocess with the ByteBuddy agent
 * attached.
 */
public class ClientLauncher {

    private static final String JAVA8_PATH = "/usr/lib/jvm/temurin-8-jdk-amd64/bin/java";
    private static final String LIBS_DIR = "rd-e2e/libs";
    private static final String NATIVES_DIR = LIBS_DIR + "/natives-linux";

    private static final String ALPHA_122_JAR_URL = "https://launcher.mojang.com/v1/objects/1c28c8431392641045b59e98a81877d7c94ff0ca/client.jar";
    private static final String ALPHA_122_JAR = "alpha-1.2.2b-client.jar";

    private static final String ALPHA_123_JAR_URL = "https://launcher.mojang.com/v1/objects/7f60cb9d0d40af20001d15287b78aa26a217a910/client.jar";
    private static final String ALPHA_123_JAR = "alpha-1.2.3_04-client.jar";

    private static final String ALPHA_126_JAR_URL = "https://piston-data.mojang.com/v1/objects/a68c817afd6c05c253ba5462287c2c19bbb57935/client.jar";
    private static final String ALPHA_126_JAR = "alpha-1.2.6-client.jar";

    private static final String BETA_181_JAR_URL = "https://launcher.mojang.com/v1/objects/6b562463ccc2c7ff12ff350a2b04a67b3adcd37b/client.jar";
    private static final String BETA_181_JAR = "beta-1.8.1-client.jar";

    // JInput (required by Beta 1.8 for controller support, loaded via LWJGL)
    private static final String JINPUT_URL = "https://libraries.minecraft.net/net/java/jinput/jinput/2.0.5/jinput-2.0.5.jar";
    private static final String JINPUT_JAR = "jinput-2.0.5.jar";
    private static final String JINPUT_NATIVES_URL = "https://libraries.minecraft.net/net/java/jinput/jinput-platform/2.0.5/jinput-platform-2.0.5-natives-linux.jar";
    private static final String JINPUT_NATIVES_JAR = "jinput-platform-natives-linux.jar";

    // LWJGL 2 from Mojang CDN (includes native .so files)
    private static final String LWJGL2_NATIVES_URL = "https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl-platform/2.9.4-nightly-20150209/lwjgl-platform-2.9.4-nightly-20150209-natives-linux.jar";
    private static final String LWJGL2_NATIVES_JAR = "lwjgl-platform-natives-linux.jar";

    private static final String LWJGL2_JAR_URL = "https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209/lwjgl-2.9.4-nightly-20150209.jar";
    private static final String LWJGL2_JAR = "lwjgl-2.9.4.jar";

    private static final String LWJGL2_UTIL_URL = "https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl_util/2.9.4-nightly-20150209/lwjgl_util-2.9.4-nightly-20150209.jar";
    private static final String LWJGL2_UTIL_JAR = "lwjgl_util-2.9.4.jar";

    private Process clientProcess;

    /**
     * Launch an Alpha 1.2.6 client with the E2E agent attached.
     * Uses the default "world_loaded" scenario.
     */
    public Process launchAlpha126(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchAlpha126(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    /**
     * Launch an Alpha 1.2.6 client with the E2E agent attached.
     *
     * @param agentJarPath path to the rd-e2e-agent fat JAR
     * @param serverPort   the RDForward server port to connect to
     * @param statusDir    directory for agent status JSON output
     * @param display      X display string (e.g. ":99")
     * @param scenario     scenario name to run (e.g. "world_loaded", "environment_check")
     * @return the launched process
     */
    public Process launchAlpha126(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchAlpha126(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    /**
     * Launch an Alpha 1.2.6 client with cross-client support.
     */
    public Process launchAlpha126(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        ensureLibs();

        String clientJar = new File(LIBS_DIR, ALPHA_126_JAR).getAbsolutePath();
        String lwjglJar = new File(LIBS_DIR, LWJGL2_JAR).getAbsolutePath();
        String lwjglUtilJar = new File(LIBS_DIR, LWJGL2_UTIL_JAR).getAbsolutePath();
        String nativesPath = new File(NATIVES_DIR).getAbsolutePath();

        String agentArgs = "version=alpha126"
                + ",serverHost=localhost"
                + ",serverPort=" + serverPort
                + ",statusDir=" + statusDir.getAbsolutePath()
                + ",scenario=" + scenario;
        if (username != null) agentArgs += ",username=" + username;
        if (role != null) agentArgs += ",role=" + role;
        if (syncDir != null) agentArgs += ",syncDir=" + syncDir.getAbsolutePath();

        List<String> cmd = new ArrayList<>();
        cmd.add(JAVA8_PATH);
        cmd.add("-javaagent:" + agentJarPath + "=" + agentArgs);
        cmd.add("-Djava.library.path=" + nativesPath);
        cmd.add("-Djava.util.Arrays.useLegacyMergeSort=true");
        cmd.add("-Dhttp.proxyHost=127.0.0.1"); // Fast fail for dead legacy URLs
        cmd.add("-Dhttp.proxyPort=65535"); // Invalid port
        cmd.add("-Xmx256m");
        cmd.add("-cp");
        cmd.add(clientJar + ":" + lwjglJar + ":" + lwjglUtilJar);
        cmd.add("net.minecraft.client.Minecraft");

        System.out.println("[E2E] Launching client: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Map<String, String> env = pb.environment();
        env.put("DISPLAY", display);
        env.put("LIBGL_ALWAYS_SOFTWARE", "1"); // Mesa software rendering fallback
        pb.redirectErrorStream(true);
        pb.inheritIO();

        clientProcess = pb.start();
        return clientProcess;
    }

    /**
     * Launch an Alpha 1.2.2b client with the E2E agent attached.
     * Uses the default "world_loaded" scenario.
     */
    public Process launchAlpha122(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchAlpha122(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchAlpha122(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchAlpha122(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchAlpha122(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        ensureLibs();

        String clientJar = new File(LIBS_DIR, ALPHA_122_JAR).getAbsolutePath();
        String lwjglJar = new File(LIBS_DIR, LWJGL2_JAR).getAbsolutePath();
        String lwjglUtilJar = new File(LIBS_DIR, LWJGL2_UTIL_JAR).getAbsolutePath();
        String nativesPath = new File(NATIVES_DIR).getAbsolutePath();

        String agentArgs = "version=alpha122"
                + ",serverHost=localhost"
                + ",serverPort=" + serverPort
                + ",statusDir=" + statusDir.getAbsolutePath()
                + ",scenario=" + scenario;
        if (username != null) agentArgs += ",username=" + username;
        if (role != null) agentArgs += ",role=" + role;
        if (syncDir != null) agentArgs += ",syncDir=" + syncDir.getAbsolutePath();

        List<String> cmd = new ArrayList<>();
        cmd.add(JAVA8_PATH);
        cmd.add("-javaagent:" + agentJarPath + "=" + agentArgs);
        cmd.add("-Djava.library.path=" + nativesPath);
        cmd.add("-Djava.util.Arrays.useLegacyMergeSort=true");
        cmd.add("-Dhttp.proxyHost=127.0.0.1");
        cmd.add("-Dhttp.proxyPort=65535");
        cmd.add("-Xmx256m");
        cmd.add("-cp");
        cmd.add(clientJar + ":" + lwjglJar + ":" + lwjglUtilJar);
        cmd.add("net.minecraft.client.Minecraft");

        System.out.println("[E2E] Launching Alpha 1.2.2 client: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Map<String, String> env = pb.environment();
        env.put("DISPLAY", display);
        env.put("LIBGL_ALWAYS_SOFTWARE", "1");
        pb.redirectErrorStream(true);
        pb.inheritIO();

        clientProcess = pb.start();
        return clientProcess;
    }

    /**
     * Launch an Alpha 1.2.3_04 client with the E2E agent attached.
     * Uses the default "world_loaded" scenario.
     */
    public Process launchAlpha123(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchAlpha123(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchAlpha123(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchAlpha123(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchAlpha123(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        ensureLibs();

        String clientJar = new File(LIBS_DIR, ALPHA_123_JAR).getAbsolutePath();
        String lwjglJar = new File(LIBS_DIR, LWJGL2_JAR).getAbsolutePath();
        String lwjglUtilJar = new File(LIBS_DIR, LWJGL2_UTIL_JAR).getAbsolutePath();
        String nativesPath = new File(NATIVES_DIR).getAbsolutePath();

        String agentArgs = "version=alpha123"
                + ",serverHost=localhost"
                + ",serverPort=" + serverPort
                + ",statusDir=" + statusDir.getAbsolutePath()
                + ",scenario=" + scenario;
        if (username != null) agentArgs += ",username=" + username;
        if (role != null) agentArgs += ",role=" + role;
        if (syncDir != null) agentArgs += ",syncDir=" + syncDir.getAbsolutePath();

        List<String> cmd = new ArrayList<>();
        cmd.add(JAVA8_PATH);
        cmd.add("-javaagent:" + agentJarPath + "=" + agentArgs);
        cmd.add("-Djava.library.path=" + nativesPath);
        cmd.add("-Djava.util.Arrays.useLegacyMergeSort=true");
        cmd.add("-Dhttp.proxyHost=127.0.0.1");
        cmd.add("-Dhttp.proxyPort=65535");
        cmd.add("-Xmx256m");
        cmd.add("-cp");
        cmd.add(clientJar + ":" + lwjglJar + ":" + lwjglUtilJar);
        cmd.add("net.minecraft.client.Minecraft");

        System.out.println("[E2E] Launching Alpha 1.2.3 client: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Map<String, String> env = pb.environment();
        env.put("DISPLAY", display);
        env.put("LIBGL_ALWAYS_SOFTWARE", "1");
        pb.redirectErrorStream(true);
        pb.inheritIO();

        clientProcess = pb.start();
        return clientProcess;
    }

    /**
     * Launch a Beta 1.8.1 client with the E2E agent attached.
     * Uses the default "world_loaded" scenario.
     */
    public Process launchBeta181(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchBeta181(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    /**
     * Launch a Beta 1.8.1 client with the E2E agent attached.
     * Beta 1.8 introduced creative mode (gameMode=1). The server sends creative
     * mode in JoinGame, so the agent gets creative=true to adjust expectations.
     *
     * @param agentJarPath path to the rd-e2e-agent fat JAR
     * @param serverPort   the RDForward server port to connect to
     * @param statusDir    directory for agent status JSON output
     * @param display      X display string (e.g. ":99")
     * @param scenario     scenario name to run
     * @return the launched process
     */
    public Process launchBeta181(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchBeta181(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    /**
     * Launch a Beta 1.8.1 client with cross-client support.
     */
    public Process launchBeta181(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        ensureLibs();

        String clientJar = new File(LIBS_DIR, BETA_181_JAR).getAbsolutePath();
        String lwjglJar = new File(LIBS_DIR, LWJGL2_JAR).getAbsolutePath();
        String lwjglUtilJar = new File(LIBS_DIR, LWJGL2_UTIL_JAR).getAbsolutePath();
        String jinputJar = new File(LIBS_DIR, JINPUT_JAR).getAbsolutePath();
        String nativesPath = new File(NATIVES_DIR).getAbsolutePath();

        String agentArgs = "version=beta18"
                + ",creative=true"
                + ",serverHost=localhost"
                + ",serverPort=" + serverPort
                + ",statusDir=" + statusDir.getAbsolutePath()
                + ",scenario=" + scenario;
        if (username != null) agentArgs += ",username=" + username;
        if (role != null) agentArgs += ",role=" + role;
        if (syncDir != null) agentArgs += ",syncDir=" + syncDir.getAbsolutePath();

        List<String> cmd = new ArrayList<>();
        cmd.add(JAVA8_PATH);
        cmd.add("-javaagent:" + agentJarPath + "=" + agentArgs);
        cmd.add("-Djava.library.path=" + nativesPath);
        cmd.add("-Dhttp.proxyHost=127.0.0.1");
        cmd.add("-Dhttp.proxyPort=65535");
        cmd.add("-Xmx256m");
        cmd.add("-cp");
        cmd.add(clientJar + ":" + lwjglJar + ":" + lwjglUtilJar + ":" + jinputJar);
        cmd.add("net.minecraft.client.Minecraft");

        System.out.println("[E2E] Launching Beta 1.8.1 client: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Map<String, String> env = pb.environment();
        env.put("DISPLAY", display);
        env.put("LIBGL_ALWAYS_SOFTWARE", "1");
        pb.redirectErrorStream(true);
        pb.inheritIO();

        clientProcess = pb.start();
        return clientProcess;
    }

    /**
     * Launch an RD modded client (for pre-multiplayer versions like RubyDung,
     * Classic).
     * The modded fat JAR is built by rd-client and includes Fabric Loader + Mixin +
     * LWJGL 3 natives. Server connection is handled by the Fabric Mixin via CLI
     * args.
     *
     * Uses Java 21 (rd-client targets Java 21). The E2E agent (Java 8 bytecode)
     * runs fine on Java 21 via backwards compatibility.
     *
     * @param moddedJarPath path to the rd-client fat modded JAR (rd-client-all.jar)
     * @param agentJarPath  path to the rd-e2e-agent fat JAR
     * @param version       version identifier for agent mappings (e.g. "rubydung")
     * @param serverPort    the RDForward server port
     * @param username      player username
     * @param statusDir     directory for agent status JSON output
     * @param display       X display string
     * @return the launched process
     */
    public Process launchModdedClient(String moddedJarPath, String agentJarPath,
            String version, int serverPort, String username,
            File statusDir, String display) throws IOException {
        return launchModdedClient(moddedJarPath, agentJarPath, version, serverPort,
                username, statusDir, display, null);
    }

    public Process launchModdedClient(String moddedJarPath, String agentJarPath,
            String version, int serverPort, String username,
            File statusDir, String display, String scenario) throws IOException {
        return launchModdedClient(moddedJarPath, agentJarPath, version, serverPort,
                username, statusDir, display, scenario, null, null);
    }

    public Process launchModdedClient(String moddedJarPath, String agentJarPath,
            String version, int serverPort, String username,
            File statusDir, String display, String scenario,
            String role, File syncDir) throws IOException {
        String agentArgs = "version=" + version
                + ",statusDir=" + statusDir.getAbsolutePath();
        if (scenario != null) agentArgs += ",scenario=" + scenario;
        if (username != null) agentArgs += ",username=" + username;
        if (role != null) agentArgs += ",role=" + role;
        if (syncDir != null) agentArgs += ",syncDir=" + syncDir.getAbsolutePath();

        List<String> cmd = new ArrayList<>();
        cmd.add("java"); // system Java 21+
        cmd.add("-javaagent:" + agentJarPath + "=" + agentArgs);
        cmd.add("-Xmx256m");
        cmd.add("-jar");
        cmd.add(moddedJarPath);
        // Fabric Mixin reads these CLI args and auto-connects to the server
        cmd.add("--server=localhost:" + serverPort);
        cmd.add("--username=" + (username != null ? username : "E2EBot"));

        System.out.println("[E2E] Launching modded client: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Map<String, String> env = pb.environment();
        env.put("DISPLAY", display);
        env.put("LIBGL_ALWAYS_SOFTWARE", "1");
        // Force GLFW to use X11 backend (Xvfb) instead of Wayland.
        // GLFW 3.4+ calls wl_display_connect(NULL) which uses the default
        // "wayland-0" socket in XDG_RUNTIME_DIR, ignoring WAYLAND_DISPLAY.
        // Override XDG_RUNTIME_DIR to a clean directory without wayland-0.
        File cleanRuntime = new File(statusDir, "xdg-runtime");
        cleanRuntime.mkdirs();
        env.put("XDG_RUNTIME_DIR", cleanRuntime.getAbsolutePath());
        env.remove("WAYLAND_DISPLAY");
        env.put("XDG_SESSION_TYPE", "x11");
        pb.redirectErrorStream(true);
        // Redirect output to a log file in the status dir so we can debug failures
        File logFile = new File(statusDir, "client-output.log");
        pb.redirectOutput(logFile);

        clientProcess = pb.start();
        return clientProcess;
    }

    public void stop() {
        if (clientProcess != null && clientProcess.isAlive()) {
            clientProcess.destroyForcibly();
            System.out.println("[E2E] Client process stopped");
        }
    }

    /**
     * Ensure all required libraries are downloaded and natives extracted.
     * Uses a file lock to prevent races when multiple Gradle forks run in parallel.
     */
    private void ensureLibs() throws IOException, InterruptedException {
        File libsDir = new File(LIBS_DIR);
        libsDir.mkdirs();
        File lockFile = new File(libsDir, ".libs-lock");

        try (RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");
             FileChannel ch = raf.getChannel();
             FileLock lock = ch.lock()) {
            downloadIfMissing(ALPHA_122_JAR_URL, new File(libsDir, ALPHA_122_JAR));
            downloadIfMissing(ALPHA_123_JAR_URL, new File(libsDir, ALPHA_123_JAR));
            downloadIfMissing(ALPHA_126_JAR_URL, new File(libsDir, ALPHA_126_JAR));
            downloadIfMissing(BETA_181_JAR_URL, new File(libsDir, BETA_181_JAR));
            downloadIfMissing(LWJGL2_JAR_URL, new File(libsDir, LWJGL2_JAR));
            downloadIfMissing(LWJGL2_UTIL_URL, new File(libsDir, LWJGL2_UTIL_JAR));
            downloadIfMissing(JINPUT_URL, new File(libsDir, JINPUT_JAR));
            ensureNatives(libsDir);
        }
    }

    private void ensureNatives(File libsDir) throws IOException, InterruptedException {
        File nativesDir = new File(NATIVES_DIR);
        File nativesJar = new File(libsDir, LWJGL2_NATIVES_JAR);
        File jinputNativesJar = new File(libsDir, JINPUT_NATIVES_JAR);

        downloadIfMissing(LWJGL2_NATIVES_URL, nativesJar);
        downloadIfMissing(JINPUT_NATIVES_URL, jinputNativesJar);

        // Extract .so files from natives JARs if not already done
        if (!nativesDir.exists() || nativesDir.list().length == 0) {
            nativesDir.mkdirs();
            extractNatives(nativesJar, nativesDir);
            extractNatives(jinputNativesJar, nativesDir);
        }
    }

    private void downloadIfMissing(String url, File dest) throws IOException, InterruptedException {
        if (dest.exists() && dest.length() > 0)
            return;
        System.out.println("[E2E] Downloading " + dest.getName() + "...");
        ProcessBuilder pb = new ProcessBuilder("curl", "-fSL", "-o", dest.getAbsolutePath(), url);
        pb.redirectErrorStream(true);
        pb.inheritIO();
        Process p = pb.start();
        int rc = p.waitFor();
        if (rc != 0) {
            throw new IOException("Failed to download " + url + " (exit code " + rc + ")");
        }
    }

    private void extractNatives(File jar, File destDir) throws IOException {
        System.out.println("[E2E] Extracting natives to " + destDir.getAbsolutePath());
        ZipFile zip = new ZipFile(jar);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory())
                continue;
            String name = entry.getName();
            // Only extract .so files (Linux natives)
            if (name.endsWith(".so")) {
                // Strip directory prefix (e.g. "linux/" or just filename)
                String filename = name.contains("/")
                        ? name.substring(name.lastIndexOf('/') + 1)
                        : name;
                File dest = new File(destDir, filename);
                InputStream is = zip.getInputStream(entry);
                FileOutputStream fos = new FileOutputStream(dest);
                byte[] buf = new byte[8192];
                int n;
                while ((n = is.read(buf)) > 0) {
                    fos.write(buf, 0, n);
                }
                fos.close();
                is.close();
            }
        }
        zip.close();
    }
}
