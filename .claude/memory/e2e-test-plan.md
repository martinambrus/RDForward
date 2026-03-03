# E2E Test Plan — First Server Version

This is the manual/automated test sequence for validating each client version against the RDForward server.

## Pre-Test Setup

- Stop server, delete all map and player data
- Start fresh server (can run 2 servers on different ports for parallel testing — split clients into two halves)
- Each parallel tester has its own client set

## Single-Client Tests (Steps 0-21)

Performed by the first client logged in. Screenshot after every step unless noted.

### Login & Environment
0. Check chat for "joined the game" message with matching username
1. Verify standing on grass block, air in all directions (5 blocks each way: front/back/left/right/top)
2. Verify all chunks loaded, flat map visible
3. **Survival-only clients (pre-Beta 1.8)**: Verify 64 cobblestone in inventory
4. **Alpha a1.0.15 / a1.0.16 only**: Verify all other inventory slots filled with cooked porkchops

### Block Breaking & Placement
5. Remove grass block to the right, wait 4s, verify it's still removed (server accepted destroy)
6. Place cobblestone where grass was removed, wait 4s, verify it's placed AND converted to grass
7. **Survival-only**: Verify inventory has 64 cobblestone stack (replenishment works)
8. Place cobblestone next to you to the right, wait 4s, verify it's placed

### Column Building (Jump + Place)
9. Jump on the cobblestone, look down, hold jump + right-click until max height (max 20s timeout), wait 4s, verify at map height with cobblestone below feet
10. Hold jump + right-click for 5s more at max height, stop, wait 4s, verify still at max height (validates above-map-height placement rejected)

### Block Breaking (Column Tear-Down)
11. Hold left-click until block under feet is grass, wait 4s, verify grass below feet (validates sequential block breaking)

### Inventory & Drops
12. **Survival-only**: Select 64 cobblestone stack, press Q, verify stack stays at 64
13. Press T, type "HELLO", Enter — verify chat shows your name + message

### Inventory Manipulation
14. Open inventory, click empty slot (survival: consume porkchops to free slots first) — validates no crash on right-click empty slot
15. (Creative: grab 64 cobblestone to 2nd quick slot) Right-click 64-stack, place 32 on next available slot — verify 2x32 stacks
16. Right-click 32-stack, throw 16 outside inventory — Alpha: verify 64 total cobblestone (immediate replenish); Others: verify 1x32 + 1x16
17. Right-click 32-stack, right-click outside to throw 1 block, left-click back on origin slot — Alpha: verify 64 total; Others: verify 1x31 + 1x16
18. Close inventory — Beta: verify only 1x64 cobblestone stack and nothing else; Alpha: verify 64 total cobblestone

### Edge-of-Map & Respawn
19. Walk/fly to map edge (detect non-air 10 blocks ahead), walk into void, verify falling at 10+ blocks/sec, when below Y=-10 wait 4s, verify teleported back to spawn with grass below feet

### Creative Block Placement Grid (Creative Mode Only)
20. Place 1 block from each inventory slot in a row (max 80 per row, then start new row 3 blocks behind), verify each block converts to cobblestone, non-block items disappear within 4s (some items do nothing or trigger actions — that's OK). After all placed: walk to start, face placed blocks, jump onto first block, screenshot.

### Return to Spawn
21. Return to spawn point

## Multi-Client Tests (Steps 22-31)

After single-client tests, test interaction with every other available client version.

### Setup
22. Initialize internal counter at 1
23. Start next client from the full test list, offline mode, username "Tester X" (X = counter, increment after start), log into same server

### Player Visibility
24. Walk 3 steps back, rotate to face the other player

### Chat Exchange
25. Client 2 presses T, types "HERE I AM", Enter — verify both clients see the message (2 screenshots, low similarity threshold for username variance)
26. Client 1 presses T, types "I SEE YOU", Enter — verify both clients see the message (2 screenshots)

### Block Sync
27. Client 1 places block to the right — verify both clients see the placed block (2 screenshots)
28. Client 1 destroys that block — verify both clients see block removed (2 screenshots)
29. Client 2 places block to the right — verify both clients see the placed block (2 screenshots)
30. Client 2 destroys that block — verify both clients see block removed (2 screenshots)

### Cleanup
31. Close client 2 (server auto-logout)

### Repeat
Repeat steps 23-31 for every remaining client in the test list.

## Mapping to rd-bot Test Scenarios

| Test Plan Step | rd-bot Test Class(es) | Layer |
|----------------|----------------------|-------|
| 0 (join chat) | AlphaLoginTest, BetaLoginTest, NettyLoginTest, ChatTest | Bot |
| 1-2 (environment) | SpawnPositionTest, CrossVersionSpawnTest | Bot |
| 3-4 (inventory) | PorkchopInventoryTest, AlphaLoginTest (cobblestone) | Bot |
| 5-6 (break/place + grass conversion) | BlockPlaceTest, GrassConversionTest | Bot |
| 7 (replenishment) | CobblestoneReplenishmentTest | Bot |
| 8-10 (column build) | ColumnBuildTest, WorldHeightLimitTest | Bot |
| 11 (column tear-down) | BlockBreakTest | Bot |
| 12 (Q-drop) | QDropTest (11 tests: survival replenishment + creative no-change) | Bot |
| 13 (chat) | ChatTest, CrossVersionChatTest | Bot |
| 14-18 (inventory manipulation) | InventoryClickTest (Beta v14), InventoryNettyTest (v47) | Bot (limited) + E2E |
| 19 (void fall + respawn) | VoidFallRespawnTest, VoidFallSpawnValidationTest | Bot |
| 20 (creative block grid) | CreativeBlockPlacementTest (protocol-level) | Bot + E2E (visual) |
| 23-31 (multi-player) | MultiPlayerTest, BlockSyncTest, CrossVersionChatTest | Bot |

## Notes
- Steps 14-18 (inventory manipulation) are partially covered by rd-bot for Beta v14 and Netty v47 via WindowClick packets. v393+ inventory tests are not feasible because ConfirmTransaction was removed in 1.17, and v393+ slot formats differ enough to require per-version handling. Full GUI-level testing requires rd-e2e (Java agent controlling real client).
- Step 20 (creative block grid) visual verification needs rd-e2e for screenshot comparison. The protocol-level aspect (block conversion, non-block rejection) is covered by CreativeBlockPlacementTest.
- All other steps are fully testable via rd-bot protocol-level assertions.
- Additional test classes beyond the plan: BedrockLoginTest, BedrockChatTest, BedrockBlockTest, BedrockCrossVersionTest, AdminCommandTest, TimeWeatherTest, JoinLeaveBroadcastTest, PlayerDespawnTest, CrossVersionBlockTest, CrossVersionVisibilityTest.
