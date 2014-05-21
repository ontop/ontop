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
import java.util.Properties;
import java.util.Set;

import org.openrdf.model.Graph;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.io.ModelIOManager;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDADataSource;
import org.semanticweb.ontop.model.OBDAException;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.model.impl.OBDADataFactoryImpl;
import org.semanticweb.ontop.model.impl.RDBMSourceParameterConstants;
import org.semanticweb.ontop.ontology.Ontology;
import org.semanticweb.ontop.ontology.impl.OntologyFactoryImpl;
import org.semanticweb.ontop.owlapi3.OBDAModelSynchronizer;
import org.semanticweb.ontop.owlapi3.OWLAPI3Translator;
import org.semanticweb.ontop.owlapi3.directmapping.DirectMappingEngine;
import org.semanticweb.ontop.owlrefplatform.core.Quest;
import org.semanticweb.ontop.owlrefplatform.core.QuestConnection;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.sesame.r2rml.R2RMLReader;
import org.semanticweb.ontop.sql.DBMetadata;
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
	private OWLAPI3Translator translator = new OWLAPI3Translator();

	public QuestDBVirtualStore(String name, URI obdaURI) throws Exception {
		this(name, null, obdaURI, null);
	}

	public QuestDBVirtualStore(String name, URI obdaURI, QuestPreferences config) throws Exception {
		this(name, null, obdaURI, config);
	}

	public QuestDBVirtualStore(String name, URI tboxFile, URI obdaURI) throws Exception {
		this(name, tboxFile, obdaURI, null);
	}
	
	public QuestDBVirtualStore(String name, QuestPreferences pref) throws Exception {
		// direct mapping : no tbox, no obda file, repo in-mem h2
		this(name, (URI)null, null, pref);
	}

	/**
	 * The method generates the OBDAModel from an
	 * obda or ttl (r2rml) file
	 * @param obdaURI - the file URI
	 * @return the generated OBDAModel
	 * @throws IOException
	 * @throws InvalidMappingException
	 */
	public OBDAModel getObdaModel(URI obdaURI) throws IOException, InvalidMappingException {
		//create empty model
		OBDAModel obdaModel = fac.getOBDAModel();
		// System.out.println(obdaURI.toString());
		if (obdaURI.toString().endsWith(".obda")) {
			//read obda file
			ModelIOManager modelIO = new ModelIOManager(obdaModel);
			modelIO.load(new File(obdaURI));
		} else if (obdaURI.toString().endsWith(".ttl")) {
			//read R2RML file
			R2RMLReader reader = new R2RMLReader(new File(obdaURI));
			obdaModel = reader.readModel(obdaURI);
		}
		return obdaModel;
	}

	/**
	 * The constructor to setup Quest virtual store given
	 * an owl file URI and an obda or R2rml mapping file URI
	 * @param name - the name of the triple store
	 * @param tboxFile - the owl file URI
	 * @param obdaUri - the obda or ttl file URI
	 * @param config - QuestPreferences
	 * @throws Exception
	 */
	public QuestDBVirtualStore(String name, URI tboxFile, URI obdaUri, QuestPreferences config) throws Exception {

		super(name);

		//obtain the model
		OBDAModel obdaModel = null;
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
		config.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

		//obtain the ontology
		OWLOntology owlontology = null;
		Ontology tbox;
		if (tboxFile != null) {
			//read owl file
			owlontology = getOntologyFromFile(tboxFile);
			//get transformation from owlontology into ontology
			 tbox = getOntologyFromOWLOntology(owlontology);

		} else { 
			// create empty ontology
			owlontology = man.createOntology();// createOntology(OBDADataFactoryImpl.getIRI(name));
			tbox = OntologyFactoryImpl.getInstance().createOntology();
			if (obdaModel.getSources().size() == 0)
				obdaModel.addSource(getMemOBDADataSource("MemH2"));
		}
		OBDAModelSynchronizer.declarePredicates(owlontology, obdaModel);

		//set up Quest
		setupQuest(tbox, obdaModel, null, config);
	}

	
	/**
	 * Constructor to start Quest given an OWL ontology and an RDF Graph
	 * representing R2RML mappings
	 * @param name - the name of the triple store
	 * @param tbox - the OWLOntology
	 * @param mappings - the RDF Graph (Sesame API)
	 * @param pref - QuestPreferences
	 * @throws Exception
	 */
	public QuestDBVirtualStore(String name, OWLOntology tbox, Graph mappings, DBMetadata metadata, QuestPreferences config) throws Exception {
		//call super constructor -> QuestDBAbstractStore
		super(name);
		
		//obtain ontology
		Ontology ontology = getOntologyFromOWLOntology(tbox);
		//obtain datasource
		OBDADataSource source = getDataSourceFromConfig(config);
		//obtain obdaModel
		R2RMLReader reader = new R2RMLReader(mappings);
		OBDAModel obdaModel = reader.readModel(source.getSourceID());
		//add data source to model
		obdaModel.addSource(source);
		OBDAModelSynchronizer.declarePredicates(tbox, obdaModel);
		//setup Quest
		setupQuest(ontology, obdaModel, metadata, config);
	}
	
	
private OBDADataSource getDataSourceFromConfig(QuestPreferences config) {
		String id = config.get(QuestPreferences.DBNAME).toString();
		String url = config.get(QuestPreferences.JDBC_URL).toString();
		String username = config.get(QuestPreferences.DBUSER).toString();
		String password = config.get(QuestPreferences.DBPASSWORD).toString();
		String driver = config.get(QuestPreferences.JDBC_DRIVER).toString();
		
		OBDADataSource source = OBDADataFactoryImpl.getInstance().getDataSource(URI.create(id));
		source.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME, username);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD, password);
		source.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER, driver);
		
		return source;
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
		Set<OWLOntology> clousure = owlontology.getOWLOntologyManager().getImportsClosure(owlontology);
		return translator.mergeTranslateOntologies(clousure);
	}
	
	private void setupQuest(Ontology tbox, OBDAModel obdaModel, DBMetadata metadata, QuestPreferences pref) throws Exception {
		//start Quest with the given ontology and model and preferences
		if (metadata == null) {
			//start up quest by obtaining metadata from given data source 
			questInstance = new Quest(tbox, obdaModel, pref);
		}
		else {
			//start up quest with given metadata 
			questInstance = new Quest(tbox, obdaModel, metadata, pref);
		}
		
		questInstance.setupRepository();
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

		DirectMappingEngine dm = new DirectMappingEngine("http://example.org/base", 0);
		try {
			OBDAModel model = dm.extractMappings(getMemOBDADataSource("H2m"));
			return model;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get a Quest connection from the Quest instance
	 * @return the QuestConnection
	 */
	public QuestConnection getQuestConnection() {
		try {
			// System.out.println("getquestconn..");
			questConn = questInstance.getConnection();
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
