package it.unibz.krdb.obda.owlrefplatform.questdb;

import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlapi3.OWLAPI3Translator;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;

import java.io.File;
import java.net.URI;
import java.util.Collections;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * An instance of Store that encapsulates all the functionality needed for a
 * "classic" store.
 * 
 * @author mariano
 * 
 */
public class QuestDBClassicStore extends QuestDBAbstractStore {

	// TODO all this needs to be refactored later to allow for transactions,
	// autocommit enable/disable, clients ids, etc

	private static final long serialVersionUID = 2495624993519521937L;

	private static Logger log = LoggerFactory.getLogger(QuestDBClassicStore.class);
	
	protected transient OWLOntologyManager man = OWLManager.createOWLOntologyManager();

	public QuestDBClassicStore(String name, URI tboxFile) throws Exception {
		this(name, tboxFile, null);

	}

	public QuestDBClassicStore(String name, URI tboxFile, QuestPreferences config) throws Exception {

		super(name);
		if (config == null) {
			config = new QuestPreferences();			
		}
		config.setProperty(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);

		if (!config.getProperty(QuestPreferences.ABOX_MODE).equals(QuestConstants.CLASSIC))
			throw new Exception("A classic repository must be created with the CLASSIC flag in the configuration.");

		OWLAPI3Translator translator = new OWLAPI3Translator();
		OWLOntology owlontology = man.loadOntologyFromOntologyDocument(new File(tboxFile));
		Ontology tbox = translator.mergeTranslateOntologies(Collections.singleton(owlontology));
		questInstance = new Quest();
		questInstance.setPreferences(config);
		questInstance.loadTBox(tbox);
		questInstance.setupRepository();

		log.debug("Store {} has been created successfully", name);
	}



	// /* Move to query time ? */
	// public void deleteAllTriples() {
	//
	// }

	// /* Move to query time ? */
	// public void addOBDAModel(URI model) {
	//
	// }
	//
	// /* Move to query time ? */
	// public void addOBDAModel(URI model, String name) {
	//
	// }
	//
	// /* Move to query time ? */
	// public void dropOBDAModel(URI model) {
	//
	// }
	//
	// /* Move to query time ? */
	// public void dropOBDAModel(String name) {
	//
	// }
//
//	public OBDAModel getOBDAModel(String name) {
//		return null;
//	}
//
//	public List<OBDAModel> getOBDAModels() {
//		return null;
//	}
//

//
//	/* Move to query time ? */
//	public void refreshOBDAData() {
//
//	}
//
//	/* Move to query time ? */
//	public void refreshOBDAData(boolean fast) {
//
//	}

}
