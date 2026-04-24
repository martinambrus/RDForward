package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ServerLinks {
    org.bukkit.ServerLinks$ServerLink getLink(org.bukkit.ServerLinks$Type arg0);
    java.util.List getLinks();
    org.bukkit.ServerLinks$ServerLink setLink(org.bukkit.ServerLinks$Type arg0, java.net.URI arg1);
    org.bukkit.ServerLinks$ServerLink addLink(org.bukkit.ServerLinks$Type arg0, java.net.URI arg1);
    org.bukkit.ServerLinks$ServerLink addLink(net.kyori.adventure.text.Component arg0, java.net.URI arg1);
    org.bukkit.ServerLinks$ServerLink addLink(java.lang.String arg0, java.net.URI arg1);
    boolean removeLink(org.bukkit.ServerLinks$ServerLink arg0);
    org.bukkit.ServerLinks copy();
}
