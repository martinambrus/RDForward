package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.7.10 (protocol v5).
 * Same obfuscation as 1.7.6 except GUI class names shifted:
 * GuiScreen bdq->bdw, GuiInventory bfp->bfu, GuiContainerCreative bfg->bfl.
 * Verified by CFR decompilation of the 1.7.10 client JAR.
 */
public class NettyReleaseV5_1710Mappings extends NettyReleaseV5Mappings {
    @Override public String guiScreenClassName() { return "bdw"; }
    @Override public String creativeInventoryClassName() { return "bfl"; }
    @Override public String guiInventoryClassName() { return "bfu"; }
}
