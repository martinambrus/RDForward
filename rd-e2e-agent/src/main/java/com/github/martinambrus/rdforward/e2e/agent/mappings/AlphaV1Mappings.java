package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Alpha 1.0.17_04 (protocol v1).
 * Verified by CFR decompilation of the official Alpha 1.0.17_04 client JAR.
 *
 * Compared to v2 (Alpha 1.1.0): Entity fields shifted -1,
 * GuiScreen and GuiInventory class names shifted.
 * All Minecraft class fields, inventory, input, and world fields are identical to v2.
 */
public class AlphaV1Mappings extends AlphaV2Mappings {

    // Entity position fields (kf class in 1.0.17) shifted -1 vs v2
    @Override
    public String posXFieldName() {
        return "aj";
    }

    @Override
    public String posYFieldName() {
        return "ak";
    }

    @Override
    public String posZFieldName() {
        return "al";
    }

    @Override
    public String yawFieldName() {
        return "ap";
    }

    @Override
    public String pitchFieldName() {
        return "aq";
    }

    @Override
    public String onGroundFieldName() {
        return "au";
    }

    // bg = GuiScreen class in v1 (was bh in v2)
    @Override
    public String guiScreenClassName() {
        return "bg";
    }

    // lm = survival inventory GUI (GuiInventory) in v1 (was lo in v2)
    @Override
    public String guiInventoryClassName() {
        return "lm";
    }
}
