package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.14.4 (protocol v498).
 * Verified by CFR decompilation of the 1.14.4 client JAR.
 *
 * Minecraft = cyc, Entity = aio, LivingEntity = aix, PlayerEntity = awg,
 * AbstractClientPlayerEntity = dmm, ClientPlayerEntity = dmp,
 * GameSettings = cyg, PlayerInventory = awf, ItemStack = bcj, Item = bce,
 * World = bhr, ClientWorld = dkf,
 * Screen = dcl, IngameGui = cyv, NewChatGui = czj, ChatLine = cxx,
 * PlayerController = dke, KeyBinding = cxz, Session = cyn,
 * MovementInput = dmn, Window = cuo,
 * MouseHelper = cyd, CreativeInventoryScreen = dds, InventoryScreen = deb.
 *
 * Key differences from V480 (1.14.1):
 * - Minecraft class renamed: cvk -> cyc. Most classes shifted significantly.
 * - playerControllerFieldName shifted: f -> e.
 * - Window field shifted: g -> f. World field shifted: h -> g.
 * - playerFieldName shifted: j -> i. currentScreenFieldName shifted: o -> n.
 * - clickCooldownFieldName shifted: s -> r. ingameGuiFieldName shifted: t -> s.
 * - gameSettingsFieldName shifted: w -> v. MouseHelper shifted: x -> w.
 * - Window framebuffer fields shifted: r,s -> q,r.
 * - GameSettings key bindings shifted by one: X,Y,Z,aa,ab,ac -> Y,Z,aa,ab,ac,ad.
 * - CreativeInventoryScreen: dba -> dds. InventoryScreen: dbj -> deb.
 * - All method names and field names within their respective classes unchanged.
 */
public class NettyReleaseV498Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "cyc"; } // default package, obfuscated
    @Override public String runMethodName() { return "b"; } // Main calls new cyc(...).b()
    @Override public String tickMethodName() { return "m"; } // cyc.m() game tick; decrements al, ticks screens
    @Override public String playerFieldName() { return "i"; } // dmp (ClientPlayerEntity) on cyc
    @Override public String worldFieldName() { return "g"; } // dkf (ClientWorld) on cyc
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "q"; } // int framebufferWidth on cuo (Window); Window is field f on cyc
    @Override public String displayHeightFieldName() { return "r"; } // int framebufferHeight on cuo (Window); Window is field f on cyc
    @Override public String displayObjectFieldName() { return "f"; } // cuo (Window) on cyc
    @Override public String posXFieldName() { return "p"; } // double on aio (Entity)
    @Override public String posYFieldName() { return "q"; } // double on aio (Entity)
    @Override public String posZFieldName() { return "r"; } // double on aio (Entity)
    @Override public String gameSettingsFieldName() { return "v"; } // cyg (GameSettings) on cyc
    @Override public String movementInputFieldName() { return "f"; } // dmn (MovementInput) on dmp (ClientPlayerEntity)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "s"; } // float on aio (Entity)
    @Override public String pitchFieldName() { return "t"; } // float on aio (Entity)
    @Override public String onGroundFieldName() { return "w"; } // boolean on aio (Entity)
    @Override public String inventoryFieldName() { return "bx"; } // awf (PlayerInventory) on awg (PlayerEntity)
    @Override public String mainInventoryFieldName() { return "a"; } // fk<bcj> (NonNullList<ItemStack>, size 36) on awf
    @Override public String currentItemFieldName() { return "d"; } // int selectedSlot on awf (PlayerInventory)
    @Override public String itemIdFieldName() { return "f"; } // bce (Item) on bcj (ItemStack); deprecated field
    @Override public String stackSizeFieldName() { return "d"; } // int count on bcj (ItemStack)
    @Override public String getBlockIdMethodName() { return "e_"; } // bhr.e_(ew) returns bvt (BlockState)
    @Override public String clickMethodName() { return "f"; } // cyc.f(boolean) continuous left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on cyd (MouseHelper); MouseHelper is field w on cyc
    @Override public String mouseHelperFieldName() { return "w"; } // cyd (MouseHelper) on cyc
    @Override public String sendChatMessageMethodName() { return "f"; } // dmp.f(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "n"; } // dmp.n(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses n(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // cyc.a(dcl) sets current screen
    @Override public String currentScreenFieldName() { return "n"; } // dcl (Screen) on cyc, @Nullable
    @Override public String ingameGuiFieldName() { return "s"; } // cyv (IngameGui) on cyc
    @Override public String chatLinesFieldName() { return "d"; } // List<cxx> allMessages on czj (NewChatGui); czj is field h on cyv
    @Override public String cursorItemFieldName() { return "g"; } // bcj (ItemStack) on awf (PlayerInventory); mouse-held item
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "dcl"; } // Screen class
    @Override public String sessionFieldName() { return "Z"; } // cyn (Session) on cyc
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on cyn (Session)
    @Override public String clickCooldownFieldName() { return "r"; } // protected int on cyc; leftClickCounter/attackCooldown
    @Override public String playerControllerFieldName() { return "e"; } // dke (PlayerController) on cyc
    @Override public String digMethodName() { return "b"; } // dke.b(ew, fb) onPlayerDamageBlock continuous dig
    @Override public String chatLineTextFieldName() { return "b"; } // jo (ITextComponent) on cxx (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "width"; } // int on dcl (Screen); un-obfuscated in 1.14.4
    @Override public String guiScreenHeightFieldName() { return "height"; } // int on dcl (Screen); un-obfuscated in 1.14.4
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on cxz (KeyBinding)
    @Override public String forwardKeyFieldName() { return "Y"; } // cxz on cyg (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "Z"; } // cxz on cyg (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "aa"; } // cxz on cyg (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "ab"; } // cxz on cyg (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ac"; } // cxz on cyg (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "ad"; } // cxz on cyg (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "dds"; } // CreativeInventoryScreen
    @Override public String guiInventoryClassName() { return "deb"; } // InventoryScreen
    @Override public String closeContainerMethodName() { return "t_"; } // dmp.t_() sends close packet + calls w()
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "g"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "dnw"; }
    @Override public String renderMethodName() { return "d"; } // Minecraft render method (takes boolean)
}
