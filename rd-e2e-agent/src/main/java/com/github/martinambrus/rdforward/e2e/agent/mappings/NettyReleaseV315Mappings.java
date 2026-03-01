package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.11 (protocol v315).
 * Verified by CFR decompilation of the 1.11 client JAR.
 *
 * Minecraft = beq, Entity = sm, EntityLivingBase = sv, EntityPlayer = aax,
 * AbstractClientPlayer = bpn, EntityPlayerSP = bpq,
 * GameSettings = bes, InventoryPlayer = aaw, ItemStack = afi, Item = afg,
 * GuiScreen = bhm, GuiIngame = bff, GuiNewChat = bfl, ChatLine = bem,
 * PlayerControllerMP = bnn, KeyBinding = ben, Session = bex,
 * WorldClient = bno (extends World ajq), MovementInput = bpo,
 * GuiContainerCreative = bin, GuiInventory = biv,
 * GuiContainer = bie (EffectRenderingInventoryScreen base = bip).
 *
 * Differences from V110 (1.9.4):
 * - Minecraft class: bcd -> beq
 * - EntityPlayerSP: bmr -> bpq
 * - AbstractClientPlayer: bmo -> bpn
 * - WorldClient: bks -> bno
 * - PlayerControllerMP: bkr -> bnn
 * - GameSettings: bcf -> bes
 * - GuiScreen: bez -> bhm
 * - GuiIngame: bcs -> bff (field r unchanged)
 * - GuiNewChat: bcy -> bfl
 * - Session: bck -> bex
 * - KeyBinding: bca -> ben
 * - ChatLine: bbz -> bem
 * - MovementInput: bmp -> bpo
 * - inventoryFieldName on EntityPlayer (aax): bs (unchanged)
 * - ItemStack: adq -> afi; item field d -> e; stackSize field b -> c
 * - GuiContainerCreative: bga -> bin
 * - GuiInventory: bgi -> biv
 * - sessionFieldName: ag -> ae
 * - clickCooldownFieldName: ai -> ag
 * - Key bindings shifted: Q->S, R->T, S->U, T->V, U->W, V->X
 *
 * All field names on Entity (sm), InventoryPlayer (aaw), and most
 * Minecraft fields remain identical to V110.
 */
public class NettyReleaseV315Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "beq"; }
    @Override public String runMethodName() { return "a"; } // Main calls new beq(...).a()
    @Override public String tickMethodName() { return "t"; } // beq.t() is the tick method
    @Override public String playerFieldName() { return "h"; } // bpq h (EntityPlayerSP)
    @Override public String worldFieldName() { return "f"; } // bno f (WorldClient)
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "d"; } // int d
    @Override public String displayHeightFieldName() { return "e"; } // int e
    @Override public String posXFieldName() { return "p"; } // double on Entity (sm)
    @Override public String posYFieldName() { return "q"; }
    @Override public String posZFieldName() { return "r"; }
    @Override public String gameSettingsFieldName() { return "u"; } // bes u (GameSettings)
    @Override public String movementInputFieldName() { return "e"; } // bpo e on bpq (EntityPlayerSP)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "v"; } // float on Entity (sm)
    @Override public String pitchFieldName() { return "w"; }
    @Override public String onGroundFieldName() { return "z"; } // boolean on Entity (sm)
    @Override public String inventoryFieldName() { return "bs"; } // aaw bs on aax (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // dd<afi> a on aaw (InventoryPlayer)
    @Override public String currentItemFieldName() { return "d"; } // int d on aaw (InventoryPlayer)
    @Override public String itemIdFieldName() { return "e"; } // afg e on afi (Item object, not int)
    @Override public String stackSizeFieldName() { return "c"; } // int c on afi (ItemStack)
    @Override public String getBlockIdMethodName() { return "o"; } // ajq.o(co) -> atj (IBlockState)
    @Override public String clickMethodName() { return "aw"; } // private void aw() left-click attack; no-arg
    @Override public String mouseGrabbedFieldName() { return "x"; } // boolean inGameHasFocus on beq
    @Override public String sendChatMessageMethodName() { return "g"; } // bpq.g(String)
    @Override public String dropPlayerItemMethodName() { return "a"; } // aax.a(boolean) -> zi
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // beq.a(bhm)
    @Override public String currentScreenFieldName() { return "m"; } // bhm m on beq
    @Override public String ingameGuiFieldName() { return "r"; } // bff r on beq (GuiIngame)
    @Override public String chatLinesFieldName() { return "j"; } // List<bem> j on bfl (GuiNewChat); 3-level via bff.d()
    @Override public String cursorItemFieldName() { return "h"; } // afi h on aaw (InventoryPlayer)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "bhm"; }
    @Override public String sessionFieldName() { return "ae"; } // bex ae on beq (Session)
    @Override public String sessionUsernameFieldName() { return "a"; } // String a on bex (username)
    @Override public String clickCooldownFieldName() { return "ag"; } // int ag on beq (leftClickCounter)
    @Override public String playerControllerFieldName() { return "c"; } // bnn c on beq (PlayerControllerMP)
    @Override public String digMethodName() { return "b"; } // bnn.b(co, cv) onPlayerDamageBlock
    @Override public String chatLineTextFieldName() { return "b"; } // fb b on bem (IChatComponent, not String)
    @Override public String guiScreenWidthFieldName() { return "l"; } // int l on bhm (GuiScreen)
    @Override public String guiScreenHeightFieldName() { return "m"; } // int m on bhm (GuiScreen)
    @Override public String keyBindingPressedFieldName() { return "h"; } // boolean h on ben (KeyBinding)
    @Override public String forwardKeyFieldName() { return "S"; } // ben S on bes (key.forward)
    @Override public String leftKeyFieldName() { return "T"; } // ben T on bes (key.left)
    @Override public String backKeyFieldName() { return "U"; } // ben U on bes (key.back)
    @Override public String rightKeyFieldName() { return "V"; } // ben V on bes (key.right)
    @Override public String jumpKeyFieldName() { return "W"; } // ben W on bes (key.jump)
    @Override public String sneakKeyFieldName() { return "X"; } // ben X on bes (key.sneak)
    @Override public String creativeInventoryClassName() { return "bin"; } // GuiContainerCreative
    @Override public String guiInventoryClassName() { return "biv"; } // GuiInventory
    @Override public String closeContainerMethodName() { return "q"; } // bpq.q() sends close window + resets
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "ax"; } // right-click/use method
    @Override public boolean isNettyClient() { return true; }
}
