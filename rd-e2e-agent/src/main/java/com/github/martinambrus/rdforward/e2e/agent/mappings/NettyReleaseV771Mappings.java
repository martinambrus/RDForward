package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.21.6/1.21.7 (protocol v771).
 * Derived from Fabric Yarn + Intermediary mappings.
 */
public class NettyReleaseV771Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "fud"; }
    @Override public String runMethodName() { return "f"; }
    @Override public String tickMethodName() { return "t"; }
    @Override public String playerFieldName() { return "t"; }
    @Override public String worldFieldName() { return "s"; }
    @Override public String serverHostFieldName() { return null; }
    @Override public String serverPortFieldName() { return null; }
    @Override public String displayWidthFieldName() { return null; }
    @Override public String displayHeightFieldName() { return null; }
    @Override public String posXFieldName() { return "X"; }
    @Override public String posYFieldName() { return "Y"; }
    @Override public String posZFieldName() { return "Z"; }
    @Override public String gameSettingsFieldName() { return "n"; }
    @Override public String movementInputFieldName() { return "k"; }
    @Override public String pressedKeysFieldName() { return null; }
    @Override public String yawFieldName() { return "aY"; }
    @Override public String pitchFieldName() { return "aZ"; }
    @Override public String onGroundFieldName() { return "bb"; }
    @Override public String inventoryFieldName() { return "cQ"; }
    @Override public String mainInventoryFieldName() { return "k"; }
    @Override public String currentItemFieldName() { return "l"; }
    @Override public String itemIdFieldName() { return "s"; }
    @Override public String stackSizeFieldName() { return "q"; }
    @Override public String getBlockIdMethodName() { return "a_"; }
    @Override public String clickMethodName() { return "d"; }
    @Override public String mouseGrabbedFieldName() { return "s"; }
    @Override public String mouseHelperFieldName() { return "o"; }
    @Override public String sendChatMessageMethodName() { return "b"; }
    @Override public String networkHandlerFieldName() { return "j"; }
    @Override public String dropPlayerItemMethodName() { return "a"; }
    @Override public String dropOneItemMethodName() { return null; }
    @Override public String displayGuiScreenMethodName() { return "a"; }
    @Override public String currentScreenFieldName() { return "z"; }
    @Override public String ingameGuiFieldName() { return "m"; }
    @Override public String chatLinesFieldName() { return "k"; }
    @Override public String cursorItemFieldName() { return "p"; }
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "ges"; }
    @Override public String sessionFieldName() { return "aa"; }
    @Override public String sessionUsernameFieldName() { return "a"; }
    @Override public String clickCooldownFieldName() { return "x"; }
    @Override public String playerControllerFieldName() { return "r"; }
    @Override public String digMethodName() { return "b"; }
    @Override public String chatLineTextFieldName() { return "b"; }
    @Override public String guiScreenWidthFieldName() { return "o"; }
    @Override public String guiScreenHeightFieldName() { return "p"; }
    @Override public String keyBindingPressedFieldName() { return "p"; }
    @Override public String forwardKeyFieldName() { return "v"; }
    @Override public String leftKeyFieldName() { return "w"; }
    @Override public String backKeyFieldName() { return "x"; }
    @Override public String rightKeyFieldName() { return "y"; }
    @Override public String jumpKeyFieldName() { return "z"; }
    @Override public String sneakKeyFieldName() { return "A"; }
    @Override public String creativeInventoryClassName() { return "ggp"; }
    @Override public String guiInventoryClassName() { return "gha"; }
    @Override public String closeContainerMethodName() { return "p"; }
    @Override public boolean posYIsFeetLevel() { return true; }
    @Override public String rightClickMethodName() { return "bq"; }
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "gyr"; }
    @Override public String renderMethodName() { return "c"; }
    @Override public String gameRendererClassName() { return "gxa"; }
}
