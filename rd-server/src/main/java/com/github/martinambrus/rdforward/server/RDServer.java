package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.PacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.PacketEncoder;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.server.api.BanManager;
import com.github.martinambrus.rdforward.server.api.CommandRegistry;
import com.github.martinambrus.rdforward.server.api.PermissionManager;
import com.github.martinambrus.rdforward.server.api.Scheduler;
import com.github.martinambrus.rdforward.server.api.ServerProperties;
import com.github.martinambrus.rdforward.world.FlatWorldGenerator;
import com.github.martinambrus.rdforward.world.WorldGenerator;
import com.github.martinambrus.rdforward.server.bedrock.BedrockBlockMapper;
import com.github.martinambrus.rdforward.server.bedrock.BedrockChunkConverter;
import com.github.martinambrus.rdforward.server.bedrock.BedrockLoginHandler;
import com.github.martinambrus.rdforward.server.bedrock.BedrockProtocolConstants;
import com.github.martinambrus.rdforward.server.bedrock.BedrockRegistryData;
import com.github.martinambrus.rdforward.server.mcpe.BedrockOutboundRedirector;
import com.github.martinambrus.rdforward.server.mcpe.LegacyRakNetServer;
import com.github.martinambrus.rdforward.server.mcpe.MCPEConstants;
import com.github.martinambrus.rdforward.server.mcpe.UdpFrontEndHandler;
import com.github.martinambrus.rdforward.protocol.event.EventResult;
import com.github.martinambrus.rdforward.server.event.ServerEvents;
import io.netty.bootstrap.Bootstrap;
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

    /** Default world dimensions matching RubyDung's original size.
     *  These must stay in sync with ServerProperties.DEFAULTS. */
    public static final int DEFAULT_WORLD_WIDTH = 256;
    public static final int DEFAULT_WORLD_HEIGHT = 64;
    public static final int DEFAULT_WORLD_DEPTH = 256;

    /** Default world seed (can be overridden via constructor). */
    private static final long DEFAULT_SEED = 0L;

    private final int port;
    private int bedrockPort = BedrockProtocolConstants.DEFAULT_PORT; // 19132
    private final ProtocolVersion protocolVersion;
    private final WorldGenerator worldGenerator;
    private final long worldSeed;
    private final File dataDir;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ChunkManager chunkManager;
    private final ServerTickLoop tickLoop;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private Channel bedrockChannel;
    private Channel udpFrontEndChannel;
    private LegacyRakNetServer mcpeServer;
    private UdpFrontEndHandler udpFrontEndHandler;
    private volatile BedrockBlockMapper bedrockBlockMapper;
    private volatile BedrockChunkConverter bedrockChunkConverter;
    private volatile BedrockRegistryData bedrockRegistryData;
    private volatile boolean stopped = false;

    /** Lazy getter for BedrockBlockMapper — loaded on first Bedrock client connection. */
    private BedrockBlockMapper getBedrockBlockMapper() {
        BedrockBlockMapper mapper = bedrockBlockMapper;
        if (mapper == null) {
            synchronized (this) {
                mapper = bedrockBlockMapper;
                if (mapper == null) {
                    mapper = new BedrockBlockMapper(BedrockProtocolConstants.getVanillaBlockStates());
                    bedrockBlockMapper = mapper;
                }
            }
        }
        return mapper;
    }

    /** Lazy getter for BedrockChunkConverter — loaded on first Bedrock client connection. */
    private BedrockChunkConverter getBedrockChunkConverter() {
        BedrockChunkConverter converter = bedrockChunkConverter;
        if (converter == null) {
            synchronized (this) {
                converter = bedrockChunkConverter;
                if (converter == null) {
                    converter = new BedrockChunkConverter(getBedrockBlockMapper());
                    bedrockChunkConverter = converter;
                }
            }
        }
        return converter;
    }

    /** Lazy getter for BedrockRegistryData — loaded on first Bedrock client connection. */
    private BedrockRegistryData getBedrockRegistryData() {
        BedrockRegistryData data = bedrockRegistryData;
        if (data == null) {
            synchronized (this) {
                data = bedrockRegistryData;
                if (data == null) {
                    data = new BedrockRegistryData();
                    bedrockRegistryData = data;
                }
            }
        }
        return data;
    }

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
        this(port, protocolVersion, worldGenerator, worldSeed, worldWidth, worldHeight, worldDepth, null);
    }

    public RDServer(int port, ProtocolVersion protocolVersion, WorldGenerator worldGenerator, long worldSeed,
                    int worldWidth, int worldHeight, int worldDepth, File dataDir) {
        this.port = port;
        this.protocolVersion = protocolVersion;
        this.worldGenerator = worldGenerator;
        this.worldSeed = worldSeed;
        this.dataDir = dataDir;
        this.world = new ServerWorld(worldWidth, worldHeight, worldDepth, dataDir);
        this.playerManager = new PlayerManager();
        String levelName = ServerProperties.getLevelName();
        File worldDir = (dataDir != null) ? new File(dataDir, levelName) : new File(levelName);
        this.chunkManager = new ChunkManager(worldGenerator, worldSeed, worldDir);
        System.out.println("[RDServer] ChunkManager viewDistance="
                + ChunkManager.DEFAULT_VIEW_DISTANCE
                + " (e2e.viewDistance=" + System.getProperty("e2e.viewDistance") + ")");
        this.chunkManager.setServerWorld(world);
        this.tickLoop = new ServerTickLoop(playerManager, world, chunkManager);
    }

    /**
     * Start the server: generate world, start tick loop, begin accepting connections.
     */
    public void start() throws InterruptedException {
        // Initialize mod APIs
        PermissionManager.load(dataDir);
        BanManager.load(dataDir);
        Scheduler.init();
        registerBuiltInCommands();
        registerSpawnProtection();

        if (!world.load()) {
            System.out.println("Generating world (" + world.getWidth() + "x"
                    + world.getHeight() + "x" + world.getDepth()
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

        // Start unified UDP server (legacy MCPE + modern Bedrock on port 19132)
        startUnifiedUdpServer();
    }

    /**
     * Start the unified UDP server that multiplexes legacy MCPE (RakNet v6)
     * and modern Bedrock (RakNet v10/v11) on a single port (19132).
     *
     * Architecture:
     * 1. Front-end NioDatagramChannel on 0.0.0.0:19132 receives all UDP traffic
     * 2. UdpFrontEndHandler detects client type from RakNet protocol version
     * 3. Legacy MCPE packets → LegacyRakNetServer (direct method dispatch)
     * 4. Bedrock packets → injected into CloudburstMC's internal pipeline
     * 5. CloudburstMC's outbound responses → redirected through front-end channel
     */
    private void startUnifiedUdpServer() {
        int udpPort = bedrockPort; // default 19132, overridable via setBedrockPort()

        // Bedrock infrastructure (BedrockBlockMapper, BedrockChunkConverter,
        // BedrockRegistryData) is lazy-loaded on first Bedrock client connection
        // via getBedrockBlockMapper(), getBedrockChunkConverter(), getBedrockRegistryData().

        final long bedrockGuid = System.currentTimeMillis();

        // Pong updater — updates CloudburstMC's advertisement when player count changes
        Runnable bedrockPongUpdater = () -> {
            if (bedrockChannel == null) return;
            BedrockPong p = new BedrockPong()
                    .edition("MCPE")
                    .motd(ServerProperties.getMotd())
                    .subMotd("RDForward")
                    .playerCount(playerManager.getPlayerCount())
                    .maximumPlayerCount(PlayerManager.getMaxPlayers())
                    .gameType(ServerProperties.getGameModeName())
                    .protocolVersion(BedrockProtocolConstants.CODEC.getProtocolVersion())
                    .version(BedrockProtocolConstants.CODEC.getMinecraftVersion())
                    .ipv4Port(udpPort)
                    .ipv6Port(udpPort)
                    .serverId(bedrockGuid)
                    .nintendoLimited(false);
            bedrockChannel.config().setOption(RakChannelOption.RAK_ADVERTISEMENT, p.toByteBuf());
        };

        // --- Initialize Legacy MCPE support ---
        long mcpeGuid = System.currentTimeMillis() + 1; // distinct from Bedrock GUID
        Runnable mcpePongUpdater = () -> {
            // Pong data is regenerated per-ping in LegacyRakNetServer
        };
        mcpeServer = new LegacyRakNetServer(
                mcpeGuid, ServerProperties.getMotd(), world, playerManager, mcpePongUpdater);

        // --- Create front-end handler ---
        udpFrontEndHandler = new UdpFrontEndHandler(mcpeServer);

        // --- Start front-end UDP channel on port 19132 ---
        try {
            Bootstrap frontEndBootstrap = new Bootstrap()
                    .group(workerGroup)
                    .channel(NioDatagramChannel.class)
                    .handler(udpFrontEndHandler);

            udpFrontEndChannel = frontEndBootstrap.bind(udpPort).sync().channel();
        } catch (Exception e) {
            System.err.println("Failed to start UDP front-end on port " + udpPort
                    + ": " + e.getMessage());
            System.err.println("Legacy MCPE and Bedrock support disabled. TCP server still running.");
            return;
        }

        // --- Start CloudburstMC internally on loopback (no external port) ---
        BedrockPong initialPong = new BedrockPong()
                .edition("MCPE")
                .motd(ServerProperties.getMotd())
                .subMotd("RDForward")
                .playerCount(0)
                .maximumPlayerCount(PlayerManager.getMaxPlayers())
                .gameType(ServerProperties.getGameModeName())
                .protocolVersion(BedrockProtocolConstants.CODEC.getProtocolVersion())
                .version(BedrockProtocolConstants.CODEC.getMinecraftVersion())
                .ipv4Port(udpPort)
                .ipv6Port(udpPort)
                .serverId(bedrockGuid)
                .nintendoLimited(false);

        ServerBootstrap bedrockBootstrap = new ServerBootstrap()
                .channelFactory(RakChannelFactory.server(NioDatagramChannel.class))
                .group(workerGroup)
                .option(RakChannelOption.RAK_HANDLE_PING, false)
                .option(RakChannelOption.RAK_GUID, bedrockGuid)
                .option(RakChannelOption.RAK_SUPPORTED_PROTOCOLS, new int[]{11})
                .option(RakChannelOption.RAK_ADVERTISEMENT, initialPong.toByteBuf())
                .childOption(RakChannelOption.RAK_PROTOCOL_VERSION, 11)
                .childHandler(new BedrockServerInitializer() {
                    @Override
                    protected void initSession(BedrockServerSession session) {
                        session.setCodec(BedrockProtocolConstants.CODEC);
                        session.setPacketHandler(new BedrockLoginHandler(
                                session, world, playerManager, chunkManager,
                                getBedrockBlockMapper(), getBedrockChunkConverter(),
                                getBedrockRegistryData(), bedrockPongUpdater));
                    }
                });

        try {
            // Bind to loopback:0 — CloudburstMC doesn't receive traffic directly;
            // all packets are injected via pipeline from the front-end handler.
            bedrockChannel = bedrockBootstrap.bind(new InetSocketAddress("127.0.0.1", 0))
                    .sync().channel();

            // Wire up the front-end → CloudburstMC pipeline injection.
            // bedrockChannel.parent() is the internal NioDatagramChannel (via ProxyChannel).
            Channel bedrockInternalChannel = bedrockChannel.parent();
            udpFrontEndHandler.setBedrockInternalChannel(bedrockInternalChannel);

            // Add outbound redirector so CloudburstMC's responses go through the front-end
            bedrockInternalChannel.pipeline().addFirst("outboundRedirector",
                    new BedrockOutboundRedirector(udpFrontEndChannel));

            // Update pong advertisement on player join/leave
            ServerEvents.PLAYER_JOIN.register((name, version) -> bedrockPongUpdater.run());
            ServerEvents.PLAYER_LEAVE.register(name ->
                    bedrockChannel.eventLoop().execute(bedrockPongUpdater));

            System.out.println("Bedrock server started on port " + udpPort
                    + " (protocol: " + BedrockProtocolConstants.CODEC.getMinecraftVersion()
                    + ", version " + BedrockProtocolConstants.CODEC.getProtocolVersion() + ")");
        } catch (Exception e) {
            System.err.println("Failed to start Bedrock server: " + e.getMessage());
            System.err.println("Bedrock Edition support disabled. Legacy MCPE still active.");
        }

        System.out.println("Legacy MCPE server started on port " + udpPort
                + " (protocol: " + MCPEConstants.MCPE_VERSION_STRING
                + ", version " + MCPEConstants.MCPE_PROTOCOL_VERSION_11
                + "-" + MCPEConstants.MCPE_PROTOCOL_VERSION_MAX + ")");
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

        if (udpFrontEndChannel != null) {
            udpFrontEndChannel.close();
        }
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

        // Clear static event listeners and scheduler state so that
        // subsequent server instances (e.g. in test suites) start clean.
        ServerEvents.clearAll();
        Scheduler.reset();

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
    public int getActualPort() {
        if (serverChannel != null) {
            return ((InetSocketAddress) serverChannel.localAddress()).getPort();
        }
        return port;
    }
    public void setBedrockPort(int bedrockPort) { this.bedrockPort = bedrockPort; }
    public int getActualBedrockPort() {
        if (udpFrontEndChannel != null) {
            return ((InetSocketAddress) udpFrontEndChannel.localAddress()).getPort();
        }
        return bedrockPort;
    }
    public ProtocolVersion getProtocolVersion() { return protocolVersion; }
    public ServerWorld getWorld() { return world; }
    public PlayerManager getPlayerManager() { return playerManager; }
    public ChunkManager getChunkManager() { return chunkManager; }

    /**
     * Register built-in server commands with the command registry.
     */
    private void registerBuiltInCommands() {
        CommandRegistry.register("help", "Show available commands", ctx -> {
            int myLevel = ctx.isConsole() ? PermissionManager.MAX_OP_LEVEL
                    : PermissionManager.getOpLevel(ctx.getSenderName());
            ctx.reply("Commands:");
            java.util.TreeMap<String, CommandRegistry.RegisteredCommand> sorted =
                    new java.util.TreeMap<>(CommandRegistry.getCommands());
            for (CommandRegistry.RegisteredCommand cmd : sorted.values()) {
                if (cmd.requiredOpLevel > 0 && cmd.requiredOpLevel > myLevel) {
                    continue;
                }
                String levelTag = cmd.requiredOpLevel > 0
                        ? " (op level " + cmd.requiredOpLevel + ")" : "";
                ctx.reply("  " + cmd.name + " - " + cmd.description + levelTag);
            }
        });

        CommandRegistry.register("list", "Show connected players", ctx -> {
            Collection<ConnectedPlayer> players = playerManager.getAllPlayers();
            ctx.reply("Players online: " + players.size() + "/" + PlayerManager.getMaxPlayers());
            for (ConnectedPlayer p : players) {
                ctx.reply("  [" + p.getPlayerId() + "] " + p.getUsername()
                    + " (" + String.format("%.1f, %.1f, %.1f",
                        p.getX() / 32.0, p.getY() / 32.0, p.getZ() / 32.0) + ")");
            }
        });

        CommandRegistry.registerOp("save", "Save the world to disk", PermissionManager.OP_ADMIN, ctx -> {
            world.save();
            world.savePlayers(playerManager.getAllPlayers());
            chunkManager.saveAllDirty();
            ctx.reply("World saved.");
        });

        CommandRegistry.registerOp("stop", "Save and stop the server (use: stop confirm)", PermissionManager.OP_ADMIN, ctx -> {
            if (!ctx.isConsole() && (ctx.getArgs().length == 0 || !ctx.getArgs()[0].equalsIgnoreCase("confirm"))) {
                ctx.reply("This will shut down the server. Use /stop confirm to proceed.");
                return;
            }
            System.out.println("[INFO] Server stop initiated by " + ctx.getSenderName());
            ctx.reply("Stopping server...");
            stop();
        });

        CommandRegistry.registerOp("op", "Grant operator status to a player", PermissionManager.OP_MANAGE, ctx -> {
            if (ctx.getArgs().length == 0) {
                ctx.reply("Usage: op <player> [level] (default level: 1)");
                return;
            }
            String targetName = ctx.getArgs()[0];
            int senderLevel = ctx.isConsole() ? PermissionManager.MAX_OP_LEVEL
                    : PermissionManager.getOpLevel(ctx.getSenderName());
            int grantLevel;
            if (ctx.getArgs().length >= 2) {
                try {
                    grantLevel = Integer.parseInt(ctx.getArgs()[1]);
                } catch (NumberFormatException e) {
                    ctx.reply("Invalid level: " + ctx.getArgs()[1]);
                    return;
                }
                if (grantLevel < 1 || grantLevel > PermissionManager.MAX_OP_LEVEL) {
                    ctx.reply("Op level must be between 1 and " + PermissionManager.MAX_OP_LEVEL + ".");
                    return;
                }
            } else {
                grantLevel = ctx.isConsole() ? PermissionManager.MAX_OP_LEVEL
                        : PermissionManager.OP_BYPASS_SPAWN;
            }
            if (!ctx.isConsole() && grantLevel > senderLevel) {
                ctx.reply("You cannot grant a higher op level than your own (" + senderLevel + ").");
                return;
            }
            int oldLevel = PermissionManager.getOpLevel(targetName);
            if (playerManager.getPlayerByName(targetName) == null) {
                ctx.reply("Note: " + targetName + " is not online. Op will take effect when they join.");
            }
            PermissionManager.addOp(targetName, grantLevel);
            if (oldLevel > 0 && oldLevel != grantLevel) {
                ctx.reply("Changed " + targetName + "'s op level from " + oldLevel + " to " + grantLevel + ".");
            } else {
                ctx.reply("Made " + targetName + " an operator (level " + grantLevel + ").");
            }
            System.out.println("[AUDIT] " + ctx.getSenderName() + " opped " + targetName
                    + " at level " + grantLevel + (oldLevel > 0 ? " (was " + oldLevel + ")" : ""));
        });

        CommandRegistry.registerOp("deop", "Revoke operator status from a player", PermissionManager.OP_MANAGE, ctx -> {
            if (ctx.getArgs().length == 0) {
                ctx.reply("Usage: deop <player>");
                return;
            }
            String targetName = ctx.getArgs()[0];
            int targetLevel = PermissionManager.getOpLevel(targetName);
            if (targetLevel == 0) {
                ctx.reply(targetName + " is not an operator.");
                return;
            }
            if (!ctx.isConsole()) {
                if (targetName.equalsIgnoreCase(ctx.getSenderName())) {
                    ctx.reply("You cannot deop yourself. Ask another operator or use the console.");
                    return;
                }
                int senderLevel = PermissionManager.getOpLevel(ctx.getSenderName());
                if (targetLevel > senderLevel) {
                    ctx.reply("You cannot deop a player with a higher op level than your own.");
                    return;
                }
            }
            PermissionManager.removeOp(targetName);
            ctx.reply("Removed " + targetName + " from operators (was level " + targetLevel + ").");
            System.out.println("[AUDIT] " + ctx.getSenderName() + " deopped " + targetName
                    + " (was level " + targetLevel + ")");
        });

        CommandRegistry.registerOp("kick", "Kick a player from the server", PermissionManager.OP_MANAGE, ctx -> {
            if (ctx.getArgs().length == 0) {
                ctx.reply("Usage: kick <player> [reason]");
                return;
            }
            String target = ctx.getArgs()[0];
            String reason = ctx.getArgs().length > 1
                    ? String.join(" ", java.util.Arrays.copyOfRange(ctx.getArgs(), 1, ctx.getArgs().length))
                    : "Kicked by operator";
            if (playerManager.kickPlayer(target, reason, world)) {
                ctx.reply("Kicked " + target + " (reason: " + reason + ")");
                System.out.println("[INFO] " + ctx.getSenderName() + " kicked " + target + ": " + reason);
            } else {
                ctx.reply("Player not found: " + target);
            }
        });

        CommandRegistry.registerOp("tp", "Teleport players", PermissionManager.OP_CHEAT, ctx -> {
            String[] args = ctx.getArgs();
            if (args.length == 1) {
                // /tp <player> — teleport sender to target
                if (ctx.isConsole()) {
                    ctx.reply("Cannot tp from console without specifying a target");
                    return;
                }
                ConnectedPlayer sender = playerManager.getPlayerByName(ctx.getSenderName());
                ConnectedPlayer target = playerManager.getPlayerByName(args[0]);
                if (sender == null) {
                    ctx.reply("Sender not found");
                    return;
                }
                if (target == null) {
                    ctx.reply("Player not found: " + args[0]);
                    return;
                }
                playerManager.teleportPlayer(sender, target.getDoubleX(), target.getDoubleY(),
                        target.getDoubleZ(), target.getFloatYaw(), target.getFloatPitch(), chunkManager);
                ctx.reply("Teleported to " + target.getUsername());
            } else if (args.length == 2) {
                // /tp <player1> <player2> — teleport player1 to player2
                ConnectedPlayer p1 = playerManager.getPlayerByName(args[0]);
                ConnectedPlayer p2 = playerManager.getPlayerByName(args[1]);
                if (p1 == null) {
                    ctx.reply("Player not found: " + args[0]);
                    return;
                }
                if (p2 == null) {
                    ctx.reply("Player not found: " + args[1]);
                    return;
                }
                playerManager.teleportPlayer(p1, p2.getDoubleX(), p2.getDoubleY(),
                        p2.getDoubleZ(), p2.getFloatYaw(), p2.getFloatPitch(), chunkManager);
                ctx.reply("Teleported " + p1.getUsername() + " to " + p2.getUsername());
            } else if (args.length == 3) {
                // /tp <x> <y> <z> — teleport self to coordinates
                if (ctx.isConsole()) {
                    ctx.reply("Cannot tp from console without specifying a player");
                    return;
                }
                ConnectedPlayer sender = playerManager.getPlayerByName(ctx.getSenderName());
                if (sender == null) {
                    ctx.reply("Sender not found");
                    return;
                }
                try {
                    double x = Double.parseDouble(args[0]);
                    double y = Double.parseDouble(args[1]);
                    double z = Double.parseDouble(args[2]);
                    double eyeY = y + (double) 1.62f;
                    playerManager.teleportPlayer(sender, x, eyeY, z,
                            sender.getFloatYaw(), sender.getFloatPitch(), chunkManager);
                    ctx.reply("Teleported to " + args[0] + " " + args[1] + " " + args[2]);
                } catch (NumberFormatException e) {
                    ctx.reply("Invalid coordinates");
                }
            } else if (args.length == 4) {
                // /tp <player> <x> <y> <z>
                ConnectedPlayer target = playerManager.getPlayerByName(args[0]);
                if (target == null) {
                    ctx.reply("Player not found: " + args[0]);
                    return;
                }
                try {
                    double x = Double.parseDouble(args[1]);
                    double y = Double.parseDouble(args[2]);
                    double z = Double.parseDouble(args[3]);
                    double eyeY = y + (double) 1.62f;
                    playerManager.teleportPlayer(target, x, eyeY, z,
                            target.getFloatYaw(), target.getFloatPitch(), chunkManager);
                    ctx.reply("Teleported " + target.getUsername() + " to "
                            + args[1] + " " + args[2] + " " + args[3]);
                } catch (NumberFormatException e) {
                    ctx.reply("Invalid coordinates");
                }
            } else {
                ctx.reply("Usage: /tp <player> | /tp <x> <y> <z> | /tp <player1> <player2> | /tp <player> <x> <y> <z>");
            }
        });

        CommandRegistry.registerOp("ban", "Ban a player", PermissionManager.OP_MANAGE, ctx -> {
            if (ctx.getArgs().length == 0) {
                ctx.reply("Usage: ban <player> [reason]");
                return;
            }
            String target = ctx.getArgs()[0];
            String reason = ctx.getArgs().length > 1
                    ? String.join(" ", java.util.Arrays.copyOfRange(ctx.getArgs(), 1, ctx.getArgs().length))
                    : "Banned";
            BanManager.banPlayer(target);
            playerManager.kickPlayer(target, reason, world);
            ctx.reply("Banned " + target + (ctx.getArgs().length > 1 ? " (reason: " + reason + ")" : ""));
            System.out.println("[INFO] " + ctx.getSenderName() + " banned " + target + ": " + reason);
        });

        CommandRegistry.registerOp("banip", "Ban an IP address", PermissionManager.OP_MANAGE, ctx -> {
            if (ctx.getArgs().length == 0) {
                ctx.reply("Usage: banip <player|ip>");
                return;
            }
            String arg = ctx.getArgs()[0];
            if (arg.contains(".") || arg.contains(":")) {
                // IP literal
                BanManager.banIp(arg);
                int kicked = 0;
                for (ConnectedPlayer p : playerManager.getAllPlayers()) {
                    String pip = PlayerManager.extractIp(p);
                    if (arg.equals(pip)) {
                        playerManager.kickPlayer(p.getUsername(), "IP banned", world);
                        kicked++;
                    }
                }
                ctx.reply("Banned IP " + arg + (kicked > 0 ? " (" + kicked + " player(s) kicked)" : ""));
                System.out.println("[INFO] " + ctx.getSenderName() + " banned IP " + arg
                        + " (" + kicked + " kicked)");
            } else {
                // Player name — look up their IP and also name-ban
                ConnectedPlayer target = playerManager.getPlayerByName(arg);
                if (target == null) {
                    ctx.reply("Player not found and not a valid IP");
                    return;
                }
                String ip = PlayerManager.extractIp(target);
                if (ip == null) {
                    ctx.reply("Could not determine IP for " + arg);
                    return;
                }
                BanManager.banIp(ip);
                BanManager.banPlayer(arg);
                playerManager.kickPlayer(target.getUsername(), "IP banned", world);
                ctx.reply("Banned IP " + ip + " and player " + arg);
                System.out.println("[INFO] " + ctx.getSenderName() + " banned IP " + ip
                        + " + player " + arg);
            }
        });

        CommandRegistry.registerOp("time", "Query or set the world time", PermissionManager.OP_CHEAT, ctx -> {
            String[] args = ctx.getArgs();
            if (args.length == 0) {
                long time = world.getWorldTime() % 24000;
                String phase;
                if (time < 1000) phase = "early morning";
                else if (time < 6000) phase = "morning";
                else if (time < 12000) phase = "afternoon";
                else if (time < 13000) phase = "evening";
                else if (time < 18000) phase = "night";
                else phase = "late night";
                ctx.reply("The time is " + world.getWorldTime() + " (" + phase + ")"
                        + (world.isTimeFrozen() ? " [frozen]" : ""));
                return;
            }
            String sub = args[0].toLowerCase();
            if (sub.equals("query")) {
                if (args.length >= 2) {
                    switch (args[1].toLowerCase()) {
                        case "daytime":
                            ctx.reply("The time is " + (world.getWorldTime() % 24000));
                            break;
                        case "gametime":
                            ctx.reply("The game time is " + world.getWorldTime());
                            break;
                        case "day":
                            ctx.reply("The day is " + (world.getWorldTime() / 24000));
                            break;
                        default:
                            ctx.reply("Usage: /time query <daytime|gametime|day>");
                    }
                } else {
                    ctx.reply("The time is " + (world.getWorldTime() % 24000));
                }
            } else if (sub.equals("set") && args.length >= 2) {
                long newTime;
                switch (args[1].toLowerCase()) {
                    case "day": newTime = 1000; break;
                    case "noon": newTime = 6000; break;
                    case "sunset": newTime = 12000; break;
                    case "night": newTime = 13000; break;
                    case "midnight": newTime = 18000; break;
                    case "sunrise": newTime = 23000; break;
                    default:
                        try {
                            newTime = Long.parseLong(args[1]);
                        } catch (NumberFormatException e) {
                            ctx.reply("Invalid time: " + args[1]);
                            return;
                        }
                }
                world.setWorldTime(newTime);
                long timeOfDay = world.isTimeFrozen() ? -newTime : newTime;
                playerManager.broadcastTimeUpdate(0, timeOfDay);
                ctx.reply("Set the time to " + newTime);
            } else if (sub.equals("add") && args.length >= 2) {
                long amount;
                try {
                    amount = Long.parseLong(args[1]);
                } catch (NumberFormatException e) {
                    ctx.reply("Invalid time: " + args[1]);
                    return;
                }
                long newTime = world.getWorldTime() + amount;
                world.setWorldTime(newTime);
                long timeOfDay = world.isTimeFrozen() ? -newTime : newTime;
                playerManager.broadcastTimeUpdate(0, timeOfDay);
                ctx.reply("Added " + amount + " to the time (now " + newTime + ")");
            } else if (sub.equals("freeze")) {
                world.setTimeFrozen(true);
                playerManager.broadcastTimeUpdate(0, -world.getWorldTime());
                ctx.reply("Time frozen at " + world.getWorldTime());
            } else if (sub.equals("unfreeze")) {
                world.setTimeFrozen(false);
                playerManager.broadcastTimeUpdate(0, world.getWorldTime());
                ctx.reply("Time unfrozen");
            } else {
                ctx.reply("Usage: /time <add|query|set|freeze|unfreeze>");
                ctx.reply("  /time set <day|noon|sunset|night|midnight|sunrise|ticks>");
                ctx.reply("  /time add <ticks>");
                ctx.reply("  /time query <daytime|gametime|day>");
                ctx.reply("  /time freeze | /time unfreeze");
            }
        });

        CommandRegistry.registerOp("weather", "Set the weather", PermissionManager.OP_CHEAT, ctx -> {
            String[] args = ctx.getArgs();
            if (args.length == 0) {
                ctx.reply("Weather: " + world.getWeather().name().toLowerCase());
                ctx.reply("Usage: /weather <clear|rain|thunder> [duration_seconds]");
                return;
            }
            String type = args[0].toLowerCase();
            int durationTicks = 0; // 0 = indefinite
            if (args.length >= 2) {
                try {
                    int seconds = Integer.parseInt(args[1]);
                    durationTicks = seconds * 20;
                } catch (NumberFormatException e) {
                    ctx.reply("Invalid duration: " + args[1]);
                    return;
                }
            }
            ServerWorld.WeatherState newWeather;
            switch (type) {
                case "clear":
                    newWeather = ServerWorld.WeatherState.CLEAR;
                    break;
                case "rain":
                    newWeather = ServerWorld.WeatherState.RAIN;
                    break;
                case "thunder":
                    newWeather = ServerWorld.WeatherState.THUNDER;
                    break;
                default:
                    ctx.reply("Unknown weather type: " + type);
                    ctx.reply("Usage: /weather <clear|rain|thunder> [duration_seconds]");
                    return;
            }
            world.setWeather(newWeather, durationTicks);
            playerManager.broadcastWeatherChange(newWeather);
            ctx.reply("Set weather to " + type
                    + (durationTicks > 0 ? " for " + (durationTicks / 20) + " seconds" : ""));
        });

        CommandRegistry.registerOp("unban", "Unban a player or IP", PermissionManager.OP_MANAGE, ctx -> {
            if (ctx.getArgs().length == 0) {
                java.util.Set<String> players = BanManager.getBannedPlayers();
                java.util.Set<String> ips = BanManager.getBannedIps();
                if (players.isEmpty() && ips.isEmpty()) {
                    ctx.reply("No banned players or IPs.");
                } else {
                    if (!players.isEmpty()) {
                        ctx.reply("Banned players: " + String.join(", ", players));
                    }
                    if (!ips.isEmpty()) {
                        ctx.reply("Banned IPs: " + String.join(", ", ips));
                    }
                }
                ctx.reply("Usage: unban <player|ip>");
                return;
            }
            String arg = ctx.getArgs()[0];
            boolean wasIp = arg.contains(".") || arg.contains(":");
            boolean wasBanned;
            if (wasIp) {
                wasBanned = BanManager.getBannedIps().contains(arg);
                if (wasBanned) {
                    BanManager.unbanIp(arg);
                }
            } else {
                wasBanned = BanManager.getBannedPlayers().contains(arg);
                if (wasBanned) {
                    BanManager.unbanPlayer(arg);
                }
            }
            if (wasBanned) {
                ctx.reply("Unbanned " + arg);
                System.out.println("[INFO] " + ctx.getSenderName() + " unbanned " + arg);
            } else {
                ctx.reply(arg + " is not banned.");
            }
        });
    }

    /**
     * Register spawn protection event listeners that prevent non-op players
     * from breaking or placing blocks within the configured radius of spawn.
     */
    private void registerSpawnProtection() {
        int spawnRadius = ServerProperties.getSpawnProtection();
        if (spawnRadius <= 0) {
            System.out.println("Spawn protection disabled (spawn-protection=0)");
            return;
        }

        int spawnX = world.getSpawnX();
        int spawnZ = world.getSpawnZ();

        // Warn if the protection area covers the entire world
        if (spawnRadius >= world.getWidth() / 2 || spawnRadius >= world.getDepth() / 2) {
            System.out.println("[WARN] spawn-protection=" + spawnRadius
                    + " covers the entire world — non-op players cannot modify any blocks");
        }

        java.util.concurrent.ConcurrentHashMap<String, Long> lastNotified =
                new java.util.concurrent.ConcurrentHashMap<>();

        // Shared check for both break and place — same signature (String, int, int, int, int) -> EventResult
        ServerEvents.BLOCK_BREAK.register((player, x, y, z, blockType) ->
                checkSpawnProtection(player, x, z, spawnX, spawnZ, spawnRadius, lastNotified));
        ServerEvents.BLOCK_PLACE.register((player, x, y, z, blockType) ->
                checkSpawnProtection(player, x, z, spawnX, spawnZ, spawnRadius, lastNotified));

        // Clean up notification tracking when players leave
        ServerEvents.PLAYER_LEAVE.register(lastNotified::remove);

        System.out.println("Spawn protection enabled: " + spawnRadius + " blocks around ("
                + spawnX + ", " + spawnZ + ")");
    }

    private EventResult checkSpawnProtection(String player, int x, int z,
            int spawnX, int spawnZ, int spawnRadius,
            java.util.concurrent.ConcurrentHashMap<String, Long> lastNotified) {
        if (Math.abs(x - spawnX) <= spawnRadius && Math.abs(z - spawnZ) <= spawnRadius
                && PermissionManager.getOpLevel(player) < PermissionManager.OP_BYPASS_SPAWN) {
            long now = System.currentTimeMillis();
            Long last = lastNotified.get(player);
            if (last == null || now - last >= 5_000) {
                lastNotified.put(player, now);
                System.out.println("[SpawnProtection] Blocked " + player
                        + " at (" + x + ", " + z + ")");
                ConnectedPlayer cp = playerManager.getPlayerByName(player);
                if (cp != null) {
                    playerManager.sendChat(cp, "You cannot modify blocks in the spawn protection area.");
                }
            }
            return EventResult.CANCEL;
        }
        return EventResult.PASS;
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
     *
     * Configuration is read from server.properties (created with defaults if missing).
     * CLI port argument overrides server-port from the file.
     * System properties (-Drdforward.*, -De2e.viewDistance) override file values.
     */
    public static void main(String[] args) {
        // Load server.properties (creates with defaults if missing)
        ServerProperties.load();

        int port;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port: " + args[0]);
                System.exit(1);
                return;
            }
        } else {
            port = ServerProperties.getServerPort();
        }

        ProtocolVersion version;
        try {
            version = ServerProperties.getProtocolVersion();
        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR] " + e.getMessage());
            System.exit(1);
            return;
        }
        WorldGenerator generator = ServerProperties.createWorldGenerator();
        long seed = ServerProperties.getLevelSeed();
        int worldWidth = ServerProperties.getWorldWidth();
        int worldHeight = ServerProperties.getWorldHeight();
        int worldDepth = ServerProperties.getWorldDepth();

        PlayerManager.setMaxPlayers(ServerProperties.getMaxPlayers());

        System.out.println("[RDForward] World config: " + worldWidth + "x" + worldHeight + "x" + worldDepth
            + ", generator=" + generator.getName() + ", seed=" + seed);

        RDServer server = new RDServer(port, version, generator, seed,
            worldWidth, worldHeight, worldDepth);
        server.setBedrockPort(ServerProperties.getBedrockPort());
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "RDForward-Shutdown"));

        try {
            server.start();
            server.runConsole();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
