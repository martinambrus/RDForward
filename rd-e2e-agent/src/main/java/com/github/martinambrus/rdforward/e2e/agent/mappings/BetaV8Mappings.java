package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Beta 1.2_02 (protocol v8).
 * Verified by CFR decompile of the official Beta 1.2_02 client JAR.
 *
 * Very similar to Beta 1.0 — same field letter assignments.
 * Differences from Beta 1.0 (BetaV7Mappings):
 * - Entity position fields shifted (aF/aG/aH vs aG/aH/aI)
 * - Yaw/pitch shifted (aL/aM vs aM/aN)
 * - onGround shifted (aQ vs aR)
 * - dropOneItemMethodName is "z" (not "w")
 * - GuiScreen class is by, GuiInventory is ov
 */
public class BetaV8Mappings implements FieldMappings {

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
        return "i";
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
        return "V";
    }

    @Override
    public String serverPortFieldName() {
        return "W";
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
        return "aF";
    }

    @Override
    public String posYFieldName() {
        return "aG";
    }

    @Override
    public String posZFieldName() {
        return "aH";
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
        return "aL";
    }

    @Override
    public String pitchFieldName() {
        return "aM";
    }

    @Override
    public String onGroundFieldName() {
        return "aQ";
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
        return "z";
    }

    @Override
    public String displayGuiScreenMethodName() {
        return "a";
    }

    @Override
    public String currentScreenFieldName() {
        return "p";
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
        return "by";
    }

    // Beta 1.0/1.2 session field is "i" (same as Alpha default)
    @Override
    public String sessionFieldName() {
        return "i";
    }

    @Override
    public String clickCooldownFieldName() {
        return "S";
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
        return "k";
    }

    @Override
    public String leftKeyFieldName() {
        return "l";
    }

    @Override
    public String backKeyFieldName() {
        return "m";
    }

    @Override
    public String rightKeyFieldName() {
        return "n";
    }

    @Override
    public String jumpKeyFieldName() {
        return "o";
    }

    @Override
    public String sneakKeyFieldName() {
        return "t";
    }

    @Override
    public String creativeInventoryClassName() {
        return null;
    }

    @Override
    public String guiInventoryClassName() {
        return "ov";
    }

    @Override
    public String closeContainerMethodName() {
        return "p";
    }
}
