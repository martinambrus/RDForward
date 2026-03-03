# E2E Test Version Coverage

## Versions WITH Tests (complete)

### RubyDung (1)
- rd-132211

### Alpha (8)
- 1.0.15, 1.0.16, 1.0.17, 1.1.0, 1.2.0, 1.2.2, 1.2.3, 1.2.6

### Beta (10)
- 1.0, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.7.3, 1.8.1, 1.9-pre5

### Release Pre-Netty (19)
- 1.0, 1.1, 1.2.1, 1.2.2, 1.2.3, 1.2.4, 1.2.5, 1.3.1, 1.3.2
- 1.4.2, 1.4.4, 1.4.5, 1.4.6, 1.4.7, 1.5.1, 1.5.2, 1.6.1, 1.6.2, 1.6.4

### Release Netty (57)
- 1.7.2-1.7.10, 1.8-1.8.9, 1.9-1.9.4
- 1.10-1.10.2, 1.11-1.11.2, 1.12-1.12.2
- 1.13-1.13.2, 1.14-1.14.4, 1.15-1.15.2

### Cross-version (6 combos)

**Total: 668 test classes, 97 baseline dirs**

---

## Versions NEEDING Tests (24 versions)

All need: ClientLauncher JAR URLs + launch methods, agent field mappings, test scenario classes.

### Release 1.19.x (5 versions)
| Version | Client JAR SHA1 | JAR URL |
|---------|----------------|---------|
| 1.19 | c0898ec7c6a5a2eaa317770203a1554260699994 | https://piston-data.mojang.com/v1/objects/c0898ec7c6a5a2eaa317770203a1554260699994/client.jar |
| 1.19.1 | 90d438c3e432add8848a9f9f368ce5a52f6bc4a7 | https://piston-data.mojang.com/v1/objects/90d438c3e432add8848a9f9f368ce5a52f6bc4a7/client.jar |
| 1.19.2 | 055b30d860ead928cba3849ba920c88b6950b654 | https://piston-data.mojang.com/v1/objects/055b30d860ead928cba3849ba920c88b6950b654/client.jar |
| 1.19.3 | 977727ec9ab8b4631e5c12839f064092f17663f8 | https://piston-data.mojang.com/v1/objects/977727ec9ab8b4631e5c12839f064092f17663f8/client.jar |
| 1.19.4 | 958928a560c9167687bea0cefeb7375da1e552a8 | https://piston-data.mojang.com/v1/objects/958928a560c9167687bea0cefeb7375da1e552a8/client.jar |

### Release 1.20.x (7 versions)
| Version | Client JAR SHA1 | JAR URL |
|---------|----------------|---------|
| 1.20 | e575a48efda46cf88111ba05b624ef90c520eef1 | https://piston-data.mojang.com/v1/objects/e575a48efda46cf88111ba05b624ef90c520eef1/client.jar |
| 1.20.1 | 0c3ec587af28e5a785c0b4a7b8a30f9a8f78f838 | https://piston-data.mojang.com/v1/objects/0c3ec587af28e5a785c0b4a7b8a30f9a8f78f838/client.jar |
| 1.20.2 | 82d1974e75fc984c5ed4b038e764e50958ac61a0 | https://piston-data.mojang.com/v1/objects/82d1974e75fc984c5ed4b038e764e50958ac61a0/client.jar |
| 1.20.3 | b178a327a96f2cf1c9f98a45e5588d654a3e4369 | https://piston-data.mojang.com/v1/objects/b178a327a96f2cf1c9f98a45e5588d654a3e4369/client.jar |
| 1.20.4 | fd19469fed4a4b4c15b2d5133985f0e3e7816a8a | https://piston-data.mojang.com/v1/objects/fd19469fed4a4b4c15b2d5133985f0e3e7816a8a/client.jar |
| 1.20.5 | c6b92b2374a629f20802bb284f98a4ee790e950a | https://piston-data.mojang.com/v1/objects/c6b92b2374a629f20802bb284f98a4ee790e950a/client.jar |
| 1.20.6 | 05b6f1c6b46a29d6ea82b4e0d42190e42402030f | https://piston-data.mojang.com/v1/objects/05b6f1c6b46a29d6ea82b4e0d42190e42402030f/client.jar |

### Release 1.21.x (12 versions)
| Version | Client JAR SHA1 | JAR URL |
|---------|----------------|---------|
| 1.21 | 0e9a07b9bb3390602f977073aa12884a4ce12431 | https://piston-data.mojang.com/v1/objects/0e9a07b9bb3390602f977073aa12884a4ce12431/client.jar |
| 1.21.1 | 30c73b1c5da787909b2f73340419fdf13b9def88 | https://piston-data.mojang.com/v1/objects/30c73b1c5da787909b2f73340419fdf13b9def88/client.jar |
| 1.21.2 | c7ac2d0d86f4ca416cab9064ff8a281852ad0c7b | https://piston-data.mojang.com/v1/objects/c7ac2d0d86f4ca416cab9064ff8a281852ad0c7b/client.jar |
| 1.21.3 | 6f67d19b4467240639cb2c368ffd4b94ba889705 | https://piston-data.mojang.com/v1/objects/6f67d19b4467240639cb2c368ffd4b94ba889705/client.jar |
| 1.21.4 | a7e5a6024bfd3cd614625aa05629adf760020304 | https://piston-data.mojang.com/v1/objects/a7e5a6024bfd3cd614625aa05629adf760020304/client.jar |
| 1.21.5 | b88808bbb3da8d9f453694b5d8f74a3396f1a533 | https://piston-data.mojang.com/v1/objects/b88808bbb3da8d9f453694b5d8f74a3396f1a533/client.jar |
| 1.21.6 | 740a125b83dd3447feaa3c5e891ead7fbb21ae28 | https://piston-data.mojang.com/v1/objects/740a125b83dd3447feaa3c5e891ead7fbb21ae28/client.jar |
| 1.21.7 | a2db1ea98c37b2d00c83f6867fb8bb581a593e07 | https://piston-data.mojang.com/v1/objects/a2db1ea98c37b2d00c83f6867fb8bb581a593e07/client.jar |
| 1.21.8 | a19d9badbea944a4369fd0059e53bf7286597576 | https://piston-data.mojang.com/v1/objects/a19d9badbea944a4369fd0059e53bf7286597576/client.jar |
| 1.21.9 | ce92fd8d1b2460c41ceda07ae7b3fe863a80d045 | https://piston-data.mojang.com/v1/objects/ce92fd8d1b2460c41ceda07ae7b3fe863a80d045/client.jar |
| 1.21.10 | d3bdf582a7fa723ce199f3665588dcfe6bf9aca8 | https://piston-data.mojang.com/v1/objects/d3bdf582a7fa723ce199f3665588dcfe6bf9aca8/client.jar |
| 1.21.11 | ba2df812c2d12e0219c489c4cd9a5e1f0760f5bd | https://piston-data.mojang.com/v1/objects/ba2df812c2d12e0219c489c4cd9a5e1f0760f5bd/client.jar |

## Protocol Version Reference (for agent mappings)
- 1.19: 759, 1.19.1-1.19.2: 760, 1.19.3: 761, 1.19.4: 762
- 1.20-1.20.1: 763, 1.20.2: 764, 1.20.3-1.20.4: 765, 1.20.5-1.20.6: 766
- 1.21-1.21.1: 767, 1.21.2-1.21.3: 768, 1.21.4: 769, 1.21.5: 770
- 1.21.6-1.21.7: 771, 1.21.8: 772, 1.21.9-1.21.10: 773, 1.21.11: 774
