package com.github.martinambrus.rdforward.e2e;

import java.io.IOException;

/**
 * Manages an Xvfb virtual display for headless OpenGL rendering.
 * Starts Xvfb on display :99 with GLX extension for LWJGL 2 compatibility.
 */
public class HeadlessDisplay {

    private final String display;
    private final String[] xvfbCmd;

    private Process xvfbProcess;

    /**
     * Create a display with a unique number derived from the Gradle test fork
     * worker ID. Each parallel fork gets its own display to avoid collisions.
     */
    public static HeadlessDisplay forFork() {
        String workerId = System.getProperty("org.gradle.test.worker");
        int id = (workerId != null) ? Integer.parseInt(workerId) : 1;
        return new HeadlessDisplay(98 + id);
    }

    /**
     * Create a second display for cross-version tests that need two concurrent
     * clients. Offset by 100 from forFork() to avoid collisions.
     */
    public static HeadlessDisplay secondForFork() {
        String workerId = System.getProperty("org.gradle.test.worker");
        int id = (workerId != null) ? Integer.parseInt(workerId) : 1;
        return new HeadlessDisplay(198 + id);
    }

    public HeadlessDisplay() {
        this(99);
    }

    public HeadlessDisplay(int displayNum) {
        this.display = ":" + displayNum;
        this.xvfbCmd = new String[]{
                "Xvfb", display,
                "-screen", "0", "854x480x24",
                "+extension", "GLX",
                "+render",
                "-ac"
        };
    }

    /**
     * Start Xvfb. If the display is already in use (from a previous run or
     * system service), this is a no-op.
     */
    public void start() throws IOException, InterruptedException {
        // Check if Xvfb is already running (with a 1 second timeout to prevent
        // 75s TCP SYN hanging)
        ProcessBuilder check = new ProcessBuilder("xdpyinfo", "-display", display);
        check.redirectErrorStream(true);
        Process checkProc = check.start();
        boolean finished = checkProc.waitFor(1, java.util.concurrent.TimeUnit.SECONDS);
        if (finished && checkProc.exitValue() == 0) {
            System.out.println("[E2E] Xvfb already running on " + display);
            return;
        } else if (!finished) {
            checkProc.destroyForcibly();
        }

        System.out.println("[E2E] Starting Xvfb on " + display);
        ProcessBuilder pb = new ProcessBuilder(xvfbCmd);
        pb.redirectErrorStream(true);
        xvfbProcess = pb.start();

        // Wait for Xvfb to be ready (poll with xdpyinfo)
        for (int i = 0; i < 50; i++) {
            Thread.sleep(100);
            ProcessBuilder poll = new ProcessBuilder("xdpyinfo", "-display", display);
            poll.redirectErrorStream(true);
            Process pollProc = poll.start();
            if (pollProc.waitFor() == 0) {
                System.out.println("[E2E] Xvfb ready on " + display);
                return;
            }
        }
        throw new IOException("Xvfb failed to start within 5 seconds");
    }

    public void stop() {
        if (xvfbProcess != null) {
            xvfbProcess.destroyForcibly();
            System.out.println("[E2E] Xvfb stopped");
        }
    }

    public String getDisplay() {
        return display;
    }
}
