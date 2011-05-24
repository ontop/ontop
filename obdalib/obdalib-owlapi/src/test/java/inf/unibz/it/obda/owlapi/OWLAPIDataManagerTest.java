package inf.unibz.it.obda.owlapi;

import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;
import inf.unibz.it.obda.domain.Query;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class OWLAPIDataManagerTest extends TestCase {

	/** The expected output for old OBDA file (v1) */
	private final String[][] ORACLE_FAIL = {
		// The answer for the target queries.
		{"nullq(id, name, ssn, lastname, dateofbirth) :- " +
			"nullInvestor(nullgetPersonObj(id)), " +
			"nullfirstName(nullgetPersonObj(id), name), " +
			"nulllastName(nullgetPersonObj(id), lastname), " +
			"nulldateOfBirth(nullgetPersonObj(id), dateofbirth), " +
			"nullssn(nullgetPersonObj(id), ssn)",
		 "nullq(id, addressid) :- " +
		 	"nullhasAddress(nullgetPersonObj(id), " +
		 	"nullgetAddressObj(addressid))",
		 "nullq(id, addressid) :- " +
		 	"nullhasAddress(nullgetCompanyObj(id), " +
		 	"nullgetAddressObj(addressid))",
		 "nullq(id, city) :- " +
		 	"nullhasBase(nullgetCompanyObj(id), " +
		 	"nullbaseCity(city))"
		},
		// The answer for the source queries.
		{"select id, name, lastname, dateofbirth, ssn from client",
		 "select id, name, lastname, addressid from client",
		 "select id, addressid from company",
		 "select id, baseCity from company"
		}
	};

	/** The expected output for newer OBDA file (v2 and v3) */
	private final String[][] ORACLE_SUCCESS = {
		// The answer for the target queries.
		{"http://obda.org/mapping/predicates/q(id, name, ssn, lastname, dateofbirth) :- " +
			"http://www.owl-ontologies.com/ontology.owl#Investor(http://www.owl-ontologies.com/ontology.owl#getPersonObj(id)), " +
			"http://www.owl-ontologies.com/ontology.owl#firstName(http://www.owl-ontologies.com/ontology.owl#getPersonObj(id), name), " +
			"http://www.owl-ontologies.com/ontology.owl#lastName(http://www.owl-ontologies.com/ontology.owl#getPersonObj(id), lastname), " +
			"http://www.owl-ontologies.com/ontology.owl#dateOfBirth(http://www.owl-ontologies.com/ontology.owl#getPersonObj(id), dateofbirth), " +
			"http://www.owl-ontologies.com/ontology.owl#ssn(http://www.owl-ontologies.com/ontology.owl#getPersonObj(id), ssn)",
		 "http://obda.org/mapping/predicates/q(id, addressid) :- " +
		 	"http://www.owl-ontologies.com/ontology.owl#hasAddress(http://www.owl-ontologies.com/ontology.owl#getPersonObj(id), " +
		 	"http://www.owl-ontologies.com/ontology.owl#getAddressObj(addressid))",
		 "http://obda.org/mapping/predicates/q(id, addressid) :- " +
		 	"http://www.owl-ontologies.com/ontology.owl#hasAddress(http://www.owl-ontologies.com/ontology.owl#getCompanyObj(id), " +
		 	"http://www.owl-ontologies.com/ontology.owl#getAddressObj(addressid))",
		 "http://obda.org/mapping/predicates/q(id, city) :- " +
		 	"http://www.owl-ontologies.com/ontology-base.owl#hasBase(http://www.owl-ontologies.com/ontology.owl#getCompanyObj(id), " +
		 	"http://www.owl-ontologies.com/ontology-base.owl#baseCity(city))"
		},
		// The answer for the source queries.
		{"select id, name, lastname, dateofbirth, ssn from client",
		 "select id, name, lastname, addressid from client",
		 "select id, addressid from company",
		 "select id, baseCity from company"
		}
	};

	private OWLAPIController controller;

	@Test
	public void testMappingOnVersionOne() throws Exception {

		loadObdaFile("input-v1.owl");

		MappingController mapControl = controller.getMappingController();
		Set<URI> uris = mapControl.getMappings().keySet();
		for (URI uri : uris) {
			ArrayList<OBDAMappingAxiom> axioms = mapControl.getMappings().get(uri);
			int i = 0;
			for (OBDAMappingAxiom axiom : axioms) {
				// The target queries
				Query targetQuery = axiom.getTargetQuery();
				String sTargetQuery = targetQuery.toString();
				assertEquals("Target query is not the same!",
						ORACLE_FAIL[0][i], sTargetQuery);

				// The source queries
				Query sourceQuery = axiom.getSourceQuery();
				String sSourceQuery = sourceQuery.toString();
				assertEquals("Source query is not the same!",
						ORACLE_FAIL[1][i], sSourceQuery);

				i++; // increment
			}
		}
	}

	@Test
	public void testMappingOnVersionTwo() throws Exception {

		loadObdaFile("input-v2.owl");

		MappingController mapControl = controller.getMappingController();
		Set<URI> uris = mapControl.getMappings().keySet();
		for (URI uri : uris) {
			ArrayList<OBDAMappingAxiom> axioms = mapControl.getMappings().get(uri);
			int i = 0;
			for (OBDAMappingAxiom axiom : axioms) {
				// The target queries
				Query targetQuery = axiom.getTargetQuery();
				String sTargetQuery = targetQuery.toString();
				assertEquals("Target query is not the same!",
						ORACLE_SUCCESS[0][i], sTargetQuery);

				// The source queries
				Query sourceQuery = axiom.getSourceQuery();
				String sSourceQuery = sourceQuery.toString();
				assertEquals("Source query is not the same!",
						ORACLE_SUCCESS[1][i], sSourceQuery);

				i++; // increment
			}
		}
	}

	@Test
	public void testMappingOnVersionThree() throws Exception {

		loadObdaFile("input-v3.owl");

		MappingController mapControl = controller.getMappingController();
		Set<URI> uris = mapControl.getMappings().keySet();
		for (URI uri : uris) {
			ArrayList<OBDAMappingAxiom> axioms = mapControl.getMappings().get(uri);
			int i = 0;
			for (OBDAMappingAxiom axiom : axioms) {
				// The target queries
				Query targetQuery = axiom.getTargetQuery();
				String sTargetQuery = targetQuery.toString();
				assertEquals("Target query is not the same!",
						ORACLE_SUCCESS[0][i], sTargetQuery);

				// The source queries
				Query sourceQuery = axiom.getSourceQuery();
				String sSourceQuery = sourceQuery.toString();
				assertEquals("Source query is not the same!",
						ORACLE_SUCCESS[1][i], sSourceQuery);

				i++; // increment
			}
		}
	}

	private void loadObdaFile(String filename) throws Exception {
		super.setUp();

		String owlfile = "src/test/resources/" + filename;

		// Load the OWL file.
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology =
        	manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

        String obdafilestr = owlfile.substring(0, owlfile.length() -3) + "obda";
        URI obdafileuri = new File(obdafilestr).toURI();
        // Load the OBDA data.
        
        
        controller = new OWLAPIController();
        controller.getIOManager().loadOBDADataFromURI(obdafileuri,ontology.getURI(),controller.getPrefixManager());
	}
}
