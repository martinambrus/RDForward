package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.15 (protocol v573).
 * Verified by CFR decompilation of the 1.15 client JAR (protocol 573).
 *
 * Minecraft = dbl, Entity = akn, LivingEntity = akw, PlayerEntity = ayg,
 * AbstractClientPlayerEntity = dpv, ClientPlayerEntity = dpy,
 * GameSettings = dbp, PlayerInventory = ayf, ItemStack = bek, Item = bef,
 * Block = byi, World = bjt, ClientWorld = dnl,
 * Screen = dfz, IngameGui = dcg, NewChatGui = dcu, ChatLine = dbg,
 * PlayerController = dno, KeyBinding = dbi, Session = dbx,
 * MovementInput = dpw, KeyboardInput = dpx, Window = cxu,
 * MouseHelper = dbm, CreativeInventoryScreen = dhg, InventoryScreen = dhp.
 *
 * Key differences from V477 (1.14):
 * - Minecraft class obfuscated name changed from cvi to dbl.
 * - Player field on Minecraft changed from j to r (dpy).
 * - World field on Minecraft changed from h to q (dnl).
 * - PlayerController field on Minecraft changed from f to p (dno).
 * - IngameGui field on Minecraft changed from t to i (dcg).
 * - GameSettings field on Minecraft changed from w to j (dbp).
 * - Session field on Minecraft changed from Z to U (dbx).
 * - CurrentScreen field on Minecraft changed from o to x (dfz).
 * - Window framebuffer fields shifted: width q, height r (was r, s on cub).
 * - PlayerInventory field on PlayerEntity changed from bx to bu.
 * - Entity position stored in private fields aq/ar/as (accessed via ct()/cu()/cx()).
 * - Entity yaw/pitch shifted from s/t to p/q; onGround shifted from w to t.
 * - getBlockState on World changed from e_ to d_.
 * - closeContainer changed from t_() to v_() on ClientPlayerEntity.
 * - clickCooldown changed from s to v on Minecraft.
 *
 * Obfuscation is identical to V575 (1.15.1) - same class/field/method names.
 */
public class NettyReleaseV573Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "dbl"; } // default package, obfuscated
    @Override public String runMethodName() { return "b"; } // Main calls new dbl(...).b()
    @Override public String tickMethodName() { return "n"; } // dbl.n() game tick; decrements v, ticks screens
    @Override public String playerFieldName() { return "r"; } // dpy (ClientPlayerEntity) on dbl
    @Override public String worldFieldName() { return "q"; } // dnl (ClientWorld) on dbl
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "q"; } // int framebufferWidth on cxu (Window); Window is field M on dbl
    @Override public String displayHeightFieldName() { return "r"; } // int framebufferHeight on cxu (Window); Window is field M on dbl
    @Override public String displayObjectFieldName() { return "M"; } // cxu (Window) on dbl
    @Override public String posXFieldName() { return "aq"; } // private double on akn (Entity); accessed via ct()
    @Override public String posYFieldName() { return "ar"; } // private double on akn (Entity); accessed via cu()
    @Override public String posZFieldName() { return "as"; } // private double on akn (Entity); accessed via cx()
    @Override public String gameSettingsFieldName() { return "j"; } // dbp (GameSettings) on dbl
    @Override public String movementInputFieldName() { return "f"; } // dpw (MovementInput) on dpy (ClientPlayerEntity)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "p"; } // float on akn (Entity)
    @Override public String pitchFieldName() { return "q"; } // float on akn (Entity)
    @Override public String onGroundFieldName() { return "t"; } // boolean on akn (Entity)
    @Override public String inventoryFieldName() { return "bu"; } // ayf (PlayerInventory) on ayg (PlayerEntity)
    @Override public String mainInventoryFieldName() { return "a"; } // fy<bek> (NonNullList<ItemStack>, size 36) on ayf
    @Override public String currentItemFieldName() { return "d"; } // int selectedSlot on ayf (PlayerInventory)
    @Override public String itemIdFieldName() { return "f"; } // bef (Item) on bek (ItemStack); deprecated field
    @Override public String stackSizeFieldName() { return "d"; } // int count on bek (ItemStack)
    @Override public String getBlockIdMethodName() { return "d_"; } // bjt.d_(fk) returns byg (BlockState)
    @Override public String clickMethodName() { return "e"; } // dbl.e(boolean) continuous left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on dbm (MouseHelper); MouseHelper is field k on dbl
    @Override public String mouseHelperFieldName() { return "k"; } // dbm (MouseHelper) on dbl
    @Override public String sendChatMessageMethodName() { return "f"; } // dpy.f(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "n"; } // dpy.n(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses n(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // dbl.a(dfz) sets current screen
    @Override public String currentScreenFieldName() { return "x"; } // dfz (Screen) on dbl, @Nullable
    @Override public String ingameGuiFieldName() { return "i"; } // dcg (IngameGui) on dbl
    @Override public String chatLinesFieldName() { return "d"; } // List<dbg> allMessages on dcu (NewChatGui); dcu is field h on dcg
    @Override public String cursorItemFieldName() { return "g"; } // bek (ItemStack) on ayf (PlayerInventory); mouse-held item
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "dfz"; } // Screen class
    @Override public String sessionFieldName() { return "U"; } // dbx (Session) on dbl
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on dbx (Session)
    @Override public String clickCooldownFieldName() { return "v"; } // protected int on dbl; leftClickCounter/attackCooldown
    @Override public String playerControllerFieldName() { return "p"; } // dno (PlayerController) on dbl
    @Override public String digMethodName() { return "b"; } // dno.b(fk, fp) onPlayerDamageBlock continuous dig
    @Override public String chatLineTextFieldName() { return "b"; } // lf (ITextComponent) on dbg (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "width"; } // int on dfz (Screen); un-obfuscated in 1.15
    @Override public String guiScreenHeightFieldName() { return "height"; } // int on dfz (Screen); un-obfuscated in 1.15
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on dbi (KeyBinding)
    @Override public String forwardKeyFieldName() { return "Z"; } // dbi on dbp (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "aa"; } // dbi on dbp (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "ab"; } // dbi on dbp (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "ac"; } // dbi on dbp (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ad"; } // dbi on dbp (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "ae"; } // dbi on dbp (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "dhg"; } // CreativeInventoryScreen
    @Override public String guiInventoryClassName() { return "dhp"; } // InventoryScreen
    @Override public String closeContainerMethodName() { return "v_"; } // dpy.v_() sends close packet + calls w()
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "f"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "drh"; }
    @Override public String renderMethodName() { return "d"; } // Minecraft render method (takes boolean)
}
