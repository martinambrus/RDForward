package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.8.8/1.8.9 (protocol v47).
 * Verified by CFR decompilation of the 1.8.8 client JAR.
 *
 * Only difference from 1.8.4: GameSettings (avh) gained a new boolean field W
 * before the key bindings, shifting all key binding field names by one letter.
 */
public class NettyReleaseV47_188Mappings extends NettyReleaseV47_184Mappings {
    @Override public String forwardKeyFieldName() { return "X"; }
    @Override public String leftKeyFieldName() { return "Y"; }
    @Override public String backKeyFieldName() { return "Z"; }
    @Override public String rightKeyFieldName() { return "aa"; }
    @Override public String jumpKeyFieldName() { return "ab"; }
    @Override public String sneakKeyFieldName() { return "ac"; }
}
