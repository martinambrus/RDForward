// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.fabric.api.client.particle.v1;

/**
 * Noop stub: RDForward has no particle pipeline, so {@link #register}
 * accepts any arguments without doing work. Keeps Fabric client mods that
 * register particle factories compiling and running.
 *
 * <p>Upstream Fabric exposes a generic-heavy API around
 * {@code ParticleType<T>} and {@code PendingParticleFactory<T>}; since
 * neither RDForward type exists, this stub takes raw {@link Object}s.
 */
public final class ParticleFactoryRegistry {

    private static final ParticleFactoryRegistry INSTANCE = new ParticleFactoryRegistry();

    private ParticleFactoryRegistry() {}

    /** @return the singleton instance, matching Fabric's API shape. */
    public static ParticleFactoryRegistry getInstance() {
        return INSTANCE;
    }

    /** Noop — RDForward does not render particles. */
    public void register(Object particleType, Object factory) {
        // intentional noop
    }

    /** Noop — alternate Fabric signature. */
    public void register(Object particleType, Object factory, Object sprites) {
        // intentional noop
    }
}
