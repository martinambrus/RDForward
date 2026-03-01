package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.17 (protocol v755).
 * Verified by CFR decompilation of the 1.17 client JAR.
 *
 * Minecraft = dvo, Entity = atf, LivingEntity = att, PlayerEntity = bkd,
 * AbstractClientPlayerEntity = emi, ClientPlayerEntity = eml,
 * GameSettings = dvs, PlayerInventory = bkc, ItemStack = bqp, Item = bql,
 * Block = bzp, Level = bwp, ClientLevel = ejh,
 * Screen = eap, IngameGui = dwl, ChatComponent = dxa, GuiMessage = dvj,
 * MultiPlayerGameMode = ejk, KeyMapping = dvl, Session = dwc,
 * MovementInput = emj, KeyboardInput = emk, Window = dpq,
 * MouseHandler = dvp, CreativeModeInventoryScreen = ebz, InventoryScreen = ebm.
 *
 * Key differences from V753/V754 (1.16.x):
 * - Complete repackage: all class names shifted significantly (Caves and Cliffs Part 1).
 * - Minecraft class: djw -> dvo. Run method stays e(). Tick method stays q(). Render method: e(boolean) -> f(boolean).
 * - Player field: s -> t (eml). World field: r -> s (ejh).
 * - PlayerController field: q -> r (ejk). GameSettings field: k -> l (dvs).
 * - IngameGui field: j -> k (dwl). Session field: V -> X (dwc).
 * - CurrentScreen field: y -> z (eap). ClickCooldown field: w -> x.
 * - Click/attack method: f(boolean) -> g(boolean).
 * - Entity position: xOld/yOld/zOld shifted from m/n/o to u/v/w on atf.
 * - Entity yaw/pitch: p/q (public float) -> ay/az (private float) on atf.
 * - Entity onGround: t -> z on atf.
 * - PlayerInventory field on PlayerEntity: bm -> co (private final) on bkd.
 * - PlayerInventory mainInventory: a -> h on bkc. selectedSlot: d -> k on bkc.
 * - ItemStack item field: h -> v (bql) on bqp. count field: f -> t on bqp.
 * - MovementInput field: f -> cy on eml (ClientPlayerEntity).
 * - Key bindings shifted: ad-ai -> an-as on dvs.
 * - KeyBinding pressed field: i -> p on dvl.
 * - sendChatMessage: f(String) -> e(String) on eml.
 * - closeContainer: m() -> n() on eml.
 * - dropPlayerItem: a(boolean) stays a(boolean) on eml.
 * - displayGuiScreen: a(eap) stays a on dvo.
 * - getBlockState: d_(fx) -> a_(gg) on bwp (Level); BlockPos renamed fx -> gg.
 * - digMethod: b(gg, gl) stays b on ejk.
 * - Screen width/height: k/l -> j/k on eap.
 * - ChatComponent (dxa) is now a separate field v on IngameGui (dwl).
 *   chatLinesFieldName = "e" (List<dvj<os>> allMessages on dxa).
 *   NOTE: Agent code needs two-hop access: dvo.k -> dwl.v -> dxa.e.
 *   ingameGuiFieldName points to dwl; chatLinesFieldName "e" resolves on dwl
 *   to a static ww, not the chat List. Chat reading requires agent code update.
 * - cursorItem moved from PlayerInventory to AbstractContainerMenu (bmj, field m).
 *   cursorItemFieldName = "m" but agent resolves on bkc (no carried field).
 *   Cursor item reading requires agent code update for 1.17+.
 * - chatLineTextFieldName = "b" (the T field on dvj<T>, generic message content).
 */
public class NettyReleaseV755Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "dvo"; } // default package, obfuscated
    @Override public String runMethodName() { return "e"; } // Main calls new dvo(...).e()
    @Override public String tickMethodName() { return "q"; } // dvo.q() game tick; decrements x
    @Override public String playerFieldName() { return "t"; } // eml (ClientPlayerEntity) on dvo
    @Override public String worldFieldName() { return "s"; } // ejh (ClientLevel) on dvo
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return null; } // LWJGL3: on Window (dpq)
    @Override public String displayHeightFieldName() { return null; } // LWJGL3: on Window (dpq)
    @Override public String posXFieldName() { return "u"; } // public double xOld on atf (Entity)
    @Override public String posYFieldName() { return "v"; } // public double yOld on atf (Entity)
    @Override public String posZFieldName() { return "w"; } // public double zOld on atf (Entity)
    @Override public String gameSettingsFieldName() { return "l"; } // dvs (Options) on dvo
    @Override public String movementInputFieldName() { return "cy"; } // emj (MovementInput) on eml
    @Override public String pressedKeysFieldName() { return null; } // KeyMapping-based
    @Override public String yawFieldName() { return "ay"; } // private float yRot on atf (Entity)
    @Override public String pitchFieldName() { return "az"; } // private float xRot on atf (Entity)
    @Override public String onGroundFieldName() { return "z"; } // protected boolean on atf (Entity)
    @Override public String inventoryFieldName() { return "co"; } // bkc (Inventory) on bkd (Player)
    @Override public String mainInventoryFieldName() { return "h"; } // gs<bqp> (NonNullList<ItemStack>, size 36) on bkc
    @Override public String currentItemFieldName() { return "k"; } // int selected on bkc (Inventory)
    @Override public String itemIdFieldName() { return "v"; } // bql (Item) on bqp (ItemStack); @Deprecated
    @Override public String stackSizeFieldName() { return "t"; } // int count on bqp (ItemStack)
    @Override public String getBlockIdMethodName() { return "a_"; } // bwp.a_(gg) returns cks (BlockState)
    @Override public String clickMethodName() { return "g"; } // dvo.g(boolean) continuous left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on dvp (MouseHandler); MouseHandler is field m on dvo
    @Override public String mouseHelperFieldName() { return "m"; } // dvp (MouseHandler) on dvo
    @Override public String sendChatMessageMethodName() { return "e"; } // eml.e(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "a"; } // eml.a(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // dvo.a(eap) sets current screen
    @Override public String currentScreenFieldName() { return "z"; } // eap (Screen) on dvo, @Nullable
    @Override public String ingameGuiFieldName() { return "k"; } // dwl (Gui) on dvo
    @Override public String chatLinesFieldName() { return "e"; } // List<dvj<os>> allMessages on dxa (ChatComponent); dxa is field v on dwl
    @Override public String cursorItemFieldName() { return "m"; } // bqp (ItemStack) carried on bmj (AbstractContainerMenu); bV on bkd
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "eap"; } // Screen class
    @Override public String sessionFieldName() { return "X"; } // dwc (Session) on dvo
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on dwc (Session)
    @Override public String clickCooldownFieldName() { return "x"; } // protected int on dvo; missTime/attackCooldown
    @Override public String playerControllerFieldName() { return "r"; } // ejk (MultiPlayerGameMode) on dvo
    @Override public String digMethodName() { return "b"; } // ejk.b(gg, gl) continueDestroyingBlock
    @Override public String chatLineTextFieldName() { return "b"; } // T (os/Component) on dvj (GuiMessage)
    @Override public String guiScreenWidthFieldName() { return "j"; } // int on eap (Screen)
    @Override public String guiScreenHeightFieldName() { return "k"; } // int on eap (Screen)
    @Override public String keyBindingPressedFieldName() { return "p"; } // boolean on dvl (KeyMapping)
    @Override public String forwardKeyFieldName() { return "an"; } // dvl on dvs (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "ao"; } // dvl on dvs (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "ap"; } // dvl on dvs (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "aq"; } // dvl on dvs (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ar"; } // dvl on dvs (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "as"; } // dvl on dvs (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "ebz"; } // CreativeModeInventoryScreen
    @Override public String guiInventoryClassName() { return "ebm"; } // InventoryScreen
    @Override public String closeContainerMethodName() { return "n"; } // eml.n() sends close window packet
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "h"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "eny"; }
    @Override public String renderMethodName() { return "f"; } // Minecraft render method f(boolean) on dvo; called from e() game loop
    @Override public String gameRendererClassName() { return "ena"; } // GameRenderer class; render method a(float, long, boolean)
}
