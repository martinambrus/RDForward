package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.PacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.PacketEncoder;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.world.AlphaWorldGenerator;
import com.github.martinambrus.rdforward.world.FlatWorldGenerator;
import com.github.martinambrus.rdforward.world.RubyDungWorldGenerator;
import com.github.martinambrus.rdforward.world.WorldGenerator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Collection;

/**
 * The RDForward dedicated server.
 *
 * Manages the world state, accepts client connections via Netty,
 * runs the server tick loop, and dispatches events to mods.
 *
 * The server is authoritative: clients send requests (place block,
 * move), the server validates them, updates world state, and
 * broadcasts the result to all connected clients.
 *
 * Uses the MC Classic protocol (wiki.vg protocol version 7) as the
 * base protocol, with version translation for older (RubyDung) and
 * newer (Alpha) clients.
 */
public class RDServer {

    /** Default world dimensions matching RubyDung's original size. */
    public static final int DEFAULT_WORLD_WIDTH = 256;
    public static final int DEFAULT_WORLD_HEIGHT = 64;
    public static final int DEFAULT_WORLD_DEPTH = 256;

    /** Default world seed (can be overridden via constructor). */
    private static final long DEFAULT_SEED = 0L;

    /** Default directory for Alpha-format chunk storage. */
    private static final String DEFAULT_WORLD_DIR = "world";

    private final int port;
    private final ProtocolVersion protocolVersion;
    private final WorldGenerator worldGenerator;
    private final long worldSeed;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;
    private final ServerTickLoop tickLoop;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private volatile boolean stopped = false;

    public RDServer(int port) {
        this(port, ProtocolVersion.RUBYDUNG, new FlatWorldGenerator(), DEFAULT_SEED);
    }

    public RDServer(int port, ProtocolVersion protocolVersion) {
        this(port, protocolVersion, new FlatWorldGenerator(), DEFAULT_SEED);
    }

    public RDServer(int port, ProtocolVersion protocolVersion, WorldGenerator worldGenerator, long worldSeed) {
        this(port, protocolVersion, worldGenerator, worldSeed,
             DEFAULT_WORLD_WIDTH, DEFAULT_WORLD_HEIGHT, DEFAULT_WORLD_DEPTH);
    }

    public RDServer(int port, ProtocolVersion protocolVersion, WorldGenerator worldGenerator, long worldSeed,
                    int worldWidth, int worldHeight, int worldDepth) {
        this.port = port;
        this.protocolVersion = protocolVersion;
        this.worldGenerator = worldGenerator;
        this.worldSeed = worldSeed;
        this.world = new ServerWorld(worldWidth, worldHeight, worldDepth);
        this.playerManager = new PlayerManager();
        this.chunkManager = new ChunkManager(worldGenerator, worldSeed, new File(DEFAULT_WORLD_DIR));
        this.tickLoop = new ServerTickLoop(playerManager, world, chunkManager);
    }

    /**
     * Start the server: generate world, start tick loop, begin accepting connections.
     */
    public void start() throws InterruptedException {
        if (!world.load()) {
            System.out.println("Generating world (" + DEFAULT_WORLD_WIDTH + "x"
                    + DEFAULT_WORLD_HEIGHT + "x" + DEFAULT_WORLD_DEPTH
                    + ") using " + worldGenerator.getName() + " generator...");
            world.generate(worldGenerator, worldSeed);
            System.out.println("World generated.");
        }

        tickLoop.start();

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("loginTimeout", new ReadTimeoutHandler(
                                ServerConnectionHandler.LOGIN_TIMEOUT_SECONDS));
                        pipeline.addLast("decoder", new PacketDecoder(
                                PacketDirection.CLIENT_TO_SERVER, protocolVersion));
                        pipeline.addLast("encoder", new PacketEncoder());
                        pipeline.addLast("handler", new ServerConnectionHandler(
                                protocolVersion, world, playerManager, chunkManager));
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true);

        serverChannel = bootstrap.bind(port).sync().channel();
        System.out.println("RDForward server started on port " + port
                + " (protocol: " + protocolVersion.getDisplayName()
                + ", version " + protocolVersion.getVersionNumber() + ")");
    }

    /**
     * Stop the server and release all resources.
     */
    public void stop() {
        if (stopped) return;
        stopped = true;

        System.out.println("Stopping server...");
        tickLoop.stop();

        System.out.println("Saving world and player data...");
        world.save();
        world.savePlayers(playerManager.getAllPlayers());
        chunkManager.saveAllDirty();

        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        System.out.println("RDForward server stopped.");
    }

    /**
     * Block the calling thread until the server channel closes.
     */
    public void awaitShutdown() throws InterruptedException {
        if (serverChannel != null) {
            serverChannel.closeFuture().sync();
        }
    }

    public int getPort() { return port; }
    public ProtocolVersion getProtocolVersion() { return protocolVersion; }
    public ServerWorld getWorld() { return world; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public ChunkManager getChunkManager() { return chunkManager; }

    /**
     * Run the interactive console command loop.
     * Reads commands from stdin until "stop" or EOF.
     */
    private void runConsole() {
        System.out.println("Type 'help' for a list of commands.");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                switch (line.toLowerCase()) {
                    case "help":
                        System.out.println("Commands:");
                        System.out.println("  list     - Show connected players");
                        System.out.println("  save     - Save the world to disk");
                        System.out.println("  stop     - Save and stop the server");
                        System.out.println("  help     - Show this help message");
                        break;

                    case "list":
                        Collection<ConnectedPlayer> players = playerManager.getAllPlayers();
                        System.out.println("Players online: " + players.size() + "/" + PlayerManager.MAX_PLAYERS);
                        for (ConnectedPlayer p : players) {
                            System.out.println("  [" + p.getPlayerId() + "] " + p.getUsername()
                                + " (" + (p.getX() / 32.0) + ", " + (p.getY() / 32.0) + ", " + (p.getZ() / 32.0) + ")");
                        }
                        break;

                    case "save":
                        world.save();
                        break;

                    case "stop":
                    case "shutdown":
                        System.out.println("Stopping server...");
                        stop();
                        return;

                    default:
                        System.out.println("Unknown command: " + line + " (type 'help' for commands)");
                        break;
                }
            }
        } catch (Exception e) {
            // stdin closed (e.g. running without a terminal) — just wait for shutdown
        }
    }

    /**
     * Server entry point.
     *
     * Usage: java -cp rd-server.jar com.github.martinambrus.rdforward.server.RDServer [port]
     * Default port: 25565
     *
     * System properties for world configuration:
     *   -Drdforward.world.width=256    World X dimension (default: 256)
     *   -Drdforward.world.height=64    World Y/vertical dimension (default: 64)
     *   -Drdforward.world.depth=256    World Z dimension (default: 256)
     *   -Drdforward.generator=flat     Generator: flat, rubydung, alpha (default: flat)
     *   -Drdforward.seed=12345         World seed (default: 0)
     */
    public static void main(String[] args) {
        int port = 25565;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port: " + args[0]);
                System.exit(1);
            }
        }

        // Parse world dimensions from system properties
        int worldWidth = getIntProperty("rdforward.world.width", DEFAULT_WORLD_WIDTH);
        int worldHeight = getIntProperty("rdforward.world.height", DEFAULT_WORLD_HEIGHT);
        int worldDepth = getIntProperty("rdforward.world.depth", DEFAULT_WORLD_DEPTH);
        long seed = getLongProperty("rdforward.seed", DEFAULT_SEED);

        // Select world generator
        String generatorName = System.getProperty("rdforward.generator", "flat").toLowerCase();
        WorldGenerator generator;
        switch (generatorName) {
            case "rubydung":
            case "classic":
                generator = new RubyDungWorldGenerator();
                break;
            case "alpha":
            case "terrain":
                generator = new AlphaWorldGenerator();
                break;
            case "flat":
            default:
                generator = new FlatWorldGenerator();
                break;
        }

        System.out.println("[RDForward] World config: " + worldWidth + "x" + worldHeight + "x" + worldDepth
            + ", generator=" + generator.getName() + ", seed=" + seed);

        RDServer server = new RDServer(port, ProtocolVersion.RUBYDUNG, generator, seed,
            worldWidth, worldHeight, worldDepth);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "RDForward-Shutdown"));

        try {
            server.start();
            server.runConsole();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static int getIntProperty(String key, int defaultValue) {
        String value = System.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println("Invalid value for " + key + ": " + value + ", using default " + defaultValue);
            return defaultValue;
        }
    }

    private static long getLongProperty(String key, long defaultValue) {
        String value = System.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            System.err.println("Invalid value for " + key + ": " + value + ", using default " + defaultValue);
            return defaultValue;
        }
    }
}
