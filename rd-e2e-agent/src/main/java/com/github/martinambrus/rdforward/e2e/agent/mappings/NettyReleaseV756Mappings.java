package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.17.1 (protocol v756, Java 16+).
 * Verified by CFR decompilation of the 1.17.1 client JAR (protocol 756).
 *
 * Minecraft = dvp, Entity = atg, LivingEntity = atu, PlayerEntity = bke,
 * AbstractClientPlayerEntity = emj, ClientPlayerEntity = emm,
 * GameSettings = dvt, PlayerInventory = bkd, ItemStack = bqq, Item = bqm,
 * Block = bzp, World = bwq, ClientWorld = eji,
 * Screen = eaq, IngameGui = dwm, NewChatGui = dxb, ChatLine = dvk,
 * PlayerController = ejl, KeyBinding = dvm, Session = dwd,
 * MovementInput = emk, Window = dpr,
 * MouseHelper = dvn, CreativeInventoryScreen = eca, InventoryScreen = ecj,
 * AbstractContainerMenu = bmk, NonNullList = gs.
 *
 * Key differences from V735 (1.16):
 * - Minecraft class shifted: dlx -> dvp. Run method unchanged: e.
 * - Tick method unchanged: q.
 * - Player field unchanged: s (emm on dvp).
 * - World field unchanged: r (eji on dvp).
 * - PlayerController unchanged: q (ejl on dvp).
 * - IngameGui shifted: j -> k (dwm on dvp).
 * - GameSettings shifted: k -> l (dvt on dvp).
 * - Session shifted: V -> W (dwd on dvp).
 * - CurrentScreen unchanged: y (eaq on dvp).
 * - clickCooldown unchanged: w (protected int on dvp).
 * - Click method shifted: f(boolean) -> g(boolean).
 * - Entity position: xOld/yOld/zOld shifted from m/n/o to u/v/w on atg.
 * - Entity yaw/pitch shifted: p/q -> x/y on atg.
 * - Entity onGround shifted: t -> z on atg.
 * - PlayerInventory shifted: bt -> co (bkd on bke). Private field.
 * - PlayerInventory main items shifted: a -> h (gs<bqq> on bkd).
 * - PlayerInventory selectedSlot shifted: d -> k (int on bkd).
 * - ItemStack item shifted: h -> t (bqm on bqq). Count shifted: f -> r.
 * - getBlockState changed: d_ -> a_ on bwq (World).
 * - Screen width/height shifted: k/l -> j/k on eaq.
 * - Key bindings shifted: forward=an, left=ao, back=ap, right=aq, jump=ar, sneak=as.
 * - KeyBinding pressed shifted: i -> p on dvm.
 * - sendChatMessage shifted: f(String) -> e(String) on emm.
 * - dropPlayerItem unchanged: a(boolean) on emm.
 * - closeContainer shifted: m() -> n() on emm.
 * - movementInput shifted: f -> cy on emm.
 * - displayGuiScreen unchanged: a(eaq) on dvp.
 * - digMethod unchanged: b(gg, gl) on ejl.
 * - NewChatGui (dxb) is field v on IngameGui (dwm). ChatLines (e) are on dxb.
 * - ChatLine text unchanged: b on dvk.
 * - CursorItem: no longer on PlayerInventory; moved to AbstractContainerMenu (bmk) field m.
 *   cursorItemFieldName set to "m" but requires container access, not inventory access.
 */
public class NettyReleaseV756Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "dvp"; } // default package, obfuscated
    @Override public String runMethodName() { return "e"; } // Main calls new dvp(...).e()
    @Override public String tickMethodName() { return "q"; } // dvp.q() game tick; decrements w, ticks screens
    @Override public String playerFieldName() { return "s"; } // emm (ClientPlayerEntity) on dvp
    @Override public String worldFieldName() { return "r"; } // eji (ClientWorld) on dvp
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return null; } // LWJGL3: on Window (dpr), field O on dvp
    @Override public String displayHeightFieldName() { return null; } // LWJGL3: on Window (dpr), field O on dvp
    @Override public String posXFieldName() { return "u"; } // public double xOld on atg (Entity)
    @Override public String posYFieldName() { return "v"; } // public double yOld on atg (Entity)
    @Override public String posZFieldName() { return "w"; } // public double zOld on atg (Entity)
    @Override public String gameSettingsFieldName() { return "l"; } // dvt (GameSettings) on dvp
    @Override public String movementInputFieldName() { return "cy"; } // emk (MovementInput) on emm (ClientPlayerEntity)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "x"; } // float yRotO on atg (Entity)
    @Override public String pitchFieldName() { return "y"; } // float xRotO on atg (Entity)
    @Override public String onGroundFieldName() { return "z"; } // boolean onGround on atg (Entity)
    @Override public String inventoryFieldName() { return "co"; } // bkd (PlayerInventory) on bke (PlayerEntity); private
    @Override public String mainInventoryFieldName() { return "h"; } // gs<bqq> (NonNullList<ItemStack>, size 36) on bkd
    @Override public String currentItemFieldName() { return "k"; } // int selectedSlot on bkd (PlayerInventory)
    @Override public String itemIdFieldName() { return "t"; } // bqm (Item) on bqq (ItemStack); @Deprecated field
    @Override public String stackSizeFieldName() { return "r"; } // int count on bqq (ItemStack)
    @Override public String getBlockIdMethodName() { return "a_"; } // bwq.a_(gg) returns ckt (BlockState)
    @Override public String clickMethodName() { return "g"; } // dvp.g(boolean) continuous left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on dvn (MouseHandler); MouseHandler is field n on dvp
    @Override public String mouseHelperFieldName() { return "n"; } // dvn (MouseHandler) on dvp
    @Override public String sendChatMessageMethodName() { return "e"; } // emm.e(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "a"; } // emm.a(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // dvp.a(eaq) sets current screen
    @Override public String currentScreenFieldName() { return "y"; } // eaq (Screen) on dvp, @Nullable
    @Override public String ingameGuiFieldName() { return "k"; } // dwm (IngameGui) on dvp
    @Override public String chatLinesFieldName() { return "e"; } // List<dvk<os>> allMessages on dxb (NewChatGui); dxb is field v on dwm
    @Override public String cursorItemFieldName() { return "m"; } // bqq on bmk (AbstractContainerMenu); access via bke.bV.m
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "eaq"; } // Screen class
    @Override public String sessionFieldName() { return "W"; } // dwd (Session) on dvp
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on dwd (Session)
    @Override public String clickCooldownFieldName() { return "w"; } // protected int on dvp; leftClickCounter/attackCooldown
    @Override public String playerControllerFieldName() { return "q"; } // ejl (PlayerController) on dvp
    @Override public String digMethodName() { return "b"; } // ejl.b(gg, gl) onPlayerDamageBlock continuous dig
    @Override public String chatLineTextFieldName() { return "b"; } // os (Component) on dvk (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "j"; } // int width on eaq (Screen)
    @Override public String guiScreenHeightFieldName() { return "k"; } // int height on eaq (Screen)
    @Override public String keyBindingPressedFieldName() { return "p"; } // boolean on dvm (KeyBinding)
    @Override public String forwardKeyFieldName() { return "an"; } // dvm on dvt (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "ao"; } // dvm on dvt (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "ap"; } // dvm on dvt (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "aq"; } // dvm on dvt (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ar"; } // dvm on dvt (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "as"; } // dvm on dvt (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "eca"; } // CreativeInventoryScreen
    @Override public String guiInventoryClassName() { return "ecj"; } // InventoryScreen
    @Override public String closeContainerMethodName() { return "n"; } // emm.n() sends close window packet + calls r()
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "h"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "enz"; }
    @Override public String renderMethodName() { return "f"; } // Minecraft render method f(boolean) on dvp; called from e() game loop
    @Override public String gameRendererClassName() { return "enb"; } // GameRenderer class; render method a(float, long, boolean)
}
