package com.github.martinambrus.rdforward.api.world;

/**
 * Per-world coercion rule applied at the universal block-write
 * chokepoint. The world consults its policy on every {@code setBlock}
 * to decide what actually gets stored — letting the world reject or
 * substitute blocks that aren't part of its supported vocabulary
 * (RubyDung worlds replace non-grass-layer blocks with cobblestone;
 * older protocol-version worlds map "future" blocks down to their
 * nearest equivalent in the world's vocabulary; modern worlds use
 * {@link #IDENTITY}).
 *
 * <p>Coercion happens for writes only — chunk-load and persistence
 * paths bypass it so on-disk world state is preserved verbatim.
 *
 * <p>Implementations must be thread-safe; {@code coerce} can be called
 * concurrently from any thread that issues a block write (Netty
 * gameplay handlers, mod-driven placement, scheduler tasks).
 */
@FunctionalInterface
public interface BlockPolicy {

    /**
     * Decide what block actually gets placed at {@code (x,y,z)} given a
     * request for {@code requested}. May return {@code requested}
     * unchanged (identity) or substitute. Position-aware policies
     * (e.g. RubyDung's grass-layer rule) inspect the coordinates;
     * type-only policies ignore them.
     */
    BlockType coerce(int x, int y, int z, BlockType requested);

    /** Identity passthrough — returns {@code requested} verbatim. The
     *  default policy for worlds that haven't declared one and for
     *  protocol families RDForward hasn't authored replacement maps
     *  for yet. */
    BlockPolicy IDENTITY = (x, y, z, requested) -> requested;
}
