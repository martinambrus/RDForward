package org.bukkit.entity;

/**
 * Marker stub — Paper's {@code CommandSourceStack.getExecutor()} returns an
 * {@link Entity}, so plugins porting against our stubs need the type to
 * resolve. The bridge always returns {@code null} from {@code getExecutor},
 * so nothing ever instantiates {@code Entity}.
 *
 * <p>Note: {@link Player} in {@code rd-bridge-bukkit} does not implement
 * this interface. Plugins that {@code instanceof Player} the result of
 * {@code getExecutor()} will match {@code false} — use {@code getSender()}
 * when you need a real player object.
 */
public interface Entity {
}
