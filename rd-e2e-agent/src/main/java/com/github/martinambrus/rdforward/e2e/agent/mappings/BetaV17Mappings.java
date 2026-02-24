package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Beta 1.8.1 (protocol v17).
 * Verified by CFR decompile of the official Beta 1.8.1 client JAR.
 *
 * Key differences from Alpha 1.2.6:
 * - No boolean[] pressedKeys — movement uses KeyBinding.pressed on GameSettings
 * - InventoryPlayer.craftingInventory replaced by Container system
 * - Creative inventory is class "on" (GuiContainerCreative)
 * - GuiScreen width/height fields shifted from c/d to m/n
 */
public class BetaV17Mappings implements FieldMappings {

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

    // h = thePlayer (type qs = EntityPlayerSP)
    @Override
    public String playerFieldName() {
        return "h";
    }

    // f = theWorld (type rv = World)
    @Override
    public String worldFieldName() {
        return "f";
    }

    // ac = serverName (String)
    @Override
    public String serverHostFieldName() {
        return "ac";
    }

    // ad = serverPort (int)
    @Override
    public String serverPortFieldName() {
        return "ad";
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

    // Entity (kj) position fields
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

    // z = GameSettings (type fv) on Minecraft
    @Override
    public String gameSettingsFieldName() {
        return "z";
    }

    // a = movementInput (type lp) on qs (EntityPlayerSP)
    @Override
    public String movementInputFieldName() {
        return "a";
    }

    // Beta 1.8.1 does NOT have a boolean[] pressedKeys array.
    // Movement uses KeyBinding.pressed fields on GameSettings instead.
    @Override
    public String pressedKeysFieldName() {
        return null;
    }

    // u = yaw (float) on kj (Entity)
    @Override
    public String yawFieldName() {
        return "u";
    }

    // v = pitch (float) on kj (Entity)
    @Override
    public String pitchFieldName() {
        return "v";
    }

    // z = onGround (boolean) on kj (Entity)
    @Override
    public String onGroundFieldName() {
        return "z";
    }

    // as = inventory (type ui = InventoryPlayer) on sz (EntityPlayer)
    @Override
    public String inventoryFieldName() {
        return "as";
    }

    // a = mainInventory (type ul[] = ItemStack[36]) on ui (InventoryPlayer)
    @Override
    public String mainInventoryFieldName() {
        return "a";
    }

    // c = currentItem (int) on ui (InventoryPlayer)
    @Override
    public String currentItemFieldName() {
        return "c";
    }

    // c = itemID (int) on ul (ItemStack)
    @Override
    public String itemIdFieldName() {
        return "c";
    }

    // a = stackSize (int) on ul (ItemStack)
    @Override
    public String stackSizeFieldName() {
        return "a";
    }

    // a = getBlockId(int,int,int) on rv (World) -> returns int
    @Override
    public String getBlockIdMethodName() {
        return "a";
    }

    // a = click handler (private void a(int)) on Minecraft
    @Override
    public String clickMethodName() {
        return "a";
    }

    // O = mouseGrabbed (boolean) on Minecraft
    @Override
    public String mouseGrabbedFieldName() {
        return "O";
    }

    // a = sendChatMessage(String) on qs/aan (EntityPlayerSP/ClientPlayer)
    @Override
    public String sendChatMessageMethodName() {
        return "a";
    }

    // a = dropPlayerItem(ul, boolean) on sz (EntityPlayer)
    @Override
    public String dropPlayerItemMethodName() {
        return "a";
    }

    // a = displayGuiScreen(qr) on Minecraft
    @Override
    public String displayGuiScreenMethodName() {
        return "a";
    }

    // r = currentScreen (type qr = GuiScreen) on Minecraft
    @Override
    public String currentScreenFieldName() {
        return "r";
    }

    // v = ingameGUI (type abj = GuiIngame) on Minecraft
    @Override
    public String ingameGuiFieldName() {
        return "v";
    }

    // f = chatLines (List<kq>) on abj (GuiIngame)
    @Override
    public String chatLinesFieldName() {
        return "f";
    }

    // f = cursorItem (type ul = ItemStack, private) on ui (InventoryPlayer)
    @Override
    public String cursorItemFieldName() {
        return "f";
    }

    // Beta 1.8 uses Container system, no direct ItemStack[] craftingInventory
    @Override
    public String craftingInventoryFieldName() {
        return null;
    }

    // qr = GuiScreen class name
    @Override
    public String guiScreenClassName() {
        return "qr";
    }

    // --- Phase 4: Beta-specific overrides ---

    // X = clickCooldown (int, private) on Minecraft
    @Override
    public String clickCooldownFieldName() {
        return "X";
    }

    // c = playerController (type hw) on Minecraft
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

    // m = width (int) on qr (GuiScreen)
    @Override
    public String guiScreenWidthFieldName() {
        return "m";
    }

    // n = height (int) on qr (GuiScreen)
    @Override
    public String guiScreenHeightFieldName() {
        return "n";
    }

    // --- KeyBinding-based movement ---

    // e = pressed (boolean) on ys (KeyBinding)
    @Override
    public String keyBindingPressedFieldName() {
        return "e";
    }

    // GameSettings (fv) key binding fields
    @Override
    public String forwardKeyFieldName() {
        return "m";
    }

    @Override
    public String leftKeyFieldName() {
        return "n";
    }

    @Override
    public String backKeyFieldName() {
        return "o";
    }

    @Override
    public String rightKeyFieldName() {
        return "p";
    }

    @Override
    public String jumpKeyFieldName() {
        return "q";
    }

    @Override
    public String sneakKeyFieldName() {
        return "u";
    }

    // on = GuiContainerCreative
    @Override
    public String creativeInventoryClassName() {
        return "on";
    }

    // abd = GuiInventory (survival)
    @Override
    public String guiInventoryClassName() {
        return "abd";
    }
}
