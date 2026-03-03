# Beta Protocol (v7-v17)

## Version Map
- Beta 1.0-1.1=v7, Beta 1.1_02-1.2_02=v8, Beta 1.3=v9, Beta 1.4=v10, Beta 1.5=v11, Beta 1.6=v12, Beta 1.6.1-1.6.6=v13, Beta 1.7-1.7_01=v13, Beta 1.7.2-1.7.3=v14, Beta 1.8-1.8.1=v17

## Protocol Number Clashes
- v7 clashes with Classic v7, v13 clashes with Alpha 1.0.15/Beta 1.7, v14 clashes with Alpha 1.0.16/Beta 1.7.3 — use `fromNumber(int, Family...)` for family-aware lookup.
- String16 detection in Handshake disambiguates Beta 1.5+ from Alpha: `detectedString16` flag in `AlphaConnectionHandler` -> `fromNumber(pv, Family.BETA)` for String16 clients, `fromNumber(pv, Family.ALPHA, Family.BETA)` otherwise.

## Phantom KeepAlive Trick (v8)
- Beta 1.2 changed ItemStack damage from byte to short. Since both 1.1_02 and 1.2 send v8, we can't distinguish them.
- S2C uses short damage (Beta 1.2 format) — older clients read byte damage and the trailing 0x00 is a valid zero-payload KeepAlive.
- C2S keeps byte damage (Beta 1.1_02 format) — Beta 1.2 clients' extra 0x00 is also a phantom KeepAlive. Works because damage is always 0 on this server.

## Packet Changes
- **BlockPlacement (0x0F)**: coordinates-first with conditional item data: `[int x, byte y, int z, byte dir, short itemId, if >=0: byte amount, byte damage]`. Uses `PlayerBlockPlacementPacketBeta`. C2S damage stays `byte` for phantom KeepAlive compatibility.
- **HoldingChange (0x10)**: just `[short slotId]` (2 bytes), no entityId. Uses `HoldingChangePacketBeta`.
- **SetSlot (0x67) S2C**: damage field is `short` (Beta 1.2 format). `SetSlotPacket` uses `writeShort(damage)`/`readShort()`.
- **EntityEquipment (0x05) S2C**: has `short damage` field after itemId. 10 bytes total.
- **PickupSpawn (0x15) C2S removed for Beta**: damage is in the MIDDLE of the packet, so phantom KeepAlive doesn't work. Beta clients use PlayerDigging status=4 for Q-drops instead.
- **0x05 C2S removed**: Alpha's PlayerInventory replaced by EntityEquipment S2C only.
- **AddToInventory (0x11) removed**: use SetSlot (0x67) to give items. `giveItem()` routes by version.
- **SetSlot replenishment**: per-placement `SetSlot(0, 36, item, 64, 0)` — no batched tracker needed.
- **Respawn (0x09)**: v7-v11 empty C2S packet; v12+ (Beta 1.6+) adds `byte dimension`. `RespawnPacketV12`.
- New C2S packets silently accepted: UseEntity (0x07), CloseWindow (0x65), WindowClick (0x66), ConfirmTransaction (0x6A), UpdateSign (0x82).
- **EntityAction (0x13) C2S**: `[int entityId, byte actionId]`. Registered for all Beta versions.
- **InputPacket (0x1B) C2S**: 18-byte vehicle steering input, silently consumed. Registered for all Beta versions.

## String Encoding Transition (v11/Beta 1.5)
- Changed from Java Modified UTF-8 (`writeUTF`: 2-byte byte count + UTF-8) to String16 (`writeString16`: 2-byte char count + UTF-16BE).
- Auto-detected in `HandshakeC2SPacket.readStringAuto()` by peeking at first byte after length prefix (0x00 = String16 for ASCII, non-zero = writeUTF).
- `McDataTypes.STRING16_MODE` ThreadLocal controls delegation. `RawPacketDecoder`/`RawPacketEncoder` set per-connection via `setUseString16()`.

## WindowClick (0x66) v11+
- Added `byte shift` after actionNumber (0=normal, 1=shift-click). Item damage changed from `byte` to `short`.
- `WindowClickPacketBeta15` extends `WindowClickPacket`, registered for v11+ via `betaV.getVersionNumber() >= 11`.

## Beta 1.6 (v12) Wire Changes
- Respawn (0x09) C2S/S2C gained `byte dimension`. Login format unchanged.
- ViaLegacy `Protocolb1_6_0_6Tob1_7_0_3` is empty (v12->v13 has zero wire changes).

## Beta 1.8 (v17) — Major Wire Format Overhaul
- **Creative mode NOT available until Beta 1.8 (v17)**. All Alpha and Beta 1.0-1.7 require survival-mode hacks.
- KeepAlive (0x00) gained `int keepAliveId` (was zero-payload).
- Login S2C gained `int gameMode` + `byte difficulty` + `byte worldHeight` + `byte maxPlayers`.
- Login C2S has NO "unused" String16 field (removed in v17).
- Respawn (0x09) expanded to `byte dimension + byte difficulty + byte gameMode + short worldHeight + long seed`.
- New C2S: CreativeSlot (0x6B) — 4 unconditional shorts = 8 bytes.
- **0xCA PlayerAbilities does NOT exist in Beta 1.8** — abilities from gameMode in Login/Respawn.
- `gameMode=1` in Login S2C enables instant break, creative inventory, flying, no fall damage.
- **Beta 1.8 Handshake format**: "username;host:port" — strip ";host:port" suffix.
- **Beta 1.8 server list ping (0xFE)**: Respond with 0xFF + String16("motd§players§max") and close.
- **Beta 1.8 BlockPlacement damage is `short`**: `PlayerBlockPlacementPacketV17` has `short damage` (not `byte`).
- **Tick-loop PingPacket must be dropped for v17+**: `dropPing` flag on translator suppresses this.
