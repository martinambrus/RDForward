package com.github.martinambrus.rdforward.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreserveMarkerTest {

    @Test
    void lineCommentMarkerIsDetected() {
        assertTrue(PreserveMarker.containsMarkerIn(
                "// @rdforward:preserve\npackage org.bukkit;\n"));
    }

    @Test
    void blockCommentMarkerIsDetected() {
        assertTrue(PreserveMarker.containsMarkerIn(
                "/* @rdforward:preserve */\npackage org.bukkit;\n"));
    }

    @Test
    void javadocMarkerIsDetected() {
        assertTrue(PreserveMarker.containsMarkerIn("""
                /**
                 * hand-tuned forwarder.
                 * @rdforward:preserve
                 */
                package org.bukkit;
                """));
    }

    @Test
    void fileWithoutMarkerIsNotPreserved() {
        assertFalse(PreserveMarker.containsMarkerIn("""
                package org.bukkit;
                public class Foo {}
                """));
    }

    @Test
    void markerBelowHeaderThresholdIsIgnored() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < PreserveMarker.DEFAULT_MAX_HEADER_LINES; i++) {
            sb.append("// padding line ").append(i).append('\n');
        }
        sb.append("// @rdforward:preserve\n");
        assertFalse(PreserveMarker.containsMarkerIn(sb.toString()),
                "marker past the header scan limit must not be treated as preserving");
    }

    @Test
    void emptyInputIsNotPreserved() {
        assertFalse(PreserveMarker.containsMarkerIn(""));
        assertFalse(PreserveMarker.containsMarkerIn(null));
    }

    @Test
    void markerInMiddleOfHeaderIsDetected() {
        assertTrue(PreserveMarker.containsMarkerIn("""
                package org.bukkit;

                // @rdforward:preserve
                // hand-tuned forwarder
                public class Foo {}
                """));
    }

    @Test
    void realFileWithMarkerIsPreserved(@TempDir Path tmp) throws IOException {
        Path f = tmp.resolve("Preserved.java");
        Files.writeString(f, "// @rdforward:preserve\npackage x;\nclass Preserved {}\n");
        assertTrue(PreserveMarker.isPreserved(f));
    }

    @Test
    void realFileWithoutMarkerIsNotPreserved(@TempDir Path tmp) throws IOException {
        Path f = tmp.resolve("Generated.java");
        Files.writeString(f, "package x;\nclass Generated {}\n");
        assertFalse(PreserveMarker.isPreserved(f));
    }

    @Test
    void nonExistentFileIsNotPreserved(@TempDir Path tmp) throws IOException {
        assertFalse(PreserveMarker.isPreserved(tmp.resolve("missing.java")));
    }

    @Test
    void directoryArgumentIsNotPreserved(@TempDir Path tmp) throws IOException {
        assertFalse(PreserveMarker.isPreserved(tmp));
    }

    @Test
    void nullPathIsNotPreserved() throws IOException {
        assertFalse(PreserveMarker.isPreserved(null));
    }
}
