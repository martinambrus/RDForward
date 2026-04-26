// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.permissions;

/**
 * Bukkit-shaped {@code PermissionDefault} enum with real
 * {@link #getValue(boolean)} semantics. Plugins (Vault, LuckPerms,
 * LoginSecurity) consume the value to decide whether a non-op player
 * should be granted the permission by default.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public enum PermissionDefault {
    TRUE, FALSE, OP, NOT_OP;

    public boolean getValue(boolean op) {
        switch (this) {
            case TRUE:   return true;
            case FALSE:  return false;
            case OP:     return op;
            case NOT_OP: return !op;
            default:     return false;
        }
    }

    public static org.bukkit.permissions.PermissionDefault getByName(java.lang.String name) {
        if (name == null) return null;
        String key = name.toLowerCase(java.util.Locale.ENGLISH).replace('_', ' ');
        switch (key) {
            case "true": case "yes":          return TRUE;
            case "false": case "no":          return FALSE;
            case "op": case "isop": case "operator": case "admin": case "administrator":
                return OP;
            case "!op": case "not op": case "notop": case "deop": case "deoperator":
            case "nonop": case "non op": case "non-op":
                return NOT_OP;
            default: return null;
        }
    }

    @Override
    public java.lang.String toString() {
        return name();
    }
}
