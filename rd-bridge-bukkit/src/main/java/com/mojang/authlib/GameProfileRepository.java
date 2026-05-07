// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.mojang.authlib;

/**
 * Mojang authlib stub. Real authlib's {@code GameProfileRepository}
 * queries Mojang's profile service for UUID/name lookups. RDForward
 * has no authlib runtime; this stub interface exists so plugins
 * (Citizens 2.0.42 NMS clinit) can declare fields and method
 * signatures referencing it without {@link NoClassDefFoundError}.
 * Calls into a stub instance no-op via
 * {@link com.github.martinambrus.rdforward.api.stub.StubCallLog}.
 */
public interface GameProfileRepository {

    void findProfilesByNames(String[] names, Agent agent, ProfileLookupCallback callback);
}
