package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test for HomeSpawnPlus's CommandRegister, which calls
 * {@code commandMap.register("hsp", pc)} where "hsp" is the fallback
 * prefix, NOT the primary label. The 2-arg register must use
 * {@code command.getName()} as the primary label.
 */
class SimpleCommandMapRegisterTest {

    private static Command cmd(String name) {
        return new Command(name) {};
    }

    @Test
    void registerTwoArgUsesCommandNameAsLabel() {
        SimpleCommandMap map = new SimpleCommandMap();
        Command home = cmd("home");
        map.register("hsp", home);
        assertNotNull(map.getCommand("home"),
                "register('hsp', homeCmd) must store under 'home', not 'hsp'");
    }

    @Test
    void registerTwoArgDoesNotStoreUnderFallbackPrefix() {
        SimpleCommandMap map = new SimpleCommandMap();
        map.register("hsp", cmd("home"));
        assertNull(map.getCommand("hsp"),
                "fallbackPrefix must not become the primary label");
    }

    @Test
    void registerTwoArgRegistersPrefixedAliasWhenPrimaryTaken() {
        SimpleCommandMap map = new SimpleCommandMap();
        map.register("hsp", cmd("home"));
        map.register("other", cmd("home"));
        assertNotNull(map.getCommand("home"));
        assertNotNull(map.getCommand("other:home"),
                "second registration must fall back to prefixed alias");
    }

    @Test
    void registerTwoArgReturnsTrueOnFirstRegistration() {
        SimpleCommandMap map = new SimpleCommandMap();
        assertTrue(map.register("hsp", cmd("home")));
    }

    @Test
    void registerTwoArgReturnsFalseWhenLabelTaken() {
        SimpleCommandMap map = new SimpleCommandMap();
        map.register("hsp", cmd("home"));
        assertFalse(map.register("other", cmd("home")));
    }

    @Test
    void registerTwoArgNullCommandReturnsFalse() {
        SimpleCommandMap map = new SimpleCommandMap();
        assertFalse(map.register("hsp", null));
    }

    @Test
    void registerThreeArgWithExplicitLabel() {
        SimpleCommandMap map = new SimpleCommandMap();
        Command home = cmd("home");
        map.register("customLabel", "hsp", home);
        assertNotNull(map.getCommand("customLabel"),
                "3-arg register must use the explicit label");
    }

    @Test
    void registerSetsCommandLabel() {
        SimpleCommandMap map = new SimpleCommandMap();
        Command home = cmd("home");
        map.register("hsp", home);
        assertEquals("home", home.getLabel(),
                "register must set the command label to the primary key");
    }

    @Test
    void registerAliasesAreRegistered() {
        SimpleCommandMap map = new SimpleCommandMap();
        Command home = cmd("home");
        home.setAliases(List.of("h", "residence"));
        map.register("hsp", home);
        assertNotNull(map.getCommand("h"));
        assertNotNull(map.getCommand("residence"));
    }

    @Test
    void registerAllDelegatesToTwoArgRegister() {
        SimpleCommandMap map = new SimpleCommandMap();
        Command a = cmd("alpha");
        Command b = cmd("beta");
        map.registerAll("mod", List.of(a, b));
        assertNotNull(map.getCommand("alpha"));
        assertNotNull(map.getCommand("beta"));
    }
}
