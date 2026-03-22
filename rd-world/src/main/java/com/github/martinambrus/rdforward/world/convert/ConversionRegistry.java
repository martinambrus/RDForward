package com.github.martinambrus.rdforward.world.convert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Registry of format converters with automatic path-finding between formats.
 *
 * Converters are registered as single-step transformations (e.g. RUBYDUNG to ALPHA).
 * When a multi-step conversion is needed (e.g. RUBYDUNG to MCREGION), the registry
 * finds the shortest path via BFS and chains the converters automatically, using
 * temporary directories for intermediate results.
 */
public class ConversionRegistry {

    private final List<FormatConverter> converters = new ArrayList<>();

    /**
     * Register a converter.
     */
    public void register(FormatConverter converter) {
        converters.add(converter);
    }

    /**
     * Find the shortest conversion path from source to target format.
     *
     * @return ordered list of converters to apply, or empty list if no path exists
     */
    public List<FormatConverter> findPath(WorldFormat source, WorldFormat target) {
        if (source == target) {
            return Collections.emptyList();
        }

        // Build adjacency map: format -> list of converters that start from it
        Map<WorldFormat, List<FormatConverter>> adjacency = new HashMap<>();
        for (FormatConverter c : converters) {
            adjacency.computeIfAbsent(c.sourceFormat(), k -> new ArrayList<>()).add(c);
        }

        // BFS to find shortest path
        Queue<WorldFormat> queue = new ArrayDeque<>();
        Map<WorldFormat, FormatConverter> cameFrom = new HashMap<>();
        Set<WorldFormat> visited = new HashSet<>();

        queue.add(source);
        visited.add(source);

        while (!queue.isEmpty()) {
            WorldFormat current = queue.poll();

            List<FormatConverter> edges = adjacency.get(current);
            if (edges == null) {
                continue;
            }

            for (FormatConverter edge : edges) {
                WorldFormat next = edge.targetFormat();
                if (visited.contains(next)) {
                    continue;
                }

                cameFrom.put(next, edge);

                if (next == target) {
                    // Reconstruct path
                    List<FormatConverter> path = new ArrayList<>();
                    WorldFormat step = target;
                    while (cameFrom.containsKey(step)) {
                        FormatConverter c = cameFrom.get(step);
                        path.add(c);
                        step = c.sourceFormat();
                    }
                    Collections.reverse(path);
                    return path;
                }

                visited.add(next);
                queue.add(next);
            }
        }

        return Collections.emptyList();
    }

    /**
     * Convert a world from source format to target format, chaining converters
     * as needed via temporary directories for intermediate steps.
     *
     * @param input       source world file or directory
     * @param output      target directory for the final converted world
     * @param source      source world format
     * @param target      target world format
     * @param seed        world seed for level.dat (passed to each converter)
     * @throws IOException if conversion fails
     * @throws IllegalArgumentException if no conversion path exists
     */
    public void convert(File input, File output, WorldFormat source, WorldFormat target,
                         long seed) throws IOException {
        List<FormatConverter> path = findPath(source, target);
        if (path.isEmpty()) {
            if (source == target) {
                return; // No-op: already in the right format
            }
            throw new IllegalArgumentException(
                    "No conversion path from " + source + " to " + target);
        }

        if (path.size() == 1) {
            path.get(0).convert(input, output, seed);
            return;
        }

        // Multi-step conversion: use temp directories for intermediate results
        List<File> tempDirs = new ArrayList<>();
        try {
            File currentInput = input;
            for (int i = 0; i < path.size(); i++) {
                FormatConverter converter = path.get(i);
                boolean isLast = (i == path.size() - 1);
                File currentOutput;

                if (isLast) {
                    currentOutput = output;
                } else {
                    currentOutput = new File(output.getParentFile(),
                            ".rdforward-convert-temp-" + System.currentTimeMillis() + "-" + i);
                    tempDirs.add(currentOutput);
                }

                converter.convert(currentInput, currentOutput, seed);
                currentInput = currentOutput;
            }
        } finally {
            // Clean up temp directories
            for (File tempDir : tempDirs) {
                deleteRecursive(tempDir);
            }
        }
    }

    /**
     * Create a registry pre-populated with all built-in converters.
     */
    public static ConversionRegistry createDefault() {
        ConversionRegistry registry = new ConversionRegistry();
        registry.register(new OriginalRubyDungToServerConverter());
        registry.register(new ServerToOriginalRubyDungConverter());
        registry.register(new RubyDungToAlphaConverter());
        registry.register(new McRegionWriter());
        return registry;
    }

    private static void deleteRecursive(File file) {
        if (java.nio.file.Files.isSymbolicLink(file.toPath())) {
            file.delete();
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
        file.delete();
    }
}
