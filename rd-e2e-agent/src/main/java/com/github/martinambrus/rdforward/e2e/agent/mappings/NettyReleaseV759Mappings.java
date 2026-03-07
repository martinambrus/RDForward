package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.19 (protocol v759).
 * Derived from Fabric Yarn + Intermediary mappings.
 */
public class NettyReleaseV759Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "eev"; }
    @Override public String runMethodName() { return "e"; }
    @Override public String tickMethodName() { return "q"; }
    @Override public String playerFieldName() { return "s"; }
    @Override public String worldFieldName() { return "r"; }
    @Override public String serverHostFieldName() { return null; }
    @Override public String serverPortFieldName() { return null; }
    @Override public String displayWidthFieldName() { return null; }
    @Override public String displayHeightFieldName() { return null; }
    @Override public String posXFieldName() { return "t"; }
    @Override public String posYFieldName() { return "u"; }
    @Override public String posZFieldName() { return "v"; }
    @Override public String gameSettingsFieldName() { return "l"; }
    @Override public String movementInputFieldName() { return "cz"; }
    @Override public String pressedKeysFieldName() { return null; }
    @Override public String yawFieldName() { return "aA"; }
    @Override public String pitchFieldName() { return "aB"; }
    @Override public String onGroundFieldName() { return "y"; }
    @Override public String inventoryFieldName() { return "cp"; }
    @Override public String mainInventoryFieldName() { return "h"; }
    @Override public String currentItemFieldName() { return "k"; }
    @Override public String itemIdFieldName() { return "t"; }
    @Override public String stackSizeFieldName() { return "r"; }
    @Override public String getBlockIdMethodName() { return "a_"; }
    @Override public String clickMethodName() { return "g"; }
    @Override public String mouseGrabbedFieldName() { return "r"; }
    @Override public String mouseHelperFieldName() { return "m"; }
    @Override public String sendChatMessageMethodName() { return "d"; }
    @Override public String dropPlayerItemMethodName() { return "a"; }
    @Override public String dropOneItemMethodName() { return null; }
    @Override public String displayGuiScreenMethodName() { return "a"; }
    @Override public String currentScreenFieldName() { return "y"; }
    @Override public String ingameGuiFieldName() { return "k"; }
    @Override public String chatLinesFieldName() { return "e"; }
    @Override public String cursorItemFieldName() { return "n"; }
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "ekj"; }
    @Override public String sessionFieldName() { return "V"; }
    @Override public String sessionUsernameFieldName() { return "a"; }
    @Override public String clickCooldownFieldName() { return "w"; }
    @Override public String playerControllerFieldName() { return "q"; }
    @Override public String digMethodName() { return "b"; }
    @Override public String chatLineTextFieldName() { return "b"; }
    @Override public String guiScreenWidthFieldName() { return "j"; }
    @Override public String guiScreenHeightFieldName() { return "k"; }
    @Override public String keyBindingPressedFieldName() { return "p"; }
    @Override public String forwardKeyFieldName() { return "y"; }
    @Override public String leftKeyFieldName() { return "z"; }
    @Override public String backKeyFieldName() { return "A"; }
    @Override public String rightKeyFieldName() { return "B"; }
    @Override public String jumpKeyFieldName() { return "C"; }
    @Override public String sneakKeyFieldName() { return "D"; }
    @Override public String creativeInventoryClassName() { return "elu"; }
    @Override public String guiInventoryClassName() { return "emd"; }
    @Override public String closeContainerMethodName() { return "q"; }
    @Override public boolean posYIsFeetLevel() { return true; }
    @Override public String rightClickMethodName() { return "aU"; }
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "eyl"; }
    @Override public String renderMethodName() { return "f"; }
    @Override public String gameRendererClassName() { return "exn"; }
    @Override public String smartCullFieldName() { return "D"; } // public boolean smartCull on eev
}
