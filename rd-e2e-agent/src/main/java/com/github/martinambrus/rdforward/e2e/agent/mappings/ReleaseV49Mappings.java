package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.4.4 (protocol v49).
 * Verified by CFR decompilation of the 1.4.4 client JAR.
 */
public class ReleaseV49Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "net.minecraft.client.Minecraft"; }
    @Override public String runMethodName() { return "run"; }
    @Override public String tickMethodName() { return "l"; }
    @Override public String playerFieldName() { return "g"; }
    @Override public String worldFieldName() { return "e"; }
    @Override public String serverHostFieldName() { return "ae"; }
    @Override public String serverPortFieldName() { return "af"; }
    @Override public String displayWidthFieldName() { return "c"; }
    @Override public String displayHeightFieldName() { return "d"; }
    @Override public String posXFieldName() { return "t"; }
    @Override public String posYFieldName() { return "u"; }
    @Override public String posZFieldName() { return "v"; }
    @Override public String gameSettingsFieldName() { return "y"; }
    @Override public String movementInputFieldName() { return "b"; }
    @Override public String pressedKeysFieldName() { return null; }
    @Override public String yawFieldName() { return "z"; }
    @Override public String pitchFieldName() { return "A"; }
    @Override public String onGroundFieldName() { return "E"; }
    @Override public String inventoryFieldName() { return "bI"; }
    @Override public String mainInventoryFieldName() { return "a"; }
    @Override public String currentItemFieldName() { return "c"; }
    @Override public String itemIdFieldName() { return "c"; }
    @Override public String stackSizeFieldName() { return "a"; }
    @Override public String getBlockIdMethodName() { return "a"; }
    @Override public String clickMethodName() { return "c"; }
    @Override public String mouseGrabbedFieldName() { return "G"; }
    @Override public String sendChatMessageMethodName() { return "c"; }
    @Override public String dropPlayerItemMethodName() { return "a"; }
    @Override public String dropOneItemMethodName() { return "bR"; }
    @Override public String displayGuiScreenMethodName() { return "a"; }
    @Override public String currentScreenFieldName() { return "r"; }
    @Override public String ingameGuiFieldName() { return "v"; }
    @Override public String chatLinesFieldName() { return "c"; }
    @Override public String cursorItemFieldName() { return "f"; }
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "aue"; }
    @Override public String sessionFieldName() { return "j"; }
    @Override public String clickCooldownFieldName() { return "ac"; }
    @Override public String playerControllerFieldName() { return "b"; }
    @Override public String digMethodName() { return "b"; }
    @Override public String chatLineTextFieldName() { return "b"; }
    @Override public String guiScreenWidthFieldName() { return "g"; }
    @Override public String guiScreenHeightFieldName() { return "h"; }
    @Override public String keyBindingPressedFieldName() { return "e"; }
    @Override public String forwardKeyFieldName() { return "B"; }
    @Override public String leftKeyFieldName() { return "C"; }
    @Override public String backKeyFieldName() { return "D"; }
    @Override public String rightKeyFieldName() { return "E"; }
    @Override public String jumpKeyFieldName() { return "F"; }
    @Override public String sneakKeyFieldName() { return "J"; }
    @Override public String creativeInventoryClassName() { return "avl"; }
    @Override public String guiInventoryClassName() { return "avs"; }
    @Override public String closeContainerMethodName() { return "i"; }
}
