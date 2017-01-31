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


import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLResultSet;
import it.unibz.inf.ontop.owlrefplatform.owlapi.QuestOWLStatement;
import it.unibz.inf.ontop.quest.AbstractVirtualModeTest;
import org.junit.Ignore;
import org.semanticweb.owlapi.model.OWLObject;

/***
 * Check how the optional filter is converted in left join
 * We do not support this kind of SPARQL query because it is not a well designed graph pattern
 * 
 * We are going to create an H2 DB, the .sql file is fixed. We will map directly
 * there and then query on top.
 */
@Ignore("Won't fix because it is not a well-designed BGP")
public class CompanyTest2 extends AbstractVirtualModeTest {

	// TODO We need to extend this test to import the contents of the mappings
	// into OWL and repeat everything taking form OWL

	private static final String owlfile = "resources/optional/company.owl";
	private static final String obdafile = "resources/optional/company.obda";

	protected CompanyTest2() {
		super(owlfile, obdafile);
	}

//	@Override
//	public void setUp() throws Exception {
//
//
//		/*
//		 * Initializing and H2 database with the stock exchange data
//		 */
//		// String driver = "org.h2.Driver";
//		String url = "jdbc:h2:mem:questjunitdb";
//		String username = "fish";
//		String password = "fish";
//
//		fac = OBDADataFactoryImpl.getInstance();
//
//		conn = DriverManager.getConnection(url, username, password);
//
//		Statement st = conn.createStatement();
//
//		//with simple h2 test we enter in a second nested left join and it fails
//		FileReader reader = new FileReader("resources/optional/company-h2.sql");
//		BufferedReader in = new BufferedReader(reader);
//		StringBuilder bf = new StringBuilder();
//		String line = in.readLine();
//		while (line != null) {
//			bf.append(line);
//			line = in.readLine();
//		}
//
//		st.executeUpdate(bf.toString());
//		conn.commit();
//
//		// Loading the OWL file
//		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//		ontology = manager.loadOntologyFromOntologyDocument((new File(owlfile)));
//
//		// Loading the OBDA data
//		obdaModel = fac.parse();
//
//		ModelIOManager ioManager = new ModelIOManager(obdaModel);
//		ioManager.load(obdafile);
//
//	}
//
//	@Override
//	public void tearDown() throws Exception {
//		try {
//			dropTables();
//			conn.close();
//		} catch (Exception e) {
//			log.debug(e.getMessage());
//		}
//	}
//
//	private void dropTables() throws SQLException, IOException {
//
//		Statement st = conn.createStatement();
//
//		FileReader reader = new FileReader("resources/optional/drop-company.sql");
//		BufferedReader in = new BufferedReader(reader);
//		StringBuilder bf = new StringBuilder();
//		String line = in.readLine();
//		while (line != null) {
//			bf.append(line);
//			line = in.readLine();
//		}
//
//		st.executeUpdate(bf.toString());
//		st.close();
//		conn.commit();
//	}

	private void runTests() throws Exception {

		OntopOWLStatement st = conn.createStatement();

		
		String queryEx =  "PREFIX : <http://it.unibz.krdb/obda/test/company#> SELECT * WHERE"
				+ "{  ?v ?w  ?x } ";
//		String query = "PREFIX : <http://it.unibz.krdb/obda/test/company#> SELECT * WHERE"
//				+ "{ :A a :Company .  OPTIONAL  {  ?x :companyName :A .  ?x :depName :HR .  OPTIONAL{?z :depId ?x }}}";
		String query = "PREFIX : <http://it.unibz.krdb/obda/test/company#> SELECT * WHERE"
				+ "{ ?x a :Company .  OPTIONAL  {?w :companyName ?x .  ?w :depName :HR .  OPTIONAL{?z :depId ?x }}}";	
			
		
		
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
			OWLObject ind2 = rs2.getOWLIndividual("z");
			
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
