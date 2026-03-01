package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.4.2 (pre-Netty protocol v47).
 * Verified by CFR decompilation of the 1.4.2 client JAR.
 * This is distinct from the Netty-era protocol v47 (Release 1.8).
 *
 * Similar layout to 1.3.1 (V39) but with shifted fields due to new features
 * (anvil, beacon, wither, item frames, etc.).
 * inventoryFieldName: by->bK, dropOneItem: bB->bN, key bindings shifted: w->A etc.
 * sendChatMessage: d->c, closeContainer: j->i.
 * Note: chatLines on GuiNewChat (arm.c), not directly on GuiIngame.
 */
public class ReleaseV47PreNettyMappings implements FieldMappings {

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
        return "l";
    }

    @Override
    public String playerFieldName() {
        return "g";
    }

    @Override
    public String worldFieldName() {
        return "e";
    }

    @Override
    public String serverHostFieldName() {
        return "ae";
    }

    @Override
    public String serverPortFieldName() {
        return "af";
    }

    @Override
    public String displayWidthFieldName() {
        return "c";
    }

    @Override
    public String displayHeightFieldName() {
        return "d";
    }

    // Entity (lb) position fields
    @Override
    public String posXFieldName() {
        return "t";
    }

    @Override
    public String posYFieldName() {
        return "u";
    }

    @Override
    public String posZFieldName() {
        return "v";
    }

    // y = GameSettings (type ard)
    @Override
    public String gameSettingsFieldName() {
        return "y";
    }

    // b = movementInput on aym (EntityPlayerSP)
    @Override
    public String movementInputFieldName() {
        return "b";
    }

    @Override
    public String pressedKeysFieldName() {
        return null;
    }

    @Override
    public String yawFieldName() {
        return "z";
    }

    @Override
    public String pitchFieldName() {
        return "A";
    }

    @Override
    public String onGroundFieldName() {
        return "E";
    }

    // bK = inventory (type qf = InventoryPlayer) on qg (EntityPlayer)
    @Override
    public String inventoryFieldName() {
        return "bK";
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

    @Override
    public String clickMethodName() {
        return "c";
    }

    @Override
    public String mouseGrabbedFieldName() {
        return "G";
    }

    // c = sendChatMessage(String) on axc (EntityClientPlayerMP)
    @Override
    public String sendChatMessageMethodName() {
        return "c";
    }

    // a = dropPlayerItem(tv, boolean) on qg (EntityPlayer)
    @Override
    public String dropPlayerItemMethodName() {
        return "a";
    }

    // bN = dropOneItem() on qg (EntityPlayer)
    @Override
    public String dropOneItemMethodName() {
        return "bN";
    }

    @Override
    public String displayGuiScreenMethodName() {
        return "a";
    }

    @Override
    public String currentScreenFieldName() {
        return "r";
    }

    @Override
    public String ingameGuiFieldName() {
        return "v";
    }

    // c = chatLines (List) on arm (GuiNewChat)
    @Override
    public String chatLinesFieldName() {
        return "c";
    }

    // g = cursorItem on qf (InventoryPlayer)
    @Override
    public String cursorItemFieldName() {
        return "g";
    }

    @Override
    public String craftingInventoryFieldName() {
        return null;
    }

    // asw = GuiScreen
    @Override
    public String guiScreenClassName() {
        return "asw";
    }

    @Override
    public String sessionFieldName() {
        return "j";
    }

    @Override
    public String clickCooldownFieldName() {
        return "W";
    }

    @Override
    public String playerControllerFieldName() {
        return "b";
    }

    // b = clickBlock on awy (PlayerControllerMP)
    @Override
    public String digMethodName() {
        return "b";
    }

    // b = text on aqh (ChatLine)
    @Override
    public String chatLineTextFieldName() {
        return "b";
    }

    // f = width on asw (GuiScreen)
    @Override
    public String guiScreenWidthFieldName() {
        return "f";
    }

    // g = height on asw (GuiScreen)
    @Override
    public String guiScreenHeightFieldName() {
        return "g";
    }

    @Override
    public String keyBindingPressedFieldName() {
        return "e";
    }

    // GameSettings (ard) key binding fields - shifted from 1.3.1
    @Override
    public String forwardKeyFieldName() {
        return "A";
    }

    @Override
    public String leftKeyFieldName() {
        return "B";
    }

    @Override
    public String backKeyFieldName() {
        return "C";
    }

    @Override
    public String rightKeyFieldName() {
        return "D";
    }

    @Override
    public String jumpKeyFieldName() {
        return "E";
    }

    @Override
    public String sneakKeyFieldName() {
        return "I";
    }

    // aud = GuiContainerCreative
    @Override
    public String creativeInventoryClassName() {
        return "aud";
    }

    // auk = GuiInventory (survival)
    @Override
    public String guiInventoryClassName() {
        return "auk";
    }

    // i = closeContainer() on axc (EntityClientPlayerMP)
    @Override
    public String closeContainerMethodName() {
        return "i";
    }
}
