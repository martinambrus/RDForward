package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.13.1 (protocol v401).
 * LWJGL3 version. Verified by CFR decompilation of the 1.13.1 client JAR.
 *
 * Minecraft = cfs, Entity = aer, EntityLivingBase = afa, EntityPlayer = aof,
 * AbstractClientPlayer = ctf, EntityPlayerSP = cti,
 * GameSettings = cfv, InventoryPlayer = aoe, ItemStack = atd, Item = asz,
 * Block = bdr, World = axx, WorldClient = crf, Window = cfp,
 * GuiScreen = ckc, GuiIngame = cgk, GuiNewChat = cgu, ChatLine = cfl,
 * PlayerControllerMP = cre, KeyBinding = cfo, Session = cfz,
 * MovementInput = ctg, MouseHelper = cfu,
 * GuiContainerCreative = clg, GuiInventory = clo.
 *
 * Key differences from V393 (1.13):
 * - Minecraft class: cfs (was cfi). Session class: cfz (was cfp).
 * - Window class: cfp (was cfs). MouseHelper class: cfu (was cfk).
 * - Entity class: aer (was aeo). EntityLivingBase: afa (was aex).
 * - EntityPlayer: aof (was aoc). EntityPlayerSP: cti (was csy).
 * - PlayerControllerMP: cre (was cqu). WorldClient: crf (was cqv).
 * - InventoryPlayer: aoe (was aob). ItemStack: atd (was ata). Item: asz (was asw).
 * - GuiScreen: ckc (was cjs). GuiIngame: cgk (was cga). GuiNewChat: cgu (was cgk).
 * - ChatLine: cfl (was cfb). KeyBinding: cfo (was cfe).
 * - GameSettings: cfv (was cfl). MovementInput: ctg (was csw).
 * - GuiContainerCreative: clg (was ckw). GuiInventory: clo (was cle).
 * - Field names on Minecraft: mostly same letters (i=player, g=world, e=playerController,
 *   t=gameSettings, u=mouseHelper, v=window, Z=session, m=currentScreen, q=ingameGui, p=clickCooldown).
 * - Entity pos q/r/s, yaw/pitch w/x, onGround A: unchanged.
 * - Inventory bB on aof, mainInventory a, currentItem d, cursorItem g: unchanged.
 * - ItemStack item f, count d: unchanged.
 * - Key bindings X/Y/Z/aa/ab/ac on cfv: unchanged.
 * - KeyBinding pressed i: unchanged.
 * - GuiScreen width m, height n: unchanged.
 * - ChatLine text b: unchanged.
 * - getBlockState a_ on axx: unchanged.
 * - dig b(el,eq) on cre: unchanged.
 * - sendChatMessage f(String), closeContainer dm(), dropPlayerItem a(boolean): unchanged.
 */
public class NettyReleaseV401Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "cfs"; }
    @Override public String runMethodName() { return "a"; } // Main calls new cfs(...).a()
    @Override public String tickMethodName() { return "m"; } // cfs.m() game tick
    @Override public String playerFieldName() { return "i"; } // cti (EntityPlayerSP) on cfs
    @Override public String worldFieldName() { return "g"; } // crf (WorldClient) on cfs
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return null; } // LWJGL3: moved to Window (cfp); defaults to 854
    @Override public String displayHeightFieldName() { return null; } // LWJGL3: moved to Window (cfp); defaults to 480
    @Override public String posXFieldName() { return "q"; } // double on aer (Entity)
    @Override public String posYFieldName() { return "r"; }
    @Override public String posZFieldName() { return "s"; }
    @Override public String gameSettingsFieldName() { return "t"; } // cfv (GameSettings) on cfs
    @Override public String movementInputFieldName() { return "e"; } // ctg (MovementInput) on cti (EntityPlayerSP)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "w"; } // float on aer (Entity)
    @Override public String pitchFieldName() { return "x"; }
    @Override public String onGroundFieldName() { return "A"; } // boolean on aer (Entity)
    @Override public String inventoryFieldName() { return "bB"; } // aoe (InventoryPlayer) on aof (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // ez<atd> (NonNullList<ItemStack>) on aoe
    @Override public String currentItemFieldName() { return "d"; } // int on aoe (InventoryPlayer)
    @Override public String itemIdFieldName() { return "f"; } // asz (Item) on atd (ItemStack); NOT int
    @Override public String stackSizeFieldName() { return "d"; } // int on atd (ItemStack)
    @Override public String getBlockIdMethodName() { return "a_"; } // axx.a_(el) returns blb (IBlockState)
    @Override public String clickMethodName() { return "d"; } // cfs.d(boolean) left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on cfu (MouseHelper); MouseHelper is field u on cfs
    @Override public String mouseHelperFieldName() { return "u"; } // cfu (MouseHelper) on cfs
    @Override public String sendChatMessageMethodName() { return "f"; } // cti.f(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "a"; } // cti.a(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // cfs.a(ckc) sets current screen
    @Override public String currentScreenFieldName() { return "m"; } // ckc (GuiScreen) on cfs
    @Override public String ingameGuiFieldName() { return "q"; } // cgk (GuiIngame) on cfs
    @Override public String chatLinesFieldName() { return "i"; } // List<cfl> allChatLines on cgu (GuiNewChat); cgu is field "l" on cgk
    @Override public String cursorItemFieldName() { return "g"; } // atd (ItemStack) on aoe (InventoryPlayer)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "ckc"; } // GuiScreen
    @Override public String sessionFieldName() { return "Z"; } // cfz (Session) on cfs
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on cfz (Session)
    @Override public String clickCooldownFieldName() { return "p"; } // int leftClickCounter on cfs
    @Override public String playerControllerFieldName() { return "e"; } // cre (PlayerControllerMP) on cfs
    @Override public String digMethodName() { return "b"; } // cre.b(el, eq) onPlayerDamageBlock
    @Override public String chatLineTextFieldName() { return "b"; } // ij (ITextComponent) on cfl (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "m"; } // int on ckc (GuiScreen)
    @Override public String guiScreenHeightFieldName() { return "n"; } // int on ckc (GuiScreen)
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on cfo (KeyBinding)
    @Override public String forwardKeyFieldName() { return "X"; } // cfo on cfv (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "Y"; } // cfo on cfv (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "Z"; } // cfo on cfv (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "aa"; } // cfo on cfv (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ab"; } // cfo on cfv (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "ac"; } // cfo on cfv (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "clg"; } // GuiContainerCreative
    @Override public String guiInventoryClassName() { return "clo"; } // GuiInventory
    @Override public String closeContainerMethodName() { return "dm"; } // cti.dm() sends close window packet
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "e"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
}
