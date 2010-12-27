package inf.unibz.it.obda.owlapi;

import inf.unibz.it.obda.api.controller.MappingController;
import inf.unibz.it.obda.domain.OBDAMappingAxiom;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;
import org.obda.query.domain.Query;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;

public class OWLAPIDataManagerTest extends TestCase {

	private final String[][] ORACLE = {
		// The answer for the target queries.
		{"name, lastname, dateofbirth, ssn :- " +
			"Investor(http://www.owl-ontologies.com/ontology.owl#getPersonObj(id)), " +
			"firstName(http://www.owl-ontologies.com/ontology.owl#getPersonObj(id), name), " +
			"lastName(http://www.owl-ontologies.com/ontology.owl#getPersonObj(id), lastname), " +
			"dateOfBirth(http://www.owl-ontologies.com/ontology.owl#getPersonObj(id), dateofbirth), " +
			"ssn(http://www.owl-ontologies.com/ontology.owl#getPersonObj(id), ssn)",
		 ":- hasAddress(http://www.owl-ontologies.com/ontology.owl#getPersonObj(id), " +
		 	"http://www.owl-ontologies.com/ontology.owl#getAddressObj(addressid))",
		 ":- hasAddress(http://www.owl-ontologies.com/ontology.owl#getCompanyObj(id), " +
		 "http://www.owl-ontologies.com/ontology.owl#getAddressObj(addressid))"
		},
		// The answer for the source queries.
		{"select id, name, lastname, dateofbirth, ssn from client",
		 "select id, name, lastname, addressid from client",
		 "select id, addressid from company"
		}
	};

	private OWLAPIController controller;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		String owlfile = "src/test/resources/input.owl";

		// Load the OWL file.
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology =
        	manager.loadOntologyFromPhysicalURI((new File(owlfile)).toURI());

        // Load the OBDA data.
        controller = new OWLAPIController(manager, ontology);
        controller.loadData(new File(owlfile).toURI());
	}

	@Test
	public void testMappings() {
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
						ORACLE[0][i], sTargetQuery);

				// The source queries
				Query sourceQuery = axiom.getSourceQuery();
				String sSourceQuery = sourceQuery.toString();
				assertEquals("Source query is not the same!",
						ORACLE[1][i], sSourceQuery);

				i++; // increment
			}
		}
	}

}
