package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.19.4 (protocol v762).
 * Derived from Fabric Yarn + Intermediary mappings.
 */
public class NettyReleaseV762Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "emh"; }
    @Override public String runMethodName() { return "e"; }
    @Override public String tickMethodName() { return "s"; }
    @Override public String playerFieldName() { return "t"; }
    @Override public String worldFieldName() { return "s"; }
    @Override public String serverHostFieldName() { return null; }
    @Override public String serverPortFieldName() { return null; }
    @Override public String displayWidthFieldName() { return null; }
    @Override public String displayHeightFieldName() { return null; }
    @Override public String posXFieldName() { return "I"; }
    @Override public String posYFieldName() { return "J"; }
    @Override public String posZFieldName() { return "K"; }
    @Override public String gameSettingsFieldName() { return "m"; }
    @Override public String movementInputFieldName() { return "cl"; }
    @Override public String pressedKeysFieldName() { return null; }
    @Override public String yawFieldName() { return "aE"; }
    @Override public String pitchFieldName() { return "aF"; }
    @Override public String onGroundFieldName() { return "N"; }
    @Override public String inventoryFieldName() { return "ck"; }
    @Override public String mainInventoryFieldName() { return "i"; }
    @Override public String currentItemFieldName() { return "l"; }
    @Override public String itemIdFieldName() { return "u"; }
    @Override public String stackSizeFieldName() { return "s"; }
    @Override public String getBlockIdMethodName() { return "a_"; }
    @Override public String clickMethodName() { return "g"; }
    @Override public String mouseGrabbedFieldName() { return "r"; }
    @Override public String mouseHelperFieldName() { return "n"; }
    @Override public String sendChatMessageMethodName() { return "b"; }
    @Override public String networkHandlerFieldName() { return "ck"; }
    @Override public String dropPlayerItemMethodName() { return "a"; }
    @Override public String dropOneItemMethodName() { return null; }
    @Override public String displayGuiScreenMethodName() { return "a"; }
    @Override public String currentScreenFieldName() { return "z"; }
    @Override public String ingameGuiFieldName() { return "l"; }
    @Override public String chatLinesFieldName() { return "o"; }
    @Override public String cursorItemFieldName() { return "n"; }
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "etd"; }
    @Override public String sessionFieldName() { return "W"; }
    @Override public String sessionUsernameFieldName() { return "a"; }
    @Override public String clickCooldownFieldName() { return "x"; }
    @Override public String playerControllerFieldName() { return "r"; }
    @Override public String digMethodName() { return "b"; }
    @Override public String chatLineTextFieldName() { return "b"; }
    @Override public String guiScreenWidthFieldName() { return "k"; }
    @Override public String guiScreenHeightFieldName() { return "l"; }
    @Override public String keyBindingPressedFieldName() { return "p"; }
    @Override public String forwardKeyFieldName() { return "x"; }
    @Override public String leftKeyFieldName() { return "y"; }
    @Override public String backKeyFieldName() { return "z"; }
    @Override public String rightKeyFieldName() { return "A"; }
    @Override public String jumpKeyFieldName() { return "B"; }
    @Override public String sneakKeyFieldName() { return "C"; }
    @Override public String creativeInventoryClassName() { return "eup"; }
    @Override public String guiInventoryClassName() { return "eva"; }
    @Override public String closeContainerMethodName() { return "q"; }
    @Override public boolean posYIsFeetLevel() { return true; }
    @Override public String rightClickMethodName() { return "bi"; }
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "fix"; }
    @Override public String renderMethodName() { return "f"; }
    @Override public String gameRendererClassName() { return "fhz"; }
    @Override public String smartCullFieldName() { return "E"; } // public boolean smartCull on emh
}
