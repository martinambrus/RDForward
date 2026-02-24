package com.github.martinambrus.rdforward.e2e;

import java.io.IOException;

/**
 * Manages an Xvfb virtual display for headless OpenGL rendering.
 * Starts Xvfb on display :99 with GLX extension for LWJGL 2 compatibility.
 */
public class HeadlessDisplay {

    private static final String DISPLAY = ":99";
    private static final String[] XVFB_CMD = {
            "Xvfb", DISPLAY,
            "-screen", "0", "854x480x24",
            "+extension", "GLX",
            "+render",
            "-ac"
    };

    private Process xvfbProcess;

    /**
     * Start Xvfb. If display :99 is already in use (from a previous run or
     * system service), this is a no-op.
     */
    public void start() throws IOException, InterruptedException {
        // Check if Xvfb is already running on :99 (with a 1 second timeout to prevent
        // 75s TCP SYN hanging)
        ProcessBuilder check = new ProcessBuilder("xdpyinfo", "-display", DISPLAY);
        check.redirectErrorStream(true);
        Process checkProc = check.start();
        boolean finished = checkProc.waitFor(1, java.util.concurrent.TimeUnit.SECONDS);
        if (finished && checkProc.exitValue() == 0) {
            System.out.println("[E2E] Xvfb already running on " + DISPLAY);
            return;
        } else if (!finished) {
            checkProc.destroyForcibly();
        }

        System.out.println("[E2E] Starting Xvfb on " + DISPLAY);
        ProcessBuilder pb = new ProcessBuilder(XVFB_CMD);
        pb.redirectErrorStream(true);
        xvfbProcess = pb.start();

        // Wait for Xvfb to be ready (poll with xdpyinfo)
        for (int i = 0; i < 50; i++) {
            Thread.sleep(100);
            ProcessBuilder poll = new ProcessBuilder("xdpyinfo", "-display", DISPLAY);
            poll.redirectErrorStream(true);
            Process pollProc = poll.start();
            if (pollProc.waitFor() == 0) {
                System.out.println("[E2E] Xvfb ready on " + DISPLAY);
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
        return DISPLAY;
    }
}
