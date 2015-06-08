package it.unibz.krdb.sql;

import org.junit.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import it.unibz.krdb.obda.io.ModelIOManager;
import it.unibz.krdb.obda.model.OBDAModel;
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
public class TestQuestInequalitiesSatisfiability {
	private final static String  OWLFILE = "src/test/resources/inequalities/in.owl" ;
	private final static String OBDAFILE = "src/test/resources/inequalities/in.obda";
	
	private QuestOWLConnection conn;
	private QuestOWLFactory factory;
	
	private OBDAModel obdaModel;
	private OWLOntology ontology;

	private QuestOWL reasoner;
	private Connection sqlConnection;
	
	private static Logger log;
	
	@Before public void initialize() throws Exception {
		log = LoggerFactory.getLogger(this.getClass());
		
		sqlConnection = DriverManager.getConnection("jdbc:h2:mem:test", "sa", "");
		
		java.sql.Statement s = sqlConnection.createStatement();
		s.execute("CREATE TABLE TABLE (\"COL\" FLOAT);");
		s.close();

		// Load the OWL file
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		ontology = manager.loadOntologyFromOntologyDocument(new File(OWLFILE));

		// Load the OBDA data
		OBDADataFactory fac = OBDADataFactoryImpl.getInstance();
		obdaModel = fac.getOBDAModel();

		ModelIOManager ioManager = new ModelIOManager(obdaModel);
		ioManager.load(OBDAFILE);

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);

		// Create a new instance of the reasoner
		factory = new QuestOWLFactory();
		factory.setOBDAController(obdaModel);

		factory.setPreferenceHolder(p);
		
		reasoner = (QuestOWL) factory.createReasoner(ontology, new SimpleConfiguration());
		conn = reasoner.getConnection();
	}


	@After public void clean() throws Exception {
		if (    conn != null) conn.close();
		if (reasoner != null) reasoner.dispose();
		
		if (!sqlConnection.isClosed()) {
				sqlConnection.close();
		}
	}
	
	@Ignore private String qunfold(String query) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		String result = st.getUnfolding("PREFIX : <http://www.semanticweb.org/ontologies/2015/5/untitled-ontology-1636#> SELECT * WHERE {" + query + "}");
		log.debug(query + " ==> " + result);
		System.out.println(query + " ==> " + result);
		return result;
	}
	
	@Test public void test() throws Exception {
		// a > b > c > d > e > f
		assertTrue(qunfold("?a :Gt ?b . ?b :Gt ?c . ?c :Gt ?d . ?d :Gt ?e . ?e :Gt ?f .") != "");
		// a =< b =< c =< d =< e =< a, a != c
		assertEquals(qunfold("?a :Lte ?b. ?b :Lte ?c. ?c :Lte ?d. ?d :Lte ?e. ?e :Lte ?a. ?a :Neq ?c."), "");
		// a < b < c < d < e < a
		assertEquals(qunfold("?a :Gt ?b. ?b :Gt ?c. ?c :Gt ?d. ?d :Gt ?e. ?e :Gt ?a."), "");
		// x < 1, x > 3
		assertEquals(qunfold("?x a :Lt1. ?x a :Gt3."), "");
		// x < 1, y > 3, x > y
		assertEquals(qunfold("?x a :Lt1. ?y a :Gt3. ?x :Gt ?y."), "");
		// x > 3, x > 5, y < x, y < 1
		assertFalse(qunfold("?x a :Gt3. ?x a :Gt5. ?y :Lt ?x. ?y a :Lt1.") == "");
		// x = y, x = z, y != z
		assertEquals(qunfold("?x :Eq ?y. ?x :Eq ?z. ?y :Neq ?z."), "");
		// 3 < x =< y < z <= w < 1, 
		assertEquals(qunfold("?x a :Gt3. ?x :Lte ?y. ?y :Lt ?z. ?z :Lte ?w. ?w a :Lt1."), "");
	}
	
	@Test public void disjunctions() throws Exception {
		// x < 1 /\ ( x > 5 \/ x > 3 )
		assertEquals(qunfold("?x a :Lt1. ?x a :Gt3or5."), "");
		// x > 3 /\ ( x > 5 \/ x > 3 )
		assertTrue(qunfold("?x a :Gt3. ?x a :Gt3or5.") != "");
		// x > 5 /\ ( x > 5 \/ x > 3 )
		assertTrue(qunfold("?x a :Gt5. ?x a :Gt3or5.") != "");
		// x > 5 /\ ( x > 5 \/ x > 3 )
		assertTrue(qunfold("?x a :Gt5. ?x a :Gt3. ?x a :Gt3or5.") != "");
		
		// (x < 1 \/ x < 2) /\ ( x > 5 \/ x > 3 )
		assertEquals(qunfold("?x a :Lt1or2. ?x a :Gt3or5."), "");
	}

}
