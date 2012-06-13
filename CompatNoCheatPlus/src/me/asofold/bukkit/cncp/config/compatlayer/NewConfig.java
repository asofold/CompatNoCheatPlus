package me.asofold.bukkit.cncp.config.compatlayer;

import java.io.File;
import java.util.Map;

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

	
}
