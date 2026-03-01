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
    private static final String JAVA16_PATH = "/tmp/temurin-17/bin/java"; // Java 16 not installed; 17 is compatible
    private static final String JAVA17_PATH = "/tmp/temurin-17/bin/java";
    private static final String LIBS_DIR = "rd-e2e/libs";
    private static final String NATIVES_DIR = LIBS_DIR + "/natives-linux";
    private static final String VERSION_MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    private static final String ALPHA_1015_JAR_URL = "https://launcher.mojang.com/v1/objects/03edaff812bedd4157a90877e779d7b7ecf78e97/client.jar";
    private static final String ALPHA_1015_JAR = "alpha-1.0.15-client.jar";

    private static final String ALPHA_1016_JAR_URL = "https://launcher.mojang.com/v1/objects/98ce80c7630ccb3bb38687ff98bfd18935d49a57/client.jar";
    private static final String ALPHA_1016_JAR = "alpha-1.0.16-client.jar";

    private static final String ALPHA_1017_JAR_URL = "https://launcher.mojang.com/v1/objects/61cb4c717981f34bf90e8502d2eb8cf2aa6db0cd/client.jar";
    private static final String ALPHA_1017_JAR = "alpha-1.0.17_04-client.jar";

    private static final String ALPHA_110_JAR_URL = "https://launcher.mojang.com/v1/objects/d58d1db929994ff383bdbe6fed31887e04b965c3/client.jar";
    private static final String ALPHA_110_JAR = "alpha-1.1.0-client.jar";

    private static final String ALPHA_120_JAR_URL = "https://launcher.mojang.com/v1/objects/b99da0a683e6dc1ade4df1bf159e021ad07d4fca/client.jar";
    private static final String ALPHA_120_JAR = "alpha-1.2.0_02-client.jar";

    private static final String ALPHA_122_JAR_URL = "https://launcher.mojang.com/v1/objects/1c28c8431392641045b59e98a81877d7c94ff0ca/client.jar";
    private static final String ALPHA_122_JAR = "alpha-1.2.2b-client.jar";

    private static final String ALPHA_123_JAR_URL = "https://launcher.mojang.com/v1/objects/7f60cb9d0d40af20001d15287b78aa26a217a910/client.jar";
    private static final String ALPHA_123_JAR = "alpha-1.2.3_04-client.jar";

    private static final String ALPHA_126_JAR_URL = "https://piston-data.mojang.com/v1/objects/a68c817afd6c05c253ba5462287c2c19bbb57935/client.jar";
    private static final String ALPHA_126_JAR = "alpha-1.2.6-client.jar";

    private static final String BETA_10_JAR_URL = "https://launcher.mojang.com/v1/objects/93faf3398ebf8008d59852dc3c2b22b909ca8a49/client.jar";
    private static final String BETA_10_JAR = "beta-1.0-client.jar";

    private static final String BETA_122_JAR_URL = "https://launcher.mojang.com/v1/objects/093f371e1a05d89664cfb8068d607953687d5d94/client.jar";
    private static final String BETA_122_JAR = "beta-1.2_02-client.jar";

    private static final String BETA_131_JAR_URL = "https://launcher.mojang.com/v1/objects/add3809d2c075e985d4b583632dac3d9c3872945/client.jar";
    private static final String BETA_131_JAR = "beta-1.3_01-client.jar";

    private static final String BETA_141_JAR_URL = "https://launcher.mojang.com/v1/objects/6f157f26955c35006c1afa8b0479e0ce785fb864/client.jar";
    private static final String BETA_141_JAR = "beta-1.4_01-client.jar";

    private static final String BETA_151_JAR_URL = "https://launcher.mojang.com/v1/objects/e2a692e5e8160c84b29c834ecbf398618db9749c/client.jar";
    private static final String BETA_151_JAR = "beta-1.5_01-client.jar";

    private static final String BETA_16_JAR_URL = "https://launcher.mojang.com/v1/objects/ecc0288d218fd7479027a17c150cbf283fa950a1/client.jar";
    private static final String BETA_16_JAR = "beta-1.6-client.jar";

    private static final String BETA_17_JAR_URL = "https://launcher.mojang.com/v1/objects/ad7960853437bcab86bd72c4a1b95f6fe19f4258/client.jar";
    private static final String BETA_17_JAR = "beta-1.7-client.jar";

    private static final String BETA_173_JAR_URL = "https://launcher.mojang.com/v1/objects/43db9b498cb67058d2e12d394e6507722e71bb45/client.jar";
    private static final String BETA_173_JAR = "beta-1.7.3-client.jar";

    private static final String BETA_181_JAR_URL = "https://launcher.mojang.com/v1/objects/6b562463ccc2c7ff12ff350a2b04a67b3adcd37b/client.jar";
    private static final String BETA_181_JAR = "beta-1.8.1-client.jar";

    private static final String BETA_19PRE5_JAR_URL = "https://vault.omniarchive.uk/archive/java/client-beta/b1.9/pre/b1.9-pre5.jar";
    private static final String BETA_19PRE5_JAR = "beta-1.9-pre5-client.jar";

    // --- Pre-Netty Release JARs (1.0-1.6.4) ---
    private static final String RELEASE_10_JAR_URL = "https://launcher.mojang.com/v1/objects/b679fea27f2284836202e9365e13a82552092e5d/client.jar";
    private static final String RELEASE_10_JAR = "release-1.0-client.jar";

    private static final String RELEASE_11_JAR_URL = "https://launcher.mojang.com/v1/objects/f690d4136b0026d452163538495b9b0e8513d718/client.jar";
    private static final String RELEASE_11_JAR = "release-1.1-client.jar";

    private static final String RELEASE_121_JAR_URL = "https://launcher.mojang.com/v1/objects/c7662ac43dd04bfd677694a06972a2aaaf426505/client.jar";
    private static final String RELEASE_121_JAR = "release-1.2.1-client.jar";

    private static final String RELEASE_122_JAR_URL = "https://launcher.mojang.com/v1/objects/1dadfc4de6898751f547f24f72c7271218e4e28f/client.jar";
    private static final String RELEASE_122_JAR = "release-1.2.2-client.jar";

    private static final String RELEASE_123_JAR_URL = "https://launcher.mojang.com/v1/objects/5134e433afeba375c00bbdcd8aead1d3222813ee/client.jar";
    private static final String RELEASE_123_JAR = "release-1.2.3-client.jar";

    private static final String RELEASE_124_JAR_URL = "https://launcher.mojang.com/v1/objects/ad6d1fe7455857269d4185cb8f24e62cc0241aaf/client.jar";
    private static final String RELEASE_124_JAR = "release-1.2.4-client.jar";

    private static final String RELEASE_125_JAR_URL = "https://launcher.mojang.com/v1/objects/4a2fac7504182a97dcbcd7560c6392d7c8139928/client.jar";
    private static final String RELEASE_125_JAR = "release-1.2.5-client.jar";

    private static final String RELEASE_131_JAR_URL = "https://launcher.mojang.com/v1/objects/33167e71e85ab8e6ddbe168bc67f6ec19d708d62/client.jar";
    private static final String RELEASE_131_JAR = "release-1.3.1-client.jar";

    private static final String RELEASE_132_JAR_URL = "https://launcher.mojang.com/v1/objects/c2efd57c7001ddf505ca534e54abf3d006e48309/client.jar";
    private static final String RELEASE_132_JAR = "release-1.3.2-client.jar";

    private static final String RELEASE_142_JAR_URL = "https://launcher.mojang.com/v1/objects/42d6744cfbbd2958f9e6688dd6e78d86d658d0d4/client.jar";
    private static final String RELEASE_142_JAR = "release-1.4.2-client.jar";

    private static final String RELEASE_144_JAR_URL = "https://launcher.mojang.com/v1/objects/b9b2a9e9adf1bc834647febc93a4222b4fd6e403/client.jar";
    private static final String RELEASE_144_JAR = "release-1.4.4-client.jar";

    private static final String RELEASE_145_JAR_URL = "https://launcher.mojang.com/v1/objects/7a8a963ababfec49406e1541d3a87198e50604e5/client.jar";
    private static final String RELEASE_145_JAR = "release-1.4.5-client.jar";

    private static final String RELEASE_146_JAR_URL = "https://launcher.mojang.com/v1/objects/116758f41b32e8d1a71a4ad6236579acd724bca7/client.jar";
    private static final String RELEASE_146_JAR = "release-1.4.6-client.jar";

    private static final String RELEASE_147_JAR_URL = "https://launcher.mojang.com/v1/objects/53ed4b9d5c358ecfff2d8b846b4427b888287028/client.jar";
    private static final String RELEASE_147_JAR = "release-1.4.7-client.jar";

    private static final String RELEASE_151_JAR_URL = "https://launcher.mojang.com/v1/objects/047136381a552f34b1963c43304a1ad4dc0d2d8e/client.jar";
    private static final String RELEASE_151_JAR = "release-1.5.1-client.jar";

    private static final String RELEASE_152_JAR_URL = "https://launcher.mojang.com/v1/objects/465378c9dc2f779ae1d6e8046ebc46fb53a57968/client.jar";
    private static final String RELEASE_152_JAR = "release-1.5.2-client.jar";

    private static final String RELEASE_161_JAR_URL = "https://launcher.mojang.com/v1/objects/17e2c28fb54666df5640b2c822ea8042250ef592/client.jar";
    private static final String RELEASE_161_JAR = "release-1.6.1-client.jar";

    private static final String RELEASE_162_JAR_URL = "https://launcher.mojang.com/v1/objects/b6cb68afde1d9cf4a20cbf27fa90d0828bf440a4/client.jar";
    private static final String RELEASE_162_JAR = "release-1.6.2-client.jar";

    private static final String RELEASE_164_JAR_URL = "https://launcher.mojang.com/v1/objects/1703704407101cf72bd88e68579e3696ce733ecd/client.jar";
    private static final String RELEASE_164_JAR = "release-1.6.4-client.jar";

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
     * Launch an Alpha 1.0.15 client with the E2E agent attached.
     * Uses the default "world_loaded" scenario.
     */
    public Process launchAlpha1015(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchAlpha1015(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchAlpha1015(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchAlpha1015(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchAlpha1015(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        ensureLibs();

        String clientJar = new File(LIBS_DIR, ALPHA_1015_JAR).getAbsolutePath();
        String lwjglJar = new File(LIBS_DIR, LWJGL2_JAR).getAbsolutePath();
        String lwjglUtilJar = new File(LIBS_DIR, LWJGL2_UTIL_JAR).getAbsolutePath();
        String nativesPath = new File(NATIVES_DIR).getAbsolutePath();

        // Pre-1.2.0 clients get kicked on first connect (TimSort warning).
        if (username == null) username = "E2EBot";

        String agentArgs = "version=alpha1015"
                + ",serverHost=localhost"
                + ",serverPort=" + serverPort
                + ",statusDir=" + statusDir.getAbsolutePath()
                + ",scenario=" + scenario;
        agentArgs += ",username=" + username;
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

        System.out.println("[E2E] Launching Alpha 1.0.15 client: " + String.join(" ", cmd));

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
     * Launch an Alpha 1.0.16 client with the E2E agent attached.
     * Uses the default "world_loaded" scenario.
     */
    public Process launchAlpha1016(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchAlpha1016(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchAlpha1016(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchAlpha1016(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchAlpha1016(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        ensureLibs();

        String clientJar = new File(LIBS_DIR, ALPHA_1016_JAR).getAbsolutePath();
        String lwjglJar = new File(LIBS_DIR, LWJGL2_JAR).getAbsolutePath();
        String lwjglUtilJar = new File(LIBS_DIR, LWJGL2_UTIL_JAR).getAbsolutePath();
        String nativesPath = new File(NATIVES_DIR).getAbsolutePath();

        // Pre-1.2.0 clients get kicked on first connect (TimSort warning).
        if (username == null) username = "E2EBot";

        String agentArgs = "version=alpha1016"
                + ",serverHost=localhost"
                + ",serverPort=" + serverPort
                + ",statusDir=" + statusDir.getAbsolutePath()
                + ",scenario=" + scenario;
        agentArgs += ",username=" + username;
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

        System.out.println("[E2E] Launching Alpha 1.0.16 client: " + String.join(" ", cmd));

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
     * Launch an Alpha 1.0.17_04 client with the E2E agent attached.
     * Uses the default "world_loaded" scenario.
     */
    public Process launchAlpha1017(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchAlpha1017(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchAlpha1017(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchAlpha1017(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchAlpha1017(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        ensureLibs();

        String clientJar = new File(LIBS_DIR, ALPHA_1017_JAR).getAbsolutePath();
        String lwjglJar = new File(LIBS_DIR, LWJGL2_JAR).getAbsolutePath();
        String lwjglUtilJar = new File(LIBS_DIR, LWJGL2_UTIL_JAR).getAbsolutePath();
        String nativesPath = new File(NATIVES_DIR).getAbsolutePath();

        // Pre-1.2.0 clients get kicked on first connect (TimSort warning).
        // Use a fixed username so the server can pre-seed the player position.
        if (username == null) username = "E2EBot";

        String agentArgs = "version=alpha1017"
                + ",serverHost=localhost"
                + ",serverPort=" + serverPort
                + ",statusDir=" + statusDir.getAbsolutePath()
                + ",scenario=" + scenario;
        agentArgs += ",username=" + username;
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

        System.out.println("[E2E] Launching Alpha 1.0.17 client: " + String.join(" ", cmd));

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
     * Launch an Alpha 1.1.0 client with the E2E agent attached.
     * Uses the default "world_loaded" scenario.
     */
    public Process launchAlpha110(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchAlpha110(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchAlpha110(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchAlpha110(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchAlpha110(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        ensureLibs();

        String clientJar = new File(LIBS_DIR, ALPHA_110_JAR).getAbsolutePath();
        String lwjglJar = new File(LIBS_DIR, LWJGL2_JAR).getAbsolutePath();
        String lwjglUtilJar = new File(LIBS_DIR, LWJGL2_UTIL_JAR).getAbsolutePath();
        String nativesPath = new File(NATIVES_DIR).getAbsolutePath();

        // Pre-1.2.0 clients get kicked on first connect (TimSort warning).
        // Use a fixed username so the server can pre-seed the player position.
        if (username == null) username = "E2EBot";

        String agentArgs = "version=alpha110"
                + ",serverHost=localhost"
                + ",serverPort=" + serverPort
                + ",statusDir=" + statusDir.getAbsolutePath()
                + ",scenario=" + scenario;
        agentArgs += ",username=" + username;
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

        System.out.println("[E2E] Launching Alpha 1.1.0 client: " + String.join(" ", cmd));

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
     * Launch an Alpha 1.2.0_02 client with the E2E agent attached.
     * Uses the default "world_loaded" scenario.
     */
    public Process launchAlpha120(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchAlpha120(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchAlpha120(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchAlpha120(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchAlpha120(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        ensureLibs();

        String clientJar = new File(LIBS_DIR, ALPHA_120_JAR).getAbsolutePath();
        String lwjglJar = new File(LIBS_DIR, LWJGL2_JAR).getAbsolutePath();
        String lwjglUtilJar = new File(LIBS_DIR, LWJGL2_UTIL_JAR).getAbsolutePath();
        String nativesPath = new File(NATIVES_DIR).getAbsolutePath();

        String agentArgs = "version=alpha120"
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

        System.out.println("[E2E] Launching Alpha 1.2.0 client: " + String.join(" ", cmd));

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

    // --- Beta 1.0 through 1.7.3 launchers (survival only, no JInput) ---

    public Process launchBeta10(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchBeta10(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchBeta10(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchBeta10(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchBeta10(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreBeta18(BETA_10_JAR, "beta10", "Beta 1.0",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    public Process launchBeta122(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchBeta122(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchBeta122(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchBeta122(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchBeta122(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreBeta18(BETA_122_JAR, "beta12", "Beta 1.2_02",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    public Process launchBeta131(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchBeta131(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchBeta131(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchBeta131(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchBeta131(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreBeta18(BETA_131_JAR, "beta13", "Beta 1.3_01",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    public Process launchBeta141(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchBeta141(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchBeta141(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchBeta141(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchBeta141(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreBeta18(BETA_141_JAR, "beta14", "Beta 1.4_01",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    public Process launchBeta151(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchBeta151(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchBeta151(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchBeta151(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchBeta151(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreBeta18(BETA_151_JAR, "beta15", "Beta 1.5_01",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    public Process launchBeta16(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchBeta16(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchBeta16(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchBeta16(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchBeta16(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreBeta18(BETA_16_JAR, "beta16", "Beta 1.6",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    public Process launchBeta17(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchBeta17(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchBeta17(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchBeta17(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchBeta17(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreBeta18(BETA_17_JAR, "beta17", "Beta 1.7",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    public Process launchBeta173(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchBeta173(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    public Process launchBeta173(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchBeta173(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    public Process launchBeta173(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreBeta18(BETA_173_JAR, "beta173", "Beta 1.7.3",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    /**
     * Shared launcher for pre-1.8 Beta clients (survival only, no JInput needed).
     */
    private Process launchPreBeta18(String jarName, String versionId, String displayName,
            String agentJarPath, int serverPort, File statusDir, String display,
            String scenario, String username, String role,
            File syncDir) throws IOException, InterruptedException {
        ensureLibs();

        String clientJar = new File(LIBS_DIR, jarName).getAbsolutePath();
        String lwjglJar = new File(LIBS_DIR, LWJGL2_JAR).getAbsolutePath();
        String lwjglUtilJar = new File(LIBS_DIR, LWJGL2_UTIL_JAR).getAbsolutePath();
        String nativesPath = new File(NATIVES_DIR).getAbsolutePath();

        String agentArgs = "version=" + versionId
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

        System.out.println("[E2E] Launching " + displayName + " client: " + String.join(" ", cmd));

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
     * Launch a Beta 1.9 Pre-release 5 client with the E2E agent attached.
     * Uses the default "world_loaded" scenario.
     */
    public Process launchBeta19Pre5(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchBeta19Pre5(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }

    /**
     * Launch a Beta 1.9 Pre-release 5 client with the E2E agent attached.
     * Beta 1.9 has creative mode (like 1.8.1), so creative=true.
     *
     * @param agentJarPath path to the rd-e2e-agent fat JAR
     * @param serverPort   the RDForward server port to connect to
     * @param statusDir    directory for agent status JSON output
     * @param display      X display string (e.g. ":99")
     * @param scenario     scenario name to run
     * @return the launched process
     */
    public Process launchBeta19Pre5(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchBeta19Pre5(agentJarPath, serverPort, statusDir, display, scenario,
                null, null, null);
    }

    /**
     * Launch a Beta 1.9 Pre-release 5 client with cross-client support.
     */
    public Process launchBeta19Pre5(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        ensureLibs();

        String clientJar = new File(LIBS_DIR, BETA_19PRE5_JAR).getAbsolutePath();
        String lwjglJar = new File(LIBS_DIR, LWJGL2_JAR).getAbsolutePath();
        String lwjglUtilJar = new File(LIBS_DIR, LWJGL2_UTIL_JAR).getAbsolutePath();
        String jinputJar = new File(LIBS_DIR, JINPUT_JAR).getAbsolutePath();
        String nativesPath = new File(NATIVES_DIR).getAbsolutePath();

        String agentArgs = "version=beta19pre5"
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

        System.out.println("[E2E] Launching Beta 1.9 Pre-release 5 client: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Map<String, String> env = pb.environment();
        env.put("DISPLAY", display);
        env.put("LIBGL_ALWAYS_SOFTWARE", "1");
        pb.redirectErrorStream(true);
        pb.inheritIO();

        clientProcess = pb.start();
        return clientProcess;
    }

    // =====================================================================
    // Pre-Netty Release launchers (1.0-1.6.4)
    // All use creative=true, JInput, net.minecraft.client.Minecraft main class.
    // =====================================================================

    /**
     * Shared launcher for pre-Netty release clients (1.0-1.6.4).
     * Same pattern as Beta 1.8+: LWJGL2 + JInput + Minecraft main class.
     */
    private Process launchPreNettyRelease(String jarName, String versionId,
            String displayName, String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role,
            File syncDir) throws IOException, InterruptedException {
        ensureLibs();

        String clientJar = new File(LIBS_DIR, jarName).getAbsolutePath();
        String lwjglJar = new File(LIBS_DIR, LWJGL2_JAR).getAbsolutePath();
        String lwjglUtilJar = new File(LIBS_DIR, LWJGL2_UTIL_JAR).getAbsolutePath();
        String jinputJar = new File(LIBS_DIR, JINPUT_JAR).getAbsolutePath();
        String nativesPath = new File(NATIVES_DIR).getAbsolutePath();

        String agentArgs = "version=" + versionId
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

        System.out.println("[E2E] Launching " + displayName + " client: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Map<String, String> env = pb.environment();
        env.put("DISPLAY", display);
        env.put("LIBGL_ALWAYS_SOFTWARE", "1");
        pb.redirectErrorStream(true);
        pb.inheritIO();

        clientProcess = pb.start();
        return clientProcess;
    }

    // --- Release 1.0 ---
    public Process launchRelease10(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease10(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease10(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease10(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease10(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_10_JAR, "release10", "Release 1.0",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.1 ---
    public Process launchRelease11(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease11(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease11(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease11(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease11(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_11_JAR, "release11", "Release 1.1",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.2.1 ---
    public Process launchRelease1_2_1(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease1_2_1(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease1_2_1(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease1_2_1(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease1_2_1(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_121_JAR, "release_1_2_1", "Release 1.2.1",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.2.2 ---
    public Process launchRelease1_2_2(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease1_2_2(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease1_2_2(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease1_2_2(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease1_2_2(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_122_JAR, "release_1_2_2", "Release 1.2.2",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.2.3 ---
    public Process launchRelease1_2_3(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease1_2_3(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease1_2_3(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease1_2_3(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease1_2_3(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_123_JAR, "release_1_2_3", "Release 1.2.3",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.2.4 ---
    public Process launchRelease1_2_4(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease1_2_4(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease1_2_4(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease1_2_4(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease1_2_4(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_124_JAR, "release_1_2_4", "Release 1.2.4",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.2.5 ---
    public Process launchRelease1_2_5(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease1_2_5(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease1_2_5(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease1_2_5(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease1_2_5(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_125_JAR, "release_1_2_5", "Release 1.2.5",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.3.1 ---
    public Process launchRelease131(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease131(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease131(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease131(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease131(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_131_JAR, "release131", "Release 1.3.1",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.3.2 ---
    public Process launchRelease132(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease132(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease132(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease132(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease132(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_132_JAR, "release132", "Release 1.3.2",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.4.2 ---
    public Process launchRelease142(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease142(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease142(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease142(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease142(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_142_JAR, "release142", "Release 1.4.2",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.4.4 ---
    public Process launchRelease144(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease144(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease144(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease144(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease144(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_144_JAR, "release144", "Release 1.4.4",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.4.5 ---
    public Process launchRelease145(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease145(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease145(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease145(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease145(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_145_JAR, "release145", "Release 1.4.5",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.4.6 ---
    public Process launchRelease146(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease146(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease146(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease146(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease146(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_146_JAR, "release146", "Release 1.4.6",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.4.7 ---
    public Process launchRelease147(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease147(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease147(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease147(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease147(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_147_JAR, "release147", "Release 1.4.7",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.5.1 ---
    public Process launchRelease151(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease151(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease151(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease151(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease151(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_151_JAR, "release151", "Release 1.5.1",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.5.2 ---
    public Process launchRelease152(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease152(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease152(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease152(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease152(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchPreNettyRelease(RELEASE_152_JAR, "release152", "Release 1.5.2",
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.6.1 ---
    // 1.6.x introduced net.minecraft.client.main.Main with CLI args (same as 1.7.2+).
    // Needs full library deps from Mojang CDN, not the simple pre-Netty classpath.
    public Process launchRelease161(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease161(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease161(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease161(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease161(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchNettyRelease("1.6.1", "release161", "Release 1.6.1", JAVA8_PATH,
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.6.2 ---
    public Process launchRelease162(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease162(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease162(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease162(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease162(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchNettyRelease("1.6.2", "release162", "Release 1.6.2", JAVA8_PATH,
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // --- Release 1.6.4 ---
    public Process launchRelease164(String agentJarPath, int serverPort,
            File statusDir, String display) throws IOException, InterruptedException {
        return launchRelease164(agentJarPath, serverPort, statusDir, display, "world_loaded");
    }
    public Process launchRelease164(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario) throws IOException, InterruptedException {
        return launchRelease164(agentJarPath, serverPort, statusDir, display, scenario, null, null, null);
    }
    public Process launchRelease164(String agentJarPath, int serverPort,
            File statusDir, String display, String scenario,
            String username, String role, File syncDir) throws IOException, InterruptedException {
        return launchNettyRelease("1.6.4", "release164", "Release 1.6.4", JAVA8_PATH,
                agentJarPath, serverPort, statusDir, display, scenario, username, role, syncDir);
    }

    // =====================================================================
    // Netty Release launchers (1.7.2-1.18.2)
    // Use version manifest to resolve library dependencies dynamically.
    // Entry point: net.minecraft.client.main.Main with --server/--port CLI args.
    // =====================================================================

    /**
     * Extracts assets/ from the client JAR into a cached directory, then symlinks
     * to it from assetsDir. 1.14+ DefaultClientResourcePack reads assets from the
     * file system path rather than ClassLoader resources, so the assets must be
     * available as files. The extraction is cached per version in LIBS_DIR.
     */
    private void extractClientAssets(String versionId, File assetsDir) throws IOException {
        File versionLibsDir = new File(LIBS_DIR, "netty-" + versionId);
        File cachedAssets = new File(versionLibsDir, "extracted-assets");
        File clientJar = new File(versionLibsDir, "client.jar");

        // Only extract once per version
        File marker = new File(cachedAssets, ".extracted");
        if (!marker.exists() && clientJar.exists()) {
            cachedAssets.mkdirs();
            try (ZipFile zip = new ZipFile(clientJar)) {
                Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    // Only extract assets/ and pack.mcmeta/pack.png
                    if (!name.startsWith("assets/") && !name.equals("pack.mcmeta")
                            && !name.equals("pack.png")) {
                        continue;
                    }
                    File outFile = new File(cachedAssets, name);
                    if (entry.isDirectory()) {
                        outFile.mkdirs();
                        continue;
                    }
                    outFile.getParentFile().mkdirs();
                    try (InputStream in = zip.getInputStream(entry);
                         FileOutputStream out = new FileOutputStream(outFile)) {
                        byte[] buf = new byte[8192];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }
                    }
                }
            }
            marker.createNewFile();
        }

        // Point assetsDir at the cached extraction via symlink, or copy the
        // minecraft assets subdirectory. The client reads assets from assetsDir.
        // The DefaultClientResourcePack (1.14+) resolves assets at
        // {assetsDir}/minecraft/... (relative to assetsDir path, not under assets/).
        // Actually, the --assetsDir is the parent: client expects
        // {assetsDir}/indexes/{assetIndex}.json for the index, and the built-in
        // pack reads from the JAR classpath. We need the assets in the game dir.
        if (cachedAssets.isDirectory()) {
            File targetMinecraft = new File(assetsDir, "minecraft");
            File srcMinecraft = new File(cachedAssets, "assets/minecraft").getAbsoluteFile();
            if (!targetMinecraft.exists() && srcMinecraft.isDirectory()) {
                java.nio.file.Files.createSymbolicLink(
                        targetMinecraft.toPath(), srcMinecraft.toPath());
            }
        }
    }

    /**
     * Resolves a Minecraft version JSON from the Mojang CDN version manifest.
     * Caches the version JSON in LIBS_DIR/{versionId}.json.
     * Returns the path to the cached version JSON file.
     */
    private File resolveVersionJson(String versionId) throws IOException, InterruptedException {
        File libsDir = new File(LIBS_DIR);
        libsDir.mkdirs();
        File versionJson = new File(libsDir, versionId + ".json");
        if (versionJson.exists() && versionJson.length() > 0) return versionJson;

        // Download manifest and find version URL
        File manifestFile = new File(libsDir, "version_manifest_v2.json");
        downloadIfMissing(VERSION_MANIFEST_URL, manifestFile);

        // Parse manifest to find the version entry URL (simple string search)
        String manifest = new String(java.nio.file.Files.readAllBytes(manifestFile.toPath()));
        String searchKey = "\"id\": \"" + versionId + "\"";
        int idx = manifest.indexOf(searchKey);
        if (idx < 0) {
            // Try without space after colon
            searchKey = "\"id\":\"" + versionId + "\"";
            idx = manifest.indexOf(searchKey);
        }
        if (idx < 0) throw new IOException("Version " + versionId + " not found in manifest");

        // Find the "url" field near this entry
        int urlIdx = manifest.indexOf("\"url\"", idx);
        if (urlIdx < 0 || urlIdx - idx > 500) {
            // Search backwards
            urlIdx = manifest.lastIndexOf("\"url\"", idx);
        }
        int colonIdx = manifest.indexOf(':', urlIdx);
        int quoteStart = manifest.indexOf('"', colonIdx + 1);
        int quoteEnd = manifest.indexOf('"', quoteStart + 1);
        String versionUrl = manifest.substring(quoteStart + 1, quoteEnd);

        downloadIfMissing(versionUrl, versionJson);
        return versionJson;
    }

    /**
     * Downloads the client JAR and all required library JARs for a Netty release version.
     * Libraries are cached in LIBS_DIR/netty-{versionId}/.
     * Returns the complete classpath string (colon-separated).
     */
    private String ensureVersionLibs(String versionId) throws IOException, InterruptedException {
        File versionJson = resolveVersionJson(versionId);
        String json = new String(java.nio.file.Files.readAllBytes(versionJson.toPath()));

        File versionLibsDir = new File(LIBS_DIR, "netty-" + versionId);
        versionLibsDir.mkdirs();

        // Download client JAR
        String clientUrl = extractJsonStringValue(json, "downloads", "client", "url");
        File clientJar = new File(versionLibsDir, "client.jar");
        downloadIfMissing(clientUrl, clientJar);

        // Parse library entries and download each
        List<String> classpathEntries = new ArrayList<>();
        classpathEntries.add(clientJar.getAbsolutePath());

        // Simple JSON parsing for libraries array
        int libsStart = json.indexOf("\"libraries\"");
        if (libsStart >= 0) {
            int arrStart = json.indexOf('[', libsStart);
            int arrEnd = findMatchingBracket(json, arrStart);
            String libsJson = json.substring(arrStart, arrEnd + 1);

            // Find each library artifact
            int searchFrom = 0;
            while (true) {
                int artifactIdx = libsJson.indexOf("\"artifact\"", searchFrom);
                if (artifactIdx < 0) break;

                // Check for rules that might exclude this library on linux
                int prevLibStart = libsJson.lastIndexOf('{', artifactIdx);
                String libEntry = libsJson.substring(prevLibStart, artifactIdx);
                if (libEntry.contains("\"rules\"") && !isAllowedOnLinux(libEntry, libsJson, artifactIdx)) {
                    searchFrom = artifactIdx + 1;
                    continue;
                }

                int urlStart = libsJson.indexOf("\"url\"", artifactIdx);
                if (urlStart < 0 || urlStart - artifactIdx > 500) {
                    searchFrom = artifactIdx + 1;
                    continue;
                }
                int pathStart = libsJson.indexOf("\"path\"", artifactIdx);
                if (pathStart < 0 || pathStart - artifactIdx > 500) {
                    searchFrom = artifactIdx + 1;
                    continue;
                }

                String libUrl = extractSimpleValue(libsJson, urlStart);
                String libPath = extractSimpleValue(libsJson, pathStart);

                if (libUrl != null && libPath != null && !libUrl.isEmpty()) {
                    // Use just the filename from the path
                    String fileName = libPath.contains("/")
                            ? libPath.substring(libPath.lastIndexOf('/') + 1) : libPath;
                    File libFile = new File(versionLibsDir, fileName);
                    downloadIfMissing(libUrl, libFile);
                    classpathEntries.add(libFile.getAbsolutePath());
                }

                searchFrom = artifactIdx + 1;
            }

            // Also download native classifier JARs (needed for LWJGL 3 in 1.13+).
            // LWJGL 3 auto-extracts .so files from native JARs on the classpath.
            searchFrom = 0;
            while (true) {
                int nativeIdx = libsJson.indexOf("\"natives-linux\"", searchFrom);
                if (nativeIdx < 0) break;

                int urlStart = libsJson.indexOf("\"url\"", nativeIdx);
                if (urlStart < 0 || urlStart - nativeIdx > 500) {
                    searchFrom = nativeIdx + 1;
                    continue;
                }
                int pathStart = libsJson.indexOf("\"path\"", nativeIdx);
                if (pathStart < 0 || pathStart - nativeIdx > 500) {
                    searchFrom = nativeIdx + 1;
                    continue;
                }

                String nativeUrl = extractSimpleValue(libsJson, urlStart);
                String nativePath = extractSimpleValue(libsJson, pathStart);

                if (nativeUrl != null && nativePath != null && !nativeUrl.isEmpty()) {
                    String fileName = nativePath.contains("/")
                            ? nativePath.substring(nativePath.lastIndexOf('/') + 1) : nativePath;
                    File nativeFile = new File(versionLibsDir, fileName);
                    downloadIfMissing(nativeUrl, nativeFile);
                    classpathEntries.add(nativeFile.getAbsolutePath());
                }

                searchFrom = nativeIdx + 1;
            }
        }

        StringBuilder cp = new StringBuilder();
        for (int i = 0; i < classpathEntries.size(); i++) {
            if (i > 0) cp.append(':');
            cp.append(classpathEntries.get(i));
        }
        return cp.toString();
    }

    /**
     * Downgrade LWJGL 3.2.2 to 3.2.1 on the classpath. LWJGL 3.2.2 native
     * libraries crash with SIGSEGV on WSL2 due to corrupt JNI function pointers
     * in liblwjgl.so. The 1.15+ version JSONs ship both 3.2.1 Java JARs and
     * 3.2.2 Java+native JARs. We remove all 3.2.2 entries and copy matching
     * 3.2.1 native JARs from the 1.14 version directory (which only has 3.2.1).
     */
    private String downgradeLwjgl322(String classpath, String versionId) throws IOException, InterruptedException {
        if (!classpath.contains("3.2.2")) {
            return classpath; // No 3.2.2 on classpath, nothing to do
        }

        // Source directory for 3.2.1 native JARs (1.14 only ships 3.2.1)
        File sourceDir = new File(LIBS_DIR, "netty-1.14");
        File targetDir = new File(LIBS_DIR, "netty-" + versionId);
        if (!sourceDir.isDirectory()) {
            // Need to download 1.14 libs first to get the 3.2.1 native JARs
            ensureVersionLibs("1.14");
        }

        // Copy 3.2.1 JARs (both native and Java) from 1.14 to the target version dir
        String[] jars321 = sourceDir.list(
                (dir, name) -> name.contains("3.2.1") && name.endsWith(".jar"));
        List<String> addedJars = new ArrayList<>();
        if (jars321 != null) {
            for (String jar : jars321) {
                File src = new File(sourceDir, jar);
                File dst = new File(targetDir, jar);
                if (!dst.exists()) {
                    java.nio.file.Files.copy(src.toPath(), dst.toPath());
                }
                addedJars.add(dst.getAbsolutePath());
            }
        }

        // Rebuild classpath: remove all 3.2.2 JARs, add 3.2.1 JARs
        StringBuilder newCp = new StringBuilder();
        for (String entry : classpath.split(":")) {
            String name = entry.substring(entry.lastIndexOf('/') + 1);
            if (name.contains("3.2.2")) {
                continue; // Skip all 3.2.2 JARs
            }
            if (newCp.length() > 0) newCp.append(':');
            newCp.append(entry);
        }
        for (String jar : addedJars) {
            newCp.append(':').append(jar);
        }
        return newCp.toString();
    }

    /**
     * Generic launcher for Netty release clients (1.7.2+).
     * Uses net.minecraft.client.main.Main with --server/--port CLI args.
     */
    private Process launchNettyRelease(String versionId, String agentVersionId,
            String displayName, String javaPath, String agentJarPath,
            int serverPort, File statusDir, String display, String scenario,
            String username, String role,
            File syncDir) throws IOException, InterruptedException {
        String classpath = ensureVersionLibs(versionId);

        // LWJGL 3.2.2 crashes with SIGSEGV on WSL2 (corrupt JNI function pointer
        // in liblwjgl.so). Downgrade to 3.2.1 by removing 3.2.2 JARs from the
        // classpath and using the 3.2.1 native JARs from the 1.14 version directory.
        // The 1.15+ version JSONs already include 3.2.1 Java JARs alongside 3.2.2;
        // we just need to add the matching 3.2.1 native JARs.
        classpath = downgradeLwjgl322(classpath, versionId);

        // Ensure LWJGL natives are available
        ensureLibs();

        String nativesPath = new File(NATIVES_DIR).getAbsolutePath();

        String agentArgs = "version=" + agentVersionId
                + ",creative=true"
                + ",serverPort=" + serverPort
                + ",statusDir=" + statusDir.getAbsolutePath()
                + ",scenario=" + scenario;
        if (username != null) agentArgs += ",username=" + username;
        if (role != null) agentArgs += ",role=" + role;
        if (syncDir != null) agentArgs += ",syncDir=" + syncDir.getAbsolutePath();

        // Create a temp game directory for this client
        File gameDir = java.nio.file.Files.createTempDirectory("e2e-gamedir-" + versionId + "-").toFile();
        gameDir.deleteOnExit();
        File assetsDir = new File(gameDir, "assets");
        assetsDir.mkdirs();

        // For 1.14+ clients, extract assets from the client JAR into assetsDir.
        // The 1.14+ resource pack system doesn't fall back to ClassLoader resources
        // the same way 1.13 does. Without extracting, the client crashes with NPE
        // during block tessellation because models/textures can't be resolved.
        extractClientAssets(versionId, assetsDir);

        // Pre-create options.txt to disable ambient occlusion and set safe defaults.
        // In 1.14+, the BlockModelRenderer checks ebs.a() (isAmbientOcclusion) on the
        // BakedModel at SourceFile:42 before the try/catch. If the model lookup returns
        // null (missing model resolution failure), this NPEs. Disabling AO avoids this
        // code path. Also reduces render distance to minimize chunk load.
        File optionsFile = new File(gameDir, "options.txt");
        try (java.io.PrintWriter pw = new java.io.PrintWriter(optionsFile)) {
            pw.println("ao:0");
            pw.println("renderDistance:2");
            pw.println("lang:en_us");
            pw.println("fov:0.0");
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(javaPath);
        cmd.add("-javaagent:" + agentJarPath + "=" + agentArgs);
        cmd.add("-Djava.library.path=" + nativesPath);
        // LWJGL 3.2.1's bundled jemalloc (Dec 2018) crashes with SIGSEGV on
        // Java 17+ (corrupt function pointer at 0xc00000 during je_malloc).
        // Bypass jemalloc entirely by using the system malloc allocator.
        cmd.add("-Dorg.lwjgl.system.allocator=system");
        cmd.add("-Dhttp.proxyHost=127.0.0.1");
        cmd.add("-Dhttp.proxyPort=65535");
        // MC 1.16.2+ DataFixerUpper OOMs at 256m; 512m gives sufficient headroom.
        cmd.add("-Xmx512m");
        cmd.add("-cp");
        cmd.add(classpath);
        cmd.add("net.minecraft.client.main.Main");
        cmd.add("--username");
        cmd.add(username != null ? username : "E2EBot");
        cmd.add("--version");
        cmd.add(versionId);
        cmd.add("--gameDir");
        cmd.add(gameDir.getAbsolutePath());
        cmd.add("--assetsDir");
        cmd.add(assetsDir.getAbsolutePath());
        // Do NOT pass --assetIndex: without it, 1.14+ clients use the legacy
        // resource pack (eao) which resolves assets as files at
        // {assetsDir}/minecraft/... matching our symlink to extracted JAR assets.
        // With --assetIndex, it uses hash-based object storage which requires
        // downloading ~226MB of assets per version.
        cmd.add("--uuid");
        cmd.add("0");
        cmd.add("--accessToken");
        cmd.add("0");
        cmd.add("--userProperties");
        cmd.add("{}");
        cmd.add("--server");
        cmd.add("localhost");
        cmd.add("--port");
        cmd.add(String.valueOf(serverPort));

        System.out.println("[E2E] Launching " + displayName + " client: " + String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Map<String, String> env = pb.environment();
        env.put("DISPLAY", display);
        env.put("LIBGL_ALWAYS_SOFTWARE", "1");
        // Force GLFW to use X11 backend (Xvfb) instead of Wayland.
        // GLFW 3.3+ (LWJGL 3.2.1, used by MC 1.15+) probes for Wayland first;
        // if it finds a wayland-0 socket in XDG_RUNTIME_DIR it tries Wayland,
        // which crashes under Xvfb.  Override XDG_RUNTIME_DIR to a clean dir.
        File cleanRuntime = new File(statusDir, "xdg-runtime");
        cleanRuntime.mkdirs();
        env.put("XDG_RUNTIME_DIR", cleanRuntime.getAbsolutePath());
        env.remove("WAYLAND_DISPLAY");
        env.put("XDG_SESSION_TYPE", "x11");
        // MC 1.17+ requests Core Profile; force Mesa to expose high Compat Profile
        // so legacy GL calls don't cause FATAL native aborts in software rendering.
        env.put("MESA_GL_VERSION_OVERRIDE", "4.5COMPAT");
        pb.redirectErrorStream(true);
        // Redirect to file for diagnostic capture in addition to inherited IO
        File clientLog = new File(gameDir, "client-output.log");
        pb.redirectOutput(clientLog);

        clientProcess = pb.start();
        return clientProcess;
    }

    // --- Netty LWJGL2 Release versions (1.7.2-1.12.2) ---

    public Process launchRelease172(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease172(a,p,s,d,"world_loaded"); }
    public Process launchRelease172(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease172(a,p,s,d,sc,null,null,null); }
    public Process launchRelease172(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.7.2", "release172", "Release 1.7.2", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease173(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease173(a,p,s,d,"world_loaded"); }
    public Process launchRelease173(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease173(a,p,s,d,sc,null,null,null); }
    public Process launchRelease173(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.7.3", "release173", "Release 1.7.3", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease174(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease174(a,p,s,d,"world_loaded"); }
    public Process launchRelease174(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease174(a,p,s,d,sc,null,null,null); }
    public Process launchRelease174(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.7.4", "release174", "Release 1.7.4", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease175(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease175(a,p,s,d,"world_loaded"); }
    public Process launchRelease175(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease175(a,p,s,d,sc,null,null,null); }
    public Process launchRelease175(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.7.5", "release175", "Release 1.7.5", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease176(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease176(a,p,s,d,"world_loaded"); }
    public Process launchRelease176(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease176(a,p,s,d,sc,null,null,null); }
    public Process launchRelease176(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.7.6", "release176", "Release 1.7.6", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease177(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease177(a,p,s,d,"world_loaded"); }
    public Process launchRelease177(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease177(a,p,s,d,sc,null,null,null); }
    public Process launchRelease177(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.7.7", "release177", "Release 1.7.7", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease178(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease178(a,p,s,d,"world_loaded"); }
    public Process launchRelease178(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease178(a,p,s,d,sc,null,null,null); }
    public Process launchRelease178(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.7.8", "release178", "Release 1.7.8", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease179(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease179(a,p,s,d,"world_loaded"); }
    public Process launchRelease179(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease179(a,p,s,d,sc,null,null,null); }
    public Process launchRelease179(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.7.9", "release179", "Release 1.7.9", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1710(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1710(a,p,s,d,"world_loaded"); }
    public Process launchRelease1710(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1710(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1710(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.7.10", "release1710", "Release 1.7.10", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease18(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease18(a,p,s,d,"world_loaded"); }
    public Process launchRelease18(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease18(a,p,s,d,sc,null,null,null); }
    public Process launchRelease18(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.8", "release18", "Release 1.8", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease181(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease181(a,p,s,d,"world_loaded"); }
    public Process launchRelease181(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease181(a,p,s,d,sc,null,null,null); }
    public Process launchRelease181(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.8.1", "release181", "Release 1.8.1", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease182(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease182(a,p,s,d,"world_loaded"); }
    public Process launchRelease182(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease182(a,p,s,d,sc,null,null,null); }
    public Process launchRelease182(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.8.2", "release182", "Release 1.8.2", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease183(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease183(a,p,s,d,"world_loaded"); }
    public Process launchRelease183(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease183(a,p,s,d,sc,null,null,null); }
    public Process launchRelease183(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.8.3", "release183", "Release 1.8.3", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease184(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease184(a,p,s,d,"world_loaded"); }
    public Process launchRelease184(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease184(a,p,s,d,sc,null,null,null); }
    public Process launchRelease184(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.8.4", "release184", "Release 1.8.4", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease185(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease185(a,p,s,d,"world_loaded"); }
    public Process launchRelease185(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease185(a,p,s,d,sc,null,null,null); }
    public Process launchRelease185(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.8.5", "release185", "Release 1.8.5", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease186(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease186(a,p,s,d,"world_loaded"); }
    public Process launchRelease186(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease186(a,p,s,d,sc,null,null,null); }
    public Process launchRelease186(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.8.6", "release186", "Release 1.8.6", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease187(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease187(a,p,s,d,"world_loaded"); }
    public Process launchRelease187(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease187(a,p,s,d,sc,null,null,null); }
    public Process launchRelease187(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.8.7", "release187", "Release 1.8.7", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease188(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease188(a,p,s,d,"world_loaded"); }
    public Process launchRelease188(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease188(a,p,s,d,sc,null,null,null); }
    public Process launchRelease188(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.8.8", "release188", "Release 1.8.8", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease189(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease189(a,p,s,d,"world_loaded"); }
    public Process launchRelease189(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease189(a,p,s,d,sc,null,null,null); }
    public Process launchRelease189(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.8.9", "release189", "Release 1.8.9", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease19(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease19(a,p,s,d,"world_loaded"); }
    public Process launchRelease19(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease19(a,p,s,d,sc,null,null,null); }
    public Process launchRelease19(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.9", "release19", "Release 1.9", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease191(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease191(a,p,s,d,"world_loaded"); }
    public Process launchRelease191(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease191(a,p,s,d,sc,null,null,null); }
    public Process launchRelease191(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.9.1", "release191", "Release 1.9.1", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease192(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease192(a,p,s,d,"world_loaded"); }
    public Process launchRelease192(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease192(a,p,s,d,sc,null,null,null); }
    public Process launchRelease192(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.9.2", "release192", "Release 1.9.2", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease193(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease193(a,p,s,d,"world_loaded"); }
    public Process launchRelease193(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease193(a,p,s,d,sc,null,null,null); }
    public Process launchRelease193(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.9.3", "release193", "Release 1.9.3", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease194(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease194(a,p,s,d,"world_loaded"); }
    public Process launchRelease194(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease194(a,p,s,d,sc,null,null,null); }
    public Process launchRelease194(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.9.4", "release194", "Release 1.9.4", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease110(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease110(a,p,s,d,"world_loaded"); }
    public Process launchRelease110(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease110(a,p,s,d,sc,null,null,null); }
    public Process launchRelease110(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.10", "release110", "Release 1.10", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1101(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1101(a,p,s,d,"world_loaded"); }
    public Process launchRelease1101(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1101(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1101(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.10.1", "release1101", "Release 1.10.1", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1102(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1102(a,p,s,d,"world_loaded"); }
    public Process launchRelease1102(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1102(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1102(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.10.2", "release1102", "Release 1.10.2", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease111(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease111(a,p,s,d,"world_loaded"); }
    public Process launchRelease111(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease111(a,p,s,d,sc,null,null,null); }
    public Process launchRelease111(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.11", "release111", "Release 1.11", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1111(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1111(a,p,s,d,"world_loaded"); }
    public Process launchRelease1111(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1111(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1111(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.11.1", "release1111", "Release 1.11.1", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1112(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1112(a,p,s,d,"world_loaded"); }
    public Process launchRelease1112(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1112(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1112(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.11.2", "release1112", "Release 1.11.2", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease112(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease112(a,p,s,d,"world_loaded"); }
    public Process launchRelease112(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease112(a,p,s,d,sc,null,null,null); }
    public Process launchRelease112(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.12", "release112", "Release 1.12", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1121(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1121(a,p,s,d,"world_loaded"); }
    public Process launchRelease1121(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1121(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1121(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.12.1", "release1121", "Release 1.12.1", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1122(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1122(a,p,s,d,"world_loaded"); }
    public Process launchRelease1122(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1122(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1122(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.12.2", "release1122", "Release 1.12.2", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    // --- Netty LWJGL3 Release versions (1.13-1.18.2) ---

    public Process launchRelease113(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease113(a,p,s,d,"world_loaded"); }
    public Process launchRelease113(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease113(a,p,s,d,sc,null,null,null); }
    public Process launchRelease113(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.13", "release113", "Release 1.13", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1131(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1131(a,p,s,d,"world_loaded"); }
    public Process launchRelease1131(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1131(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1131(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.13.1", "release1131", "Release 1.13.1", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1132(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1132(a,p,s,d,"world_loaded"); }
    public Process launchRelease1132(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1132(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1132(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.13.2", "release1132", "Release 1.13.2", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease114(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease114(a,p,s,d,"world_loaded"); }
    public Process launchRelease114(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease114(a,p,s,d,sc,null,null,null); }
    public Process launchRelease114(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.14", "release114", "Release 1.14", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1141(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1141(a,p,s,d,"world_loaded"); }
    public Process launchRelease1141(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1141(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1141(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.14.1", "release1141", "Release 1.14.1", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1142(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1142(a,p,s,d,"world_loaded"); }
    public Process launchRelease1142(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1142(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1142(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.14.2", "release1142", "Release 1.14.2", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1143(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1143(a,p,s,d,"world_loaded"); }
    public Process launchRelease1143(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1143(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1143(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.14.3", "release1143", "Release 1.14.3", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1144(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1144(a,p,s,d,"world_loaded"); }
    public Process launchRelease1144(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1144(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1144(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.14.4", "release1144", "Release 1.14.4", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease115(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease115(a,p,s,d,"world_loaded"); }
    public Process launchRelease115(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease115(a,p,s,d,sc,null,null,null); }
    public Process launchRelease115(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.15", "release115", "Release 1.15", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1151(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1151(a,p,s,d,"world_loaded"); }
    public Process launchRelease1151(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1151(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1151(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.15.1", "release1151", "Release 1.15.1", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1152(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1152(a,p,s,d,"world_loaded"); }
    public Process launchRelease1152(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1152(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1152(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.15.2", "release1152", "Release 1.15.2", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease116(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease116(a,p,s,d,"world_loaded"); }
    public Process launchRelease116(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease116(a,p,s,d,sc,null,null,null); }
    public Process launchRelease116(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.16", "release116", "Release 1.16", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1161(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1161(a,p,s,d,"world_loaded"); }
    public Process launchRelease1161(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1161(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1161(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.16.1", "release1161", "Release 1.16.1", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1162(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1162(a,p,s,d,"world_loaded"); }
    public Process launchRelease1162(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1162(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1162(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.16.2", "release1162", "Release 1.16.2", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1163(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1163(a,p,s,d,"world_loaded"); }
    public Process launchRelease1163(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1163(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1163(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.16.3", "release1163", "Release 1.16.3", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1164(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1164(a,p,s,d,"world_loaded"); }
    public Process launchRelease1164(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1164(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1164(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.16.4", "release1164", "Release 1.16.4", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1165(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1165(a,p,s,d,"world_loaded"); }
    public Process launchRelease1165(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1165(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1165(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.16.5", "release1165", "Release 1.16.5", JAVA8_PATH, a, p, s, d, sc, u, r, sy);
    }

    // 1.17+ requires Java 16
    public Process launchRelease117(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease117(a,p,s,d,"world_loaded"); }
    public Process launchRelease117(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease117(a,p,s,d,sc,null,null,null); }
    public Process launchRelease117(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.17", "release117", "Release 1.17", JAVA16_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1171(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1171(a,p,s,d,"world_loaded"); }
    public Process launchRelease1171(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1171(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1171(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.17.1", "release1171", "Release 1.17.1", JAVA16_PATH, a, p, s, d, sc, u, r, sy);
    }

    // 1.18+ requires Java 17
    public Process launchRelease118(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease118(a,p,s,d,"world_loaded"); }
    public Process launchRelease118(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease118(a,p,s,d,sc,null,null,null); }
    public Process launchRelease118(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.18", "release118", "Release 1.18", JAVA17_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1181(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1181(a,p,s,d,"world_loaded"); }
    public Process launchRelease1181(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1181(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1181(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.18.1", "release1181", "Release 1.18.1", JAVA17_PATH, a, p, s, d, sc, u, r, sy);
    }

    public Process launchRelease1182(String a, int p, File s, String d) throws IOException, InterruptedException { return launchRelease1182(a,p,s,d,"world_loaded"); }
    public Process launchRelease1182(String a, int p, File s, String d, String sc) throws IOException, InterruptedException { return launchRelease1182(a,p,s,d,sc,null,null,null); }
    public Process launchRelease1182(String a, int p, File s, String d, String sc, String u, String r, File sy) throws IOException, InterruptedException {
        return launchNettyRelease("1.18.2", "release1182", "Release 1.18.2", JAVA17_PATH, a, p, s, d, sc, u, r, sy);
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
            downloadIfMissing(ALPHA_1015_JAR_URL, new File(libsDir, ALPHA_1015_JAR));
            downloadIfMissing(ALPHA_1016_JAR_URL, new File(libsDir, ALPHA_1016_JAR));
            downloadIfMissing(ALPHA_1017_JAR_URL, new File(libsDir, ALPHA_1017_JAR));
            downloadIfMissing(ALPHA_110_JAR_URL, new File(libsDir, ALPHA_110_JAR));
            downloadIfMissing(ALPHA_120_JAR_URL, new File(libsDir, ALPHA_120_JAR));
            downloadIfMissing(ALPHA_122_JAR_URL, new File(libsDir, ALPHA_122_JAR));
            downloadIfMissing(ALPHA_123_JAR_URL, new File(libsDir, ALPHA_123_JAR));
            downloadIfMissing(ALPHA_126_JAR_URL, new File(libsDir, ALPHA_126_JAR));
            downloadIfMissing(BETA_10_JAR_URL, new File(libsDir, BETA_10_JAR));
            downloadIfMissing(BETA_122_JAR_URL, new File(libsDir, BETA_122_JAR));
            downloadIfMissing(BETA_131_JAR_URL, new File(libsDir, BETA_131_JAR));
            downloadIfMissing(BETA_141_JAR_URL, new File(libsDir, BETA_141_JAR));
            downloadIfMissing(BETA_151_JAR_URL, new File(libsDir, BETA_151_JAR));
            downloadIfMissing(BETA_16_JAR_URL, new File(libsDir, BETA_16_JAR));
            downloadIfMissing(BETA_17_JAR_URL, new File(libsDir, BETA_17_JAR));
            downloadIfMissing(BETA_173_JAR_URL, new File(libsDir, BETA_173_JAR));
            downloadIfMissing(BETA_181_JAR_URL, new File(libsDir, BETA_181_JAR));
            downloadIfMissing(BETA_19PRE5_JAR_URL, new File(libsDir, BETA_19PRE5_JAR));
            // Pre-Netty Release JARs (1.0-1.6.4)
            downloadIfMissing(RELEASE_10_JAR_URL, new File(libsDir, RELEASE_10_JAR));
            downloadIfMissing(RELEASE_11_JAR_URL, new File(libsDir, RELEASE_11_JAR));
            downloadIfMissing(RELEASE_121_JAR_URL, new File(libsDir, RELEASE_121_JAR));
            downloadIfMissing(RELEASE_122_JAR_URL, new File(libsDir, RELEASE_122_JAR));
            downloadIfMissing(RELEASE_123_JAR_URL, new File(libsDir, RELEASE_123_JAR));
            downloadIfMissing(RELEASE_124_JAR_URL, new File(libsDir, RELEASE_124_JAR));
            downloadIfMissing(RELEASE_125_JAR_URL, new File(libsDir, RELEASE_125_JAR));
            downloadIfMissing(RELEASE_131_JAR_URL, new File(libsDir, RELEASE_131_JAR));
            downloadIfMissing(RELEASE_132_JAR_URL, new File(libsDir, RELEASE_132_JAR));
            downloadIfMissing(RELEASE_142_JAR_URL, new File(libsDir, RELEASE_142_JAR));
            downloadIfMissing(RELEASE_144_JAR_URL, new File(libsDir, RELEASE_144_JAR));
            downloadIfMissing(RELEASE_145_JAR_URL, new File(libsDir, RELEASE_145_JAR));
            downloadIfMissing(RELEASE_146_JAR_URL, new File(libsDir, RELEASE_146_JAR));
            downloadIfMissing(RELEASE_147_JAR_URL, new File(libsDir, RELEASE_147_JAR));
            downloadIfMissing(RELEASE_151_JAR_URL, new File(libsDir, RELEASE_151_JAR));
            downloadIfMissing(RELEASE_152_JAR_URL, new File(libsDir, RELEASE_152_JAR));
            downloadIfMissing(RELEASE_161_JAR_URL, new File(libsDir, RELEASE_161_JAR));
            downloadIfMissing(RELEASE_162_JAR_URL, new File(libsDir, RELEASE_162_JAR));
            downloadIfMissing(RELEASE_164_JAR_URL, new File(libsDir, RELEASE_164_JAR));
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

    /**
     * Extract a nested JSON string value. Navigates through nested objects by key names.
     * E.g. extractJsonStringValue(json, "downloads", "client", "url") finds
     * "downloads": { "client": { "url": "..." } }.
     */
    private String extractJsonStringValue(String json, String... keys) {
        int pos = 0;
        for (int i = 0; i < keys.length; i++) {
            int keyIdx = json.indexOf("\"" + keys[i] + "\"", pos);
            if (keyIdx < 0) return null;
            pos = keyIdx + keys[i].length() + 2;
        }
        // pos is after the last key's closing quote; find : then value
        int colonIdx = json.indexOf(':', pos);
        if (colonIdx < 0) return null;
        int quoteStart = json.indexOf('"', colonIdx + 1);
        if (quoteStart < 0) return null;
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteEnd < 0) return null;
        return json.substring(quoteStart + 1, quoteEnd);
    }

    /** Extract a simple string value from a "key": "value" pair starting at the given "key" position. */
    private String extractSimpleValue(String json, int keyStart) {
        int colonIdx = json.indexOf(':', keyStart);
        if (colonIdx < 0) return null;
        int quoteStart = json.indexOf('"', colonIdx + 1);
        if (quoteStart < 0) return null;
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteEnd < 0) return null;
        return json.substring(quoteStart + 1, quoteEnd);
    }

    /** Find the matching closing bracket for an opening bracket at pos. */
    private int findMatchingBracket(String json, int pos) {
        char open = json.charAt(pos);
        char close = open == '[' ? ']' : '}';
        int depth = 1;
        boolean inString = false;
        for (int i = pos + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            } else if (!inString) {
                if (c == open) depth++;
                else if (c == close) {
                    depth--;
                    if (depth == 0) return i;
                }
            }
        }
        return json.length() - 1;
    }

    /** Check if a library entry with rules is allowed on Linux. */
    private boolean isAllowedOnLinux(String entryBefore, String fullJson, int artifactIdx) {
        // Find the enclosing library object
        int libStart = fullJson.lastIndexOf('{', artifactIdx);
        // Look backwards for "rules"
        String libText = fullJson.substring(libStart, artifactIdx);
        if (!libText.contains("\"rules\"")) return true; // No rules = allowed

        // Simple heuristic: if "os" with "name": "linux" is present with action "allow", it's allowed.
        // If "os" with "name": "windows" or "osx" is present with action "allow", it's NOT allowed.
        // If action is "disallow" with linux, NOT allowed.
        if (libText.contains("\"linux\"") && libText.contains("\"allow\"")) return true;
        if (libText.contains("\"windows\"") && !libText.contains("\"linux\"")) return false;
        if (libText.contains("\"osx\"") && !libText.contains("\"linux\"")) return false;
        // Default: allow if no OS restriction found
        return !libText.contains("\"os\"");
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
