package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Alpha 1.0.16 (protocol v14, pre-rewrite SMP).
 * Verified by CFR decompilation of the official Alpha 1.0.16 client JAR.
 *
 * Compared to v1 (Alpha 1.0.17_04): Entity fields shifted -2,
 * GuiScreen and GuiInventory class names shifted.
 * InventoryPlayer has no cursorItem field (cursor item is on GuiContainer).
 */
public class AlphaV14Mappings extends AlphaV1Mappings {

    // Entity position fields (jx class in 1.0.16) shifted -2 vs v1
    @Override
    public String posXFieldName() {
        return "ah";
    }

    @Override
    public String posYFieldName() {
        return "ai";
    }

    @Override
    public String posZFieldName() {
        return "aj";
    }

    @Override
    public String yawFieldName() {
        return "an";
    }

    @Override
    public String pitchFieldName() {
        return "ao";
    }

    @Override
    public String onGroundFieldName() {
        return "as";
    }

    // bd = GuiScreen class in v14 (was bg in v1)
    @Override
    public String guiScreenClassName() {
        return "bd";
    }

    // ld = survival inventory GUI (GuiInventory) in v14 (was lm in v1)
    @Override
    public String guiInventoryClassName() {
        return "ld";
    }

    // No cursorItem field on InventoryPlayer in v14
    // (cursor item lives on GuiContainer base class instead)
    @Override
    public String cursorItemFieldName() {
        return null;
    }
}
