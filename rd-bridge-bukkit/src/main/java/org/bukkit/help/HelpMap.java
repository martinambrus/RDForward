package org.bukkit.help;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface HelpMap {
    org.bukkit.help.HelpTopic getHelpTopic(java.lang.String arg0);
    java.util.Collection getHelpTopics();
    void addTopic(org.bukkit.help.HelpTopic arg0);
    void clear();
    void registerHelpTopicFactory(java.lang.Class arg0, org.bukkit.help.HelpTopicFactory arg1);
    java.util.List getIgnoredPlugins();
}
