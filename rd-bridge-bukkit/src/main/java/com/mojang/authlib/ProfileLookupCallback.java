// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.mojang.authlib;

/**
 * Mojang authlib stub. Real authlib calls into this when a profile
 * lookup against Mojang's service completes; RDForward has no such
 * service so the stub merely carries the signature.
 */
public interface ProfileLookupCallback {

    void onProfileLookupSucceeded(GameProfile profile);

    void onProfileLookupFailed(GameProfile profile, Exception exception);
}
