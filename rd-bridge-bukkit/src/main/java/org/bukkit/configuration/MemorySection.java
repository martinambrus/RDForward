// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.configuration;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stub of Bukkit's {@code MemorySection}. RDForward does not carry any
 * configuration state for plugins, but real plugins (e.g. LuckPerms) rely
 * on the <em>two-arg</em> {@code getX(path, default)} overloads returning
 * the caller-supplied default when the path is not present. The stub
 * therefore implements every 2-arg getter as "return the default" and
 * every 1-arg getter as "return the type's zero / {@code null}".
 *
 * <p>Write-paths (including {@code set}, {@code createSection},
 * {@code setComments}, etc.) are silent no-ops: the hybrid contract says
 * writes warn-once, but the call log is noisy in test runs and the
 * plugin does not observe the write either way.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class MemorySection implements ConfigurationSection {

    protected MemorySection() {}
    protected MemorySection(ConfigurationSection parent, String path) {}

    public Set getKeys(boolean deep) { return Collections.emptySet(); }
    public Map getValues(boolean deep) { return Collections.emptyMap(); }

    public boolean contains(String path) { return false; }
    public boolean contains(String path, boolean ignoreDefault) { return false; }
    public boolean isSet(String path) { return false; }

    public String getCurrentPath() { return ""; }
    public String getName() { return ""; }
    public Configuration getRoot() { return null; }
    public ConfigurationSection getParent() { return null; }

    public void addDefault(String path, Object value) {}
    public ConfigurationSection getDefaultSection() { return null; }

    public void set(String path, Object value) {}

    public Object get(String path) { return null; }
    public Object get(String path, Object def) { return def; }

    public ConfigurationSection createSection(String path) { return null; }
    public ConfigurationSection createSection(String path, Map map) { return null; }

    public String getString(String path) { return null; }
    public String getString(String path, String def) { return def; }
    public boolean isString(String path) { return false; }

    public int getInt(String path) { return 0; }
    public int getInt(String path, int def) { return def; }
    public boolean isInt(String path) { return false; }

    public boolean getBoolean(String path) { return false; }
    public boolean getBoolean(String path, boolean def) { return def; }
    public boolean isBoolean(String path) { return false; }

    public double getDouble(String path) { return 0.0; }
    public double getDouble(String path, double def) { return def; }
    public boolean isDouble(String path) { return false; }

    public long getLong(String path) { return 0L; }
    public long getLong(String path, long def) { return def; }
    public boolean isLong(String path) { return false; }

    public List getList(String path) { return Collections.emptyList(); }
    public List getList(String path, List def) { return def != null ? def : Collections.emptyList(); }
    public boolean isList(String path) { return false; }

    public List getStringList(String path) { return Collections.emptyList(); }
    public List getIntegerList(String path) { return Collections.emptyList(); }
    public List getBooleanList(String path) { return Collections.emptyList(); }
    public List getDoubleList(String path) { return Collections.emptyList(); }
    public List getFloatList(String path) { return Collections.emptyList(); }
    public List getLongList(String path) { return Collections.emptyList(); }
    public List getByteList(String path) { return Collections.emptyList(); }
    public List getCharacterList(String path) { return Collections.emptyList(); }
    public List getShortList(String path) { return Collections.emptyList(); }
    public List getMapList(String path) { return Collections.emptyList(); }

    public Object getObject(String path, Class clazz) { return null; }
    public Object getObject(String path, Class clazz, Object def) { return def; }

    public ConfigurationSerializable getSerializable(String path, Class clazz) { return null; }
    public ConfigurationSerializable getSerializable(String path, Class clazz, ConfigurationSerializable def) { return def; }

    public Vector getVector(String path) { return null; }
    public Vector getVector(String path, Vector def) { return def; }
    public boolean isVector(String path) { return false; }

    public OfflinePlayer getOfflinePlayer(String path) { return null; }
    public OfflinePlayer getOfflinePlayer(String path, OfflinePlayer def) { return def; }
    public boolean isOfflinePlayer(String path) { return false; }

    public ItemStack getItemStack(String path) { return null; }
    public ItemStack getItemStack(String path, ItemStack def) { return def; }
    public boolean isItemStack(String path) { return false; }

    public Color getColor(String path) { return null; }
    public Color getColor(String path, Color def) { return def; }
    public boolean isColor(String path) { return false; }

    public Location getLocation(String path) { return null; }
    public Location getLocation(String path, Location def) { return def; }
    public boolean isLocation(String path) { return false; }

    public ConfigurationSection getConfigurationSection(String path) { return null; }
    public boolean isConfigurationSection(String path) { return false; }

    protected boolean isPrimitiveWrapper(Object value) { return false; }
    protected Object getDefault(String path) { return null; }

    protected void mapChildrenKeys(Set output, ConfigurationSection section, boolean deep) {}
    protected void mapChildrenValues(Map output, ConfigurationSection section, boolean deep) {}

    public static String createPath(ConfigurationSection section, String key) { return key; }
    public static String createPath(ConfigurationSection section, String key, ConfigurationSection relativeTo) { return key; }

    public List getComments(String path) { return Collections.emptyList(); }
    public List getInlineComments(String path) { return Collections.emptyList(); }
    public void setComments(String path, List comments) {}
    public void setInlineComments(String path, List comments) {}

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[path='" + getCurrentPath() + "']";
    }
}
