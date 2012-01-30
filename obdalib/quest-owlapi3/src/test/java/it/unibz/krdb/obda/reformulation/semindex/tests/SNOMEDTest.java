package it.unibz.krdb.obda.reformulation.semindex.tests;

import it.unibz.krdb.obda.io.QueryStorageManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.querymanager.QueryController;

import java.io.File;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SNOMEDTest {
	public static void main(String args[]) throws Exception {
		
		Logger log = LoggerFactory.getLogger("SNOMEDTEST");

		String owlfile = "/Users/mariano/Downloads/SnomedCT_INT_20110731/res_StatedOWLF_Core_INT_20110731.owl";
		
		log.info("Loading SNOMED");

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();

		OBDAModel obdaModel = fac.getOBDAModel();

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_ONTOLOGY, "false");

		p.setCurrentValueOf(QuestPreferences.DBTYPE, QuestConstants.SEMANTIC);

		/*
		 * Loading the queries (we have 11 queries)
		 */

		QueryController qcontroller = new QueryController();
		QueryStorageManager qman = new QueryStorageManager(qcontroller);

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		log.info("Creating the reasoner");
		
		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
		
		log.info("Done");
		
		reasoner.dispose();
		
		

	}
}
