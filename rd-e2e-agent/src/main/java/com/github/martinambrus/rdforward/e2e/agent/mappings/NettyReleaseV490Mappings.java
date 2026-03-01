package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.14.3 (protocol v490).
 * Verified by CFR decompilation of the 1.14.3 client JAR.
 *
 * Minecraft = cvo, Entity = ail, LivingEntity = ait, PlayerEntity = awb,
 * AbstractClientPlayerEntity = djy, ClientPlayerEntity = dkb,
 * GameSettings = cvs, PlayerInventory = awa, ItemStack = bce, Item = bbz,
 * Block = bmq, World = bhm, ClientWorld = dhr,
 * Screen = czx, IngameGui = cwh, NewChatGui = cwv, ChatLine = cvj,
 * PlayerController = dhq, KeyBinding = cvl, Session = cvz,
 * MovementInput = djz, KeyboardInput = dka, Window = cuh,
 * MouseHelper = cvp, CreativeInventoryScreen = dbe, InventoryScreen = dbn.
 *
 * Key differences from V480 (1.14.1):
 * - Minecraft class renamed: cvk -> cvo. Session: cvv -> cvz. Window: cud -> cuh.
 * - GameSettings: cvo -> cvs. MouseHelper: cvl -> cvp. KeyBinding: cvh -> cvl.
 * - Entity: aii -> ail. PlayerEntity: avy -> awb.
 * - ClientPlayerEntity: djx -> dkb. AbstractClientPlayerEntity: djv -> djy.
 * - MovementInput: djv -> djz.
 * - IngameGui: cwd -> cwh. NewChatGui: cwr -> cwv. ChatLine: cvf -> cvj.
 * - PlayerController: dhm -> dhq. Screen: czt -> czx. Item: bbw -> bbz.
 * - PlayerInventory: avx -> awa. ItemStack: bcb -> bce.
 * - World: bhi -> bhm. ClientWorld: dhn -> dhr.
 * - CreativeInventoryScreen: dba -> dbe. InventoryScreen: dbj -> dbn.
 * - All field names within classes remain the same as V480.
 */
public class NettyReleaseV490Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "cvo"; } // default package, obfuscated
    @Override public String runMethodName() { return "b"; } // Main calls new cvo(...).b()
    @Override public String tickMethodName() { return "m"; } // cvo.m() game tick; decrements s, ticks screens
    @Override public String playerFieldName() { return "j"; } // dkb (ClientPlayerEntity) on cvo
    @Override public String worldFieldName() { return "h"; } // dhr (ClientWorld) on cvo
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return "r"; } // int framebufferWidth on cuh (Window); Window is field g on cvo
    @Override public String displayHeightFieldName() { return "s"; } // int framebufferHeight on cuh (Window); Window is field g on cvo
    @Override public String displayObjectFieldName() { return "g"; } // cuh (Window) on cvo
    @Override public String posXFieldName() { return "p"; } // double on ail (Entity)
    @Override public String posYFieldName() { return "q"; } // double on ail (Entity)
    @Override public String posZFieldName() { return "r"; } // double on ail (Entity)
    @Override public String gameSettingsFieldName() { return "w"; } // cvs (GameSettings) on cvo
    @Override public String movementInputFieldName() { return "f"; } // djz (MovementInput) on dkb (ClientPlayerEntity)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "s"; } // float on ail (Entity)
    @Override public String pitchFieldName() { return "t"; } // float on ail (Entity)
    @Override public String onGroundFieldName() { return "w"; } // boolean on ail (Entity)
    @Override public String inventoryFieldName() { return "bx"; } // awa (PlayerInventory) on awb (PlayerEntity)
    @Override public String mainInventoryFieldName() { return "a"; } // fj<bce> (NonNullList<ItemStack>, size 36) on awa
    @Override public String currentItemFieldName() { return "d"; } // int selectedSlot on awa (PlayerInventory)
    @Override public String itemIdFieldName() { return "f"; } // bbz (Item) on bce (ItemStack)
    @Override public String stackSizeFieldName() { return "d"; } // int count on bce (ItemStack)
    @Override public String getBlockIdMethodName() { return "e_"; } // bhm.e_(ev) returns bvo (BlockState)
    @Override public String clickMethodName() { return "f"; } // cvo.f(boolean) continuous left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on cvp (MouseHelper); MouseHelper is field x on cvo
    @Override public String mouseHelperFieldName() { return "x"; } // cvp (MouseHelper) on cvo
    @Override public String sendChatMessageMethodName() { return "f"; } // dkb.f(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "n"; } // dkb.n(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses n(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // cvo.a(czx) sets current screen
    @Override public String currentScreenFieldName() { return "o"; } // czx (Screen) on cvo, @Nullable
    @Override public String ingameGuiFieldName() { return "t"; } // cwh (IngameGui) on cvo
    @Override public String chatLinesFieldName() { return "d"; } // List<cvj> allMessages on cwv (NewChatGui); cwv is field h on cwh
    @Override public String cursorItemFieldName() { return "g"; } // bce (ItemStack) on awa (PlayerInventory); mouse-held item
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "czx"; } // Screen class
    @Override public String sessionFieldName() { return "Z"; } // cvz (Session) on cvo
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on cvz (Session)
    @Override public String clickCooldownFieldName() { return "s"; } // protected int on cvo; leftClickCounter/attackCooldown
    @Override public String playerControllerFieldName() { return "f"; } // dhq (PlayerController) on cvo
    @Override public String digMethodName() { return "b"; } // dhq.b(ev, fa) onPlayerDamageBlock continuous dig
    @Override public String chatLineTextFieldName() { return "b"; } // jn (ITextComponent) on cvj (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "width"; } // int on czx (Screen); un-obfuscated in 1.14.3
    @Override public String guiScreenHeightFieldName() { return "height"; } // int on czx (Screen); un-obfuscated in 1.14.3
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on cvl (KeyBinding)
    @Override public String forwardKeyFieldName() { return "X"; } // cvl on cvs (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "Y"; } // cvl on cvs (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "Z"; } // cvl on cvs (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "aa"; } // cvl on cvs (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ab"; } // cvl on cvs (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "ac"; } // cvl on cvs (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "dbe"; } // CreativeInventoryScreen
    @Override public String guiInventoryClassName() { return "dbn"; } // InventoryScreen
    @Override public String closeContainerMethodName() { return "t_"; } // dkb.t_() sends close packet + calls w()
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "g"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "dli"; }
    @Override public String renderMethodName() { return "d"; } // Minecraft render method (takes boolean)
}
