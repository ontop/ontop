package it.unibz.krdb.obda.protege4.core;

import java.util.Iterator;
import java.util.Set;

import it.unibz.krdb.obda.model.impl.OBDAModelImpl;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.utils.OBDAPreferences;

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
		String  value = pref.getString(OBDAPreferences.ADD_MAPPING, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.ADD_MAPPING, value);
		}
		value = pref.getString(OBDAPreferences.DELETE_MAPPING, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.DELETE_MAPPING, value);
		}
		value = pref.getString(OBDAPreferences.EDIT_BODY, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.EDIT_BODY, value);
		}
		value = pref.getString(OBDAPreferences.EDIT_HEAD, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.EDIT_HEAD, value);
		}
		value = pref.getString(OBDAPreferences.EDIT_ID, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.EDIT_ID, value);
		}
		
		value = pref.getString(OBDAPreferences.USE_DEAFAULT, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.USE_DEAFAULT, value);
		}
		
		value = pref.getString(OBDAPreferences.JODS_RESULTSET_FETCHSIZE, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.JODS_RESULTSET_FETCHSIZE, value);
		}
		
		value = pref.getString(OBDAPreferences.OBDAPREFS_FONTFAMILY, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.OBDAPREFS_FONTFAMILY, value);
		}
		value = pref.getString(OBDAPreferences.OBDAPREFS_FONTSIZE, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.OBDAPREFS_FONTSIZE, value);
		}
		value = pref.getString(OBDAPreferences.OBDAPREFS_ISBOLD, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.OBDAPREFS_ISBOLD, value);
		}
		
		value = pref.getString(OBDAPreferences.CLASS_COLOR, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.CLASS_COLOR, value);
		}
		value = pref.getString(OBDAPreferences.VARIABLE_COLOR, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.VARIABLE_COLOR, value);
		}
		value = pref.getString(OBDAPreferences.OBJECTPROPTERTY_COLOR, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.OBJECTPROPTERTY_COLOR, value);
		}
		value = pref.getString(OBDAPreferences.DATAPROPERTY_COLOR, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.DATAPROPERTY_COLOR, value);
		}
		value = pref.getString(OBDAPreferences.PARAMETER_COLOR, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.PARAMETER_COLOR, value);
		}
		value = pref.getString(OBDAPreferences.FUCNTOR_COLOR, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.FUCNTOR_COLOR, value);
		}
		value = pref.getString(OBDAPreferences.MAPPING_BODY_COLOR, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.MAPPING_BODY_COLOR, value);
		}
		value = pref.getString(OBDAPreferences.MAPPING_ID_COLOR, null);
		if(value != null){
			obdaPref.put(OBDAPreferences.MAPPING_ID_COLOR, value);
		}
		
		value = pref.getString(ReformulationPlatformPreferences.ABOX_MODE, null);
		if(value != null){
			refplatPref.put(ReformulationPlatformPreferences.ABOX_MODE, value);
		}
		value = pref.getString(ReformulationPlatformPreferences.DATA_LOCATION, null);
		if(value != null){
			refplatPref.put(ReformulationPlatformPreferences.DATA_LOCATION, value);
		}
		value = pref.getString(ReformulationPlatformPreferences.DBTYPE, null);
		if(value != null){
			refplatPref.put(ReformulationPlatformPreferences.DBTYPE, value);
		}
		value = pref.getString(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, null);
		if(value != null){
			refplatPref.put(ReformulationPlatformPreferences.REFORMULATION_TECHNIQUE, value);
		}
		value = pref.getString(ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS, null);
		if(value != null){
			refplatPref.put(ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS, value);
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
