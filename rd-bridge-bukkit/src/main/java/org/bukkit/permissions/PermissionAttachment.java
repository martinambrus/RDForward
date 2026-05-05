package org.bukkit.permissions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.plugin.Plugin;

public class PermissionAttachment {

    private final Plugin plugin;
    private final Permissible permissible;
    private final Map<String, Boolean> permissions = new HashMap<>();
    private PermissionRemovedExecutor removalCallback;

    public PermissionAttachment(Plugin plugin, Permissible permissible) {
        this.plugin = plugin;
        this.permissible = permissible;
    }

    public PermissionAttachment() {
        this.plugin = null;
        this.permissible = null;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setRemovalCallback(PermissionRemovedExecutor removalCallback) {
        this.removalCallback = removalCallback;
    }

    public PermissionRemovedExecutor getRemovalCallback() {
        return removalCallback;
    }

    public Permissible getPermissible() {
        return permissible;
    }

    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    public void setPermission(String name, boolean value) {
        permissions.put(name, value);
        if (permissible != null) {
            permissible.recalculatePermissions();
        }
    }

    public void setPermission(Permission perm, boolean value) {
        setPermission(perm.getName(), value);
    }

    public void unsetPermission(String name) {
        permissions.remove(name);
        if (permissible != null) {
            permissible.recalculatePermissions();
        }
    }

    public void unsetPermission(Permission perm) {
        unsetPermission(perm.getName());
    }

    public boolean remove() {
        if (permissible != null) {
            permissible.removeAttachment(this);
        }
        if (removalCallback != null) {
            removalCallback.attachmentRemoved(this);
        }
        return true;
    }
}
