package org.semanticweb.ontop.sesame.tests.general;

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

import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.query.Binding;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.ontop.model.OBDADataFactory;
import org.semanticweb.ontop.model.OBDAModel;
import org.semanticweb.ontop.owlrefplatform.core.QuestConstants;
import org.semanticweb.ontop.owlrefplatform.core.QuestPreferences;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLResultSet;
import org.semanticweb.ontop.owlrefplatform.owlapi3.QuestOWLStatement;
import org.semanticweb.ontop.sesame.SesameVirtualRepo;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;



import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import junit.framework.TestCase;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregatesTestWP5 extends TestCase {

	private OBDADataFactory fac;

	//Logger log = LoggerFactory.getLogger(this.getClass());
	
	private OBDAModel obdaModel;
	private OWLOntology owlontology;
	Model mappings;
	QuestPreferences pref;
	RepositoryConnection con;

	
	final String owlfile = "src/test/resources/wp5/optique-demo.ttl";
	final String mappingfile = "src/test/resources/wp5/mapping-1.ttl";

	
	
	
	
	
	
	
	
	@Override
	public void setUp() throws Exception {
	
		
	// create owlontology from file
	OWLOntologyManager man = OWLManager.createOWLOntologyManager();
	OWLOntologyIRIMapper iriMapper = new AutoIRIMapper(	new File(owlfile).getParentFile(), false);
	man.addIRIMapper(iriMapper);
	try{
		owlontology = man
				.loadOntologyFromOntologyDocument(new File(owlfile));
	} catch (Exception e) {

		e.printStackTrace();
		assertFalse(false);
	}
		
	/*
	 * Mappings
	 */
	try {
		// create RDF Graph from ttl file
		RDFParser parser = Rio.createParser(RDFFormat.TURTLE);
		InputStream in = new FileInputStream(mappingfile);
		URL documentUrl = new URL("file://" + mappingfile);
		mappings = new LinkedHashModel();
		StatementCollector collector = new StatementCollector(mappings);
		parser.setRDFHandler(collector);
		parser.parse(in, documentUrl.toString());

	} catch (Exception e) {

		e.printStackTrace();
		assertFalse(true);
	}
		
	/*
	 * PREFERENCES
	 */
		
	pref = new QuestPreferences();
	pref.setCurrentValueOf(QuestPreferences.ABOX_MODE,	QuestConstants.VIRTUAL);
//	pref.setCurrentValueOf(QuestPreferences.REWRITE, "true");
//	pref.setCurrentValueOf(QuestPreferences.REFORMULATION_TECHNIQUE, QuestConstants.TW);
	// set jdbc params in config
	pref.setCurrentValueOf(QuestPreferences.DBNAME, "siemens-test");
	pref.setCurrentValueOf(QuestPreferences.JDBC_URL,"jdbc:postgresql://10.254.11.17:5432/siemens-test");
	pref.setCurrentValueOf(QuestPreferences.DBUSER, "postgres");
	pref.setCurrentValueOf(QuestPreferences.DBPASSWORD, "postgres");
	pref.setCurrentValueOf(QuestPreferences.JDBC_DRIVER,"org.postgresql.Driver");	
		
	Repository repo;
	try {
		repo = new SesameVirtualRepo("virtualExample2", owlontology, mappings, pref);
		/*
		 * Repository must be always initialized first
		 */
		repo.initialize();

		/*
		 * Get the repository connection
		 */
		con = repo.getConnection();

	} catch (Exception e) {
		e.printStackTrace();
		assertFalse(true);
	}
		
	}

	@Override
	public void tearDown() throws Exception {
		try {
		//	dropTables();
			con.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void executeQueryAssertResults(String query,  int expectedRows) throws Exception {
		try {
			TupleQuery tupleQuery = con.prepareTupleQuery(QueryLanguage.SPARQL,	query);
			TupleQueryResult result = tupleQuery.evaluate();

			while (result.hasNext()) {
				for (Binding binding : result.next()) {
					System.out.print(binding.getValue() + ", ");
				}
				System.out.println();
			}
			result.close();

		} catch (Exception e) {
			e.printStackTrace();
			assertFalse(true);
		}
	}

	
	
	private void runTests(QuestPreferences p, String query, int expectedvalue) throws Exception {
		try {
			executeQueryAssertResults(query,  expectedvalue);
		} catch (Exception e) {
			assertFalse(true);
			throw e;
		} finally {
			con.close();
		
		}
	}
	
	public void executeQueryAssertValue(String query, QuestOWLStatement st, int expectedValue) throws Exception {
		QuestOWLResultSet rs = st.executeTuple(query);
		rs.nextRow();
		int count = rs.getCount();
		System.out.print(rs.getSignature().get(0));
		System.out.print("=" + count);
		System.out.print(" ");
		System.out.println();
		rs.close();
		assertEquals(expectedValue, count);
	}
	
	
	
	
	


	/*
	 * 	test queries
	 */

	
	
	
	public void testAggrCount1() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String query = "select ?x (count(?state) as ?c) where { ?x <http://www.siemens.com/demo#hasState> ?state.} GROUP BY ?x LIMIT 1 ";

		runTests(p,query,1);

	}
	
	public void testMap() throws Exception {

		QuestPreferences p = new QuestPreferences();
		p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");
		p.setCurrentValueOf(QuestPreferences.OPTIMIZE_TBOX_SIGMA, "true");
		String query = "select *  WHERE { ?measurement a <http://www.siemens.com/demo#Measurement>; " +
									"<http://www.siemens.com/optique/demo#measuredBy> <http://www.optique-project.eu/resource/sensor/57>; " +
									"   <http://www.siemens.com/optique/demo#hasTS> ?ts;" +
									"   <http://www.siemens.com/optique/demo#hasValue> ?value. " +     
									"   FILTER(?ts > \"2009-01-05T00:00:00.000\"^^xsd:dateTime && ?ts < \"2009-01-07T15:00:00.000\"^^xsd:dateTime)}  ";

		runTests(p,query,1);

	}
	
	
	
   public void testAggrCount4() throws Exception {
	   QuestPreferences p = new QuestPreferences();
	   p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
	   p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");

	   String query = "select ?ts ( COUNT (?et) AS ?c)  WHERE {\n" +
			   "?message a <http://www.siemens.com/demo#Message>;\n" +
			   "   <http://www.siemens.com/optique/demo#hasTS> ?ts;\n" +
			   "   <http://www.siemens.com/optique/demo#hasEventtext> ?et.\n" +
			   "   FILTER(?ts > \"2009-01-01T00:00:00Z\"^^xsd:dateTime && ?ts < \"2009-01-07T23:59:59Z\"^^xsd:dateTime)\n" +
			   "}\n" +
			   "GROUP BY ?ts\n" +
			   "LIMIT 20";

	   runTests(p,query,1);

   }

    public void testAggrCount5() throws Exception {
        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.OPTIMIZE_EQUIVALENCES, "true");

        String query = "PREFIX siemens: <http://www.siemens.com/demo#>" +
                "SELECT ?assembly (COUNT (?message) AS ?eventFrequency) WHERE { \n" +
                "   ?message a siemens:Message ;\n" +
                "      siemens:forAssembly ?assembly;\n" +
                "      siemens:messageHasTS ?ts .\n" +
                "   FILTER(?ts > \"2009-01-01T00:00:00Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime> " +
                "          && ?ts < \"2009-01-03T23:00:00Z\"^^<http://www.w3.org/2001/XMLSchema#dateTime>)\n" +
                "}\n" +
                "GROUP BY ?assembly";

        runTests(p,query,1);

    }

    public void testQS4() throws Exception {
        QuestPreferences p = new QuestPreferences();
        p.setCurrentValueOf(QuestPreferences.ABOX_MODE, QuestConstants.VIRTUAL);
        p.setCurrentValueOf(QuestPreferences.SQL_GENERATE_REPLACE, QuestConstants.FALSE);
        p.setCurrentValueOf(QuestPreferences.OBTAIN_FULL_METADATA, QuestConstants.FALSE);
        String query
                = "PREFIX siemens: <http://www.siemens.com/demo#> \n"
                + "SELECT ?assembly (COUNT (?message) AS ?eventFrequency) WHERE { \n"
                + "   ?message a siemens:Message ;\n"
                + "      siemens:hasCategory <http://www.optique-project.eu/resource/event-category/EventCategory-28>;\n"
                + "      siemens:forAssembly ?assembly;\n"
                + "      siemens:messageHasTS ?ts .\n"
                + "   FILTER(?ts > '2005-01-01T00:00:00Z'^^xsd:dateTime && ?ts < '2012-01-03T23:00:00Z'^^xsd:dateTime)\n"
                + "}\n"
                + "GROUP BY ?assembly\n"
                + "ORDER BY ?eventFrequency\n"
                + "LIMIT 10";
        runTests(p, query, 10);
    }


}
