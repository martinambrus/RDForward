package com.github.martinambrus.rdforward.e2e;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.RDServer;
import com.github.martinambrus.rdforward.world.FlatWorldGenerator;

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
    private final int worldHeight;

    public E2ETestServer() {
        // Alpha world height is 128 blocks (Classic/RubyDung default is 64).
        // Use 128 so placement at Y >= 64 passes ServerWorld.inBounds().
        this(128);
    }

    /**
     * Create an E2E test server with a custom world height.
     * RubyDung clients only support 64-block height worlds.
     */
    public E2ETestServer(int worldHeight) {
        this.worldHeight = worldHeight;
    }

    public void start() throws InterruptedException {
        deleteStaleFiles();
        server = new RDServer(0, ProtocolVersion.RUBYDUNG,
                new FlatWorldGenerator(), 0L, 256, worldHeight, 256);
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
