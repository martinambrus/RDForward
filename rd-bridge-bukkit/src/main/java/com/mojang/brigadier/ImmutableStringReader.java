package com.mojang.brigadier;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ImmutableStringReader {
    java.lang.String getString();
    int getRemainingLength();
    int getTotalLength();
    int getCursor();
    java.lang.String getRead();
    java.lang.String getRemaining();
    boolean canRead(int arg0);
    boolean canRead();
    char peek();
    char peek(int arg0);
}
