package net.kyori.adventure.text.minimessage;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class ParsingException extends java.lang.RuntimeException {
    public static final int LOCATION_UNKNOWN = -1;
    protected ParsingException(java.lang.String arg0) {}
    protected ParsingException(java.lang.String arg0, java.lang.Throwable arg1) {}
    protected ParsingException(java.lang.String arg0, java.lang.Throwable arg1, boolean arg2, boolean arg3) {}
    protected ParsingException() {}
    public abstract java.lang.String originalText();
    public abstract java.lang.String detailMessage();
    public abstract int startIndex();
    public abstract int endIndex();
}
