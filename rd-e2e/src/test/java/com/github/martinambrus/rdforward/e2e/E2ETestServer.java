package com.github.martinambrus.rdforward.e2e;

import com.github.martinambrus.rdforward.server.RDServer;

import java.io.File;

/**
 * RDServer lifecycle for E2E tests.
 * Starts on a random port, cleans stale files before/after.
 * Mirrors rd-bot's TestServer but without the Netty EventLoopGroup
 * (E2E tests launch real client subprocesses, not bot channels).
 */
public class E2ETestServer {

    private static final String[] STALE_FILES = {
            "server-world.dat", "server-players.dat",
            "banned-players.txt", "banned-ips.txt"
    };
    private static final String STALE_DIR = "world";

    private RDServer server;
    private int port;

    public void start() throws InterruptedException {
        deleteStaleFiles();
        server = new RDServer(0);
        server.setBedrockPort(0);
        server.start();
        port = server.getActualPort();
        System.out.println("[E2E] Server started on port " + port);
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
        deleteStaleFiles();
    }

    public int getPort() {
        return port;
    }

    private void deleteStaleFiles() {
        for (String name : STALE_FILES) {
            new File(name).delete();
        }
        deleteDir(new File(STALE_DIR));
    }

    private void deleteDir(File dir) {
        if (!dir.exists())
            return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory())
                    deleteDir(f);
                else
                    f.delete();
            }
        }
        dir.delete();
    }
}
