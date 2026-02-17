package com.github.martinambrus.rdforward.bot;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.RDServer;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Test infrastructure that starts an RDServer on a random port
 * and provides utilities for creating bot clients.
 */
public class TestServer {

    private static final String[] STALE_FILES = {
            "server-world.dat", "server-players.dat",
            "banned-players.txt", "banned-ips.txt"
    };
    private static final String STALE_DIR = "world";

    private RDServer server;
    private int port;
    private NioEventLoopGroup botGroup;
    /** Usernames that have completed the pre-1.2.0 warmup (TimSort kick). */
    private final Set<String> warmedUp = Collections.synchronizedSet(new HashSet<>());

    /**
     * Start the server on a random port.
     * Deletes stale world/player files to ensure a clean state.
     */
    public void start() throws InterruptedException {
        deleteStaleFiles();
        server = new RDServer(0);
        server.setBedrockPort(0); // random port for test isolation
        server.start();
        port = server.getActualPort();
        botGroup = new NioEventLoopGroup(2);
    }

    /**
     * Stop the server and release bot client resources.
     * Cleans up world/player files created during tests.
     */
    public void stop() {
        if (botGroup != null) {
            botGroup.shutdownGracefully();
        }
        if (server != null) {
            server.stop();
        }
        deleteStaleFiles();
    }

    private void deleteStaleFiles() {
        for (String name : STALE_FILES) {
            new File(name).delete();
        }
        deleteDir(new File(STALE_DIR));
    }

    private void deleteDir(File dir) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) deleteDir(f);
                else f.delete();
            }
        }
        dir.delete();
    }

    /**
     * Create a connected bot client with the given protocol version and username.
     * For pre-1.2.0 Alpha clients, automatically handles the first-connect rejection
     * (server kicks with TimSort JVM flag warning and saves a default position;
     * second connect succeeds).
     *
     * @param version  the protocol version to use
     * @param username the bot's username
     * @return a connected BotClient with login complete
     */
    public BotClient createBot(ProtocolVersion version, String username) throws Exception {
        warmUpIfNeeded(version, username);
        BotClient bot = new BotClient("localhost", port, version, username, botGroup);
        bot.connectSync(5000);
        return bot;
    }

    /**
     * Pre-1.2.0 Alpha clients are rejected on first connect with a TimSort
     * JVM flag warning. The server saves a default spawn position so subsequent
     * connects succeed. This method performs that warmup connect-and-kick cycle.
     */
    private void warmUpIfNeeded(ProtocolVersion version, String username) {
        if (version.isAtLeast(ProtocolVersion.ALPHA_1_2_0)) return;
        String key = username.trim();
        if (warmedUp.contains(key)) return;
        BotClient warmup = new BotClient("localhost", port, version, username, botGroup);
        try {
            warmup.connectSync(2000);
        } catch (Exception ignored) {
            // Expected: first connect kicked with TimSort JVM flag message
        } finally {
            warmup.disconnect();
        }
        warmedUp.add(key);
    }

    /**
     * Create a connected Bedrock bot client.
     *
     * @param username the bot's username
     * @return a connected BotBedrockClient with login complete
     */
    public BotBedrockClient createBedrockBot(String username) throws Exception {
        int bedrockPort = server.getActualBedrockPort();
        BotBedrockClient bot = new BotBedrockClient("localhost", bedrockPort, username, botGroup);
        bot.connectSync(10000);
        return bot;
    }

    public int getPort() {
        return port;
    }

    public RDServer getServer() {
        return server;
    }

    public NioEventLoopGroup getBotGroup() {
        return botGroup;
    }
}
