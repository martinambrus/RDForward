package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.8 (protocol v47).
 * Verified by CFR decompilation of the 1.8 client JAR.
 *
 * Class hierarchy:
 *   Minecraft = bsu, Entity = wv, EntityLivingBase = xm, EntityPlayer = ahd,
 *   AbstractClientPlayer = cil, EntityPlayerSP = cio,
 *   World = aqu, WorldClient = cen, GameSettings = bto, KeyBinding = bsr,
 *   InventoryPlayer = ahb, ItemStack = amj, Item = alq,
 *   GuiScreen = bxf, GuiIngame = btz, GuiNewChat = buh, ChatLine = bsq,
 *   PlayerControllerMP = cem, Session = btw, MovementInput = cim,
 *   GuiInventory = bzj, GuiContainerCreative = byz.
 *
 * Notes:
 * - clickMethodName: 1.8 has no single click(int) dispatcher. Left=at(), right=au(), middle=aw()
 *   on bsu (all private no-arg). Agent code that calls clickMethod(int) needs adaptation.
 * - itemIdFieldName: 1.8 uses Item object (alq) field "d" instead of int itemID.
 * - chatLinesFieldName: chatLines list "h" is on GuiNewChat (buh), not directly on GuiIngame (btz).
 *   ingameGuiFieldName "q" returns btz; btz.l is the buh; buh.h is the List of ChatLine (bsq).
 * - chatLineTextFieldName: ChatLine.b is IChatComponent (ho), not String. Use ho.c() for text.
 * - getBlockIdMethodName: World.p(dt) returns IBlockState (bec), not int block ID.
 * - dropOneItemMethodName: null because drop uses a(boolean) with boolean param, not no-arg.
 */
public class NettyReleaseV47Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "bsu"; }
    @Override public String runMethodName() { return "a"; } // bsu.a() is the run loop
    @Override public String tickMethodName() { return "r"; } // bsu.r() is the tick method
    @Override public String playerFieldName() { return "h"; } // cio h (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // cen f (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; } // int d
    @Override public String displayHeightFieldName() { return "e"; } // int e
    @Override public String posXFieldName() { return "s"; } // double s on wv (Entity)
    @Override public String posYFieldName() { return "t"; } // double t on wv (Entity)
    @Override public String posZFieldName() { return "u"; } // double u on wv (Entity)
    @Override public String gameSettingsFieldName() { return "t"; } // bto t (GameSettings)
    @Override public String movementInputFieldName() { return "b"; } // cim b on cio (EntityPlayerSP)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "y"; } // float y on wv (Entity)
    @Override public String pitchFieldName() { return "z"; } // float z on wv (Entity)
    @Override public String onGroundFieldName() { return "C"; } // boolean C on wv (Entity)
    @Override public String inventoryFieldName() { return "bg"; } // ahb bg on ahd (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // amj[] a on ahb (InventoryPlayer)
    @Override public String currentItemFieldName() { return "c"; } // int c on ahb (InventoryPlayer)
    @Override public String itemIdFieldName() { return "d"; } // alq d on amj (Item object, not int)
    @Override public String stackSizeFieldName() { return "b"; } // int b on amj (ItemStack)
    @Override public String getBlockIdMethodName() { return "p"; } // aqu.p(dt) returns bec (IBlockState)
    @Override public String clickMethodName() { return "at"; } // bsu.at() left-click attack (no int param)
    @Override public String mouseGrabbedFieldName() { return "w"; } // boolean w (inGameHasFocus)
    @Override public String sendChatMessageMethodName() { return "e"; } // cio.e(String)
    @Override public String dropPlayerItemMethodName() { return "a"; } // ahd.a(boolean) / cio.a(boolean)
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // bsu.a(bxf)
    @Override public String currentScreenFieldName() { return "m"; } // bxf m on bsu
    @Override public String ingameGuiFieldName() { return "q"; } // btz q on bsu (GuiIngame)
    @Override public String chatLinesFieldName() { return "h"; } // List h on buh (GuiNewChat, accessed via btz.l)
    @Override public String cursorItemFieldName() { return "f"; } // amj f on ahb (InventoryPlayer)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "bxf"; } // GuiScreen
    @Override public String sessionFieldName() { return "aa"; } // btw aa on bsu (Session)
    @Override public String sessionUsernameFieldName() { return "a"; } // String a on btw (username)
    @Override public String clickCooldownFieldName() { return "ac"; } // int ac on bsu (leftClickCounter)
    @Override public String playerControllerFieldName() { return "c"; } // cem c on bsu (PlayerControllerMP)
    @Override public String digMethodName() { return "b"; } // cem.b(dt, ej) clickBlock
    @Override public String chatLineTextFieldName() { return "b"; } // ho b on bsq (IChatComponent, not String)
    @Override public String guiScreenWidthFieldName() { return "l"; } // int l on bxf (GuiScreen)
    @Override public String guiScreenHeightFieldName() { return "m"; } // int m on bxf (GuiScreen)
    @Override public String keyBindingPressedFieldName() { return "h"; } // boolean h on bsr (KeyBinding)
    @Override public String forwardKeyFieldName() { return "U"; } // bsr U on bto (key.forward)
    @Override public String leftKeyFieldName() { return "V"; } // bsr V on bto (key.left)
    @Override public String backKeyFieldName() { return "W"; } // bsr W on bto (key.back)
    @Override public String rightKeyFieldName() { return "X"; } // bsr X on bto (key.right)
    @Override public String jumpKeyFieldName() { return "Y"; } // bsr Y on bto (key.jump)
    @Override public String sneakKeyFieldName() { return "Z"; } // bsr Z on bto (key.sneak)
    @Override public String creativeInventoryClassName() { return "byz"; } // GuiContainerCreative
    @Override public String guiInventoryClassName() { return "bzj"; } // GuiInventory
    @Override public String closeContainerMethodName() { return "n"; } // cio.n() sends close + resets
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "au"; } // bsu.au() right-click/use (no-arg)
    @Override public String chatTextMethodName() { return "c"; } // ho.c() IChatComponent -> formatted String
    @Override public boolean isNettyClient() { return true; }
}
