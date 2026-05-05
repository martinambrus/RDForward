package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers NMS (net.minecraft.server) and OBC (org.bukkit.craftbukkit) stubs
 * added for CS-CoreLib compatibility. The plugin uses reflection
 * ({@code Class.forName}) to resolve these classes at runtime; they must
 * exist on the classpath with the expected methods and fields.
 */
class NmsAndObcStubsTest {

    // --- OBC stubs ---

    @Test
    void obcCraftWorldExistsAndHasGetHandle() throws Exception {
        Class<?> cls = Class.forName("org.bukkit.craftbukkit.v1_21_R1.CraftWorld");
        assertNotNull(cls.getMethod("getHandle"));
    }

    @Test
    void obcCraftPlayerExistsAndHasGetHandle() throws Exception {
        Class<?> cls = Class.forName("org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer");
        assertNotNull(cls.getMethod("getHandle"));
    }

    @Test
    void obcCraftEntityExistsAndHasGetHandle() throws Exception {
        Class<?> cls = Class.forName("org.bukkit.craftbukkit.v1_21_R1.entity.CraftEntity");
        assertNotNull(cls.getMethod("getHandle"));
    }

    @Test
    void obcCraftAnimalsExistsAndHasGetHandle() throws Exception {
        Class<?> cls = Class.forName("org.bukkit.craftbukkit.v1_21_R1.entity.CraftAnimals");
        assertNotNull(cls.getMethod("getHandle"));
    }

    @Test
    void obcCraftItemStackExistsAndHasAsNMSCopy() throws Exception {
        Class<?> cls = Class.forName("org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack");
        assertNotNull(cls.getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class));
    }

    // --- NMS stubs ---

    @Test
    void nmsEnumHandExistsWithMainHand() throws Exception {
        Class<?> cls = Class.forName("net.minecraft.server.v1_21_R1.EnumHand");
        assertTrue(cls.isEnum());
        Object[] constants = cls.getEnumConstants();
        assertTrue(constants.length >= 2, "EnumHand should have MAIN_HAND and OFF_HAND");
    }

    @Test
    void nmsEntityPlayerExistsWithPlayerConnectionField() throws Exception {
        Class<?> cls = Class.forName("net.minecraft.server.v1_21_R1.EntityPlayer");
        assertNotNull(cls.getField("playerConnection"));
    }

    @Test
    void nmsEntityPlayerHasOpenBookMethod() throws Exception {
        Class<?> cls = Class.forName("net.minecraft.server.v1_21_R1.EntityPlayer");
        Class<?> itemStack = Class.forName("net.minecraft.server.v1_21_R1.ItemStack");
        Class<?> enumHand = Class.forName("net.minecraft.server.v1_21_R1.EnumHand");
        // CS-CoreLib looks for "a" first, then "openBook"
        assertNotNull(cls.getMethod("a", itemStack, enumHand),
                "EntityPlayer must have NMS-mapped method 'a(ItemStack, EnumHand)'");
        assertNotNull(cls.getMethod("openBook", itemStack, enumHand),
                "EntityPlayer must have 'openBook(ItemStack, EnumHand)'");
    }

    @Test
    void nmsPlayerConnectionExistsWithSendPacket() throws Exception {
        Class<?> cls = Class.forName("net.minecraft.server.v1_21_R1.PlayerConnection");
        assertNotNull(cls.getMethod("sendPacket", Object.class));
    }

    @Test
    void nmsItemStackExists() throws Exception {
        assertNotNull(Class.forName("net.minecraft.server.v1_21_R1.ItemStack"));
    }

    // --- Spigot inner classes ---

    @Test
    void commandSenderSpigotIsInnerClass() {
        assertNotNull(CommandSender.Spigot.class);
        assertTrue(CommandSender.class.isAssignableFrom(
                CommandSender.Spigot.class.getDeclaringClass()),
                "Spigot should be declared inside CommandSender");
    }

    @Test
    void entitySpigotIsInnerClass() {
        assertNotNull(Entity.Spigot.class);
        assertEquals(Entity.class.getName(), Entity.Spigot.class.getDeclaringClass().getName());
    }

    @Test
    void playerSpigotIsInnerClass() {
        assertNotNull(Player.Spigot.class);
        assertEquals(Player.class.getName(), Player.Spigot.class.getDeclaringClass().getName());
    }

    @Test
    void lightningStrikeSpigotIsInnerClass() {
        assertNotNull(LightningStrike.Spigot.class);
        assertEquals(LightningStrike.class.getName(), LightningStrike.Spigot.class.getDeclaringClass().getName());
    }

    @Test
    void entitySpigotExtendsCommandSenderSpigot() {
        assertTrue(CommandSender.Spigot.class.isAssignableFrom(Entity.Spigot.class));
    }

    @Test
    void playerSpigotExtendsEntitySpigot() {
        assertTrue(Entity.Spigot.class.isAssignableFrom(Player.Spigot.class));
    }

    @Test
    void lightningStrikeSpigotExtendsEntitySpigot() {
        assertTrue(Entity.Spigot.class.isAssignableFrom(LightningStrike.Spigot.class));
    }

    @Test
    void commandSenderSpigotHasSendMessage() throws Exception {
        assertNotNull(CommandSender.Spigot.class.getMethod(
                "sendMessage", net.md_5.bungee.api.chat.BaseComponent.class));
    }

    @Test
    void playerSpigotHasGetPing() throws Exception {
        assertNotNull(Player.Spigot.class.getMethod("getPing"));
    }

    @Test
    void playerSpigotHasGetHiddenPlayers() throws Exception {
        assertNotNull(Player.Spigot.class.getMethod("getHiddenPlayers"));
    }

    @Test
    void playerSpigotGetPingReturnsZero() {
        Player.Spigot spigot = new Player.Spigot();
        assertEquals(Integer.valueOf(0), Integer.valueOf(spigot.getPing()));
    }

    @Test
    void lightningStrikeSpigotIsSilentReturnsFalse() {
        LightningStrike.Spigot spigot = new LightningStrike.Spigot();
        assertTrue(!spigot.isSilent());
    }
}
