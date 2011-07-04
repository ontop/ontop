package it.unibz.krdb.obda.protege4.gui.preferences;

import it.unibz.krdb.obda.utils.OBDAPreferencePersistanceManager;

import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;


public class ProtegeOBDAPerferencesPersistanceManager implements OBDAPreferencePersistanceManager {

	private Preferences preferences = null;
	
	public ProtegeOBDAPerferencesPersistanceManager(){
		
		PreferencesManager man = PreferencesManager.getInstance();
		preferences = man.getApplicationPreferences("OBDA Plugin");
	}
	
	@Override
	public String loadPreference(String pref) {
		return preferences.getString(pref, null);
	}

	@Override
	public void storePreference(String pref, String value) {
		preferences.putString(pref, value);		
	}

}
