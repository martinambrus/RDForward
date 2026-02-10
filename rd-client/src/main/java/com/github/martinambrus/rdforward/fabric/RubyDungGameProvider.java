package com.github.martinambrus.rdforward.fabric;

import net.fabricmc.loader.impl.game.GameProvider;
import net.fabricmc.loader.impl.game.GameProviderHelper;
import net.fabricmc.loader.impl.game.patch.GameTransformer;
import net.fabricmc.loader.impl.launch.FabricLauncher;
import net.fabricmc.loader.impl.metadata.BuiltinModMetadata;
import net.fabricmc.loader.impl.util.Arguments;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Fabric Loader GameProvider for RubyDung (rd-132211).
 *
 * This tells Fabric Loader how to discover, set up, and launch the game.
 * Registered via META-INF/services/net.fabricmc.loader.impl.game.GameProvider.
 */
public class RubyDungGameProvider implements GameProvider {

    private static final String GAME_ID = "rubydung";
    private static final String GAME_NAME = "RubyDung";
    private static final String GAME_VERSION = "rd-132211";
    private static final String ENTRYPOINT = "com.mojang.rubydung.RubyDung";

    private static final GameTransformer TRANSFORMER = new GameTransformer();

    private Arguments arguments;
    private Path gameJar;
    private final List<Path> gameClassPath = new ArrayList<>();

    @Override
    public String getGameId() {
        return GAME_ID;
    }

    @Override
    public String getGameName() {
        return GAME_NAME;
    }

    @Override
    public String getRawGameVersion() {
        return GAME_VERSION;
    }

    @Override
    public String getNormalizedGameVersion() {
        return GAME_VERSION;
    }

    @Override
    public Collection<BuiltinMod> getBuiltinMods() {
        List<BuiltinMod> mods = new ArrayList<>();

        BuiltinModMetadata.Builder gameMetadata =
            new BuiltinModMetadata.Builder(GAME_ID, getNormalizedGameVersion())
                .setName(GAME_NAME)
                .setDescription("Notch's RubyDung prototype (2009)");

        mods.add(new BuiltinMod(gameClassPath, gameMetadata.build()));
        return mods;
    }

    @Override
    public String getEntrypoint() {
        return ENTRYPOINT;
    }

    @Override
    public Path getLaunchDirectory() {
        return Paths.get(".");
    }

    @Override
    public boolean isObfuscated() {
        return false;
    }

    @Override
    public boolean requiresUrlClassLoader() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean locateGame(FabricLauncher launcher, String[] args) {
        this.arguments = new Arguments();
        this.arguments.parse(args);

        // Find the game classes on the classpath by looking for the main class
        Optional<Path> source = GameProviderHelper.getSource(
            launcher.getTargetClassLoader(), ENTRYPOINT.replace('.', '/') + ".class");

        if (!source.isPresent()) {
            return false;
        }

        this.gameJar = source.get();
        this.gameClassPath.add(gameJar);
        return true;
    }

    @Override
    public void initialize(FabricLauncher launcher) {
        TRANSFORMER.locateEntrypoints(launcher, gameClassPath);
    }

    @Override
    public GameTransformer getEntrypointTransformer() {
        return TRANSFORMER;
    }

    @Override
    public void unlockClassPath(FabricLauncher launcher) {
        for (Path path : gameClassPath) {
            launcher.addToClassPath(path);
        }
    }

    @Override
    public void launch(ClassLoader loader) {
        try {
            Class<?> mainClass = loader.loadClass(ENTRYPOINT);
            Method mainMethod = mainClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) arguments.toArray());
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Game crashed", e.getCause());
        } catch (Exception e) {
            throw new RuntimeException("Failed to launch " + GAME_NAME, e);
        }
    }

    @Override
    public Arguments getArguments() {
        return arguments;
    }

    @Override
    public String[] getLaunchArguments(boolean sanitize) {
        return arguments != null ? arguments.toArray() : new String[0];
    }

    @Override
    public boolean hasAwtSupport() {
        return true;
    }
}
