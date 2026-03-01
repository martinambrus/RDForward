package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.14 (protocol v477).
 * Verified by CFR decompilation of the 1.14 client JAR.
 *
 * Minecraft = cvi, Entity = aif, LivingEntity = aio, PlayerEntity = avx,
 * AbstractClientPlayerEntity = djs, ClientPlayerEntity = djv,
 * GameSettings = cvm, PlayerInventory = avw, ItemStack = bca, Item = bbv,
 * Block = bml, World = bhh, ClientWorld = dhl,
 * Screen = czr, IngameGui = cwb, NewChatGui = cwp, ChatLine = cvd,
 * PlayerController = dhk, KeyBinding = cvf, Session = cvt,
 * MovementInput = djt, KeyboardInput = dju, Window = cub,
 * MouseHelper = cvj, CreativeInventoryScreen = day, InventoryScreen = dbh.
 *
 * Key differences from V340 (1.12.2 / LWJGL2):
 * - LWJGL3: Window dimensions moved to Window class (cub), accessed via field g on cvi.
 *   displayWidthFieldName/displayHeightFieldName refer to framebuffer fields on cub.
 * - mouseGrabbed moved to MouseHelper (cvj, field x on cvi); boolean r on cvj.
 * - Screen width/height fields are now un-obfuscated: "width" and "height".
 * - minecraftClassName is now the obfuscated "cvi" (default package).
 * - runMethodName is b() (called from Main.java: new cvi(...).b()).
 * - Chat lines are on NewChatGui (cwp, field h on cwb), not directly on IngameGui.
 *   ingameGuiFieldName returns the IngameGui field (t on cvi), chatLinesFieldName
 *   returns the all-messages list field (d on cwp). Agent code needs two-hop resolution
 *   for LWJGL3: Minecraft.t -> cwb.h -> cwp.d.
 */
public class NettyReleaseV477Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "cvi"; } // default package, obfuscated
    @Override public String runMethodName() { return "b"; } // Main calls new cvi(...).b()
    @Override public String tickMethodName() { return "n"; } // cvi.n() game tick; decrements s, ticks screens
    @Override public String playerFieldName() { return "j"; } // djv (ClientPlayerEntity) on cvi
    @Override public String worldFieldName() { return "h"; } // dhl (ClientWorld) on cvi
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "r"; } // int framebufferWidth on cub (Window); Window is field g on cvi
    @Override public String displayHeightFieldName() { return "s"; } // int framebufferHeight on cub (Window); Window is field g on cvi
    @Override public String displayObjectFieldName() { return "g"; } // cub (Window) on cvi
    @Override public String posXFieldName() { return "p"; } // double on aif (Entity)
    @Override public String posYFieldName() { return "q"; } // double on aif (Entity)
    @Override public String posZFieldName() { return "r"; } // double on aif (Entity)
    @Override public String gameSettingsFieldName() { return "w"; } // cvm (GameSettings) on cvi
    @Override public String movementInputFieldName() { return "f"; } // djt (MovementInput) on djv (ClientPlayerEntity)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "s"; } // float on aif (Entity)
    @Override public String pitchFieldName() { return "t"; } // float on aif (Entity)
    @Override public String onGroundFieldName() { return "w"; } // boolean on aif (Entity)
    @Override public String inventoryFieldName() { return "bx"; } // avw (PlayerInventory) on avx (PlayerEntity)
    @Override public String mainInventoryFieldName() { return "a"; } // fj<bca> (NonNullList<ItemStack>, size 36) on avw
    @Override public String currentItemFieldName() { return "d"; } // int selectedSlot on avw (PlayerInventory)
    @Override public String itemIdFieldName() { return "f"; } // bbv (Item) on bca (ItemStack); deprecated field
    @Override public String stackSizeFieldName() { return "d"; } // int count on bca (ItemStack)
    @Override public String getBlockIdMethodName() { return "e_"; } // bhh.e_(ev) returns bvj (BlockState)
    @Override public String clickMethodName() { return "e"; } // cvi.e(boolean) continuous left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on cvj (MouseHelper); MouseHelper is field x on cvi
    @Override public String mouseHelperFieldName() { return "x"; } // cvj (MouseHelper) on cvi
    @Override public String sendChatMessageMethodName() { return "f"; } // djv.f(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "n"; } // djv.n(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses n(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // cvi.a(czr) sets current screen
    @Override public String currentScreenFieldName() { return "o"; } // czr (Screen) on cvi, @Nullable
    @Override public String ingameGuiFieldName() { return "t"; } // cwb (IngameGui) on cvi
    @Override public String chatLinesFieldName() { return "d"; } // List<cvd> allMessages on cwp (NewChatGui); cwp is field h on cwb
    @Override public String cursorItemFieldName() { return "g"; } // bca (ItemStack) on avw (PlayerInventory); mouse-held item
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "czr"; } // Screen class
    @Override public String sessionFieldName() { return "Z"; } // cvt (Session) on cvi
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on cvt (Session)
    @Override public String clickCooldownFieldName() { return "s"; } // protected int on cvi; leftClickCounter/attackCooldown
    @Override public String playerControllerFieldName() { return "f"; } // dhk (PlayerController) on cvi
    @Override public String digMethodName() { return "b"; } // dhk.b(ev, fa) onPlayerDamageBlock continuous dig
    @Override public String chatLineTextFieldName() { return "b"; } // jm (ITextComponent) on cvd (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "width"; } // int on czr (Screen); un-obfuscated in 1.14
    @Override public String guiScreenHeightFieldName() { return "height"; } // int on czr (Screen); un-obfuscated in 1.14
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on cvf (KeyBinding)
    @Override public String forwardKeyFieldName() { return "X"; } // cvf on cvm (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "Y"; } // cvf on cvm (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "Z"; } // cvf on cvm (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "aa"; } // cvf on cvm (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ab"; } // cvf on cvm (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "ac"; } // cvf on cvm (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "day"; } // CreativeInventoryScreen
    @Override public String guiInventoryClassName() { return "dbh"; } // InventoryScreen
    @Override public String closeContainerMethodName() { return "t_"; } // djv.t_() sends close packet + calls w()
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "f"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "dlc"; }
    @Override public String renderMethodName() { return "d"; } // Minecraft render method (takes boolean)
}
