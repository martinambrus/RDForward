package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.0 (protocol v22).
 * Verified by CFR decompilation of the 1.0 client JAR.
 *
 * Key differences from Beta 1.9 Pre-5 (BetaV21Mappings):
 * - Entity position/rotation fields shifted: o->s, p->t, q->u, u->y, v->z, z->D
 * - inventoryFieldName shifted: ar->by
 * - movementInputFieldName shifted: a->b
 * - GuiScreen class changed: tm->xe, width/height: q/r->m/n
 * - dropOneItemMethodName: ap->l_
 * - closeContainerMethodName: ac->m
 * - digMethodName: a->b
 * - Creative/survival GUI class names changed
 */
public class ReleaseV22Mappings implements FieldMappings {

    @Override
    public String minecraftClassName() {
        return "net.minecraft.client.Minecraft";
    }

    @Override
    public String runMethodName() {
        return "run";
    }

    // k() = runTick (called 20x/sec from run() loop)
    @Override
    public String tickMethodName() {
        return "k";
    }

    // h = thePlayer (type di = EntityPlayerSP)
    @Override
    public String playerFieldName() {
        return "h";
    }

    // f = theWorld (type ry = World)
    @Override
    public String worldFieldName() {
        return "f";
    }

    // af = serverName (String)
    @Override
    public String serverHostFieldName() {
        return "af";
    }

    // ag = serverPort (int)
    @Override
    public String serverPortFieldName() {
        return "ag";
    }

    // d = displayWidth (int)
    @Override
    public String displayWidthFieldName() {
        return "d";
    }

    // e = displayHeight (int)
    @Override
    public String displayHeightFieldName() {
        return "e";
    }

    // Entity (ia) position fields
    @Override
    public String posXFieldName() {
        return "s";
    }

    @Override
    public String posYFieldName() {
        return "t";
    }

    @Override
    public String posZFieldName() {
        return "u";
    }

    // A = GameSettings (type ki) on Minecraft
    @Override
    public String gameSettingsFieldName() {
        return "A";
    }

    // b = movementInput (type agn) on di (EntityPlayerSP)
    @Override
    public String movementInputFieldName() {
        return "b";
    }

    // Release 1.0 does NOT have a boolean[] pressedKeys array.
    // Movement uses KeyBinding.pressed fields on GameSettings instead.
    @Override
    public String pressedKeysFieldName() {
        return null;
    }

    // y = yaw (float) on ia (Entity)
    @Override
    public String yawFieldName() {
        return "y";
    }

    // z = pitch (float) on ia (Entity)
    @Override
    public String pitchFieldName() {
        return "z";
    }

    // D = onGround (boolean) on ia (Entity)
    @Override
    public String onGroundFieldName() {
        return "D";
    }

    // by = inventory (type x = InventoryPlayer) on vi (EntityPlayer)
    @Override
    public String inventoryFieldName() {
        return "by";
    }

    // a = mainInventory (type dk[] = ItemStack[36]) on x (InventoryPlayer)
    @Override
    public String mainInventoryFieldName() {
        return "a";
    }

    // c = currentItem (int) on x (InventoryPlayer)
    @Override
    public String currentItemFieldName() {
        return "c";
    }

    // c = itemID (int) on dk (ItemStack)
    @Override
    public String itemIdFieldName() {
        return "c";
    }

    // a = stackSize (int) on dk (ItemStack)
    @Override
    public String stackSizeFieldName() {
        return "a";
    }

    // a = getBlockId(int,int,int) on ry (World) -> returns int
    @Override
    public String getBlockIdMethodName() {
        return "a";
    }

    // c = click handler (private void c(int)) on Minecraft
    @Override
    public String clickMethodName() {
        return "c";
    }

    // R = mouseGrabbed (boolean) on Minecraft
    @Override
    public String mouseGrabbedFieldName() {
        return "R";
    }

    // a = sendChatMessage(String) on cx (EntityClientPlayerMP)
    @Override
    public String sendChatMessageMethodName() {
        return "a";
    }

    // a = dropPlayerItem(dk, boolean) on vi (EntityPlayer)
    @Override
    public String dropPlayerItemMethodName() {
        return "a";
    }

    // l_ = dropOneItem() on cx (EntityClientPlayerMP)
    // Sends dig packet with status=4
    @Override
    public String dropOneItemMethodName() {
        return "l_";
    }

    // a = displayGuiScreen(xe) on Minecraft
    @Override
    public String displayGuiScreenMethodName() {
        return "a";
    }

    // s = currentScreen (type xe = GuiScreen) on Minecraft
    @Override
    public String currentScreenFieldName() {
        return "s";
    }

    // w = ingameGUI (type qd = GuiIngame) on Minecraft
    @Override
    public String ingameGuiFieldName() {
        return "w";
    }

    // e = chatLines (List<ahe>) on qd (GuiIngame)
    @Override
    public String chatLinesFieldName() {
        return "e";
    }

    // f = cursorItem (type dk = ItemStack, private) on x (InventoryPlayer)
    @Override
    public String cursorItemFieldName() {
        return "f";
    }

    // Container system, no direct ItemStack[] craftingInventory
    @Override
    public String craftingInventoryFieldName() {
        return null;
    }

    // xe = GuiScreen class name
    @Override
    public String guiScreenClassName() {
        return "xe";
    }

    // k = session (type dr) on Minecraft
    @Override
    public String sessionFieldName() {
        return "k";
    }

    // aa = clickCooldown (int, private) on Minecraft
    @Override
    public String clickCooldownFieldName() {
        return "aa";
    }

    // c = playerController (type aes) on Minecraft
    @Override
    public String playerControllerFieldName() {
        return "c";
    }

    // b = clickBlock on zn (PlayerControllerMP)
    @Override
    public String digMethodName() {
        return "b";
    }

    // a = text (String) on ahe (ChatLine)
    @Override
    public String chatLineTextFieldName() {
        return "a";
    }

    // m = width (int) on xe (GuiScreen)
    @Override
    public String guiScreenWidthFieldName() {
        return "m";
    }

    // n = height (int) on xe (GuiScreen)
    @Override
    public String guiScreenHeightFieldName() {
        return "n";
    }

    // e = pressed (boolean) on aby (KeyBinding)
    @Override
    public String keyBindingPressedFieldName() {
        return "e";
    }

    // GameSettings (ki) key binding fields
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

    // aec = GuiContainerCreative
    @Override
    public String creativeInventoryClassName() {
        return "aec";
    }

    // hw = GuiInventory (survival)
    @Override
    public String guiInventoryClassName() {
        return "hw";
    }

    // m = closeContainer() on cx (EntityClientPlayerMP)
    @Override
    public String closeContainerMethodName() {
        return "m";
    }
}
