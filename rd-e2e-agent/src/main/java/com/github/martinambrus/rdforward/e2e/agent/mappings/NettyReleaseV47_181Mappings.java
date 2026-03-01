package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.8.1 (protocol v47).
 * Verified by CFR decompilation of the 1.8.1 client JAR.
 *
 * Class hierarchy:
 *   Minecraft = bss, Entity = wx, EntityLivingBase = xo, EntityPlayer = aha,
 *   AbstractClientPlayer = cik, EntityPlayerSP = cin,
 *   World = aqr, WorldClient = cem, GameSettings = btm, KeyBinding = bsp,
 *   InventoryPlayer = agy, ItemStack = amg, Item = aln,
 *   GuiScreen = bxe, GuiIngame = btx, GuiNewChat = buf, ChatLine = bso,
 *   PlayerControllerMP = cel, Session = btu, MovementInput = cil,
 *   GuiInventory = bzi, GuiContainerCreative = byy.
 *
 * Notes:
 * - clickMethodName: 1.8.1 has no single click(int) dispatcher. Left=au(), right=av(), middle=ax()
 *   on bss (all private no-arg). Agent code that calls clickMethod(int) needs adaptation.
 * - itemIdFieldName: 1.8.1 uses Item object (aln) field "d" instead of int itemID.
 * - chatLinesFieldName: chatLines list "h" is on GuiNewChat (buf), not directly on GuiIngame (btx).
 *   ingameGuiFieldName "q" returns btx; btx.l is the buf; buf.h is the List of ChatLine (bso).
 * - chatLineTextFieldName: ChatLine.b is IChatComponent (hp), not String. Use hp.c() for text.
 * - getBlockIdMethodName: World.p(dt) returns IBlockState (bea), not int block ID.
 * - dropOneItemMethodName: null because drop uses a(boolean) with boolean param, not no-arg.
 */
public class NettyReleaseV47_181Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "bss"; }
    @Override public String runMethodName() { return "a"; } // bss.a() is the run loop
    @Override public String tickMethodName() { return "r"; } // bss.r() is the tick method
    @Override public String playerFieldName() { return "h"; } // cin h (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // cem f (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; } // int d
    @Override public String displayHeightFieldName() { return "e"; } // int e
    @Override public String posXFieldName() { return "s"; } // double s on wx (Entity)
    @Override public String posYFieldName() { return "t"; } // double t on wx (Entity)
    @Override public String posZFieldName() { return "u"; } // double u on wx (Entity)
    @Override public String gameSettingsFieldName() { return "t"; } // btm t (GameSettings)
    @Override public String movementInputFieldName() { return "b"; } // cil b on cin (EntityPlayerSP)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "y"; } // float y on wx (Entity)
    @Override public String pitchFieldName() { return "z"; } // float z on wx (Entity)
    @Override public String onGroundFieldName() { return "C"; } // boolean C on wx (Entity)
    @Override public String inventoryFieldName() { return "bi"; } // agy bi on aha (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // amg[] a on agy (InventoryPlayer)
    @Override public String currentItemFieldName() { return "c"; } // int c on agy (InventoryPlayer)
    @Override public String itemIdFieldName() { return "d"; } // aln d on amg (Item object, not int)
    @Override public String stackSizeFieldName() { return "b"; } // int b on amg (ItemStack)
    @Override public String getBlockIdMethodName() { return "p"; } // aqr.p(dt) returns bea (IBlockState)
    @Override public String clickMethodName() { return "au"; } // bss.au() left-click attack (no int param)
    @Override public String mouseGrabbedFieldName() { return "w"; } // boolean w (inGameHasFocus)
    @Override public String sendChatMessageMethodName() { return "e"; } // cin.e(String)
    @Override public String dropPlayerItemMethodName() { return "a"; } // aha.a(boolean) / cin.a(boolean)
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // bss.a(bxe)
    @Override public String currentScreenFieldName() { return "m"; } // bxe m on bss
    @Override public String ingameGuiFieldName() { return "q"; } // btx q on bss (GuiIngame)
    @Override public String chatLinesFieldName() { return "h"; } // List h on buf (GuiNewChat, accessed via btx.l)
    @Override public String cursorItemFieldName() { return "f"; } // amg f on agy (InventoryPlayer)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "bxe"; } // GuiScreen
    @Override public String sessionFieldName() { return "ab"; } // btu ab on bss (Session)
    @Override public String sessionUsernameFieldName() { return "a"; } // String a on btu (username)
    @Override public String clickCooldownFieldName() { return "ad"; } // int ad on bss (leftClickCounter)
    @Override public String playerControllerFieldName() { return "c"; } // cel c on bss (PlayerControllerMP)
    @Override public String digMethodName() { return "b"; } // cel.b(dt, ej) clickBlock
    @Override public String chatLineTextFieldName() { return "b"; } // hp b on bso (IChatComponent, not String)
    @Override public String guiScreenWidthFieldName() { return "l"; } // int l on bxe (GuiScreen)
    @Override public String guiScreenHeightFieldName() { return "m"; } // int m on bxe (GuiScreen)
    @Override public String keyBindingPressedFieldName() { return "h"; } // boolean h on bsp (KeyBinding)
    @Override public String forwardKeyFieldName() { return "W"; } // bsp W on btm (key.forward)
    @Override public String leftKeyFieldName() { return "X"; } // bsp X on btm (key.left)
    @Override public String backKeyFieldName() { return "Y"; } // bsp Y on btm (key.back)
    @Override public String rightKeyFieldName() { return "Z"; } // bsp Z on btm (key.right)
    @Override public String jumpKeyFieldName() { return "aa"; } // bsp aa on btm (key.jump)
    @Override public String sneakKeyFieldName() { return "ab"; } // bsp ab on btm (key.sneak)
    @Override public String creativeInventoryClassName() { return "byy"; } // GuiContainerCreative
    @Override public String guiInventoryClassName() { return "bzi"; } // GuiInventory
    @Override public String closeContainerMethodName() { return "n"; } // cin.n() sends close + resets
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "av"; } // bss.av() right-click/use (no-arg)
    @Override public String chatTextMethodName() { return "c"; } // hp.c() IChatComponent -> unformatted String
    @Override public boolean isNettyClient() { return true; }
}
