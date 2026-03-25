package com.github.martinambrus.rdforward.protocol.codec;

import io.netty.handler.codec.DecoderException;

/**
 * A DecoderException that suppresses stack trace generation.
 * Used for common protocol errors (malformed VarInts, bad lengths) where
 * the stack trace is wasted work — the error is expected and the trace
 * is never useful for debugging. Derived from Velocity/Krypton.
 */
public class QuietDecoderException extends DecoderException {

    /** Cached instance for negative/bad frame lengths. */
    public static final QuietDecoderException BAD_LENGTH = new QuietDecoderException("Bad packet length");

    /** Cached instance for oversized VarInts (> 5 bytes). */
    public static final QuietDecoderException VARINT_TOO_BIG = new QuietDecoderException("VarInt too big");

    public QuietDecoderException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
