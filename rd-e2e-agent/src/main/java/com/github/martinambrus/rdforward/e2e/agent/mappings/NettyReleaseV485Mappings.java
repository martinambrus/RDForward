package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.14.2 (protocol v485).
 * Verified by CFR decompilation of the 1.14.2 client JAR.
 *
 * Minecraft = cvk, Entity = aii, LivingEntity = air, PlayerEntity = avy,
 * AbstractClientPlayerEntity = dju, ClientPlayerEntity = djx,
 * GameSettings = cvo, PlayerInventory = avx, ItemStack = bcb, Item = bbw,
 * Block = bml, World = bhi, ClientWorld = dhn,
 * Screen = czt, IngameGui = cwd, NewChatGui = cwr, ChatLine = cvf,
 * PlayerController = dhm, KeyBinding = cvh, Session = cvv,
 * MovementInput = djv, KeyboardInput = djw, Window = cud,
 * MouseHelper = cvl, CreativeInventoryScreen = dba, InventoryScreen = dbj.
 *
 * Key differences from V477 (1.14):
 * - Most class names shifted by +2 (cvi->cvk, czr->czt, cwb->cwd, etc.).
 * - tickMethodName changed: n -> m.
 * - clickMethodName changed: e -> f.
 * - PlayerController: dhk -> dhm, field on Minecraft still f.
 * - All field names on respective classes remain the same as V477.
 */
public class NettyReleaseV485Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "cvk"; } // default package, obfuscated
    @Override public String runMethodName() { return "b"; } // Main calls new cvk(...).b()
    @Override public String tickMethodName() { return "m"; } // cvk.m() game tick; called from e() loop
    @Override public String playerFieldName() { return "j"; } // djx (ClientPlayerEntity) on cvk
    @Override public String worldFieldName() { return "h"; } // dhn (ClientWorld) on cvk
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "r"; } // int framebufferWidth on cud (Window); Window is field g on cvk
    @Override public String displayHeightFieldName() { return "s"; } // int framebufferHeight on cud (Window); Window is field g on cvk
    @Override public String displayObjectFieldName() { return "g"; } // cud (Window) on cvk
    @Override public String posXFieldName() { return "p"; } // double on aii (Entity)
    @Override public String posYFieldName() { return "q"; } // double on aii (Entity)
    @Override public String posZFieldName() { return "r"; } // double on aii (Entity)
    @Override public String gameSettingsFieldName() { return "w"; } // cvo (GameSettings) on cvk
    @Override public String movementInputFieldName() { return "f"; } // djv (MovementInput) on djx (ClientPlayerEntity)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "s"; } // float on aii (Entity)
    @Override public String pitchFieldName() { return "t"; } // float on aii (Entity)
    @Override public String onGroundFieldName() { return "w"; } // boolean on aii (Entity)
    @Override public String inventoryFieldName() { return "bx"; } // avx (PlayerInventory) on avy (PlayerEntity)
    @Override public String mainInventoryFieldName() { return "a"; } // fj<bcb> (NonNullList<ItemStack>, size 36) on avx
    @Override public String currentItemFieldName() { return "d"; } // int selectedSlot on avx (PlayerInventory)
    @Override public String itemIdFieldName() { return "f"; } // bbw (Item) on bcb (ItemStack); deprecated field
    @Override public String stackSizeFieldName() { return "d"; } // int count on bcb (ItemStack)
    @Override public String getBlockIdMethodName() { return "e_"; } // bhi.e_(ev) returns bvk (BlockState)
    @Override public String clickMethodName() { return "f"; } // cvk.f(boolean) continuous left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on cvl (MouseHelper); MouseHelper is field x on cvk
    @Override public String mouseHelperFieldName() { return "x"; } // cvl (MouseHelper) on cvk
    @Override public String sendChatMessageMethodName() { return "f"; } // djx.f(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "n"; } // djx.n(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses n(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // cvk.a(czt) sets current screen
    @Override public String currentScreenFieldName() { return "o"; } // czt (Screen) on cvk, @Nullable
    @Override public String ingameGuiFieldName() { return "t"; } // cwd (IngameGui) on cvk
    @Override public String chatLinesFieldName() { return "d"; } // List<cvf> allMessages on cwr (NewChatGui); cwr is field h on cwd
    @Override public String cursorItemFieldName() { return "g"; } // bcb (ItemStack) on avx (PlayerInventory); mouse-held item
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "czt"; } // Screen class
    @Override public String sessionFieldName() { return "Z"; } // cvv (Session) on cvk
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on cvv (Session)
    @Override public String clickCooldownFieldName() { return "s"; } // protected int on cvk; leftClickCounter/attackCooldown
    @Override public String playerControllerFieldName() { return "f"; } // dhm (PlayerController) on cvk
    @Override public String digMethodName() { return "b"; } // dhm.b(ev, fa) onPlayerDamageBlock continuous dig
    @Override public String chatLineTextFieldName() { return "b"; } // jm (ITextComponent) on cvf (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "width"; } // int on czt (Screen); un-obfuscated in 1.14.2
    @Override public String guiScreenHeightFieldName() { return "height"; } // int on czt (Screen); un-obfuscated in 1.14.2
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on cvh (KeyBinding)
    @Override public String forwardKeyFieldName() { return "X"; } // cvh on cvo (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "Y"; } // cvh on cvo (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "Z"; } // cvh on cvo (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "aa"; } // cvh on cvo (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ab"; } // cvh on cvo (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "ac"; } // cvh on cvo (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "dba"; } // CreativeInventoryScreen
    @Override public String guiInventoryClassName() { return "dbj"; } // InventoryScreen
    @Override public String closeContainerMethodName() { return "t_"; } // djx.t_() sends close packet + calls w()
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "g"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "dle"; }
    @Override public String renderMethodName() { return "d"; } // Minecraft render method (takes boolean)
}
