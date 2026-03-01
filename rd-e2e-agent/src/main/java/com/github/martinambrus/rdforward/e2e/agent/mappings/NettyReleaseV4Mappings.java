package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.7.2 (protocol v4, first Netty version).
 * Verified by CFR decompilation of the 1.7.2 client JAR.
 *
 * Class hierarchy:
 *   Minecraft = azd, Entity = qn, EntityLivingBase = rh, EntityPlayer = xl,
 *   EntityClientPlayerMP = blc, EntityPlayerSP = bje,
 *   World = afn, WorldClient = biz, GameSettings = azw, KeyBinding = aza,
 *   InventoryPlayer = xj, ItemStack = abp, Item = abn, Block = ahu,
 *   GuiScreen = bcd, GuiIngame = bah, GuiNewChat = bao, ChatLine = ayz,
 *   PlayerControllerMP = biy, Session = baf, MovementInput = bla,
 *   GuiInventory = bea, GuiContainerCreative = bdr.
 *
 * Notes:
 * - clickMethodName: 1.7.2 has no single click(int) dispatcher. Left=af(), right=ag()
 *   on azd (both private no-arg). Agent code that calls clickMethod(int) needs adaptation.
 * - itemIdFieldName: 1.7.2 stores Item object (abn) as private field "e" on abp (ItemStack).
 * - chatLinesFieldName: chatLines list "h" is on GuiNewChat (bao), not directly on GuiIngame (bah).
 *   bah.l is the bao (GuiNewChat sub-object); bao.h is the List of ChatLine (ayz).
 * - chatLineTextFieldName: ChatLine.b is IChatComponent (fa), not String.
 * - getBlockIdMethodName: World.a(int,int,int) returns Block (ahu), not int block ID.
 * - dropOneItemMethodName: null because drop uses a(boolean) with boolean param, not no-arg.
 */
public class NettyReleaseV4Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "azd"; }
    @Override public String runMethodName() { return "e"; } // Main calls azd.e()
    @Override public String tickMethodName() { return "o"; } // azd.o() is the tick method
    @Override public String playerFieldName() { return "h"; } // bje h (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // biz f (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; } // int d
    @Override public String displayHeightFieldName() { return "e"; } // int e
    @Override public String posXFieldName() { return "t"; } // double t on qn (Entity)
    @Override public String posYFieldName() { return "u"; } // double u on qn (Entity)
    @Override public String posZFieldName() { return "v"; } // double v on qn (Entity)
    @Override public String gameSettingsFieldName() { return "u"; } // azw u on azd (GameSettings)
    @Override public String movementInputFieldName() { return "c"; } // bla c on blc (MovementInput)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "z"; } // float z on qn (Entity)
    @Override public String pitchFieldName() { return "A"; } // float A on qn (Entity)
    @Override public String onGroundFieldName() { return "E"; } // boolean E on qn (Entity)
    @Override public String inventoryFieldName() { return "bn"; } // xj bn on xl (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // abp[] a on xj (InventoryPlayer)
    @Override public String currentItemFieldName() { return "c"; } // int c on xj (InventoryPlayer)
    @Override public String itemIdFieldName() { return "e"; } // abn e on abp (Item object, not int)
    @Override public String stackSizeFieldName() { return "b"; } // int b on abp (ItemStack)
    @Override public String getBlockIdMethodName() { return "a"; } // afn.a(int,int,int) returns ahu (Block)
    @Override public String clickMethodName() { return "af"; } // azd.af() left-click attack (no int param)
    @Override public String mouseGrabbedFieldName() { return "y"; } // boolean y (inGameHasFocus)
    @Override public String sendChatMessageMethodName() { return "a"; } // bje.a(String) sends chat
    @Override public String dropPlayerItemMethodName() { return "a"; } // xl.a(boolean) drops item
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // azd.a(bcd) opens screen
    @Override public String currentScreenFieldName() { return "n"; } // bcd n on azd (GuiScreen)
    @Override public String ingameGuiFieldName() { return "r"; } // bah r on azd (GuiIngame)
    @Override public String chatLinesFieldName() { return "h"; } // List h on bao (GuiNewChat, via bah.l)
    @Override public String cursorItemFieldName() { return "g"; } // abp g on xj (InventoryPlayer)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "bcd"; } // GuiScreen
    @Override public String sessionFieldName() { return "S"; } // baf S on azd (Session)
    @Override public String sessionUsernameFieldName() { return "a"; } // String a on baf (username)
    @Override public String clickCooldownFieldName() { return "ad"; } // int ad on azd (leftClickCounter)
    @Override public String playerControllerFieldName() { return "c"; } // biy c on azd (PlayerControllerMP)
    @Override public String digMethodName() { return "b"; } // biy.b(int,int,int,int) clickBlock
    @Override public String chatLineTextFieldName() { return "b"; } // fa b on ayz (IChatComponent, not String)
    @Override public String guiScreenWidthFieldName() { return "l"; } // int l on bcd (GuiScreen)
    @Override public String guiScreenHeightFieldName() { return "m"; } // int m on bcd (GuiScreen)
    @Override public String keyBindingPressedFieldName() { return "h"; } // boolean h on aza (KeyBinding)
    @Override public String forwardKeyFieldName() { return "K"; } // aza K on azw (key.forward)
    @Override public String leftKeyFieldName() { return "L"; } // aza L on azw (key.left)
    @Override public String backKeyFieldName() { return "M"; } // aza M on azw (key.back)
    @Override public String rightKeyFieldName() { return "N"; } // aza N on azw (key.right)
    @Override public String jumpKeyFieldName() { return "O"; } // aza O on azw (key.jump)
    @Override public String sneakKeyFieldName() { return "P"; } // aza P on azw (key.sneak)
    @Override public String creativeInventoryClassName() { return "bdr"; } // GuiContainerCreative
    @Override public String guiInventoryClassName() { return "bea"; } // GuiInventory
    @Override public String closeContainerMethodName() { return "k"; } // bje.k() sends close packet
    @Override public String rightClickMethodName() { return "ag"; } // azd.ag() right-click/use (no-arg)
    @Override public String chatTextMethodName() { return "c"; } // fa.c() IChatComponent -> formatted String
    @Override public boolean isNettyClient() { return true; }
}
