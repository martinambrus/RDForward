// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.mojang.authlib.properties;

/**
 * Mojang authlib stub. Real authlib's {@code Property} represents
 * a signed key/value pair attached to a {@code GameProfile}
 * (textures, signature, etc.). RDForward has no signing path;
 * the stub carries name + value + optional signature for plugins
 * that call accessors. Signature verification is out of scope.
 */
public class Property {

    private final String name;
    private final String value;
    private final String signature;

    public Property(String name, String value) {
        this(name, value, null);
    }

    public Property(String name, String value, String signature) {
        this.name = name;
        this.value = value;
        this.signature = signature;
    }

    public String getName() { return name; }
    public String getValue() { return value; }
    public String getSignature() { return signature; }
    public boolean hasSignature() { return signature != null; }
    /** Real authlib verifies a Yggdrasil-key-signed property; the
     *  stub accepts everything since RDForward has no auth chain. */
    public boolean isSignatureValid(java.security.PublicKey key) { return true; }
}
