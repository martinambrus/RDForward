package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.16.3 (protocol v753).
 * Verified by CFR decompilation of the 1.16.3 client JAR.
 *
 * Minecraft = djw, Entity = apx, LivingEntity = aqj, PlayerEntity = bft,
 * AbstractClientPlayerEntity = dzb, ClientPlayerEntity = dze,
 * GameSettings = dka, PlayerInventory = bfs, ItemStack = bly, Item = blu,
 * Block = bul, World = bru, ClientWorld = dwl,
 * Screen = doq, IngameGui = dks, NewChatGui = dlh, ChatLine = djr,
 * PlayerController = dwo, KeyBinding = djt, Session = dkj,
 * MovementInput = dzc, KeyboardInput = dzd, Window = dew,
 * MouseHelper = djx, CreativeInventoryScreen = dpz, InventoryScreen = dqi.
 *
 * Key differences from V578 (1.15.2):
 * - Minecraft class shifted: dbn -> djw.
 * - runMethodName changed: d -> e (Main calls djw2.e()).
 * - tickMethodName changed: p -> q.
 * - playerFieldName changed: r -> s (dze). worldFieldName changed: q -> r (dwl).
 * - gameSettingsFieldName changed: j -> k (dka). ingameGuiFieldName changed: i -> j (dks).
 * - playerControllerFieldName changed: p -> q (dwo). sessionFieldName changed: U -> V (dkj).
 * - currentScreenFieldName changed: x -> y (doq). clickCooldownFieldName changed: v -> w.
 * - clickMethodName changed: e -> f. Window field on Minecraft: M -> N (dew).
 * - closeContainerMethodName changed: v_ -> m (on dze, no longer uses SRG-style name).
 * - dropPlayerItemMethodName changed: n -> a (on dze).
 * - Screen width/height changed from un-obfuscated to obfuscated: width -> k, height -> l.
 * - Key bindings shifted: forward=ad, left=ae, back=af, right=ag, jump=ah, sneak=ai.
 * - inventoryFieldName changed: bu -> bm (on bft/PlayerEntity).
 * - NewChatGui field on IngameGui changed: h -> l. chatLinesFieldName still d on dlh.
 * - ItemStack item field changed: f -> h (blu). stackSize field changed: d -> f.
 * - cursorItemFieldName changed: g -> g (still g on bfs/PlayerInventory).
 */
public class NettyReleaseV753Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "djw"; } // default package, obfuscated
    @Override public String runMethodName() { return "e"; } // Main calls new djw(...).e()
    @Override public String tickMethodName() { return "q"; } // djw.q() game tick; decrements w, ticks screens
    @Override public String playerFieldName() { return "s"; } // dze (ClientPlayerEntity) on djw
    @Override public String worldFieldName() { return "r"; } // dwl (ClientWorld) on djw
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return null; } // LWJGL3: on Window (dew), field N on djw
    @Override public String displayHeightFieldName() { return null; } // LWJGL3: on Window (dew), field N on djw
    @Override public String posXFieldName() { return "m"; } // double on apx (Entity); xOld, set by moveTo
    @Override public String posYFieldName() { return "n"; } // double on apx (Entity); yOld, set by moveTo
    @Override public String posZFieldName() { return "o"; } // double on apx (Entity); zOld, set by moveTo
    @Override public String gameSettingsFieldName() { return "k"; } // dka (GameSettings) on djw
    @Override public String movementInputFieldName() { return "f"; } // dzc (MovementInput) on dze (ClientPlayerEntity)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "p"; } // float on apx (Entity)
    @Override public String pitchFieldName() { return "q"; } // float on apx (Entity)
    @Override public String onGroundFieldName() { return "t"; } // boolean on apx (Entity)
    @Override public String inventoryFieldName() { return "bm"; } // bfs (PlayerInventory) on bft (PlayerEntity)
    @Override public String mainInventoryFieldName() { return "a"; } // gj<bly> (NonNullList<ItemStack>, size 36) on bfs
    @Override public String currentItemFieldName() { return "d"; } // int selectedSlot on bfs (PlayerInventory)
    @Override public String itemIdFieldName() { return "h"; } // blu (Item) on bly (ItemStack); deprecated field
    @Override public String stackSizeFieldName() { return "f"; } // int count on bly (ItemStack)
    @Override public String getBlockIdMethodName() { return "d_"; } // bru.d_(fx) returns cee (BlockState)
    @Override public String clickMethodName() { return "f"; } // djw.f(boolean) continuous left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on djx (MouseHelper); MouseHelper is field l on djw
    @Override public String mouseHelperFieldName() { return "l"; } // djx (MouseHelper) on djw
    @Override public String sendChatMessageMethodName() { return "f"; } // dze.f(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "a"; } // dze.a(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // djw.a(doq) sets current screen
    @Override public String currentScreenFieldName() { return "y"; } // doq (Screen) on djw, @Nullable
    @Override public String ingameGuiFieldName() { return "j"; } // dks (IngameGui) on djw
    @Override public String chatLinesFieldName() { return "d"; } // List<djr<nr>> allMessages on dlh (NewChatGui); dlh is field l on dks
    @Override public String cursorItemFieldName() { return "g"; } // bly (ItemStack) on bfs (PlayerInventory); mouse-held item
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "doq"; } // Screen class
    @Override public String sessionFieldName() { return "V"; } // dkj (Session) on djw
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on dkj (Session)
    @Override public String clickCooldownFieldName() { return "w"; } // protected int on djw; leftClickCounter/attackCooldown
    @Override public String playerControllerFieldName() { return "q"; } // dwo (PlayerController) on djw
    @Override public String digMethodName() { return "b"; } // dwo.b(fx, gc) onPlayerDamageBlock continuous dig
    @Override public String chatLineTextFieldName() { return "b"; } // T (nr/ITextComponent) on djr (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "k"; } // int on doq (Screen); obfuscated in 1.16.3
    @Override public String guiScreenHeightFieldName() { return "l"; } // int on doq (Screen); obfuscated in 1.16.3
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on djt (KeyBinding)
    @Override public String forwardKeyFieldName() { return "ad"; } // djt on dka (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "ae"; } // djt on dka (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "af"; } // djt on dka (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "ag"; } // djt on dka (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ah"; } // djt on dka (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "ai"; } // djt on dka (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "dpz"; } // CreativeInventoryScreen
    @Override public String guiInventoryClassName() { return "dqi"; } // InventoryScreen
    @Override public String closeContainerMethodName() { return "m"; } // dze.m() sends close window packet + calls x()
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "g"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "eap"; }
    @Override public String renderMethodName() { return "e"; } // Minecraft render method (takes boolean)
}
