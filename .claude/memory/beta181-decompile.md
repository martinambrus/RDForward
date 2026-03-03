# Beta 1.8.1 Decompilation Mappings

JAR: `rd-e2e/libs/beta-1.8.1-client.jar` (Mojang CDN sha1: 6b562463ccc2c7ff12ff350a2b04a67b3adcd37b)
Decompile: CFR at `/tmp/cfr.jar` -> `/tmp/decompiled_b1.8.1/`
Agent mappings: `BetaV17Mappings.java`

## Class Name Mapping

| MCP Name | Alpha (v6) | Beta 1.8.1 (v17) |
|---|---|---|
| Entity | lw | kj |
| EntityLiving | - | wd |
| EntityPlayer | eb | sz |
| EntityPlayerSP | bq/mp | qs |
| EntityClientPlayerMP | - | aan |
| World | cy | rv |
| GuiScreen | bp | qr |
| GuiContainer | - | em |
| GuiIngame | nl | abj |
| GuiInventory | ne | abd |
| GuiContainerCreative | N/A | on |
| PlayerController | - | hw (abstract) |
| PlayerControllerCreative | - | yf |
| GameSettings | gq | fv |
| KeyBinding | - | ys |
| InventoryPlayer | fo | ui |
| ItemStack | fp | ul |
| MovementInput | nn | lp |
| MovementInputFromOptions | he | gh |
| ChatLine | me | kq |
| Container | - | cf |

## Field/Method Mapping

### Minecraft class (net.minecraft.client.Minecraft)

| Concept | Alpha | Beta 1.8.1 |
|---|---|---|
| tick method | i() | k() |
| thePlayer | g | h |
| theWorld | e | f |
| serverHost | V | ac |
| serverPort | W | ad |
| displayWidth | c | d |
| displayHeight | d | e |
| currentScreen | p | r |
| ingameGUI | u | v |
| mouseGrabbed | L | O |
| gameSettings | y | z |
| clickMethod | a(int) | a(int) |
| clickCooldown | S | X |
| playerController | b | c |
| displayGuiScreen | a(bp) | a(qr) |

### Entity (kj)

| Concept | Alpha (lw) | Beta (kj) |
|---|---|---|
| posX | aw | o |
| posY | ax | p |
| posZ | ay | q |
| yaw | aC | u |
| pitch | aD | v |
| onGround | aH | z |

### EntityPlayerSP

| Concept | Alpha | Beta |
|---|---|---|
| movementInput | a | a |
| sendChatMessage | a(String) | a(String) |

### EntityPlayer (sz)

| Concept | Alpha (eb) | Beta (sz) |
|---|---|---|
| inventory | e | as |
| dropPlayerItem | a(fp,boolean) | a(ul,boolean) |

### InventoryPlayer (ui)

| Concept | Alpha (fo) | Beta (ui) |
|---|---|---|
| mainInventory | a | a |
| currentItem | d | c |
| cursorItem | e | f (private) |
| craftingInventory | c (ItemStack[]) | N/A (Container system) |

### ItemStack (ul)

| Concept | Alpha (fp) | Beta (ul) |
|---|---|---|
| itemID | c | c |
| stackSize | a | a |

### GuiScreen (qr)

| Concept | Alpha (bp) | Beta (qr) |
|---|---|---|
| width | c | m |
| height | d | n |

### GuiIngame (abj)

| Concept | Alpha (nl) | Beta (abj) |
|---|---|---|
| chatLines | e | f |

### Movement Input

Alpha has boolean[10] array (`he.f`). Beta removed it — uses KeyBinding.pressed on GameSettings.

GameSettings (fv) key bindings:
- m = forward (key 17/W)
- n = left (key 30/A)
- o = back (key 31/S)
- p = right (key 32/D)
- q = jump (key 57/Space)
- u = sneak (key 42/LShift)

KeyBinding (ys): field `e` = pressed (boolean)

### Creative Inventory

- Creative screen: `on` (GuiContainerCreative), extends `em` (GuiContainer)
- Constructor: `on(sz)` — takes EntityPlayer
- Survival screen: `abd` (GuiInventory), constructor `abd(qs)` — takes EntityPlayerSP
- `on.p_()` checks `this.l.c.h()` (isCreative) — redirects to survival if not creative
