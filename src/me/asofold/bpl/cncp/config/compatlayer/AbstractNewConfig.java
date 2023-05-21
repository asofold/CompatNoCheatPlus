package me.asofold.bpl.cncp.config.compatlayer;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationOptions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

public abstract class AbstractNewConfig extends AbstractConfig {
	File file = null;
	MemoryConfiguration config = null;
	
	public AbstractNewConfig(File file){
		setFile(file);
	}
	
	public void setFile(File file) {
		this.file = file;
		this.config = new MemoryConfiguration();
		setOptions(config);
	}

	@Override
	public boolean hasEntry(String path) {
		return config.contains(path) || (config.get(path) != null);
	}
	
	


	@Override
	public String getString(String path, String defaultValue) {
		if (!hasEntry(path)) return defaultValue;
		return config.getString(path, defaultValue);
	}

	

	@Override
	public List<String> getStringKeys(String path) {
		// TODO policy: only strings or all keys as strings ?
		List<String> out = new LinkedList<String>();
		List<Object> keys = getKeys(path);
		if ( keys == null ) return out;
		for ( Object obj : keys){
			if ( obj instanceof String ) out.add((String) obj);
			else{
				try{
					out.add(obj.toString());
				} catch ( Throwable t){
					// ignore.
				}
			}
		}
		return out;
	}

	@Override
	public List<Object> getKeys(String path) {
		List<Object> out = new LinkedList<Object>();
		Set<String> keys;
		if ( path == null) keys = config.getKeys(false);
		else{
			ConfigurationSection sec = config.getConfigurationSection(path);
			if (sec == null) return out;
			keys = sec.getKeys(false);
		}
		if ( keys == null) return out;
		out.addAll(keys);
		return out;
	}
	
	@Override
	public List<Object> getKeys() {
		return getKeys(null);
	}

	@Override
	public Object getProperty(String path, Object defaultValue) {
		Object obj = config.get(path);
		if ( obj  == null ) return defaultValue;
		else return obj;
	}

	@Override
	public List<String> getStringKeys() {
		return getStringKeys(null);
	}

	@Override
	public void setProperty(String path, Object obj) {
		config.set(path, obj);
	}

	@Override
	public List<String> getStringList(String path, List<String> defaultValue) {
		if ( !hasEntry(path)) return defaultValue;
		List<String> out = new LinkedList<String>();
		List<String> entries = config.getStringList(path);
		if ( entries == null ) return defaultValue;
		for ( String entry : entries){
			if ( entry instanceof String) out.add(entry);
			else{
				try{
					out.add(entry.toString());
				} catch (Throwable t){
					// ignore
				}
			}
		}
		return out;
	}

	@Override
	public void removeProperty(String path) {
		if (path.startsWith(".")) path = path.substring(1);
		// VERY EXPENSIVE
		MemoryConfiguration temp = new MemoryConfiguration();
		setOptions(temp);
		Map<String, Object> values = config.getValues(true);
		if (values.containsKey(path)) values.remove(path);
		else{
			final String altPath = "."+path;
			if (values.containsKey(altPath)) values.remove(altPath);
		}
		for ( String _p : values.keySet()){
			Object v = values.get(_p);
			if (v == null) continue;
			else if (v instanceof ConfigurationSection) continue;
			String p;
			if (_p.startsWith(".")) p = _p.substring(1);
			else p = _p;
			if (p.startsWith(path)) continue;
			temp.set(p, v);
		}
		config = temp;
	}


	@Override
	public Boolean getBoolean(String path, Boolean defaultValue) {
		if (!config.contains(path)) return defaultValue;
		String val = config.getString(path, null);
		if (val != null){
			if (val.equalsIgnoreCase("true")) return true;
			else if (val.equalsIgnoreCase("false")) return false;
			else return defaultValue;
		}
		Boolean res = defaultValue; 
		if ( val == null ){
			if ( defaultValue == null) defaultValue = false;
			res = config.getBoolean(path, defaultValue);
		}
		return res;
	}




	@Override
	public Double getDouble(String path, Double defaultValue) {
		if (!config.contains(path)) return defaultValue;
		Double res = super.getDouble(path, null);
		if ( res == null ) res = config.getDouble(path, ConfigUtil.canaryDouble);
		if ( res == ConfigUtil.canaryDouble) return defaultValue;
		return res;
	}




	@Override
	public Long getLong(String path, Long defaultValue) {
		if (!config.contains(path)) return defaultValue;
		Long res = super.getLong(path, null);
		if ( res == null ) res = config.getLong(path, ConfigUtil.canaryLong);
		if ( res == ConfigUtil.canaryLong) return defaultValue;
		return res;
	}




	@Override
	public Integer getInt(String path, Integer defaultValue) {
		if (!config.contains(path)) return defaultValue;
		Integer res = super.getInt(path, null);
		if ( res == null ) res = config.getInt(path, ConfigUtil.canaryInt);
		if ( res == ConfigUtil.canaryInt) return defaultValue;
		return res;
	}




	@Override
	public List<Integer> getIntList(String path, List<Integer> defaultValue) {
		// TODO Auto-generated method stub
		return super.getIntList(path, defaultValue);
	}




	@Override
	public List<Double> getDoubleList(String path, List<Double> defaultValue) {
		// TODO Auto-generated method stub
		return super.getDoubleList(path, defaultValue);
	}


	void addAll(Configuration source, Configuration target){
		Map<String, Object> all = source.getValues(true);
		for ( String path: all.keySet()){
			target.set(path, source.get(path));
		}
	}

	void setOptions(Configuration cfg){
		ConfigurationOptions opt = cfg.options();
		opt.pathSeparator(this.sep);
		//opt.copyDefaults(true);
	}
	
	@Override
	public boolean setPathSeparatorChar(char sep) {
		this.sep = sep;
		setOptions(config);
		return true;
	}

}
