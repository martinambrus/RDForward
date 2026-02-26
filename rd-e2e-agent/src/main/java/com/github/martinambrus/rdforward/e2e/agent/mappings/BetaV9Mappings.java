package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Beta 1.3_01 (protocol v9).
 * Verified by CFR decompile of the official Beta 1.3_01 client JAR.
 *
 * Key differences from Beta 1.4_01 (BetaV10Mappings):
 * - serverHost/Port shifted to W/X (not Y/Z)
 * - gameSettingsFieldName is "x" (not "y")
 * - mouseGrabbedFieldName is "J" (not "L")
 * - currentScreenFieldName is "q" (same)
 * - ingameGuiFieldName is "t" (not "u")
 * - clickCooldownFieldName is "R" (not "T")
 * - Entity fields shifted (aI/aJ/aK, yaw aO/aP, onGround aT)
 * - inventoryFieldName is "f" (same as 1.4/1.5)
 * - sendChatMessageMethodName is "b" (not "a")
 * - dropOneItemMethodName is "A" (not "D")
 * - GuiScreen is cb, GuiInventory is pw
 */
public class BetaV9Mappings implements FieldMappings {

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
        return "W";
    }

    @Override
    public String serverPortFieldName() {
        return "X";
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
        return "aI";
    }

    @Override
    public String posYFieldName() {
        return "aJ";
    }

    @Override
    public String posZFieldName() {
        return "aK";
    }

    @Override
    public String gameSettingsFieldName() {
        return "x";
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
        return "aO";
    }

    @Override
    public String pitchFieldName() {
        return "aP";
    }

    @Override
    public String onGroundFieldName() {
        return "aT";
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
        return "J";
    }

    @Override
    public String sendChatMessageMethodName() {
        return "b";
    }

    @Override
    public String dropPlayerItemMethodName() {
        return "a";
    }

    @Override
    public String dropOneItemMethodName() {
        return "A";
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
        return "t";
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
        return "cb";
    }

    @Override
    public String sessionFieldName() {
        return "j";
    }

    @Override
    public String clickCooldownFieldName() {
        return "R";
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
    public String creativeInventoryClassName() {
        return null;
    }

    @Override
    public String guiInventoryClassName() {
        return "pw";
    }

    @Override
    public String closeContainerMethodName() {
        return "p";
    }
}
