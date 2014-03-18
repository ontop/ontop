package it.unibz.krdb.obda.subclass;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLConnection;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLResultSet;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLStatement;

import java.io.File;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubDescriptionTest extends TestCase {

	private OBDADataFactory fac;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	final String owlfile = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange.owl";
	final String obdafile = "src/main/resources/testcases-scenarios/virtual-mode/stockexchange/simplecq/stockexchange-mysql.obda";
	private QuestOWL reasoner;
	private QuestOWLConnection conn;

	@Override
	@Before
	public void setUp() throws Exception {
		try {

			// Loading the OWL file
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

			// Loading the OBDA data
			fac = OBDADataFactoryImpl.getInstance();
			obdaModel = fac.getOBDAModel();

			ModelIOManager ioManager = new ModelIOManager(obdaModel);
			ioManager.load(obdafile);

			QuestPreferences p = new QuestPreferences();
			p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
			p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);
			p.setCurrentValueOf(QuestPreferences.ENTAILMENTS_SPARQL, QuestConstants.TRUE);
			// Creating a new instance of the reasoner
			QuestOWLFactory factory = new QuestOWLFactory();
			factory.setOBDAController(obdaModel);

			factory.setPreferenceHolder(p);

			reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());

			// Now we are ready for querying
			conn = reasoner.getConnection();
		} catch (Exception exc) {
			try {
				tearDown();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

	}

	@After
	public void tearDown() throws Exception {
		conn.close();
		reasoner.dispose();
	}

	private void runTests(String query) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		String retval;
		boolean result = false;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);

			while (rs.nextRow()) {
				result = true;
				OWLIndividual xsub = rs.getOWLIndividual("x");
				OWLIndividual y = rs.getOWLIndividual("y");
				retval = xsub.toString() + " subOf " + y.toString();
				log.info(retval);
			}
			assertTrue(result);

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

	public void testSubClass() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {?x rdfs:subClassOf ?y }";
		runTests(query);

	}

	public void testSubProperty() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {?x rdfs:subPropertyOf ?y }";
		runTests(query);

	}

	public void testOneSubClass() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {?y rdfs:subClassOf :FinantialInstrument }";
		QuestOWLStatement st = conn.createStatement();
		String retval;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);

			assertTrue(rs.nextRow());
			OWLIndividual y = rs.getOWLIndividual("y");
			retval = y.toString();
			assertEquals("<http://www.owl-ontologies.com/Ontology1207768242.owl#Stock>", retval);
			log.info(retval);

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

	public void testOneSubProperty() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {?y rdfs:subPropertyOf <http://www.owl-ontologies.com/Ontology1207768242.owl#inverse_of_test1> }";
		QuestOWLStatement st = conn.createStatement();
		String retval;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);

			assertTrue(rs.nextRow());
			OWLIndividual y = rs.getOWLIndividual("y");
			retval = y.toString();
			assertEquals("<http://www.owl-ontologies.com/Ontology1207768242.owl#inverse_test2>", retval);
			log.info(retval);

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

}
