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

public class ValuesVirtualTest extends AbstractVirtualModeTest {
	
	private static final String owlfile = "/mysql/person/person.owl";
	private static final String obdafile = "/mysql/person/person.obda";
	private static final String propertiesfile = "/mysql/person/person.properties";
	private static final String PREFIX = "PREFIX : <http://www.semanticweb.org/mindaugas/ontologies/2013/9/untitled-ontology-58#> ";

	public ValuesVirtualTest() {
		super(owlfile, obdafile, propertiesfile);
	}

	private void runTest(String query, int expectedRows) throws Exception {
		countResults(PREFIX + "\n" + query, expectedRows);	
	}

	@Test
	public void testQ01() throws Exception {
		String query1 =
				"SELECT * " +
				"WHERE {" +
				"   ?p a :Person ; :name ?name . " +
				"}";
		runTest(query1, 4);
	}

	@Test
	public void testQ02() throws Exception {
		String query2 =
				"SELECT * " +
				"WHERE {" +
				"   ?p a :Person ; :name ?name . " +
				"   FILTER ( ?name = \"Alice\" ) " +
				"}";
		runTest(query2, 1);
	}

	@Test
	public void testQ03() throws Exception {
		String query3 =
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Alice\" } " +
				"}";
		runTest(query3, 1);
	}

	@Test
	public void testQ04() throws Exception {
		String query4 =
				"SELECT * " +
				"WHERE {" +
				"   ?p a :Person ; :name ?name . " +
				"   FILTER ( ?name = \"Alice\" || ?name = \"Bob\" ) " +
				"}";
		runTest(query4, 2);
	}

	@Test
	public void testQ05() throws Exception {
		String query5 =
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Alice\" \"Bob\" } " +
				"}";
		runTest(query5, 2);
	}

	@Test
	public void testQ06() throws Exception {
		String query6 =
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  FILTER ( ?name  = \"Alice\" && ?age = 18 ) " +
				"}";
		runTest(query6, 1);
	}

	@Test
	public void testQ07() throws Exception {
		String query7 =
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  VALUES (?name ?age) { (\"Alice\" 18) } " +
				"}";
		runTest(query7, 1);
	}

	@Test
	public void testQ08() throws Exception {
		String query8 =
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Nobody\" } " +
				"}";
		runTest(query8, 0);
	}

	@Test
	public void testQ09() throws Exception {
		String query9 =
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  VALUES (?name ?age) { (\"Alice\" UNDEF) } " +
				"}";
		runTest(query9, 1);
	}

	@Test
	public void testQ10() throws Exception {
		String query10 =
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  VALUES (?name ?age) { (\"Mark\" UNDEF) } " +
				"}";
		runTest(query10, 0);
	}

	@Test
	public void testQ11() throws Exception {
		String query11 =
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ." +
				"  VALUES (?name ?age) { " +
				"     (\"Alice\" 18) " +
				"     (\"Bob\" 19) " +
				"  } " +
				"}";
		runTest(query11, 2);
	}


	/**
	 * Not yet supported
	 */
//	public void testQ12() throws Exception {
//		String query12 =  PREFIX +
//				"SELECT * " +
//				"WHERE {" +
//				"  VALUES (?name ?age) { " +
//				"     (\"Alice\" 18) " +
//				"     (\"Bob\" 19) " +
//				"  } " +
//				"  ?p a :Person ; :name ?name ; :age ?age ." +
//				"}";
//		runTest(query12, 2);
//	}

	@Test
	public void testQ12b() throws Exception {
		String query12 =
				"SELECT * " +
				"WHERE {" +
				"  VALUES (?name ?age) { " +
				"     (\"Alice\" 18) " +
				"     (\"Bob\" 19) " +
				"  } " +
				"  { ?p a :Person ; :name ?name ; :age ?age . }" +
				"}";
		runTest(query12, 2);
	}

	@Test
	public void testQ13() throws Exception {
		String query13 =
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Alice\" \"Bob\" \"Eve\" } " +
				"}";
		runTest(query13, 3);
	}

	@Test
	public void testQ14() throws Exception {
		String query14 =
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Alice\" \"Bob\" \"Eve\" \"Mark\" } " +
				"}";
		runTest(query14, 4);
	}

	@Test
	public void testQ15() throws Exception {
		String query15 =
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ." +
				"  VALUES ?name { \"Alice\" \"Bob\" \"Eve\" \"Mark\" \"Nobody\" } " +
				"}";
		runTest(query15, 4);
	}

	@Test
	public void testQ16() throws Exception {
		String query16 =
				"SELECT * " +
				"WHERE {" +
				"  ?p a :Person ; :name ?name ; :age ?age ; :mbox ?mbox ." +
				"  VALUES (?name ?age ?mbox) { " +
				"     (\"Alice\" 18 \"alice@example.org\" ) " +
				"     (\"Bob\" 19 \"bob@example.org\" ) " +
				"     (\"Eve\" 20 \"eve@example.org\" ) " +
				"  } " +
				"}";
		runTest(query16, 3);
	}

}
