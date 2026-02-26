package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Alpha 1.0.15 (protocol v13, pre-rewrite SMP).
 * Verified by CFR decompilation of the official Alpha 1.0.15 client JAR.
 *
 * Compared to v14 (Alpha 1.0.16): only GuiInventory class name differs
 * (lc in 1.0.15, ld in 1.0.16).
 */
public class AlphaV13Mappings extends AlphaV14Mappings {

    // lc = survival inventory GUI (GuiInventory) in v13 (was ld in v14)
    @Override
    public String guiInventoryClassName() {
        return "lc";
    }
}
