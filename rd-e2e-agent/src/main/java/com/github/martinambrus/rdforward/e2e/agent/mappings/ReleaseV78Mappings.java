package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.6.4 (protocol v78).
 * Verified by CFR decompilation of the 1.6.4 client JAR.
 * Last pre-Netty release. Field-identical to 1.6.2 except obfuscated type names shifted.
 */
public class ReleaseV78Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "atv"; }
    @Override public String runMethodName() { return "d"; }
    @Override public String tickMethodName() { return "k"; }
    @Override public String playerFieldName() { return "h"; }
    @Override public String worldFieldName() { return "f"; }
    @Override public String serverHostFieldName() { return null; }
    @Override public String serverPortFieldName() { return null; }
    @Override public String displayWidthFieldName() { return "d"; }
    @Override public String displayHeightFieldName() { return "e"; }
    @Override public String posXFieldName() { return "u"; }
    @Override public String posYFieldName() { return "v"; }
    @Override public String posZFieldName() { return "w"; }
    @Override public String gameSettingsFieldName() { return "u"; }
    @Override public String movementInputFieldName() { return "c"; }
    @Override public String pressedKeysFieldName() { return null; }
    @Override public String yawFieldName() { return "A"; }
    @Override public String pitchFieldName() { return "B"; }
    @Override public String onGroundFieldName() { return "F"; }
    @Override public String inventoryFieldName() { return "bn"; }
    @Override public String mainInventoryFieldName() { return "a"; }
    @Override public String currentItemFieldName() { return "c"; }
    @Override public String itemIdFieldName() { return "d"; }
    @Override public String stackSizeFieldName() { return "b"; }
    @Override public String getBlockIdMethodName() { return "a"; }
    @Override public String clickMethodName() { return "c"; }
    @Override public String mouseGrabbedFieldName() { return "A"; }
    @Override public String sendChatMessageMethodName() { return "b"; }
    @Override public String dropPlayerItemMethodName() { return "a"; }
    @Override public String dropOneItemMethodName() { return "a"; }
    @Override public String displayGuiScreenMethodName() { return "a"; }
    @Override public String currentScreenFieldName() { return "n"; }
    @Override public String ingameGuiFieldName() { return "r"; }
    @Override public String chatLinesFieldName() { return "d"; }
    @Override public String cursorItemFieldName() { return "g"; }
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "awe"; }
    @Override public String sessionFieldName() { return "U"; }
    @Override public String sessionUsernameFieldName() { return "a"; }
    @Override public String clickCooldownFieldName() { return "W"; }
    @Override public String playerControllerFieldName() { return "c"; }
    @Override public String digMethodName() { return "b"; }
    @Override public String chatLineTextFieldName() { return "b"; }
    @Override public String guiScreenWidthFieldName() { return "g"; }
    @Override public String guiScreenHeightFieldName() { return "h"; }
    @Override public String keyBindingPressedFieldName() { return "e"; }
    @Override public String forwardKeyFieldName() { return "I"; }
    @Override public String leftKeyFieldName() { return "J"; }
    @Override public String backKeyFieldName() { return "K"; }
    @Override public String rightKeyFieldName() { return "L"; }
    @Override public String jumpKeyFieldName() { return "M"; }
    @Override public String sneakKeyFieldName() { return "Q"; }
    @Override public String creativeInventoryClassName() { return "axm"; }
    @Override public String guiInventoryClassName() { return "axv"; }
    @Override public String closeContainerMethodName() { return "i"; }
}
