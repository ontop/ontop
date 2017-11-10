package it.unibz.inf.ontop.protege.core;

/*
 * #%L
 * ontop-protege
 * %%
 * Copyright (C) 2009 - 2013 KRDB Research Centre. Free University of Bozen Bolzano.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.answering.connection.pool.JDBCConnectionPool;
import it.unibz.inf.ontop.answering.connection.pool.impl.ConnectionGenerator;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.editorkit.plugin.EditorKitHook;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.owl.OWLEditorKit;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static it.unibz.inf.ontop.protege.core.impl.DeprecatedConstants.ABOX_MODE;
import static it.unibz.inf.ontop.protege.core.impl.DeprecatedConstants.CLASSIC;

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
	DisposableOBDAPreferences obdaPref = null;
	DisposableProperties reasonerPref = null;

	
	@Override
	protected void setup(EditorKit editorKit) {
        super.setup(editorKit);
    } 
	
	@Override
	public void initialise() throws Exception {


        /***
         * Preferences for the OBDA plugin (gui, etc)
         */
        obdaPref = new DisposableOBDAPreferences();
        getEditorKit().put(DisposableOBDAPreferences.class.getName(), obdaPref);

        /***
         * Preferences for Quest
         */
        reasonerPref = new DisposableProperties();
        getEditorKit().put(DisposableProperties.class.getName(), reasonerPref);
        loadPreferences();
		
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

		getEditorKit().put(OBDAModelManager.class.getName(), instance);
		/**
		 * TODO: Not sound!! remove it!!!
		 */
		getEditorKit().put(SQLPPMappingImpl.class.getName(), instance);

		// getEditorKit().getModelManager().put(APIController.class.getName(),
		// instance);

		loadPreferences();
	}

	@Override
	public void dispose() throws Exception {
//		mmgr.removeListener(instance.getModelManagerListener());
		storePreferences();
		instance.dispose();
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
		
		keys = reasonerPref.getReformulationPlatformPreferencesKeys();
		it = keys.iterator();
		boolean isCalssic = false;
		while(it.hasNext()){
			String key = it.next();
			String value = pref.getString(key, null);
			if(value != null){			// here we ensure that if the abox mode is classic the the data location can only be in memory
				if (key.equals(ABOX_MODE) && value.equals(CLASSIC)) {
//					reasonerPref.put(ReformulationPlatformPreferences.DATA_LOCATION, QuestConstants.INMEMORY);
					reasonerPref.put(key, value);
					isCalssic = true;
				}else{
					reasonerPref.put(key, value);
				}
			}
		}

		/***
		 * Preferences for JDBC Connection
		 */

		reasonerPref.put(JDBCConnectionPool.class.getCanonicalName(), ConnectionGenerator.class.getCanonicalName());

		// Publish the new reasonerPref
		getEditorKit().put(DisposableProperties.class.getName(), reasonerPref);
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
		
		keys = reasonerPref.keySet();
		it = keys.iterator();
		while(it.hasNext()){
			Object key = it.next();
			Object value = reasonerPref.get(key);
			pref.putString(key.toString(), value.toString());
		}
	}
}
