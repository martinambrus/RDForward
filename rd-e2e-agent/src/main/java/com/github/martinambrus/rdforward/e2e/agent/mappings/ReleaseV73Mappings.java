package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.6.1 (protocol v73).
 * Verified by CFR decompilation of the 1.6.1 client JAR.
 * Major changes from 1.5.x: Minecraft class is now obfuscated (atn),
 * tickMethod reverted to k, gameSettings moved to t, itemId c->d, stackSize a->b.
 * Chat requires 3-level access: ingameGui -> GuiNewChat (field h) -> chatLines (field d).
 */
public class ReleaseV73Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "atn"; }
    @Override public String runMethodName() { return "d"; }
    @Override public String tickMethodName() { return "k"; }
    @Override public String playerFieldName() { return "g"; }
    @Override public String worldFieldName() { return "e"; }
    @Override public String serverHostFieldName() { return null; }
    @Override public String serverPortFieldName() { return null; }
    @Override public String displayWidthFieldName() { return "c"; }
    @Override public String displayHeightFieldName() { return "d"; }
    @Override public String posXFieldName() { return "u"; }
    @Override public String posYFieldName() { return "v"; }
    @Override public String posZFieldName() { return "w"; }
    @Override public String gameSettingsFieldName() { return "t"; }
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
    @Override public String mouseGrabbedFieldName() { return "z"; }
    @Override public String sendChatMessageMethodName() { return "b"; }
    @Override public String dropPlayerItemMethodName() { return "a"; }
    @Override public String dropOneItemMethodName() { return "a"; }
    @Override public String displayGuiScreenMethodName() { return "a"; }
    @Override public String currentScreenFieldName() { return "m"; }
    @Override public String ingameGuiFieldName() { return "q"; }
    @Override public String chatLinesFieldName() { return "d"; }
    @Override public String cursorItemFieldName() { return "g"; }
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "avv"; }
    @Override public String sessionFieldName() { return "T"; }
    @Override public String sessionUsernameFieldName() { return "a"; }
    @Override public String clickCooldownFieldName() { return "V"; }
    @Override public String playerControllerFieldName() { return "b"; }
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
    @Override public String creativeInventoryClassName() { return "axd"; }
    @Override public String guiInventoryClassName() { return "axm"; }
    @Override public String closeContainerMethodName() { return "i"; }
}
