package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Beta 1.9 Pre-release 5 (protocol v21).
 * Verified by CFR decompile of the OmniArchive b1.9-pre5.jar.
 *
 * Key differences from Beta 1.8.1 (BetaV17Mappings):
 * - Many fields shifted due to new classes (enchanting, brewing, The End, XP)
 * - clickMethodName changed from "a" to "c"
 * - closeContainerMethodName now available ("ac")
 * - GuiScreen width/height shifted from m/n to q/r
 */
public class BetaV21Mappings implements FieldMappings {

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

    // h = thePlayer (type tn = EntityPlayerSP)
    @Override
    public String playerFieldName() {
        return "h";
    }

    // f = theWorld (type uu = World)
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

    // Entity (md) position fields
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

    // A = GameSettings (type gv) on Minecraft
    @Override
    public String gameSettingsFieldName() {
        return "A";
    }

    // a = movementInput (type nl) on tn (EntityPlayerSP)
    @Override
    public String movementInputFieldName() {
        return "a";
    }

    // Beta 1.9 does NOT have a boolean[] pressedKeys array.
    // Movement uses KeyBinding.pressed fields on GameSettings instead.
    @Override
    public String pressedKeysFieldName() {
        return null;
    }

    // u = yaw (float) on md (Entity)
    @Override
    public String yawFieldName() {
        return "u";
    }

    // v = pitch (float) on md (Entity)
    @Override
    public String pitchFieldName() {
        return "v";
    }

    // z = onGround (boolean) on md (Entity)
    @Override
    public String onGroundFieldName() {
        return "z";
    }

    // ar = inventory (type xp = InventoryPlayer) on wd (EntityPlayer)
    @Override
    public String inventoryFieldName() {
        return "ar";
    }

    // a = mainInventory (type xs[] = ItemStack[36]) on xp (InventoryPlayer)
    @Override
    public String mainInventoryFieldName() {
        return "a";
    }

    // c = currentItem (int) on xp (InventoryPlayer)
    @Override
    public String currentItemFieldName() {
        return "c";
    }

    // c = itemID (int) on xs (ItemStack)
    @Override
    public String itemIdFieldName() {
        return "c";
    }

    // a = stackSize (int) on xs (ItemStack)
    @Override
    public String stackSizeFieldName() {
        return "a";
    }

    // a = getBlockId(int,int,int) on uu (World) -> returns int
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

    // a = sendChatMessage(String) on aem (EntityClientPlayerMP)
    @Override
    public String sendChatMessageMethodName() {
        return "a";
    }

    // a = dropPlayerItem(xs, boolean) on wd (EntityPlayer)
    @Override
    public String dropPlayerItemMethodName() {
        return "a";
    }

    // ap = dropOneItem() on aem (EntityClientPlayerMP)
    // Sends Packet14BlockDig(status=4) to the server
    @Override
    public String dropOneItemMethodName() {
        return "ap";
    }

    // a = displayGuiScreen(tm) on Minecraft
    @Override
    public String displayGuiScreenMethodName() {
        return "a";
    }

    // s = currentScreen (type tm = GuiScreen) on Minecraft
    @Override
    public String currentScreenFieldName() {
        return "s";
    }

    // w = ingameGUI (type afl = GuiIngame) on Minecraft
    @Override
    public String ingameGuiFieldName() {
        return "w";
    }

    // e = chatLines (List<mk>) on afl (GuiIngame)
    @Override
    public String chatLinesFieldName() {
        return "e";
    }

    // f = cursorItem (type xs = ItemStack, private) on xp (InventoryPlayer)
    @Override
    public String cursorItemFieldName() {
        return "f";
    }

    // Beta 1.9 uses Container system, no direct ItemStack[] craftingInventory
    @Override
    public String craftingInventoryFieldName() {
        return null;
    }

    // tm = GuiScreen class name
    @Override
    public String guiScreenClassName() {
        return "tm";
    }

    // --- Phase 6: Session field ---

    // k = session (type ek) on Minecraft
    @Override
    public String sessionFieldName() { return "k"; }
    // sessionUsernameFieldName default "b" is correct for Beta too

    // --- Phase 4: Beta-specific overrides ---

    // aa = clickCooldown (int, private) on Minecraft
    @Override
    public String clickCooldownFieldName() {
        return "aa";
    }

    // c = playerController (type jf) on Minecraft
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

    // q = width (int) on tm (GuiScreen)
    @Override
    public String guiScreenWidthFieldName() {
        return "q";
    }

    // r = height (int) on tm (GuiScreen)
    @Override
    public String guiScreenHeightFieldName() {
        return "r";
    }

    // --- KeyBinding-based movement ---

    // e = pressed (boolean) on acp (KeyBinding)
    @Override
    public String keyBindingPressedFieldName() {
        return "e";
    }

    // GameSettings (gv) key binding fields
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

    // qz = GuiContainerCreative
    @Override
    public String creativeInventoryClassName() {
        return "qz";
    }

    // afd = GuiInventory (survival)
    @Override
    public String guiInventoryClassName() {
        return "afd";
    }

    // ac = closeContainer() on aem (EntityClientPlayerMP)
    @Override
    public String closeContainerMethodName() {
        return "ac";
    }
}
