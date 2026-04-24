package org.bukkit.configuration.file;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class FileConfiguration extends org.bukkit.configuration.MemoryConfiguration {
    public FileConfiguration() {}
    public FileConfiguration(org.bukkit.configuration.Configuration arg0) {}
    public void save(java.io.File arg0) throws java.io.IOException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.configuration.file.FileConfiguration.save(Ljava/io/File;)V");
    }
    public void save(java.lang.String arg0) throws java.io.IOException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.configuration.file.FileConfiguration.save(Ljava/lang/String;)V");
    }
    public abstract java.lang.String saveToString();
    public void load(java.io.File arg0) throws java.io.FileNotFoundException, java.io.IOException, org.bukkit.configuration.InvalidConfigurationException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.configuration.file.FileConfiguration.load(Ljava/io/File;)V");
    }
    public void load(java.io.Reader arg0) throws java.io.IOException, org.bukkit.configuration.InvalidConfigurationException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.configuration.file.FileConfiguration.load(Ljava/io/Reader;)V");
    }
    public void load(java.lang.String arg0) throws java.io.FileNotFoundException, java.io.IOException, org.bukkit.configuration.InvalidConfigurationException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.configuration.file.FileConfiguration.load(Ljava/lang/String;)V");
    }
    public abstract void loadFromString(java.lang.String arg0) throws org.bukkit.configuration.InvalidConfigurationException;
    protected java.lang.String buildHeader() {
        return null;
    }
    public org.bukkit.configuration.file.FileConfigurationOptions options() {
        return null;
    }
}
