package it.unibz.krdb.obda.utils;

public interface OBDAPreferencePersistanceManager {

	public String loadPreference(String pref);
	public void storePreference(String pref, String value);
	
}
