package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLConnection;
import it.unibz.krdb.obda.owlapi3.OWLResultSet;
import it.unibz.krdb.obda.owlapi3.OWLStatement;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * A simple test that check if the system is able to handle Mappings for
 * classes/roles and attributes even if there are no URI templates. i.e., the
 * database stores URI's directly.
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class BSBMMappingVirtualABoxTest extends TestCase {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private OBDADataFactory fac;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/test/resources/test/bsbm/bsbm.owl";
	final String obdafile = "src/test/resources/test/bsbm/bsbm200.obda";

	@Override
	public void setUp() throws Exception {
		
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();
		
		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(obdafile);
		
	}

//	@Override
//	public void tearDown() throws Exception {
//		
//	}

	private void runTests(Properties p) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

		// Now we are ready for querying
		OWLConnection conn = reasoner.getConnection();
		OWLStatement st = conn.createStatement();

		String query = "PREFIX bsbm: <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/vocabulary/>\n" +
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
				"PREFIX dc: <http://purl.org/dc/elements/1.1/>\n" +
				"\n" +
				"SELECT DISTINCT ?offer ?price\n" +
				"WHERE {\n" +
				"	?offer bsbm:product <http://www4.wiwiss.fu-berlin.de/bizer/bsbm/v01/instances/dataFromProducer5749/Product291706> .\n" +
				"	?offer bsbm:vendor ?vendor .\n" +
				"    ?offer dc:publisher ?vendor .\n" +
				"	?vendor bsbm:country <http://downlode.org/rdf/iso-3166/countries#US> .\n" +
				"	?offer bsbm:deliveryDays ?deliveryDays .\n" +
				"	FILTER (?deliveryDays <= 3)\n" +
				"	?offer bsbm:price ?price .\n" +
				"    ?offer bsbm:validTo ?date .\n" +
				"    FILTER (?date > \"2008-06-20T00:00:00\"^^<http://www.w3.org/2001/XMLSchema#dateTime> )\n" +
				"}\n" +
				"ORDER BY xsd:double(str(?price))\n" +
				"LIMIT 10\n";
		try {
			OWLResultSet rs = st.executeTuple(query);
			int cc = rs.getColumCount();
			List<String> signature = rs.getSignature();
			while (rs.nextRow()) {
				for (int i = 0; i <= cc; i++) {
					System.out.println(signature.get(i)+" = " + rs.getOWLObject(i));
				}
			}
			
			

		} catch (Exception e) {
			throw e;
		} finally {
			try {

			} catch (Exception e) {
				st.close();
			}
			conn.close();
			reasoner.dispose();
		}
	}

	public void testViEqSig() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");

		runTests(p);
	}
}
