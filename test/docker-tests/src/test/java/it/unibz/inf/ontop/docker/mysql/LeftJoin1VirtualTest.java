package it.unibz.inf.ontop.docker.mysql;

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import it.unibz.inf.ontop.owlapi.OntopOWLReasoner;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLConnection;
import it.unibz.inf.ontop.owlapi.connection.OntopOWLStatement;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Class to check the translation of the combination of Optional/Union in SPARQL into Datalog, and finally 
 * SQL
 * @author Minda, Guohui, mrezk
 */
public class LeftJoin1VirtualTest extends AbstractVirtualModeTest {
	//private static Logger log = LoggerFactory.getLogger(LeftJoinTest1Virtual.class);

	private static final String owlfile = "/mysql/person/person.owl";
	private static final String obdafile = "/mysql/person/person1.obda";
	private static final String propertyfile = "/mysql/person/person1.properties";

	private static OntopOWLReasoner REASONER;
	private static OntopOWLConnection CONNECTION;

	@BeforeClass
	public static void before() throws OWLOntologyCreationException {
		REASONER = createReasoner(owlfile, obdafile, propertyfile);
		CONNECTION = REASONER.getConnection();
	}

	@Override
	protected OntopOWLStatement createStatement() throws OWLException {
		return CONNECTION.createStatement();
	}

	@AfterClass
	public static void after() throws OWLException {
		CONNECTION.close();
		REASONER.dispose();
	}

    @Test
	public void testLeftJoin1() throws Exception {

		String query2 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {?p a :Person . ?p :name ?name . "
				+ "  OPTIONAL {?p :nick11 ?nick1} "
				+ "  OPTIONAL {?p :nick22 ?nick2} }";
		countResults(4, query2);
	}
	
	@Test
	public void testLeftJoin2() throws Exception {
		
		String query6 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT * "
				+ "WHERE {"
				+ " ?p a :Person . ?p :name ?name ."
				+ " OPTIONAL {"
				+ "   ?p :nick1 ?nick1 "
				+ "   OPTIONAL {"
				+ "     {?p :nick2 ?nick2 } UNION {?p :nick22 ?nick22} } } }";

		countResults(4, query6);
	}

	@Test
	public void testLeftJoin3() throws Exception {
		
		String query5 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "  ?p a :Person . "
				+ "  ?p :name ?name ."
				+ "    OPTIONAL {?p :age ?age} }";

		countResults(4, query5);
	}
	@Test
	public void testLeftJoin4() throws Exception {
		String query4 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ " ?p a :Person . ?p :name ?name . "
				+ "  OPTIONAL {?p :nick1 ?nick1} "
				+ "  OPTIONAL {?p :nick2 ?nick2} }";

	
		

		countResults(4, query4);
	}	
	
	
	@Test
	public void testLeftJoin5() throws Exception {
		String query3 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "?p a :Person . ?p :name ?name . "
				+ "		OPTIONAL {?p :nick11 ?nick1} }";
		
		

		countResults(4, query3);
	}	
	
	@Test
	public void testLeftJoin6() throws Exception {
		
		String query7 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "  ?p a :Person . "
				+ "  ?p :name ?name ."
				+ "  OPTIONAL {"
				+ "    ?p :nick11 ?nick11 "
				+ "    OPTIONAL { {?p :nick33 ?nick33 } UNION {?p :nick22 ?nick22} } } }";

		countResults(4, query7);
	}	
	
	@Test
	public void testLeftJoin7() throws Exception {
		
		String query1 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * WHERE "
				+ "{?p a :Person . ?p :name ?name . ?p :age ?age }";

		countResults(3, query1);
	}	


	
	@Test
	public void testLeftJoin8() throws Exception {
		
		String query_multi7 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "  ?p a :Person . "
				+ "  OPTIONAL {"
				+ "    ?p :name ?name . "
				+ "    OPTIONAL {"
				+ "      ?p :nick1 ?nick1 "
				+ "      OPTIONAL {?p :nick2 ?nick2. FILTER (?nick2 = 'alice2')} } } }";

		countResults(4, query_multi7);
	}	

	@Test
	public void testLeftJoin9() throws Exception {
		
		String query_multi6 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "  ?p a :Person . "
				+ "  OPTIONAL {"
				+ "    ?p :name ?name . "
				+ "    OPTIONAL {"
				+ "      ?p :nick1 ?nick1 "
				+ "      OPTIONAL {?p :nick2 ?nick2} } } }";
		

		countResults(4, query_multi6);
	}	




	@Test
	public void testLeftJoin10() throws Exception {
		
		String query_multi = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE "
				+ "{?p a :Person . OPTIONAL {{?p :salary ?salary .} UNION   {?p :name ?name .}}}";

		countResults(4, query_multi);
	}	
	
	@Test
	public void testLeftJoin11() throws Exception {
		
		String query_multi1 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . ?p :name ?name }";

		countResults(4, query_multi1);
	}		
	
	@Test
	public void testLeftJoin12() throws Exception {
		
		String query_multi2 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . OPTIONAL {?p :name ?name} }";

		countResults(4, query_multi2);
	}		
	
	@Test
	public void testLeftJoin13() throws Exception {
		
		String query_multi3 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p :name ?name . OPTIONAL {?p :nick1 ?nick1} }";

		countResults(4, query_multi3);
	}		
	
	@Test
	public void testLeftJoin14() throws Exception {
		

		String query_multi4 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . OPTIONAL {?p :name ?name . OPTIONAL {?p :nick1 ?nick1} } }";

		countResults(4, query_multi4);
	}		
	
	@Test
	public void testLeftJoin15() throws Exception {
		

		String query_multi5 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . OPTIONAL {?p :name ?name . OPTIONAL {?p :nick1 ?nick1} OPTIONAL {?p :nick2 ?nick2} } }";

		countResults(4, query_multi5);
	}	
	
	@Test
	public void testLeftJoin16() throws Exception {
		

		String query_multi7 = "PREFIX : <http://www.example.org/test#> SELECT ?person ?name ?nick1 ?nick2 WHERE{ ?person :name ?name . OPTIONAL { { ?person :nick1 ?nick1 } UNION { ?person :nick2 ?nick2 } FILTER ( bound( ?nick1 ) && bound( ?nick2) ) } }";

		countResults(4, query_multi7);
	}
	
	@Test
	public void testLeftJoin17() throws Exception {
		

		String query_multi7 = "PREFIX : <http://www.example.org/test#> SELECT ?person ?name ?nick1 ?nick2 WHERE{ ?person :name ?name . OPTIONAL { { ?person :nick1 ?nick1 } UNION { ?person :nick2 ?nick2 } FILTER ( bound( ?nick1 ) ) } }";

		countResults(4, query_multi7);
	}

	@Test
	public void testUnion1() throws Exception {
		

		String query_multi7 = "PREFIX : <http://www.example.org/test#> SELECT ?person ?nick1 ?nick2 WHERE{ { ?person :nick1 ?nick1 } UNION { ?person :nick2 ?nick2 } FILTER ( bound( ?nick1 ) ) }";

		countResults(2, query_multi7);
	}

	@Test
	public void testUnion2() throws Exception {
		

		String query_multi7 = "PREFIX : <http://www.example.org/test#> SELECT ?person ?nick1 ?nick2 WHERE{ { ?person :nick1 ?nick1 } UNION { ?person :nick2 ?nick2 } }";

		countResults(4, query_multi7);
	}

	@Test
	public void testLeftJoin19() throws Exception {
		

		String query_multi7 = "PREFIX : <http://www.example.org/test#> SELECT ?person ?name ?nick1 ?nick2 WHERE{ ?person :name ?name . OPTIONAL { ?person :nick1 ?nick1 . ?person :nick2 ?nick2 . FILTER ( bound( ?nick1 ) && bound( ?nick2) ) } }";

		countResults(4, query_multi7);
	}

}
