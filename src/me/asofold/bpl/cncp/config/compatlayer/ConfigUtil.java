package me.asofold.bpl.cncp.config.compatlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.neatmonster.nocheatplus.checks.CheckType;

public class ConfigUtil {
	
	public static final int canaryInt = Integer.MIN_VALUE +7;
	public static final long canaryLong = Long.MIN_VALUE + 7L;
	public static final double canaryDouble = -Double.MAX_VALUE + 1.2593;
	
	public static String stringPath( String path){
		return stringPath(path, '.');
	}
	
	public static String stringPath( String path , char sep ){
		String useSep = (sep=='.')?"\\.":""+sep;
		String[] split = path.split(useSep);
		StringBuilder builder = new StringBuilder();
		builder.append(stringPart(split[0]));
		for (int i = 1; i<split.length; i++){
			builder.append(sep+stringPart(split[i]));
		}
		return builder.toString();
	}
	
	/**
	 * Aimed at numbers in paths.
	 * @param cfg
	 * @param path
	 * @return
	 */
	public static String bestPath(CompatConfig cfg, String path){
		return bestPath(cfg, path, '.');
	}
			
			
	/**
	 * Aimed at numbers in paths.
	 * @param cfg
	 * @param path
	 * @param sep
	 * @return
	 */
	public static String bestPath(CompatConfig cfg, String path, char sep){
		String useSep = (sep=='.')?"\\.":""+sep;
		String[] split = path.split(useSep);
		String res;
		if (cfg.hasEntry(split[0]) )res = split[0];
		else{
			res = stringPart(split[0]);
			if ( !cfg.hasEntry(res)) return path;
		}
		for (int i = 1; i<split.length; i++){
			if (cfg.hasEntry(res+sep+split[i]) ) res += sep+split[i];
			else{
				res += sep+stringPart(split[i]);
				if ( !cfg.hasEntry(res)) return path;
			}
		}
		return res;
	}
	
	public static String stringPart(String input){
		try{
			Double.parseDouble(input);
			return "'"+input+"'";
		} catch (NumberFormatException e){
		}
		try{
			Long.parseLong(input);
			return "'"+input+"'";
		} catch (NumberFormatException e){
		}
		try{
			Integer.parseInt(input);
			return "'"+input+"'";
		} catch (NumberFormatException e){
		}
		return input;
	}
	
	public static boolean forceDefaults(CompatConfig defaults, CompatConfig config){
		Map<String ,Object> all = defaults.getValuesDeep();
		boolean changed = false;
		for ( String path : all.keySet()){
			if ( !config.hasEntry(path)){
				config.setProperty(path, defaults.getProperty(path, null));
				changed = true;
			}
		}
		return changed;
	}
	
	/**
	 * Add StringList entries to a set.
	 * @param cfg
	 * @param path
	 * @param set
	 * @param clear If to clear the set.
	 * @param trim
	 * @param lowerCase
	 */
	public static void readStringSetFromList(CompatConfig cfg, String path, Set<String> set, boolean clear, boolean trim, boolean lowerCase){
		if (clear) set.clear();
		List<String> tempList = cfg.getStringList(path , null);
		if (tempList != null){
			for (String entry : tempList){
				if (trim) entry = entry.trim();
				if (lowerCase) entry = entry.toLowerCase();
				set.add(entry);
			}
		}
	}

	/**
     * Add StringList entries to a set.
     * @param cfg
     * @param path
     * @param set
     * @param clear If to clear the set.
     * @param trim
     * @param upperCase
     */
    public static void readCheckTypeSetFromList(CompatConfig cfg, String path, Set<CheckType> set, boolean clear, boolean trim, boolean upperCase){
        if (clear) set.clear();
        List<String> tempList = cfg.getStringList(path , null);
        if (tempList != null){
            for (String entry : tempList) {
                if (trim) entry = entry.trim();
                if (upperCase) entry = entry.toUpperCase();
                try {
                    final CheckType checkType = CheckType.valueOf(entry);
                    set.add(checkType);
                } catch (Exception e) {
                    System.out.println("[cncp] Unknow check " + entry + " is. Skipping!");
                }
            }
        }
    }
	
	/**
	 * Return an ArrayList.
	 * @param input
	 * @return
	 */
	public static final <T>  List<T> asList(final T[] input){
		final List<T> out = new ArrayList<T>(input.length);
		for (int i = 0; i < input.length; i++){
			out.add(input[i]);
		}
		return out;
	}
	
	/**
	 * Return an ArrayList.
	 * @param input
	 * @return
	 */
	public static final List<Integer> asList(final int[] input){
		final List<Integer> out = new ArrayList<Integer>(input.length);
		for (int i = 0; i < input.length; i++){
			out.add(input[i]);
		}
		return out;
	}
	
	/**
	 * Return an ArrayList.
	 * @param input
	 * @return
	 */
	public static final List<Long> asList(final long[] input){
		final List<Long> out = new ArrayList<Long>(input.length);
		for (int i = 0; i < input.length; i++){
			out.add(input[i]);
		}
		return out;
	}
	
	/**
	 * Return an ArrayList.
	 * @param input
	 * @return
	 */
	public static final List<Double> asList(final double[] input){
		final List<Double> out = new ArrayList<Double>(input.length);
		for (int i = 0; i < input.length; i++){
			out.add(input[i]);
		}
		return out;
	}
	
	/**
	 * Return an ArrayList.
	 * @param input
	 * @return
	 */
	public static final List<Float> asList(final float[] input){
		final List<Float> out = new ArrayList<Float>(input.length);
		for (int i = 0; i < input.length; i++){
			out.add(input[i]);
		}
		return out;
	}
	
	/**
	 * Return an ArrayList.
	 * @param input
	 * @return
	 */
	public static final List<Boolean> asList(final boolean[] input){
		final List<Boolean> out = new ArrayList<Boolean>(input.length);
		for (int i = 0; i < input.length; i++){
			out.add(input[i]);
		}
		return out;
	}
	
}
