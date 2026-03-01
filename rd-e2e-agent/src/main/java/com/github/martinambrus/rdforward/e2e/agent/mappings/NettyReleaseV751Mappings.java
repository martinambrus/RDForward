package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.16.2 (protocol v751).
 * Verified by CFR decompilation of the 1.16.2 client JAR (protocol 751).
 *
 * Minecraft = djw, Entity = apx, LivingEntity = aqj, PlayerEntity = bft,
 * AbstractClientPlayerEntity = dzb, ClientPlayerEntity = dze,
 * GameSettings = dka, PlayerInventory = bfs, ItemStack = bly, Item = blu,
 * Block = caj, World = bru, ClientWorld = dwl,
 * Screen = doq, IngameGui = dks, NewChatGui = dlh, ChatLine = djr,
 * PlayerController = dwo, KeyBinding = djt, Session = dkj,
 * MovementInput = dzc, KeyboardInput = dzd, Window = dew,
 * MouseHelper = djx, CreativeInventoryScreen = dpz, InventoryScreen = dqi.
 *
 * Key differences from V735 (1.16.0):
 * - Minecraft class obfuscated name changed from dcb to djw.
 * - Player field on Minecraft changed from r to s (dze).
 * - World field on Minecraft changed from q to r (dwl).
 * - PlayerController field on Minecraft changed from p to q (dwo).
 * - IngameGui field on Minecraft changed from i to j (dks).
 * - GameSettings field on Minecraft changed from j to k (dka).
 * - Session field on Minecraft changed from U to V (dkj).
 * - CurrentScreen field on Minecraft changed from x to y (doq).
 * - Run method changed from b() to e() (Main calls djw2.e()).
 * - Tick method changed from n() to q() on djw.
 * - Click/attack method remains f(boolean).
 * - ClickCooldown field changed from v to w on djw.
 * - Window field changed from M to N on djw.
 * - MouseHelper field changed from k to l (djx).
 * - Entity position now stored in Vec3d (private dck ai); using prevPos
 *   doubles m/n/o as accessible fallback.
 * - Screen width/height obfuscated as k/l (was "width"/"height" in 1.15).
 * - JoinGame rewritten: isHardcore separate boolean, maxPlayers VarInt,
 *   dimension NBT compound, registry format dimension codec.
 * - closeContainer changed from v_() to m() on ClientPlayerEntity.
 * - dropPlayerItem: a(boolean) on dze sends dig packet; parent bft has
 *   a(bly, boolean) for ItemStack drop.
 */
public class NettyReleaseV751Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "djw"; } // default package, obfuscated
    @Override public String runMethodName() { return "e"; } // Main calls new djw(...).e()
    @Override public String tickMethodName() { return "q"; } // djw.q() game tick; decrements w
    @Override public String playerFieldName() { return "s"; } // dze (ClientPlayerEntity) on djw
    @Override public String worldFieldName() { return "r"; } // dwl (ClientWorld) on djw
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "q"; } // int framebufferWidth on dew (Window); Window is field N on djw
    @Override public String displayHeightFieldName() { return "r"; } // int framebufferHeight on dew (Window); Window is field N on djw
    @Override public String displayObjectFieldName() { return "N"; } // dew (Window) on djw
    @Override public String posXFieldName() { return "m"; } // public double prevPosX on apx (Entity); actual pos in Vec3d ai
    @Override public String posYFieldName() { return "n"; } // public double prevPosY on apx (Entity); actual pos in Vec3d ai
    @Override public String posZFieldName() { return "o"; } // public double prevPosZ on apx (Entity); actual pos in Vec3d ai
    @Override public String gameSettingsFieldName() { return "k"; } // dka (GameSettings) on djw
    @Override public String movementInputFieldName() { return "f"; } // dzc (MovementInput) on dze (ClientPlayerEntity)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "p"; } // float on apx (Entity)
    @Override public String pitchFieldName() { return "q"; } // float on apx (Entity)
    @Override public String onGroundFieldName() { return "t"; } // boolean on apx (Entity)
    @Override public String inventoryFieldName() { return "bm"; } // bfs (PlayerInventory) on bft (PlayerEntity)
    @Override public String mainInventoryFieldName() { return "a"; } // gj<bly> (NonNullList<ItemStack>, size 36) on bfs
    @Override public String currentItemFieldName() { return "d"; } // int selectedSlot on bfs (PlayerInventory)
    @Override public String itemIdFieldName() { return "h"; } // blu (Item) on bly (ItemStack); @Deprecated field
    @Override public String stackSizeFieldName() { return "f"; } // int count on bly (ItemStack)
    @Override public String getBlockIdMethodName() { return "d_"; } // bru.d_(fx) returns cee (BlockState)
    @Override public String clickMethodName() { return "f"; } // djw.f(boolean) continuous left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on djx (MouseHelper); MouseHelper is field l on djw
    @Override public String mouseHelperFieldName() { return "l"; } // djx (MouseHelper) on djw
    @Override public String sendChatMessageMethodName() { return "f"; } // dze.f(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "a"; } // bft.a(bly, boolean) drops ItemStack; dze.a(boolean) sends dig packet
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
    @Override public String chatLineTextFieldName() { return "b"; } // nr (ITextComponent) on djr (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "k"; } // int on doq (Screen); obfuscated in 1.16.2
    @Override public String guiScreenHeightFieldName() { return "l"; } // int on doq (Screen); obfuscated in 1.16.2
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on djt (KeyBinding)
    @Override public String forwardKeyFieldName() { return "ad"; } // djt on dka (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "ae"; } // djt on dka (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "af"; } // djt on dka (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "ag"; } // djt on dka (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ah"; } // djt on dka (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "ai"; } // djt on dka (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "dpz"; } // CreativeInventoryScreen
    @Override public String guiInventoryClassName() { return "dqi"; } // InventoryScreen
    @Override public String closeContainerMethodName() { return "m"; } // dze.m() sends close packet + calls x()
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "g"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "eap"; }
    @Override public String renderMethodName() { return "e"; } // Minecraft render method (takes boolean)
}
