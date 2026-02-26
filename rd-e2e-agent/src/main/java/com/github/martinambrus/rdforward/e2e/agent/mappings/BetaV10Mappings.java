package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Beta 1.4_01 (protocol v10).
 * Verified by CFR decompile of the official Beta 1.4_01 client JAR.
 *
 * Differences from Beta 1.5_01 (BetaV11Mappings):
 * - Entity position fields shifted by -1 (aJ/aK/aL vs aK/aL/aM)
 * - Yaw/pitch shifted (aP/aQ vs aQ/aR)
 * - onGround shifted (aU vs aV)
 * - Key bindings shifted by -1 (l/m/n/o/p/u vs m/n/o/p/q/v)
 * - dropOneItemMethodName is "D" (not "E")
 * - GuiScreen class is cf, GuiInventory is qm
 */
public class BetaV10Mappings extends BetaV11Mappings {

    @Override
    public String posXFieldName() {
        return "aJ";
    }

    @Override
    public String posYFieldName() {
        return "aK";
    }

    @Override
    public String posZFieldName() {
        return "aL";
    }

    @Override
    public String yawFieldName() {
        return "aP";
    }

    @Override
    public String pitchFieldName() {
        return "aQ";
    }

    @Override
    public String onGroundFieldName() {
        return "aU";
    }

    @Override
    public String dropOneItemMethodName() {
        return "D";
    }

    @Override
    public String guiScreenClassName() {
        return "cf";
    }

    @Override
    public String forwardKeyFieldName() {
        return "l";
    }

    @Override
    public String leftKeyFieldName() {
        return "m";
    }

    @Override
    public String backKeyFieldName() {
        return "n";
    }

    @Override
    public String rightKeyFieldName() {
        return "o";
    }

    @Override
    public String jumpKeyFieldName() {
        return "p";
    }

    @Override
    public String sneakKeyFieldName() {
        return "u";
    }

    @Override
    public String guiInventoryClassName() {
        return "qm";
    }

    @Override
    public String closeContainerMethodName() {
        return "q";
    }
}
