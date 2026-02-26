package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Beta 1.7.3 (protocol v14).
 * Verified by CFR decompile of the official Beta 1.7.3 client JAR.
 *
 * Key differences from Beta 1.8.1 (BetaV17Mappings):
 * - Uses boolean[] pressedKeys (field "f" on MovementInputFromOptions),
 *   NOT KeyBinding.pressed
 * - Entity position fields shifted (aM/aN/aO vs o/p/q)
 * - No creative inventory (creative was added in Beta 1.8)
 * - GuiScreen width/height fields are c/d (Alpha-style), not m/n
 */
public class BetaV14Mappings implements FieldMappings {

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
        return "k";
    }

    @Override
    public String playerFieldName() {
        return "h";
    }

    @Override
    public String worldFieldName() {
        return "f";
    }

    @Override
    public String serverHostFieldName() {
        return "ab";
    }

    @Override
    public String serverPortFieldName() {
        return "ac";
    }

    @Override
    public String displayWidthFieldName() {
        return "d";
    }

    @Override
    public String displayHeightFieldName() {
        return "e";
    }

    @Override
    public String posXFieldName() {
        return "aM";
    }

    @Override
    public String posYFieldName() {
        return "aN";
    }

    @Override
    public String posZFieldName() {
        return "aO";
    }

    @Override
    public String gameSettingsFieldName() {
        return "z";
    }

    @Override
    public String movementInputFieldName() {
        return "a";
    }

    // Beta 1.7.3 uses boolean[] pressedKeys on MovementInputFromOptions (lr),
    // NOT KeyBinding.pressed (KeyBinding class qb has no pressed field).
    @Override
    public String pressedKeysFieldName() {
        return "f";
    }

    @Override
    public String yawFieldName() {
        return "aS";
    }

    @Override
    public String pitchFieldName() {
        return "aT";
    }

    @Override
    public String onGroundFieldName() {
        return "aX";
    }

    // c = inventory (type ix = InventoryPlayer) on gs (EntityPlayer)
    @Override
    public String inventoryFieldName() {
        return "c";
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
        return "N";
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
        return "D";
    }

    @Override
    public String displayGuiScreenMethodName() {
        return "a";
    }

    @Override
    public String currentScreenFieldName() {
        return "r";
    }

    @Override
    public String ingameGuiFieldName() {
        return "v";
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
        return "da";
    }

    @Override
    public String sessionFieldName() {
        return "k";
    }

    @Override
    public String clickCooldownFieldName() {
        return "W";
    }

    @Override
    public String playerControllerFieldName() {
        return "c";
    }

    @Override
    public String digMethodName() {
        return "a";
    }

    @Override
    public String chatLineTextFieldName() {
        return "a";
    }

    // GuiScreen width/height fields are c/d (same as Alpha, not m/n like Beta 1.8)
    @Override
    public String guiScreenWidthFieldName() {
        return "c";
    }

    @Override
    public String guiScreenHeightFieldName() {
        return "d";
    }

    // No KeyBinding.pressed in Beta 1.7.3 — uses boolean[] pressedKeys instead
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
        return "ue";
    }

    @Override
    public String closeContainerMethodName() {
        return "r";
    }
}
