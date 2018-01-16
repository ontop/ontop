package it.unibz.inf.ontop.docker.mysql;

/*
 * #%L
 * ontop-test
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import it.unibz.inf.ontop.docker.AbstractVirtualModeTest;
import org.junit.Test;

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

    public LeftJoin1VirtualTest() {
        super(owlfile, obdafile, propertyfile);
    }

    @Test
	public void testLeftJoin1() throws Exception {

		String query2 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {?p a :Person . ?p :name ?name . "
				+ "  OPTIONAL {?p :nick11 ?nick1} "
				+ "  OPTIONAL {?p :nick22 ?nick2} }";
		countResults(query2, 4);
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

		countResults(query6, 4);
	}

	@Test
	public void testLeftJoin3() throws Exception {
		
		String query5 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "  ?p a :Person . "
				+ "  ?p :name ?name ."
				+ "    OPTIONAL {?p :age ?age} }";

		countResults(query5,4);
	}
	@Test
	public void testLeftJoin4() throws Exception {
		String query4 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ " ?p a :Person . ?p :name ?name . "
				+ "  OPTIONAL {?p :nick1 ?nick1} "
				+ "  OPTIONAL {?p :nick2 ?nick2} }";

	
		

		countResults(query4,4);
	}	
	
	
	@Test
	public void testLeftJoin5() throws Exception {
		String query3 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "?p a :Person . ?p :name ?name . "
				+ "		OPTIONAL {?p :nick11 ?nick1} }";
		
		

		countResults(query3, 4);
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

		countResults(query7, 4);
	}	
	
	@Test
	public void testLeftJoin7() throws Exception {
		
		String query1 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * WHERE "
				+ "{?p a :Person . ?p :name ?name . ?p :age ?age }";

		countResults(query1,3);
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

		countResults(query_multi7,4);
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
		

		countResults(query_multi6,4);
	}	




	@Test
	public void testLeftJoin10() throws Exception {
		
		String query_multi = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE "
				+ "{?p a :Person . OPTIONAL {{?p :salary ?salary .} UNION   {?p :name ?name .}}}";

		countResults(query_multi,4);
	}	
	
	@Test
	public void testLeftJoin11() throws Exception {
		
		String query_multi1 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . ?p :name ?name }";

		countResults(query_multi1,4);
	}		
	
	@Test
	public void testLeftJoin12() throws Exception {
		
		String query_multi2 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . OPTIONAL {?p :name ?name} }";

		countResults(query_multi2,4);
	}		
	
	@Test
	public void testLeftJoin13() throws Exception {
		
		String query_multi3 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p :name ?name . OPTIONAL {?p :nick1 ?nick1} }";

		countResults(query_multi3,4);
	}		
	
	@Test
	public void testLeftJoin14() throws Exception {
		

		String query_multi4 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . OPTIONAL {?p :name ?name . OPTIONAL {?p :nick1 ?nick1} } }";

		countResults(query_multi4,4);
	}		
	
	@Test
	public void testLeftJoin15() throws Exception {
		

		String query_multi5 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . OPTIONAL {?p :name ?name . OPTIONAL {?p :nick1 ?nick1} OPTIONAL {?p :nick2 ?nick2} } }";

		countResults(query_multi5,4);
	}	
	
	@Test
	public void testLeftJoin16() throws Exception {
		

		String query_multi7 = "PREFIX : <http://www.example.org/test#> SELECT ?person ?name ?nick1 ?nick2 WHERE{ ?person :name ?name . OPTIONAL { { ?person :nick1 ?nick1 } UNION { ?person :nick2 ?nick2 } FILTER ( bound( ?nick1 ) && bound( ?nick2) ) } }";

		countResults(query_multi7,4);
	}
	
	@Test
	public void testLeftJoin17() throws Exception {
		

		String query_multi7 = "PREFIX : <http://www.example.org/test#> SELECT ?person ?name ?nick1 ?nick2 WHERE{ ?person :name ?name . OPTIONAL { { ?person :nick1 ?nick1 } UNION { ?person :nick2 ?nick2 } FILTER ( bound( ?nick1 ) ) } }";

		countResults(query_multi7,4);
	}

	@Test
	public void testUnion1() throws Exception {
		

		String query_multi7 = "PREFIX : <http://www.example.org/test#> SELECT ?person ?nick1 ?nick2 WHERE{ { ?person :nick1 ?nick1 } UNION { ?person :nick2 ?nick2 } FILTER ( bound( ?nick1 ) ) }";

		countResults(query_multi7,2);
	}

	@Test
	public void testUnion2() throws Exception {
		

		String query_multi7 = "PREFIX : <http://www.example.org/test#> SELECT ?person ?nick1 ?nick2 WHERE{ { ?person :nick1 ?nick1 } UNION { ?person :nick2 ?nick2 } }";

		countResults(query_multi7,4);
	}

	@Test
	public void testLeftJoin19() throws Exception {
		

		String query_multi7 = "PREFIX : <http://www.example.org/test#> SELECT ?person ?name ?nick1 ?nick2 WHERE{ ?person :name ?name . OPTIONAL { ?person :nick1 ?nick1 . ?person :nick2 ?nick2 . FILTER ( bound( ?nick1 ) && bound( ?nick2) ) } }";

		countResults(query_multi7,4);
	}

}
