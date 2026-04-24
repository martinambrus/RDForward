package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TextDisplay extends org.bukkit.entity.Display {
    java.lang.String getText();
    void setText(java.lang.String arg0);
    net.kyori.adventure.text.Component text();
    void text(net.kyori.adventure.text.Component arg0);
    int getLineWidth();
    void setLineWidth(int arg0);
    org.bukkit.Color getBackgroundColor();
    void setBackgroundColor(org.bukkit.Color arg0);
    byte getTextOpacity();
    void setTextOpacity(byte arg0);
    boolean isShadowed();
    void setShadowed(boolean arg0);
    boolean isSeeThrough();
    void setSeeThrough(boolean arg0);
    boolean isDefaultBackground();
    void setDefaultBackground(boolean arg0);
    org.bukkit.entity.TextDisplay$TextAlignment getAlignment();
    void setAlignment(org.bukkit.entity.TextDisplay$TextAlignment arg0);
}
