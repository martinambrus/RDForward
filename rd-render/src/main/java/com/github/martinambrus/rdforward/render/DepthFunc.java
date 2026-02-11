package com.github.martinambrus.rdforward.render;

/** Depth comparison functions used by {@link RDGraphics#depthFunc}. */
public enum DepthFunc {
    NEVER,
    LESS,
    EQUAL,
    LEQUAL,
    GREATER,
    NOTEQUAL,
    GEQUAL,
    ALWAYS
}
