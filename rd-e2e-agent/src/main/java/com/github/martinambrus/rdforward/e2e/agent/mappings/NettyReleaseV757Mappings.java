package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.18 (protocol v757).
 * Verified by CFR decompilation of the 1.18 client JAR (protocol 757).
 *
 * Minecraft = dxo, Entity = awt, LivingEntity = axh, PlayerEntity = bnr,
 * AbstractClientPlayerEntity = eom, ClientPlayerEntity = eop,
 * GameSettings = dxs, PlayerInventory = bnq, ItemStack = bue, Item = bua,
 * Block = cac, World = cad, ClientWorld = ell,
 * Screen = ecr, IngameGui = dym, ChatComponent = dzb, ChatLine = dxj,
 * PlayerController = elo, KeyBinding = dxl, Session = dyd,
 * MovementInput = eon, KeyboardInput = eoo, Window = drp,
 * MouseHelper = dxp, CreativeInventoryScreen = eec, InventoryScreen = eel.
 *
 * Key differences from V751 (1.16.2):
 * - Minecraft class obfuscated name changed from djw to dxo.
 * - Player field on Minecraft changed from s to s (eop), same letter.
 * - World field on Minecraft changed from r to r (ell), same letter.
 * - PlayerController field on Minecraft changed from q to q (elo), same letter.
 * - IngameGui field on Minecraft changed from j to k (dym).
 * - GameSettings field on Minecraft changed from k to l (dxs).
 * - Session field on Minecraft changed from V to V (dyd), same letter.
 * - CurrentScreen field on Minecraft changed from y to y (ecr), same letter.
 * - Run method: e() on dxo. Main calls new dxo(...); then .e() for the run loop.
 * - Tick method: q() on dxo. Decrements aK, ticks screens and keybindings.
 * - Click/attack method: g(boolean) on dxo (was f(boolean) in V751).
 * - ClickCooldown field: w on dxo (protected int, same letter as V751).
 * - Window field: O on dxo (was N on djw).
 * - MouseHelper field: m on dxo (was l on djw).
 * - Entity position stored in Vec3 (private dom aw); using prevPos doubles
 *   u/v/w as accessible fallback. Yaw/pitch now private aA/aB on awt.
 * - Screen width/height: j/k on ecr (public int).
 * - Inventory field on PlayerEntity: private cp (was bm on bft in V751).
 * - closeContainer: w() on eop sends ContainerClose packet.
 * - sendChatMessage: e(String) on eop.
 * - dropPlayerItem: a(boolean) on eop sends dig packet for drop.
 * - movementInput: cz (eon) on eop (was f on dze in V751).
 * - ChatComponent (dzb) is field x on dym. Messages in field e on dzb.
 * - onGround changed from t to z on awt (Entity).
 * - Key bindings shifted: forward=as, left=at, back=au, right=av, jump=aw, sneak=ax.
 */
public class NettyReleaseV757Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "dxo"; } // default package, obfuscated
    @Override public String runMethodName() { return "e"; } // Main calls new dxo(...).e()
    @Override public String tickMethodName() { return "q"; } // dxo.q() game tick; decrements aK
    @Override public String playerFieldName() { return "s"; } // eop (ClientPlayerEntity) on dxo
    @Override public String worldFieldName() { return "r"; } // ell (ClientWorld) on dxo
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "q"; } // int framebufferWidth on drp (Window); Window is field O on dxo
    @Override public String displayHeightFieldName() { return "r"; } // int framebufferHeight on drp (Window); Window is field O on dxo
    @Override public String displayObjectFieldName() { return "O"; } // drp (Window) on dxo
    @Override public String posXFieldName() { return "u"; } // public double prevPosX on awt (Entity); actual pos in Vec3 dom aw
    @Override public String posYFieldName() { return "v"; } // public double prevPosY on awt (Entity); actual pos in Vec3 dom aw
    @Override public String posZFieldName() { return "w"; } // public double prevPosZ on awt (Entity); actual pos in Vec3 dom aw
    @Override public String gameSettingsFieldName() { return "l"; } // dxs (GameSettings) on dxo
    @Override public String movementInputFieldName() { return "cz"; } // eon (MovementInput) on eop (ClientPlayerEntity)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "aA"; } // private float on awt (Entity); accessed via dm()
    @Override public String pitchFieldName() { return "aB"; } // private float on awt (Entity); accessed via dn()
    @Override public String onGroundFieldName() { return "z"; } // protected boolean on awt (Entity)
    @Override public String inventoryFieldName() { return "cp"; } // bnq (PlayerInventory) on bnr (PlayerEntity)
    @Override public String mainInventoryFieldName() { return "h"; } // gt<bue> (NonNullList<ItemStack>, size 36) on bnq
    @Override public String currentItemFieldName() { return "k"; } // int selectedSlot on bnq (PlayerInventory)
    @Override public String itemIdFieldName() { return "t"; } // bua (Item) on bue (ItemStack); @Deprecated field
    @Override public String stackSizeFieldName() { return "r"; } // int count on bue (ItemStack)
    @Override public String getBlockIdMethodName() { return "a_"; } // cad.a_(gh) returns coc (BlockState)
    @Override public String clickMethodName() { return "g"; } // dxo.g(boolean) continuous left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on dxp (MouseHelper); MouseHelper is field m on dxo
    @Override public String mouseHelperFieldName() { return "m"; } // dxp (MouseHelper) on dxo
    @Override public String sendChatMessageMethodName() { return "e"; } // eop.e(String) sends chat packet vb
    @Override public String dropPlayerItemMethodName() { return "a"; } // eop.a(boolean) sends dig packet vv for drop
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // dxo.a(ecr) sets current screen
    @Override public String currentScreenFieldName() { return "y"; } // ecr (Screen) on dxo, @Nullable
    @Override public String ingameGuiFieldName() { return "k"; } // dym (IngameGui) on dxo
    @Override public String chatLinesFieldName() { return "e"; } // List<dxj<pz>> allMessages on dzb (ChatComponent); dzb is field x on dym
    @Override public String cursorItemFieldName() { return "m"; } // bue (ItemStack) carried on bpx (AbstractContainerMenu)
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "ecr"; } // Screen class
    @Override public String sessionFieldName() { return "V"; } // dyd (Session) on dxo
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on dyd (Session)
    @Override public String clickCooldownFieldName() { return "w"; } // protected int on dxo; leftClickCounter/attackCooldown
    @Override public String playerControllerFieldName() { return "q"; } // elo (PlayerController) on dxo
    @Override public String digMethodName() { return "b"; } // elo.b(gh, gm) onPlayerDamageBlock continuous dig
    @Override public String chatLineTextFieldName() { return "b"; } // pz (Component) on dxj (ChatLine/GuiMessage)
    @Override public String guiScreenWidthFieldName() { return "j"; } // int on ecr (Screen); public
    @Override public String guiScreenHeightFieldName() { return "k"; } // int on ecr (Screen); public
    @Override public String keyBindingPressedFieldName() { return "p"; } // private boolean on dxl (KeyBinding)
    @Override public String forwardKeyFieldName() { return "as"; } // dxl on dxs (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "at"; } // dxl on dxs (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "au"; } // dxl on dxs (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "av"; } // dxl on dxs (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "aw"; } // dxl on dxs (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "ax"; } // dxl on dxs (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "eec"; } // CreativeInventoryScreen
    @Override public String guiInventoryClassName() { return "eel"; } // InventoryScreen
    @Override public String closeContainerMethodName() { return "w"; } // eop.w() sends ContainerClose packet + calls super
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "h"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "eqc"; }
    @Override public String renderMethodName() { return "f"; } // Minecraft render method f(boolean) on dxo; called from e() game loop
    @Override public String gameRendererClassName() { return "epe"; } // GameRenderer class; render method a(float, long, boolean)
}
