package org.semanticweb.ontop.unfold;

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

import org.semanticweb.ontop.exception.DuplicateMappingException;
import org.semanticweb.ontop.exception.InvalidMappingException;
import org.semanticweb.ontop.io.InvalidDataSourceException;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import java.io.IOException;

import org.junit.Test;
import org.semanticweb.ontop.test.AbstractQuestOWLTest;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Class to check the translation of the combination of Optional/Union in SPARQL into Datalog, and finally 
 * SQL
 * @author Minda, Guohui, mrezk
 */
public class LeftJoinTest1Virtual extends AbstractQuestOWLTest {
	//private static Logger log = LoggerFactory.getLogger(LeftJoinTest1Virtual.class);

    public LeftJoinTest1Virtual() throws IOException, InvalidMappingException, OWLOntologyCreationException, DuplicateMappingException, InvalidDataSourceException {
        super(new QuestPreferences());
    }

    @Test
	public void testLeftJoin1() throws Exception {

		String query2 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {?p a :Person . ?p :name ?name . "
				+ "  OPTIONAL {?p :nick11 ?nick1} "
				+ "  OPTIONAL {?p :nick22 ?nick2} }";
		runTests(query2,4);
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

		runTests(query6,4);
	}

	@Test
	public void testLeftJoin3() throws Exception {
		
		String query5 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "  ?p a :Person . "
				+ "  ?p :name ?name ."
				+ "    OPTIONAL {?p :age ?age} }";

		runTests(query5,4);
	}
	@Test
	public void testLeftJoin4() throws Exception {
		String query4 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ " ?p a :Person . ?p :name ?name . "
				+ "  OPTIONAL {?p :nick1 ?nick1} "
				+ "  OPTIONAL {?p :nick2 ?nick2} }";

	
		
		QuestPreferences p = new QuestPreferences();
		runTests(query4,4);
	}	
	
	
	@Test
	public void testLeftJoin5() throws Exception {
		String query3 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * "
				+ "WHERE {"
				+ "?p a :Person . ?p :name ?name . "
				+ "		OPTIONAL {?p :nick11 ?nick1} }";
		
		
		QuestPreferences p = new QuestPreferences();
		runTests(query3, 4);
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

		runTests(query7, 4);
	}	
	
	@Test
	public void testLeftJoin7() throws Exception {
		
		String query1 = "PREFIX : <http://www.example.org/test#> "
				+ "SELECT DISTINCT * WHERE "
				+ "{?p a :Person . ?p :name ?name . ?p :age ?age }";

		runTests(query1,3);
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

		runTests(query_multi7,4);
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
		

		runTests(query_multi6,4);
	}	




	@Test
	public void testLeftJoin10() throws Exception {
		
		String query_multi = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE "
				+ "{?p a :Person . OPTIONAL {{?p :salary ?salary .} UNION   {?p :name ?name .}}}";

		runTests(query_multi,4);
	}	
	
	@Test
	public void testLeftJoin11() throws Exception {
		
		String query_multi1 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . ?p :name ?name }";

		runTests(query_multi1,4);
	}		
	
	@Test
	public void testLeftJoin12() throws Exception {
		
		String query_multi2 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . OPTIONAL {?p :name ?name} }";

		runTests(query_multi2,4);
	}		
	
	@Test
	public void testLeftJoin13() throws Exception {
		
		String query_multi3 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p :name ?name . OPTIONAL {?p :nick1 ?nick1} }";

		runTests(query_multi3,4);
	}		
	@Test
	public void testLeftJoin14() throws Exception {
		

		String query_multi4 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . OPTIONAL {?p :name ?name . OPTIONAL {?p :nick1 ?nick1} } }";

		runTests(query_multi4,4);
	}		
	@Test
	public void testLeftJoin15() throws Exception {
		

		String query_multi5 = "PREFIX : <http://www.example.org/test#> SELECT DISTINCT * WHERE {?p a :Person . OPTIONAL {?p :name ?name . OPTIONAL {?p :nick1 ?nick1} OPTIONAL {?p :nick2 ?nick2} } }";

		runTests(query_multi5,4);
	}	


}
