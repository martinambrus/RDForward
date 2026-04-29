package com.github.martinambrus.rdforward.modloader.impl;

import com.github.martinambrus.rdforward.api.inventory.PlayerInventoryView;
import com.github.martinambrus.rdforward.api.player.Player;
import com.github.martinambrus.rdforward.api.version.ProtocolVersion;
import com.github.martinambrus.rdforward.api.world.Location;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.api.PermissionManager;

import java.util.Objects;

/**
 * Adapter from {@link ConnectedPlayer} to {@link Player}. Binds back to
 * the owning {@link RDServer} so teleport/kick can delegate to
 * {@code PlayerManager} without dragging Netty-specific types into mods.
 */
public final class RDPlayer implements Player {

    private final ConnectedPlayer player;
    private final RDServer server;
    /** Lazy view; cached so plugins that hold onto Player.getInventory() see consistent state. */
    private volatile RDPlayerInventoryView inventoryView;

    public RDPlayer(ConnectedPlayer player, RDServer server) {
        this.player = Objects.requireNonNull(player, "player");
        this.server = Objects.requireNonNull(server, "server");
    }

    public ConnectedPlayer delegate() { return player; }

    @Override
    public String getName() { return player.getUsername(); }

    @Override
    public Location getLocation() {
        return new Location(
                "overworld",
                player.getDoubleX(), player.getDoubleY(), player.getDoubleZ(),
                player.getFloatYaw(), player.getFloatPitch()
        );
    }

    @Override
    public void teleport(Location location) {
        server.playerManager().teleportPlayer(
                player,
                location.x(), location.y(), location.z(),
                location.yaw(), location.pitch(),
                server.chunkManager()
        );
    }

    @Override
    public void sendMessage(String message) {
        ChatDispatch.send(player, message);
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return player.getProtocolVersion();
    }

    @Override
    public boolean isOp() {
        return PermissionManager.isOp(player.getUsername());
    }

    @Override
    public void kick(String reason) {
        player.disconnect();
    }

    @Override
    public PlayerInventoryView getInventory() {
        RDPlayerInventoryView v = inventoryView;
        if (v != null) return v;
        synchronized (this) {
            if (inventoryView == null) {
                inventoryView = new RDPlayerInventoryView(player, server.playerManager().getInventoryAdapter());
            }
            return inventoryView;
        }
    }

    @Override
    public java.net.InetSocketAddress getAddress() {
        io.netty.channel.Channel ch = player.getChannel();
        if (ch == null) return null;
        java.net.SocketAddress addr = ch.remoteAddress();
        return addr instanceof java.net.InetSocketAddress isa ? isa : null;
    }

    @Override
    public void setFlySpeed(float speed) { player.setFlySpeed(speed); }

    @Override
    public void setWalkSpeed(float speed) { player.setWalkSpeed(speed); }

    @Override
    public float getFlySpeed() { return player.getFlySpeed(); }

    @Override
    public float getWalkSpeed() { return player.getWalkSpeed(); }

    @Override
    public void setGameMode(int gameMode) { player.setGameMode(gameMode); }

    @Override
    public int getGameMode() { return player.getGameMode(); }
}
