package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Release 1.16.4 (protocol v754).
 * Verified by CFR decompilation of the 1.16.4 client JAR.
 *
 * Minecraft = djz, Entity = aqa, LivingEntity = aql, PlayerEntity = bfw,
 * AbstractClientPlayerEntity = dzj, ClientPlayerEntity = dzm,
 * GameSettings = dkd, PlayerInventory = bfv, ItemStack = bmb, Item = blx,
 * Block = buo, World = brx, ClientWorld = dwt,
 * Screen = dot, IngameGui = dkv, NewChatGui = dlk, ChatLine = dju,
 * PlayerController = dww, KeyBinding = djw, Session = dkm,
 * MovementInput = dzk, KeyboardInput = dzl, Window = dez,
 * MouseHelper = dka, CreativeInventoryScreen = dqc, InventoryScreen = dql.
 *
 * Key differences from V753 (1.16.3):
 * - All class names shifted by ~3 letters due to new SocialInteractionsService class.
 * - Field names on each class remain identical to V753.
 * - Session field on Minecraft changed: V -> W (dkm on djz).
 * - Window field on Minecraft changed: N -> O (dez on djz).
 */
public class NettyReleaseV754Mappings implements FieldMappings {

    @Override public String minecraftClassName() { return "djz"; } // default package, obfuscated
    @Override public String runMethodName() { return "e"; } // Main calls new djz(...).e()
    @Override public String tickMethodName() { return "q"; } // djz.q() game tick; decrements w, ticks screens
    @Override public String playerFieldName() { return "s"; } // dzm (ClientPlayerEntity) on djz
    @Override public String worldFieldName() { return "r"; } // dwt (ClientWorld) on djz
    @Override public String serverHostFieldName() { return null; } // Netty: CLI args
    @Override public String serverPortFieldName() { return null; } // Netty: CLI args
    @Override public String displayWidthFieldName() { return null; } // LWJGL3: on Window (dez), field O on djz
    @Override public String displayHeightFieldName() { return null; } // LWJGL3: on Window (dez), field O on djz
    @Override public String posXFieldName() { return "m"; } // double on aqa (Entity)
    @Override public String posYFieldName() { return "n"; } // double on aqa (Entity)
    @Override public String posZFieldName() { return "o"; } // double on aqa (Entity)
    @Override public String gameSettingsFieldName() { return "k"; } // dkd (GameSettings) on djz
    @Override public String movementInputFieldName() { return "f"; } // dzk (MovementInput) on dzm (ClientPlayerEntity)
    @Override public String pressedKeysFieldName() { return null; } // KeyBinding-based
    @Override public String yawFieldName() { return "p"; } // float on aqa (Entity)
    @Override public String pitchFieldName() { return "q"; } // float on aqa (Entity)
    @Override public String onGroundFieldName() { return "t"; } // boolean on aqa (Entity)
    @Override public String inventoryFieldName() { return "bm"; } // bfv (PlayerInventory) on bfw (PlayerEntity)
    @Override public String mainInventoryFieldName() { return "a"; } // gj<bmb> (NonNullList<ItemStack>, size 36) on bfv
    @Override public String currentItemFieldName() { return "d"; } // int selectedSlot on bfv (PlayerInventory)
    @Override public String itemIdFieldName() { return "h"; } // blx (Item) on bmb (ItemStack); deprecated field
    @Override public String stackSizeFieldName() { return "f"; } // int count on bmb (ItemStack)
    @Override public String getBlockIdMethodName() { return "d_"; } // brx.d_(fx) returns ceh (BlockState)
    @Override public String clickMethodName() { return "f"; } // djz.f(boolean) continuous left-click/attack
    @Override public String mouseGrabbedFieldName() { return "r"; } // boolean on dka (MouseHelper); MouseHelper is field l on djz
    @Override public String mouseHelperFieldName() { return "l"; } // dka (MouseHelper) on djz
    @Override public String sendChatMessageMethodName() { return "f"; } // dzm.f(String) sends chat packet
    @Override public String dropPlayerItemMethodName() { return "a"; } // dzm.a(boolean) sends dig packet for drop
    @Override public String dropOneItemMethodName() { return null; } // uses a(boolean), no no-arg variant
    @Override public String displayGuiScreenMethodName() { return "a"; } // djz.a(dot) sets current screen
    @Override public String currentScreenFieldName() { return "y"; } // dot (Screen) on djz, @Nullable
    @Override public String ingameGuiFieldName() { return "j"; } // dkv (IngameGui) on djz
    @Override public String chatLinesFieldName() { return "d"; } // List<dju<nr>> allMessages on dlk (NewChatGui); dlk is field l on dkv
    @Override public String cursorItemFieldName() { return "g"; } // bmb (ItemStack) on bfv (PlayerInventory); mouse-held item
    @Override public String craftingInventoryFieldName() { return null; }
    @Override public String guiScreenClassName() { return "dot"; } // Screen class
    @Override public String sessionFieldName() { return "W"; } // dkm (Session) on djz
    @Override public String sessionUsernameFieldName() { return "a"; } // String username on dkm (Session)
    @Override public String clickCooldownFieldName() { return "w"; } // protected int on djz; leftClickCounter/attackCooldown
    @Override public String playerControllerFieldName() { return "q"; } // dww (PlayerController) on djz
    @Override public String digMethodName() { return "b"; } // dww.b(fx, gc) onPlayerDamageBlock continuous dig
    @Override public String chatLineTextFieldName() { return "b"; } // T (nr/ITextComponent) on dju (ChatLine)
    @Override public String guiScreenWidthFieldName() { return "k"; } // int on dot (Screen); obfuscated in 1.16.4
    @Override public String guiScreenHeightFieldName() { return "l"; } // int on dot (Screen); obfuscated in 1.16.4
    @Override public String keyBindingPressedFieldName() { return "i"; } // boolean on djw (KeyBinding)
    @Override public String forwardKeyFieldName() { return "af"; } // djw on dkd (key.forward, GLFW 87=W)
    @Override public String leftKeyFieldName() { return "ag"; } // djw on dkd (key.left, GLFW 65=A)
    @Override public String backKeyFieldName() { return "ah"; } // djw on dkd (key.back, GLFW 83=S)
    @Override public String rightKeyFieldName() { return "ai"; } // djw on dkd (key.right, GLFW 68=D)
    @Override public String jumpKeyFieldName() { return "aj"; } // djw on dkd (key.jump, GLFW 32=Space)
    @Override public String sneakKeyFieldName() { return "ak"; } // djw on dkd (key.sneak, GLFW 340=LShift)
    @Override public String creativeInventoryClassName() { return "dqc"; } // CreativeInventoryScreen
    @Override public String guiInventoryClassName() { return "dql"; } // InventoryScreen
    @Override public String closeContainerMethodName() { return "m"; } // dzm.m() sends close window packet + calls x()
    @Override public boolean posYIsFeetLevel() { return true; } // 1.8+: posY = feet
    @Override public String rightClickMethodName() { return "g"; } // right-click/use method
    @Override public boolean isLwjgl3() { return true; }
    @Override public boolean isNettyClient() { return true; }
    @Override public String blockRenderDispatcherClassName() { return "eax"; }
    @Override public String renderMethodName() { return "e"; } // Minecraft render method (takes boolean)
}
