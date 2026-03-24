package com.github.martinambrus.rdforward.server;

/**
 * Chunk lock usage types for fine-grained concurrent access control.
 *
 * READ: Multiple concurrent readers allowed (serialization, sending).
 * WORLDGEN: Exclusive access for chunk generation.
 * SAVE: Exclusive access for chunk saving to disk.
 */
public enum Usage {
    READ,
    WORLDGEN,
    SAVE
}
