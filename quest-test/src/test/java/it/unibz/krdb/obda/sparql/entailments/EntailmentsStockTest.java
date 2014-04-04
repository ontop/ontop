package it.unibz.krdb.obda.sparql.entailments;

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
import java.util.ArrayList;
import java.util.List;

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

public class EntailmentsStockTest extends TestCase {

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
			p.setCurrentValueOf(QuestPreferences.SPARQL_OWL_ENTAILMENT, QuestConstants.TRUE);
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

	private int runTests(String query, String function) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		String retval;
		int resultCount = 0;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);

			while (rs.nextRow()) {
				resultCount++;
				OWLIndividual xsub = rs.getOWLIndividual("x");
				OWLIndividual y = rs.getOWLIndividual("y");
				retval = xsub.toString() + " " + function + " " + y.toString();
				System.out.println(retval);
			}
			assertTrue(resultCount > 0);

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
		return resultCount;
	}

	public void testSubClass() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {?x rdfs:subClassOf ?y }";
		int numbersub = runTests(query, "rdfs:subClassOf");
		assertEquals(344, numbersub);

	}

	public void testSubProperty() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {?x rdfs:subPropertyOf ?y }";
		int numbersub = runTests(query, "rdfs:subPropertyOf");
		assertEquals(45, numbersub);
	}

	public void testOneSubClass() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {?y rdfs:subClassOf :FinantialInstrument }";
		QuestOWLStatement st = conn.createStatement();
		String retval;
		List<String> named = new ArrayList<String>();
		try {
			QuestOWLResultSet rs = st.executeTuple(query);

			while (rs.nextRow()) {
				OWLIndividual y = rs.getOWLIndividual("y");
				if (y.isNamed())
					named.add(y.toString());
				retval = y.toString();
				log.info(retval);
			}
			assertEquals(named.size(), 2);

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

	public void testEquivalentClass() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {?x owl:equivalentClass ?y }";
		int equivalent = runTests(query, "owl:equivalentClass");
		assertEquals(210, equivalent);

	}

	public void testOneEquivalentClass() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {:Transaction owl:equivalentClass ?y }";
		QuestOWLStatement st = conn.createStatement();
		String retval;
		List<String> named = new ArrayList<String>();
		try {
			QuestOWLResultSet rs = st.executeTuple(query);

			while (rs.nextRow()) {
				OWLIndividual y = rs.getOWLIndividual("y");
				if (y.isNamed())
					named.add(y.toString());
				retval = y.toString();
				log.info(retval);

			}

			assertEquals(named.size(), 1);
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

	public void testOneEquivalencesClass() throws Exception {

		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {?y owl:equivalentClass :Dealer }";
		QuestOWLStatement st = conn.createStatement();
		String retval;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);

			assertTrue(rs.nextRow());
			OWLIndividual y = rs.getOWLIndividual("y");
			retval = y.toString();
			assertEquals("<http://www.owl-ontologies.com/Ontology1207768242.owl#StockTrader>", retval);
			log.info(retval);

			assertTrue(rs.nextRow());
			y = rs.getOWLIndividual("y");
			retval = y.toString();
			assertEquals("<http://www.owl-ontologies.com/Ontology1207768242.owl#Dealer>", retval);
			log.info(retval);

			assertTrue(rs.nextRow());
			y = rs.getOWLIndividual("y");
			retval = y.toString();
			assertEquals("<http://www.owl-ontologies.com/Ontology1207768242.owl#Trader>", retval);
			log.info(retval);

			assertFalse(rs.nextRow());

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

	public void testRange() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {?x rdfs:range ?y }";
		int range = runTests(query, "rdfs:range");
		assertEquals(35, range);

	}

	public void testOneRange() throws Exception {

		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {?x rdfs:range <http://www.owl-ontologies.com/Ontology1207768242.owl#Person> }";
		QuestOWLStatement st = conn.createStatement();
		String retval;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);

			assertTrue(rs.nextRow());
			OWLIndividual x = rs.getOWLIndividual("x");
			retval = x.toString();

			log.info(retval);

			while (rs.nextRow()) {
				x = rs.getOWLIndividual("x");
				retval = x.toString();
				log.info(retval);
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

	public void testDomain() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {?x rdfs:domain ?y }";
		int domain = runTests(query, "rdfs:domain");
		assertEquals(34, domain);

	}

	public void testOneDomain() throws Exception {

		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {<http://www.owl-ontologies.com/Ontology1207768242.owl#hasAddress> rdfs:domain ?x }";
		QuestOWLStatement st = conn.createStatement();
		String retval;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);

			assertTrue(rs.nextRow());
			OWLIndividual x = rs.getOWLIndividual("x");
			retval = x.toString();
			assertEquals("<http://www.owl-ontologies.com/Ontology1207768242.owl#Person>", retval);
			log.info(retval);

			assertFalse(rs.nextRow());

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

	public void testInverses() throws Exception {
		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {?x owl:inverseOf ?y }";
		int inverse = runTests(query, "owl:inverseOf");
		assertEquals(14, inverse);

	}

	public void testOneInverse() throws Exception {

		String query = "PREFIX : <http://www.owl-ontologies.com/Ontology1207768242.owl#> select * where {?x owl:inverseOf <http://www.owl-ontologies.com/Ontology1207768242.owl#hasStock> }";
		QuestOWLStatement st = conn.createStatement();
		String retval;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);

			assertTrue(rs.nextRow());
			OWLIndividual x = rs.getOWLIndividual("x");
			retval = x.toString();
			assertEquals("<http://www.owl-ontologies.com/Ontology1207768242.owl#belongsToCompany>", retval);
			log.info(retval);

			assertFalse(rs.nextRow());

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
