package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.21.4 (protocol v769).
 * Derived from Fabric Yarn + Intermediary mappings.
 */
public class NettyReleaseV769Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "flk"; }
    @Override public String runMethodName() { return "f"; }
    @Override public String tickMethodName() { return "t"; }
    @Override public String playerFieldName() { return "t"; }
    @Override public String worldFieldName() { return "s"; }
    @Override public String serverHostFieldName() { return null; }
    @Override public String serverPortFieldName() { return null; }
    @Override public String displayWidthFieldName() { return null; }
    @Override public String displayHeightFieldName() { return null; }
    @Override public String posXFieldName() { return "K"; }
    @Override public String posYFieldName() { return "L"; }
    @Override public String posZFieldName() { return "M"; }
    @Override public String gameSettingsFieldName() { return "n"; }
    @Override public String movementInputFieldName() { return "k"; }
    @Override public String pressedKeysFieldName() { return null; }
    @Override public String yawFieldName() { return "aA"; }
    @Override public String pitchFieldName() { return "aB"; }
    @Override public String onGroundFieldName() { return "aD"; }
    @Override public String inventoryFieldName() { return "g"; }
    @Override public String mainInventoryFieldName() { return "g"; }
    @Override public String currentItemFieldName() { return "j"; }
    @Override public String itemIdFieldName() { return "p"; }
    @Override public String stackSizeFieldName() { return "n"; }
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
    @Override public String guiScreenClassName() { return "fum"; }
    @Override public String sessionFieldName() { return "Z"; }
    @Override public String sessionUsernameFieldName() { return "a"; }
    @Override public String clickCooldownFieldName() { return "x"; }
    @Override public String playerControllerFieldName() { return "r"; }
    @Override public String digMethodName() { return "b"; }
    @Override public String chatLineTextFieldName() { return "b"; }
    @Override public String guiScreenWidthFieldName() { return "n"; }
    @Override public String guiScreenHeightFieldName() { return "o"; }
    @Override public String keyBindingPressedFieldName() { return "p"; }
    @Override public String forwardKeyFieldName() { return "v"; }
    @Override public String leftKeyFieldName() { return "w"; }
    @Override public String backKeyFieldName() { return "x"; }
    @Override public String rightKeyFieldName() { return "y"; }
    @Override public String jumpKeyFieldName() { return "z"; }
    @Override public String sneakKeyFieldName() { return "A"; }
    @Override public String creativeInventoryClassName() { return "fvr"; }
    @Override public String guiInventoryClassName() { return "fwc"; }
    @Override public String closeContainerMethodName() { return "p"; }
    @Override public boolean posYIsFeetLevel() { return true; }
    @Override public String rightClickMethodName() { return "bp"; }
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "gnd"; }
    @Override public String renderMethodName() { return "c"; }
    @Override public String gameRendererClassName() { return "glq"; }
}
