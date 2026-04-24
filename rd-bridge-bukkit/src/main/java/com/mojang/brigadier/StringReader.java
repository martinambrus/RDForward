package com.mojang.brigadier;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class StringReader implements com.mojang.brigadier.ImmutableStringReader {
    public StringReader(com.mojang.brigadier.StringReader arg0) {}
    public StringReader(java.lang.String arg0) {}
    public StringReader() {}
    public java.lang.String getString() {
        return null;
    }
    public void setCursor(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.StringReader.setCursor(I)V");
    }
    public int getRemainingLength() {
        return 0;
    }
    public int getTotalLength() {
        return 0;
    }
    public int getCursor() {
        return 0;
    }
    public java.lang.String getRead() {
        return null;
    }
    public java.lang.String getRemaining() {
        return null;
    }
    public boolean canRead(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.StringReader.canRead(I)Z");
        return false;
    }
    public boolean canRead() {
        return false;
    }
    public char peek() {
        return (char) 0;
    }
    public char peek(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.StringReader.peek(I)C");
        return (char) 0;
    }
    public char read() {
        return (char) 0;
    }
    public void skip() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.StringReader.skip()V");
    }
    public static boolean isAllowedNumber(char arg0) {
        return false;
    }
    public static boolean isQuotedStringStart(char arg0) {
        return false;
    }
    public void skipWhitespace() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.StringReader.skipWhitespace()V");
    }
    public int readInt() throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return 0;
    }
    public long readLong() throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return 0L;
    }
    public double readDouble() throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return 0.0;
    }
    public float readFloat() throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return 0.0f;
    }
    public static boolean isAllowedInUnquotedString(char arg0) {
        return false;
    }
    public java.lang.String readUnquotedString() {
        return null;
    }
    public java.lang.String readQuotedString() throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return null;
    }
    public java.lang.String readStringUntil(char arg0) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.StringReader.readStringUntil(C)Ljava/lang/String;");
        return null;
    }
    public java.lang.String readString() throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return null;
    }
    public boolean readBoolean() throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        return false;
    }
    public void expect(char arg0) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "com.mojang.brigadier.StringReader.expect(C)V");
    }
}
