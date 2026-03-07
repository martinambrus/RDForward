package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.20.5/1.20.6 (protocol v766).
 * Derived from Fabric Yarn + Intermediary mappings.
 */
public class NettyReleaseV766Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "ffg"; }
    @Override public String runMethodName() { return "f"; }
    @Override public String tickMethodName() { return "t"; }
    @Override public String playerFieldName() { return "s"; }
    @Override public String worldFieldName() { return "r"; }
    @Override public String serverHostFieldName() { return null; }
    @Override public String serverPortFieldName() { return null; }
    @Override public String displayWidthFieldName() { return null; }
    @Override public String displayHeightFieldName() { return null; }
    @Override public String posXFieldName() { return "L"; }
    @Override public String posYFieldName() { return "M"; }
    @Override public String posZFieldName() { return "N"; }
    @Override public String gameSettingsFieldName() { return "m"; }
    @Override public String movementInputFieldName() { return "cA"; }
    @Override public String pressedKeysFieldName() { return null; }
    @Override public String yawFieldName() { return "aF"; }
    @Override public String pitchFieldName() { return "aG"; }
    @Override public String onGroundFieldName() { return "aI"; }
    @Override public String inventoryFieldName() { return "g"; }
    @Override public String mainInventoryFieldName() { return "h"; }
    @Override public String currentItemFieldName() { return "k"; }
    @Override public String itemIdFieldName() { return "q"; }
    @Override public String stackSizeFieldName() { return "o"; }
    @Override public String getBlockIdMethodName() { return "a_"; }
    @Override public String clickMethodName() { return "d"; }
    @Override public String mouseGrabbedFieldName() { return "s"; }
    @Override public String mouseHelperFieldName() { return "n"; }
    @Override public String sendChatMessageMethodName() { return "b"; }
    @Override public String networkHandlerFieldName() { return "cz"; }
    @Override public String dropPlayerItemMethodName() { return "a"; }
    @Override public String dropOneItemMethodName() { return null; }
    @Override public String displayGuiScreenMethodName() { return "a"; }
    @Override public String currentScreenFieldName() { return "y"; }
    @Override public String ingameGuiFieldName() { return "l"; }
    @Override public String chatLinesFieldName() { return "k"; }
    @Override public String cursorItemFieldName() { return "n"; }
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "fne"; }
    @Override public String sessionFieldName() { return "W"; }
    @Override public String sessionUsernameFieldName() { return "a"; }
    @Override public String clickCooldownFieldName() { return "w"; }
    @Override public String playerControllerFieldName() { return "q"; }
    @Override public String digMethodName() { return "b"; }
    @Override public String chatLineTextFieldName() { return "b"; }
    @Override public String guiScreenWidthFieldName() { return "n"; }
    @Override public String guiScreenHeightFieldName() { return "o"; }
    @Override public String keyBindingPressedFieldName() { return "p"; }
    @Override public String forwardKeyFieldName() { return "w"; }
    @Override public String leftKeyFieldName() { return "x"; }
    @Override public String backKeyFieldName() { return "y"; }
    @Override public String rightKeyFieldName() { return "z"; }
    @Override public String jumpKeyFieldName() { return "A"; }
    @Override public String sneakKeyFieldName() { return "B"; }
    @Override public String creativeInventoryClassName() { return "fos"; }
    @Override public String guiInventoryClassName() { return "fpd"; }
    @Override public String closeContainerMethodName() { return "r"; }
    @Override public boolean posYIsFeetLevel() { return true; }
    @Override public String rightClickMethodName() { return "bp"; }
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "gej"; }
    @Override public String renderMethodName() { return "c"; }
    @Override public String gameRendererClassName() { return "gdi"; }
    @Override public String smartCullFieldName() { return "D"; } // public boolean smartCull on ffg
}
