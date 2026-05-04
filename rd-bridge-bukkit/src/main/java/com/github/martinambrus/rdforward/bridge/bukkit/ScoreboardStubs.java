// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.lang.reflect.Proxy;
import java.util.Collections;

/**
 * Shared no-op stubs for the {@code org.bukkit.scoreboard} interface
 * tree. RDForward has no scoreboard / team / objective model, but legacy
 * plugins (Jail 2.x's {@code JailScoreboardManager}, EssentialsChat's
 * lowest-priority chat listener) call these APIs unconditionally and
 * NPE on a {@code null} return.
 *
 * <p>The stubs return non-null nested stubs for the interface-typed
 * accessors plugins chain: {@link ScoreboardManager#getNewScoreboard()}
 * yields a Scoreboard proxy whose {@code registerNewObjective} yields an
 * Objective proxy whose {@code getScore} yields a Score proxy. Set/reset
 * mutators are silent no-ops. Collection-returning methods return empty
 * immutable collections (matching Bukkit's "never null" contract).
 */
public final class ScoreboardStubs {

    private ScoreboardStubs() {}

    public static final Score SCORE = (Score) Proxy.newProxyInstance(
            ScoreboardStubs.class.getClassLoader(),
            new Class<?>[] { Score.class },
            (proxy, method, args) -> defaultValue(method.getReturnType()));

    public static final Team TEAM = (Team) Proxy.newProxyInstance(
            ScoreboardStubs.class.getClassLoader(),
            new Class<?>[] { Team.class },
            (proxy, method, args) -> defaultValue(method.getReturnType()));

    public static final Objective OBJECTIVE = (Objective) Proxy.newProxyInstance(
            ScoreboardStubs.class.getClassLoader(),
            new Class<?>[] { Objective.class },
            (proxy, method, args) -> {
                Class<?> rt = method.getReturnType();
                if (rt == Score.class) return SCORE;
                return defaultValue(rt);
            });

    public static final Scoreboard SCOREBOARD = (Scoreboard) Proxy.newProxyInstance(
            ScoreboardStubs.class.getClassLoader(),
            new Class<?>[] { Scoreboard.class },
            (proxy, method, args) -> {
                Class<?> rt = method.getReturnType();
                if (rt == Objective.class) return OBJECTIVE;
                if (rt == Team.class) return TEAM;
                if (rt == Score.class) return SCORE;
                return defaultValue(rt);
            });

    public static final ScoreboardManager MANAGER = (ScoreboardManager) Proxy.newProxyInstance(
            ScoreboardStubs.class.getClassLoader(),
            new Class<?>[] { ScoreboardManager.class },
            (proxy, method, args) -> {
                if (method.getReturnType() == Scoreboard.class) return SCOREBOARD;
                return defaultValue(method.getReturnType());
            });

    private static Object defaultValue(Class<?> rt) {
        if (rt == void.class) return null;
        if (rt.isPrimitive()) {
            if (rt == boolean.class) return Boolean.FALSE;
            if (rt == int.class) return 0;
            if (rt == long.class) return 0L;
            if (rt == short.class) return (short) 0;
            if (rt == byte.class) return (byte) 0;
            if (rt == float.class) return 0f;
            if (rt == double.class) return 0.0d;
            if (rt == char.class) return '\0';
            return null;
        }
        if (rt == java.util.List.class
                || rt == java.util.Collection.class
                || rt == Iterable.class) return Collections.emptyList();
        if (rt == java.util.Set.class) return Collections.emptySet();
        if (rt == java.util.Map.class) return Collections.emptyMap();
        return null;
    }
}
