package com.github.martinambrus.rdforward.client.mod.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the pure Java2D portion of RDDrawContext — text metrics — without
 * requiring an OpenGL context. Rasterization and drawing paths call into
 * GL immediate mode and are exercised end-to-end in the game/e2e suites.
 */
class RDDrawContextTextMetricsTest {

    private final RDDrawContext ctx = new RDDrawContext(1024, 768);

    @Test
    void getScreenDimensionsMirrorConstructor() {
        assertEquals(1024, ctx.getScreenWidth());
        assertEquals(768, ctx.getScreenHeight());
    }

    @Test
    void getTextWidthEmptyReturnsZero() {
        assertEquals(0, ctx.getTextWidth(""));
        assertEquals(0, ctx.getTextWidth(null));
    }

    @Test
    void getTextWidthScalesWithLength() {
        int narrow = ctx.getTextWidth("x");
        int wide = ctx.getTextWidth("xxxxxxxxxxxx");
        assertTrue(wide > narrow, "longer strings must produce wider measurements");
        assertTrue(narrow > 0, "a single glyph must have non-zero width");
    }

    @Test
    void getTextHeightIsPositiveAndConstant() {
        int h1 = ctx.getTextHeight();
        int h2 = ctx.getTextHeight();
        assertEquals(h1, h2);
        assertTrue(h1 > 0);
    }
}
