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

import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.answering.connection.pool.JDBCConnectionPool;
import it.unibz.inf.ontop.answering.connection.pool.impl.ConnectionGenerator;
import it.unibz.inf.ontop.spec.mapping.pp.impl.SQLPPMappingImpl;
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
		Preferences pref = getPreferences();
		for (String key : getReformulationPlatformPreferencesKeys()) {
			String value = pref.getString(key, null);
			if (value != null) {
				reasonerPref.put(key, value);
			}
		}

		// Preferences for JDBC Connection
		reasonerPref.put(JDBCConnectionPool.class.getCanonicalName(), ConnectionGenerator.class.getCanonicalName());

		// 	Publish the new reasonerPref
		// 		ConnectionGenerator@JDBCConnectionPool
		//      rubbish below?
		//      QuestPreferencesPanel uses
		//      	OntopReformulationSettings.EXISTENTIAL_REASONING
		//        	OntopMappingSettings.QUERY_ONTOLOGY_ANNOTATIONS
		//        	OntopOBDASettings.SAME_AS
		editorKit.put(DisposableProperties.class.getName(), reasonerPref);

		obdaModelManager = new OBDAModelManager((OWLEditorKit) editorKit);
	}

	@Override
	public void dispose() throws Exception {
		Preferences pref = getPreferences();
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

	public static DisposableProperties getProperties(EditorKit editorKit) {
		Disposable object = editorKit.get(DisposableProperties.class.getName());
		if (!(object instanceof DisposableProperties))
			throw new RuntimeException("Cannot find DisposableProperties");

		return (DisposableProperties)object;
	}

	private Preferences getPreferences() {
		PreferencesManager man = PreferencesManager.getInstance();
		return man.getApplicationPreferences("OBDA Plugin");
	}

	// TODO: check if the constants below can be removed

	private static final String	DBTYPE	= "org.obda.owlreformulationplatform.dbtype";
	private static final String OBTAIN_FROM_ONTOLOGY = "org.obda.owlreformulationplatform.obtainFromOntology";
	private static final String OBTAIN_FROM_MAPPINGS = "org.obda.owlreformulationplatform.obtainFromMappings";
	private static final String	ABOX_MODE = "org.obda.owlreformulationplatform.aboxmode";

	private static ImmutableList<String> getReformulationPlatformPreferencesKeys() {
		return ImmutableList.of(
				ABOX_MODE,
				DBTYPE,
				OBTAIN_FROM_ONTOLOGY,
				OBTAIN_FROM_MAPPINGS);
	}

}
