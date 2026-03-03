# E2E Screenshot Fixes (Feb 2026)

## Issues Found During Visual Audit

### 1. Column build back_on_ground.png cobble remnant
- **Affected**: alpha110, alpha120, alpha123, beta181
- **Cause**: BreakDownStep doesn't fully remove column blocks before player lands. breakBlock() calls PlayerController.dig() which only instant-breaks in creative mode.
- **Fix**: Use `gs.offsetPlayerX(-5.0)` in VerifyGroundStep (settleTicks==0) to nudge player away from column before screenshot. Uses direct posX field write (not forcePlayerPosition) to avoid Entity.a()→move() issues on Alpha 1.2.2+. Added `offsetPlayerX(double)` to GameState.java.
- **Status**: Testing third iteration of fix. alpha120/alpha110 confirmed working with forcePlayerPosition approach. alpha123/beta181 needed direct field write approach.

### 2. beta10 inventory_complete.png - 64 cobble in 1 slot
- **User says**: Should show cobble split into 2 stacks of 32 across 2 slots.
- **Current**: Shows world with 64 cobble in slot 1 (server replenished to 64).
- **Investigation needed**: The scenario's final step expects 64 total (replenished). User may expect split state.

### 3. beta15/beta16 inventory_split.png - floating cobble
- **Cause**: VerifySplitStep only waits 5 ticks before screenshot. The 32-stack placed via left-click hasn't visually landed in the slot yet.
- **Fix needed**: Increase wait time in VerifySplitStep or verify item is in correct slot before screenshot.

### 4. beta18 inventory_complete.png - empty inventory
- **Current**: Creative mode throws all cobble, no replenishment, so 0 cobble = empty hotbar.
- **May be expected behavior** for creative mode.

### 5. All alpha/beta cross tests - wrong camera direction
- **Issue**: Player faces ground instead of other player. RD cross test is correct (faces other player).
- **Fix needed**: Add setLookDirection to face the other player before screenshot in cross-test scenario.

### 6. RD column_build missing back_on_ground.png
- **Check**: Does the RD path in VerifyGroundStep reach the screenshot capture?

### 7. RD environment_check.png - incomplete map
- **Issue**: Shows same as world_loaded.png (not fully rendered). Needs more wait time.

## Key Files
- `rd-e2e-agent/src/main/java/.../scenario/ColumnBuildScenario.java` - column build fixes
- `rd-e2e-agent/src/main/java/.../scenario/InventoryManipulationScenario.java` - inventory timing
- `rd-e2e-agent/src/main/java/.../InputController.java` - prevRotation fix, breakBlock, closeContainer
- `rd-e2e-agent/src/main/java/.../GameState.java` - forcePlayerPosition, getBlockId
