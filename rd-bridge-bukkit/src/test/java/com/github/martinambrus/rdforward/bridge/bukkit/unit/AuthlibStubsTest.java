package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Locks in the shape of the {@code com.mojang.authlib.*} stubs the
 * bridge ships so plugins that probe authlib via reflection
 * (Citizens 2.0.42 NMS clinit is the reason the stubs exist) keep
 * resolving the expected accessors. Each assertion documents one
 * plugin-visible hook.
 */
class AuthlibStubsTest {

    @Test
    void gameProfileExposesIdNameAndPropertyMap() {
        UUID id = UUID.randomUUID();
        GameProfile profile = new GameProfile(id, "alex");
        assertSame(id, profile.getId());
        assertEquals("alex", profile.getName());
        assertNotNull(profile.getProperties(),
                "properties map must be non-null — Citizens iterates it");
        assertTrue(profile.isComplete());
        assertFalse(profile.isLegacy());
    }

    @Test
    void gameProfileIncompleteWhenIdOrNameMissing() {
        assertFalse(new GameProfile(null, "alex").isComplete());
        assertFalse(new GameProfile(UUID.randomUUID(), "").isComplete());
    }

    @Test
    void gameProfileRepositoryIsAnInterface() {
        // Plugins compile against the interface symbol; concrete
        // impls in plugin code must extend / implement it.
        assertTrue(GameProfileRepository.class.isInterface());
    }

    @Test
    void agentMinecraftSingletonIsPresent() {
        assertNotNull(Agent.MINECRAFT,
                "plugins look up Agent.MINECRAFT directly");
        assertEquals("Minecraft", Agent.MINECRAFT.getName());
        assertTrue(Agent.MINECRAFT.getVersion() >= 1);
    }

    @Test
    void profileLookupCallbackIsAnInterface() {
        assertTrue(ProfileLookupCallback.class.isInterface());
    }

    @Test
    void propertyAccessorsReturnConstructorArguments() {
        Property p = new Property("textures", "VAL", "SIG");
        assertEquals("textures", p.getName());
        assertEquals("VAL", p.getValue());
        assertEquals("SIG", p.getSignature());
        assertTrue(p.hasSignature());
        // Stub accepts every signature — no real Yggdrasil chain.
        assertTrue(p.isSignatureValid(null));
    }

    @Test
    void propertyMapInstantiates() {
        // Guava ForwardingMultimap is on testRuntimeOnly only — compile-time
        // assertions about size()/get() would need guava on testCompileOnly.
        // The shape contract is that PropertyMap is constructible and
        // implements the same supertype real authlib uses, so plugins
        // compiled against authlib resolve it without rewrite.
        assertNotNull(new PropertyMap());
    }

    @Test
    void minecraftClientCarriesNonNullLogger() {
        // Citizens 2.0.42 NMS clinit reflectively overwrites this
        // field with a NOPLogger. The bridge pre-initialises it to
        // a non-null NOPLogger so plugins that fail to find / write
        // the field still observe a usable logger.
        assertNotNull(MinecraftClient.LOGGER);
    }
}
