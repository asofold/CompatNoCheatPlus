package me.asofold.bpl.cncp.config.compatlayer;


import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CONVENTIONS: 
 * - Return strings if objects can be made strings.
 * - No exceptions, rather leave elements out of lists.
 * - Lists of length 0 and null can not always be distinguished (?->extra safe wrapper ?)
 * - All contents are treated alike, even if given as a string (!): true and 'true', 1 and '1'
 * @author mc_dev
 *
 */
public interface CompatConfig {
	
	// Boolean
	/**
	 * Only accepts true and false , 'true' and 'false'.
	 * @param path
	 * @param defaultValue
	 * @return
	 */
	public Boolean getBoolean(String path, Boolean defaultValue);
	public Boolean getBoolean(String path);
	
	// Long
	public Long getLong(String path);
	public Long getLong(String path, Long defaultValue);
	
	// Double
	public Double getDouble(String path);
	public Double getDouble(String path, Double defaultValue);
	public List<Double> getDoubleList(String path);
	public List<Double> getDoubleList(String path , List<Double> defaultValue);
	
	// Integer (abbreviation)
	public Integer getInt(String path);
	public Integer getInt(String path, Integer defaultValue);
	public List<Integer> getIntList(String path);
	public List<Integer> getIntList(String path, List<Integer> defaultValue);
	// Integer (full name)
	public Integer getInteger(String path);
	public Integer getInteger(String path, Integer defaultValue);
	public List<Integer> getIntegerList(String path);
	public List<Integer> getIntegerList(String path, List<Integer> defaultValue);

	// String
	public String getString(String path);
	public String getString(String path, String defaultValue);
	public List<String> getStringList(String path);
	public List<String> getStringList(String path, List<String> defaultValue);
	
	// Generic methods:
	public Object get(String path);
	public Object get(String path, Object defaultValue);
	public Object getProperty(String path);
	public Object getProperty(String path, Object defaultValue);
	public void set(String path, Object value);
	public void setProperty(String path, Object value);
	
	/**
	 * Remove a path (would also remove sub sections, unless for path naming problems).
	 * @param path
	 */
	public void remove(String path);
	
	/**
	 * Works same as remove(path): removes properties and sections alike.
	 * @param path
	 */
	public void removeProperty(String path);
	
	// Contains/has
	public boolean hasEntry(String path);
	public boolean contains(String path);
	
	// Keys (Object): [possibly deprecated]
	/**
	 * @deprecated Seems not to be supported anymore by new configuration, use getStringKeys(String path);
	 * @param path
	 * @return
	 */
	public List<Object> getKeys(String path);
	/**
	 * @deprecated Seems not to be supported anymore by new configuration, use getStringKeys();
	 * @return
	 */
	public List<Object> getKeys();
	
	// Keys (String):
	/**
	 * 
	 * @return never null
	 */
	public List<String> getStringKeys();
	
	public List<String> getStringKeys(String path);
	
	/**
	 * Get all keys from the section (deep or shallow).
	 * @param path
	 * @param deep
	 * @return Never null.
	 */
	public Set<String> getStringKeys(String path, boolean deep);
	
	/**
	 * convenience method.
	 * @param path
	 * @return never null
	 * 
	 */
	public Set<String> getStringKeysDeep(String path);
	
	// Values:
	/**
	 * Equivalent to new config: values(true)
	 * @return
	 */
	public Map<String, Object> getValuesDeep();
	
	// Technical / IO:
	/**
	 * False if not supported.
	 * @param sep
	 * @return
	 */
	public boolean setPathSeparatorChar(char sep);
	
	public void load();
	
	public boolean save();
	
	/**
	 * Clear all contents.
	 */
	public void clear();

	/**
	 * Return a YAML-String representation of the contents, null if not supported.
	 * @return null if not supported.
	 */
	public String getYAMLString();
	
	/**
	 * Add all entries from the YAML-String representation to the configuration, forget or clear all previous entries. 
	 * @return
	 */
	public boolean fromYamlString(String input);
	

}
