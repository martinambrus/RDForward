package com.github.martinambrus.rdforward.render;

/** Texture sampling modes used by {@link RDGraphics#loadTexture}. */
public enum TextureFilter {
    /** Nearest-neighbour (pixelated). */
    NEAREST,
    /** Bilinear interpolation (smooth). */
    LINEAR,
    /** Nearest with mipmap selection. */
    NEAREST_MIPMAP_NEAREST,
    /** Linear with mipmap interpolation. */
    LINEAR_MIPMAP_LINEAR
}
