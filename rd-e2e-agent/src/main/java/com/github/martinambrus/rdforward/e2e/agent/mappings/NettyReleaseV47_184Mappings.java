package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.8.4 (protocol v47).
 * Verified by CFR decompilation of the 1.8.4 client JAR.
 *
 * Class hierarchy:
 *   Minecraft = ave, Entity = pk, EntityLivingBase = pr, EntityPlayer = wn,
 *   AbstractClientPlayer = bet, EntityPlayerSP = bew,
 *   World = adm, WorldClient = bdb, GameSettings = avh, KeyBinding = avb,
 *   InventoryPlayer = wm, ItemStack = zx, Item = zw,
 *   GuiScreen = axu, GuiIngame = avo, GuiNewChat = avt, ChatLine = ava,
 *   PlayerControllerMP = bda, Session = avm, MovementInput = beu,
 *   GuiInventory = azc, GuiContainerCreative = ayu.
 *
 * Notes:
 * - clickMethodName: 1.8.4 has no single click(int) dispatcher. Left=aw(), right=ax(), middle=az()
 *   on ave (all private no-arg). Agent code that calls clickMethod(int) needs adaptation.
 * - itemIdFieldName: 1.8.4 uses Item object (zw) field "d" instead of int itemID.
 * - chatLinesFieldName: chatLines list "h" is on GuiNewChat (avt), not directly on GuiIngame (avo).
 *   ingameGuiFieldName "q" returns avo; avo.l is the avt; avt.h is the List of ChatLine (ava).
 * - chatLineTextFieldName: ChatLine.b is IChatComponent (eu), not String. Use eu.c() for text.
 * - getBlockIdMethodName: World.p(cj) returns IBlockState (alz), not int block ID.
 * - dropOneItemMethodName: null because drop uses a(boolean) with boolean param, not no-arg.
 */
public class NettyReleaseV47_184Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "ave"; }
    @Override public String runMethodName() { return "a"; } // ave.a() is the run loop
    @Override public String tickMethodName() { return "s"; } // ave.s() is the tick method
    @Override public String playerFieldName() { return "h"; } // bew h (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // bdb f (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; } // int d
    @Override public String displayHeightFieldName() { return "e"; } // int e
    @Override public String posXFieldName() { return "s"; } // double s on pk (Entity)
    @Override public String posYFieldName() { return "t"; } // double t on pk (Entity)
    @Override public String posZFieldName() { return "u"; } // double u on pk (Entity)
    @Override public String gameSettingsFieldName() { return "t"; } // avh t (GameSettings)
    @Override public String movementInputFieldName() { return "b"; } // beu b on bew (EntityPlayerSP)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "y"; } // float y on pk (Entity)
    @Override public String pitchFieldName() { return "z"; } // float z on pk (Entity)
    @Override public String onGroundFieldName() { return "C"; } // boolean C on pk (Entity)
    @Override public String inventoryFieldName() { return "bi"; } // wm bi on wn (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // zx[] a on wm (InventoryPlayer)
    @Override public String currentItemFieldName() { return "c"; } // int c on wm (InventoryPlayer)
    @Override public String itemIdFieldName() { return "d"; } // zw d on zx (Item object, not int)
    @Override public String stackSizeFieldName() { return "b"; } // int b on zx (ItemStack)
    @Override public String getBlockIdMethodName() { return "p"; } // adm.p(cj) returns alz (IBlockState)
    @Override public String clickMethodName() { return "aw"; } // ave.aw() left-click attack (no int param)
    @Override public String mouseGrabbedFieldName() { return "w"; } // boolean w (inGameHasFocus)
    @Override public String sendChatMessageMethodName() { return "e"; } // bew.e(String)
    @Override public String dropPlayerItemMethodName() { return "a"; } // wn.a(boolean) / bew.a(boolean)
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // ave.a(axu)
    @Override public String currentScreenFieldName() { return "m"; } // axu m on ave
    @Override public String ingameGuiFieldName() { return "q"; } // avo q on ave (GuiIngame)
    @Override public String chatLinesFieldName() { return "h"; } // List h on avt (GuiNewChat, accessed via avo.l)
    @Override public String cursorItemFieldName() { return "f"; } // zx f on wm (InventoryPlayer)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "axu"; } // GuiScreen
    @Override public String sessionFieldName() { return "ae"; } // avm ae on ave (Session)
    @Override public String sessionUsernameFieldName() { return "a"; } // String a on avm (username)
    @Override public String clickCooldownFieldName() { return "ag"; } // int ag on ave (leftClickCounter)
    @Override public String playerControllerFieldName() { return "c"; } // bda c on ave (PlayerControllerMP)
    @Override public String digMethodName() { return "b"; } // bda.b(cj, cq) clickBlock
    @Override public String chatLineTextFieldName() { return "b"; } // eu b on ava (IChatComponent, not String)
    @Override public String guiScreenWidthFieldName() { return "l"; } // int l on axu (GuiScreen)
    @Override public String guiScreenHeightFieldName() { return "m"; } // int m on axu (GuiScreen)
    @Override public String keyBindingPressedFieldName() { return "h"; } // boolean h on avb (KeyBinding)
    @Override public String forwardKeyFieldName() { return "W"; } // avb W on avh (key.forward)
    @Override public String leftKeyFieldName() { return "X"; } // avb X on avh (key.left)
    @Override public String backKeyFieldName() { return "Y"; } // avb Y on avh (key.back)
    @Override public String rightKeyFieldName() { return "Z"; } // avb Z on avh (key.right)
    @Override public String jumpKeyFieldName() { return "aa"; } // avb aa on avh (key.jump)
    @Override public String sneakKeyFieldName() { return "ab"; } // avb ab on avh (key.sneak)
    @Override public String creativeInventoryClassName() { return "ayu"; } // GuiContainerCreative
    @Override public String guiInventoryClassName() { return "azc"; } // GuiInventory
    @Override public String closeContainerMethodName() { return "n"; } // bew.n() sends close + resets
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "ax"; } // ave.ax() right-click/use (no-arg)
    @Override public String chatTextMethodName() { return "c"; } // eu.c() IChatComponent -> formatted String
    @Override public boolean isNettyClient() { return true; }
}
