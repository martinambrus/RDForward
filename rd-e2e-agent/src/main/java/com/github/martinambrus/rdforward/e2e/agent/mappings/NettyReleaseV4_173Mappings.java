package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.7.3 (protocol v4, same as 1.7.2 but different obfuscation).
 * Verified by CFR decompilation of the 1.7.3 client JAR.
 *
 * Class hierarchy:
 *   Minecraft = azi, Entity = qr, EntityLivingBase = rm, EntityPlayer = xq,
 *   EntityClientPlayerMP = bma, EntityPlayerSP = bkf (extends bmd extends bma extends xq),
 *   World = afs, WorldClient = bka, GameSettings = bac, KeyBinding = azf,
 *   InventoryPlayer = xo, ItemStack = abu, Item = abs, Block = ahz,
 *   GuiScreen = bcl, GuiIngame = bao, GuiNewChat = bav, ChatLine = aze,
 *   PlayerControllerMP = bjz, Session = bal, MovementInput = bmb,
 *   GuiInventory = bek, GuiContainerCreative = beb.
 *
 * Notes:
 * - clickMethodName: 1.7.3 has no single click(int) dispatcher. Left=ai(), right=aj()
 *   on azi (both private no-arg). Agent code that calls clickMethod(int) needs adaptation.
 * - itemIdFieldName: 1.7.3 stores Item object (abs) as private field "e" on abu (ItemStack).
 * - chatLinesFieldName: chatLines list "h" is on GuiNewChat (bav), not directly on GuiIngame (bao).
 *   bao.l is the bav (GuiNewChat sub-object); bav.h is the List of ChatLine (aze).
 * - chatLineTextFieldName: ChatLine.b is IChatComponent (fa), not String.
 * - getBlockIdMethodName: World.a(int,int,int) returns Block (ahz), not int block ID.
 * - dropOneItemMethodName: null because drop uses a(boolean) with boolean param, not no-arg.
 * - Session field S is private final on azi; agent uses reflection with setAccessible.
 * - sessionUsernameFieldName: bal.a is the username (returned by bal.c()).
 */
public class NettyReleaseV4_173Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "azi"; }
    @Override public String runMethodName() { return "f"; } // Main calls azi.f()
    @Override public String tickMethodName() { return "p"; } // azi.p() is the tick method
    @Override public String playerFieldName() { return "h"; } // bkf h (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // bka f (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; } // int d
    @Override public String displayHeightFieldName() { return "e"; } // int e
    @Override public String posXFieldName() { return "s"; } // double s on qr (Entity)
    @Override public String posYFieldName() { return "t"; } // double t on qr (Entity)
    @Override public String posZFieldName() { return "u"; } // double u on qr (Entity)
    @Override public String gameSettingsFieldName() { return "u"; } // bac u on azi (GameSettings)
    @Override public String movementInputFieldName() { return "c"; } // bmb c on bmd (MovementInput)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "y"; } // float y on qr (Entity)
    @Override public String pitchFieldName() { return "z"; } // float z on qr (Entity)
    @Override public String onGroundFieldName() { return "D"; } // boolean D on qr (Entity)
    @Override public String inventoryFieldName() { return "bm"; } // xo bm on xq (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // abu[] a on xo (InventoryPlayer)
    @Override public String currentItemFieldName() { return "c"; } // int c on xo (InventoryPlayer)
    @Override public String itemIdFieldName() { return "e"; } // abs e on abu (Item object, not int)
    @Override public String stackSizeFieldName() { return "b"; } // int b on abu (ItemStack)
    @Override public String getBlockIdMethodName() { return "a"; } // afs.a(int,int,int) returns ahz (Block)
    @Override public String clickMethodName() { return "ai"; } // azi.ai() left-click attack (no int param)
    @Override public String mouseGrabbedFieldName() { return "x"; } // boolean x (inGameHasFocus)
    @Override public String sendChatMessageMethodName() { return "a"; } // bkf.a(String) sends chat
    @Override public String dropPlayerItemMethodName() { return "a"; } // xq.a(boolean) drops item
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // azi.a(bcl) opens screen
    @Override public String currentScreenFieldName() { return "n"; } // bcl n on azi (GuiScreen)
    @Override public String ingameGuiFieldName() { return "r"; } // bao r on azi (GuiIngame)
    @Override public String chatLinesFieldName() { return "h"; } // List h on bav (GuiNewChat, via bao.l)
    @Override public String cursorItemFieldName() { return "g"; } // abu g on xo (InventoryPlayer)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "bcl"; } // GuiScreen
    @Override public String sessionFieldName() { return "S"; } // bal S on azi (Session, private final)
    @Override public String sessionUsernameFieldName() { return "a"; } // String a on bal (username)
    @Override public String clickCooldownFieldName() { return "ad"; } // int ad on azi (leftClickCounter)
    @Override public String playerControllerFieldName() { return "c"; } // bjz c on azi (PlayerControllerMP)
    @Override public String digMethodName() { return "b"; } // bjz.b(int,int,int,int) clickBlock
    @Override public String chatLineTextFieldName() { return "b"; } // fa b on aze (IChatComponent, not String)
    @Override public String guiScreenWidthFieldName() { return "l"; } // int l on bcl (GuiScreen)
    @Override public String guiScreenHeightFieldName() { return "m"; } // int m on bcl (GuiScreen)
    @Override public String keyBindingPressedFieldName() { return "h"; } // boolean h on azf (KeyBinding)
    @Override public String forwardKeyFieldName() { return "V"; } // azf V on bac (key.forward)
    @Override public String leftKeyFieldName() { return "W"; } // azf W on bac (key.left)
    @Override public String backKeyFieldName() { return "X"; } // azf X on bac (key.back)
    @Override public String rightKeyFieldName() { return "Y"; } // azf Y on bac (key.right)
    @Override public String jumpKeyFieldName() { return "Z"; } // azf Z on bac (key.jump)
    @Override public String sneakKeyFieldName() { return "aa"; } // azf aa on bac (key.sneak)
    @Override public String creativeInventoryClassName() { return "beb"; } // GuiContainerCreative
    @Override public String guiInventoryClassName() { return "bek"; } // GuiInventory
    @Override public String closeContainerMethodName() { return "k"; } // bkf.k() sends close packet
    @Override public String rightClickMethodName() { return "aj"; } // azi.aj() right-click/use (no-arg)
    @Override public String chatTextMethodName() { return "c"; } // fa.c() IChatComponent -> formatted String
    @Override public boolean isNettyClient() { return true; }
}
