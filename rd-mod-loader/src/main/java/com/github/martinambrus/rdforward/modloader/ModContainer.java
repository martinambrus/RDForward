package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.mod.ClientMod;
import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import com.github.martinambrus.rdforward.api.mod.Reloadable;
import com.github.martinambrus.rdforward.api.mod.ServerMod;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Runtime wrapper for a single loaded mod: descriptor, jar path, owning
 * classloader, entrypoint instances, and current {@link ModState}. One
 * {@code ModContainer} exists per mod id for the life of the server
 * (state cycles through as the mod is enabled / reloaded / disabled).
 */
public final class ModContainer {

    private final ModDescriptor descriptor;
    private final Path jarPath;

    private ModClassLoader classLoader;
    private Object serverInstance;
    private Object clientInstance;
    private volatile ModState state = ModState.DISCOVERED;
    private Throwable lastError;

    public ModContainer(ModDescriptor descriptor, Path jarPath) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.jarPath = Objects.requireNonNull(jarPath, "jarPath");
    }

    public ModDescriptor descriptor() { return descriptor; }
    public Path jarPath() { return jarPath; }
    public ModClassLoader classLoader() { return classLoader; }
    public ModState state() { return state; }
    public Throwable lastError() { return lastError; }

    public String id() { return descriptor.id(); }

    public void setClassLoader(ModClassLoader loader) { this.classLoader = loader; }

    public void setServerInstance(Object instance) { this.serverInstance = instance; }
    public void setClientInstance(Object instance) { this.clientInstance = instance; }

    /** @return the server entrypoint, or {@code null} if the mod is client-only or not yet loaded. */
    public ServerMod serverMod() {
        return serverInstance instanceof ServerMod sm ? sm : null;
    }

    /** @return the client entrypoint, or {@code null} if the mod is server-only or not yet loaded. */
    public ClientMod clientMod() {
        return clientInstance instanceof ClientMod cm ? cm : null;
    }

    /** @return the entrypoint that implements {@link Reloadable}, or {@code null} if none. */
    public Reloadable reloadable() {
        if (serverInstance instanceof Reloadable r) return r;
        if (clientInstance instanceof Reloadable r) return r;
        return null;
    }

    public void setState(ModState newState) {
        this.state = Objects.requireNonNull(newState, "newState");
    }

    public void fail(Throwable error) {
        this.lastError = error;
        this.state = ModState.ERROR;
    }
}
