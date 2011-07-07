package it.unibz.krdb.obda.protege4.core;

import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.OBDAConstants;
import it.unibz.krdb.obda.utils.OBDAPreferences;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.protege.editor.core.editorkit.plugin.EditorKitHook;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.owl.OWLEditorKit;

/***
 * This class is responsible for initializing all base classes for the OBDA
 * plugin. In particular this class will register an instance of
 * OBDAPluginController and server preference holder objects into the current
 * EditorKit. These instances can be retrieved by other components (Tabs, Views,
 * Actions, etc) by doing EditorKit.get(key).
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class OBDAEditorKitSynchronizerPlugin extends EditorKitHook {

	OBDAModelManager instance = null;
	OWLEditorKit kit = null;
//	OWLModelManager mmgr = null;
	ProtegeOBDAPreferences obdaPref = null;
	ProtegeReformulationPlatformPreferences refplatPref = null;
	
	public void initialise() throws Exception {

		/***
		 * Each editor kit has its own instance of the ProtegePluginController.
		 * Note, the OBDA model is inside this object (do
		 * .getOBDAModelManager())
		 */
		instance = new OBDAModelManager(this.getEditorKit());
		getEditorKit().put(OBDAEditorKitSynchronizerPlugin.class.getName(), this);
		kit = (OWLEditorKit)getEditorKit();
//		mmgr = (OWLModelManager)kit.getModelManager();
//		mmgr.addListener(instance.getModelManagerListener());
		getEditorKit().put(OBDAModelImpl.class.getName(), instance);

		// getEditorKit().getModelManager().put(APIController.class.getName(),
		// instance);

		/***
		 * Preferences for the OBDA plugin (gui, etc)
		 */
		obdaPref = new ProtegeOBDAPreferences();
		getEditorKit().put(OBDAPreferences.class.getName(), obdaPref);

		/***
		 * Preferences for Quest
		 */
		refplatPref = new ProtegeReformulationPlatformPreferences();
		getEditorKit().put(ReformulationPlatformPreferences.class.getName(),refplatPref);
		loadPreferences();
	}

	@Override
	public void dispose() throws Exception {
//		mmgr.removeListener(instance.getModelManagerListener());
		storePreferences();
	}
	
	private void loadPreferences(){
		PreferencesManager man = PreferencesManager.getInstance();
		Preferences pref = man.getApplicationPreferences("OBDA Plugin");
		
		List<String> keys = obdaPref.getOBDAPreferenceKeys();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()){
			String key = it.next();
			String  value = pref.getString(key, null);
			if(value != null){
				obdaPref.put(key, value);
			}
		}
		
		keys = refplatPref.getReformulationPlatformPreferencesKeys();
		it = keys.iterator();
		boolean isCalssic = false;
		while(it.hasNext()){
			String key = it.next();
			String value = pref.getString(key, null);
			if(value != null){
				// here we ensure that if the abox mode is classic the the data location can only be in memory
				if (key.equals(ReformulationPlatformPreferences.ABOX_MODE) && value.equals(OBDAConstants.CLASSIC)) { 
					refplatPref.put(ReformulationPlatformPreferences.DATA_LOCATION, OBDAConstants.INMEMORY);
					refplatPref.put(key, value);
					isCalssic = true;
				}else if(key.equals(ReformulationPlatformPreferences.DATA_LOCATION)){//if it is classic the data location is already set
					if(!isCalssic){
						refplatPref.put(key, value);
					}
				}else{
					refplatPref.put(key, value);
				}
			}
		}
	}
	
	private void storePreferences(){
		
		PreferencesManager man = PreferencesManager.getInstance();
		Preferences pref = man.getApplicationPreferences("OBDA Plugin");
		Set<Object> keys = obdaPref.keySet();
		Iterator<Object> it = keys.iterator();
		while(it.hasNext()){
			Object key = it.next();
			Object value = obdaPref.get(key);
			pref.putString(key.toString(), value.toString());
		}
		
		keys = refplatPref.keySet();
		it = keys.iterator();
		while(it.hasNext()){
			Object key = it.next();
			Object value = refplatPref.get(key);
			pref.putString(key.toString(), value.toString());
		}
	}
}
