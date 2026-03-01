package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.7.8/1.7.9 (protocol v5).
 * Same obfuscation as 1.7.6 except GUI class names shifted:
 * GuiScreen bdq->bds, GuiInventory bfp->bfr, GuiContainerCreative bfg->bfi.
 * Verified by CFR decompilation of the 1.7.8 client JAR.
 */
public class NettyReleaseV5_178Mappings extends NettyReleaseV5Mappings {
    @Override public String guiScreenClassName() { return "bds"; }
    @Override public String creativeInventoryClassName() { return "bfi"; }
    @Override public String guiInventoryClassName() { return "bfr"; }
}
