package it.unibz.krdb.obda.reformulation.tests;

import it.unibz.krdb.obda.io.DataManager;
import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.OBDAModel;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlapi3.OWLResultSet;
import it.unibz.krdb.obda.owlapi3.OWLStatement;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWL;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.QuestOWLFactory;
import it.unibz.krdb.obda.querymanager.QueryController;
import it.unibz.krdb.obda.reformulation.tests.StockExchangeTestH2.Result;
import it.unibz.krdb.obda.reformulation.tests.StockExchangeTestH2.TestQuery;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MergeScenarioTestH2 extends TestCase{

	private OBDADataFactory fac;
	private Connection conn;

	Logger log = LoggerFactory.getLogger(this.getClass());
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	List<TestQuery> testQueries = new LinkedList<TestQuery>();

	final String owlfile = "src/test/resources/test/merge/Reading.owl";
	final String obdafile = "src/test/resources/test/merge/Reading.obda";
	
	
	

	@Override
	public void setUp() throws Exception {
		/*
		 * Initializing and H2 database with the stock exchange data
		 */
//		String driver = "org.h2.Driver";
		String url = "jdbc:h2:tcp://localhost/test";
		String username = "sa";
		String password = "";

		fac = OBDADataFactoryImpl.getInstance();

		conn = DriverManager.getConnection(url, username, password);
		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntologyIRIMapper iriMapper = new AutoIRIMapper(new File(owlfile).getParentFile(), false);
		manager.addIRIMapper(iriMapper);
		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));

		// Loading the OBDA data
		obdaModel = fac.getOBDAModel();
		ModelIOManager modelIO = new ModelIOManager(obdaModel);
		modelIO.load(new File(new File(obdafile).toURI()));
	//	DataManager ioManager = new DataManager(obdaModel, new QueryController());
	//	ioManager.loadOBDADataFromURI(new File(obdafile).toURI(), ontology.getOntologyID().getOntologyIRI().toURI(), obdaModel.getPrefixManager());
	}
	
	@Override
	public void tearDown() throws Exception {
		try {
			conn.close();
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
	}
	
	private void runTests(Properties p) throws Exception {

		// Creating a new instance of the reasoner
		QuestOWLFactory factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);
	
		factory.setPreferenceHolder(p);

		QuestOWL reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
		reasoner.loadOBDAModel(obdaModel);

		// Now we are ready for querying
		OWLStatement st = reasoner.getStatement();

	
	String query = "PREFIX person: <http://www.semanticweb.org/tibagosi/ontologies/2012/11/Ontology1355819625360.owl#>\n" +
			"PREFIX book: <http://www.semanticweb.org/tibagosi/ontologies/2012/11/Ontology1355819752067.owl#>\n" +
			"SELECT * WHERE " +
			"{?person a person:Person. " +
			"?book a book:Book. " +
			"?person book:isReading ?book.}";	
	
			log.debug("Query: {}", query);
			
			
			int count = 0;
			long start = System.currentTimeMillis();
			long end = 0;
			try {
				OWLResultSet rs = st.execute(query);
				end = System.currentTimeMillis();
				while (rs.nextRow()) {
					count += 1;
				}
			} catch (Exception e) {
				log.debug(e.getMessage(), e);
				end = System.currentTimeMillis();
				count = -1;
			}

		
		

		/* Closing resources */
		st.close();
		reasoner.dispose();

		boolean fail = false;
		/* Comparing and printing results */

		System.out.println("RESULTS COUNT: "+count);
		assertFalse(fail);
	}

	public void testViEqSig() throws Exception {
		
		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		p.setProperty("rewrite", "true");
		
		runTests(p);
	}
	
	
	
}
