package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Alpha 1.1.0 (protocol v2).
 * Verified by CFR decompilation of the official Alpha 1.1.0 client JAR.
 *
 * Significant shifts vs v3: Entity fields shifted -5, Minecraft fields
 * shifted -2, EntityPlayer inventory field changed, pressedKeys shifted,
 * GUI class names changed.
 */
public class AlphaV2Mappings extends AlphaV6Mappings {

    // Entity position fields (kh class in 1.1.0)
    @Override
    public String posXFieldName() {
        return "ak";
    }

    @Override
    public String posYFieldName() {
        return "al";
    }

    @Override
    public String posZFieldName() {
        return "am";
    }

    @Override
    public String yawFieldName() {
        return "aq";
    }

    @Override
    public String pitchFieldName() {
        return "ar";
    }

    @Override
    public String onGroundFieldName() {
        return "av";
    }

    // Minecraft class fields shifted -2 vs v3
    @Override
    public String serverHostFieldName() {
        return "S";
    }

    @Override
    public String serverPortFieldName() {
        return "T";
    }

    @Override
    public String clickCooldownFieldName() {
        return "P";
    }

    @Override
    public String mouseGrabbedFieldName() {
        return "I";
    }

    // EntityPlayer inventory field changed from "e" to "b"
    @Override
    public String inventoryFieldName() {
        return "b";
    }

    // MovementInputFromOptions pressedKeys shifted from "f" to "e"
    @Override
    public String pressedKeysFieldName() {
        return "e";
    }

    // GuiScreen class is bh (not bp/bn)
    @Override
    public String guiScreenClassName() {
        return "bh";
    }

    // lo = survival inventory GUI (GuiInventory) in v2
    @Override
    public String guiInventoryClassName() {
        return "lo";
    }
}
