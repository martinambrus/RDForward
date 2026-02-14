package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.PacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.PacketEncoder;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.server.api.CommandRegistry;
import com.github.martinambrus.rdforward.server.api.PermissionManager;
import com.github.martinambrus.rdforward.server.api.Scheduler;
import com.github.martinambrus.rdforward.world.AlphaWorldGenerator;
import com.github.martinambrus.rdforward.world.FlatWorldGenerator;
import com.github.martinambrus.rdforward.world.RubyDungWorldGenerator;
import com.github.martinambrus.rdforward.world.WorldGenerator;
import com.github.martinambrus.rdforward.server.bedrock.BedrockBlockMapper;
import com.github.martinambrus.rdforward.server.bedrock.BedrockChunkConverter;
import com.github.martinambrus.rdforward.server.bedrock.BedrockLoginHandler;
import com.github.martinambrus.rdforward.server.bedrock.BedrockProtocolConstants;
import com.github.martinambrus.rdforward.server.bedrock.BedrockRegistryData;
import com.github.martinambrus.rdforward.server.event.ServerEvents;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.protocol.bedrock.BedrockPong;
import org.cloudburstmc.protocol.bedrock.BedrockServerSession;
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockServerInitializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
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
    private Channel bedrockChannel;
    private BedrockBlockMapper bedrockBlockMapper;
    private BedrockChunkConverter bedrockChunkConverter;
    private BedrockRegistryData bedrockRegistryData;
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
        this.chunkManager.setServerWorld(world);
        this.tickLoop = new ServerTickLoop(playerManager, world, chunkManager);
    }

    /**
     * Start the server: generate world, start tick loop, begin accepting connections.
     */
    public void start() throws InterruptedException {
        // Initialize mod APIs
        PermissionManager.load();
        Scheduler.init();
        registerBuiltInCommands();

        if (!world.load()) {
            System.out.println("Generating world (" + DEFAULT_WORLD_WIDTH + "x"
                    + DEFAULT_WORLD_HEIGHT + "x" + DEFAULT_WORLD_DEPTH
                    + ") using " + worldGenerator.getName() + " generator...");
            world.generate(worldGenerator, worldSeed);
            System.out.println("World generated.");
        }

        // Migrate invalid block types from old RubyDung worlds
        if (protocolVersion == ProtocolVersion.RUBYDUNG) {
            world.migrateRubyDungBlocks();
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
                        pipeline.addLast("protocolDetect", new ProtocolDetectionHandler(
                                protocolVersion, world, playerManager, chunkManager));
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

        // Start Bedrock Edition server (UDP/RakNet on port 19132)
        startBedrockServer();
    }

    /**
     * Start the Bedrock Edition UDP/RakNet server on port 19132.
     */
    private void startBedrockServer() {
        int bedrockPort = BedrockProtocolConstants.DEFAULT_PORT;

        // Initialize block mapper, chunk converter, and registry data
        bedrockBlockMapper = new BedrockBlockMapper(BedrockProtocolConstants.getVanillaBlockStates());
        bedrockChunkConverter = new BedrockChunkConverter(bedrockBlockMapper);
        bedrockRegistryData = new BedrockRegistryData();

        // Configure BedrockPong for server list advertisement
        final long serverGuid = System.currentTimeMillis();
        final int bPort = bedrockPort;

        // Runnable that rebuilds and updates the pong advertisement (player count etc.)
        Runnable pongUpdater = () -> {
            if (bedrockChannel == null) return;
            BedrockPong p = new BedrockPong()
                    .edition("MCPE")
                    .motd("RDForward Server")
                    .subMotd("RDForward")
                    .playerCount(playerManager.getPlayerCount())
                    .maximumPlayerCount(PlayerManager.MAX_PLAYERS)
                    .gameType("Creative")
                    .protocolVersion(BedrockProtocolConstants.CODEC.getProtocolVersion())
                    .version(BedrockProtocolConstants.CODEC.getMinecraftVersion())
                    .ipv4Port(bPort)
                    .ipv6Port(bPort)
                    .serverId(serverGuid)
                    .nintendoLimited(false);
            bedrockChannel.config().setOption(RakChannelOption.RAK_ADVERTISEMENT, p.toByteBuf());
        };

        BedrockPong pong = new BedrockPong()
                .edition("MCPE")
                .motd("RDForward Server")
                .subMotd("RDForward")
                .playerCount(0)
                .maximumPlayerCount(PlayerManager.MAX_PLAYERS)
                .gameType("Creative")
                .protocolVersion(BedrockProtocolConstants.CODEC.getProtocolVersion())
                .version(BedrockProtocolConstants.CODEC.getMinecraftVersion())
                .ipv4Port(bedrockPort)
                .ipv6Port(bedrockPort)
                .serverId(serverGuid)
                .nintendoLimited(false);

        io.netty.buffer.ByteBuf pongBuf = pong.toByteBuf();

        ServerBootstrap bedrockBootstrap = new ServerBootstrap()
                .channelFactory(RakChannelFactory.server(NioDatagramChannel.class))
                .group(workerGroup)
                .option(RakChannelOption.RAK_HANDLE_PING, false)
                .option(RakChannelOption.RAK_GUID, serverGuid)
                .option(RakChannelOption.RAK_SUPPORTED_PROTOCOLS, new int[]{11})
                .option(RakChannelOption.RAK_ADVERTISEMENT, pongBuf)
                .childHandler(new BedrockServerInitializer() {
                    @Override
                    protected void initSession(BedrockServerSession session) {
                        session.setCodec(BedrockProtocolConstants.CODEC);
                        session.setPacketHandler(new BedrockLoginHandler(
                                session, world, playerManager, chunkManager,
                                bedrockBlockMapper, bedrockChunkConverter,
                                bedrockRegistryData, pongUpdater));
                    }
                });

        try {
            bedrockChannel = bedrockBootstrap.bind(bedrockPort).sync().channel();
            System.out.println("Bedrock server started on port " + bedrockPort
                    + " (protocol: " + BedrockProtocolConstants.CODEC.getMinecraftVersion()
                    + ", version " + BedrockProtocolConstants.CODEC.getProtocolVersion() + ")");

            // Update pong advertisement when ANY player (TCP or Bedrock) joins or leaves.
            // PLAYER_JOIN fires after addPlayer, so the count is already correct.
            ServerEvents.PLAYER_JOIN.register((name, version) -> pongUpdater.run());
            // PLAYER_LEAVE fires before removePlayer, so defer the update to run
            // after the current handler method completes (removePlayer included).
            ServerEvents.PLAYER_LEAVE.register(name ->
                    bedrockChannel.eventLoop().execute(pongUpdater));
        } catch (Exception e) {
            System.err.println("Failed to start Bedrock server on port " + bedrockPort
                    + ": " + e.getMessage());
            System.err.println("Bedrock Edition support disabled. TCP server still running.");
        }
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

        if (bedrockChannel != null) {
            bedrockChannel.close();
        }
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
     * Register built-in server commands with the command registry.
     */
    private void registerBuiltInCommands() {
        CommandRegistry.register("help", "Show available commands", ctx -> {
            ctx.reply("Commands:");
            for (CommandRegistry.RegisteredCommand cmd : CommandRegistry.getCommands().values()) {
                ctx.reply("  " + cmd.name + " - " + cmd.description
                    + (cmd.requiresOp ? " (op)" : ""));
            }
        });

        CommandRegistry.register("list", "Show connected players", ctx -> {
            Collection<ConnectedPlayer> players = playerManager.getAllPlayers();
            ctx.reply("Players online: " + players.size() + "/" + PlayerManager.MAX_PLAYERS);
            for (ConnectedPlayer p : players) {
                ctx.reply("  [" + p.getPlayerId() + "] " + p.getUsername()
                    + " (" + String.format("%.1f, %.1f, %.1f",
                        p.getX() / 32.0, p.getY() / 32.0, p.getZ() / 32.0) + ")");
            }
        });

        CommandRegistry.register("save", "Save the world to disk", ctx -> {
            world.save();
            world.savePlayers(playerManager.getAllPlayers());
            chunkManager.saveAllDirty();
            ctx.reply("World saved.");
        });

        CommandRegistry.registerOp("stop", "Save and stop the server", ctx -> {
            ctx.reply("Stopping server...");
            stop();
        });

        CommandRegistry.registerOp("op", "Grant operator status to a player", ctx -> {
            if (ctx.getArgs().length == 0) {
                ctx.reply("Usage: op <player>");
                return;
            }
            PermissionManager.addOp(ctx.getArgs()[0]);
            ctx.reply("Made " + ctx.getArgs()[0] + " an operator.");
        });

        CommandRegistry.registerOp("deop", "Revoke operator status from a player", ctx -> {
            if (ctx.getArgs().length == 0) {
                ctx.reply("Usage: deop <player>");
                return;
            }
            PermissionManager.removeOp(ctx.getArgs()[0]);
            ctx.reply("Removed " + ctx.getArgs()[0] + " from operators.");
        });
    }

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

                if (!CommandRegistry.dispatch(line, "CONSOLE", true, System.out::println)) {
                    System.out.println("Unknown command: " + line + " (type 'help' for commands)");
                }

                if (stopped) return;
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
