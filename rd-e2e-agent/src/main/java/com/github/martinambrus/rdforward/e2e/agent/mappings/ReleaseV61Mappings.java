package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.5.2 (protocol v61).
 * Verified by CFR decompilation of the 1.5.2 client JAR.
 * Identical to 1.5.1 except GuiScreen width/height shifted by one (g/h -> h/i)
 * due to a new static field inserted in GuiScreen.
 */
public class ReleaseV61Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "net.minecraft.client.Minecraft"; }
    @Override public String runMethodName() { return "run"; }
    @Override public String tickMethodName() { return "l"; }
    @Override public String playerFieldName() { return "g"; }
    @Override public String worldFieldName() { return "e"; }
    @Override public String serverHostFieldName() { return "ag"; }
    @Override public String serverPortFieldName() { return "ah"; }
    @Override public String displayWidthFieldName() { return "c"; }
    @Override public String displayHeightFieldName() { return "d"; }
    @Override public String posXFieldName() { return "u"; }
    @Override public String posYFieldName() { return "v"; }
    @Override public String posZFieldName() { return "w"; }
    @Override public String gameSettingsFieldName() { return "z"; }
    @Override public String movementInputFieldName() { return "b"; }
    @Override public String pressedKeysFieldName() { return null; }
    @Override public String yawFieldName() { return "A"; }
    @Override public String pitchFieldName() { return "B"; }
    @Override public String onGroundFieldName() { return "F"; }
    @Override public String inventoryFieldName() { return "bK"; }
    @Override public String mainInventoryFieldName() { return "a"; }
    @Override public String currentItemFieldName() { return "c"; }
    @Override public String itemIdFieldName() { return "c"; }
    @Override public String stackSizeFieldName() { return "a"; }
    @Override public String getBlockIdMethodName() { return "a"; }
    @Override public String clickMethodName() { return "c"; }
    @Override public String mouseGrabbedFieldName() { return "H"; }
    @Override public String sendChatMessageMethodName() { return "d"; }
    @Override public String dropPlayerItemMethodName() { return "a"; }
    @Override public String dropOneItemMethodName() { return null; }
    @Override public String displayGuiScreenMethodName() { return "a"; }
    @Override public String currentScreenFieldName() { return "s"; }
    @Override public String ingameGuiFieldName() { return "w"; }
    @Override public String chatLinesFieldName() { return "d"; }
    @Override public String cursorItemFieldName() { return "g"; }
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "axr"; }
    @Override public String sessionFieldName() { return "k"; }
    @Override public String sessionUsernameFieldName() { return "a"; }
    @Override public String clickCooldownFieldName() { return "Y"; }
    @Override public String playerControllerFieldName() { return "b"; }
    @Override public String digMethodName() { return "b"; }
    @Override public String chatLineTextFieldName() { return "b"; }
    @Override public String guiScreenWidthFieldName() { return "h"; }
    @Override public String guiScreenHeightFieldName() { return "i"; }
    @Override public String keyBindingPressedFieldName() { return "e"; }
    @Override public String forwardKeyFieldName() { return "I"; }
    @Override public String leftKeyFieldName() { return "J"; }
    @Override public String backKeyFieldName() { return "K"; }
    @Override public String rightKeyFieldName() { return "L"; }
    @Override public String jumpKeyFieldName() { return "M"; }
    @Override public String sneakKeyFieldName() { return "Q"; }
    @Override public String creativeInventoryClassName() { return "ayy"; }
    @Override public String guiInventoryClassName() { return "azg"; }
    @Override public String closeContainerMethodName() { return "h"; }
}
