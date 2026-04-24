package org.bukkit.event.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public enum ClickType {
    LEFT, SHIFT_LEFT, RIGHT, SHIFT_RIGHT, WINDOW_BORDER_LEFT, WINDOW_BORDER_RIGHT, MIDDLE, NUMBER_KEY, DOUBLE_CLICK, DROP, CONTROL_DROP, CREATIVE, SWAP_OFFHAND, UNKNOWN;
    public boolean isKeyboardClick() {
        return false;
    }
    public boolean isMouseClick() {
        return false;
    }
    public boolean isCreativeAction() {
        return false;
    }
    public boolean isRightClick() {
        return false;
    }
    public boolean isLeftClick() {
        return false;
    }
    public boolean isShiftClick() {
        return false;
    }
}
