package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.12.1 (protocol v338).
 * Verified by CFR decompilation of the 1.12.1 client JAR.
 *
 * Minecraft = bib, Entity = vg, EntityLivingBase = vp, EntityPlayer = aed,
 * AbstractClientPlayer = bua, EntityPlayerSP = bud,
 * GameSettings = bid, InventoryPlayer = aec, ItemStack = aip, Item = ain,
 * Block = aow, Blocks = aox, World = amu, WorldClient = bsb,
 * GuiScreen = blk, GuiIngame = biq, GuiNewChat = bjb, ChatLine = bhx,
 * PlayerControllerMP = bsa, KeyBinding = bhy, Session = bii,
 * MovementInput = bub, MouseHelper = bic,
 * GuiContainerCreative = bmp, GuiInventory = bmx.
 *
 * Key field shifts from V210 (1.10):
 * - Minecraft class: bcx -> bib
 * - Session field: ae -> af
 * - ClickCooldown field: ag -> ai
 * - Inventory field on EntityPlayer: bt -> bv
 * - Key bindings shifted +1: forward S->T, left T->U, back U->V,
 *   right V->W, jump W->X, sneak X->Y
 * - KeyBinding pressed: h -> i
 * - ItemStack.item: d -> e, ItemStack.count: b -> c
 * - cursorItem on InventoryPlayer: h -> g
 * - GuiIngame on Minecraft: r -> q
 * - chatLines on GuiNewChat: j -> h (field h = allChatLines, field i = drawnChatLines)
 * - closeContainer on EntityPlayerSP: q -> p (sends CPacketCloseWindow + resets)
 * - GameSettings on Minecraft: u -> t
 *
 * clickMethodName "b" takes (boolean), not (int); agent needs adaptation.
 */
public class NettyReleaseV338Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "bib"; }
    @Override public String runMethodName() { return "a"; } // Main calls new bib(...).a()
    @Override public String tickMethodName() { return "t"; }
    @Override public String playerFieldName() { return "h"; } // bud (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // bsb (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; }
    @Override public String displayHeightFieldName() { return "e"; }
    @Override public String posXFieldName() { return "p"; } // double on Entity (vg)
    @Override public String posYFieldName() { return "q"; }
    @Override public String posZFieldName() { return "r"; }
    @Override public String gameSettingsFieldName() { return "t"; } // bid (GameSettings)
    @Override public String movementInputFieldName() { return "e"; } // bub on bud
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "v"; } // float on Entity (vg)
    @Override public String pitchFieldName() { return "w"; }
    @Override public String onGroundFieldName() { return "z"; } // boolean on Entity (vg)
    @Override public String inventoryFieldName() { return "bv"; } // aec on aed (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // fi<aip> on aec
    @Override public String currentItemFieldName() { return "d"; } // int on aec
    @Override public String itemIdFieldName() { return "e"; } // ain (Item) on aip; NOT int
    @Override public String stackSizeFieldName() { return "c"; } // int on aip
    @Override public String getBlockIdMethodName() { return "o"; } // amu.o(et) -> awt (IBlockState)
    @Override public String clickMethodName() { return "b"; } // bib.b(boolean) left-click; takes boolean, not int
    @Override public String mouseGrabbedFieldName() { return "x"; } // boolean inGameHasFocus
    @Override public String sendChatMessageMethodName() { return "g"; } // bud.g(String)
    @Override public String dropPlayerItemMethodName() { return "a"; } // aed.a(boolean)
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // bib.a(blk)
    @Override public String currentScreenFieldName() { return "m"; } // blk on bib
    @Override public String ingameGuiFieldName() { return "q"; } // biq (GuiIngame) on bib
    @Override public String chatLinesFieldName() { return "h"; } // List<bhx> on bjb (GuiNewChat); 3-level via biq.d()
    @Override public String cursorItemFieldName() { return "g"; } // aip on aec
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "blk"; }
    @Override public String sessionFieldName() { return "af"; } // bii (Session)
    @Override public String sessionUsernameFieldName() { return "a"; } // String on bii
    @Override public String clickCooldownFieldName() { return "ai"; } // int leftClickCounter
    @Override public String playerControllerFieldName() { return "c"; } // bsa (PlayerControllerMP)
    @Override public String digMethodName() { return "b"; } // bsa.b(et, fa) onPlayerDamageBlock
    @Override public String chatLineTextFieldName() { return "b"; } // hh (ITextComponent) on bhx; NOT String
    @Override public String guiScreenWidthFieldName() { return "l"; }
    @Override public String guiScreenHeightFieldName() { return "m"; }
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on bhy
    @Override public String forwardKeyFieldName() { return "T"; }
    @Override public String leftKeyFieldName() { return "U"; }
    @Override public String backKeyFieldName() { return "V"; }
    @Override public String rightKeyFieldName() { return "W"; }
    @Override public String jumpKeyFieldName() { return "X"; }
    @Override public String sneakKeyFieldName() { return "Y"; }
    @Override public String creativeInventoryClassName() { return "bmp"; }
    @Override public String guiInventoryClassName() { return "bmx"; }
    @Override public String closeContainerMethodName() { return "p"; } // bud.p() sends CPacketCloseWindow + calls x()
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "aB"; } // right-click/use method (no-arg)
    @Override public boolean isNettyClient() { return true; }
}
