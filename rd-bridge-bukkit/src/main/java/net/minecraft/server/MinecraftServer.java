// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.minecraft.server;

/**
 * NMS MinecraftServer stub at the unversioned package layout that Paper
 * adopted in 1.17. Plugins like EssentialsX's {@code ReflServerStateProvider}
 * reflectively look up {@code net.minecraft.server.MinecraftServer} via
 * {@code ReflUtil.getNMSClass("MinecraftServer")} (Essentials drops the
 * versioned segment when the parsed NMS version is >= v1_17_R1, which our
 * {@code org.bukkit.craftbukkit.v1_21_R1.CraftServer} package layout
 * advertises). Without this stub the lookup returns {@code null} and the
 * provider ctor NPEs.
 *
 * <p>Three reflection surfaces are required:
 * <ul>
 *   <li>{@code static MinecraftServer getServer()} — Essentials calls
 *       {@code nmsClass.getMethod("getServer").invoke(null)} to obtain the
 *       NMS server singleton.</li>
 *   <li>{@code boolean x()} — obfuscated "isRunning" name selected by
 *       Essentials's {@code MDFIVEMAGICLETTER} switch for NMS versions in
 *       the v1_20_R4..v1_21_R5 range. Returns {@code true} while the
 *       Bukkit facade is installed.</li>
 *   <li>{@code boolean isStopped()} — convenience accessor mirroring real
 *       Paper's deobfuscated method name. Inverse of {@link #x()}.</li>
 * </ul>
 *
 * <p>Both runtime accessors use {@code org.bukkit.Bukkit.getServer() ==
 * null} as the shutdown proxy: the bridge installs a CraftServer at boot
 * and clears it during {@code BukkitBridge.uninstall()} at shutdown, so the
 * field naturally tracks server lifecycle without any extra wiring.
 */
public final class MinecraftServer {

    private static final MinecraftServer INSTANCE = new MinecraftServer();

    public MinecraftServer() {}

    /** Static accessor that mirrors real CraftBukkit's
     *  {@code MinecraftServer.getServer()} pattern. Essentials invokes this
     *  reflectively to obtain the NMS server before binding {@link #x()}. */
    public static MinecraftServer getServer() { return INSTANCE; }

    /** Obfuscated "isRunning" surface for the v1_20_R4..v1_21_R5 magic-letter
     *  bucket. Returns {@code true} while the Bukkit facade is installed. */
    public boolean x() { return org.bukkit.Bukkit.getServer() != null; }

    /** Deobfuscated convenience for plugins that look up the canonical name
     *  rather than the magic letter. Inverse of {@link #x()}. */
    public boolean isStopped() { return org.bukkit.Bukkit.getServer() == null; }
}
