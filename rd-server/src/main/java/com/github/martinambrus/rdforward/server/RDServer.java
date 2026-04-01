package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.codec.PacketDecoder;
import com.github.martinambrus.rdforward.protocol.codec.PacketEncoder;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.server.api.BlockOwnerRegistry;
import com.github.martinambrus.rdforward.server.api.BanManager;
import com.github.martinambrus.rdforward.server.api.GriefProtection;
import com.github.martinambrus.rdforward.server.api.TeamManager;
import com.github.martinambrus.rdforward.server.api.WhitelistManager;
import com.github.martinambrus.rdforward.server.api.CommandRegistry;
import com.github.martinambrus.rdforward.server.api.PermissionManager;
import com.github.martinambrus.rdforward.server.api.Scheduler;
import com.github.martinambrus.rdforward.server.api.ServerProperties;
import com.github.martinambrus.rdforward.world.FlatWorldGenerator;
import com.github.martinambrus.rdforward.world.WorldGenerator;
import com.github.martinambrus.rdforward.world.convert.ConversionRegistry;
import com.github.martinambrus.rdforward.world.convert.ServerToOriginalRubyDungConverter;
import com.github.martinambrus.rdforward.world.convert.ServerWorldHeader;
import com.github.martinambrus.rdforward.world.convert.WorldFormat;
import com.github.martinambrus.rdforward.world.convert.WorldFormatDetector;
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
import java.io.IOException;
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

    static {
        // Reduce Netty's PooledByteBufAllocator arena size from 16MiB (default maxOrder=11)
        // to 4MiB (maxOrder=9). MC's max packet size is 2MiB, so 16MiB arenas waste memory.
        // User can still override via -Dio.netty.allocator.maxOrder=N on the command line.
        if (System.getProperty("io.netty.allocator.maxOrder") == null) {
            System.setProperty("io.netty.allocator.maxOrder", "9");
        }
    }

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
    /** Secondary listener on port 5565 for Classic 0.0.15a (hardcoded port in client). */
    private Channel classicChannel;
    private Channel bedrockChannel;
    private com.github.martinambrus.rdforward.server.lce.LCELanAdvertiser lceLanAdvertiser;
    private Channel udpFrontEndChannel;
    private LegacyRakNetServer mcpeServer;
    private UdpFrontEndHandler udpFrontEndHandler;
    private volatile BedrockBlockMapper bedrockBlockMapper;
    private volatile BedrockChunkConverter bedrockChunkConverter;
    private volatile BedrockRegistryData bedrockRegistryData;
    private volatile boolean stopped = false;

    private File getDataDir() {
        return (dataDir != null) ? dataDir : new File(".");
    }

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
                    chunkManager.setBedrockChunkConverter(converter);
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
                + ChunkManager.DEFAULT_VIEW_DISTANCE);
        this.chunkManager.setServerWorld(world);
        this.world.setIOThread(chunkManager.getIOThread());
        this.tickLoop = new ServerTickLoop(playerManager, world, chunkManager);
    }

    /**
     * Start the server: generate world, start tick loop, begin accepting connections.
     */
    public void start() throws InterruptedException {
        // Initialize mod APIs
        PermissionManager.load(dataDir);
        BanManager.load(dataDir);
        WhitelistManager.load(dataDir);
        TeamManager.load(dataDir);
        BlockOwnerRegistry.load(dataDir);
        Scheduler.init();
        chunkManager.initAsyncDelivery();
        registerBuiltInCommands();
        registerSpawnProtection();
        GriefProtection.init(ServerProperties.getMaxBlockChangesPerSecond(), playerManager, world, chunkManager);

        // Track player activity for block ownership and protection budget
        ServerEvents.PLAYER_JOIN.register((name, version) -> {
            // Check expiry BEFORE updating last login
            int daysLeft = BlockOwnerRegistry.getDaysUntilExpiry(name);
            BlockOwnerRegistry.updateLastLogin(name);
            BlockOwnerRegistry.startSession(name);
            ConnectedPlayer cp = playerManager.getPlayerByName(name);
            // Warn returning players whose blocks are close to expiring
            if (daysLeft >= 0 && daysLeft <= 7 && cp != null) {
                if (daysLeft == 0) {
                    playerManager.sendChat(cp,
                            "[GriefProtection] Your blocks have expired due to inactivity.");
                } else {
                    playerManager.sendChat(cp,
                            "[GriefProtection] Your blocks will expire in " + daysLeft
                            + " day(s) of inactivity. Play to reset the timer.");
                }
            }
            // Remind players who were kicked/banned by grief protection about /rtp
            if (GriefProtection.wasGriefKicked(name) && cp != null) {
                playerManager.sendChat(cp,
                        "[GriefProtection] Use /rtp to teleport away from traps or encasements.");
            }
            // Onboarding: tell new players about the protection system
            if (cp != null && BlockOwnerRegistry.getEffectivePlayTimeMs(name) == 0
                    && BlockOwnerRegistry.getId(name) == 0) {
                playerManager.sendChat(cp,
                        "[GriefProtection] Blocks you place are protected from griefing."
                        + " Use /griefinfo for details.");
            }
        });

        // Accumulate play time when players leave (for protection budget accrual)
        ServerEvents.PLAYER_LEAVE.register(name -> BlockOwnerRegistry.endSession(name));

        // Apply locked time from config, or auto-lock for rubydung maps
        long lockTime = ServerProperties.getLockTime();
        if (lockTime >= 0) {
            world.setWorldTime(lockTime);
            world.setTimeFrozen(true);
            System.out.println("World time locked to " + lockTime + " ticks");
        } else if ("rubydung".equals(ServerProperties.getLevelType())) {
            world.setWorldTime(6000);
            world.setTimeFrozen(true);
            System.out.println("World time locked to noon (RubyDung map has no night)");
        }

        convertWorldIfNeeded();
        checkWorldCompatibility();

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

        // Classic 0.0.15a hardcodes port 5565 — bind there too if main port differs
        if (port != 5565) {
            try {
                classicChannel = bootstrap.bind(5565).sync().channel();
                System.out.println("Classic 0.0.15a listener started on port 5565");
            } catch (Exception e) {
                System.err.println("Could not bind Classic 0.0.15a port 5565: " + e.getMessage());
            }
        }

        // Start unified UDP server (legacy MCPE + modern Bedrock on port 19132)
        startUnifiedUdpServer();
    }

    /**
     * Detect and convert world files to the server format if needed.
     *
     * In RD/Classic mode, server-world.dat is the authoritative source.
     * The world/ directory is just a ChunkManager cache (rebuilt on demand
     * via overlayServerWorldBlocks), not an independent Alpha world.
     *
     * This method handles two scenarios:
     *   1. server-world.dat exists but is original RD format (no header) — convert in place
     *   2. level.dat exists but no server-world.dat — convert level.dat to server format
     *
     * In both cases, any stale world/ chunk cache is removed since it
     * corresponds to a different save and would be overwritten by
     * ChunkManager's overlay anyway.
     */
    private void convertWorldIfNeeded() {
        File dir = getDataDir();
        File worldFile = new File(dir, "server-world.dat");
        File originalRdFile = new File(dir, "level.dat");

        boolean converted = false;

        // Clean up leftover temp file from a previously interrupted conversion
        File staleTemp = new File(dir, "server-world.dat.converting");
        if (staleTemp.exists()) {
            System.out.println("[RDServer] Removing leftover conversion temp file.");
            staleTemp.delete();
        }

        // Case 1: server-world.dat exists but is actually original RD format
        if (worldFile.exists()) {
            WorldFormat detected = WorldFormatDetector.detect(worldFile);
            if (detected == WorldFormat.RUBYDUNG) {
                System.out.println("[RDServer] server-world.dat is in original RubyDung format, "
                        + "converting to server format...");
                File tempFile = new File(dir, "server-world.dat.converting");
                File backupFile = new File(dir, "server-world.dat.backup");
                try {
                    long startMs = System.currentTimeMillis();
                    ConversionRegistry.createDefault().convert(worldFile, tempFile,
                            WorldFormat.RUBYDUNG, WorldFormat.RUBYDUNG_SERVER, worldSeed);
                    // Backup original, then replace with converted file
                    java.nio.file.Files.move(worldFile.toPath(), backupFile.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    if (!tempFile.renameTo(worldFile)) {
                        java.nio.file.Files.move(tempFile.toPath(), worldFile.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    backupFile.delete();
                    System.out.println("[RDServer] Conversion complete in "
                            + (System.currentTimeMillis() - startMs) + "ms.");
                    converted = true;
                } catch (Exception e) {
                    System.err.println("[RDServer] Conversion failed: " + e.getMessage());
                    e.printStackTrace();
                    // Restore backup if original was moved
                    if (!worldFile.exists() && backupFile.exists()) {
                        backupFile.renameTo(worldFile);
                    }
                    tempFile.delete();
                }
            }
        }

        // Case 2: level.dat exists but no server-world.dat
        // Write to temp file first, then rename — crash-safe
        if (!worldFile.exists() && originalRdFile.exists()) {
            WorldFormat detected = WorldFormatDetector.detect(originalRdFile);
            if (detected == WorldFormat.RUBYDUNG) {
                System.out.println("[RDServer] Original RubyDung level.dat detected, "
                        + "converting to server format...");
                File tempFile = new File(dir, "server-world.dat.converting");
                try {
                    long startMs = System.currentTimeMillis();
                    ConversionRegistry.createDefault().convert(originalRdFile, tempFile,
                            WorldFormat.RUBYDUNG, WorldFormat.RUBYDUNG_SERVER, worldSeed);
                    if (!tempFile.renameTo(worldFile)) {
                        java.nio.file.Files.move(tempFile.toPath(), worldFile.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                    System.out.println("[RDServer] Conversion complete in "
                            + (System.currentTimeMillis() - startMs) + "ms.");
                    converted = true;
                } catch (Exception e) {
                    System.err.println("[RDServer] Conversion failed: " + e.getMessage());
                    e.printStackTrace();
                    tempFile.delete();
                }
            }
        }

        // If we converted a new world, clear the stale chunk cache
        // (it was derived from the old save and won't match the new data)
        if (converted) {
            String levelName = ServerProperties.getLevelName();
            File worldDir = new File(dir, levelName);
            if (worldDir.isDirectory()) {
                System.out.println("[RDServer] Clearing stale chunk cache at " + worldDir + "...");
                deleteRecursive(worldDir);
            }
        }
    }

    /**
     * Validate that {@code server-version} and {@code level-type} are compatible,
     * then check whether the existing world on disk matches the current config.
     *
     * <b>Config validation:</b>
     *   server-version determines the server mode (finite-world vs chunk-gen).
     *   level-type must be compatible — e.g. "alpha" level-type with a RubyDung
     *   server-version is not allowed because a finite-world server cannot
     *   host infinite Alpha terrain.
     *
     * <b>Downgrade</b> (newer world → older server-version, e.g. Alpha→RubyDung):
     *   Backwards conversion is not possible. The server refuses to start and
     *   tells the user to back up and remove the old world or restore settings.
     *
     * <b>Upgrade</b> (older world → newer server-version, e.g. RubyDung→Alpha):
     *   The existing world is automatically converted to the new format. The
     *   old world file is renamed (not deleted) so the user can remove it once
     *   they are satisfied with the result.
     */
    private void checkWorldCompatibility() {
        boolean finiteServer = protocolVersion.isFiniteWorld();
        String levelType = ServerProperties.getLevelType();

        // --- Config validation: level-type must be compatible with server-version ---
        if (finiteServer && "alpha".equals(levelType)) {
            System.err.println("==========================================================");
            System.err.println("INCOMPATIBLE CONFIGURATION");
            System.err.println();
            System.err.println("server-version '" + ServerProperties.getServerVersion()
                    + "' uses a finite world, but level-type is set to 'alpha'");
            System.err.println("which requires infinite chunk-based world generation.");
            System.err.println();
            System.err.println("Options:");
            System.err.println("  1. Change level-type to 'flat', 'rubydung', or 'classic'");
            System.err.println("  2. Change server-version to an Alpha or later version");
            System.err.println("     (e.g. a1.2.6, b1.7.3)");
            System.err.println("==========================================================");
            throw new IllegalStateException("Incompatible config: level-type 'alpha'"
                    + " requires a chunk-gen server-version, but '"
                    + ServerProperties.getServerVersion() + "' is finite-world");
        }
        // Chunk-gen servers cannot use finite-only level types
        if (!finiteServer && ("rubydung".equals(levelType) || "classic".equals(levelType))) {
            System.err.println("==========================================================");
            System.err.println("INCOMPATIBLE CONFIGURATION");
            System.err.println();
            System.err.println("server-version '" + ServerProperties.getServerVersion()
                    + "' uses chunk-based world generation, but level-type");
            System.err.println("is set to '" + levelType + "' which only supports finite worlds.");
            System.err.println();
            System.err.println("Options:");
            System.err.println("  1. Change level-type to 'flat' or 'alpha'");
            System.err.println("  2. Change server-version to a finite-world version");
            System.err.println("     (e.g. rd-132211, c0.30)");
            System.err.println("==========================================================");
            throw new IllegalStateException("Incompatible config: level-type '" + levelType
                    + "' requires a finite-world server-version, but '"
                    + ServerProperties.getServerVersion() + "' uses chunk generation");
        }

        File dir = getDataDir();
        File serverWorldFile = new File(dir, "server-world.dat");
        String levelName = ServerProperties.getLevelName();
        File worldDir = (dataDir != null) ? new File(dataDir, levelName) : new File(levelName);

        boolean hasFiniteWorld = serverWorldFile.exists();
        boolean hasAlphaWorld = worldDir.isDirectory()
                && WorldFormatDetector.detect(worldDir) == WorldFormat.ALPHA;

        // Both worlds exist — warn that one is unused so the admin doesn't get confused.
        if (hasAlphaWorld && hasFiniteWorld) {
            if (finiteServer) {
                System.out.println("[RDServer] Both server-world.dat and Alpha world directory"
                        + " found; using server-world.dat (finite mode). The Alpha world at '"
                        + worldDir + "' is unused and can be removed.");
            } else {
                System.out.println("[RDServer] Both server-world.dat and Alpha world directory"
                        + " found; using Alpha world (chunk-gen mode). server-world.dat"
                        + " is unused and can be removed.");
            }
        }

        // --- Downgrade: Alpha world on disk, server-version switched to finite ---
        // Backwards conversion is not possible. Stop and let the user decide.
        if (hasAlphaWorld && !hasFiniteWorld && finiteServer) {
            System.err.println("==========================================================");
            System.err.println("INCOMPATIBLE WORLD FORMAT");
            System.err.println();
            System.err.println("Found an Alpha world at '" + worldDir + "' but server-version");
            System.err.println("is '" + ServerProperties.getServerVersion()
                    + "' (" + protocolVersion.getDisplayName() + ").");
            System.err.println("Alpha worlds cannot be converted back to the finite-world format.");
            System.err.println();
            System.err.println("Options:");
            System.err.println("  1. Change server-version to an Alpha or later version");
            System.err.println("     (e.g. a1.2.6, b1.7.3) to keep using this world.");
            System.err.println("  2. Back up and remove the '" + levelName + "/' directory");
            System.err.println("     to start fresh with a new "
                    + protocolVersion.getDisplayName() + " world.");
            System.err.println("==========================================================");
            throw new IllegalStateException("Incompatible world format: Alpha world"
                    + " cannot be loaded with server-version '"
                    + ServerProperties.getServerVersion() + "'");
        }

        // --- Upgrade: finite world on disk, server-version switched to chunk-gen ---
        // Auto-convert RubyDung → Alpha. Rename the old server-world.dat so the
        // user can delete it once they're happy with the converted world.
        if (hasFiniteWorld && !finiteServer && !hasAlphaWorld) {
            int version = ServerWorldHeader.readFormatVersion(serverWorldFile);
            if (version >= ServerWorldHeader.FORMAT_V1_FINITE
                    && version <= ServerWorldHeader.CURRENT_FORMAT_VERSION) {
                System.out.println("[RDServer] server-version is '"
                        + ServerProperties.getServerVersion()
                        + "' but a finite world (server-world.dat) exists.");
                System.out.println("[RDServer] Auto-converting to Alpha format...");
                try {
                    long startMs = System.currentTimeMillis();
                    if (!worldDir.mkdirs() && !worldDir.isDirectory()) {
                        throw new IOException("Failed to create world directory: " + worldDir);
                    }
                    ConversionRegistry.createDefault().convert(
                            serverWorldFile, worldDir,
                            WorldFormat.RUBYDUNG_SERVER, WorldFormat.ALPHA, worldSeed);

                    // Rename old file instead of deleting — user can remove it later
                    File backup = new File(dir, "server-world.dat.old");
                    if (!serverWorldFile.renameTo(backup)) {
                        java.nio.file.Files.move(serverWorldFile.toPath(), backup.toPath(),
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }

                    // Detach ServerWorld from ChunkManager so the converted Alpha
                    // world is the sole authority — no finite-world overlay.
                    chunkManager.setServerWorld(null);
                    System.out.println("[ChunkManager] Overlay detached; Alpha chunk saves enabled.");

                    System.out.println("[RDServer] Conversion complete in "
                            + (System.currentTimeMillis() - startMs) + "ms.");
                    System.out.println("[RDServer] Old world backed up to " + backup.getName()
                            + " — you can delete it once you're happy with the converted world.");
                } catch (Exception e) {
                    // Clean up partial Alpha directory to allow retry on next startup
                    if (worldDir.isDirectory()) {
                        System.out.println("[RDServer] Cleaning up partial conversion at " + worldDir + "...");
                        deleteRecursive(worldDir);
                    }
                    System.err.println("[RDServer] Auto-conversion failed: " + e.getMessage());
                    e.printStackTrace();
                    System.err.println("[RDServer] The original server-world.dat is preserved.");
                    System.err.println("[RDServer] The server will start in finite-world overlay mode.");
                    System.err.println("[RDServer] To retry, fix the issue above and restart.");
                }
            } else {
                System.out.println("[RDServer] server-world.dat has unrecognized format version "
                        + version + "; skipping auto-conversion to Alpha.");
            }
        }
    }

    private static void deleteRecursive(File file) {
        // Don't follow symlinks — delete the link itself, not its target
        if (java.nio.file.Files.isSymbolicLink(file.toPath())) {
            if (!file.delete()) {
                System.err.println("[RDServer] Failed to delete symlink: " + file);
            }
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        if (!file.delete()) {
            System.err.println("[RDServer] Failed to delete: " + file);
        }
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
                .childOption(RakChannelOption.RAK_SESSION_TIMEOUT, 30000L)
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

        // Start LCE LAN discovery advertiser
        lceLanAdvertiser = new com.github.martinambrus.rdforward.server.lce.LCELanAdvertiser(
                port, com.github.martinambrus.rdforward.server.api.ServerProperties.getMotd(),
                playerManager);
        lceLanAdvertiser.start();
    }

    /**
     * Stop the server and release all resources.
     */
    public void stop() {
        if (stopped) return;
        stopped = true;

        System.out.println("Stopping server...");
        if (lceLanAdvertiser != null) lceLanAdvertiser.stop();
        tickLoop.stop();

        System.out.println("Saving world and player data...");
        world.save();
        world.savePlayers(playerManager.getAllPlayers());
        BlockOwnerRegistry.saveIfDirty();
        TeamManager.saveIfDirty();
        chunkManager.shutdown();

        if (udpFrontEndChannel != null) {
            udpFrontEndChannel.close();
        }
        if (bedrockChannel != null) {
            bedrockChannel.close();
        }
        if (classicChannel != null) {
            classicChannel.close();
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
     * Send EntityEvent (OP permission level) and DeclareCommands to an online
     * player after their OP level changes. If the player is not online or not
     * a 1.8+ Netty client, this is a no-op.
     */
    private void sendOpUpdatePackets(String playerName, int newOpLevel) {
        ConnectedPlayer target = playerManager.getPlayerByName(playerName);
        if (target == null || target.getChannel() == null || !target.getChannel().isActive()) return;

        // 1.8+: send EntityEvent with OP permission level
        if (target.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            int entityId = target.getPlayerId() + 1;
            target.sendPacket(new com.github.martinambrus.rdforward.protocol.packet.netty.NettyEntityEventPacket(
                    entityId, com.github.martinambrus.rdforward.protocol.packet.netty.NettyEntityEventPacket.OP_PERMISSION_BASE + newOpLevel));
        }

        // 1.13+: resend DeclareCommands with updated command list
        if (target.getProtocolVersion().isAtLeast(ProtocolVersion.RELEASE_1_13)) {
            java.util.List<String> cmds = CommandRegistry.getCommandNamesForOpLevel(newOpLevel);
            target.sendPacket(com.github.martinambrus.rdforward.protocol.packet.netty.DeclareCommandsPacketV393.withCommands(cmds));
        }
    }

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

        CommandRegistry.registerOp("debug", "Toggle debug logging (console only)", PermissionManager.OP_ADMIN, ctx -> {
            if (!ctx.isConsole()) {
                ctx.reply("Debug commands are console-only.");
                return;
            }
            String[] args = ctx.getArgs();
            if (args.length == 0) {
                // Toggle
                DebugLog.setEnabled(!DebugLog.isEnabled());
                ctx.reply("[Server] Debug " + (DebugLog.isEnabled() ? "ON" + DebugLog.getAutoOffDesc() : "OFF"));
                return;
            }
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "on":
                    DebugLog.setEnabled(true);
                    ctx.reply("[Server] Debug ON (blocks=" + DebugLog.isBlocks()
                            + " pos=" + DebugLog.isPos() + " chunks=" + DebugLog.isChunks()
                            + " packets=" + DebugLog.isPackets()
                            + " players=" + DebugLog.getPlayerFilterDesc() + ")"
                            + DebugLog.getAutoOffDesc());
                    break;
                case "off":
                    DebugLog.setEnabled(false);
                    ctx.reply("[Server] Debug OFF");
                    break;
                case "status":
                    ctx.reply("[Server] Debug: " + (DebugLog.isEnabled() ? "ON" + DebugLog.getAutoOffDesc() : "OFF"));
                    ctx.reply("[Server]   blocks=" + DebugLog.isBlocks()
                            + " pos=" + DebugLog.isPos() + " chunks=" + DebugLog.isChunks()
                            + " packets=" + DebugLog.isPackets() + " verbose=" + DebugLog.isVerbose());
                    ctx.reply("[Server]   players=" + DebugLog.getPlayerFilterDesc());
                    break;
                case "player":
                    if (args.length < 2) {
                        ctx.reply("Usage: debug player <name|*|-name>");
                        return;
                    }
                    String pArg = args[1];
                    if ("*".equals(pArg)) {
                        DebugLog.clearPlayerFilter();
                        ctx.reply("[Server] Debug player filter cleared (logging all players)");
                    } else if (pArg.startsWith("-")) {
                        String removeName = pArg.substring(1);
                        if (removeName.isEmpty()) {
                            ctx.reply("Usage: debug player -<name>");
                            return;
                        }
                        DebugLog.removePlayerFilter(removeName);
                        ctx.reply("[Server] Removed " + removeName + " from debug filter (players=" + DebugLog.getPlayerFilterDesc() + ")");
                    } else {
                        if (pArg.isEmpty()) {
                            ctx.reply("Usage: debug player <name>");
                            return;
                        }
                        DebugLog.addPlayerFilter(pArg);
                        ctx.reply("[Server] Added " + pArg + " to debug filter (players=" + DebugLog.getPlayerFilterDesc() + ")");
                    }
                    break;
                case "blocks":
                case "pos":
                case "chunks":
                case "packets": {
                    if (args.length >= 2) {
                        String toggle = args[1].toLowerCase();
                        if (!"on".equals(toggle) && !"off".equals(toggle)) {
                            ctx.reply("Usage: debug " + sub + " on|off");
                            return;
                        }
                        boolean val = "on".equals(toggle);
                        if ("blocks".equals(sub)) DebugLog.setBlocks(val);
                        else if ("pos".equals(sub)) DebugLog.setPos(val);
                        else if ("packets".equals(sub)) DebugLog.setPackets(val);
                        else DebugLog.setChunks(val);
                        ctx.reply("[Server] Debug " + sub + " " + (val ? "ON" : "OFF"));
                    } else {
                        // No value — toggle
                        boolean current;
                        if ("blocks".equals(sub)) { current = DebugLog.isBlocks(); DebugLog.setBlocks(!current); }
                        else if ("pos".equals(sub)) { current = DebugLog.isPos(); DebugLog.setPos(!current); }
                        else if ("packets".equals(sub)) { current = DebugLog.isPackets(); DebugLog.setPackets(!current); }
                        else { current = DebugLog.isChunks(); DebugLog.setChunks(!current); }
                        ctx.reply("[Server] Debug " + sub + " " + (!current ? "ON" : "OFF"));
                    }
                    break;
                }
                case "verbose":
                    DebugLog.setVerbose(!DebugLog.isVerbose());
                    ctx.reply("[Server] Debug verbose " + (DebugLog.isVerbose() ? "ON — logging ALL block changes (no sampling)" : "OFF — sampling enabled"));
                    break;
                case "info":
                    ctx.reply("[Server] Debug logging — usage guide:");
                    ctx.reply("  debug on/off          — master toggle (auto-off after " + DebugLog.AUTO_OFF_MINUTES + " min)");
                    ctx.reply("  debug status          — show current state");
                    ctx.reply("  debug player <name>   — add <name> to filter (additive)");
                    ctx.reply("  debug player -<name>  — remove <name> from filter");
                    ctx.reply("  debug player *        — clear filter (log all players)");
                    ctx.reply("  debug blocks [on|off]  — toggle block place/break logging");
                    ctx.reply("  debug pos [on|off]     — toggle position/teleport logging");
                    ctx.reply("  debug chunks [on|off]  — toggle chunk send/load logging");
                    ctx.reply("  debug packets [on|off] — toggle S2C packet trace (WARNING: very verbose)");
                    ctx.reply("  debug verbose          — toggle verbose mode (log every block change, no sampling)");
                    ctx.reply("  Categories: BLOCK (place/break/grief), POS (move/teleport/save), CHUNK (load/send), PKT (S2C wire)");
                    ctx.reply("  State does not survive restarts.");
                    ctx.reply("  Example: debug player Alice  then  debug on");
                    break;
                default:
                    ctx.reply("Usage: debug [on|off|status|info|verbose|player <name>|blocks|pos|chunks|packets] [on|off]");
                    break;
            }
        });

        CommandRegistry.registerOp("say", "Broadcast a message to all players", PermissionManager.OP_CHEAT, ctx -> {
            if (ctx.getArgs().length == 0) {
                ctx.reply("Usage: say <message>");
                return;
            }
            String message = String.join(" ", ctx.getArgs());
            String sender = ctx.isConsole() ? "Server" : ctx.getSenderName();
            String formatted = "[" + sender + "] " + message;
            playerManager.broadcastChat((byte) 0, formatted);
            System.out.println("[INFO] " + ctx.getSenderName() + " issued say: " + formatted);
        });

        CommandRegistry.registerOp("save", "Save the world to disk", PermissionManager.OP_ADMIN, ctx -> {
            world.save();
            world.savePlayers(playerManager.getAllPlayers());
            BlockOwnerRegistry.saveIfDirty();
            TeamManager.saveIfDirty();
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
            sendOpUpdatePackets(targetName, grantLevel);
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
            sendOpUpdatePackets(targetName, 0);
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

        CommandRegistry.registerOp("ip", "Show a player's IP address", PermissionManager.OP_ADMIN, ctx -> {
            if (ctx.getArgs().length == 0) {
                ctx.reply("Usage: ip <player>");
                return;
            }
            String target = ctx.getArgs()[0];
            ConnectedPlayer player = playerManager.getPlayerByName(target);
            if (player == null) {
                ctx.reply("Player not found: " + target);
                return;
            }
            String ip = PlayerManager.extractIp(player);
            ctx.reply(player.getUsername() + "'s IP: " + (ip != null ? ip : "unknown"));
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

        CommandRegistry.register("rtp", "Random teleport to a safe location", ctx -> {
            if (ctx.isConsole()) {
                ctx.reply("Cannot rtp from console");
                return;
            }
            ConnectedPlayer sender = playerManager.getPlayerByName(ctx.getSenderName());
            if (sender == null) {
                ctx.reply("Player not found");
                return;
            }
            // Determine if this is a finite or chunk-based world
            boolean finite = protocolVersion.isFiniteWorld();
            int worldH = finite ? world.getHeight() : 128; // Alpha chunks are 128 high
            java.util.Random rng = new java.util.Random();
            int maxAttempts = 50;
            // Range: finite worlds use world bounds; chunk worlds use ±500 around sender
            int rangeMin, rangeMaxX, rangeMaxZ;
            if (finite) {
                int margin = 5;
                rangeMin = margin;
                rangeMaxX = Math.max(1, world.getWidth() - 2 * margin);
                rangeMaxZ = Math.max(1, world.getDepth() - 2 * margin);
            } else {
                rangeMin = 0; // not used for offset calculation
                rangeMaxX = 1000;
                rangeMaxZ = 1000;
            }
            for (int attempt = 0; attempt < maxAttempts; attempt++) {
                int rx, rz;
                if (finite) {
                    rx = rangeMin + rng.nextInt(rangeMaxX);
                    rz = rangeMin + rng.nextInt(rangeMaxZ);
                } else {
                    // Random offset ±500 from sender position
                    rx = (int) sender.getDoubleX() + rng.nextInt(rangeMaxX) - 500;
                    rz = (int) sender.getDoubleZ() + rng.nextInt(rangeMaxZ) - 500;
                }
                // Find the highest solid block (surface) at this column
                int surfaceY = -1;
                for (int y = worldH - 1; y >= 0; y--) {
                    byte block = finite ? world.getBlock(rx, y, rz)
                                        : chunkManager.getBlock(rx, y, rz);
                    if (block != 0) {
                        surfaceY = y;
                        break;
                    }
                }
                if (surfaceY < 0 || surfaceY >= worldH - 3) continue;
                int feetY = surfaceY + 1;
                // Check two blocks of air at feet and head level
                byte feetBlock = finite ? world.getBlock(rx, feetY, rz)
                                        : chunkManager.getBlock(rx, feetY, rz);
                if (feetBlock != 0) continue;
                byte headBlock = finite ? world.getBlock(rx, feetY + 1, rz)
                                        : chunkManager.getBlock(rx, feetY + 1, rz);
                if (headBlock != 0) continue;
                // Check air on all four sides at both feet and head height
                boolean sidesOk = true;
                int[][] sides = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
                for (int[] side : sides) {
                    int sx = rx + side[0], sz = rz + side[1];
                    if (finite && !world.inBounds(sx, feetY, sz)) {
                        sidesOk = false;
                        break;
                    }
                    byte sf = finite ? world.getBlock(sx, feetY, sz)
                                     : chunkManager.getBlock(sx, feetY, sz);
                    byte sh = finite ? world.getBlock(sx, feetY + 1, sz)
                                     : chunkManager.getBlock(sx, feetY + 1, sz);
                    if (sf != 0 || sh != 0) {
                        sidesOk = false;
                        break;
                    }
                }
                if (!sidesOk) continue;
                // Sky visibility check at the DESTINATION (not current pos).
                // Allows cave escapes — only the target needs open sky.
                boolean skyVisible = true;
                for (int y = feetY + 2; y < worldH; y++) {
                    byte above = finite ? world.getBlock(rx, y, rz)
                                        : chunkManager.getBlock(rx, y, rz);
                    if (above != 0) {
                        skyVisible = false;
                        break;
                    }
                }
                if (!skyVisible) continue;
                // Safe spot found — teleport
                double tx = rx + 0.5;
                double tFeetY = feetY;
                double tz = rz + 0.5;
                double eyeY = tFeetY + (double) 1.62f;
                playerManager.teleportPlayer(sender, tx, eyeY, tz,
                        sender.getFloatYaw(), sender.getFloatPitch(), chunkManager);
                ctx.reply("[Server] Teleported to " + rx + " " + feetY + " " + rz);
                return;
            }
            ctx.reply("[Server] Could not find a safe spot. Try again.");
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

        CommandRegistry.registerOp("tempban", "Temporarily ban a player", PermissionManager.OP_MANAGE, ctx -> {
            String[] args = ctx.getArgs();
            if (args.length < 2) {
                ctx.reply("Usage: tempban <player> <duration> (e.g., 30m, 2h, 1d)");
                return;
            }
            String target = args[0];
            String durationStr = args[1].toLowerCase();
            long durationMs;
            try {
                char unit = durationStr.charAt(durationStr.length() - 1);
                long value = Long.parseLong(durationStr.substring(0, durationStr.length() - 1));
                if (value <= 0) throw new NumberFormatException();
                switch (unit) {
                    case 'm': durationMs = value * 60 * 1000L; break;
                    case 'h': durationMs = value * 60 * 60 * 1000L; break;
                    case 'd': durationMs = value * 24 * 60 * 60 * 1000L; break;
                    default: throw new NumberFormatException();
                }
            } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                ctx.reply("Invalid duration. Use a number followed by m (minutes), h (hours), or d (days).");
                return;
            }
            BanManager.tempBanPlayer(target, durationMs);
            String formatted = BanManager.formatDuration(durationMs);
            ConnectedPlayer p = playerManager.getPlayerByName(target);
            if (p != null) {
                playerManager.kickPlayer(target, "Temporarily banned for " + formatted, world);
            }
            ctx.reply("Temporarily banned " + target + " for " + formatted);
            System.out.println("[INFO] " + ctx.getSenderName() + " temp-banned " + target
                    + " for " + formatted);
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
                wasBanned = BanManager.isIpBanned(arg);
                if (wasBanned) {
                    BanManager.unbanIp(arg);
                }
            } else {
                wasBanned = BanManager.isPlayerBanned(arg);
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

        CommandRegistry.registerOp("whitelist", "Manage the server whitelist", PermissionManager.OP_MANAGE, ctx -> {
            String[] args = ctx.getArgs();
            if (args.length == 0) {
                ctx.reply("Usage: whitelist <on|off|add|remove|list|reload>");
                return;
            }
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "on":
                    if (ServerProperties.isWhiteList()) {
                        ctx.reply("Whitelist is already enabled");
                        return;
                    }
                    ServerProperties.setWhiteList(true);
                    ServerProperties.save();
                    ctx.reply("Whitelist is now enabled");
                    System.out.println("[INFO] " + ctx.getSenderName() + " enabled the whitelist");
                    if (ServerProperties.isEnforceWhitelist()) {
                        int kicked = 0;
                        for (ConnectedPlayer p : new java.util.ArrayList<>(playerManager.getAllPlayers())) {
                            if (!WhitelistManager.isAllowed(p.getUsername())) {
                                System.out.println("[INFO] Kicking " + p.getUsername()
                                        + " (not white-listed, enforce-whitelist is on)");
                                playerManager.kickPlayer(p.getUsername(),
                                        "You are not white-listed on this server!", world);
                                kicked++;
                            }
                        }
                        if (kicked > 0) {
                            ctx.reply("Kicked " + kicked + " non-whitelisted player(s)");
                        }
                    }
                    break;
                case "off":
                    if (!ServerProperties.isWhiteList()) {
                        ctx.reply("Whitelist is already disabled");
                        return;
                    }
                    ServerProperties.setWhiteList(false);
                    ServerProperties.save();
                    ctx.reply("Whitelist is now disabled");
                    System.out.println("[INFO] " + ctx.getSenderName() + " disabled the whitelist");
                    break;
                case "add":
                    if (args.length < 2) {
                        ctx.reply("Usage: whitelist add <player>");
                        return;
                    }
                    if (WhitelistManager.addPlayer(args[1])) {
                        ctx.reply("Added " + args[1] + " to the whitelist");
                        System.out.println("[INFO] " + ctx.getSenderName()
                                + " added " + args[1] + " to whitelist");
                    } else {
                        ctx.reply(args[1] + " is already whitelisted");
                    }
                    break;
                case "remove":
                    if (args.length < 2) {
                        ctx.reply("Usage: whitelist remove <player>");
                        return;
                    }
                    if (WhitelistManager.removePlayer(args[1])) {
                        ctx.reply("Removed " + args[1] + " from the whitelist");
                        System.out.println("[INFO] " + ctx.getSenderName()
                                + " removed " + args[1] + " from whitelist");
                    } else {
                        ctx.reply(args[1] + " is not whitelisted");
                    }
                    break;
                case "list": {
                    java.util.Set<String> wl = WhitelistManager.getWhitelistedPlayers();
                    String status = ServerProperties.isWhiteList() ? "enabled" : "disabled";
                    if (wl.isEmpty()) {
                        ctx.reply("Whitelist is empty. Whitelist is " + status + ".");
                    } else {
                        java.util.ArrayList<String> sorted = new java.util.ArrayList<>(wl);
                        java.util.Collections.sort(sorted);
                        ctx.reply("Whitelisted (" + wl.size() + "): " + String.join(", ", sorted));
                        ctx.reply("Whitelist is " + status + ".");
                    }
                    break;
                }
                case "reload":
                    WhitelistManager.reload();
                    int reloadCount = WhitelistManager.getWhitelistedPlayers().size();
                    ctx.reply("Reloaded whitelist from file (" + reloadCount + " entries)");
                    System.out.println("[INFO] " + ctx.getSenderName()
                            + " reloaded the whitelist (" + reloadCount + " entries)");
                    break;
                default:
                    ctx.reply("Unknown subcommand. Usage: whitelist <on|off|add|remove|list|reload>");
                    break;
            }
        });

        CommandRegistry.registerOp("convert_to_rd_world",
                "Convert server world to original RubyDung level.dat",
                PermissionManager.OP_ADMIN, ctx -> {
            // Only available when the server uses RubyDung flat format
            if (protocolVersion != ProtocolVersion.RUBYDUNG) {
                ctx.reply("This command is only available when the server uses the RubyDung map format.");
                return;
            }

            File dir = getDataDir();
            File serverWorldFile = new File(dir, "server-world.dat");
            if (!serverWorldFile.exists()) {
                ctx.reply("No server-world.dat found — nothing to convert.");
                return;
            }

            File outputFile = new File(dir, "level.dat");
            if (outputFile.exists()) {
                ctx.reply("level.dat already exists. Delete or rename it first to avoid overwriting.");
                return;
            }

            ctx.reply("Converting server world to original RubyDung format...");
            try {
                new ServerToOriginalRubyDungConverter().convert(serverWorldFile, outputFile, 0L);
                ctx.reply("Done! level.dat has been created in the server directory.");
                ctx.reply("Players can download this file and place it in their RubyDung game folder to play the map locally.");
            } catch (Exception e) {
                ctx.reply("Conversion failed: " + e.getMessage());
                System.err.println("[convert_to_rd_world] " + e.getMessage());
                e.printStackTrace();
            }
        });

        // /trust — manage your trusted teammates (available to all players)
        CommandRegistry.register("trust", "Manage your trusted teammates", ctx -> {
            if (ctx.isConsole()) {
                ctx.reply("This command can only be used by players");
                return;
            }
            String[] args = ctx.getArgs();
            if (args.length == 0) {
                ctx.reply("Usage: /trust <add|remove|list> [player]");
                return;
            }
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "add":
                    if (args.length < 2) {
                        ctx.reply("Usage: /trust add <player>");
                        return;
                    }
                    String trustTarget = PlayerManager.sanitizeUsername(args[1]);
                    if (trustTarget.isEmpty() || trustTarget.equals("Player")) {
                        ctx.reply("[GriefProtection] Invalid player name");
                        return;
                    }
                    if (trustTarget.equalsIgnoreCase(ctx.getSenderName())) {
                        ctx.reply("[GriefProtection] You cannot add yourself to your trust list");
                        return;
                    }
                    if (TeamManager.addTeammate(ctx.getSenderName(), trustTarget)) {
                        ctx.reply("[GriefProtection] Added " + trustTarget + " to your trust list."
                                + " They can now break your blocks without triggering grief protection.");
                        // Notify the trusted player so they can reciprocate
                        TeamManager.setLastTruster(trustTarget, ctx.getSenderName());
                        ConnectedPlayer target = playerManager.getPlayerByName(trustTarget);
                        if (target != null) {
                            playerManager.sendChat(target,
                                    "[GriefProtection] " + ctx.getSenderName()
                                    + " added you to their trust list."
                                    + " To trust them back, type: /trustback");
                        }
                    } else {
                        ctx.reply("[GriefProtection] " + trustTarget + " is already in your trust list");
                    }
                    break;
                case "remove":
                    if (args.length < 2) {
                        ctx.reply("Usage: /trust remove <player or number>");
                        return;
                    }
                    // Support numbered removal from /trust list
                    String removeTarget = args[1];
                    try {
                        int idx = Integer.parseInt(args[1]);
                        java.util.List<String> indexed = TeamManager.getTeammatesList(ctx.getSenderName());
                        if (idx < 1 || idx > indexed.size()) {
                            ctx.reply("Invalid number. Use /trust list to see valid numbers (1-" + indexed.size() + ").");
                            return;
                        }
                        removeTarget = indexed.get(idx - 1);
                    } catch (NumberFormatException ignored) {
                        // Not a number — treat as player name
                    }
                    if (TeamManager.removeTeammate(ctx.getSenderName(), removeTarget)) {
                        ctx.reply("[GriefProtection] Removed " + removeTarget + " from your trust list");
                    } else {
                        ctx.reply("[GriefProtection] " + removeTarget + " is not in your trust list");
                    }
                    break;
                case "list":
                    java.util.List<String> teammateList = TeamManager.getTeammatesList(ctx.getSenderName());
                    if (teammateList.isEmpty()) {
                        ctx.reply("[GriefProtection] Your trust list is empty. Use /trust add <player> to add someone.");
                    } else {
                        StringBuilder sb = new StringBuilder("[GriefProtection] Trusted players:");
                        for (int i = 0; i < teammateList.size(); i++) {
                            sb.append(" ").append(i + 1).append(". ").append(teammateList.get(i));
                        }
                        ctx.reply(sb.toString());
                    }
                    break;
                default:
                    ctx.reply("Usage: /trust <add|remove|list> [player]");
                    break;
            }
        });

        // /trustback — trust back the last player who trusted you
        CommandRegistry.register("trustback", "Trust back the last player who trusted you", ctx -> {
            if (ctx.isConsole()) {
                ctx.reply("This command can only be used by players");
                return;
            }
            String truster = TeamManager.getLastTruster(ctx.getSenderName());
            if (truster == null) {
                ctx.reply("[GriefProtection] Nobody has added you to their trust list yet.");
                return;
            }
            if (truster.equalsIgnoreCase(ctx.getSenderName())) {
                ctx.reply("[GriefProtection] Cannot trust yourself");
                return;
            }
            if (TeamManager.addTeammate(ctx.getSenderName(), truster)) {
                ctx.reply("[GriefProtection] Added " + truster + " to your trust list. Trust is now mutual!");
                ConnectedPlayer target = playerManager.getPlayerByName(truster);
                if (target != null) {
                    playerManager.sendChat(target,
                            "[GriefProtection] " + ctx.getSenderName()
                            + " trusted you back! Trust is now mutual.");
                }
            } else {
                ctx.reply("[GriefProtection] " + truster + " is already in your trust list");
            }
        });

        // /griefinfo — grief protection information (available to all players)
        // /griefinfo <player> — inspect another player's grief data (ops only)
        CommandRegistry.register("griefinfo", "Grief protection information", ctx -> {
            String[] args = ctx.getArgs();
            if (args.length > 0
                    && !ctx.isConsole()
                    && PermissionManager.getOpLevel(ctx.getSenderName()) >= PermissionManager.OP_BYPASS_SPAWN) {
                // Op inspecting another player
                String target = args[0];
                short targetId = BlockOwnerRegistry.getId(target);
                ctx.reply("[GriefProtection] === " + target + " ===");
                if (targetId == 0) {
                    ctx.reply("[GriefProtection] No block ownership data found for " + target);
                } else {
                    ctx.reply("[GriefProtection] Owner ID: " + targetId);
                    ctx.reply("[GriefProtection] Budget: "
                            + BlockOwnerRegistry.getRemainingBudget(target)
                            + " / " + BlockOwnerRegistry.getTotalBudget(target) + " blocks");
                    ctx.reply("[GriefProtection] Protected blocks placed: "
                            + BlockOwnerRegistry.getUsedBlocks(target));
                    int daysLeft = BlockOwnerRegistry.getDaysUntilExpiry(target);
                    if (daysLeft >= 0) {
                        ctx.reply("[GriefProtection] Expiry: " + (daysLeft == 0 ? "EXPIRED" : daysLeft + " day(s) until expiry"));
                    }
                }
                return;
            }
            // Self-info (available to all players)
            String self = ctx.isConsole() ? "console" : ctx.getSenderName();
            ctx.reply("");
            ctx.reply("[GriefProtection] === Grief Protection ===");
            ctx.reply("[GriefProtection] Blocks you place are protected.");
            ctx.reply("[GriefProtection] Breaking others' blocks gives grief points.");
            ctx.reply("");
            ctx.reply("[GriefProtection] Use \"/trust add <name>\" to allow <name> to break your blocks.");
            ctx.reply("[GriefProtection] Use /rtp to teleport away if trapped.");
            if (!ctx.isConsole()) {
                ctx.reply("");
                ctx.reply("[GriefProtection] Budget: "
                        + BlockOwnerRegistry.getRemainingBudget(self)
                        + " / " + BlockOwnerRegistry.getTotalBudget(self)
                        + " blocks. Grows as you play.");
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
        // Initialize logging — tee stdout/stderr to logs/latest.log
        ServerLogger.init();

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
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.stop();
            ServerLogger.close();
        }, "RDForward-Shutdown"));

        try {
            server.start();
            server.runConsole();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
