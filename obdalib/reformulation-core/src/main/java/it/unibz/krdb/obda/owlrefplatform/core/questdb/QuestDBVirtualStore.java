package it.unibz.krdb.obda.owlrefplatform.core.questdb;

import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.Quest;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.ontology.Ontology;
import it.unibz.krdb.obda.owlrefplatform.core.translator.OWLAPI2Translator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
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

	public QuestDBVirtualStore(String name, URI tboxFile, OBDAModel obdaModel, ReformulationPlatformPreferences config) throws Exception {

		super(name);

		if (!config.getProperty(ReformulationPlatformPreferences.ABOX_MODE).equals(QuestConstants.VIRTUAL))
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
