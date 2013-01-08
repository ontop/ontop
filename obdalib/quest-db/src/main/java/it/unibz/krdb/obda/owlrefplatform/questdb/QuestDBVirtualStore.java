package it.unibz.krdb.obda.owlrefplatform.questdb;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAException;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConnection;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.querymanager.QueryController;

import java.io.File;
import java.net.URI;
import java.util.Collections;
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
 * 
 * @author mariano
 * 
 */
public class QuestDBVirtualStore extends QuestDBAbstractStore {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2495624993519521937L;

	private static Logger log = LoggerFactory
			.getLogger(QuestDBVirtualStore.class);

	protected transient OWLOntologyManager man = OWLManager
			.createOWLOntologyManager();

	public QuestDBVirtualStore(String name, URI tboxFile, URI obdaModelURI)
			throws Exception {

		this(name, tboxFile, obdaModelURI, null);

	}

	public QuestDBVirtualStore(String name, URI tboxFile, URI obdaModelURI,
			QuestPreferences config) throws Exception {

		super(name);

		if (config == null) {
			config = new QuestPreferences();
		}
		config.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);

		OWLAPI3Translator translator = new OWLAPI3Translator();
		OWLOntologyIRIMapper iriMapper = new AutoIRIMapper(new File(tboxFile).getParentFile(), false);
		man.addIRIMapper(iriMapper);
		OWLOntology owlontology = man
				.loadOntologyFromOntologyDocument(new File(tboxFile));
		
		Set<OWLOntology> clousure = man.getImportsClosure(owlontology);
		
		Ontology tbox = translator.mergeTranslateOntologies(clousure);

		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

		OBDAModel obdaModel = fac.getOBDAModel();
//		QueryController qcontroller = new QueryController();
		ModelIOManager modelIO = new ModelIOManager(obdaModel);
		modelIO.load(new File(obdaModelURI));
//		DataManager ioManager = new DataManager(obdaModel, qcontroller);
//		ioManager.loadOBDADataFromURI(obdaModelURI, owlontology.getOntologyID()
//				.getOntologyIRI().toURI(), obdaModel.getPrefixManager());

		questInstance = new Quest();
		questInstance.setPreferences(config);
		questInstance.loadTBox(tbox);
		questInstance.loadOBDAModel(obdaModel);
		questInstance.setupRepository();
	}
	
	public QuestConnection getQuestConnection() {
		if (questConn == null) {
			try {
			//	System.out.println("getquestconn..");
				questConn = questInstance.getConnection();
			} catch (OBDAException e) {
				e.printStackTrace();
			}
		}
		return questConn;
	}

}
