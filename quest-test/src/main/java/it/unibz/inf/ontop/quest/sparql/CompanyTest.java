package it.unibz.inf.ontop.quest.sparql;

/*
 * #%L
 * ontop-quest-owlapi3
 * %%
 * Copyright (C) 2009 - 2013 Free University of Bozen-Bolzano
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

import it.unibz.inf.ontop.injection.QuestConfiguration;
import it.unibz.inf.ontop.owlrefplatform.owlapi.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import static junit.framework.TestCase.*;


/***
 * Check how the optional filter is converted in left join
 * We do not support this kind of SPARQL query because it is not a well designed graph pattern
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
public class CompanyTest  {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private static final String owlFile = "resources/optional/company.owl";
	private static final String obdaFile = "resources/optional/company.obda";

	private QuestOWL reasoner;
	private OntopOWLConnection conn;
	Connection sqlConnection;

//	public CompanyTest() {
//		super(owlfile, obdafile);
//	}

	@Before
	public void setUp() throws Exception {

		sqlConnection = DriverManager.getConnection("jdbc:h2:mem:questjunitdb","fish", "fish");
		java.sql.Statement s = sqlConnection.createStatement();
		String text = new Scanner( new File("resources/optional/company-h2.sql") ).useDelimiter("\\A").next();
		s.execute(text);
		s.close();

		QuestConfiguration config = QuestConfiguration.defaultBuilder()
				.ontologyFile(owlFile)
				.nativeOntopMappingFile(obdaFile)
				.build();

		/*
		 * Create the instance of Quest OWL reasoner.
		 */
		QuestOWLFactory factory = new QuestOWLFactory();

		reasoner = factory.createReasoner(config);
		conn = reasoner.getConnection();



	}

	@After
	public void tearDown() throws Exception {
		try {
			dropTables();
			conn.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private void dropTables() throws SQLException, IOException {

		Statement st = sqlConnection.createStatement();

		FileReader reader = new FileReader("resources/optional/drop-company.sql");
		BufferedReader in = new BufferedReader(reader);
		StringBuilder bf = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			bf.append(line);
			line = in.readLine();
		}

		st.executeUpdate(bf.toString());
		st.close();
		sqlConnection.commit();
	}

	private void runTests() throws Exception {

		OntopOWLStatement st = conn.createStatement();

		
		String queryEx =  "PREFIX : <http://it.unibz.krdb/obda/test/company#> SELECT * WHERE"
				+ "{  ?v ?w  ?x } ";
		String query = "PREFIX : <http://it.unibz.krdb/obda/test/company#> SELECT ?y?z WHERE"
				+ "{ ?c a :Company . Filter (?c=:A) OPTIONAL  {  ?x :companyName ?c .  ?x :depName ?y .  FILTER (?y = :HR) OPTIONAL{?z :depId ?x }}}";
		
		String query2 = "PREFIX : <http://it.unibz.krdb/obda/test/company#> SELECT * WHERE"
				+ "{ :A a :Company .  OPTIONAL  {  ?x :companyName :A .  ?x :depName :HR .  OPTIONAL{?z :depId ?x }}}";
		
		
		
		
		//String query = "PREFIX : <http://it.unibz.krdb/obda/test/company#> SELECT ?y?z WHERE"
//				+ "{   ?x :companyName :A .  OPTIONAL  {  :A :hasStatus :G .  ?x :depName :HR .  OPTIONAL {?z :depId ?x }}}";
		
		try {
//			
//			System.out.println(queryEx);
//			
//			QuestOWLResultSet rs = st.executeTuple(queryEx);
//			
//			while (rs.nextRow()){
//				System.out.println(rs.getOWLObject(1));
//				System.out.println(rs.getOWLObject(2));
//				System.out.println(rs.getOWLObject(3));
//
//				}
		
			System.out.println(query);
			
			QuestOWLResultSet rs2 = st.executeTuple(query);
//			while (rs2.nextRow()){
//				System.out.println(rs2.getOWLObject(1));
//				System.out.println(rs2.getOWLObject(2));
//				System.out.println(rs2.getOWLObject(3));
//
//				}
			assertTrue(rs2.nextRow());
			OWLObject ind1 = rs2.getOWLIndividual("y");
			OWLObject ind2 = rs2.getOWLIndividual("z");
			
			assertEquals("<http://it.unibz.krdb/obda/test/company#HR>", ind1.toString());
			assertEquals("<http://it.unibz.krdb/obda/test/company#mark>", ind2.toString());
			
			assertFalse(rs2.nextRow());

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


	@Test
	public void testViEqSig() throws Exception {
		runTests();
	}
	
//	public void testClassicEqSig() throws Exception {
//
//		QuestPreferences p = new QuestPreferences();
//		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.CLASSIC);
//		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
//		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
//		p.setCurrentValueOf(QuestPreferences.OBTAIN_FROM_MAPPINGS, "true");
//
//		runTests(p);
//	}


}
