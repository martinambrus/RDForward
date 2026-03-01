package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.11.2 (protocol v316).
 * Verified by CFR decompilation of the 1.11.2 client JAR.
 *
 * Class hierarchy:
 *   Minecraft = bes, Entity = sn, EntityLivingBase = sw, EntityPlayer = aay,
 *   AbstractClientPlayer = bpp, EntityPlayerSP = bps,
 *   World = ajs, WorldClient = bnq, GameSettings = beu, KeyBinding = bep,
 *   InventoryPlayer = aax, ItemStack = afj, Item = afh,
 *   GuiScreen = bho, GuiIngame = bfh, GuiNewChat = bfn, ChatLine = beo,
 *   PlayerControllerMP = bnp, Session = bez, MovementInput = bpq,
 *   GuiInventory = bix, GuiContainerCreative = bip.
 *
 * Notable changes from 1.9.4/1.10 (v110):
 * - Minecraft class: bcd -> bes. Most field names unchanged.
 * - clickMethodName: b(boolean) -> aw() (no-arg private, left-click attack).
 * - clickCooldownFieldName: ai -> aq.
 * - sessionFieldName: ag -> ae.
 * - ItemStack: stackSizeFieldName b -> c, itemIdFieldName d -> e.
 *   (New static fields afj.a (EMPTY) and afj.b (DecimalFormat) shifted instance fields.)
 * - GuiNewChat chatLines: j -> h (fewer static/private fields in bfn).
 * - GuiContainerCreative: bga -> bip. GuiInventory: bgi -> bix.
 * - chatLineTextFieldName: b (fb = ITextComponent, not String).
 * - getBlockIdMethodName: o (ajs.o(co) returns IBlockState, not int).
 * - digMethodName: b (bnp.b(co, cv) = onPlayerDamageBlock; takes BlockPos+EnumFacing).
 * - mainInventoryFieldName: a (dd<afj> = NonNullList, not array).
 * - dropOneItemMethodName: null (uses a(boolean), no no-arg variant).
 */
public class NettyReleaseV316Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "bes"; }
    @Override public String runMethodName() { return "a"; } // Main calls new bes(...).a()
    @Override public String tickMethodName() { return "t"; } // bes.t() is the tick method
    @Override public String playerFieldName() { return "h"; } // bps h (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // bnq f (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; } // int d
    @Override public String displayHeightFieldName() { return "e"; } // int e
    @Override public String posXFieldName() { return "p"; } // double p on sn (Entity)
    @Override public String posYFieldName() { return "q"; } // double q on sn (Entity)
    @Override public String posZFieldName() { return "r"; } // double r on sn (Entity)
    @Override public String gameSettingsFieldName() { return "u"; } // beu u (GameSettings)
    @Override public String movementInputFieldName() { return "e"; } // bpq e on bps (EntityPlayerSP)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "v"; } // float v on sn (Entity)
    @Override public String pitchFieldName() { return "w"; } // float w on sn (Entity)
    @Override public String onGroundFieldName() { return "z"; } // boolean z on sn (Entity)
    @Override public String inventoryFieldName() { return "bs"; } // aax bs on aay (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // dd<afj> a on aax (NonNullList, not array)
    @Override public String currentItemFieldName() { return "d"; } // int d on aax (InventoryPlayer)
    @Override public String itemIdFieldName() { return "e"; } // afh e on afj (Item object, not int)
    @Override public String stackSizeFieldName() { return "c"; } // int c on afj (ItemStack)
    @Override public String getBlockIdMethodName() { return "o"; } // ajs.o(co) returns atl (IBlockState)
    @Override public String clickMethodName() { return "aw"; } // bes.aw() left-click attack (no-arg)
    @Override public String mouseGrabbedFieldName() { return "x"; } // boolean x (inGameHasFocus)
    @Override public String sendChatMessageMethodName() { return "g"; } // bps.g(String)
    @Override public String dropPlayerItemMethodName() { return "a"; } // bps.a(boolean) sends dig packet
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // bes.a(bho)
    @Override public String currentScreenFieldName() { return "m"; } // bho m on bes
    @Override public String ingameGuiFieldName() { return "r"; } // bfh r on bes (GuiIngame)
    @Override public String chatLinesFieldName() { return "h"; } // List<beo> h on bfn (GuiNewChat); via bfh.l
    @Override public String cursorItemFieldName() { return "h"; } // afj h on aax (InventoryPlayer, private)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "bho"; } // GuiScreen
    @Override public String sessionFieldName() { return "ae"; } // bez ae on bes (Session)
    @Override public String sessionUsernameFieldName() { return "a"; } // String a on bez (username)
    @Override public String clickCooldownFieldName() { return "aq"; } // int aq on bes (leftClickCounter)
    @Override public String playerControllerFieldName() { return "c"; } // bnp c on bes (PlayerControllerMP)
    @Override public String digMethodName() { return "b"; } // bnp.b(co, cv) onPlayerDamageBlock
    @Override public String chatLineTextFieldName() { return "b"; } // fb b on beo (ITextComponent, not String)
    @Override public String guiScreenWidthFieldName() { return "l"; } // int l on bho (GuiScreen)
    @Override public String guiScreenHeightFieldName() { return "m"; } // int m on bho (GuiScreen)
    @Override public String keyBindingPressedFieldName() { return "h"; } // boolean h on bep (KeyBinding)
    @Override public String forwardKeyFieldName() { return "S"; } // bep S on beu (key.forward)
    @Override public String leftKeyFieldName() { return "T"; } // bep T on beu (key.left)
    @Override public String backKeyFieldName() { return "U"; } // bep U on beu (key.back)
    @Override public String rightKeyFieldName() { return "V"; } // bep V on beu (key.right)
    @Override public String jumpKeyFieldName() { return "W"; } // bep W on beu (key.jump)
    @Override public String sneakKeyFieldName() { return "X"; } // bep X on beu (key.sneak)
    @Override public String creativeInventoryClassName() { return "bip"; } // GuiContainerCreative
    @Override public String guiInventoryClassName() { return "bix"; } // GuiInventory
    @Override public String closeContainerMethodName() { return "q"; } // bps.q() sends close + resets
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "ax"; } // right-click/use method
    @Override public boolean isNettyClient() { return true; }
}
