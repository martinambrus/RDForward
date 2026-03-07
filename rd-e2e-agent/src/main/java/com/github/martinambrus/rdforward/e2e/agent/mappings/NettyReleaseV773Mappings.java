package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.21.9/1.21.10 (protocol v773).
 * Derived from Fabric Yarn + Intermediary mappings.
 */
public class NettyReleaseV773Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "fzz"; }
    @Override public String runMethodName() { return "j"; }
    @Override public String tickMethodName() { return "x"; }
    @Override public String playerFieldName() { return "s"; }
    @Override public String worldFieldName() { return "r"; }
    @Override public String serverHostFieldName() { return null; }
    @Override public String serverPortFieldName() { return null; }
    @Override public String displayWidthFieldName() { return null; }
    @Override public String displayHeightFieldName() { return null; }
    @Override public String posXFieldName() { return "Y"; }
    @Override public String posYFieldName() { return "Z"; }
    @Override public String posZFieldName() { return "aa"; }
    @Override public String gameSettingsFieldName() { return "k"; }
    @Override public String movementInputFieldName() { return "d"; }
    @Override public String pressedKeysFieldName() { return null; }
    @Override public String yawFieldName() { return "aZ"; }
    @Override public String pitchFieldName() { return "ba"; }
    @Override public String onGroundFieldName() { return "bc"; }
    @Override public String inventoryFieldName() { return "cD"; }
    @Override public String mainInventoryFieldName() { return "l"; }
    @Override public String currentItemFieldName() { return "m"; }
    @Override public String itemIdFieldName() { return "s"; }
    @Override public String stackSizeFieldName() { return "q"; }
    @Override public String getBlockIdMethodName() { return "a_"; }
    @Override public String clickMethodName() { return "d"; }
    @Override public String mouseGrabbedFieldName() { return "v"; }
    @Override public String mouseHelperFieldName() { return "n"; }
    @Override public String sendChatMessageMethodName() { return "c"; }
    @Override public String networkHandlerFieldName() { return "c"; }
    @Override public String dropPlayerItemMethodName() { return "b"; }
    @Override public String dropOneItemMethodName() { return null; }
    @Override public String displayGuiScreenMethodName() { return "a"; }
    @Override public String currentScreenFieldName() { return "x"; }
    @Override public String ingameGuiFieldName() { return "j"; }
    @Override public String chatLinesFieldName() { return "k"; }
    @Override public String cursorItemFieldName() { return "p"; }
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "gmj"; }
    @Override public String sessionFieldName() { return "W"; }
    @Override public String sessionUsernameFieldName() { return "a"; }
    @Override public String clickCooldownFieldName() { return "v"; }
    @Override public String playerControllerFieldName() { return "q"; }
    @Override public String digMethodName() { return "b"; }
    @Override public String chatLineTextFieldName() { return "b"; }
    @Override public String guiScreenWidthFieldName() { return "o"; }
    @Override public String guiScreenHeightFieldName() { return "p"; }
    @Override public String keyBindingPressedFieldName() { return "g"; }
    @Override public String forwardKeyFieldName() { return "t"; }
    @Override public String leftKeyFieldName() { return "u"; }
    @Override public String backKeyFieldName() { return "v"; }
    @Override public String rightKeyFieldName() { return "w"; }
    @Override public String jumpKeyFieldName() { return "x"; }
    @Override public String sneakKeyFieldName() { return "y"; }
    @Override public String creativeInventoryClassName() { return "goh"; }
    @Override public String guiInventoryClassName() { return "gos"; }
    @Override public String closeContainerMethodName() { return "r"; }
    @Override public boolean posYIsFeetLevel() { return true; }
    @Override public String rightClickMethodName() { return "bt"; }
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "hhg"; }
    @Override public String renderMethodName() { return "c"; }
    @Override public String gameRendererClassName() { return "hfk"; }
    @Override public String smartCullFieldName() { return "z"; } // public boolean smartCull on fzz
}
