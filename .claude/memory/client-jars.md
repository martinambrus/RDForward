# Minecraft Client JAR Download URLs

All URLs are from Mojang's official CDN (`piston-data.mojang.com`) unless noted otherwise.

## Alpha

| Version | Protocol | Client JAR URL |
|---------|----------|----------------|
| Alpha v1.0.15 | v13 | `https://piston-data.mojang.com/v1/objects/03edaff812bedd4157a90877e779d7b7ecf78e97/client.jar` |
| Alpha v1.0.16 | v14 | `https://piston-data.mojang.com/v1/objects/98ce80c7630ccb3bb38687ff98bfd18935d49a57/client.jar` |
| Alpha v1.0.17_04 | v1 | `https://piston-data.mojang.com/v1/objects/61cb4c717981f34bf90e8502d2eb8cf2aa6db0cd/client.jar` |
| Alpha v1.1.0 | v2 | `https://piston-data.mojang.com/v1/objects/d58d1db929994ff383bdbe6fed31887e04b965c3/client.jar` |
| Alpha v1.2.0_02 | v3 | `https://piston-data.mojang.com/v1/objects/b99da0a683e6dc1ade4df1bf159e021ad07d4fca/client.jar` |
| Alpha v1.2.2 | v4 | `https://piston-data.mojang.com/v1/objects/7d9d85eaca9627d3a40e6d122182f2d22d39dbf9/client.jar` |
| Alpha v1.2.3 | v5 | `https://piston-data.mojang.com/v1/objects/f4be258122cb62208b350cd2068685ad859bb447/client.jar` |
| Alpha v1.2.6 | v6 | `https://piston-data.mojang.com/v1/objects/a68c817afd6c05c253ba5462287c2c19bbb57935/client.jar` |

Note: Alpha v1.0.16_02 is only on OmniArchive. Use v1.0.16 (same protocol v14) instead.
Note: Alpha v1.2.0 is only on OmniArchive. Use v1.2.0_02 (same protocol v3) instead.

## Beta

| Version | Protocol | Client JAR URL |
|---------|----------|----------------|
| Beta 1.0 | v7 | `https://piston-data.mojang.com/v1/objects/93faf3398ebf8008d59852dc3c2b22b909ca8a49/client.jar` |
| Beta 1.1_02 | v8 | `https://piston-data.mojang.com/v1/objects/e1c682219df45ebda589a557aadadd6ed093c86c/client.jar` |
| Beta 1.3_01 | v9 | `https://piston-data.mojang.com/v1/objects/add3809d2c075e985d4b583632dac3d9c3872945/client.jar` |
| Beta 1.4 | v10 | `https://piston-data.mojang.com/v1/objects/f6dbca5223ea2a7e89806e93d0b18162b2d58c20/client.jar` |
| Beta 1.5 | v11 | `https://piston-data.mojang.com/v1/objects/f5ce1699cd728213c21054fa2f1490d162b002b4/client.jar` |
| Beta 1.6 | v12 | `https://piston-data.mojang.com/v1/objects/ecc0288d218fd7479027a17c150cbf283fa950a1/client.jar` |
| Beta 1.7 | v13 | `https://piston-data.mojang.com/v1/objects/ad7960853437bcab86bd72c4a1b95f6fe19f4258/client.jar` |
| Beta 1.7.3 | v14 | `https://piston-data.mojang.com/v1/objects/43db9b498cb67058d2e12d394e6507722e71bb45/client.jar` |
| Beta 1.8.1 | v17 | `https://piston-data.mojang.com/v1/objects/6b562463ccc2c7ff12ff350a2b04a67b3adcd37b/client.jar` |

## Release

| Version | Protocol | Client JAR URL |
|---------|----------|----------------|
| Release 1.0.0 | v22 | `https://piston-data.mojang.com/v1/objects/b679fea27f2284836202e9365e13a82552092e5d/client.jar` |
| Release 1.1 | v23 | `https://piston-data.mojang.com/v1/objects/f690d4136b0026d452163538495b9b0e8513d718/client.jar` |
| Release 1.2.1 | v28 | `https://piston-data.mojang.com/v1/objects/c7662ac43dd04bfd677694a06972a2aaaf426505/client.jar` |
| Release 1.2.5 | v29 | `https://piston-data.mojang.com/v1/objects/4a2fac7504182a97dcbcd7560c6392d7c8139928/client.jar` |
| Release 1.3.2 | v39 | `https://piston-data.mojang.com/v1/objects/c2efd57c7001ddf505ca534e54abf3d006e48309/client.jar` |
| Release 1.4.2 | v47 | `https://piston-data.mojang.com/v1/objects/42d6744cfbbd2958f9e6688dd6e78d86d658d0d4/client.jar` |
| Release 1.4.5 | v49 | `https://piston-data.mojang.com/v1/objects/7a8a963ababfec49406e1541d3a87198e50604e5/client.jar` |
| Release 1.4.7 | v51 | `https://piston-data.mojang.com/v1/objects/53ed4b9d5c358ecfff2d8b846b4427b888287028/client.jar` |
| Release 1.5.2 | v61 | `https://piston-data.mojang.com/v1/objects/465378c9dc2f779ae1d6e8046ebc46fb53a57968/client.jar` |
| Release 1.6.1 | v73 | `https://piston-data.mojang.com/v1/objects/17e2c28fb54666df5640b2c822ea8042250ef592/client.jar` |
| Release 1.6.4 | v78 | `https://piston-data.mojang.com/v1/objects/1703704407101cf72bd88e68579e3696ce733ecd/client.jar` |
| Release 1.7.2 | v4 | `https://piston-data.mojang.com/v1/objects/0c8689f904922af71c7144dcfb81bce976cadd49/client.jar` |

## Notes
- All URLs use Mojang's official CDN and should be stable long-term
- Client JARs are the same files used by the official Minecraft Launcher
- For rd-bot (protocol-level tests), client JARs are NOT needed — bots use rd-protocol packet classes directly
- For rd-e2e (visual regression tests), client JARs ARE needed — launched with -javaagent
- Alpha/Beta clients require LWJGL 2 natives on java.library.path
- Release 1.6+ clients use the new launcher format and may need additional libraries
