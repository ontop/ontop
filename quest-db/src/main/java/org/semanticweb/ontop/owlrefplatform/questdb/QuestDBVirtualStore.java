package org.semanticweb.ontop.owlrefplatform.questdb;

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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Model;
import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.injection.NativeQueryLanguageComponentFactory;
import org.semanticweb.ontop.io.InvalidDataSourceException;
import org.semanticweb.ontop.io.OBDADataSourceFromConfigExtractor;
import org.semanticweb.ontop.owlrefplatform.core.IQuestConnection;
import org.semanticweb.ontop.owlrefplatform.injection.QuestComponentFactory;
import org.semanticweb.ontop.mapping.MappingParser;
import org.semanticweb.ontop.model.*;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.ontop.owlapi3.OWLAPI3TranslatorUtility;
import org.semanticweb.ontop.owlapi3.directmapping.DirectMappingEngine;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.sql.DBMetadata;
import org.semanticweb.ontop.sql.ImplicitDBConstraints;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * A bean that holds all the data about a store, generates a store folder and
 * maintains this data.
 */
public class QuestDBVirtualStore extends QuestDBAbstractStore {

	private static final long serialVersionUID = 2495624993519521937L;

	private static Logger log = LoggerFactory.getLogger(QuestDBVirtualStore.class);

	private static OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

	protected transient OWLOntologyManager man = OWLManager.createOWLOntologyManager();
	private OWLAPI3TranslatorUtility translator = new OWLAPI3TranslatorUtility();
	
	private boolean isinitalized = false;
	
	public QuestDBVirtualStore(String name, URI obdaURI, QuestPreferences config) throws Exception {
		this(name, null, obdaURI, config);
	}
	
	public QuestDBVirtualStore(String name, URI tboxFile, URI obdaURI) throws Exception {
		this(name, tboxFile, obdaURI, null);
	}
	
	public QuestDBVirtualStore(String name, QuestPreferences pref) throws Exception {
		// direct mapping : no tbox, no obda file, repo in-mem h2
		this(name, null, null, pref);
	}

	public QuestDBVirtualStore(String name, URI tboxFile, URI obdaUri, QuestPreferences config) throws Exception {
		this(name, tboxFile, obdaUri, config, null);
	}
	/**
	 * The method generates the OBDAModel according to the MappingParser
     * implementation deduced from the preferences.
     *
	 * @param obdaURI - the file URI
	 * @return the generated OBDAModel
	 * @throws IOException
	 * @throws InvalidMappingException
     *
	 */
	public OBDAModel getObdaModel(URI obdaURI) throws IOException, InvalidMappingException,
            DuplicateMappingException, InvalidDataSourceException {
        NativeQueryLanguageComponentFactory nativeQLFactory = getNativeQLFactory();
        MappingParser modelParser = nativeQLFactory.create(new File(obdaURI));
        OBDAModel obdaModel = modelParser.getOBDAModel();
        return obdaModel;
	}

	/**
	 * The constructor to setup Quest virtual store given
	 * an owl file URI and an obda or R2rml mapping file URI
	 * @param name - the name of the triple store
	 * @param tboxFile - the owl file URI
	 * @param obdaUri - the obda or ttl file URI
	 * @param config - QuestPreferences
	 * @param userConstraints - User-supplied database constraints (or null)
	 * @throws Exception
	 */
	public QuestDBVirtualStore(String name, URI tboxFile, URI obdaUri, QuestPreferences config,
                               ImplicitDBConstraints userConstraints) throws Exception {

		super(name, config);

		//obtain the model
		OBDAModel obdaModel;
		if (obdaUri == null) {
			log.debug("No mappings where given, mappings will be automatically generated.");
			//obtain model from direct mapping RDB2RDF method
			obdaModel = getOBDAModelDM();
		} else {
			//obtain model from file
			obdaModel = getObdaModel(obdaUri);
		}

		//set config preferences values
		if (config == null) {
			config = new QuestPreferences();
		}
		//we are working in virtual mode
		if (!config.getProperty(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL))
			throw new IllegalArgumentException("Virtual mode was expected in QuestDBVirtualStore!");

		//obtain the ontology
		Ontology tbox;
		if (tboxFile != null) {
			//read owl file
			OWLOntology owlontology = getOntologyFromFile(tboxFile);
			//get transformation from owlontology into ontology
			 tbox = getOntologyFromOWLOntology(owlontology);

		} else { 
			// create empty ontology
			//owlontology = man.createOntology();
			tbox = OntologyFactoryImpl.getInstance().createOntology();
			if (obdaModel.getSources().size() == 0) {
                Set<OBDADataSource> dataSources = new HashSet<>(obdaModel.getSources());
                dataSources.add(getMemOBDADataSource("MemH2"));
                obdaModel = obdaModel.newModel(dataSources, obdaModel.getMappings());
            }
		}
		obdaModel.declareAll(tbox.getVocabulary());
		// OBDAModelSynchronizer.declarePredicates(owlontology, obdaModel);

		//set up Quest
		setupQuest(tbox, obdaModel, null, config);
	}

	
	/**
	 * Constructor to start Quest given an OWL ontology and an RDF Graph
	 * representing R2RML mappings
	 * @param name - the name of the triple store
	 * @param tbox - the OWLOntology
	 * @param mappings - the RDF Graph (Sesame API)
	 * @param config - QuestPreferences
	 * @throws Exception
	 */
	public QuestDBVirtualStore(String name, OWLOntology tbox, Model mappings, DBMetadata metadata,
                               QuestPreferences config) throws Exception {
		//call super constructor -> QuestDBAbstractStore
		super(name, config);
		
		//obtain ontology
		Ontology ontology = getOntologyFromOWLOntology(tbox);

        MappingParser mappingParser = getNativeQLFactory().create(mappings);
        OBDAModel obdaModel = mappingParser.getOBDAModel();

		obdaModel.declareAll(ontology.getVocabulary());
		//setup Quest
		setupQuest(ontology, obdaModel, metadata, config);
	}
	
	
    private OBDADataSource getDataSourceFromConfig(QuestPreferences config) throws InvalidDataSourceException {
        OBDADataSourceFromConfigExtractor dataSourceExtractor = new OBDADataSourceFromConfigExtractor(config);
        return dataSourceExtractor.getDataSource();
	}

	/**
	 * Given a URI of an owl file returns the 
	 * translated OWLOntology object
	 * @param tboxFile - the URI of the file
	 * @return the translated OWLOntology
	 * @throws Exception
	 */
	private OWLOntology getOntologyFromFile(URI tboxFile) throws Exception{
		//get owl ontology from file
		OWLOntologyIRIMapper iriMapper = new AutoIRIMapper(new File(tboxFile).getParentFile(), false);
		man.addIRIMapper(iriMapper);
		OWLOntology owlontology = man.loadOntologyFromOntologyDocument(new File(tboxFile));
		
		return owlontology;
	}
	
	/**
	 * Given an OWL ontology returns the translated Ontology 
	 * of its closure
	 * @param owlontology
	 * @return the translated Ontology
	 * @throws Exception
	 */
	private Ontology getOntologyFromOWLOntology(OWLOntology owlontology) throws Exception{
		//compute closure first (owlontology might contain include other source declarations)
		Set<OWLOntology> closure = owlontology.getOWLOntologyManager().getImportsClosure(owlontology);
		return translator.mergeTranslateOntologies(closure);
	}
	
	private void setupQuest(Ontology tbox, OBDAModel obdaModel, DBMetadata metadata,
                            QuestPreferences pref) throws Exception {
        QuestComponentFactory factory = getComponentFactory();

		//start Quest with the given ontology and model and preferences
		questInstance = factory.create(tbox, obdaModel, metadata, pref);
	}
	
	/**
	 * Sets the implicit db constraints, i.e. primary and foreign keys not in the database
	 * Must be called before the call to initialize
	 * 
	 * @param userConstraints
	 */
	public void setImplicitDBConstraints(ImplicitDBConstraints userConstraints){
		if(userConstraints == null)
			throw new NullPointerException();
		if(this.isinitalized)
			throw new Error("Implicit DB Constraints must be given before the call to initialize to have effect. See https://github.com/ontop/ontop/wiki/Implicit-database-constraints and https://github.com/ontop/ontop/wiki/API-change-in-SesameVirtualRepo-and-QuestDBVirtualStore");
		questInstance.setImplicitDBConstraints(userConstraints);
	}

	/**
	 * Create an in-memory H2 database data source
	 * @param name - the datasource name
	 * @return the created OBDADataSource
	 */
	private static OBDADataSource getMemOBDADataSource(String name) {

		OBDADataSource obdaSource = OBDADataFactoryImpl.getInstance().getDataSource(URI.create(name));

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questrepository";
		String username = "sa";
		String password = "";

		obdaSource = fac.getDataSource(URI.create("http://www.obda.org/ABOXDUMP" + System.currentTimeMillis()));
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
	 * Calls {@link org.semanticweb.ontop.owlrefplatform.core.IQuest.setupRepository()}
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
			questConn = (IQuestConnection) questInstance.getConnection();
		} catch (OBDAException e) {
			e.printStackTrace();
		}
		return questConn;
	}

	/**
	 * Shut down Quest and its connections.
	 */
	public void close() {
		questInstance.close();
	}
}
