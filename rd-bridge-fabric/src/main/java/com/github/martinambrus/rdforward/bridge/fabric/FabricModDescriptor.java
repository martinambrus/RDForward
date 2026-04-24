package com.github.martinambrus.rdforward.bridge.fabric;

import java.util.List;
import java.util.Map;

/**
 * Parsed view of a {@code fabric.mod.json}. Only the fields the bridge
 * understands are modelled; loader features we don't honor (mixin config
 * wiring, jar-in-jar, language adapters) are silently ignored.
 *
 * @param id            unique mod id; matches Fabric's own naming
 * @param version       mod version string
 * @param name          display name (may be blank)
 * @param description   human description (may be blank)
 * @param authors       list of author names
 * @param mainEntrypoints FQCNs of {@code net.fabricmc.api.ModInitializer} entries
 * @param serverEntrypoints FQCNs of {@code net.fabricmc.api.DedicatedServerModInitializer} entries
 * @param clientEntrypoints FQCNs of {@code net.fabricmc.api.ClientModInitializer} entries
 * @param dependencies hard dependencies (modId -&gt; Fabric version predicate)
 * @param environment   {@code "*"}, {@code "client"}, or {@code "server"} — the
 *                      loader uses this plus its own deployment mode (server vs
 *                      client) to decide whether the mod is eligible to load
 */
public record FabricModDescriptor(
        String id,
        String version,
        String name,
        String description,
        List<String> authors,
        List<String> mainEntrypoints,
        List<String> serverEntrypoints,
        List<String> clientEntrypoints,
        Map<String, String> dependencies,
        String environment
) {}
