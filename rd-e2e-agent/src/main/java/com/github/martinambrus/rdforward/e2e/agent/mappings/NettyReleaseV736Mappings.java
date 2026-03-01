package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.16.1 (protocol v736).
 * Verified by CFR decompilation of the 1.16.1 client JAR.
 *
 * Minecraft = dlx, Entity = aom, LivingEntity = aoy, PlayerEntity = bec,
 * AbstractClientPlayerEntity = ebc, ClientPlayerEntity = ebf,
 * GameSettings = dmb, PlayerInventory = beb, ItemStack = bki, Item = bke,
 * Block = bvs, World = bqb, ClientWorld = dym,
 * Screen = dqs, IngameGui = dmu, NewChatGui = dnj, ChatLine = dls,
 * PlayerController = dyp, KeyBinding = dlu, Session = dml,
 * MovementInput = ebd, Window = dgy,
 * MouseHelper = dly, CreativeInventoryScreen = dsa, InventoryScreen = dsj.
 *
 * Key differences from V573 (1.15):
 * - Minecraft class still dlx (same as 1.16).
 * - Player field shifted from r (dpy) to s (ebf).
 * - World field shifted from q (dnl) to r (dym).
 * - PlayerController field shifted from p (dno) to q (dyp).
 * - IngameGui field shifted from i (dcg) to j (dmu).
 * - GameSettings field shifted from j (dbp) to k (dmb).
 * - Session field shifted from U (dbx) to V (dml).
 * - CurrentScreen field shifted from x (dfz) to y (dqs).
 * - Entity position now stored as public doubles m/n/o (xOld/yOld/zOld)
 *   and private Vec3d ap. Agent uses m/n/o which are synchronized on setPos.
 * - clickCooldown shifted from v to w.
 * - Run method shifted from b() to e().
 * - Tick method shifted from n() to q().
 * - Click method shifted from e(boolean) to f(boolean).
 * - closeContainer shifted from v_() to m() on ClientPlayerEntity.
 * - sendChatMessage is f(String) on ebf.
 * - dropPlayerItem is a(boolean) on ebf (sends dig packet for drop).
 * - Screen width/height obfuscated to k/l (were un-obfuscated in 1.15).
 * - Key bindings shifted: forward ad, left ae, back af, right ag, jump ah, sneak ai.
 * - ItemStack count field shifted from d to f; Item field shifted from f to h.
 * - PlayerInventory field on PlayerEntity shifted from bu to bt.
 */
public class NettyReleaseV736Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "dlx"; } // default package, obfuscated
    @Override public String runMethodName() { return "e"; } // dlx.e() main game loop
    @Override public String tickMethodName() { return "q"; } // dlx.q() game tick; decrements w, ticks screens
    @Override public String playerFieldName() { return "s"; } // ebf (ClientPlayerEntity) on dlx
    @Override public String worldFieldName() { return "r"; } // dym (ClientWorld) on dlx
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return null; } // LWJGL3: Window-based
    @Override public String displayHeightFieldName() { return null; } // LWJGL3: Window-based
    @Override public String posXFieldName() { return "m"; } // public double on aom (Entity); xOld, synced with position
    @Override public String posYFieldName() { return "n"; } // public double on aom (Entity); yOld, synced with position
    @Override public String posZFieldName() { return "o"; } // public double on aom (Entity); zOld, synced with position
    @Override public String gameSettingsFieldName() { return "k"; } // dmb (GameSettings) on dlx
    @Override public String movementInputFieldName() { return "f"; } // ebd (MovementInput) on ebf (ClientPlayerEntity)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "p"; } // float on aom (Entity)
    @Override public String pitchFieldName() { return "q"; } // float on aom (Entity)
    @Override public String onGroundFieldName() { return "t"; } // boolean on aom (Entity)
    @Override public String inventoryFieldName() { return "bt"; } // beb (PlayerInventory) on bec (PlayerEntity)
    @Override public String mainInventoryFieldName() { return "a"; } // gi<bki> (NonNullList<ItemStack>, size 36) on beb
    @Override public String currentItemFieldName() { return "d"; } // int selectedSlot on beb (PlayerInventory)
    @Override public String itemIdFieldName() { return "h"; } // bke (Item) on bki (ItemStack); @Deprecated
    @Override public String stackSizeFieldName() { return "f"; } // int count on bki (ItemStack)
    @Override public String getBlockIdMethodName() { return "d_"; } // bqb.d_(fu) returns cfj (BlockState)
    @Override public String clickMethodName() { return "f"; } // dlx.f(boolean) continuous left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on dly (MouseHelper); MouseHelper is field m on dlx
    @Override public String mouseHelperFieldName() { return "m"; } // dly (MouseHelper) on dlx
    @Override public String sendChatMessageMethodName() { return "f"; } // ebf.f(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "a"; } // ebf.a(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // dlx.a(dqs) sets current screen
    @Override public String currentScreenFieldName() { return "y"; } // dqs (Screen) on dlx, @Nullable
    @Override public String ingameGuiFieldName() { return "j"; } // dmu (IngameGui) on dlx
    @Override public String chatLinesFieldName() { return "d"; } // List<dls> allMessages on dnj (NewChatGui); dnj is field k on dmu
    @Override public String cursorItemFieldName() { return "g"; } // bki (ItemStack) on beb (PlayerInventory); mouse-held item
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "dqs"; } // Screen class
    @Override public String sessionFieldName() { return "V"; } // dml (Session) on dlx
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on dml (Session)
    @Override public String clickCooldownFieldName() { return "w"; } // protected int on dlx; leftClickCounter/attackCooldown
    @Override public String playerControllerFieldName() { return "q"; } // dyp (PlayerController) on dlx
    @Override public String digMethodName() { return "b"; } // dyp.b(fu, fz) onPlayerDamageBlock continuous dig
    @Override public String chatLineTextFieldName() { return "b"; } // mu (ITextComponent) on dls (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "k"; } // int width on dqs (Screen)
    @Override public String guiScreenHeightFieldName() { return "l"; } // int height on dqs (Screen)
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on dlu (KeyBinding)
    @Override public String forwardKeyFieldName() { return "ad"; } // dlu on dmb (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "ae"; } // dlu on dmb (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "af"; } // dlu on dmb (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "ag"; } // dlu on dmb (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "ah"; } // dlu on dmb (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "ai"; } // dlu on dmb (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "dsa"; } // CreativeInventoryScreen
    @Override public String guiInventoryClassName() { return "dsj"; } // InventoryScreen
    @Override public String closeContainerMethodName() { return "m"; } // ebf.m() sends close packet + calls x()
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "g"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "ecq"; }
    @Override public String renderMethodName() { return "e"; } // Minecraft render method (takes boolean)
}
