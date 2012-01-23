package it.unibz.krdb.obda.owlrefplatform.questdb;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;

import java.net.URI;
import java.util.Collections;

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

	private static Logger log = LoggerFactory.getLogger(QuestDBVirtualStore.class);

	public QuestDBVirtualStore(String name, URI tboxFile, OBDAModel obdaModel, QuestPreferences config) throws Exception {

		super(name);

		if (!config.getProperty(QuestPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL))
			throw new Exception("A virtual repository must be created with the VIRTUAL flag in the configuration.");

		OWLAPI2Translator translator = new OWLAPI2Translator();
		OWLOntology owlontology = man.loadOntology(tboxFile);
		Ontology tbox = translator.mergeTranslateOntologies(Collections.singleton(owlontology));
		questInstance = new Quest();
		questInstance.setPreferences(config);
		questInstance.loadTBox(tbox);
		questInstance.loadOBDAModel(obdaModel);
		questInstance.setupRepository();
	}

	@Override
	public void drop() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
