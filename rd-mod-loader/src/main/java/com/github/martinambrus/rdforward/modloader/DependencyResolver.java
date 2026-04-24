package com.github.martinambrus.rdforward.modloader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Topologically orders {@link ModContainer}s so that each mod is
 * initialised after every mod it hard-depends on. Ties at the same depth
 * are broken alphabetically to keep load order deterministic.
 *
 * <p>Hard dependency violations (missing mod, version mismatch, cycles)
 * throw {@link ResolutionException}. Soft dependencies only influence
 * order — missing or out-of-range soft deps do not abort the load.
 */
public final class DependencyResolver {

    private DependencyResolver() {}

    /**
     * Return the containers in a valid enable order.
     *
     * @throws ResolutionException if a hard dependency is missing,
     *     declares an incompatible version range, or if a cycle exists
     */
    public static List<ModContainer> resolve(Collection<ModContainer> containers) throws ResolutionException {
        Map<String, ModContainer> byId = new LinkedHashMap<>();
        for (ModContainer c : containers) {
            if (byId.put(c.id(), c) != null) {
                throw new ResolutionException("duplicate mod id: " + c.id());
            }
        }

        for (ModContainer c : byId.values()) {
            for (Map.Entry<String, String> dep : c.descriptor().dependencies().entrySet()) {
                ModContainer target = byId.get(dep.getKey());
                if (target == null) {
                    throw new ResolutionException(
                            c.id() + " requires " + dep.getKey() + " " + dep.getValue()
                                    + " but it is not installed");
                }
                if (!VersionRange.matches(dep.getValue(), target.descriptor().version())) {
                    throw new ResolutionException(
                            c.id() + " requires " + dep.getKey() + " " + dep.getValue()
                                    + " but found version " + target.descriptor().version());
                }
            }
        }

        Map<String, Set<String>> edges = new HashMap<>();
        Map<String, Integer> indegree = new HashMap<>();
        for (String id : byId.keySet()) {
            edges.put(id, new HashSet<>());
            indegree.put(id, 0);
        }
        for (ModContainer c : byId.values()) {
            for (String depId : c.descriptor().dependencies().keySet()) {
                if (edges.get(depId).add(c.id())) {
                    indegree.merge(c.id(), 1, Integer::sum);
                }
            }
            for (String softId : c.descriptor().softDependencies().keySet()) {
                if (byId.containsKey(softId) && edges.get(softId).add(c.id())) {
                    indegree.merge(c.id(), 1, Integer::sum);
                }
            }
        }

        List<ModContainer> sorted = new ArrayList<>(byId.size());
        List<String> ready = new ArrayList<>();
        for (Map.Entry<String, Integer> e : indegree.entrySet()) {
            if (e.getValue() == 0) ready.add(e.getKey());
        }

        while (!ready.isEmpty()) {
            ready.sort(Comparator.naturalOrder());
            String next = ready.remove(0);
            sorted.add(byId.get(next));
            for (String child : edges.get(next)) {
                int newDeg = indegree.merge(child, -1, Integer::sum);
                if (newDeg == 0) ready.add(child);
            }
        }

        if (sorted.size() != byId.size()) {
            Set<String> remaining = new HashSet<>(byId.keySet());
            sorted.forEach(c -> remaining.remove(c.id()));
            throw new ResolutionException("circular dependency involving: " + remaining);
        }

        return sorted;
    }

    /** Thrown when the declared dependency graph cannot be satisfied. */
    public static final class ResolutionException extends Exception {
        public ResolutionException(String message) { super(message); }
    }
}
