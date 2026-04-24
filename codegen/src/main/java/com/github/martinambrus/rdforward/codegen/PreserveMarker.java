package com.github.martinambrus.rdforward.codegen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Detects whether a hand-maintained bridge stub carries the
 * {@code @rdforward:preserve} header marker. The generator never
 * overwrites a file that is preserved.
 *
 * <p>The marker may appear in any form (line comment, block comment,
 * javadoc) as long as the literal substring
 * {@code @rdforward:preserve} occurs within the first
 * {@value #DEFAULT_MAX_HEADER_LINES} lines of the file.
 *
 * <p>Rationale for a header-only scan: generated stub files are often
 * 10k+ lines for large upstream APIs; scanning the entire file on
 * every regeneration would be wasteful, and the preserve contract is
 * already a header decoration.
 */
public final class PreserveMarker {

    public static final String MARKER = "@rdforward:preserve";

    public static final int DEFAULT_MAX_HEADER_LINES = 20;

    private PreserveMarker() {}

    public static boolean isPreserved(Path path) throws IOException {
        if (path == null || !Files.isRegularFile(path)) return false;
        try (BufferedReader r = Files.newBufferedReader(path)) {
            return containsMarker(r, DEFAULT_MAX_HEADER_LINES);
        }
    }

    public static boolean containsMarkerIn(String content) {
        if (content == null || content.isEmpty()) return false;
        try (BufferedReader r = new BufferedReader(new StringReader(content))) {
            return containsMarker(r, DEFAULT_MAX_HEADER_LINES);
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean containsMarker(BufferedReader reader, int maxLines) throws IOException {
        for (int i = 0; i < maxLines; i++) {
            String line = reader.readLine();
            if (line == null) return false;
            if (line.contains(MARKER)) return true;
        }
        return false;
    }
}
