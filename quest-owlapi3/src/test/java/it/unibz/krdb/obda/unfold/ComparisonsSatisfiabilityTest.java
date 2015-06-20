package it.unibz.krdb.obda.unfold;

import org.junit.*;
import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DriverManager;
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
 * This class tests the functionality of the check for satisfiability
 * of comparisons, that is made at the end of the evaluation of the expressions
 * in the body of the intermediate query.
 *  
 * The code for this tests is an adaptation from the class 
 * it.unibz.krdb.sql.TestQuestImplicitDBConstraints
 */
public class ComparisonsSatisfiabilityTest {
	private final static String  OWLFILE = "src/test/resources/comparisons/in.owl" ;
	private final static String OBDAFILE = "src/test/resources/comparisons/in.obda";
	
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
		s.execute("CREATE TABLE TABLE (\"COL\" CHAR);");
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
		if (conn != null)
			conn.close();
		if (reasoner != null)
			reasoner.dispose();
		if (!sqlConnection.isClosed())
			 sqlConnection.close();
	}
	
	@Ignore private String qunfold(String query) throws Exception {
		QuestOWLStatement st = conn.createStatement();
		String result = st.getUnfolding("PREFIX : <http://www.semanticweb.org/ontologies/2015/5/untitled-ontology-1636#> SELECT * WHERE {" + query + "}");
		log.debug(query + " ==> " + result);
		//System.out.println(query + " ==> " + result);
		return result;
	}
	
	@Test public void satisfiable() throws Exception {
		// a > b > c > d > e > f
		assertTrue(qunfold("?a :Gt ?b . ?b :Gt ?c . ?c :Gt ?d . ?d :Gt ?e . ?e :Gt ?f .") != "");

		// x > 3, x > 5, y < x, y < 1
		assertTrue(qunfold("?x :Gt '3'^^xsd:int; :Gt '5'^^xsd:int. ?y :Lt ?x; :Lt '1'^^xsd:int.") != "");
		
		// x < -1, x >= -1.0
		assertEquals(qunfold("?x :Lt '-1'^^xsd:int. ?x :Gte '-1.0'^^xsd:float."), "");
		
		// x = -4, x != -4.0
		assertTrue(qunfold("?x :Eq '-4'^^xsd:int. ?x :Neq '-4.0'^^xsd:float.") != "");
	}
	
	@Test public void unsatisfiable() throws Exception {
		// 3 >= a > b > c > d > e > f > 3
		assertEquals(qunfold("?a :Lte '3'^^xsd:int. ?a :Gt ?b . ?b :Gt ?c . ?c :Gt ?d . ?d :Gt ?e . ?e :Gt ?f . ?f a :Gt3 ."), "");
		
		// a =< b =< c =< d =< e =< a, a != c
		assertEquals(qunfold("?a :Lte ?b. ?b :Lte ?c. ?c :Lte ?d. ?d :Lte ?e. ?e :Lte ?a. ?a :Neq ?c."), "");
		
		// a < b < c < d < e < a
		assertEquals(qunfold("?a :Gt ?b. ?b :Gt ?c. ?c :Gt ?d. ?d :Gt ?e. ?e :Gt ?a."), "");
		
		// x < 2.0, x > 4
		assertEquals(qunfold("?x :Lt '2.0'^^xsd:float. ?x :Gt '4'^^xsd:int."), "");
		
		// x < -1, y > 3, x > y
		assertEquals(qunfold("?x :Lt '-1'^^xsd:int. ?y :Gt '13'^^xsd:int. ?x :Gt ?y."), "");
		
		// x = y, x = z, y != z
		assertEquals(qunfold("?x :Eq ?y. ?x :Eq ?z. ?y :Neq ?z."), "");
		
		// 3 < x =< y < z <= w < 1, 
		assertEquals(qunfold("?x :Gt '3'^^xsd:int. ?x :Lte ?y. ?y :Lt ?z. ?z :Lte ?w. ?w :Lt '1'^^xsd:int."), "");
		
	}
	
	@Test public void disjunctions() throws Exception {
		// x < 1 /\ ( x > 5 \/ x > 3 )
		assertEquals(qunfold("?x :Lt '1'^^xsd:int. ?x a :Gt3or5."), "");
		assertEquals(qunfold("?x :Lt '1.0'^^xsd:float. ?x a :Gt3or5."), "");
		
		// x > 3 /\ ( x > 5 \/ x > 3 )
		assertTrue(qunfold("?x :Gt '3'^^xsd:int. ?x a :Gt3or5.") != "");
		
		// x > 5 /\ ( x > 5 \/ x > 3 )
		assertTrue(qunfold("?x :Gt '5'^^xsd:int. ?x a :Gt3or5.") != "");
		
		// x > 4 /\ x =< 4.0 /\ ( x > 5 \/ x > 3 )
		assertEquals(qunfold("?x :Gt '4'^^xsd:int. ?x :Lte '4.0'^^xsd:float. ?x a :Gt3or5."), "");
		assertEquals(qunfold("?x :Gt '5'^^xsd:int. ?x :Lte '4.0'^^xsd:float. ?x a :Gt3or5."), "");
		
		// (x < 1 \/ x < 2) /\ ( x > 5 \/ x > 3 )
		assertEquals(qunfold("?x a :Lt1or2. ?x a :Gt3or5."), "");
		
		// (x < 1 \/ x > 7) /\ x > 1 /\ x =< 7
		assertEquals(qunfold("?x a :Lt1orGt7; :Gt '1'^^xsd:int; :Lte '7'^^xsd:int"), "");
		
		// (x < 1 \/ x > 7) /\ x > 1.0 /\ x < y /\ y =< 7.0
		assertEquals(qunfold("?x a :Lt1orGt7; :Gt '1.0'^^xsd:float; :Lte ?y. ?y :Lte '7'^^xsd:float"), "");
		
		// (x < 1 \/ x > 7) /\ x >= 2.0 /\ x =< 2
		assertEquals(qunfold("?x a :Lt1orGt7; :Gte '2.0'^^xsd:float; :Lte '2'^^xsd:int."), "");

		// (x < 1 \/ x > 7) /\ x >= 8.0 /\ x =< 9
		assertTrue(qunfold("?x a :Lt1orGt7; :Gte '8.0'^^xsd:float; :Lte '9'^^xsd:int.") != "");
	}

}
