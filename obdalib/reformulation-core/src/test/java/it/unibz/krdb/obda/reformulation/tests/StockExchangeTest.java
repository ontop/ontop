package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.OBDAResultSet;
import it.unibz.krdb.obda.model.OBDAStatement;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi.OBDAOWLReasonerFactory;
import it.unibz.krdb.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.core.QuestOWLFactory;

import java.io.File;

import junit.framework.TestCase;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class StockExchangeTest extends TestCase {
	
	public void test() throws Exception {
		String owlfile = "src/test/resources/test/ontologies/scenarios/stockexchange-workbench.owl";
		
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

		// Loading the OBDA data (note, the obda file must be in the same folder as the owl file
		OBDADataFactory obdafac = OBDADataFactoryImpl.getInstance();
        OBDAModel controller = obdafac.getOBDAModel();
		
		String obdafile = owlfile.substring(0, owlfile.length()-3) + "obda";
		DataManager ioManager = new DataManager(controller);
		ioManager.loadOBDADataFromURI(new File(obdafile).toURI(),ontology.getURI(),controller.getPrefixManager());
		
		// Creating a new instance of a quonto reasoner
		OBDAOWLReasonerFactory factory = new QuestOWLFactory();
		
		ReformulationPlatformPreferences p = new ReformulationPlatformPreferences();
		p.setCurrentValueOf(ReformulationPlatformPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(ReformulationPlatformPreferences.DATA_LOCATION, QuestConstants.PROVIDED);
		p.setCurrentValueOf(ReformulationPlatformPreferences.CREATE_TEST_MAPPINGS, "true");
		p.setCurrentValueOf(ReformulationPlatformPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(ReformulationPlatformPreferences.OBTAIN_FROM_ONTOLOGY, "true");
		p.setCurrentValueOf(ReformulationPlatformPreferences.DBTYPE, QuestConstants.SEMANTIC);
		
//		factory.setOBDAController(controller);
		factory.setPreferenceHolder(p);
		
		QuestOWL reasoner = (QuestOWL) factory.createReasoner(manager);
		reasoner.setPreferences(p);
		reasoner.loadOntologies(manager.getOntologies());
		reasoner.loadOBDAModel(controller);
		
		
		// Loading a set of configurations for the reasoner and giving them to quonto
//		Properties properties = new Properties();
//		properties.load(new FileInputStream(configFile));
//		QuontoConfiguration config = new QuontoConfiguration(properties);
//		reasoner.setConfiguration(config);
		
		// One time classification call.
		reasoner.classify();
		
		// Now we are ready for querying
		
		// The embedded query query
		String sparqlstr = "select * FROM etable (\n\t\t\nPREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> select * where { ?x rdf:type :Address}) t1";
		
		// Getting a prefix for the query
		
		OBDAStatement st = reasoner.getStatement();
		OBDAResultSet r = st.executeQuery(sparqlstr);
		int ic = r.getColumCount();
		while (r.nextRow()) {
			for (int i = 0; i < ic; i++) {
				//System.out.print(r.getAsString(i+1) + ", ");
			}
			//System.out.println("");
		}
		r.close();
		st.close();
		
		// The embedded query query
		sparqlstr = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> select distinct * where { ?x :hasAddress :getAddressObj-991 }";
		
		// Getting a prefix for the query
		
		st = reasoner.getStatement();
		r = st.executeQuery(sparqlstr);
		ic = r.getColumCount();
		int count = 0;
		while (r.nextRow()) {
			count +=1;
			for (int i = 0; i < ic; i++) {
				//System.out.print(r.getAsString(i+1) + ", ");
			}
			//System.out.println("");
		}
		r.close();
		st.close();
		assertTrue(count == 1);
		
		
		
	}

}
