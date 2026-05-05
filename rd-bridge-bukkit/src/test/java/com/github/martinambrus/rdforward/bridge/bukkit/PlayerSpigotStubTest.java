package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerSpigotStubTest {

    private StubRdServer rd;

    @BeforeEach void setUp() {
        rd = new StubRdServer();
        BukkitBridge.install(rd);
        rd.players.put("alice", new StubRdServer.StubRdPlayer("alice",
                new com.github.martinambrus.rdforward.api.world.Location(0, 64, 0)));
    }

    @AfterEach void tearDown() {
        BukkitBridge.uninstall();
    }

    private Player player() {
        return Bukkit.getServer().getPlayer("alice");
    }

    @Test
    void spigotReturnsNonNull() {
        assertNotNull(player().spigot());
    }

    @Test
    void spigotSendMessageSingleComponentDoesNotThrow() {
        player().spigot().sendMessage(new TextComponent("hello"));
    }

    @Test
    void spigotSendMessageArrayOfComponentsDoesNotThrow() {
        player().spigot().sendMessage(new net.md_5.bungee.api.chat.BaseComponent[]{
                new TextComponent("hello"),
                new TextComponent(" world")});
    }
}
