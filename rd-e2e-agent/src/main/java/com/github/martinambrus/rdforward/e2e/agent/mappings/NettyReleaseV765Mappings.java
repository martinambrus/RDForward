package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.20.3/1.20.4 (protocol v765).
 * Derived from Fabric Yarn + Intermediary mappings.
 */
public class NettyReleaseV765Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "evi"; }
    @Override public String runMethodName() { return "f"; }
    @Override public String tickMethodName() { return "s"; }
    @Override public String playerFieldName() { return "s"; }
    @Override public String worldFieldName() { return "r"; }
    @Override public String serverHostFieldName() { return null; }
    @Override public String serverPortFieldName() { return null; }
    @Override public String displayWidthFieldName() { return null; }
    @Override public String displayHeightFieldName() { return null; }
    @Override public String posXFieldName() { return "K"; }
    @Override public String posYFieldName() { return "L"; }
    @Override public String posZFieldName() { return "M"; }
    @Override public String gameSettingsFieldName() { return "m"; }
    @Override public String movementInputFieldName() { return "co"; }
    @Override public String pressedKeysFieldName() { return null; }
    @Override public String yawFieldName() { return "aG"; }
    @Override public String pitchFieldName() { return "aH"; }
    @Override public String onGroundFieldName() { return "aJ"; }
    @Override public String inventoryFieldName() { return "cm"; }
    @Override public String mainInventoryFieldName() { return "i"; }
    @Override public String currentItemFieldName() { return "l"; }
    @Override public String itemIdFieldName() { return "z"; }
    @Override public String stackSizeFieldName() { return "x"; }
    @Override public String getBlockIdMethodName() { return "a_"; }
    @Override public String clickMethodName() { return "e"; }
    @Override public String mouseGrabbedFieldName() { return "s"; }
    @Override public String mouseHelperFieldName() { return "n"; }
    @Override public String sendChatMessageMethodName() { return "b"; }
    @Override public String networkHandlerFieldName() { return "cn"; }
    @Override public String dropPlayerItemMethodName() { return "a"; }
    @Override public String dropOneItemMethodName() { return null; }
    @Override public String displayGuiScreenMethodName() { return "a"; }
    @Override public String currentScreenFieldName() { return "y"; }
    @Override public String ingameGuiFieldName() { return "l"; }
    @Override public String chatLinesFieldName() { return "k"; }
    @Override public String cursorItemFieldName() { return "n"; }
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "fdb"; }
    @Override public String sessionFieldName() { return "W"; }
    @Override public String sessionUsernameFieldName() { return "a"; }
    @Override public String clickCooldownFieldName() { return "w"; }
    @Override public String playerControllerFieldName() { return "q"; }
    @Override public String digMethodName() { return "b"; }
    @Override public String chatLineTextFieldName() { return "b"; }
    @Override public String guiScreenWidthFieldName() { return "g"; }
    @Override public String guiScreenHeightFieldName() { return "h"; }
    @Override public String keyBindingPressedFieldName() { return "p"; }
    @Override public String forwardKeyFieldName() { return "x"; }
    @Override public String leftKeyFieldName() { return "y"; }
    @Override public String backKeyFieldName() { return "z"; }
    @Override public String rightKeyFieldName() { return "A"; }
    @Override public String jumpKeyFieldName() { return "B"; }
    @Override public String sneakKeyFieldName() { return "C"; }
    @Override public String creativeInventoryClassName() { return "fep"; }
    @Override public String guiInventoryClassName() { return "ffa"; }
    @Override public String closeContainerMethodName() { return "r"; }
    @Override public boolean posYIsFeetLevel() { return true; }
    @Override public String rightClickMethodName() { return "bx"; }
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "fub"; }
    @Override public String renderMethodName() { return "d"; }
    @Override public String gameRendererClassName() { return "fta"; }
    @Override public String smartCullFieldName() { return "D"; } // public boolean smartCull on evi
}
