# Beta E2E Field Mappings

## JAR Sources
- Primary: Mojang CDN (URLs in ClientLauncher.java constants)
- Backup: MultiMC at `/mnt/c/Users/Riman/Downloads/MultiMC/libraries/com/mojang/minecraft/`
  - JAR naming: `minecraft-<version>-client.jar` (e.g., `minecraft-b1.7.3-client.jar`)
- Minecraft Wiki: Each version page has client/server JAR download links (e.g., https://minecraft.wiki/w/Java_Edition_Beta_1.7.3)
- Decompiler: CFR at `/tmp/cfr.jar` (download from https://github.com/leibnitz27/cfr/releases)
- Decompiled sources cached at `/tmp/beta-decompiled/<version>/`

## Mappings Summary (differences from Beta 1.8.1 / BetaV17Mappings)

All pre-1.8 Beta versions:
- Use boolean[] pressedKeys (field "f" on MovementInputFromOptions), NOT KeyBinding.pressed
- No creative mode / creative inventory
- GuiScreen width/height are c/d (not m/n)

### Beta 1.7.3 (v14) / Beta 1.7 (v13) - BetaV14Mappings / BetaV13Mappings
- tick=k, player=h, world=f, serverHost=ab, serverPort=ac
- posX/Y/Z=aM/aN/aO, yaw=aS, pitch=aT, onGround=aX
- inventory=c, mouseGrabbed=N, clickCooldown=W, session=k
- dropOneItem=D, sneakKey=v, guiScreen=da, guiInventory=ue
- closeContainer=r

### Beta 1.6 (v12) - BetaV12Mappings (extends BetaV14)
- posX/Y/Z=aL/aM/aN, yaw=aR, pitch=aS, onGround=aW
- guiScreen=cy, guiInventory=tz, dropOneItem=D (same)
- closeContainer=r (inherited from BetaV14)

### Beta 1.5_01 (v11) - BetaV11Mappings
- tick=j, player=g, world=e, serverHost=Y, serverPort=Z
- posX/Y/Z=aK/aL/aM, yaw=aQ, pitch=aR, onGround=aV
- gameSettings=y, inventory=f, mouseGrabbed=L, clickCooldown=T
- currentScreen=q, ingameGui=u, session=j, playerController=b
- dropOneItem=E, sneakKey=v, guiScreen=cs, guiInventory=ta
- closeContainer=r

### Beta 1.4_01 (v10) - BetaV10Mappings (extends BetaV11)
- posX/Y/Z=aJ/aK/aL, yaw=aP, pitch=aQ, onGround=aU
- keys shifted: forward=l,left=m,back=n,right=o,jump=p,sneak=u
- dropOneItem=D, guiScreen=cf, guiInventory=qm
- closeContainer=q (NOT r like parent)

### Beta 1.3_01 (v9) - BetaV9Mappings
- tick=j, player=g, world=e, serverHost=W, serverPort=X
- posX/Y/Z=aI/aJ/aK, yaw=aO, pitch=aP, onGround=aT
- gameSettings=x, inventory=f, mouseGrabbed=J, clickCooldown=R
- currentScreen=q, ingameGui=t, session=j, playerController=b
- sendChatMessage=b (not a!), dropOneItem=A
- keys: forward=l,left=m,back=n,right=o,jump=p,sneak=u
- guiScreen=cb, guiInventory=pw
- closeContainer=p

### Beta 1.2_02 (v8) - BetaV8Mappings
- tick=i, player=g, world=e, serverHost=V, serverPort=W
- posX/Y/Z=aF/aG/aH, yaw=aL, pitch=aM, onGround=aQ
- gameSettings=y, inventory=f, mouseGrabbed=L, clickCooldown=S
- currentScreen=p, ingameGui=u, session=i, playerController=b
- dropOneItem=z, keys: forward=k,left=l,back=m,right=n,jump=o,sneak=t
- guiScreen=by, guiInventory=ov
- closeContainer=p

### Beta 1.0 (v7) - BetaV7Mappings (extends BetaV8)
- posX/Y/Z=aG/aH/aI, yaw=aM, pitch=aN, onGround=aR
- dropOneItem=w, guiScreen=br, guiInventory=ns
- closeContainer=m (NOT p like parent)

## CloseContainer Method Summary
EntityClientPlayerMP close-container method sends Packet101CloseWindow to server:
- v14/v13/v12/v11: r()
- v10: q()
- v9/v8: p()
- v7: m()
Added to FieldMappings as closeContainerMethodName(). InputController.closeScreen() calls
player.closeContainer() when non-null, otherwise falls back to displayGuiScreen(null).
