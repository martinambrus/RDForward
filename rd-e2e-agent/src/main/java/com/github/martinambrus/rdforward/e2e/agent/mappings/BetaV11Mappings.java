package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Beta 1.5_01 (protocol v11).
 * Verified by CFR decompile of the official Beta 1.5_01 client JAR.
 *
 * Key differences from Beta 1.7.3:
 * - tickMethodName is "j" (not "k")
 * - playerFieldName is "g" (not "h")
 * - serverHost/Port shifted to Y/Z (not ab/ac)
 * - displayWidth/Height shifted to c/d (matches, but note gameSettings is "y")
 * - Entity position fields different (aK/aL/aM, yaw aQ/aR, onGround aV)
 * - inventoryFieldName is "f" (not "c")
 * - currentScreenFieldName is "q" (not "r")
 * - ingameGuiFieldName is "u" (not "v")
 * - mouseGrabbedFieldName is "L" (not "N")
 * - sessionFieldName is "j" (not "k")
 * - clickCooldownFieldName is "T" (not "W")
 * - playerControllerFieldName is "b" (not "c")
 * - dropOneItemMethodName is "E" (not "D")
 * - sneakKeyFieldName is "v" (same as 1.7.3)
 * - GuiScreen class is cs, GuiInventory is ta
 */
public class BetaV11Mappings implements FieldMappings {

    @Override
    public String minecraftClassName() {
        return "net.minecraft.client.Minecraft";
    }

    @Override
    public String runMethodName() {
        return "run";
    }

    @Override
    public String tickMethodName() {
        return "j";
    }

    @Override
    public String playerFieldName() {
        return "g";
    }

    @Override
    public String worldFieldName() {
        return "e";
    }

    @Override
    public String serverHostFieldName() {
        return "Y";
    }

    @Override
    public String serverPortFieldName() {
        return "Z";
    }

    @Override
    public String displayWidthFieldName() {
        return "c";
    }

    @Override
    public String displayHeightFieldName() {
        return "d";
    }

    @Override
    public String posXFieldName() {
        return "aK";
    }

    @Override
    public String posYFieldName() {
        return "aL";
    }

    @Override
    public String posZFieldName() {
        return "aM";
    }

    @Override
    public String gameSettingsFieldName() {
        return "y";
    }

    @Override
    public String movementInputFieldName() {
        return "a";
    }

    @Override
    public String pressedKeysFieldName() {
        return "f";
    }

    @Override
    public String yawFieldName() {
        return "aQ";
    }

    @Override
    public String pitchFieldName() {
        return "aR";
    }

    @Override
    public String onGroundFieldName() {
        return "aV";
    }

    @Override
    public String inventoryFieldName() {
        return "f";
    }

    @Override
    public String mainInventoryFieldName() {
        return "a";
    }

    @Override
    public String currentItemFieldName() {
        return "c";
    }

    @Override
    public String itemIdFieldName() {
        return "c";
    }

    @Override
    public String stackSizeFieldName() {
        return "a";
    }

    @Override
    public String getBlockIdMethodName() {
        return "a";
    }

    @Override
    public String clickMethodName() {
        return "a";
    }

    @Override
    public String mouseGrabbedFieldName() {
        return "L";
    }

    @Override
    public String sendChatMessageMethodName() {
        return "a";
    }

    @Override
    public String dropPlayerItemMethodName() {
        return "a";
    }

    @Override
    public String dropOneItemMethodName() {
        return "E";
    }

    @Override
    public String displayGuiScreenMethodName() {
        return "a";
    }

    @Override
    public String currentScreenFieldName() {
        return "q";
    }

    @Override
    public String ingameGuiFieldName() {
        return "u";
    }

    @Override
    public String chatLinesFieldName() {
        return "e";
    }

    @Override
    public String cursorItemFieldName() {
        return "f";
    }

    @Override
    public String craftingInventoryFieldName() {
        return null;
    }

    @Override
    public String guiScreenClassName() {
        return "cs";
    }

    @Override
    public String sessionFieldName() {
        return "j";
    }

    @Override
    public String clickCooldownFieldName() {
        return "T";
    }

    @Override
    public String playerControllerFieldName() {
        return "b";
    }

    @Override
    public String digMethodName() {
        return "a";
    }

    @Override
    public String chatLineTextFieldName() {
        return "a";
    }

    @Override
    public String guiScreenWidthFieldName() {
        return "c";
    }

    @Override
    public String guiScreenHeightFieldName() {
        return "d";
    }

    @Override
    public String keyBindingPressedFieldName() {
        return null;
    }

    @Override
    public String forwardKeyFieldName() {
        return "m";
    }

    @Override
    public String leftKeyFieldName() {
        return "n";
    }

    @Override
    public String backKeyFieldName() {
        return "o";
    }

    @Override
    public String rightKeyFieldName() {
        return "p";
    }

    @Override
    public String jumpKeyFieldName() {
        return "q";
    }

    @Override
    public String sneakKeyFieldName() {
        return "v";
    }

    @Override
    public String creativeInventoryClassName() {
        return null;
    }

    @Override
    public String guiInventoryClassName() {
        return "ta";
    }

    @Override
    public String closeContainerMethodName() {
        return "r";
    }
}
