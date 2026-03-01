package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.2.1 (protocol v28).
 * Verified by CFR decompilation of the 1.2.1 client JAR.
 *
 * Key differences from Release 1.1 (ReleaseV23Mappings):
 * - Entity class changed: ms->nk, but field names unchanged (o/p/q/u/v/z)
 * - GuiScreen: ug->vl, width/height still q/r
 * - dropOneItemMethodName: ar->as
 * - clickMethodName changed: c->a (now uses 'a' for click handler)
 * - Creative/survival GUI class names changed: rt->sn, agi->aih
 */
public class ReleaseV28Mappings implements FieldMappings {

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

    // Entity (nk) position fields
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

    // a = movementInput (type ou) on vm (EntityPlayerSP)
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

    // ap = inventory (type aaf = InventoryPlayer) on yr (EntityPlayer)
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

    // as = dropPlayerItem on yr (EntityPlayer)
    @Override
    public String dropPlayerItemMethodName() {
        return "as";
    }

    // as = dropOneItem() on yr (EntityPlayer) / ahp (EntityClientPlayerMP)
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

    // vl = GuiScreen
    @Override
    public String guiScreenClassName() {
        return "vl";
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

    // c = clickBlock on kf (PlayerController) subclass
    @Override
    public String digMethodName() {
        return "c";
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

    // sn = GuiContainerCreative
    @Override
    public String creativeInventoryClassName() {
        return "sn";
    }

    // aih = GuiInventory (survival)
    @Override
    public String guiInventoryClassName() {
        return "aih";
    }

    // af = closeContainer() on ahp (EntityClientPlayerMP)
    @Override
    public String closeContainerMethodName() {
        return "af";
    }
}
