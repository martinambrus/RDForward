package com.github.martinambrus.rdforward.server.world;

import com.github.martinambrus.rdforward.api.world.BlockPolicy;
import com.github.martinambrus.rdforward.api.world.BlockType;
import com.github.martinambrus.rdforward.api.world.BlockTypes;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-protocol-version block policy. Coerces blocks introduced in
 * versions newer than {@link #target} down to the nearest equivalent
 * in {@code target}'s vocabulary, walking single-hop replacements
 * registered in {@link BlockReplacementRegistry}.
 *
 * <p>Resolutions are memoized per policy instance, so the chain walk
 * is paid at most once per (target version, source block) pair. The
 * memo is bounded by the total number of distinct {@link BlockType}s
 * ever asked about — small hundreds in practice.
 *
 * <p>Cycle protection is implicit: a {@code Set<String> visited} is
 * tracked during each chain walk and the loop terminates when a name
 * recurs. On chain exhaustion (no replacement found) or cycle, the
 * policy falls back to {@link BlockTypes#COBBLE} — a universally safe
 * block present even in RubyDung's vocabulary.
 */
public final class VersionedBlockPolicy implements BlockPolicy {

    private final ProtocolVersion target;
    private final ConcurrentHashMap<BlockType, BlockType> memo = new ConcurrentHashMap<>();

    public VersionedBlockPolicy(ProtocolVersion target) {
        if (target == null) throw new IllegalArgumentException("target version required");
        this.target = target;
    }

    @Override
    public BlockType coerce(int x, int y, int z, BlockType requested) {
        if (requested == null || requested.isAir()) return BlockTypes.AIR;
        return memo.computeIfAbsent(requested, this::resolve);
    }

    private BlockType resolve(BlockType requested) {
        String name = requested.getName();
        if (name == null) return BlockTypes.COBBLE;
        if (!BlockReplacementRegistry.introducedAfter(name, target)) {
            // Already in this version's vocabulary — pass through.
            return requested;
        }
        Set<String> visited = new HashSet<>();
        String current = name;
        while (visited.add(current)) {
            String prior = findPriorAcrossVersions(current);
            if (prior == null) {
                // Chain exhausted without reaching the target's vocabulary.
                return BlockTypes.COBBLE;
            }
            current = prior;
            if (!BlockReplacementRegistry.introducedAfter(current, target)) {
                return BlockTypesLookup.byName(current);
            }
        }
        // Cycle detected — visited.add returned false.
        return BlockTypes.COBBLE;
    }

    /** Walk every {@link ProtocolVersion} in chronological order looking
     *  for the file that registered {@code blockName} as introduced;
     *  return the immediate-prior replacement that file declares. The
     *  registry caches each version's file at most once, so repeated
     *  calls are cheap after the first resolution. */
    private static String findPriorAcrossVersions(String blockName) {
        for (ProtocolVersion v : ProtocolVersion.values()) {
            String prior = BlockReplacementRegistry.getReplacement(v, blockName);
            if (prior != null) return prior;
        }
        return null;
    }

    /** @return the target version this policy coerces toward. */
    public ProtocolVersion getTarget() { return target; }
}
