package it.unibz.krdb.obda.owlrefplatform.questdb;

import it.unibz.krdb.obda.exception.DuplicateMappingException;
import it.unibz.krdb.obda.gui.swing.exception.InvalidMappingException;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.io.R2RMLReader;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDADataSource;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.RDBMSourceParameterConstants;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.ontology.impl.OntologyFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OBDAModelSynchronizer;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlapi3.directmapping.DirectMappingEngine;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Set;

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

	public QuestDBVirtualStore(String name, URI obdaURI) throws Exception {
		this(name, null, obdaURI, null);
	}
	public QuestDBVirtualStore(String name, URI obdaURI, QuestPreferences config)
			throws Exception {

		this(name, null, obdaURI, config);
	}
	
	//constructors String, URI, URI, (Config)
	public QuestDBVirtualStore(String name, URI tboxFile, URI obdaURI)
			throws Exception {

		this(name, tboxFile, obdaURI, null);

	}
	
	public  OBDAModel getObdaModel(URI obdaURI)
	{
		OBDAModel obdaModel = fac.getOBDAModel();
	//	System.out.println(obdaURI.toString());
		if (obdaURI.toString().endsWith(".obda"))
		{
				ModelIOManager modelIO = new ModelIOManager(obdaModel);
				try {
					modelIO.load(new File(obdaURI));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InvalidMappingException e) {
					e.printStackTrace();
				}
		}
		else if (obdaURI.toString().endsWith(".ttl"))
		{
			R2RMLReader reader = new R2RMLReader(new File(obdaURI));
			obdaModel = reader.readModel(obdaURI);
			
		}
		return obdaModel;
	}

	public QuestDBVirtualStore(String name, URI tboxFile, URI obdaUri,
			QuestPreferences config) throws Exception {

		super(name);
		
		OBDAModel obdaModel = null;
		if (obdaUri == null) {
			obdaModel = getOBDAModelDM();
		} else {
			obdaModel = getObdaModel(obdaUri);
		}
		
		if (config == null) {
			config = new QuestPreferences();
		}
		config.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		
		
		OWLOntology owlontology = null; 
		Ontology tbox;
		if (tboxFile != null)
		{
			OWLAPI3Translator translator = new OWLAPI3Translator();
			OWLOntologyIRIMapper iriMapper = new AutoIRIMapper(new File(tboxFile).getParentFile(), false);
			man.addIRIMapper(iriMapper);
			owlontology = man
				.loadOntologyFromOntologyDocument(new File(tboxFile));
			Set<OWLOntology> clousure = man.getImportsClosure(owlontology);
			
			 tbox = translator.mergeTranslateOntologies(clousure);

		}
		else
		{	//create empty ontology
			owlontology = man.createOntology();//createOntology(OBDADataFactoryImpl.getIRI(name));
			tbox = OntologyFactoryImpl.getInstance().createOntology();
			if (obdaModel.getSources().size() == 0)
				obdaModel.addSource(getMemOBDADataSource("MemH2"));
		}
		
	
		OBDAModelSynchronizer.declarePredicates(owlontology, obdaModel);
		
		questInstance = new Quest();
		questInstance.setPreferences(config);
		questInstance.loadTBox(tbox);
		questInstance.loadOBDAModel(obdaModel);
		questInstance.setupRepository();
	}
	
	public QuestDBVirtualStore(String name, QuestPreferences pref) throws Exception {
		//direct mapping : no tbox, no obda file, repo in-mem h2
		this(name, null, null, pref);
	}
	
	
	private static OBDADataSource getMemOBDADataSource(String name) {

		OBDADataSource obdaSource = OBDADataFactoryImpl.getInstance().getDataSource(URI.create(name));

		String driver = "org.h2.Driver";
		String url = "jdbc:h2:mem:questrepository";
		String username = "sa";
		String password = "";

		obdaSource = fac.getDataSource(URI
				.create("http://www.obda.org/ABOXDUMP"
						+ System.currentTimeMillis()));
		obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_DRIVER,
				driver);
		obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_PASSWORD,
				password);
		obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_URL, url);
		obdaSource.setParameter(RDBMSourceParameterConstants.DATABASE_USERNAME,
				username);
		obdaSource.setParameter(RDBMSourceParameterConstants.IS_IN_MEMORY,
				"true");
		obdaSource.setParameter(
				RDBMSourceParameterConstants.USE_DATASOURCE_FOR_ABOXDUMP,
				"true");
		return (obdaSource);
		
	}
	
	private OBDAModel  getOBDAModelDM() {
		
		DirectMappingEngine dm = new DirectMappingEngine();
		dm.setBaseURI("http://example.com/base");
		try {
			 OBDAModel model = dm.extractMappings(getMemOBDADataSource("H2m"));
			 return model;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DuplicateMappingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public QuestConnection getQuestConnection() {
			try {
			//	System.out.println("getquestconn..");
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
