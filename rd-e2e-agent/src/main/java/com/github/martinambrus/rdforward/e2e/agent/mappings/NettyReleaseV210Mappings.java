package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.10 (protocol v210).
 * Verified by CFR decompilation of the 1.10 client JAR.
 *
 * Minecraft = bcx, Entity = rw, EntityLivingBase = sf, EntityPlayer = zs,
 * AbstractClientPlayer = bnk, EntityPlayerSP = bnn,
 * GameSettings = bcz, InventoryPlayer = zr, ItemStack = adz, Item = adx,
 * Block = akf, World = aid, WorldClient = bln,
 * GuiScreen = bft, GuiIngame = bdm, GuiNewChat = bds, ChatLine = bct,
 * PlayerControllerMP = blm, KeyBinding = bcu, Session = bde,
 * MovementInput = bnl, MouseHelper = bcy,
 * GuiContainerCreative = bgu, GuiInventory = bhc.
 *
 * Significant class renaming from V108 (1.9.1). Key field shifts:
 * - Session field: ag -> ae
 * - ClickCooldown field: ai -> ag
 * - Inventory field on EntityPlayer: br -> bt
 * - Key bindings shifted +2: forward Q->S, left R->T, back S->U,
 *   right T->V, jump U->W, sneak V->X
 * - GuiScreen class: bey -> bft
 * - GuiContainerCreative: bfz -> bgu, GuiInventory: bgh -> bhc
 *
 * clickMethodName "b" takes (boolean), not (int); agent needs adaptation.
 */
public class NettyReleaseV210Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "bcx"; }
    @Override public String runMethodName() { return "a"; } // Main calls new bcx(...).a()
    @Override public String tickMethodName() { return "t"; }
    @Override public String playerFieldName() { return "h"; } // bnn (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // bln (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; }
    @Override public String displayHeightFieldName() { return "e"; }
    @Override public String posXFieldName() { return "p"; } // double on Entity (rw)
    @Override public String posYFieldName() { return "q"; }
    @Override public String posZFieldName() { return "r"; }
    @Override public String gameSettingsFieldName() { return "u"; } // bcz (GameSettings)
    @Override public String movementInputFieldName() { return "e"; } // bnl on bnn
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "v"; } // float on Entity (rw)
    @Override public String pitchFieldName() { return "w"; }
    @Override public String onGroundFieldName() { return "z"; } // boolean on Entity (rw)
    @Override public String inventoryFieldName() { return "bt"; } // zr on zs (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // adz[] on zr
    @Override public String currentItemFieldName() { return "d"; } // int on zr
    @Override public String itemIdFieldName() { return "d"; } // adx (Item) on adz; NOT int
    @Override public String stackSizeFieldName() { return "b"; } // int on adz
    @Override public String getBlockIdMethodName() { return "o"; } // aid.o(BlockPos) -> IBlockState
    @Override public String clickMethodName() { return "b"; } // bcx.b(boolean) left-click; takes boolean, not int
    @Override public String mouseGrabbedFieldName() { return "x"; } // boolean inGameHasFocus
    @Override public String sendChatMessageMethodName() { return "g"; } // bnn.g(String)
    @Override public String dropPlayerItemMethodName() { return "a"; } // zs.a(boolean)
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // bcx.a(bft)
    @Override public String currentScreenFieldName() { return "m"; } // bft on bcx
    @Override public String ingameGuiFieldName() { return "r"; } // bdm (GuiIngame) on bcx
    @Override public String chatLinesFieldName() { return "j"; } // List on bds (GuiNewChat); 3-level via bdm.d()
    @Override public String cursorItemFieldName() { return "h"; } // adz on zr
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "bft"; }
    @Override public String sessionFieldName() { return "ae"; } // bde (Session)
    @Override public String sessionUsernameFieldName() { return "a"; } // String on bde
    @Override public String clickCooldownFieldName() { return "ag"; } // int leftClickCounter
    @Override public String playerControllerFieldName() { return "c"; } // blm (PlayerControllerMP)
    @Override public String digMethodName() { return "b"; } // blm.b(BlockPos, EnumFacing)
    @Override public String chatLineTextFieldName() { return "b"; } // ey (IChatComponent) on bct; NOT String
    @Override public String guiScreenWidthFieldName() { return "l"; }
    @Override public String guiScreenHeightFieldName() { return "m"; }
    @Override public String keyBindingPressedFieldName() { return "h"; } // boolean on bcu
    @Override public String forwardKeyFieldName() { return "S"; }
    @Override public String leftKeyFieldName() { return "T"; }
    @Override public String backKeyFieldName() { return "U"; }
    @Override public String rightKeyFieldName() { return "V"; }
    @Override public String jumpKeyFieldName() { return "W"; }
    @Override public String sneakKeyFieldName() { return "X"; }
    @Override public String creativeInventoryClassName() { return "bgu"; }
    @Override public String guiInventoryClassName() { return "bhc"; }
    @Override public String closeContainerMethodName() { return "q"; } // bnn.q() sends close packet
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "ax"; } // right-click/use method (no-arg)
    @Override public boolean isNettyClient() { return true; }
}
