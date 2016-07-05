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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import it.unibz.inf.ontop.model.OBDADataFactory;
import it.unibz.inf.ontop.model.OBDADataSource;
import it.unibz.inf.ontop.model.OBDAException;
import it.unibz.inf.ontop.model.OBDAModel;
import it.unibz.inf.ontop.ontology.OntologyVocabulary;
import it.unibz.inf.ontop.owlrefplatform.core.IQuest;
import it.unibz.inf.ontop.owlrefplatform.core.IQuestConnection;
import it.unibz.inf.ontop.owlrefplatform.core.QuestConstants;
import it.unibz.inf.ontop.owlrefplatform.core.QuestPreferences;
import org.openrdf.model.Model;
import it.unibz.inf.ontop.exception.DuplicateMappingException;
import it.unibz.inf.ontop.exception.InvalidMappingException;
import it.unibz.inf.ontop.injection.NativeQueryLanguageComponentFactory;
import it.unibz.inf.ontop.io.InvalidDataSourceException;
import it.unibz.inf.ontop.io.OBDADataSourceFromConfigExtractor;
import it.unibz.inf.ontop.owlrefplatform.injection.QuestComponentFactory;
import it.unibz.inf.ontop.mapping.MappingParser;

import it.unibz.inf.ontop.model.impl.OBDADataFactoryImpl;
import it.unibz.inf.ontop.model.impl.RDBMSourceParameterConstants;
import it.unibz.inf.ontop.ontology.Ontology;
import it.unibz.inf.ontop.ontology.impl.OntologyFactoryImpl;
import it.unibz.inf.ontop.owlapi.OWLAPITranslatorUtility;
import it.unibz.inf.ontop.owlapi.directmapping.DirectMappingEngine;
import it.unibz.inf.ontop.sql.DBMetadata;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;
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

	protected transient OWLOntologyManager man = OWLManager.createOWLOntologyManager();
	private IQuest questInstance;
	
	
	private boolean isinitalized = false;
	
	public QuestDBVirtualStore(String name, QuestPreferences config) throws Exception {
		this(name, null, config);
	}

	@Override
	public QuestPreferences getPreferences() {
		return questInstance.getPreferences();
	}

	/**
	 * The method generates the OBDAModel according to the MappingParser
     * implementation deduced from the preferences.
     *
	 * @return the generated OBDAModel
	 * @throws IOException
	 * @throws InvalidMappingException
     *
	 */
	public OBDAModel getObdaModel(QuestPreferences config) throws IOException, InvalidMappingException,
            DuplicateMappingException, InvalidDataSourceException {
        NativeQueryLanguageComponentFactory nativeQLFactory = getNativeQLFactory();

		Optional<MappingParser> optionalMappingParser = config.getMappingFile()
				.map(nativeQLFactory::create)
				.map(Optional::of)
				.orElseGet(() -> config.getMappingModel()
						.map(nativeQLFactory::create)
						.map(Optional::of)
						.orElseGet(() -> config.getMappingReader()
								.map(nativeQLFactory::create)));
        if (optionalMappingParser.isPresent()) {
			return optionalMappingParser.get().getOBDAModel();
		}
		else {
			return config.getPredefinedOBDAModel()
					.orElseThrow(() -> new IllegalStateException("Invalid configuration not detected: " +
							"no mapping in virtual mode"));
		}
	}

	/**
	 * The constructor to setup Quest virtual store given
	 * an owl file URI and an obda or R2rml mapping file URI
	 * @param name - the name of the triple store
	 * @param tboxFile - the owl file URI
	 * @param config - QuestPreferences
	 * @throws Exception
	 */
	public QuestDBVirtualStore(@Nonnull String name, URI tboxFile, @Nonnull QuestPreferences config) throws Exception {

		super(name, config);

		// TODO: re-cast the exception to a Sesame-specific one
		config.validate();

		//we are working in virtual mode
		if (!config.getProperty(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL))
			throw new IllegalArgumentException("Virtual mode was expected in QuestDBVirtualStore!");

		//obtain the model
		OBDAModel obdaModel = getObdaModel(config);


		//obtain the ontology
		Ontology tbox;
		if (tboxFile != null) {
			//read owl file
			OWLOntology owlontology = getOntologyFromFile(tboxFile);
			//get transformation from owlontology into ontology
			 tbox = OWLAPITranslatorUtility.translateImportsClosure(owlontology);

		} else { 
			// create empty ontology
			//owlontology = man.createOntology();
			OntologyVocabulary voc = OntologyFactoryImpl.getInstance().createVocabulary();
			tbox = OntologyFactoryImpl.getInstance().createOntology(voc);
			if (obdaModel.getSources().size() == 0) {
                Set<OBDADataSource> dataSources = new HashSet<>(obdaModel.getSources());
                dataSources.add(getMemOBDADataSource("MemH2"));
                obdaModel = obdaModel.newModel(dataSources, obdaModel.getMappings());
            }
		}
		obdaModel.getOntologyVocabulary().merge(tbox.getVocabulary());
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
	public QuestDBVirtualStore(String name, OWLOntology tbox, DBMetadata metadata,
                               QuestPreferences config) throws Exception {
		//call super constructor -> QuestDBAbstractStore
		super(name, config);
		
		//obtain ontology
		Ontology ontology = OWLAPITranslatorUtility.translateImportsClosure(tbox);

        MappingParser mappingParser = getNativeQLFactory().create(mappings);
        OBDAModel obdaModel = mappingParser.getOBDAModel();

		obdaModel.getOntologyVocabulary().merge(ontology.getVocabulary());
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

	
	private void setupQuest(Ontology tbox, OBDAModel obdaModel, DBMetadata metadata,
                            QuestPreferences pref) throws Exception {
        QuestComponentFactory factory = getComponentFactory();

		//start Quest with the given ontology and model and preferences
		questInstance = factory.create(tbox, obdaModel, metadata, pref);
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
	 * Calls {@link IQuest.setupRepository()}
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
