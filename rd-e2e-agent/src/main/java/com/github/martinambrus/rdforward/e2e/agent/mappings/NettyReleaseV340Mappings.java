package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.12.2 (protocol v340).
 * Verified by CFR decompilation of the 1.12.2 client JAR.
 *
 * Minecraft = bib, Entity = vg, EntityLivingBase = vp, EntityPlayer = aed,
 * AbstractClientPlayer = bua, EntityPlayerSP = bud,
 * GameSettings = bid, InventoryPlayer = aec, ItemStack = aip, Item = ain,
 * Block = aow, World = amu, WorldClient = bsb,
 * GuiScreen = blk, GuiIngame = biq, GuiNewChat = bjb, ChatLine = bhx,
 * PlayerControllerMP = bsa, KeyBinding = bhy, Session = bii,
 * MovementInput = bub, PlayerAbilities = aeb,
 * GuiContainerCreative = bmp, GuiInventory = bmx.
 *
 * Key differences from V335 (1.12):
 * - Class names shifted due to new/changed classes in 1.12.2.
 * - itemIdFieldName: Item field "e" on aip (was "d" in 1.9-1.10).
 * - stackSizeFieldName: count field "c" on aip (was "b" in 1.9-1.10).
 * - inventoryFieldName: "bv" on aed (EntityPlayer).
 * - cursorItemFieldName: "g" on aec (InventoryPlayer).
 * - mouseGrabbedFieldName: "x" on bib (grab/ungrab methods o()/p() on bib).
 * - sessionFieldName: "af" on bib.
 * - keyBindingPressedFieldName: "i" on bhy (was "h" in 1.9-1.10).
 * - Key bindings on bid: forward=T, left=U, back=V, right=W, jump=X, sneak=Y.
 */
public class NettyReleaseV340Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "bib"; }
    @Override public String runMethodName() { return "a"; } // Main calls new bib(...).a()
    @Override public String tickMethodName() { return "t"; } // bib.t() decrements leftClickCounter, ticks screens
    @Override public String playerFieldName() { return "h"; } // bud (EntityPlayerSP) on bib
    @Override public String worldFieldName() { return "f"; } // bsb (WorldClient) on bib
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; } // int d on bib
    @Override public String displayHeightFieldName() { return "e"; } // int e on bib
    @Override public String posXFieldName() { return "p"; } // double on vg (Entity)
    @Override public String posYFieldName() { return "q"; }
    @Override public String posZFieldName() { return "r"; }
    @Override public String gameSettingsFieldName() { return "t"; } // bid (GameSettings) on bib
    @Override public String movementInputFieldName() { return "e"; } // bub (MovementInput) on bud
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "v"; } // float on vg (Entity)
    @Override public String pitchFieldName() { return "w"; }
    @Override public String onGroundFieldName() { return "z"; } // boolean on vg (Entity)
    @Override public String inventoryFieldName() { return "bv"; } // aec (InventoryPlayer) on aed (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // fi<aip> (NonNullList) on aec
    @Override public String currentItemFieldName() { return "d"; } // int on aec (InventoryPlayer)
    @Override public String itemIdFieldName() { return "e"; } // ain (Item) on aip (ItemStack); NOT int
    @Override public String stackSizeFieldName() { return "c"; } // int on aip (ItemStack)
    @Override public String getBlockIdMethodName() { return "o"; } // amu.o(et) returns awt (IBlockState)
    @Override public String clickMethodName() { return "b"; } // bib.b(boolean) left-click/attack
    @Override public String mouseGrabbedFieldName() { return "x"; } // boolean inGameHasFocus on bib (set in o()/p() grab methods)
    @Override public String sendChatMessageMethodName() { return "g"; } // bud.g(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "a"; } // bud.a(boolean) sends dig packet
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // bib.a(blk) sets current screen
    @Override public String currentScreenFieldName() { return "m"; } // blk (GuiScreen) on bib
    @Override public String ingameGuiFieldName() { return "q"; } // biq (GuiIngame) on bib
    @Override public String chatLinesFieldName() { return "h"; } // List<bhx> on bjb (GuiNewChat); via biq.d() -> biq.l
    @Override public String cursorItemFieldName() { return "g"; } // aip (ItemStack) on aec (InventoryPlayer)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "blk"; } // GuiScreen
    @Override public String sessionFieldName() { return "af"; } // bii (Session) on bib
    @Override public String sessionUsernameFieldName() { return "a"; } // String on bii (username)
    @Override public String clickCooldownFieldName() { return "ai"; } // int blockHitDelay on bib; checked in b(boolean)
    @Override public String playerControllerFieldName() { return "c"; } // bsa (PlayerControllerMP) on bib
    @Override public String digMethodName() { return "b"; } // bsa.b(et, fa) onPlayerDamageBlock
    @Override public String chatLineTextFieldName() { return "b"; } // hh (IChatComponent) on bhx (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "l"; } // int on blk (GuiScreen)
    @Override public String guiScreenHeightFieldName() { return "m"; } // int on blk (GuiScreen)
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on bhy (KeyBinding)
    @Override public String forwardKeyFieldName() { return "T"; } // bhy on bid (key.forward, keycode 17)
    @Override public String leftKeyFieldName() { return "U"; } // bhy on bid (key.left, keycode 30)
    @Override public String backKeyFieldName() { return "V"; } // bhy on bid (key.back, keycode 31)
    @Override public String rightKeyFieldName() { return "W"; } // bhy on bid (key.right, keycode 32)
    @Override public String jumpKeyFieldName() { return "X"; } // bhy on bid (key.jump, keycode 57)
    @Override public String sneakKeyFieldName() { return "Y"; } // bhy on bid (key.sneak, keycode 42)
    @Override public String creativeInventoryClassName() { return "bmp"; } // GuiContainerCreative
    @Override public String guiInventoryClassName() { return "bmx"; } // GuiInventory
    @Override public String closeContainerMethodName() { return "p"; } // bud.p() sends close packet + calls x()
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "aB"; } // right-click/use method (no-arg)
    @Override public boolean isNettyClient() { return true; }
}
