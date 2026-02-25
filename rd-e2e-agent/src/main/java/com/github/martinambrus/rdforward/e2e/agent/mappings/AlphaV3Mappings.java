package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Alpha 1.2.0_02 (protocol v3).
 * Verified by CFR decompilation of the official Alpha 1.2.0_02 client JAR.
 *
 * Significant shifts vs v4: Entity fields shifted -6, several Minecraft
 * fields shifted -1, GuiScreen class changed from bp to bn.
 */
public class AlphaV3Mappings extends AlphaV6Mappings {

    // Entity position fields (lk class in 1.2.0)
    @Override
    public String posXFieldName() {
        return "ap";
    }

    @Override
    public String posYFieldName() {
        return "aq";
    }

    @Override
    public String posZFieldName() {
        return "ar";
    }

    @Override
    public String yawFieldName() {
        return "av";
    }

    @Override
    public String pitchFieldName() {
        return "aw";
    }

    @Override
    public String onGroundFieldName() {
        return "aA";
    }

    // Minecraft class fields shifted -1
    @Override
    public String serverHostFieldName() {
        return "U";
    }

    @Override
    public String serverPortFieldName() {
        return "V";
    }

    @Override
    public String clickCooldownFieldName() {
        return "R";
    }

    @Override
    public String mouseGrabbedFieldName() {
        return "K";
    }

    // GuiScreen class is bn (not bp)
    @Override
    public String guiScreenClassName() {
        return "bn";
    }

    // ms = survival inventory GUI (GuiInventory) in v3
    @Override
    public String guiInventoryClassName() {
        return "ms";
    }
}
