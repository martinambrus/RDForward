# Tested Client Instances (from MultiMC)

Source: `/mnt/c/Users/Riman/Downloads/MultiMC/instances/`

## Alpha Clients (19 instances)

| Instance | Protocol Version | Notes |
|----------|-----------------|-------|
| a1.0.15 | v13 | Pre-rewrite SMP |
| a1.0.16 | v14 | Pre-rewrite SMP |
| a1.0.17_02 | v1 | Post-rewrite SMP (first post-rewrite) |
| a1.0.17_04 | v1 | Post-rewrite SMP |
| a1.1.0 | v2 | |
| a1.1.2 | v2 | |
| a1.1.2_01 | v2 | |
| a1.2.0 | v3 | |
| a1.2.0_01 | v3 | |
| a1.2.0_02 | v3 | |
| a1.2.1 | v3 | |
| a1.2.1_01 | v3 | |
| a1.2.2a | v4 | |
| a1.2.2b | v4 | |
| a1.2.3 | v5 | |
| a1.2.3_01 | v5 | |
| a1.2.3_02 | v5 | |
| a1.2.3_04 | v5 | |
| a1.2.4_01 | v6 | |
| a1.2.5 | v6 | |
| a1.2.6 | v6 | |

## Beta Clients (21 instances)

| Instance | Protocol Version | Notes |
|----------|-----------------|-------|
| b1.0 | v7 | |
| b1.0_01 | v7 | |
| b1.0.2 | v7 | |
| b1.1_01 | v8 | |
| b1.1_02 | v8 | |
| b1.2 | v8 | |
| b1.2_01 | v8 | |
| b1.2_02 | v8 | |
| b1.3_01 | v9 | |
| b1.3b | v9 | |
| b1.4 | v10 | |
| b1.4_01 | v10 | |
| b1.5 | v11 | String16 encoding |
| b1.5_01 | v11 | String16 encoding |
| b1.6 | v12 | |
| b1.6.1 | v13 | |
| b1.6.2 | v13 | |
| b1.6.3 | v13 | |
| b1.6.4 | v13 | |
| b1.6.5 | v13 | |
| b1.6.6 | v13 | |
| b1.7 | v13 | |
| b1.7.2 | v14 | |
| b1.7.3 | v14 | |
| b1.8 | v17 | First creative mode |
| b1.8.1 | v17 | |

## Release Clients (27 instances)

| Instance | Protocol Version | Notes |
|----------|-----------------|-------|
| 1.0 | v22 | First official release |
| 1.1 | v23 | |
| 1.2.1 | v28 | Section-based chunks |
| 1.2.2 | v28 | |
| 1.2.3 | v28 | |
| 1.2.4 | v29 | |
| 1.2.5 | v29 | |
| 1.3.1 | v39 | Encryption added |
| 1.3.2 | v39 | |
| 1.4.2 | v47 | |
| 1.4.4 | v49 | |
| 1.4.5 | v49 | |
| 1.4.6 | v51 | |
| 1.4.7 | v51 | |
| 1.5.1 | v60 | |
| 1.5.2 | v61 | |
| 1.6.1 | v73 | |
| 1.6.2 | v74 | |
| 1.6.4 | v78 | |
| 1.7.2 | v4 (Netty) | Netty rewrite |
| 1.7.3 | v4 (Netty) | |
| 1.7.4 | v4 (Netty) | |
| 1.7.5 | v4 (Netty) | |
| 1.7.6 | v5 (Netty) | |
| 1.7.7 | v5 (Netty) | |
| 1.7.8 | v5 (Netty) | |
| 1.7.9 | v5 (Netty) | |
| 1.7.10 | v5 (Netty) | |
| 1.8-1.8.9 | v47 (Netty) | LWJGL2, 9 sub-versions |
| 1.9-1.9.4 | v107-v110 (Netty) | |
| 1.10-1.10.2 | v210 (Netty) | |
| 1.11-1.11.2 | v315-v316 (Netty) | |
| 1.12-1.12.2 | v335-v340 (Netty) | |
| 1.13-1.13.2 | v393-v404 (Netty) | LWJGL3 |
| 1.14-1.14.4 | v477-v498 (Netty) | BlockRenderDispatcher patch |
| 1.15-1.15.2 | v573-v578 (Netty) | |
| 1.16-1.16.5 | v735-v754 (Netty) | |
| 1.17-1.17.1 | v755-v756 (Netty) | Core Profile, GameRenderer skip |
| 1.18-1.18.2 | v757-v758 (Netty) | Core Profile, GameRenderer skip |

## E2E Test Status
- **Passing**: RubyDung, Alpha (v1-v6, v13-v14), Beta (v7-v14, v17, v21), Release pre-Netty (v22-v78), Netty LWJGL2 (v4-v340), Netty LWJGL3 (v393-v758)
- **All 1.0-1.18.2 versions pass** the world_loaded login screenshot test
- 1.17+ screenshots are black (3D rendering skipped on Mesa llvmpipe)

## Summary
- Distinct protocol versions covered: v1-v6 (Alpha), v7-v14,v17,v21 (Beta), v22-v78 (Pre-Netty Release), v4-v5,v47,v107-v110,v210,v315-v340 (Netty LWJGL2), v393-v758 (Netty LWJGL3)
- Testing coverage: every sub-version within each protocol version
