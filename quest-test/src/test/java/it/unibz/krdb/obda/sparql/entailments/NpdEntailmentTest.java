package it.unibz.krdb.obda.sparql.entailments;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.io.QueryIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;
import it.unibz.krdb.obda.querymanager.QueryController;
import it.unibz.krdb.obda.querymanager.QueryControllerGroup;
import it.unibz.krdb.obda.querymanager.QueryControllerQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * Test npd ontology for sparql owl entailments.
 * rdfs:subclass, rdfs:subProperty, owl:inverseof owl:equivalentclass owl:equivalentProperty 
 * owl:propertyDisjointWith rdfs:domain rdfs:range
 * QuestPreferences has SPARQL_OWL_ENTAILMENT  set to true.
 *
 */

public class NpdEntailmentTest {

	private OBDADataFactory fac;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	String owlfile = "src/test/resources/npdEntailment/ontology/npd-v2-ql.owl";
	String obdafile = "src/test/resources/npdEntailment/mappings/mysql/npd-v2-ql-mysql.obda";
	String outputfile = "src/test/resources/npdEntailment/querytime.txt";
	private QuestOWL reasoner;
	private QuestOWLConnection conn;


	@Before
	public void setUp() throws Exception {
		try {
			long start1 = System.currentTimeMillis();
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

			long end1 = System.currentTimeMillis() - start1;
			log.info("---time Reasoner " + end1);
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

	private void generateFile( List<String> results) throws FileNotFoundException, UnsupportedEncodingException {
		/*
		 * Generate File !
		 */
		PrintWriter writer = new PrintWriter(outputfile, "UTF-8");
		for( String result: results) {
			writer.println(result);
		}
		writer.close();

	}

	private List<String> prepareTestQueries(int[] results) throws Exception {
		List<String> resultsList = new ArrayList<String>();
		/*
		 * Loading the queries
		 */
		QuestOWLStatement st = conn.createStatement();
		QueryController qc = new QueryController();
		QueryIOManager qman = new QueryIOManager(qc);
		qman.load("src/test/resources/npdEntailment/queries/npdEntailment.q");

		int nquery = 0;
		for (QueryControllerGroup group : qc.getGroups()) {
			for (QueryControllerQuery query : group.getQueries()) {

				log.debug("Executing query: {}", query.getID());
				log.debug("Query: \n{}", query.getQuery());
				int count = 0, run = 0;
				double totalTime = 0;
				while (run < 3) {
					long start = System.currentTimeMillis();
					QuestOWLResultSet res = st.executeTuple(query.getQuery());
					long end = System.currentTimeMillis();

					double time = (end - start);
					totalTime += time ;


					 count = 0;
					while (res.nextRow()) {
						count += 1;

//                    for (int i = 1; i <= res.getColumnCount(); i++) {
//                        log.debug(res.getSignature().get(i-1) +" = " + res.getOWLObject(i));
//
//                    }
					}


					assertEquals(results[nquery], count);
					log.debug("Elapsed time: {} ms", time);
					run++;
				}
				log.debug("Total result: {}", count);
				resultsList.add(group.getID() + " " + query.getID() + " results:" + count + " " + Math.round(totalTime / 3.0D) + " ms");
				nquery ++;
			}
		}

		return resultsList;
	}



	private int runTests(String query) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		String retval;
		int resultCount = 0;
		try {
			QuestOWLResultSet rs = st.executeTuple(query);

			while (rs.nextRow()) {
				resultCount++;
				for(String variable : rs.getSignature()) {
					OWLIndividual y = rs.getOWLIndividual(variable);
					retval = y.toString();
					System.out.println(variable + ": " +retval);
				}
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
	@Test
	public void testNpdQueries() throws Exception {
		int[] results = new int[]{28666, 204056, 448, 1470, 0, 1170, 408, 28948, 139730};
		generateFile(prepareTestQueries(results));



	}

	@Test
	public void testDomainNpd() throws Exception {
		String query = "PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#> select ?y where { ?y rdfs:domain :FieldReserve .}";
		int domain = runTests(query);
		assertEquals(73, domain);

	}

	@Test
	public void testSubClassNpd() throws Exception {
		String query = "PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#> select ?y where { :ProductionLicence rdfs:subClassOf ?y .}";
		int domain = runTests(query);
		assertEquals(73, domain);

	}

	@Test
	public void testSubClassAndDomainNpd() throws Exception {
		String query = "PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#> select ?y where { :ProductionLicence rdfs:subClassOf ?z . ?y rdfs:domain ?z}";
		int domain = runTests(query);
		assertEquals(73, domain);

	}

	@Test
	public void testdisjointWithNpd() throws Exception {
		String query = "PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#> select ?y where { :FieldReserve owl:disjointWith ?y .}";
		int domain = runTests(query);
		assertEquals(73, domain);

	}

	@Test
	public void testRangeWellboreNpd() throws Exception {
		String query = "PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#> select ?y where { :coreForWellbore rdfs:range ?y .}";
		int domain = runTests(query);
		assertEquals(73, domain);

	}

	@Test
	public void testsubPropertyWellboreNpd() throws Exception {
		String query = "PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#> select ?y where { ?z :coreForWellbore [ rdf:type ?y ] .}";
		int domain = runTests(query);
		assertEquals(73, domain);

	}

	@Test
	public void testResultsNpd() throws Exception {
		String query = "PREFIX : <http://sws.ifi.uio.no/vocab/npd-v2#> select * where { ?x rdfs:subClassOf :Agent . ?licenceURI a ?x . [] :licenseeForLicence ?licenceURI .}";
		int domain = runTests(query);
		assertEquals(73, domain);

	}

	private void runSingleQuery(String groupId, String queryId) throws Exception  {
	/*
		 * Loading the queries
		 */
		QuestOWLStatement st = conn.createStatement();
		QueryController qc = new QueryController();
		QueryIOManager qman = new QueryIOManager(qc);
		qman.load("src/test/resources/npdEntailment/queries/npdEntailment.q");

		QueryControllerQuery query =qc.getGroup(groupId).getQuery(queryId);

		log.debug("Executing query: {}", query.getID());
		log.debug("Query: \n{}", query.getQuery());

		long start = System.currentTimeMillis();
		QuestOWLResultSet res = st.executeTuple(query.getQuery());
		long end = System.currentTimeMillis();

		double time = (end - start) ;

		int count = 0;
		while (res.nextRow()) {
			count += 1;

                    for (int i = 1; i <= res.getColumnCount(); i++) {
                        log.debug(res.getSignature().get(i-1) +" = " + res.getOWLObject(i));

                    }
				}
		log.debug("Total result: {}", count);

		log.debug("Elapsed time: {} ms", time);

	}

	@Test
	public void getOneResult() throws Exception{
		runSingleQuery("TestQueries", "q1Materialize");
	}


	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.err.println("Usage: main ontology.owl mapping.obda outputFile.txt");
			System.exit(0);
		}

		NpdEntailmentTest benchmark = new NpdEntailmentTest();
		benchmark.owlfile = args[0];
		benchmark.obdafile = args[1];
		benchmark.outputfile = args[2];
		benchmark.setUp();
		benchmark.testNpdQueries();
		benchmark.tearDown();
	}

}
