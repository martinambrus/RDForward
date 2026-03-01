package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.8.2 (protocol v47).
 * Verified by CFR decompilation of the 1.8.2 client JAR.
 *
 * Class hierarchy:
 *   Minecraft = avf, Entity = pl, EntityLivingBase = ps, EntityPlayer = wo,
 *   AbstractClientPlayer = beu, EntityPlayerSP = bex,
 *   World = adn, WorldClient = bdc, GameSettings = avi, KeyBinding = avc,
 *   InventoryPlayer = wn, ItemStack = zy, Item = zx,
 *   GuiScreen = axv, GuiIngame = avp, GuiNewChat = avu, ChatLine = avb,
 *   PlayerControllerMP = bdb, Session = avn, MovementInput = bev,
 *   GuiInventory = azd, GuiContainerCreative = ayv, IChatComponent = ev.
 *
 * Differences from 1.8.0 (NettyReleaseV47Mappings / bsu):
 * - All class names re-obfuscated (bsu->avf, wv->pl, ahd->wo, cio->bex, etc.)
 * - tickMethodName: r -> s
 * - clickMethodName (left-click): at -> aw
 * - rightClickMethodName: au -> ax
 * - sessionFieldName: aa -> ae
 * - clickCooldownFieldName: ac -> ap
 * - inventoryFieldName (on EntityPlayer): bg -> bi
 * - forwardKeyFieldName: U -> W
 * - leftKeyFieldName: V -> X
 * - backKeyFieldName: W -> Y
 * - rightKeyFieldName: X -> Z
 * - jumpKeyFieldName: Y -> aa
 * - sneakKeyFieldName: Z -> ab
 * - guiScreenClassName: bxf -> axv
 * - creativeInventoryClassName: byz -> ayv
 * - guiInventoryClassName: bzj -> azd
 *
 * Notes (same as 1.8.0):
 * - clickMethodName: 1.8 has no single click(int) dispatcher. Left=aw(), right=ax(), middle=az()
 *   on avf (all private no-arg). Agent code that calls clickMethod(int) needs adaptation.
 * - itemIdFieldName: 1.8 uses Item object (zx) field "d" instead of int itemID.
 * - chatLinesFieldName: chatLines list "h" is on GuiNewChat (avu), not directly on GuiIngame (avp).
 *   ingameGuiFieldName "q" returns avp; avp.l is the avu; avu.h is the List of ChatLine (avb).
 * - chatLineTextFieldName: ChatLine.b is IChatComponent (ev), not String. Use ev.c() for text.
 * - getBlockIdMethodName: World.p(ck) returns IBlockState (ama), not int block ID.
 * - dropOneItemMethodName: null because drop uses a(boolean) with boolean param, not no-arg.
 */
public class NettyReleaseV47_182Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "avf"; }
    @Override public String runMethodName() { return "a"; } // avf.a() is the run loop
    @Override public String tickMethodName() { return "s"; } // avf.s() is the tick method
    @Override public String playerFieldName() { return "h"; } // bex h (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // bdc f (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; } // int d
    @Override public String displayHeightFieldName() { return "e"; } // int e
    @Override public String posXFieldName() { return "s"; } // double s on pl (Entity)
    @Override public String posYFieldName() { return "t"; } // double t on pl (Entity)
    @Override public String posZFieldName() { return "u"; } // double u on pl (Entity)
    @Override public String gameSettingsFieldName() { return "t"; } // avi t (GameSettings)
    @Override public String movementInputFieldName() { return "b"; } // bev b on bex (EntityPlayerSP)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "y"; } // float y on pl (Entity)
    @Override public String pitchFieldName() { return "z"; } // float z on pl (Entity)
    @Override public String onGroundFieldName() { return "C"; } // boolean C on pl (Entity)
    @Override public String inventoryFieldName() { return "bi"; } // wn bi on wo (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // zy[] a on wn (InventoryPlayer)
    @Override public String currentItemFieldName() { return "c"; } // int c on wn (InventoryPlayer)
    @Override public String itemIdFieldName() { return "d"; } // zx d on zy (Item object, not int)
    @Override public String stackSizeFieldName() { return "b"; } // int b on zy (ItemStack)
    @Override public String getBlockIdMethodName() { return "p"; } // adn.p(ck) returns ama (IBlockState)
    @Override public String clickMethodName() { return "aw"; } // avf.aw() left-click attack (no int param)
    @Override public String mouseGrabbedFieldName() { return "w"; } // boolean w (inGameHasFocus)
    @Override public String sendChatMessageMethodName() { return "e"; } // bex.e(String)
    @Override public String dropPlayerItemMethodName() { return "a"; } // wo.a(boolean) / bex.a(boolean)
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // avf.a(axv)
    @Override public String currentScreenFieldName() { return "m"; } // axv m on avf
    @Override public String ingameGuiFieldName() { return "q"; } // avp q on avf (GuiIngame)
    @Override public String chatLinesFieldName() { return "h"; } // List h on avu (GuiNewChat, accessed via avp.l)
    @Override public String cursorItemFieldName() { return "f"; } // zy f on wn (InventoryPlayer)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "axv"; } // GuiScreen
    @Override public String sessionFieldName() { return "ae"; } // avn ae on avf (Session)
    @Override public String sessionUsernameFieldName() { return "a"; } // String a on avn (username)
    @Override public String clickCooldownFieldName() { return "ap"; } // int ap on avf (leftClickCounter)
    @Override public String playerControllerFieldName() { return "c"; } // bdb c on avf (PlayerControllerMP)
    @Override public String digMethodName() { return "b"; } // bdb.b(ck, cr) clickBlock
    @Override public String chatLineTextFieldName() { return "b"; } // ev b on avb (IChatComponent, not String)
    @Override public String guiScreenWidthFieldName() { return "l"; } // int l on axv (GuiScreen)
    @Override public String guiScreenHeightFieldName() { return "m"; } // int m on axv (GuiScreen)
    @Override public String keyBindingPressedFieldName() { return "h"; } // boolean h on avc (KeyBinding)
    @Override public String forwardKeyFieldName() { return "W"; } // avc W on avi (key.forward)
    @Override public String leftKeyFieldName() { return "X"; } // avc X on avi (key.left)
    @Override public String backKeyFieldName() { return "Y"; } // avc Y on avi (key.back)
    @Override public String rightKeyFieldName() { return "Z"; } // avc Z on avi (key.right)
    @Override public String jumpKeyFieldName() { return "aa"; } // avc aa on avi (key.jump)
    @Override public String sneakKeyFieldName() { return "ab"; } // avc ab on avi (key.sneak)
    @Override public String creativeInventoryClassName() { return "ayv"; } // GuiContainerCreative
    @Override public String guiInventoryClassName() { return "azd"; } // GuiInventory
    @Override public String closeContainerMethodName() { return "n"; } // bex.n() sends close + resets
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "ax"; } // avf.ax() right-click/use (no-arg)
    @Override public String chatTextMethodName() { return "c"; } // ev.c() IChatComponent -> formatted String
    @Override public boolean isNettyClient() { return true; }
}
