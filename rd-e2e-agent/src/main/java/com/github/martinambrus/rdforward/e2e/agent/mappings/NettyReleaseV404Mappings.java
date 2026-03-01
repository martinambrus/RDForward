package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.13.2 (protocol v404).
 * LWJGL3 version. Verified by CFR decompilation of the 1.13.2 client JAR.
 *
 * Minecraft = cft, Entity = aer, EntityLivingBase = afa, EntityPlayer = aog,
 * AbstractClientPlayer = ctg, EntityPlayerSP = ctj,
 * GameSettings = cfw, InventoryPlayer = aof, ItemStack = ate, Item = ata,
 * Block = bct, World = axy, WorldClient = crg, Window = cgd,
 * GuiScreen = ckd, GuiIngame = cgl, GuiNewChat = cgv, ChatLine = cfm,
 * PlayerControllerMP = crf, KeyBinding = cfp, Session = cga,
 * MovementInput = cth, MouseHelper = cfv,
 * GuiContainerCreative = clh, GuiInventory = clp.
 *
 * Key differences from V393 (1.13):
 * - Minecraft class: cft (was cfi). Run method still a().
 * - Entity class: aer (was aeo). posX/Y/Z = q/r/s, yaw/pitch = w/x, onGround = A (unchanged).
 * - EntityPlayerSP: ctj (was csy). sendChatMessage = f(String), closeContainer = dm().
 * - GameSettings: cfw (was cfl). Key bindings X/Y/Z/aa/ab/ac (unchanged).
 * - InventoryPlayer: aof (was aob). Fields a/d/g (unchanged).
 * - ItemStack: ate (was ata). Fields d/f (unchanged).
 * - KeyBinding: cfp (was cfe). pressed = i (unchanged).
 * - Session: cga (was cfp). username = a (unchanged).
 * - GuiScreen: ckd (was cjs). width/height = m/n (unchanged).
 * - GuiIngame: cgl (was cga). GuiNewChat = field l.
 * - GuiNewChat: cgv (was cgk). allChatLines = i (unchanged).
 * - ChatLine: cfm (was cfb). text = b (unchanged).
 * - PlayerControllerMP: crf (was cqu). onPlayerDamageBlock = b(el,eq) (unchanged).
 * - World: axy (was axs). getBlockState = a_(el) (unchanged).
 * - GuiContainerCreative: clh (was ckw).
 * - GuiInventory: clp (was cle).
 * - MouseHelper: cfv (was cfk).
 */
public class NettyReleaseV404Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "cft"; }
    @Override public String runMethodName() { return "a"; } // Main calls new cft(...).a()
    @Override public String tickMethodName() { return "m"; } // cft.m() game tick
    @Override public String playerFieldName() { return "i"; } // ctj (EntityPlayerSP) on cft
    @Override public String worldFieldName() { return "g"; } // crg (WorldClient) on cft
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return null; } // LWJGL3: moved to Window (cgd); defaults to 854
    @Override public String displayHeightFieldName() { return null; } // LWJGL3: moved to Window (cgd); defaults to 480
    @Override public String posXFieldName() { return "q"; } // double on aer (Entity)
    @Override public String posYFieldName() { return "r"; }
    @Override public String posZFieldName() { return "s"; }
    @Override public String gameSettingsFieldName() { return "t"; } // cfw (GameSettings) on cft
    @Override public String movementInputFieldName() { return "e"; } // cth (MovementInput) on ctj
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "w"; } // float on aer (Entity)
    @Override public String pitchFieldName() { return "x"; }
    @Override public String onGroundFieldName() { return "A"; } // boolean on aer (Entity)
    @Override public String inventoryFieldName() { return "bB"; } // aof (InventoryPlayer) on aog (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // NonNullList<ate> on aof (InventoryPlayer)
    @Override public String currentItemFieldName() { return "d"; } // int on aof (InventoryPlayer)
    @Override public String itemIdFieldName() { return "f"; } // ata (Item) on ate (ItemStack); NOT int
    @Override public String stackSizeFieldName() { return "d"; } // int on ate (ItemStack)
    @Override public String getBlockIdMethodName() { return "a_"; } // axy.a_(el) returns blc (IBlockState)
    @Override public String clickMethodName() { return "d"; } // cft.d(boolean) left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on cfv (MouseHelper); MouseHelper is field u on cft
    @Override public String mouseHelperFieldName() { return "u"; } // cfv (MouseHelper) on cft
    @Override public String sendChatMessageMethodName() { return "f"; } // ctj.f(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "a"; } // aog.a(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // cft.a(ckd) sets current screen
    @Override public String currentScreenFieldName() { return "m"; } // ckd (GuiScreen) on cft
    @Override public String ingameGuiFieldName() { return "q"; } // cgl (GuiIngame) on cft
    @Override public String chatLinesFieldName() { return "i"; } // List<cfm> allChatLines on cgv (GuiNewChat); field "l" on cgl
    @Override public String cursorItemFieldName() { return "g"; } // ate (ItemStack) on aof (InventoryPlayer)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "ckd"; } // GuiScreen
    @Override public String sessionFieldName() { return "Z"; } // cga (Session) on cft
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on cga (Session)
    @Override public String clickCooldownFieldName() { return "p"; } // int leftClickCounter on cft
    @Override public String playerControllerFieldName() { return "e"; } // crf (PlayerControllerMP) on cft
    @Override public String digMethodName() { return "b"; } // crf.b(el, eq) onPlayerDamageBlock
    @Override public String chatLineTextFieldName() { return "b"; } // ij (ITextComponent) on cfm (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "m"; } // int on ckd (GuiScreen)
    @Override public String guiScreenHeightFieldName() { return "n"; } // int on ckd (GuiScreen)
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on cfp (KeyBinding)
    @Override public String forwardKeyFieldName() { return "X"; } // cfp on cfw (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "Y"; } // cfp on cfw (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "Z"; } // cfp on cfw (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "aa"; } // cfp on cfw (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ab"; } // cfp on cfw (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "ac"; } // cfp on cfw (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "clh"; } // GuiContainerCreative
    @Override public String guiInventoryClassName() { return "clp"; } // GuiInventory
    @Override public String closeContainerMethodName() { return "dm"; } // ctj.dm() sends close window packet
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "e"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
}
