# RDForward

From RubyDung to Minecraft. An upgraded multiplayer MC experience, compatible with all publicly available MC versions. Using Fabric mod loader and adding multiplayer and mods to everything. Not affiliated with Mojang or Microsoft in any way.

## Project Structure

This is a Gradle multi-module project:

| Module | Description |
|--------|-------------|
| `rd-api` | Modding API — event system, mod lifecycle, extension points (migrated from [RDApi](https://github.com/martinambrus/RDApi)) |
| `rd-protocol` | Networking — Netty-based packet codec, protocol versioning, cross-version block/action translation |
| `rd-world` | World persistence — Alpha-format NBT serialization, block registry |
| `rd-client` | Game client — RubyDung renderer with multiplayer support |
| `rd-server` | Dedicated server — authoritative world state, tick loop, mod event dispatch |

## Key Design Decisions

- **Netty** for networking (same library modern MC uses) — scalable to 100+ players
- **Capability-based protocol negotiation** — older clients can connect to newer servers
- **Alpha-compatible world format** — worlds can be opened in Minecraft Alpha
- **Fabric Loader + SpongePowered Mixin** for mod loading and code injection
- **ViaVersion-inspired version translation** — block/action mapping between protocol versions

## Building

Requires Java 8+ and Gradle 8.5+.

```bash
./gradlew build
```

## License

MIT License — see [LICENSE](LICENSE) for details.
