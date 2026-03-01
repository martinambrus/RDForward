package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.18.2 (protocol v758).
 * Verified by CFR decompilation of the 1.18.2 client JAR.
 *
 * Minecraft = dyr, Entity = axk, LivingEntity = axy, PlayerEntity = boj,
 * AbstractClientPlayerEntity = ept, ClientPlayerEntity = epw,
 * GameSettings/Options = dyv, PlayerInventory = boi, ItemStack = buw, Item = bus,
 * Block = cdq, Level/World = cav, ClientWorld = ems,
 * Screen = edw, Gui/IngameGui = dzq, ChatComponent = eaf, ChatLine = dym,
 * MultiPlayerGameMode = emv, KeyMapping = dyo, User/Session = dzh,
 * MovementInput = epu, Window = dsr,
 * MouseHandler = dys, KeyboardHandler = dyp,
 * CreativeInventoryScreen = efh, InventoryScreen = efq,
 * AbstractContainerMenu = bqp, InventoryMenu = bro.
 *
 * Key differences from V751 (1.16.2):
 * - Minecraft class shifted: djw -> dyr.
 * - Entity pos fields: In 1.18.2, current position stored in private Vec3d (dpo aw).
 *   Using xOld/yOld/zOld doubles t/u/v as accessible fallback (set from current pos at tick).
 * - Entity yaw/pitch now private: aA (yRot), aB (xRot) on axk. Resolved via setAccessible.
 * - Entity onGround: y (boolean) on axk.
 * - PlayerInventory shifted: co on boj (was bm on bft).
 * - PlayerInventory items: h (gx<buw>, size 36), selectedSlot: k.
 * - ItemStack: item field t (bus), count field r.
 * - GameSettings shifted: l on dyr (was k on djw).
 * - PlayerController shifted: q on dyr (emv).
 * - IngameGui shifted: k on dyr (dzq). ChatComponent is field x on dzq.
 * - Session shifted: W on dyr (dzh). Username: a on dzh.
 * - CurrentScreen: y on dyr (edw).
 * - Click/attack method changed: g(boolean) on dyr (was f(boolean)).
 * - ClickCooldown unchanged: w on dyr.
 * - Run method unchanged: e on dyr.
 * - Tick method unchanged: q on dyr.
 * - getBlockId: a_ on cav (was d_ on bru).
 * - sendChatMessage changed: e(String) on epw (was f(String)).
 * - dropPlayerItem unchanged: a(boolean) on epw.
 * - closeContainer unchanged: q() on epw.
 * - displayGuiScreen unchanged: a(edw) on dyr.
 * - Key bindings shifted: forward=at, left=au, back=av, right=aw, jump=ax, sneak=ay on dyv.
 * - KeyMapping pressed field: p (private boolean) on dyo.
 * - Screen width/height: j/k on edw.
 * - MouseHandler is field m on dyr (dys). mouseGrabbed: r (boolean) on dys.
 * - CursorItem: now on AbstractContainerMenu (bqp) field n, not PlayerInventory.
 *   Agent resolves from inventory object; may need adaptation for 1.18.2.
 * - ChatLines: e (List<dym<qk>>) on eaf (ChatComponent); eaf is field x on dzq.
 *   Agent resolves from ingameGui; chat is 2 levels deep (gui -> chatComponent -> lines).
 */
public class NettyReleaseV758Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "dyr"; } // default package, obfuscated
    @Override public String runMethodName() { return "e"; } // Main calls new dyr(...).e()
    @Override public String tickMethodName() { return "q"; } // dyr.q() game tick; decrements w
    @Override public String playerFieldName() { return "s"; } // epw (ClientPlayerEntity) on dyr
    @Override public String worldFieldName() { return "r"; } // ems (ClientWorld) on dyr
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return null; } // LWJGL3: on Window (dsr), not direct field
    @Override public String displayHeightFieldName() { return null; } // LWJGL3: on Window (dsr), not direct field
    @Override public String posXFieldName() { return "t"; } // public double xOld on axk (Entity); actual pos in Vec3d aw
    @Override public String posYFieldName() { return "u"; } // public double yOld on axk (Entity); actual pos in Vec3d aw
    @Override public String posZFieldName() { return "v"; } // public double zOld on axk (Entity); actual pos in Vec3d aw
    @Override public String gameSettingsFieldName() { return "l"; } // dyv (Options) on dyr
    @Override public String movementInputFieldName() { return "cy"; } // epu (MovementInput) on epw (ClientPlayerEntity)
    @Override public String pressedKeysFieldName() { return null; } // KeyMapping-based
    @Override public String yawFieldName() { return "aA"; } // private float yRot on axk (Entity); resolved via setAccessible
    @Override public String pitchFieldName() { return "aB"; } // private float xRot on axk (Entity); resolved via setAccessible
    @Override public String onGroundFieldName() { return "y"; } // protected boolean on axk (Entity)
    @Override public String inventoryFieldName() { return "co"; } // boi (Inventory) on boj (PlayerEntity); private final
    @Override public String mainInventoryFieldName() { return "h"; } // gx<buw> (NonNullList<ItemStack>, size 36) on boi
    @Override public String currentItemFieldName() { return "k"; } // int selectedSlot on boi (Inventory)
    @Override public String itemIdFieldName() { return "t"; } // bus (Item) on buw (ItemStack); @Deprecated field
    @Override public String stackSizeFieldName() { return "r"; } // int count on buw (ItemStack)
    @Override public String getBlockIdMethodName() { return "a_"; } // cav.a_(gj) returns cov (BlockState)
    @Override public String clickMethodName() { return "g"; } // dyr.g(boolean) continueAttack/left-click
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on dys (MouseHandler); dys is field m on dyr
    @Override public String mouseHelperFieldName() { return "m"; } // dys (MouseHandler) on dyr
    @Override public String sendChatMessageMethodName() { return "e"; } // epw.e(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "a"; } // epw.a(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // dyr.a(edw) sets current screen
    @Override public String currentScreenFieldName() { return "y"; } // edw (Screen) on dyr, @Nullable
    @Override public String ingameGuiFieldName() { return "k"; } // dzq (Gui) on dyr
    @Override public String chatLinesFieldName() { return "e"; } // List<dym<qk>> allMessages on eaf (ChatComponent); eaf is field x on dzq
    @Override public String cursorItemFieldName() { return "n"; } // buw carried on bqp (AbstractContainerMenu); NOT on PlayerInventory
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "edw"; } // Screen class
    @Override public String sessionFieldName() { return "W"; } // dzh (User/Session) on dyr
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on dzh (User/Session)
    @Override public String clickCooldownFieldName() { return "w"; } // protected int on dyr; attackCooldown
    @Override public String playerControllerFieldName() { return "q"; } // emv (MultiPlayerGameMode) on dyr
    @Override public String digMethodName() { return "b"; } // emv.b(gj, go) onPlayerDamageBlock continuous dig
    @Override public String chatLineTextFieldName() { return "b"; } // T (generic text) on dym (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "j"; } // int width on edw (Screen)
    @Override public String guiScreenHeightFieldName() { return "k"; } // int height on edw (Screen)
    @Override public String keyBindingPressedFieldName() { return "p"; } // private boolean on dyo (KeyMapping)
    @Override public String forwardKeyFieldName() { return "at"; } // dyo on dyv (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "au"; } // dyo on dyv (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "av"; } // dyo on dyv (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "aw"; } // dyo on dyv (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ax"; } // dyo on dyv (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "ay"; } // dyo on dyv (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "efh"; } // CreativeInventoryScreen
    @Override public String guiInventoryClassName() { return "efq"; } // InventoryScreen
    @Override public String closeContainerMethodName() { return "q"; } // epw.q() sends close window packet + calls s()
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "h"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "erj"; }
    @Override public String renderMethodName() { return "f"; } // Minecraft render method f(boolean) on dyr; called from e() game loop
    @Override public String gameRendererClassName() { return "eql"; } // GameRenderer class; render method a(float, long, boolean)
}
