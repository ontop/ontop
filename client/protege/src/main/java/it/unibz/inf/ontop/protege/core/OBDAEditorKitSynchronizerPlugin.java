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
import org.protege.editor.core.Disposable;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.editorkit.plugin.EditorKitHook;
import org.protege.editor.core.prefs.Preferences;
import org.protege.editor.core.prefs.PreferencesManager;
import org.protege.editor.owl.OWLEditorKit;

/***
 * This class is responsible for initializing all base classes for the OBDA
 * plugin. In particular this class will register an obdaModelManager of
 * OBDAPluginController and server preference holder objects into the current
 * EditorKit. These instances can be retrieved by other components (Tabs, Views,
 * Actions, etc) by doing EditorKit.get(key).
 * 
 * @author Mariano Rodriguez Muro
 * 
 */
public class OBDAEditorKitSynchronizerPlugin extends EditorKitHook {

	private OBDAModelManager obdaModelManager;
	private DisposableProperties reasonerPref;

	@Override
	public void initialise() throws Exception {
		EditorKit editorKit = getEditorKit();
		if (!(editorKit instanceof OWLEditorKit)) {
			throw new IllegalArgumentException("The OBDA Plugin only works with OWLEditorKit instances.");
		}

		editorKit.put(OBDAEditorKitSynchronizerPlugin.class.getName(), this);

		// Preferences for Quest
		reasonerPref = new DisposableProperties();

		// Preferences for JDBC Connection
		reasonerPref.put(JDBCConnectionPool.class.getCanonicalName(), ConnectionGenerator.class.getCanonicalName());

		// 	Publish the new reasonerPref
		// 		ConnectionGenerator@JDBCConnectionPool
		//      QuestPreferencesPanel uses
		//      	OntopReformulationSettings.EXISTENTIAL_REASONING
		//        	OntopMappingSettings.QUERY_ONTOLOGY_ANNOTATIONS
		//        	OntopOBDASettings.SAME_AS
		editorKit.put(DisposableProperties.class.getName(), reasonerPref);

		obdaModelManager = new OBDAModelManager((OWLEditorKit) editorKit);
	}

	@Override
	public void dispose() throws Exception {
		PreferencesManager man = PreferencesManager.getInstance();
		Preferences pref = man.getApplicationPreferences("OBDA Plugin");
		for (Object key : reasonerPref.keySet()) {
			Object value = reasonerPref.get(key);
			pref.putString(key.toString(), value.toString());
		}

		obdaModelManager.dispose();
	}

	public static OBDAModelManager getOBDAModelManager(EditorKit editorKit) {
		Disposable object = editorKit.get(OBDAEditorKitSynchronizerPlugin.class.getName());
		if (!(object instanceof OBDAEditorKitSynchronizerPlugin))
			throw new RuntimeException("Cannot find OBDAEditorKitSynchronizerPlugin");

		return ((OBDAEditorKitSynchronizerPlugin)object).obdaModelManager;
	}

	public static OBDAModel getCurrentOBDAModel(EditorKit editorKit) {
		Disposable object = editorKit.get(OBDAEditorKitSynchronizerPlugin.class.getName());
		if (!(object instanceof OBDAEditorKitSynchronizerPlugin))
			throw new RuntimeException("Cannot find OBDAEditorKitSynchronizerPlugin");

		return (((OBDAEditorKitSynchronizerPlugin)object).obdaModelManager).getCurrentOBDAModel();
	}

	public static DisposableProperties getProperties(EditorKit editorKit) {
		Disposable object = editorKit.get(DisposableProperties.class.getName());
		if (!(object instanceof DisposableProperties))
			throw new RuntimeException("Cannot find DisposableProperties");

		return (DisposableProperties)object;
	}

}
