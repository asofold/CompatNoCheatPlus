package me.asofold.bpl.cncp.config.compatlayer;

import java.io.File;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class NewConfig extends AbstractNewConfig{
	
	
	public NewConfig(File file) {
		super(file);
	}


	@Override
	public void load(){
		config = new MemoryConfiguration();
		setOptions(config);
		FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		setOptions(cfg);
		addAll(cfg, config);
	}
	

	@Override
	public boolean save(){
		YamlConfiguration cfg = new YamlConfiguration();
		setOptions(cfg);
		addAll(config, cfg);
		try{
			cfg.save(file);
			return true;
		} catch (Throwable t){
			return false;
		}
	}


	@Override
	public Map<String, Object> getValuesDeep() {
		return config.getValues(true);
	}


	@Override
	public void clear() {
		setFile(file);
	}


	@Override
	public String getYAMLString() {
		final YamlConfiguration temp = new YamlConfiguration();
		addAll(config, temp);
		return temp.saveToString();
	}


	@Override
	public boolean fromYamlString(String input) {
		final YamlConfiguration temp = new YamlConfiguration();
		try {
			clear();
			temp.loadFromString(input);
			addAll(temp, config);
			return true;
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
			return false;
		}
	}

	
}
