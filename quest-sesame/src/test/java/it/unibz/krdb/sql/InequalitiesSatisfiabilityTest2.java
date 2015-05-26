package it.unibz.krdb.sql;

import org.junit.*;

import static org.junit.Assert.*;
import it.unibz.krdb.obda.model.OBDAModel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDADataFactory;
import it.unibz.krdb.obda.model.impl.OBDADataFactoryImpl;
import it.unibz.krdb.obda.owlrefplatform.core.QuestConstants;
import it.unibz.krdb.obda.owlrefplatform.core.QuestPreferences;
import it.unibz.krdb.obda.owlrefplatform.owlapi3.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/*
 * The code for this tests is an adaptation from the class 
 * it.unibz.krdb.sql.TestQuestImplicitDBConstraints
 */
public class InequalitiesSatisfiabilityTest2 {
	private final static String  in_owlfile = "src/test/resources/inequalities/in.owl" ;
	private final static String in_obdafile = "src/test/resources/inequalities/in.obda";
	
	private QuestOWLConnection conn;
	private QuestOWLFactory factory;
	
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	private QuestOWL reasoner;
	private Connection sqlConnection;
	
	private static Logger log;
	
	@Before public void initialize() throws Exception {
		log = LoggerFactory.getLogger(this.getClass());
		
		sqlConnection = DriverManager.getConnection("jdbc:h2:mem:countries", "sa", "");
		
		java.sql.Statement s = sqlConnection.createStatement();
		s.execute("CREATE TABLE TABLE (\"COL1\" INT, \"COL2\" INT);");
		s.close();

		// Loading the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(new File(in_owlfile));

		// Loading the OBDA data
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();

		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(in_obdafile);

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);

		// Creating a new instance of the reasoner
		factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);
		
		reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
		conn = reasoner.getConnection();
	}


	@After public void tearDown() throws Exception {
		if (    conn != null) conn.close();
		if (reasoner != null) reasoner.dispose();
		
		if (!sqlConnection.isClosed()) {
			java.sql.Statement s = sqlConnection.createStatement();
			try {
				s.execute("DROP ALL OBJECTS DELETE FILES");
			} catch (SQLException sqle) {
				log.debug("Table not found, not dropping");
			} finally {
				s.close();
				sqlConnection.close();
			}
		}
	}
	
	@Ignore private String qunfold(String query) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		String result = st.getUnfolding("PREFIX : <http://www.semanticweb.org/ontologies/2015/5/untitled-ontology-1636#> SELECT * WHERE {" + query + "}");
		log.debug(query + " ==> " + result);
		return result;
	}
	
	@Test public void test() throws Exception {
		assertFalse(qunfold("?a :Gt ?b. ?b :Gt ?c. ?c :Gt ?d. ?d :Gt ?e. ?e :Gt ?f.") == "");
		assertEquals(qunfold("?a :Lte ?b. ?b :Lte ?c. ?c :Lte ?d. ?d :Lte ?e. ?e :Lte ?a. ?a :Neq ?c."), "");
		assertEquals(qunfold("?a :Gt ?b. ?b :Gt ?c. ?c :Gt ?d. ?d :Gt ?e. ?e :Gt ?a."), "");
		assertEquals(qunfold("?x a :Lt1. ?x a :Gt3."), "");
		assertEquals(qunfold("?x a :Lt1. ?y a :Gt3. ?x :Gt ?y."), "");
		assertFalse(qunfold("?x a :Gt3. ?x a :Gt5.") == "");
	}


}
