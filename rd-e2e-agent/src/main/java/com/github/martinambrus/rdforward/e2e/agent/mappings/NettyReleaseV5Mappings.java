package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.7.6-1.7.10 (protocol v5).
 * Verified by CFR decompilation of the 1.7.6 client JAR.
 *
 * Class hierarchy:
 *   Minecraft = ban, Entity = rz, EntityLivingBase = su, EntityPlayer = yy,
 *   AbstractClientPlayer = bnq, EntityPlayerSP(outer) = bnu, EntityPlayerSP = blu,
 *   World = aha, WorldClient = blp, GameSettings = bbh, KeyBinding = bak,
 *   InventoryPlayer = yw, ItemStack = adc, Item = ada, Block = ajh,
 *   GuiScreen = bdq, GuiIngame = bbt, GuiNewChat = bca, ChatLine = baj,
 *   PlayerControllerMP = blo, Session = bbq, MovementInput = bns,
 *   GuiInventory = bfp, GuiContainerCreative = bfg, IChatComponent = fj.
 *
 * Notes:
 * - minecraftClassName: ban is in default (unnamed) package, not net.minecraft.client.
 * - clickMethodName: 1.7.6 has no single click(int) dispatcher. Left=al(), right=am()
 *   on ban (both private no-arg). Agent code that calls clickMethod(int) needs adaptation.
 *   "al" is provided as the left-click attack method.
 * - itemIdFieldName: 1.7.6 stores Item object (ada) as private field "e" on adc (ItemStack).
 * - chatLinesFieldName: chatLines list "h" is on GuiNewChat (bca), not directly on GuiIngame (bbt).
 *   ingameGuiFieldName "r" returns bbt; bbt.l is the bca; bca.h is the List of ChatLine (baj).
 * - chatLineTextFieldName: ChatLine.b is IChatComponent (fj), not String. Use fj.c() for text.
 * - getBlockIdMethodName: World.a(int,int,int) returns Block (ajh), not int block ID.
 * - dropOneItemMethodName: null because drop uses a(boolean) with boolean param, not no-arg.
 * - digMethodName: blo.c(int,int,int,int) matches the agent's expected (int,int,int,int) signature.
 */
public class NettyReleaseV5Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "ban"; }
    @Override public String runMethodName() { return "f"; } // ban.f() is the main run loop
    @Override public String tickMethodName() { return "p"; } // ban.p() is the per-tick method
    @Override public String playerFieldName() { return "h"; } // blu h (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // blp f (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; } // int d
    @Override public String displayHeightFieldName() { return "e"; } // int e
    @Override public String posXFieldName() { return "s"; } // double s on rz (Entity)
    @Override public String posYFieldName() { return "t"; } // double t on rz (Entity)
    @Override public String posZFieldName() { return "u"; } // double u on rz (Entity)
    @Override public String gameSettingsFieldName() { return "u"; } // bbh u on ban (GameSettings)
    @Override public String movementInputFieldName() { return "c"; } // bns c on bnu (MovementInput)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "y"; } // float y on rz (Entity)
    @Override public String pitchFieldName() { return "z"; } // float z on rz (Entity)
    @Override public String onGroundFieldName() { return "D"; } // boolean D on rz (Entity)
    @Override public String inventoryFieldName() { return "bm"; } // yw bm on yy (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // adc[] a on yw (InventoryPlayer)
    @Override public String currentItemFieldName() { return "c"; } // int c on yw (InventoryPlayer)
    @Override public String itemIdFieldName() { return "e"; } // ada e on adc (Item object, not int)
    @Override public String stackSizeFieldName() { return "b"; } // int b on adc (ItemStack)
    @Override public String getBlockIdMethodName() { return "a"; } // aha.a(int,int,int) returns ajh (Block)
    @Override public String clickMethodName() { return "al"; } // ban.al() left-click attack (no int param)
    @Override public String mouseGrabbedFieldName() { return "x"; } // boolean x (inGameHasFocus)
    @Override public String sendChatMessageMethodName() { return "a"; } // blu.a(String) sends chat
    @Override public String dropPlayerItemMethodName() { return "a"; } // yy.a(boolean) drops current item
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // ban.a(bdq) opens screen
    @Override public String currentScreenFieldName() { return "n"; } // bdq n on ban (GuiScreen)
    @Override public String ingameGuiFieldName() { return "r"; } // bbt r on ban (GuiIngame)
    @Override public String chatLinesFieldName() { return "h"; } // List h on bca (GuiNewChat, via bbt.l)
    @Override public String cursorItemFieldName() { return "g"; } // adc g on yw (InventoryPlayer carried item)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "bdq"; } // GuiScreen
    @Override public String sessionFieldName() { return "S"; } // bbq S on ban (Session)
    @Override public String sessionUsernameFieldName() { return "a"; } // String a on bbq (username)
    @Override public String clickCooldownFieldName() { return "U"; } // int U on ban (leftClickCounter)
    @Override public String playerControllerFieldName() { return "c"; } // blo c on ban (PlayerControllerMP)
    @Override public String digMethodName() { return "c"; } // blo.c(int,int,int,int) onPlayerDamageBlock
    @Override public String chatLineTextFieldName() { return "b"; } // fj b on baj (IChatComponent, not String)
    @Override public String guiScreenWidthFieldName() { return "l"; } // int l on bdq (GuiScreen)
    @Override public String guiScreenHeightFieldName() { return "m"; } // int m on bdq (GuiScreen)
    @Override public String keyBindingPressedFieldName() { return "h"; } // boolean h on bak (KeyBinding)
    @Override public String forwardKeyFieldName() { return "U"; } // bak U on bbh (key.forward)
    @Override public String leftKeyFieldName() { return "V"; } // bak V on bbh (key.left)
    @Override public String backKeyFieldName() { return "W"; } // bak W on bbh (key.back)
    @Override public String rightKeyFieldName() { return "X"; } // bak X on bbh (key.right)
    @Override public String jumpKeyFieldName() { return "Y"; } // bak Y on bbh (key.jump)
    @Override public String sneakKeyFieldName() { return "Z"; } // bak Z on bbh (key.sneak)
    @Override public String creativeInventoryClassName() { return "bfg"; } // GuiContainerCreative
    @Override public String guiInventoryClassName() { return "bfp"; } // GuiInventory
    @Override public String closeContainerMethodName() { return "j"; } // blu.j() sends close packet
    @Override public String rightClickMethodName() { return "am"; } // ban.am() right-click/use (no-arg)
    @Override public String chatTextMethodName() { return "c"; } // fj.c() IChatComponent -> formatted String
    @Override public boolean isNettyClient() { return true; }
}
