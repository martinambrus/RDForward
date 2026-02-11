package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.PacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.PacketEncoder;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.BufferedReader;
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

    private final int port;
    private final ProtocolVersion protocolVersion;
    private final ServerWorld world;
    private final PlayerManager playerManager;
    private final ServerTickLoop tickLoop;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private volatile boolean stopped = false;

    public RDServer(int port) {
        this(port, ProtocolVersion.RUBYDUNG);
    }

    public RDServer(int port, ProtocolVersion protocolVersion) {
        this.port = port;
        this.protocolVersion = protocolVersion;
        this.world = new ServerWorld(DEFAULT_WORLD_WIDTH, DEFAULT_WORLD_HEIGHT, DEFAULT_WORLD_DEPTH);
        this.playerManager = new PlayerManager();
        this.tickLoop = new ServerTickLoop(playerManager, world);
    }

    /**
     * Start the server: generate world, start tick loop, begin accepting connections.
     */
    public void start() throws InterruptedException {
        if (!world.load()) {
            System.out.println("Generating world (" + DEFAULT_WORLD_WIDTH + "x"
                    + DEFAULT_WORLD_HEIGHT + "x" + DEFAULT_WORLD_DEPTH + ")...");
            world.generateFlatWorld();
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
                        pipeline.addLast("decoder", new PacketDecoder(
                                PacketDirection.CLIENT_TO_SERVER, protocolVersion));
                        pipeline.addLast("encoder", new PacketEncoder());
                        pipeline.addLast("handler", new ServerConnectionHandler(
                                protocolVersion, world, playerManager));
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

        System.out.println("Saving world...");
        world.save();

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
     * Usage: java -cp rd-server.jar com.github.martinambrus.rdforward.server.RDServer [port]
     * Default port: 25565
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

        RDServer server = new RDServer(port);
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "RDForward-Shutdown"));

        try {
            server.start();
            server.runConsole();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
