package org.bukkit.conversations;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Prompt extends java.lang.Cloneable {
    public static final org.bukkit.conversations.Prompt END_OF_CONVERSATION = null;
    java.lang.String getPromptText(org.bukkit.conversations.ConversationContext arg0);
    boolean blocksForInput(org.bukkit.conversations.ConversationContext arg0);
    org.bukkit.conversations.Prompt acceptInput(org.bukkit.conversations.ConversationContext arg0, java.lang.String arg1);
}
