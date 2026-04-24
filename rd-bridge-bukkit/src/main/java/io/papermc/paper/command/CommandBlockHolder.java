package io.papermc.paper.command;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CommandBlockHolder {
    java.lang.String getCommand();
    void setCommand(java.lang.String arg0);
    net.kyori.adventure.text.Component lastOutput();
    void lastOutput(net.kyori.adventure.text.Component arg0);
    int getSuccessCount();
    void setSuccessCount(int arg0);
}
