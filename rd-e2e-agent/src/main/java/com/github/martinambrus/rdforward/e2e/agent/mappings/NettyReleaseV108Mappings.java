package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.9.1 (protocol v108).
 * Verified by CFR decompilation of the 1.9.1 client JAR.
 *
 * Minecraft = bcc, Entity = rr, EntityPlayer = zj, EntityPlayerSP = bmq,
 * GameSettings = bce, InventoryPlayer = zi, ItemStack = adq, Item = ado,
 * GuiScreen = bey, GuiIngame = bcr, GuiNewChat = bcx, ChatLine = bby,
 * PlayerControllerMP = bkq, KeyBinding = bbz, Session = bcj,
 * WorldClient = bkr (extends World aht), MovementInput = bmo,
 * GuiContainerCreative = bfz, GuiInventory = bgh.
 *
 * Very similar to 1.9 (V107) with minor class renaming.
 * clickMethodName "b" takes (boolean), not (int); agent needs adaptation.
 */
public class NettyReleaseV108Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "bcc"; }
    @Override public String runMethodName() { return "a"; } // Main calls new bcc(...).a()
    @Override public String tickMethodName() { return "t"; }
    @Override public String playerFieldName() { return "h"; } // bmq (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // bkr (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; }
    @Override public String displayHeightFieldName() { return "e"; }
    @Override public String posXFieldName() { return "p"; } // double on Entity (rr)
    @Override public String posYFieldName() { return "q"; }
    @Override public String posZFieldName() { return "r"; }
    @Override public String gameSettingsFieldName() { return "u"; } // bce (GameSettings)
    @Override public String movementInputFieldName() { return "e"; } // bmo on bmq
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "v"; } // float on Entity (rr)
    @Override public String pitchFieldName() { return "w"; }
    @Override public String onGroundFieldName() { return "z"; } // boolean on Entity (rr)
    @Override public String inventoryFieldName() { return "br"; } // zi on zj (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // adq[] on zi
    @Override public String currentItemFieldName() { return "d"; } // int on zi
    @Override public String itemIdFieldName() { return "d"; } // ado (Item) on adq; NOT int
    @Override public String stackSizeFieldName() { return "b"; } // int on adq
    @Override public String getBlockIdMethodName() { return "o"; } // aht.o(BlockPos) -> IBlockState
    @Override public String clickMethodName() { return "b"; } // bcc.b(boolean) left-click; takes boolean, not int
    @Override public String mouseGrabbedFieldName() { return "x"; } // boolean inGameHasFocus
    @Override public String sendChatMessageMethodName() { return "g"; } // bmq.g(String)
    @Override public String dropPlayerItemMethodName() { return "a"; } // zj.a(boolean)
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // bcc.a(bey)
    @Override public String currentScreenFieldName() { return "m"; } // bey on bcc
    @Override public String ingameGuiFieldName() { return "r"; } // bcr (GuiIngame) on bcc
    @Override public String chatLinesFieldName() { return "j"; } // List on bcx (GuiNewChat); 3-level via bcr.d()
    @Override public String cursorItemFieldName() { return "h"; } // adq on zi
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "bey"; }
    @Override public String sessionFieldName() { return "ag"; } // bcj (Session)
    @Override public String sessionUsernameFieldName() { return "a"; } // String on bcj
    @Override public String clickCooldownFieldName() { return "ai"; } // int leftClickCounter
    @Override public String playerControllerFieldName() { return "c"; } // bkq (PlayerControllerMP)
    @Override public String digMethodName() { return "b"; } // bkq.b(BlockPos, EnumFacing)
    @Override public String chatLineTextFieldName() { return "b"; } // eu (IChatComponent) on bby; NOT String
    @Override public String guiScreenWidthFieldName() { return "l"; }
    @Override public String guiScreenHeightFieldName() { return "m"; }
    @Override public String keyBindingPressedFieldName() { return "h"; } // boolean on bbz
    @Override public String forwardKeyFieldName() { return "Q"; }
    @Override public String leftKeyFieldName() { return "R"; }
    @Override public String backKeyFieldName() { return "S"; }
    @Override public String rightKeyFieldName() { return "T"; }
    @Override public String jumpKeyFieldName() { return "U"; }
    @Override public String sneakKeyFieldName() { return "V"; }
    @Override public String creativeInventoryClassName() { return "bfz"; }
    @Override public String guiInventoryClassName() { return "bgh"; }
    @Override public String closeContainerMethodName() { return "q"; } // bmq.q() sends close packet
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "ax"; } // right-click/use method (no-arg)
    @Override public boolean isNettyClient() { return true; }
}
