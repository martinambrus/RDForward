package org.bukkit.configuration;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ConfigurationSection {
    java.util.Set getKeys(boolean arg0);
    java.util.Map getValues(boolean arg0);
    boolean contains(java.lang.String arg0);
    boolean contains(java.lang.String arg0, boolean arg1);
    boolean isSet(java.lang.String arg0);
    java.lang.String getCurrentPath();
    java.lang.String getName();
    org.bukkit.configuration.Configuration getRoot();
    org.bukkit.configuration.ConfigurationSection getParent();
    java.lang.Object get(java.lang.String arg0);
    java.lang.Object get(java.lang.String arg0, java.lang.Object arg1);
    void set(java.lang.String arg0, java.lang.Object arg1);
    org.bukkit.configuration.ConfigurationSection createSection(java.lang.String arg0);
    org.bukkit.configuration.ConfigurationSection createSection(java.lang.String arg0, java.util.Map arg1);
    java.lang.String getString(java.lang.String arg0);
    java.lang.String getString(java.lang.String arg0, java.lang.String arg1);
    boolean isString(java.lang.String arg0);
    int getInt(java.lang.String arg0);
    int getInt(java.lang.String arg0, int arg1);
    boolean isInt(java.lang.String arg0);
    boolean getBoolean(java.lang.String arg0);
    boolean getBoolean(java.lang.String arg0, boolean arg1);
    boolean isBoolean(java.lang.String arg0);
    double getDouble(java.lang.String arg0);
    double getDouble(java.lang.String arg0, double arg1);
    boolean isDouble(java.lang.String arg0);
    long getLong(java.lang.String arg0);
    long getLong(java.lang.String arg0, long arg1);
    boolean isLong(java.lang.String arg0);
    java.util.List getList(java.lang.String arg0);
    java.util.List getList(java.lang.String arg0, java.util.List arg1);
    boolean isList(java.lang.String arg0);
    java.util.List getStringList(java.lang.String arg0);
    java.util.List getIntegerList(java.lang.String arg0);
    java.util.List getBooleanList(java.lang.String arg0);
    java.util.List getDoubleList(java.lang.String arg0);
    java.util.List getFloatList(java.lang.String arg0);
    java.util.List getLongList(java.lang.String arg0);
    java.util.List getByteList(java.lang.String arg0);
    java.util.List getCharacterList(java.lang.String arg0);
    java.util.List getShortList(java.lang.String arg0);
    java.util.List getMapList(java.lang.String arg0);
    java.lang.Object getObject(java.lang.String arg0, java.lang.Class arg1);
    java.lang.Object getObject(java.lang.String arg0, java.lang.Class arg1, java.lang.Object arg2);
    org.bukkit.configuration.serialization.ConfigurationSerializable getSerializable(java.lang.String arg0, java.lang.Class arg1);
    org.bukkit.configuration.serialization.ConfigurationSerializable getSerializable(java.lang.String arg0, java.lang.Class arg1, org.bukkit.configuration.serialization.ConfigurationSerializable arg2);
    org.bukkit.util.Vector getVector(java.lang.String arg0);
    org.bukkit.util.Vector getVector(java.lang.String arg0, org.bukkit.util.Vector arg1);
    boolean isVector(java.lang.String arg0);
    org.bukkit.OfflinePlayer getOfflinePlayer(java.lang.String arg0);
    org.bukkit.OfflinePlayer getOfflinePlayer(java.lang.String arg0, org.bukkit.OfflinePlayer arg1);
    boolean isOfflinePlayer(java.lang.String arg0);
    org.bukkit.inventory.ItemStack getItemStack(java.lang.String arg0);
    org.bukkit.inventory.ItemStack getItemStack(java.lang.String arg0, org.bukkit.inventory.ItemStack arg1);
    boolean isItemStack(java.lang.String arg0);
    org.bukkit.Color getColor(java.lang.String arg0);
    org.bukkit.Color getColor(java.lang.String arg0, org.bukkit.Color arg1);
    boolean isColor(java.lang.String arg0);
    org.bukkit.Location getLocation(java.lang.String arg0);
    org.bukkit.Location getLocation(java.lang.String arg0, org.bukkit.Location arg1);
    boolean isLocation(java.lang.String arg0);
    org.bukkit.configuration.ConfigurationSection getConfigurationSection(java.lang.String arg0);
    boolean isConfigurationSection(java.lang.String arg0);
    org.bukkit.configuration.ConfigurationSection getDefaultSection();
    void addDefault(java.lang.String arg0, java.lang.Object arg1);
    java.util.List getComments(java.lang.String arg0);
    java.util.List getInlineComments(java.lang.String arg0);
    void setComments(java.lang.String arg0, java.util.List arg1);
    void setInlineComments(java.lang.String arg0, java.util.List arg1);
    default net.kyori.adventure.text.Component getRichMessage(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.configuration.ConfigurationSection.getRichMessage(Ljava/lang/String;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component getRichMessage(java.lang.String arg0, net.kyori.adventure.text.Component arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.configuration.ConfigurationSection.getRichMessage(Ljava/lang/String;Lnet/kyori/adventure/text/Component;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default void setRichMessage(java.lang.String arg0, net.kyori.adventure.text.Component arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.configuration.ConfigurationSection.setRichMessage(Ljava/lang/String;Lnet/kyori/adventure/text/Component;)V");
    }
    default net.kyori.adventure.text.Component getComponent(java.lang.String arg0, net.kyori.adventure.text.serializer.ComponentDecoder arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.configuration.ConfigurationSection.getComponent(Ljava/lang/String;Lnet/kyori/adventure/text/serializer/ComponentDecoder;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default net.kyori.adventure.text.Component getComponent(java.lang.String arg0, net.kyori.adventure.text.serializer.ComponentDecoder arg1, net.kyori.adventure.text.Component arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.configuration.ConfigurationSection.getComponent(Ljava/lang/String;Lnet/kyori/adventure/text/serializer/ComponentDecoder;Lnet/kyori/adventure/text/Component;)Lnet/kyori/adventure/text/Component;");
        return null;
    }
    default void setComponent(java.lang.String arg0, net.kyori.adventure.text.serializer.ComponentEncoder arg1, net.kyori.adventure.text.Component arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.configuration.ConfigurationSection.setComponent(Ljava/lang/String;Lnet/kyori/adventure/text/serializer/ComponentEncoder;Lnet/kyori/adventure/text/Component;)V");
    }
}
