package it.unibz.krdb.obda.utils;



public class NoPreferencePersistanceManager implements OBDAPreferencePersistanceManager{

	@Override
	public String loadPreference(String pref) {
		return null;
	}

	@Override
	public void storePreference(String pref, String value) {
	}
}
