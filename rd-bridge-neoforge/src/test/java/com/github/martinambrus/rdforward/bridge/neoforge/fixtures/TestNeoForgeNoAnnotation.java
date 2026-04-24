package com.github.martinambrus.rdforward.bridge.neoforge.fixtures;

/**
 * Fixture NeoForge mod without an {@code @Mod} annotation. Loader must
 * pick it up via the {@code mainClass} field declared in the fixture's
 * {@code neoforge.mods.toml}. Default no-arg constructor is intentional —
 * exercising that branch of {@code NeoForgeModLoader.instantiate}.
 */
public class TestNeoForgeNoAnnotation {

    public static final String PROP_CTOR = "rdforward.test.neoforge.no_annotation_ctor";

    public TestNeoForgeNoAnnotation() {
        System.setProperty(PROP_CTOR, "true");
    }
}
