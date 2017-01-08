package it.unibz.inf.ontop.owlrefplatform.questdb;

/*
 * #%L
 * ontop-quest-db
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
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


import java.util.Optional;

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.injection.QuestPreferences;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.OntologyFactory;
import it.unibz.inf.ontop.owlrefplatform.core.IQuest;
import it.unibz.inf.ontop.owlrefplatform.core.IQuestConnection;
import it.unibz.inf.ontop.injection.QuestCorePreferences;

import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/***
 * A bean that holds all the data about a store, generates a store folder and
 * maintains this data.
 */
public class QuestDBVirtualStore extends QuestDBAbstractStore {

	private static final long serialVersionUID = 2495624993519521937L;

	private static Logger log = LoggerFactory.getLogger(QuestDBVirtualStore.class);

	private static final OntologyFactory ofac = OntologyFactoryImpl.getInstance();

	private IQuest questInstance;
	
	private boolean isinitalized = false;

	@Override
	public QuestCorePreferences getPreferences() {
		return questInstance.getPreferences();
	}


	public QuestDBVirtualStore(@Nonnull String name, @Nonnull QuestConfiguration config) throws Exception {

		super(name, config);

		// TODO: re-cast the exception to a Sesame-specific one
		config.validate();

		QuestPreferences preferences = config.getProperties();

		//we are working in virtual mode
		if (!preferences.isInVirtualMode())
			throw new IllegalArgumentException("Virtual mode was expected in QuestDBVirtualStore!");

		//obtain the model
		OBDAModel obdaModel = config.loadMapping()
				.orElseThrow(() -> new IllegalStateException("Mapping are required in virtual A-box mode " +
						"so a configuration validation error should have already been thrown"));

		//obtain the ontology
		Optional<OWLOntology> optionalInputOntology = config.loadInputOntology();
		Ontology tbox = optionalInputOntology
				.map(OWLAPITranslatorUtility::translateImportsClosure)
				.orElseGet(() -> ofac.createOntology(ofac.createVocabulary()));

		obdaModel.getOntologyVocabulary().merge(tbox.getVocabulary());

		//set up Quest
		questInstance = getComponentFactory().create(tbox, Optional.of(obdaModel), config.getDatasourceMetadata(),
				config.getExecutorRegistry());
	}

	/**
	 * Must be called once after the constructor call and before any queries are run, that is,
	 * before the call to getQuestConnection.
	 * 
	 * Calls IQuest.setupRepository()
	 * @throws Exception
	 */
	public void initialize() throws Exception {
		if(this.isinitalized){
			log.warn("Double initialization of QuestDBVirtualStore");
		} else {
			this.isinitalized = true;
			questInstance.setupRepository();
		}
	}
	

	/**
	 * Get a Quest connection from the Quest instance
	 * @return the QuestConnection
	 */
	public IQuestConnection getQuestConnection() {
		if(!this.isinitalized)
			throw new Error("The QuestDBVirtualStore must be initialized before getQuestConnection can be run. See https://github.com/ontop/ontop/wiki/API-change-in-SesameVirtualRepo-and-QuestDBVirtualStore");
		try {
			// System.out.println("getquestconn..");
			return questInstance.getConnection();
		} catch (OBDAException e) {
			// TODO: throw a proper exception
			e.printStackTrace();
			// UGLY!
			return null;
		}
	}

	/**
	 * Shut down Quest and its connections.
	 */
	public void close() {
		questInstance.close();
	}
}
