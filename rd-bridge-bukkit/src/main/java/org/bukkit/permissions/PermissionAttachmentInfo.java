package org.bukkit.permissions;

public class PermissionAttachmentInfo {

    private final Permissible permissible;
    private final String permission;
    private final PermissionAttachment attachment;
    private final boolean value;

    public PermissionAttachmentInfo(Permissible permissible, String permission, PermissionAttachment attachment, boolean value) {
        this.permissible = permissible;
        this.permission = permission;
        this.attachment = attachment;
        this.value = value;
    }

    public PermissionAttachmentInfo() {
        this.permissible = null;
        this.permission = null;
        this.attachment = null;
        this.value = false;
    }

    public Permissible getPermissible() {
        return permissible;
    }

    public String getPermission() {
        return permission;
    }

    public PermissionAttachment getAttachment() {
        return attachment;
    }

    public boolean getValue() {
        return value;
    }
}
