package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Wraps a base FieldMappings and overrides the Minecraft class name,
 * run method, and tick method. Used when auto-detection discovers that
 * the client's obfuscation differs from the base protocol mapping.
 */
public class DelegatingFieldMappings implements FieldMappings {
    private final FieldMappings delegate;
    private final String minecraftClassName;
    private final String runMethodName;
    private final String tickMethodName;

    public DelegatingFieldMappings(FieldMappings delegate,
            String minecraftClassName, String runMethodName, String tickMethodName) {
        this.delegate = delegate;
        this.minecraftClassName = minecraftClassName;
        this.runMethodName = runMethodName;
        this.tickMethodName = tickMethodName;
    }

    @Override public String minecraftClassName() { return minecraftClassName; }
    @Override public String runMethodName() { return runMethodName; }
    @Override public String tickMethodName() { return tickMethodName; }

    // All other methods delegate to the base mappings
    @Override public String playerFieldName() { return delegate.playerFieldName(); }
    @Override public String worldFieldName() { return delegate.worldFieldName(); }
    @Override public String serverHostFieldName() { return delegate.serverHostFieldName(); }
    @Override public String serverPortFieldName() { return delegate.serverPortFieldName(); }
    @Override public String displayWidthFieldName() { return delegate.displayWidthFieldName(); }
    @Override public String displayHeightFieldName() { return delegate.displayHeightFieldName(); }
    @Override public String displayObjectFieldName() { return delegate.displayObjectFieldName(); }
    @Override public String posXFieldName() { return delegate.posXFieldName(); }
    @Override public String posYFieldName() { return delegate.posYFieldName(); }
    @Override public String posZFieldName() { return delegate.posZFieldName(); }
    @Override public String gameSettingsFieldName() { return delegate.gameSettingsFieldName(); }
    @Override public String movementInputFieldName() { return delegate.movementInputFieldName(); }
    @Override public String pressedKeysFieldName() { return delegate.pressedKeysFieldName(); }
    @Override public String yawFieldName() { return delegate.yawFieldName(); }
    @Override public String pitchFieldName() { return delegate.pitchFieldName(); }
    @Override public String onGroundFieldName() { return delegate.onGroundFieldName(); }
    @Override public String inventoryFieldName() { return delegate.inventoryFieldName(); }
    @Override public String mainInventoryFieldName() { return delegate.mainInventoryFieldName(); }
    @Override public String currentItemFieldName() { return delegate.currentItemFieldName(); }
    @Override public String itemIdFieldName() { return delegate.itemIdFieldName(); }
    @Override public String stackSizeFieldName() { return delegate.stackSizeFieldName(); }
    @Override public String getBlockIdMethodName() { return delegate.getBlockIdMethodName(); }
    @Override public String clickMethodName() { return delegate.clickMethodName(); }
    @Override public String mouseGrabbedFieldName() { return delegate.mouseGrabbedFieldName(); }
    @Override public String mouseHelperFieldName() { return delegate.mouseHelperFieldName(); }
    @Override public String sendChatMessageMethodName() { return delegate.sendChatMessageMethodName(); }
    @Override public String dropPlayerItemMethodName() { return delegate.dropPlayerItemMethodName(); }
    @Override public String dropOneItemMethodName() { return delegate.dropOneItemMethodName(); }
    @Override public String displayGuiScreenMethodName() { return delegate.displayGuiScreenMethodName(); }
    @Override public String currentScreenFieldName() { return delegate.currentScreenFieldName(); }
    @Override public String ingameGuiFieldName() { return delegate.ingameGuiFieldName(); }
    @Override public String chatLinesFieldName() { return delegate.chatLinesFieldName(); }
    @Override public String cursorItemFieldName() { return delegate.cursorItemFieldName(); }
    @Override public String craftingInventoryFieldName() { return delegate.craftingInventoryFieldName(); }
    @Override public String guiScreenClassName() { return delegate.guiScreenClassName(); }
    @Override public String sessionFieldName() { return delegate.sessionFieldName(); }
    @Override public String sessionUsernameFieldName() { return delegate.sessionUsernameFieldName(); }
    @Override public String clickCooldownFieldName() { return delegate.clickCooldownFieldName(); }
    @Override public String playerControllerFieldName() { return delegate.playerControllerFieldName(); }
    @Override public String digMethodName() { return delegate.digMethodName(); }
    @Override public String chatLineTextFieldName() { return delegate.chatLineTextFieldName(); }
    @Override public String guiScreenWidthFieldName() { return delegate.guiScreenWidthFieldName(); }
    @Override public String guiScreenHeightFieldName() { return delegate.guiScreenHeightFieldName(); }
    @Override public String keyBindingPressedFieldName() { return delegate.keyBindingPressedFieldName(); }
    @Override public String forwardKeyFieldName() { return delegate.forwardKeyFieldName(); }
    @Override public String leftKeyFieldName() { return delegate.leftKeyFieldName(); }
    @Override public String backKeyFieldName() { return delegate.backKeyFieldName(); }
    @Override public String rightKeyFieldName() { return delegate.rightKeyFieldName(); }
    @Override public String jumpKeyFieldName() { return delegate.jumpKeyFieldName(); }
    @Override public String sneakKeyFieldName() { return delegate.sneakKeyFieldName(); }
    @Override public String creativeInventoryClassName() { return delegate.creativeInventoryClassName(); }
    @Override public String guiInventoryClassName() { return delegate.guiInventoryClassName(); }
    @Override public String closeContainerMethodName() { return delegate.closeContainerMethodName(); }
    @Override public String setTileMethodName() { return delegate.setTileMethodName(); }
    @Override public String blockChangeQueueFieldName() { return delegate.blockChangeQueueFieldName(); }
    @Override public boolean isLwjgl3() { return delegate.isLwjgl3(); }
    @Override public boolean isNettyClient() { return delegate.isNettyClient(); }
    @Override public String blockRenderDispatcherClassName() { return delegate.blockRenderDispatcherClassName(); }
    @Override public String renderMethodName() { return delegate.renderMethodName(); }
    @Override public String gameRendererClassName() { return delegate.gameRendererClassName(); }
    @Override public boolean posYIsFeetLevel() { return delegate.posYIsFeetLevel(); }
    @Override public String rightClickMethodName() { return delegate.rightClickMethodName(); }
    @Override public String chatTextMethodName() { return delegate.chatTextMethodName(); }
}
