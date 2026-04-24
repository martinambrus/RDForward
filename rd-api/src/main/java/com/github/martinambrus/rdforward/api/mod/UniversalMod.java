package com.github.martinambrus.rdforward.api.mod;

/**
 * Mod that runs on both server and client. The loader invokes the
 * appropriate lifecycle callbacks on each side.
 */
public interface UniversalMod extends ServerMod, ClientMod {
}
