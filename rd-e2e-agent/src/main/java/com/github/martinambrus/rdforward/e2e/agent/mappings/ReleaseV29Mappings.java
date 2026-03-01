package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.2.4 (protocol v29).
 * Verified by CFR decompilation of the 1.2.4 client JAR.
 *
 * Very similar to 1.2.1 (ReleaseV28Mappings) - same Entity/position layout.
 * Differences: GuiScreen vl->vp, GUI class names shifted, clickMethodName a->c.
 */
public class ReleaseV29Mappings implements FieldMappings {

    @Override
    public String minecraftClassName() {
        return "net.minecraft.client.Minecraft";
    }

    @Override
    public String runMethodName() {
        return "run";
    }

    @Override
    public String tickMethodName() {
        return "k";
    }

    @Override
    public String playerFieldName() {
        return "h";
    }

    @Override
    public String worldFieldName() {
        return "f";
    }

    @Override
    public String serverHostFieldName() {
        return "af";
    }

    @Override
    public String serverPortFieldName() {
        return "ag";
    }

    @Override
    public String displayWidthFieldName() {
        return "d";
    }

    @Override
    public String displayHeightFieldName() {
        return "e";
    }

    // Entity (nn) position fields
    @Override
    public String posXFieldName() {
        return "o";
    }

    @Override
    public String posYFieldName() {
        return "p";
    }

    @Override
    public String posZFieldName() {
        return "q";
    }

    @Override
    public String gameSettingsFieldName() {
        return "A";
    }

    // a = movementInput on vq (EntityPlayerSP)
    @Override
    public String movementInputFieldName() {
        return "a";
    }

    @Override
    public String pressedKeysFieldName() {
        return null;
    }

    @Override
    public String yawFieldName() {
        return "u";
    }

    @Override
    public String pitchFieldName() {
        return "v";
    }

    @Override
    public String onGroundFieldName() {
        return "z";
    }

    // ap = inventory (type aak = InventoryPlayer) on yw (EntityPlayer)
    @Override
    public String inventoryFieldName() {
        return "ap";
    }

    @Override
    public String mainInventoryFieldName() {
        return "a";
    }

    @Override
    public String currentItemFieldName() {
        return "c";
    }

    @Override
    public String itemIdFieldName() {
        return "c";
    }

    @Override
    public String stackSizeFieldName() {
        return "a";
    }

    @Override
    public String getBlockIdMethodName() {
        return "a";
    }

    // c = click handler (private void c(int)) on Minecraft
    @Override
    public String clickMethodName() {
        return "c";
    }

    @Override
    public String mouseGrabbedFieldName() {
        return "R";
    }

    @Override
    public String sendChatMessageMethodName() {
        return "a";
    }

    // a = dropPlayerItem(aan, boolean) on yw (EntityPlayer)
    @Override
    public String dropPlayerItemMethodName() {
        return "a";
    }

    // as = dropOneItem() on ahv (EntityClientPlayerMP)
    @Override
    public String dropOneItemMethodName() {
        return "as";
    }

    @Override
    public String displayGuiScreenMethodName() {
        return "a";
    }

    @Override
    public String currentScreenFieldName() {
        return "s";
    }

    @Override
    public String ingameGuiFieldName() {
        return "w";
    }

    @Override
    public String chatLinesFieldName() {
        return "e";
    }

    @Override
    public String cursorItemFieldName() {
        return "f";
    }

    @Override
    public String craftingInventoryFieldName() {
        return null;
    }

    // vp = GuiScreen
    @Override
    public String guiScreenClassName() {
        return "vp";
    }

    @Override
    public String sessionFieldName() {
        return "k";
    }

    @Override
    public String clickCooldownFieldName() {
        return "aa";
    }

    @Override
    public String playerControllerFieldName() {
        return "c";
    }

    // a = clickBlock on ki (PlayerController)
    @Override
    public String digMethodName() {
        return "a";
    }

    @Override
    public String chatLineTextFieldName() {
        return "a";
    }

    @Override
    public String guiScreenWidthFieldName() {
        return "q";
    }

    @Override
    public String guiScreenHeightFieldName() {
        return "r";
    }

    @Override
    public String keyBindingPressedFieldName() {
        return "e";
    }

    @Override
    public String forwardKeyFieldName() {
        return "n";
    }

    @Override
    public String leftKeyFieldName() {
        return "o";
    }

    @Override
    public String backKeyFieldName() {
        return "p";
    }

    @Override
    public String rightKeyFieldName() {
        return "q";
    }

    @Override
    public String jumpKeyFieldName() {
        return "r";
    }

    @Override
    public String sneakKeyFieldName() {
        return "v";
    }

    // sr = GuiContainerCreative
    @Override
    public String creativeInventoryClassName() {
        return "sr";
    }

    // ain = GuiInventory (survival)
    @Override
    public String guiInventoryClassName() {
        return "ain";
    }

    // af = closeContainer() on ahv (EntityClientPlayerMP)
    @Override
    public String closeContainerMethodName() {
        return "af";
    }
}
