package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.8.9 (protocol v47).
 * Verified by CFR decompilation of the 1.8.9 client JAR.
 *
 * Difference from 1.8.8: GameSettings (avh) gained another boolean field (X)
 * before the key bindings, shifting all key binding field names by one more letter.
 */
public class NettyReleaseV47_189Mappings extends NettyReleaseV47_188Mappings {
    @Override public String forwardKeyFieldName() { return "Y"; }
    @Override public String leftKeyFieldName() { return "Z"; }
    @Override public String backKeyFieldName() { return "aa"; }
    @Override public String rightKeyFieldName() { return "ab"; }
    @Override public String jumpKeyFieldName() { return "ac"; }
    @Override public String sneakKeyFieldName() { return "ad"; }
}
