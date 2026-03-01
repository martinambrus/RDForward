package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.13 (protocol v393).
 * First LWJGL3 version. Verified by CFR decompilation of the 1.13 client JAR.
 *
 * Minecraft = cfi, Entity = aeo, EntityLivingBase = aex, EntityPlayer = aoc,
 * AbstractClientPlayer = csv, EntityPlayerSP = csy,
 * GameSettings = cfl, InventoryPlayer = aob, ItemStack = ata, Item = asw,
 * Block = bcj, World = axs, WorldClient = cqv, Window = cfs,
 * GuiScreen = cjs, GuiIngame = cga, GuiNewChat = cgk, ChatLine = cfb,
 * PlayerControllerMP = cqu, KeyBinding = cfe, Session = cfp,
 * MovementInput = csw, MouseHelper = cfk,
 * GuiContainerCreative = ckw, GuiInventory = cle.
 *
 * Key differences from V340 (1.12.2):
 * - LWJGL3: MouseHelper (cfk) is now a separate object (field "u" on cfi);
 *   mouseGrabbed is field "r" on cfk, no longer a direct boolean on Minecraft.
 * - LWJGL3: Display dimensions moved to Window class (cfs, field "f" on cfi);
 *   no direct int displayWidth/displayHeight on Minecraft.
 * - Entity position shifted: posX/Y/Z = q/r/s (was p/q/r), yaw/pitch = w/x (was v/w),
 *   onGround = A (was z).
 * - Session field = "Z" on cfi (was "af" on bib).
 * - Click cooldown = "p" on cfi (was "ai" on bib).
 * - PlayerController = "e" on cfi (was "c" on bib).
 * - Inventory = "bB" on aoc (was "bv" on aed).
 * - Click method = "d" (boolean) on cfi (was "b" on bib).
 * - Tick method = "m" on cfi (was "t" on bib).
 * - GuiScreen width/height = m/n on cjs (was l/m on blk).
 * - ItemStack item field = "f" (was "e"), stackSize = "d" (was "c").
 * - getBlockState = "a_" on axs (was "o" on amu).
 * - closeContainer = "dm" on csy (was "p" on bud).
 * - Key bindings = X/Y/Z/aa/ab/ac on cfl (was T/U/V/W/X/Y on bid).
 */
public class NettyReleaseV393Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "cfi"; }
    @Override public String runMethodName() { return "a"; } // Main calls new cfi(...).a()
    @Override public String tickMethodName() { return "m"; } // cfi.m() game tick
    @Override public String playerFieldName() { return "i"; } // csy (EntityPlayerSP) on cfi
    @Override public String worldFieldName() { return "g"; } // cqv (WorldClient) on cfi
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return null; } // LWJGL3: moved to Window (cfs); defaults to 854
    @Override public String displayHeightFieldName() { return null; } // LWJGL3: moved to Window (cfs); defaults to 480
    @Override public String posXFieldName() { return "q"; } // double on aeo (Entity)
    @Override public String posYFieldName() { return "r"; }
    @Override public String posZFieldName() { return "s"; }
    @Override public String gameSettingsFieldName() { return "t"; } // cfl (GameSettings) on cfi
    @Override public String movementInputFieldName() { return "e"; } // csw (MovementInput) on csy
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "w"; } // float on aeo (Entity)
    @Override public String pitchFieldName() { return "x"; }
    @Override public String onGroundFieldName() { return "A"; } // boolean on aeo (Entity)
    @Override public String inventoryFieldName() { return "bB"; } // aob (InventoryPlayer) on aoc (EntityPlayer)
    @Override public String mainInventoryFieldName() { return "a"; } // NonNullList<ata> on aob (InventoryPlayer)
    @Override public String currentItemFieldName() { return "d"; } // int on aob (InventoryPlayer)
    @Override public String itemIdFieldName() { return "f"; } // asw (Item) on ata (ItemStack); NOT int
    @Override public String stackSizeFieldName() { return "d"; } // int on ata (ItemStack)
    @Override public String getBlockIdMethodName() { return "a_"; } // axs.a_(ej) returns bkt (IBlockState)
    @Override public String clickMethodName() { return "d"; } // cfi.d(boolean) left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on cfk (MouseHelper); MouseHelper is field u on cfi
    @Override public String mouseHelperFieldName() { return "u"; } // cfk (MouseHelper) on cfi
    @Override public String sendChatMessageMethodName() { return "f"; } // csy.f(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "a"; } // csy.a(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // cfi.a(cjs) sets current screen
    @Override public String currentScreenFieldName() { return "m"; } // cjs (GuiScreen) on cfi
    @Override public String ingameGuiFieldName() { return "q"; } // cga (GuiIngame) on cfi
    @Override public String chatLinesFieldName() { return "i"; } // List<cfb> allChatLines on cgk (GuiNewChat); field "l" on cga
    @Override public String cursorItemFieldName() { return "g"; } // ata (ItemStack) on aob (InventoryPlayer)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "cjs"; } // GuiScreen
    @Override public String sessionFieldName() { return "Z"; } // cfp (Session) on cfi
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on cfp (Session)
    @Override public String clickCooldownFieldName() { return "p"; } // int leftClickCounter on cfi
    @Override public String playerControllerFieldName() { return "e"; } // cqu (PlayerControllerMP) on cfi
    @Override public String digMethodName() { return "b"; } // cqu.b(ej, ep) onPlayerDamageBlock
    @Override public String chatLineTextFieldName() { return "b"; } // ij (ITextComponent) on cfb (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "m"; } // int on cjs (GuiScreen)
    @Override public String guiScreenHeightFieldName() { return "n"; } // int on cjs (GuiScreen)
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on cfe (KeyBinding)
    @Override public String forwardKeyFieldName() { return "X"; } // cfe on cfl (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "Y"; } // cfe on cfl (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "Z"; } // cfe on cfl (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "aa"; } // cfe on cfl (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ab"; } // cfe on cfl (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "ac"; } // cfe on cfl (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "ckw"; } // GuiContainerCreative
    @Override public String guiInventoryClassName() { return "cle"; } // GuiInventory
    @Override public String closeContainerMethodName() { return "dm"; } // csy.dm() sends close window packet
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "e"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
}
