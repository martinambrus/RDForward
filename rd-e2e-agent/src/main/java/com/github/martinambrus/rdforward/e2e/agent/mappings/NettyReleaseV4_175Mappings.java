package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.7.5 (protocol v4).
 * Same field names within classes as 1.7.3 but different obfuscated class names.
 * Verified by CFR decompilation of the 1.7.5 client JAR.
 *
 * Differences from 1.7.3: GuiScreen=bco (was bcl), GuiContainerCreative=bee (was beb),
 * GuiInventory=ben (was bek). Minecraft class auto-detected (azl vs azi).
 */
public class NettyReleaseV4_175Mappings extends NettyReleaseV4_173Mappings {
    @Override public String guiScreenClassName() { return "bco"; }
    @Override public String creativeInventoryClassName() { return "bee"; }
    @Override public String guiInventoryClassName() { return "ben"; }
}
