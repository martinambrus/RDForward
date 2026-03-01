package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.15.2 (protocol v578).
 * Verified by CFR decompilation of the 1.15.2 client JAR.
 *
 * Minecraft = dbn, Entity = akq, LivingEntity = akz, PlayerEntity = ayj,
 * AbstractClientPlayerEntity = dpy, ClientPlayerEntity = dqb,
 * GameSettings = dbr, PlayerInventory = ayi, ItemStack = ben, Item = bei,
 * Block = bph, World = bjw, ClientWorld = dno,
 * Screen = dgb, IngameGui = dci, NewChatGui = dcw, ChatLine = dbi,
 * PlayerController = dnr, KeyBinding = dbk, Session = dbz,
 * MovementInput = dpz, KeyboardInput = dqa, Window = cxx,
 * MouseHelper = dbo, CreativeInventoryScreen = dhi, InventoryScreen = dhr.
 *
 * Key differences from V485 (1.14.2):
 * - Minecraft class shifted: cvk -> dbn.
 * - runMethodName changed: b -> d (Main calls dbn2.d()).
 * - tickMethodName changed: m -> p.
 * - Entity position shifted: posX/Y/Z = m/n/o (was p/q/r), yaw/pitch = p/q (was s/t),
 *   onGround = t (was w).
 * - playerFieldName changed: j -> r. worldFieldName changed: h -> q.
 * - gameSettingsFieldName changed: w -> j. currentScreenFieldName changed: o -> x.
 * - ingameGuiFieldName changed: t -> i. playerControllerFieldName changed: f -> p.
 * - sessionFieldName changed: Z -> U. clickCooldownFieldName changed: s -> v.
 * - inventoryFieldName changed: bx -> bu (on ayj/PlayerEntity).
 * - getBlockIdMethodName changed: e_ -> d_ (on bjw/World).
 * - clickMethodName changed: f -> e.
 * - closeContainerMethodName changed: t_ -> v_ (on dqb/ClientPlayerEntity).
 * - Key bindings shifted: forward=aa, left=ab, back=ac, right=ad, jump=ae, sneak=af.
 * - displayGuiScreen still a(dgb). displayWidth/Height null (LWJGL3 Window).
 */
public class NettyReleaseV578Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "dbn"; } // default package, obfuscated
    @Override public String runMethodName() { return "d"; } // Main calls new dbn(...).d()
    @Override public String tickMethodName() { return "p"; } // dbn.p() game tick; decrements v, ticks screens
    @Override public String playerFieldName() { return "r"; } // dqb (ClientPlayerEntity) on dbn
    @Override public String worldFieldName() { return "q"; } // dno (ClientWorld) on dbn
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return null; } // LWJGL3: on Window (cxx), field M on dbn
    @Override public String displayHeightFieldName() { return null; } // LWJGL3: on Window (cxx), field M on dbn
    @Override public String posXFieldName() { return "m"; } // double on akq (Entity)
    @Override public String posYFieldName() { return "n"; } // double on akq (Entity)
    @Override public String posZFieldName() { return "o"; } // double on akq (Entity)
    @Override public String gameSettingsFieldName() { return "j"; } // dbr (GameSettings) on dbn
    @Override public String movementInputFieldName() { return "f"; } // dpz (MovementInput) on dqb (ClientPlayerEntity)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "p"; } // float on akq (Entity)
    @Override public String pitchFieldName() { return "q"; } // float on akq (Entity)
    @Override public String onGroundFieldName() { return "t"; } // boolean on akq (Entity)
    @Override public String inventoryFieldName() { return "bu"; } // ayi (PlayerInventory) on ayj (PlayerEntity)
    @Override public String mainInventoryFieldName() { return "a"; } // fy<ben> (NonNullList<ItemStack>, size 36) on ayi
    @Override public String currentItemFieldName() { return "d"; } // int selectedSlot on ayi (PlayerInventory)
    @Override public String itemIdFieldName() { return "f"; } // bei (Item) on ben (ItemStack); deprecated field
    @Override public String stackSizeFieldName() { return "d"; } // int count on ben (ItemStack)
    @Override public String getBlockIdMethodName() { return "d_"; } // bjw.d_(fk) returns byj (BlockState)
    @Override public String clickMethodName() { return "e"; } // dbn.e(boolean) continuous left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on dbo (MouseHelper); MouseHelper is field k on dbn
    @Override public String mouseHelperFieldName() { return "k"; } // dbo (MouseHelper) on dbn
    @Override public String sendChatMessageMethodName() { return "f"; } // dqb.f(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "n"; } // dqb.n(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses n(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // dbn.a(dgb) sets current screen
    @Override public String currentScreenFieldName() { return "x"; } // dgb (Screen) on dbn, @Nullable
    @Override public String ingameGuiFieldName() { return "i"; } // dci (IngameGui) on dbn
    @Override public String chatLinesFieldName() { return "d"; } // List<dbi> allMessages on dcw (NewChatGui); dcw is field h on dci
    @Override public String cursorItemFieldName() { return "g"; } // ben (ItemStack) on ayi (PlayerInventory); mouse-held item
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "dgb"; } // Screen class
    @Override public String sessionFieldName() { return "U"; } // dbz (Session) on dbn
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on dbz (Session)
    @Override public String clickCooldownFieldName() { return "v"; } // protected int on dbn; leftClickCounter/attackCooldown
    @Override public String playerControllerFieldName() { return "p"; } // dnr (PlayerController) on dbn
    @Override public String digMethodName() { return "b"; } // dnr.b(fk, fp) onPlayerDamageBlock continuous dig
    @Override public String chatLineTextFieldName() { return "b"; } // lf (ITextComponent) on dbi (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "width"; } // int on dgb (Screen); un-obfuscated in 1.15.2
    @Override public String guiScreenHeightFieldName() { return "height"; } // int on dgb (Screen); un-obfuscated in 1.15.2
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on dbk (KeyBinding)
    @Override public String forwardKeyFieldName() { return "aa"; } // dbk on dbr (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "ab"; } // dbk on dbr (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "ac"; } // dbk on dbr (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "ad"; } // dbk on dbr (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ae"; } // dbk on dbr (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "af"; } // dbk on dbr (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "dhi"; } // CreativeInventoryScreen
    @Override public String guiInventoryClassName() { return "dhr"; } // InventoryScreen
    @Override public String closeContainerMethodName() { return "v_"; } // dqb.v_() sends close window packet + calls w()
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "f"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "drk"; }
    @Override public String renderMethodName() { return "d"; } // Minecraft render method (takes boolean)
}
