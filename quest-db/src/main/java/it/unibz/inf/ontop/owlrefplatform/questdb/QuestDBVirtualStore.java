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


import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.injection.QuestPreferences;
import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.OntologyFactory;
import it.unibz.inf.ontop.owlrefplatform.core.IQuest;
import it.unibz.inf.ontop.owlrefplatform.core.IQuestConnection;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestCorePreferences;

import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.RDBMSourceParameterConstants;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.owlapi.directmapping.DirectMappingEngine;
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

	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
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

		QuestPreferences preferences = config.getPreferences();

		//we are working in virtual mode
		if (!preferences.isInVirtualMode())
			throw new IllegalArgumentException("Virtual mode was expected in QuestDBVirtualStore!");

		//obtain the model
		OBDAModel obdaModel = config.loadInputMappings()
				//obtain model from direct mapping RDB2RDF method
				// TODO: refactor this hack
				.orElseGet(this::getOBDAModelDM);
				//.orElseThrow(() -> new IllegalStateException("Mapping are required in virtual A-box mode " +
				//		"so a configuration validation error should have already been thrown"));

		//obtain the ontology
		Optional<OWLOntology> optionalInputOntology = config.loadInputOntology();
		Ontology tbox = optionalInputOntology
				.map(OWLAPITranslatorUtility::translateImportsClosure)
				.orElseGet(() -> ofac.createOntology(ofac.createVocabulary()));

		obdaModel.getOntologyVocabulary().merge(tbox.getVocabulary());

		/**
		 * TODO: should we keep this hack??? What is its point?
		 */
		if ((!optionalInputOntology.isPresent()) && obdaModel.getSources().size() == 0) {
			Set<OBDADataSource> dataSources = new HashSet<>(obdaModel.getSources());
			dataSources.add(getMemOBDADataSource("MemH2"));
			obdaModel = obdaModel.newModel(dataSources, obdaModel.getMappings());
		}

		//set up Quest
		questInstance = getComponentFactory().create(tbox, Optional.of(obdaModel), config.getDatasourceMetadata());
	}

	/**
	 * Create an in-memory H2 database data source
	 * @param name - the datasource name
	 * @return the created OBDADataSource
	 */
	private static OBDADataSource getMemOBDADataSource(String name) {

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questrepository";
		String username = "sa";
		String password = "";

		OBDADataSource obdaSource = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP" + System.currentTimeMillis()));
		obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		obdaSource.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY, "true");
		obdaSource.setParameter(RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP, "true");
		
		return (obdaSource);
	}

	/**
	 * Generate an OBDAModel from Direct Mapping (Bootstrapping)
	 * @return the OBDAModel
	 */
	private OBDAModel getOBDAModelDM() {

		DirectMappingEngine dm = new DirectMappingEngine("http://example.org/base", 0,
                getNativeQLFactory(), getOBDAFactory());
		try {
			OBDAModel model = dm.extractMappings(getMemOBDADataSource("H2m"));
			return model;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
			questInstance.setupRepository(getInjector());
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
