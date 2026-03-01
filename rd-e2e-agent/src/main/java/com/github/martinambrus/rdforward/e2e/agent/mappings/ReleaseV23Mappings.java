package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.1 (protocol v23).
 * Verified by CFR decompilation of the 1.1 client JAR.
 *
 * Key differences from Release 1.0 (ReleaseV22Mappings):
 * - Entity pos/rot fields shifted back to Beta layout: s->o, t->p, u->q, y->u, z->v, D->z
 * - inventoryFieldName: by->ap
 * - movementInputFieldName: b->a (back to Beta layout)
 * - GuiScreen: xe->ug, width/height: m/n->q/r
 * - dropOneItemMethodName: l_->ar
 * - closeContainerMethodName: m->af
 * - digMethodName: b->a
 * - Creative/survival GUI class names changed
 */
public class ReleaseV23Mappings implements FieldMappings {

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

    // Entity (ms) position fields
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

    // a = movementInput (type ob) on uh (EntityPlayerSP)
    @Override
    public String movementInputFieldName() {
        return "a";
    }

    @Override
    public String pressedKeysFieldName() {
        return null;
    }

    // u = yaw on ms (Entity)
    @Override
    public String yawFieldName() {
        return "u";
    }

    // v = pitch on ms (Entity)
    @Override
    public String pitchFieldName() {
        return "v";
    }

    // z = onGround on ms (Entity)
    @Override
    public String onGroundFieldName() {
        return "z";
    }

    // ap = inventory (type yn = InventoryPlayer) on xb (EntityPlayer)
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

    // c = click handler on Minecraft
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

    @Override
    public String dropPlayerItemMethodName() {
        return "a";
    }

    // ar = dropOneItem() on afr (EntityClientPlayerMP)
    @Override
    public String dropOneItemMethodName() {
        return "ar";
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

    // ug = GuiScreen
    @Override
    public String guiScreenClassName() {
        return "ug";
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

    @Override
    public String digMethodName() {
        return "a";
    }

    @Override
    public String chatLineTextFieldName() {
        return "a";
    }

    // q = width on ug (GuiScreen)
    @Override
    public String guiScreenWidthFieldName() {
        return "q";
    }

    // r = height on ug (GuiScreen)
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

    // rt = GuiContainerCreative
    @Override
    public String creativeInventoryClassName() {
        return "rt";
    }

    // agi = GuiInventory (survival)
    @Override
    public String guiInventoryClassName() {
        return "agi";
    }

    // af = closeContainer() on afr (EntityClientPlayerMP)
    @Override
    public String closeContainerMethodName() {
        return "af";
    }
}
