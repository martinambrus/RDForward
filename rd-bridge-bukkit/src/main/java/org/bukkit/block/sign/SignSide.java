package org.bukkit.block.sign;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SignSide extends org.bukkit.material.Colorable {
    java.util.List lines();
    net.kyori.adventure.text.Component line(int arg0) throws java.lang.IndexOutOfBoundsException;
    void line(int arg0, net.kyori.adventure.text.Component arg1) throws java.lang.IndexOutOfBoundsException;
    java.lang.String[] getLines();
    java.lang.String getLine(int arg0) throws java.lang.IndexOutOfBoundsException;
    void setLine(int arg0, java.lang.String arg1) throws java.lang.IndexOutOfBoundsException;
    boolean isGlowingText();
    void setGlowingText(boolean arg0);
}
