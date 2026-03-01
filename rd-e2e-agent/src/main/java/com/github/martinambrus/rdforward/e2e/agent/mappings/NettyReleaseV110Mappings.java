package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.9.4 (protocol v110).
 * Verified by CFR decompilation of the 1.9.4 client JAR.
 *
 * Minecraft = bcd, Entity = rr, EntityLiving = sa, EntityPlayer = zj,
 * AbstractClientPlayer = bmo, EntityPlayerSP = bmr,
 * GameSettings = bcf, InventoryPlayer = zi, ItemStack = adq, Item = ado,
 * GuiScreen = bez, GuiIngame = bcs, GuiNewChat = bcy, ChatLine = bbz,
 * PlayerControllerMP = bkr, KeyBinding = bca, Session = bck,
 * WorldClient = bks (extends World aht), MovementInput = bmp,
 * GuiContainerCreative = bga, GuiInventory = bgi,
 * GuiContainer = bgc (EffectRenderingInventoryScreen base).
 *
 * Differences from V108 (1.9.1):
 * - Minecraft class: bcc -> bcd
 * - EntityPlayerSP: bmq -> bmr
 * - AbstractClientPlayer: bmo (unchanged name but shifted)
 * - WorldClient: bkr -> bks
 * - PlayerControllerMP: bkq -> bkr
 * - GameSettings: bce -> bcf
 * - GuiScreen: bey -> bez
 * - GuiIngame: bcr -> bcs (field r unchanged)
 * - GuiNewChat: bcx -> bcy
 * - Session: bcj -> bck
 * - KeyBinding: bbz -> bca
 * - ChatLine: bby -> bbz
 * - MovementInput: bmo -> bmp
 * - inventoryFieldName on EntityPlayer (zj): br -> bs
 *   (new DataTracker parameter kf<Byte> bq and br inserted before inventory)
 * - GuiContainerCreative: bfz -> bga
 * - GuiInventory: bgh -> bgi
 *
 * All field names on Entity (rr), InventoryPlayer (zi), ItemStack (adq),
 * and most Minecraft fields remain identical to V108.
 */
public class NettyReleaseV110Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "bcd"; }
    @Override public String runMethodName() { return "a"; } // Main calls new bcd(...).a()
    @Override public String tickMethodName() { return "t"; }
    @Override public String playerFieldName() { return "h"; } // bmr (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // bks (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; }
    @Override public String displayHeightFieldName() { return "e"; }
    @Override public String posXFieldName() { return "p"; } // double on Entity (rr)
    @Override public String posYFieldName() { return "q"; }
    @Override public String posZFieldName() { return "r"; }
    @Override public String gameSettingsFieldName() { return "u"; } // bcf (GameSettings)
    @Override public String movementInputFieldName() { return "e"; } // bmp on bmr
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "v"; } // float on Entity (rr)
    @Override public String pitchFieldName() { return "w"; }
    @Override public String onGroundFieldName() { return "z"; } // boolean on Entity (rr)
    @Override public String inventoryFieldName() { return "bs"; } // zi on zj (EntityPlayer); was br in V108
    @Override public String mainInventoryFieldName() { return "a"; } // adq[] on zi
    @Override public String currentItemFieldName() { return "d"; } // int on zi
    @Override public String itemIdFieldName() { return "d"; } // ado (Item) on adq; NOT int
    @Override public String stackSizeFieldName() { return "b"; } // int on adq
    @Override public String getBlockIdMethodName() { return "o"; } // aht.o(BlockPos) -> IBlockState
    @Override public String clickMethodName() { return "b"; } // bcd.b(boolean) left-click; takes boolean, not int
    @Override public String mouseGrabbedFieldName() { return "x"; } // boolean inGameHasFocus
    @Override public String sendChatMessageMethodName() { return "g"; } // bmr.g(String)
    @Override public String dropPlayerItemMethodName() { return "a"; } // zj.a(boolean)
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // bcd.a(bez)
    @Override public String currentScreenFieldName() { return "m"; } // bez on bcd
    @Override public String ingameGuiFieldName() { return "r"; } // bcs (GuiIngame) on bcd
    @Override public String chatLinesFieldName() { return "j"; } // List on bcy (GuiNewChat); 3-level via bcs.d()
    @Override public String cursorItemFieldName() { return "h"; } // adq on zi
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "bez"; }
    @Override public String sessionFieldName() { return "ag"; } // bck (Session)
    @Override public String sessionUsernameFieldName() { return "a"; } // String on bck
    @Override public String clickCooldownFieldName() { return "ai"; } // int leftClickCounter
    @Override public String playerControllerFieldName() { return "c"; } // bkr (PlayerControllerMP)
    @Override public String digMethodName() { return "b"; } // bkr.b(cl, cs) = dig(BlockPos, EnumFacing)
    @Override public String chatLineTextFieldName() { return "b"; } // ew (IChatComponent) on bbz; NOT String
    @Override public String guiScreenWidthFieldName() { return "l"; }
    @Override public String guiScreenHeightFieldName() { return "m"; }
    @Override public String keyBindingPressedFieldName() { return "h"; } // boolean on bca
    @Override public String forwardKeyFieldName() { return "Q"; }
    @Override public String leftKeyFieldName() { return "R"; }
    @Override public String backKeyFieldName() { return "S"; }
    @Override public String rightKeyFieldName() { return "T"; }
    @Override public String jumpKeyFieldName() { return "U"; }
    @Override public String sneakKeyFieldName() { return "V"; }
    @Override public String creativeInventoryClassName() { return "bga"; }
    @Override public String guiInventoryClassName() { return "bgi"; }
    @Override public String closeContainerMethodName() { return "q"; } // bmr.q() sends close packet
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "ax"; } // right-click/use method (no-arg)
    @Override public boolean isNettyClient() { return true; }
}
