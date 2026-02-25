package com.github.martinambrus.rdforward.e2e;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.RDServer;
import com.github.martinambrus.rdforward.world.FlatWorldGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * RDServer lifecycle for E2E tests.
 * Starts on a random port, cleans stale files before/after.
 * Mirrors rd-bot's TestServer but without the Netty EventLoopGroup
 * (E2E tests launch real client subprocesses, not bot channels).
 */
public class E2ETestServer {

    private RDServer server;
    private int port;
    private final int worldHeight;
    private File dataDir;

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
        try {
            dataDir = Files.createTempDirectory("e2e-server-").toFile();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp data directory", e);
        }
        server = new RDServer(0, ProtocolVersion.RUBYDUNG,
                new FlatWorldGenerator(), 0L, 256, worldHeight, 256, dataDir);
        server.setBedrockPort(0);
        server.start();
        port = server.getActualPort();
        System.out.println("[E2E] Server started on port " + port + " (dataDir=" + dataDir + ")");
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
        if (dataDir != null) {
            deleteDir(dataDir);
        }
    }

    public int getPort() {
        return port;
    }

    /**
     * Pre-save a player position so pre-1.2.0 Alpha clients (which get kicked
     * on first connect to warn about JVM flags) are recognized as returning
     * players and allowed through immediately.
     */
    public void preSeedPlayerPosition(String username) {
        if (server != null) {
            server.getWorld().savePlayerPosition(username.trim());
        }
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
