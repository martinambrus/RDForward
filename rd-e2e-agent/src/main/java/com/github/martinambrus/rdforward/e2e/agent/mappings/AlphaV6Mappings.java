package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Alpha 1.2.6 (protocol v6).
 * Verified by field dump of the official Alpha 1.2.6 client JAR.
 *
 * Class names are human-readable but ALL field and method names use
 * single-letter ProGuard obfuscation (NOT SRG names — those only exist
 * in RetroMCP-deobfuscated JARs, not in official Mojang client JARs).
 */
public class AlphaV6Mappings implements FieldMappings {

    @Override
    public String minecraftClassName() {
        return "net.minecraft.client.Minecraft";
    }

    // run() is NOT obfuscated (implements Runnable)
    @Override
    public String runMethodName() {
        return "run";
    }

    // i() = runTick (obfuscated as single letter "i")
    @Override
    public String tickMethodName() {
        return "i";
    }

    // g = thePlayer (type bq = EntityPlayerSP)
    @Override
    public String playerFieldName() {
        return "g";
    }

    // e = theWorld (type cy = World)
    @Override
    public String worldFieldName() {
        return "e";
    }

    // V = serverName (String, null when no server set)
    @Override
    public String serverHostFieldName() {
        return "V";
    }

    // W = serverPort (int, 0 when no server set)
    @Override
    public String serverPortFieldName() {
        return "W";
    }

    // c = displayWidth (int, default 854)
    @Override
    public String displayWidthFieldName() {
        return "c";
    }

    // d = displayHeight (int, default 480)
    @Override
    public String displayHeightFieldName() {
        return "d";
    }

    // Entity position fields in Entity base class (lw)
    // aw=posX, ax=posY, ay=posZ (verified from decompiled setPosition/b(d,d,d))
    @Override
    public String posXFieldName() {
        return "aw";
    }

    @Override
    public String posYFieldName() {
        return "ax";
    }

    @Override
    public String posZFieldName() {
        return "ay";
    }

    // --- Phase 2: Input/Inventory/World fields ---

    // y = GameSettings (type gq) on Minecraft
    @Override
    public String gameSettingsFieldName() {
        return "y";
    }

    // a = movementInput (type nn) on bq (EntityPlayerSP)
    @Override
    public String movementInputFieldName() {
        return "a";
    }

    // f = boolean[10] on he (MovementInputFromOptions)
    @Override
    public String pressedKeysFieldName() {
        return "f";
    }

    // aC = yaw (float) on lw (Entity)
    @Override
    public String yawFieldName() {
        return "aC";
    }

    // aD = pitch (float) on lw (Entity)
    @Override
    public String pitchFieldName() {
        return "aD";
    }

    // aH = onGround (boolean) on lw (Entity)
    @Override
    public String onGroundFieldName() {
        return "aH";
    }

    // e = inventory (type fo = InventoryPlayer) on eb (EntityPlayer)
    @Override
    public String inventoryFieldName() {
        return "e";
    }

    // a = mainInventory (type fp[] = ItemStack[]) on fo (InventoryPlayer)
    @Override
    public String mainInventoryFieldName() {
        return "a";
    }

    // d = currentItem (int) on fo (InventoryPlayer)
    @Override
    public String currentItemFieldName() {
        return "d";
    }

    // c = itemID (int) on fp (ItemStack)
    // Verified: fp.a=stackSize, fp.b=itemDamage, fp.c=itemID, fp.d=animationsToGo
    @Override
    public String itemIdFieldName() {
        return "c";
    }

    // a = stackSize (int) on fp (ItemStack)
    @Override
    public String stackSizeFieldName() {
        return "a";
    }

    // a = getBlockId(int,int,int) on cy (World)
    @Override
    public String getBlockIdMethodName() {
        return "a";
    }

    // a = click handler (private void a(int)) on Minecraft
    @Override
    public String clickMethodName() {
        return "a";
    }

    // L = mouseGrabbed (boolean) on Minecraft
    @Override
    public String mouseGrabbedFieldName() {
        return "L";
    }

    // --- Phase 3: Chat, Q-drop, inventory GUI ---

    // a = sendChatMessage(String) on mp (EntityPlayerSP)
    @Override
    public String sendChatMessageMethodName() {
        return "a";
    }

    // a = dropPlayerItem(fp, boolean) on eb (EntityPlayer)
    @Override
    public String dropPlayerItemMethodName() {
        return "a";
    }

    // a = displayGuiScreen(bp) on Minecraft
    @Override
    public String displayGuiScreenMethodName() {
        return "a";
    }

    // p = currentScreen (type bp = GuiScreen) on Minecraft
    @Override
    public String currentScreenFieldName() {
        return "p";
    }

    // u = ingameGUI (type nl = InGameHud) on Minecraft
    @Override
    public String ingameGuiFieldName() {
        return "u";
    }

    // e = chatLines (List of me = ChatLine) on nl (InGameHud)
    @Override
    public String chatLinesFieldName() {
        return "e";
    }

    // e = cursorItem (type fp = ItemStack) on fo (InventoryPlayer)
    @Override
    public String cursorItemFieldName() {
        return "e";
    }

    // c = craftingInventory (type fp[] = ItemStack[]) on fo (InventoryPlayer)
    @Override
    public String craftingInventoryFieldName() {
        return "c";
    }

    // bp = GuiScreen class name
    @Override
    public String guiScreenClassName() {
        return "bp";
    }
}
