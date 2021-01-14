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

	private OBDAModelManager instance;
	private DisposableProperties reasonerPref;

	@Override
	public void initialise() throws Exception {
		OWLEditorKit kit = (OWLEditorKit)getEditorKit();

		// Preferences for Quest
        reasonerPref = new DisposableProperties();
        kit.put(DisposableProperties.class.getName(), reasonerPref);

		/***
		 * Each editor kit has its own instance of the ProtegePluginController.
		 * Note, the OBDA model is inside this object (do
		 * .getOBDAModelManager())
		 */
		instance = new OBDAModelManager(kit);
		kit.put(OBDAEditorKitSynchronizerPlugin.class.getName(), this);

		kit.put(OBDAModelManager.class.getName(), instance);

		// TODO: Not sound!! remove it!!!
		kit.put(SQLPPMappingImpl.class.getName(), instance);

		PreferencesManager man = PreferencesManager.getInstance();
		Preferences pref = man.getApplicationPreferences("OBDA Plugin");

		for (String key : reasonerPref.getReformulationPlatformPreferencesKeys()) {
			String value = pref.getString(key, null);
			if (value != null) {
				reasonerPref.put(key, value);
			}
		}

		// Preferences for JDBC Connection
		reasonerPref.put(JDBCConnectionPool.class.getCanonicalName(), ConnectionGenerator.class.getCanonicalName());

		// Publish the new reasonerPref
		kit.put(DisposableProperties.class.getName(), reasonerPref);
	}

	@Override
	public void dispose() throws Exception {
		PreferencesManager man = PreferencesManager.getInstance();
		Preferences pref = man.getApplicationPreferences("OBDA Plugin");
		for (Object key : reasonerPref.keySet()) {
			Object value = reasonerPref.get(key);
			pref.putString(key.toString(), value.toString());
		}

		instance.dispose();
	}
}
