package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.12 (protocol v335).
 * Verified by CFR decompilation of the 1.12 client JAR.
 *
 * Class hierarchy:
 *   Minecraft = bhz, Entity = ve, EntityLivingBase = vn, EntityPlayer = aeb,
 *   AbstractClientPlayer = bty, EntityPlayerSP = bub,
 *   World = ams, WorldClient = brz, GameSettings = bib, KeyBinding = bhw,
 *   InventoryPlayer = aea, ItemStack = ain, Item = ail,
 *   GuiScreen = bli, GuiIngame = bio, GuiNewChat = biz, ChatLine = bhv,
 *   PlayerControllerMP = bry, Session = big, MovementInput = btz,
 *   GuiInventory = bmv, GuiContainerCreative = bmn.
 *
 * Notable changes from 1.11.2 (v316):
 * - Minecraft class: bes -> bhz. Several field name shifts.
 * - gameSettingsFieldName: u -> t (bib t).
 * - ingameGuiFieldName: r -> q (bio q).
 * - sessionFieldName: ae -> af (big af).
 * - clickCooldownFieldName: aq -> ai.
 * - clickMethodName: aw -> aA (no-arg private left-click).
 * - inventoryFieldName: bs -> bv (aea bv on aeb).
 * - cursorItemFieldName: h -> g (ain g on aea).
 * - keyBindingPressedFieldName: h -> i (boolean i on bhw).
 * - closeContainerMethodName: q -> p (bub.p() sends close + resets).
 * - GuiScreen: bho -> bli.
 * - GuiContainerCreative: bip -> bmn.
 * - GuiInventory: bix -> bmv.
 * - GuiNewChat chatLinesFieldName: h (unchanged, List of bhv on biz).
 * - Entity/key-binding field names unchanged from v316.
 */
public class NettyReleaseV335Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "bhz"; }
    @Override public String runMethodName() { return "a"; } // Main calls new bhz(...).a()
    @Override public String tickMethodName() { return "t"; } // bhz.t() is the tick method
    @Override public String playerFieldName() { return "h"; } // bub h (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // brz f (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; } // int d
    @Override public String displayHeightFieldName() { return "e"; } // int e
    @Override public String posXFieldName() { return "p"; } // double p on ve (Entity)
    @Override public String posYFieldName() { return "q"; } // double q on ve (Entity)
    @Override public String posZFieldName() { return "r"; } // double r on ve (Entity)
    @Override public String gameSettingsFieldName() { return "t"; } // bib t (GameSettings)
    @Override public String movementInputFieldName() { return "e"; } // btz e on bub (EntityPlayerSP)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "v"; } // float v on ve (Entity)
    @Override public String pitchFieldName() { return "w"; } // float w on ve (Entity)
    @Override public String onGroundFieldName() { return "z"; } // boolean z on ve (Entity)
    @Override public String inventoryFieldName() { return "bv"; } // aea bv on aeb (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // fi<ain> a on aea (NonNullList)
    @Override public String currentItemFieldName() { return "d"; } // int d on aea (InventoryPlayer)
    @Override public String itemIdFieldName() { return "e"; } // ail e on ain (Item object, not int)
    @Override public String stackSizeFieldName() { return "c"; } // int c on ain (ItemStack)
    @Override public String getBlockIdMethodName() { return "o"; } // ams.o(et) returns awr (IBlockState)
    @Override public String clickMethodName() { return "aA"; } // private void aA() left-click attack; no-arg
    @Override public String mouseGrabbedFieldName() { return "x"; } // boolean x (inGameHasFocus)
    @Override public String sendChatMessageMethodName() { return "g"; } // bub.g(String)
    @Override public String dropPlayerItemMethodName() { return "a"; } // aeb.a(ain, boolean) drop item
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // bhz.a(bli)
    @Override public String currentScreenFieldName() { return "m"; } // bli m on bhz
    @Override public String ingameGuiFieldName() { return "q"; } // bio q on bhz (GuiIngame)
    @Override public String chatLinesFieldName() { return "h"; } // List<bhv> h on biz (GuiNewChat); via bio.d()
    @Override public String cursorItemFieldName() { return "g"; } // ain g on aea (InventoryPlayer, private)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "bli"; } // GuiScreen
    @Override public String sessionFieldName() { return "af"; } // big af on bhz (Session)
    @Override public String sessionUsernameFieldName() { return "a"; } // String a on big (username)
    @Override public String clickCooldownFieldName() { return "ai"; } // int ai on bhz (leftClickCounter)
    @Override public String playerControllerFieldName() { return "c"; } // bry c on bhz (PlayerControllerMP)
    @Override public String digMethodName() { return "b"; } // bry.b(et, fa) onPlayerDamageBlock
    @Override public String chatLineTextFieldName() { return "b"; } // hh b on bhv (ITextComponent, not String)
    @Override public String guiScreenWidthFieldName() { return "l"; } // int l on bli (GuiScreen)
    @Override public String guiScreenHeightFieldName() { return "m"; } // int m on bli (GuiScreen)
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean i on bhw (KeyBinding)
    @Override public String forwardKeyFieldName() { return "T"; } // bhw T on bib (key.forward)
    @Override public String leftKeyFieldName() { return "U"; } // bhw U on bib (key.left)
    @Override public String backKeyFieldName() { return "V"; } // bhw V on bib (key.back)
    @Override public String rightKeyFieldName() { return "W"; } // bhw W on bib (key.right)
    @Override public String jumpKeyFieldName() { return "X"; } // bhw X on bib (key.jump)
    @Override public String sneakKeyFieldName() { return "Y"; } // bhw Y on bib (key.sneak)
    @Override public String creativeInventoryClassName() { return "bmn"; } // GuiContainerCreative
    @Override public String guiInventoryClassName() { return "bmv"; } // GuiInventory
    @Override public String closeContainerMethodName() { return "p"; } // bub.p() sends close window + resets
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "aB"; } // right-click/use method
    @Override public boolean isNettyClient() { return true; }
}
