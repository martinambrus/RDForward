package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.3.2 (protocol v39).
 * Verified by CFR decompilation of the 1.3.2 client JAR.
 *
 * Shares protocol v39 with 1.3.1 but GameSettings gained a new boolean w
 * (hideServerAddress), pushing all key binding fields by one letter.
 * Three class names also shifted: GuiScreen apm->apn, GuiContainerCreative
 * aqm->aqn, GuiInventory aqt->aqu.
 */
public class ReleaseV39_2Mappings extends ReleaseV39Mappings {

    @Override
    public String forwardKeyFieldName() {
        return "x";
    }

    @Override
    public String leftKeyFieldName() {
        return "y";
    }

    @Override
    public String backKeyFieldName() {
        return "z";
    }

    @Override
    public String rightKeyFieldName() {
        return "A";
    }

    @Override
    public String jumpKeyFieldName() {
        return "B";
    }

    @Override
    public String sneakKeyFieldName() {
        return "F";
    }

    @Override
    public String guiScreenClassName() {
        return "apn";
    }

    @Override
    public String creativeInventoryClassName() {
        return "aqn";
    }

    @Override
    public String guiInventoryClassName() {
        return "aqu";
    }
}
