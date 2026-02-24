package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Version-specific field/method name mappings for a Minecraft client JAR.
 * Each client version has its own obfuscation scheme; implementations
 * provide the SRG or obfuscated names for the fields the agent needs.
 */
public interface FieldMappings {

    /** Fully-qualified Minecraft main class name. */
    String minecraftClassName();

    /** The run() method name (usually not obfuscated). */
    String runMethodName();

    /** The tick method name (e.g. "func_6246_i" or "i"). */
    String tickMethodName();

    /** Field name for thePlayer (EntityPlayerSP) on Minecraft class. */
    String playerFieldName();

    /** Field name for theWorld (World) on Minecraft class. */
    String worldFieldName();

    /** Field name for serverName/host (String) on Minecraft class. */
    String serverHostFieldName();

    /** Field name for serverPort (int) on Minecraft class. */
    String serverPortFieldName();

    /** Field name for displayWidth (int) on Minecraft class. */
    String displayWidthFieldName();

    /** Field name for displayHeight (int) on Minecraft class. */
    String displayHeightFieldName();

    /** Field name for posX (double) on Entity class. */
    String posXFieldName();

    /** Field name for posY (double) on Entity class. */
    String posYFieldName();

    /** Field name for posZ (double) on Entity class. */
    String posZFieldName();

    // --- Phase 2: Input/Inventory/World methods (default null for backward compat) ---

    /** Field name for GameSettings on Minecraft class. */
    default String gameSettingsFieldName() { return null; }

    /** Field name for movementInput on EntityPlayerSP class. */
    default String movementInputFieldName() { return null; }

    /** Field name for the boolean[] pressed keys array on MovementInputFromOptions. */
    default String pressedKeysFieldName() { return null; }

    /** Field name for yaw (float) on Entity class. */
    default String yawFieldName() { return null; }

    /** Field name for pitch (float) on Entity class. */
    default String pitchFieldName() { return null; }

    /** Field name for onGround (boolean) on Entity class. */
    default String onGroundFieldName() { return null; }

    /** Field name for inventory (InventoryPlayer) on EntityPlayer class. */
    default String inventoryFieldName() { return null; }

    /** Field name for mainInventory (ItemStack[]) on InventoryPlayer class. */
    default String mainInventoryFieldName() { return null; }

    /** Field name for currentItem (int) on InventoryPlayer class. */
    default String currentItemFieldName() { return null; }

    /** Field name for itemID (int) on ItemStack class. */
    default String itemIdFieldName() { return null; }

    /** Field name for stackSize (int) on ItemStack class. */
    default String stackSizeFieldName() { return null; }

    /** Method name for World.getBlockId(int,int,int). */
    default String getBlockIdMethodName() { return null; }

    /** Method name for Minecraft click handler (private void, takes int button). */
    default String clickMethodName() { return null; }

    /** Field name for mouseGrabbed (boolean) on Minecraft class. */
    default String mouseGrabbedFieldName() { return null; }

    // --- Phase 3: Chat, Q-drop, inventory GUI methods ---

    /** Method name for EntityPlayerSP.sendChatMessage(String). */
    default String sendChatMessageMethodName() { return null; }

    /** Method name for EntityPlayer.dropPlayerItem(ItemStack, boolean). */
    default String dropPlayerItemMethodName() { return null; }

    /** Method name for Minecraft.displayGuiScreen(GuiScreen). */
    default String displayGuiScreenMethodName() { return null; }

    /** Field name for currentScreen (GuiScreen) on Minecraft class. */
    default String currentScreenFieldName() { return null; }

    /** Field name for ingameGUI (InGameHud) on Minecraft class. */
    default String ingameGuiFieldName() { return null; }

    /** Field name for chatLines (List) on InGameHud class. */
    default String chatLinesFieldName() { return null; }

    /** Field name for cursorItem (ItemStack) on InventoryPlayer class. */
    default String cursorItemFieldName() { return null; }

    /** Field name for craftingInventory (ItemStack[]) on InventoryPlayer class. */
    default String craftingInventoryFieldName() { return null; }

    /** Obfuscated class name for GuiScreen. */
    default String guiScreenClassName() { return null; }

    // --- Phase 4: Beta abstraction fields (hardcoded in Alpha, different in Beta) ---

    /** Field name for click cooldown (int) on Minecraft class. Alpha: "S", Beta: "X". */
    default String clickCooldownFieldName() { return "S"; }

    /** Field name for playerController on Minecraft class. Alpha: "b", Beta: "c". */
    default String playerControllerFieldName() { return "b"; }

    /** Method name for PlayerController dig/clickBlock(int,int,int,int). */
    default String digMethodName() { return "a"; }

    /** Field name for ChatLine text (String). Alpha: "a", Beta: "a". */
    default String chatLineTextFieldName() { return "a"; }

    /** Field name for GuiScreen width (int). Alpha: "c", Beta: "m". */
    default String guiScreenWidthFieldName() { return "c"; }

    /** Field name for GuiScreen height (int). Alpha: "d", Beta: "n". */
    default String guiScreenHeightFieldName() { return "d"; }

    // --- KeyBinding-based movement (Beta 1.8+, where boolean[] pressedKeys doesn't exist) ---

    /** Field name for KeyBinding.pressed (boolean). Null = use boolean[] pressedKeys. */
    default String keyBindingPressedFieldName() { return null; }

    /** GameSettings field names for movement key bindings (forward/left/back/right/jump/sneak). */
    default String forwardKeyFieldName() { return null; }
    default String leftKeyFieldName() { return null; }
    default String backKeyFieldName() { return null; }
    default String rightKeyFieldName() { return null; }
    default String jumpKeyFieldName() { return null; }
    default String sneakKeyFieldName() { return null; }

    /** Obfuscated class name for creative inventory GUI (null = no creative inventory). */
    default String creativeInventoryClassName() { return null; }

    /** Obfuscated class name for survival inventory GUI. Alpha: "ne", Beta: "abd". */
    default String guiInventoryClassName() { return null; }
}
