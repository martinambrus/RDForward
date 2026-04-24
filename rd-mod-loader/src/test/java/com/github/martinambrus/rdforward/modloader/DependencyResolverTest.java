package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DependencyResolverTest {

    private static ModContainer mod(String id, String version, Map<String, String> deps) {
        return mod(id, version, deps, Map.of());
    }

    private static ModContainer mod(String id, String version,
                                    Map<String, String> deps, Map<String, String> softDeps) {
        ModDescriptor d = new ModDescriptor(
                id, id, version, "", List.of(), "",
                Map.of(), deps, softDeps, List.of(),
                false, null, null);
        return new ModContainer(d, Path.of("/tmp/" + id + ".jar"));
    }

    private static List<String> ids(List<ModContainer> cs) {
        return cs.stream().map(ModContainer::id).toList();
    }

    @Test
    void emptyInputReturnsEmptyList() throws DependencyResolver.ResolutionException {
        assertTrue(DependencyResolver.resolve(List.of()).isEmpty());
    }

    @Test
    void singleModWithNoDepsIsPassedThrough() throws DependencyResolver.ResolutionException {
        List<ModContainer> out = DependencyResolver.resolve(List.of(mod("solo", "1.0", Map.of())));
        assertEquals(List.of("solo"), ids(out));
    }

    @Test
    void dependentLoadsAfterDependency() throws DependencyResolver.ResolutionException {
        ModContainer core = mod("core", "1.0", Map.of());
        ModContainer addon = mod("addon", "1.0", Map.of("core", "*"));
        List<ModContainer> out = DependencyResolver.resolve(List.of(addon, core));
        assertEquals(List.of("core", "addon"), ids(out));
    }

    @Test
    void missingDependencyThrows() {
        ModContainer addon = mod("addon", "1.0", Map.of("core", "*"));
        DependencyResolver.ResolutionException ex = assertThrows(
                DependencyResolver.ResolutionException.class,
                () -> DependencyResolver.resolve(List.of(addon)));
        assertTrue(ex.getMessage().contains("core"));
    }

    @Test
    void versionMismatchThrows() {
        ModContainer core = mod("core", "1.0", Map.of());
        ModContainer addon = mod("addon", "1.0", Map.of("core", ">=2.0"));
        DependencyResolver.ResolutionException ex = assertThrows(
                DependencyResolver.ResolutionException.class,
                () -> DependencyResolver.resolve(List.of(core, addon)));
        assertTrue(ex.getMessage().contains("found version 1.0"));
    }

    @Test
    void cycleDetected() {
        ModContainer a = mod("a", "1.0", Map.of("b", "*"));
        ModContainer b = mod("b", "1.0", Map.of("a", "*"));
        DependencyResolver.ResolutionException ex = assertThrows(
                DependencyResolver.ResolutionException.class,
                () -> DependencyResolver.resolve(List.of(a, b)));
        assertTrue(ex.getMessage().contains("circular"));
    }

    @Test
    void duplicateIdThrows() {
        ModContainer a1 = mod("same", "1.0", Map.of());
        ModContainer a2 = mod("same", "2.0", Map.of());
        assertThrows(DependencyResolver.ResolutionException.class,
                () -> DependencyResolver.resolve(List.of(a1, a2)));
    }

    @Test
    void independentModsOrderedAlphabetically() throws DependencyResolver.ResolutionException {
        List<ModContainer> input = new ArrayList<>();
        input.add(mod("zulu", "1.0", Map.of()));
        input.add(mod("alpha", "1.0", Map.of()));
        input.add(mod("mike", "1.0", Map.of()));
        List<ModContainer> out = DependencyResolver.resolve(input);
        assertEquals(List.of("alpha", "mike", "zulu"), ids(out));
    }

    @Test
    void chainDependencyProducesLinearOrder() throws DependencyResolver.ResolutionException {
        ModContainer c = mod("c", "1.0", Map.of("b", "*"));
        ModContainer b = mod("b", "1.0", Map.of("a", "*"));
        ModContainer a = mod("a", "1.0", Map.of());
        List<ModContainer> out = DependencyResolver.resolve(List.of(c, b, a));
        assertEquals(List.of("a", "b", "c"), ids(out));
    }

    @Test
    void softDependencyInfluencesOrder() throws DependencyResolver.ResolutionException {
        ModContainer core = mod("core", "1.0", Map.of(), Map.of());
        ModContainer addon = mod("addon", "1.0", Map.of(), Map.of("core", "*"));
        List<ModContainer> out = DependencyResolver.resolve(List.of(addon, core));
        assertEquals(List.of("core", "addon"), ids(out));
    }

    @Test
    void missingSoftDependencyDoesNotAbort() throws DependencyResolver.ResolutionException {
        ModContainer addon = mod("addon", "1.0", Map.of(), Map.of("ghost", "*"));
        List<ModContainer> out = DependencyResolver.resolve(List.of(addon));
        assertEquals(List.of("addon"), ids(out));
    }
}
