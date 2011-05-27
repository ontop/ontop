package org.obda.reformulation.tests;

import inf.unibz.it.obda.owlapi.ReformulationPlatformPreferences;
import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.QueryResultSet;
import it.unibz.krdb.obda.model.Statement;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.model.impl.OBDAModelImpl;

import java.io.File;

import junit.framework.TestCase;

import org.obda.owlrefplatform.core.OBDAOWLReformulationPlatform;
import org.obda.owlrefplatform.core.OBDAOWLReformulationPlatformFactory;
import org.obda.owlrefplatform.core.OBDAOWLReformulationPlatformFactoryImpl;
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
		OBDAOWLReformulationPlatformFactory factory = new OBDAOWLReformulationPlatformFactoryImpl();
		
		ReformulationPlatformPreferences p = new ReformulationPlatformPreferences();
		
		factory.setOBDAController(controller);
		factory.setPreferenceHolder(p);
		
		OBDAOWLReformulationPlatform reasoner = (OBDAOWLReformulationPlatform) factory.createReasoner(manager);
		
		reasoner.loadOntologies(manager.getOntologies());
		
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
		
		Statement st = reasoner.getStatement();
		QueryResultSet r = st.executeQuery(sparqlstr);
		int ic = r.getColumCount();
		while (r.nextRow()) {
			for (int i = 0; i < ic; i++) {
				System.out.print(r.getAsString(i+1) + ", ");
			}
			System.out.println("");
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
				System.out.print(r.getAsString(i+1) + ", ");
			}
			System.out.println("");
		}
		r.close();
		st.close();
		assertTrue(count == 1);
		
		
		
	}

}
