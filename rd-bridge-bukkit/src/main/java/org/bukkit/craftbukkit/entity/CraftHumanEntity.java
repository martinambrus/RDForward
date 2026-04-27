// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.craftbukkit.entity;

/**
 * Minimal CraftBukkit-shaped {@code CraftHumanEntity}. Exists only so
 * LuckPerms's
 * {@code me.lucko.luckperms.bukkit.inject.permissible.PermissibleInjector.<clinit>}
 * — which reflects {@code obcClass("entity.CraftHumanEntity").getDeclaredField("perm")}
 * — can complete its static initializer without
 * {@link ClassNotFoundException}. Without this class the failed clinit
 * cascades into {@link ExceptionInInitializerError} on every subsequent
 * call out of {@code BukkitContextManager.getQueryOptionsSupplier},
 * flooding the log on every player-join.
 *
 * <p>The {@code perm} field is declared private to match the real
 * CraftBukkit class; LuckPerms calls {@code setAccessible(true)} before
 * use. Per-player {@code Field.set/get} on RDForward's {@code Player}
 * proxy will not succeed (the proxy is not a {@code CraftHumanEntity}
 * instance), but those calls are isolated by
 * {@code BukkitEventAdapter.invokeListener} so the connection is not
 * aborted — and LuckPerms's own context calculator still works through
 * its non-injected fall-back path.
 */
public class CraftHumanEntity {
    /** Initialised to a non-null {@link org.bukkit.permissions.PermissibleBase}
     *  so LuckPerms's {@code PermissibleInjector.inject} can read it as
     *  the previous permissible. The injector calls {@code .getClass()}
     *  on the value and reads the {@code attachments} list — both fail
     *  with NPE when {@code perm} is null, denying the player's login.
     *  Real CraftBukkit assigns {@code perm = new PermissibleBase(this)}
     *  in the constructor; we use the no-arg form because this class
     *  doesn't implement {@link org.bukkit.permissions.ServerOperator}. */
    private org.bukkit.permissions.PermissibleBase perm = new org.bukkit.permissions.PermissibleBase();

    /** Read of the {@code perm} field — exposed as a no-op accessor so
     *  test code that wants to verify the field exists does not have
     *  to use reflection. Plugins go through reflection regardless. */
    public org.bukkit.permissions.PermissibleBase getPerm() {
        return perm;
    }
}
