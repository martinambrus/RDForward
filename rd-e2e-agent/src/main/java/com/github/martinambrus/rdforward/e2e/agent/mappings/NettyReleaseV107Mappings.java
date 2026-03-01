package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.9 (protocol v107).
 * Verified by CFR decompilation of the 1.9 client JAR.
 *
 * Minecraft class: bcf. Entity: rr. EntityPlayer: zj. EntityPlayerSP: bmt.
 * GameSettings: bch. InventoryPlayer: zi. ItemStack: adq. Item: ado.
 * GuiScreen: bfb. GuiIngame: bcu. GuiNewChat: bda. ChatLine: bcb.
 * PlayerControllerMP: bkt. KeyBinding: bcc. Session: bcm.
 * WorldClient: bku (extends World aht). MovementInput: bmr.
 * GuiContainerCreative: bgc. GuiInventory: bgk.
 *
 * Notable 1.9 changes vs pre-Netty:
 * - clickMethod aw() is no-arg (no int button param); agent needs adaptation.
 * - getBlockId uses BlockPos (cj) not raw ints; method o(cj) on World.
 * - itemId field (d) is Item object (ado), not int; needs Item.getIdFromItem().
 * - chatLines are on GuiNewChat (bda), a sub-object of GuiIngame (bcu).
 *   ingameGuiFieldName points to bcu; chatLinesFieldName "j" is on bda (field l on bcu).
 *   Agent needs 3-level access: minecraft.r -> bcu.l -> bda.j.
 * - chatLineText field (b) is IChatComponent (eu), not String.
 * - dropOneItem is a(boolean), not no-arg; null triggers alpha path in agent.
 */
public class NettyReleaseV107Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "bcf"; }
    @Override public String runMethodName() { return "a"; } // public void a() — main run loop
    @Override public String tickMethodName() { return "t"; }
    @Override public String playerFieldName() { return "h"; } // bmt (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // bku (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; }
    @Override public String displayHeightFieldName() { return "e"; }
    @Override public String posXFieldName() { return "p"; } // double on Entity (rr)
    @Override public String posYFieldName() { return "q"; }
    @Override public String posZFieldName() { return "r"; }
    @Override public String gameSettingsFieldName() { return "u"; } // bch (GameSettings)
    @Override public String movementInputFieldName() { return "e"; } // bmr on bmt
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "v"; } // float on Entity (rr)
    @Override public String pitchFieldName() { return "w"; }
    @Override public String onGroundFieldName() { return "z"; } // boolean on Entity (rr)
    @Override public String inventoryFieldName() { return "br"; } // zi (InventoryPlayer) on EntityPlayer (zj)
    @Override public String mainInventoryFieldName() { return "a"; } // adq[36] on zi
    @Override public String currentItemFieldName() { return "d"; } // int on zi
    @Override public String itemIdFieldName() { return "d"; } // ado (Item) on adq — NOT int; needs adaptation
    @Override public String stackSizeFieldName() { return "b"; } // int on adq
    @Override public String getBlockIdMethodName() { return "o"; } // World.o(cj) -> arc; takes BlockPos not ints
    @Override public String clickMethodName() { return "aw"; } // private void aw() — left click; no-arg, not (int)
    @Override public String mouseGrabbedFieldName() { return "x"; } // boolean inGameHasFocus on bcf
    @Override public String sendChatMessageMethodName() { return "g"; } // bmt.g(String)
    @Override public String dropPlayerItemMethodName() { return "a"; } // zj.a(adq, boolean) -> yd
    @Override public String dropOneItemMethodName() { return null; } // bmt.a(boolean) takes boolean; null = use alpha path
    @Override public String displayGuiScreenMethodName() { return "a"; } // bcf.a(bfb)
    @Override public String currentScreenFieldName() { return "m"; } // bfb on bcf
    @Override public String ingameGuiFieldName() { return "r"; } // bcu (GuiIngame) on bcf
    @Override public String chatLinesFieldName() { return "j"; } // List<bcb> on bda (GuiNewChat); needs 3-level access via bcu.l
    @Override public String cursorItemFieldName() { return "h"; } // adq on zi (InventoryPlayer)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "bfb"; }
    @Override public String sessionFieldName() { return "ag"; } // bcm (Session) on bcf
    @Override public String sessionUsernameFieldName() { return "a"; } // String on bcm
    @Override public String clickCooldownFieldName() { return "ai"; } // int leftClickCounter on bcf
    @Override public String playerControllerFieldName() { return "c"; } // bkt (PlayerControllerMP) on bcf
    @Override public String digMethodName() { return "b"; } // bkt.b(cj, cq) — clickBlock; takes BlockPos+EnumFacing not ints
    @Override public String chatLineTextFieldName() { return "b"; } // eu (IChatComponent) on bcb; NOT String
    @Override public String guiScreenWidthFieldName() { return "l"; } // int on bfb
    @Override public String guiScreenHeightFieldName() { return "m"; } // int on bfb
    @Override public String keyBindingPressedFieldName() { return "h"; } // boolean on bcc (KeyBinding)
    @Override public String forwardKeyFieldName() { return "P"; } // bcc on bch
    @Override public String leftKeyFieldName() { return "Q"; }
    @Override public String backKeyFieldName() { return "R"; }
    @Override public String rightKeyFieldName() { return "S"; }
    @Override public String jumpKeyFieldName() { return "T"; }
    @Override public String sneakKeyFieldName() { return "U"; }
    @Override public String creativeInventoryClassName() { return "bgc"; }
    @Override public String guiInventoryClassName() { return "bgk"; }
    @Override public String closeContainerMethodName() { return "q"; } // bmt.q() — sends close window + resets
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "ax"; } // right-click/use method
    @Override public boolean isNettyClient() { return true; }
}
