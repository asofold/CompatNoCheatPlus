package me.asofold.bpl.cncp.config.compatlayer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Some generic checks and stuff using getString, hasEntry, getStringList, ...
 * @author mc_dev
 *
 */
public abstract class AbstractConfig implements CompatConfig {
	
	protected char sep = '.';

	@Override
	public Boolean getBoolean(String path, Boolean defaultValue) {
		String val = getString(path, null);
		if ( val == null ) return defaultValue;
		try{
			// return Boolean.parseBoolean(val);
			String t = val.trim().toLowerCase();
			if ( t.equals("true")) return true;
			else if ( t.equals("false")) return false;
			else return defaultValue;
		} catch( NumberFormatException exc){
			return defaultValue;
		}
	}

	@Override
	public Double getDouble(String path, Double defaultValue) {
		String val = getString(path, null);
		if ( val == null ) return defaultValue;
		try{
			return Double.parseDouble(val);
		} catch( NumberFormatException exc){
			return defaultValue;
		}
	}

	@Override
	public Long getLong(String path, Long defaultValue) {
		String val = getString(path, null);
		if ( val == null ) return defaultValue;
		try{
			return Long.parseLong(val);
		} catch( NumberFormatException exc){
			return defaultValue;
		}
	}

	@Override
	public Integer getInt(String path, Integer defaultValue) {
		String val = getString(path, null);
		if ( val == null ) return defaultValue;
		try{
			return Integer.parseInt(val);
		} catch( NumberFormatException exc){
			return defaultValue;
		}
	}
	
	@Override
	public List<Integer> getIntList(String path, List<Integer> defaultValue){
		if ( !hasEntry(path) ) return defaultValue;
		List<String> strings = getStringList(path, null);
		if ( strings == null ) return defaultValue;
		List<Integer> out = new LinkedList<Integer>();
		for ( String s : strings){
			try{
				out.add(Integer.parseInt(s));
			} catch(NumberFormatException exc){
				// ignore
			}
		}
		return out;
	}

	@Override
	public List<Double> getDoubleList(String path, List<Double> defaultValue) {
		if ( !hasEntry(path) ) return defaultValue;
		List<String> strings = getStringList(path, null);
		if ( strings == null ) return defaultValue;
		List<Double> out = new LinkedList<Double>();
		for ( String s : strings){
			try{
				out.add(Double.parseDouble(s));
			} catch(NumberFormatException exc){
				// ignore
			}
		}
		return out;
	}

	@Override
	public Set<String> getStringKeys(String path, boolean deep) {
		if (deep) return getStringKeysDeep(path);
		Set<String> keys = new HashSet<String>();
		keys.addAll(getStringKeys(path));
		return keys;
		
	}
	
	@Override
	public Set<String> getStringKeysDeep(String path) {
		// NOTE: pretty inefficient, but aimed at seldomly read sections.
		Map<String, Object> values = getValuesDeep();
		Set<String> out = new HashSet<String>();
		final int len = path.length();
		for (String key : values.keySet()){
			if (!key.startsWith(path)) continue;
			else if (key.length() == len) continue;
			else if (key.charAt(len) == sep) out.add(key);
		}
		return out;
	}

	@Override
	public Object get(String path, Object defaultValue) {
		return getProperty(path, defaultValue);
	}

	@Override
	public void set(String path, Object value) {
		setProperty(path, value);
	}

	@Override
	public void remove(String path) {
		removeProperty(path);
	}

	@Override
	public Boolean getBoolean(String path) {
		return getBoolean(path, null);
	}

	@Override
	public Double getDouble(String path) {
		return getDouble(path, null);
	}

	@Override
	public List<Double> getDoubleList(String path) {
		return getDoubleList(path, null);
	}

	@Override
	public Integer getInt(String path) {
		return getInt(path, null);
	}

	@Override
	public List<Integer> getIntList(String path) {
		return getIntList(path, null);
	}

	@Override
	public Integer getInteger(String path) {
		return getInt(path, null);
	}

	@Override
	public List<Integer> getIntegerList(String path) {
		return getIntList(path, null);
	}

	@Override
	public String getString(String path) {
		return getString(path, null);
	}

	@Override
	public List<String> getStringList(String path) {
		return getStringList(path, null);
	}

	@Override
	public Object get(String path) {
		return getProperty(path, null);
	}

	@Override
	public Object getProperty(String path) {
		return getProperty(path, null);
	}

	@Override
	public boolean contains(String path) {
		return hasEntry(path);
	}

	@Override
	public Integer getInteger(String path, Integer defaultValue) {
		return getInt(path, defaultValue);
	}

	@Override
	public List<Integer> getIntegerList(String path, List<Integer> defaultValue) {
		return getIntList(path, defaultValue);
	}

	@Override
	public Long getLong(String path) {
		return getLong(path, null);
	}

	
}
