package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Alpha 1.2.2b (protocol v4).
 * Verified by CFR decompilation of the official Alpha 1.2.2b client JAR.
 *
 * Entity fields shifted -1 vs v5/v6 (one fewer field before posX in Entity class).
 * Inventory GUI class is mz (vs nc in v5, ne in v6).
 */
public class AlphaV4Mappings extends AlphaV6Mappings {

    // Entity position fields shifted -1 vs v6
    @Override
    public String posXFieldName() {
        return "av";
    }

    @Override
    public String posYFieldName() {
        return "aw";
    }

    @Override
    public String posZFieldName() {
        return "ax";
    }

    @Override
    public String yawFieldName() {
        return "aB";
    }

    @Override
    public String pitchFieldName() {
        return "aC";
    }

    @Override
    public String onGroundFieldName() {
        return "aG";
    }

    // mz = survival inventory GUI (GuiInventory) in v4
    @Override
    public String guiInventoryClassName() {
        return "mz";
    }
}
